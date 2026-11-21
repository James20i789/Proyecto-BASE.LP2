package pe.edu.upeu.lp2_gp2.services;

import static pe.edu.upeu.lp2_gp2.entity.EstadoPedido.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.lp2_gp2.dto.DetalleResponseDTO;
import pe.edu.upeu.lp2_gp2.dto.PedidoResponseDTO;
import pe.edu.upeu.lp2_gp2.dto.VentaDTO;
import pe.edu.upeu.lp2_gp2.entity.DetallePedido;
import pe.edu.upeu.lp2_gp2.entity.Pedido;
import pe.edu.upeu.lp2_gp2.entity.Persona;
import pe.edu.upeu.lp2_gp2.entity.Producto;
import pe.edu.upeu.lp2_gp2.exceptions.InsufficienteStockException;
import pe.edu.upeu.lp2_gp2.exceptions.ResouceNotFoundException;
import pe.edu.upeu.lp2_gp2.payment.MercadoPagoClient;
import pe.edu.upeu.lp2_gp2.repository.PedidoRepository;
import pe.edu.upeu.lp2_gp2.repository.PersonaRepository;
import pe.edu.upeu.lp2_gp2.repository.ProductoRepository;

@Service
public class PedidoServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(PedidoServiceImpl.class);
    private static final Duration CHECKOUT_TIME = Duration.ofMinutes(25);
    private static final Duration WEBHOOK_GRACE = Duration.ofMinutes(2);
    private final PedidoRepository orders;
    private final ProductoRepository products;
    private final PersonaRepository customers;
    private final MercadoPagoClient provider;
    private final TransactionTemplate transactions;

    public PedidoServiceImpl(PedidoRepository orders, ProductoRepository products, PersonaRepository customers,
                             MercadoPagoClient provider, TransactionTemplate transactions) {
        this.orders = orders;
        this.products = products;
        this.customers = customers;
        this.provider = provider;
        this.transactions = transactions;
    }

    public PedidoResponseDTO checkout(Long customerId, VentaDTO request) {
        if (request.idPersona() != null && !customerId.equals(request.idPersona())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El pedido debe pertenecer a tu cuenta.");
        }
        provider.requireConfigured();

        Map<Long, Integer> quantities = new TreeMap<>();
        for (var item : request.items()) {
            if (quantities.putIfAbsent(item.idProducto(), item.cantidad()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agrupa las cantidades de cada producto.");
            }
        }

        Long orderId = transactions.execute(status -> {
            Persona customer = customers.lockById(customerId)
                    .orElseThrow(() -> new ResouceNotFoundException("Cliente no encontrado."));
            var existing = orders.findByPersonaIdPersonaAndClaveCompra(customerId, request.intento().toString());
            if (existing.isPresent()) {
                Pedido order = existing.get();
                if (!sameItems(order, quantities)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El intento corresponde a otro carrito.");
                }
                return order.getIdPedido();
            }
            Instant now = Instant.now();
            if (orders.countByPersonaIdPersonaAndEstadoAndVenceEnAfter(customerId, PENDIENTE, now) >= 2) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya tienes dos pagos abiertos. Revisa tus pedidos antes de iniciar otro.");
            }
            return reserve(customer, request.intento(), quantities, now).getIdPedido();
        });

        return transactions.execute(status -> {
            Pedido order = lockOrder(orderId);
            if (order.getPreferenciaPago() == null && order.getEstado() == PENDIENTE
                    && order.getVenceEn().isAfter(Instant.now())) {
                var lines = order.getDetalle().stream().map(this::paymentLine).toList();
                var preference = provider.createPreference(UUID.fromString(order.getReferenciaPago()), orderId,
                        order.getVenceEn(), lines);
                order.setPreferenciaPago(preference.id());
                order.setCheckoutUrl(preference.url());
            }
            return response(order);
        });
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO get(Long customerId, Long orderId) {
        return orders.findByIdPedidoAndPersonaIdPersona(orderId, customerId).map(this::response)
                .orElseThrow(() -> new ResouceNotFoundException("Pedido no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> list(Long customerId) {
        return orders.findTop30ByPersonaIdPersonaOrderByIdPedidoDesc(customerId).stream()
                .map(this::response).toList();
    }

    @Transactional
    public void applyPayment(MercadoPagoClient.Payment payment) {
        if (payment.reference() == null || !provider.belongsToMerchant(payment)
                || payment.amount() == null || payment.status() == null) {
            return;
        }
        var found = orders.findByReferenciaPago(payment.reference());
        if (found.isEmpty()) {
            return;
        }
        Pedido order = found.get();
        if (payment.amount().compareTo(order.getTotal()) != 0) {
            LOG.error("El importe de Mercado Pago difiere del total del pedido {}", order.getIdPedido());
            return;
        }

        boolean samePayment = Objects.equals(order.getTransaccionPago(), payment.id());
        switch (payment.status()) {
            case "approved" -> approve(order, payment, samePayment);
            case "refunded", "charged_back" -> {
                if (samePayment) {
                    order.setEstado("refunded".equals(payment.status()) ? REEMBOLSADO : CONTRACARGO);
                    order.setEstadoPago(payment.status());
                } else if (order.getEstado() == VENCIDO && "refunded".equals(payment.status())) {
                    order.setTransaccionPago(payment.id());
                    order.setEstado(REEMBOLSADO);
                    order.setEstadoPago("refunded");
                }
            }
            case "pending", "in_process", "authorized", "rejected", "cancelled" -> {
                if (order.getEstado() == PENDIENTE) {
                    order.setEstadoPago(payment.status());
                }
            }
            default -> LOG.info("Estado de Mercado Pago no procesado para el pedido {}", order.getIdPedido());
        }
    }

    private void approve(Pedido order, MercadoPagoClient.Payment payment, boolean samePayment) {
        if (order.getEstado() == PENDIENTE) {
            order.setTransaccionPago(payment.id());
            order.setEstado(PAGADO);
            order.setEstadoPago("approved");
        } else if (order.getEstado() == VENCIDO || !samePayment) {
            boolean refunded = provider.refund(payment.id());
            if (order.getEstado() == VENCIDO) {
                order.setTransaccionPago(payment.id());
                order.setEstado(refunded ? REEMBOLSADO : REEMBOLSO_PENDIENTE);
                order.setEstadoPago(refunded ? "refunded" : "refund_pending");
            }
        }
        if (Objects.equals(order.getTransaccionPago(), payment.id()) && order.getEstado() == PAGADO
                && payment.refundedAmount() != null && payment.refundedAmount().signum() > 0) {
            order.setEstado(REEMBOLSO_PARCIAL);
        }
    }

    @Scheduled(fixedDelayString = "${payment.expiry-scan-ms:60000}")
    public void expireReservations() {
        Instant cutoff = Instant.now().minus(WEBHOOK_GRACE);
        List<Long> due = orders.due(PENDIENTE, cutoff, PageRequest.of(0, 100));
        for (Long id : due) {
            try {
                transactions.executeWithoutResult(status -> {
                    Pedido order = lockOrder(id);
                    if (order.getEstado() == PENDIENTE && !order.getVenceEn().isAfter(cutoff)) {
                        var details = new TreeMap<Long, Integer>();
                        order.getDetalle().forEach(detail -> details.put(detail.getProducto().getIdProducto(),
                                detail.getCantidad()));
                        details.forEach((productId, quantity) -> {
                            Producto product = lockProduct(productId);
                            product.setStock(product.getStock() + quantity);
                        });
                        order.setEstado(VENCIDO);
                    }
                });
            } catch (RuntimeException ex) {
                LOG.error("No se pudo liberar la reserva del pedido {}", id, ex);
            }
        }
    }

    private Pedido reserve(Persona customer, UUID key, Map<Long, Integer> quantities, Instant now) {
        var order = new Pedido();
        order.setPersona(customer);
        order.setEstado(PENDIENTE);
        order.setFechaPedido(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        order.setClaveCompra(key.toString());
        order.setReferenciaPago(UUID.randomUUID().toString());
        order.setVenceEn(now.plus(CHECKOUT_TIME));
        BigDecimal total = BigDecimal.ZERO;
        for (var item : quantities.entrySet()) {
            Producto product = lockProduct(item.getKey());
            int quantity = item.getValue();
            if (product.getStock() == null || product.getStock() < quantity) {
                throw new InsufficienteStockException("Stock insuficiente para " + product.getNombre() + ".");
            }
            if (product.getPrecio() == null || product.getPrecio().signum() <= 0
                    || product.getPrecio().stripTrailingZeros().scale() > 2) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un producto tiene un precio inválido.");
            }
            BigDecimal price = product.getPrecio().setScale(2, RoundingMode.UNNECESSARY);
            product.setStock(product.getStock() - quantity);
            var detail = new DetallePedido();
            detail.setPedido(order);
            detail.setProducto(product);
            detail.setCantidad(quantity);
            detail.setPrecioUnitario(price);
            detail.setSubTotal(price.multiply(BigDecimal.valueOf(quantity)));
            order.getDetalle().add(detail);
            total = total.add(detail.getSubTotal());
        }
        order.setTotal(total);
        return orders.save(order);
    }

    private MercadoPagoClient.Line paymentLine(DetallePedido detail) {
        Producto product = detail.getProducto();
        return new MercadoPagoClient.Line(Long.toString(product.getIdProducto()), product.getNombre(),
                detail.getCantidad(), detail.getPrecioUnitario(), "PEN");
    }

    private boolean sameItems(Pedido order, Map<Long, Integer> quantities) {
        return order.getDetalle().size() == quantities.size() && order.getDetalle().stream().allMatch(
                detail -> Objects.equals(quantities.get(detail.getProducto().getIdProducto()), detail.getCantidad()));
    }

    private Pedido lockOrder(Long id) {
        return orders.lockById(id).orElseThrow(() -> new ResouceNotFoundException("Pedido no encontrado."));
    }

    private Producto lockProduct(Long id) {
        return products.lockById(id).orElseThrow(() -> new ResouceNotFoundException("Producto no encontrado."));
    }

    private PedidoResponseDTO response(Pedido order) {
        String url = null;
        boolean processing = switch (Objects.toString(order.getEstadoPago(), "")) {
            case "in_process", "pending", "authorized" -> true;
            default -> false;
        };
        if (order.getEstado() == PENDIENTE && order.getVenceEn() != null
                && order.getVenceEn().isAfter(Instant.now()) && !processing) {
            url = order.getCheckoutUrl();
        }
        var details = order.getDetalle().stream().map(detail -> new DetalleResponseDTO(
                detail.getProducto().getNombre(), detail.getCantidad(), detail.getPrecioUnitario(),
                detail.getSubTotal())).toList();
        String paymentStatus = order.getEstado() == PENDIENTE && order.getVenceEn() != null
                && order.getPreferenciaPago() == null ? "SIN_PREFERENCIA" : order.getEstadoPago();
        return new PedidoResponseDTO(order.getIdPedido(), order.getPersona().getNombre(), order.getTotal(),
                order.getEstado().name(), order.getFechaPedido(), details, order.getVenceEn(), url, paymentStatus);
    }
}
