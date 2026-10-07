package pe.edu.upeu.lp2_gp2.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.DetalleResponseDTO;
import pe.edu.upeu.lp2_gp2.dto.ItemVentaDTO;
import pe.edu.upeu.lp2_gp2.dto.PedidoResponseDTO;
import pe.edu.upeu.lp2_gp2.dto.VentaDTO;
import pe.edu.upeu.lp2_gp2.entity.DetallePedido;
import pe.edu.upeu.lp2_gp2.entity.Pedido;
import pe.edu.upeu.lp2_gp2.entity.Persona;
import pe.edu.upeu.lp2_gp2.entity.Producto;
import pe.edu.upeu.lp2_gp2.exceptions.InsufficienteStockException;
import pe.edu.upeu.lp2_gp2.exceptions.ResouceNotFoundException;
import pe.edu.upeu.lp2_gp2.repository.PedidoRepository;
import pe.edu.upeu.lp2_gp2.repository.PersonaRepository;
import pe.edu.upeu.lp2_gp2.repository.ProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImpl implements IPedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PersonaRepository personaRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ProductoRepository productoRepository, PersonaRepository personaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional
    public PedidoResponseDTO realizarPedido(VentaDTO dto) {

        // CABECERA DE PEDIDO - 01 (VALIDAR CLIENTE)
        Persona persona = personaRepository.findById(dto.idPersona())
                .orElseThrow(() -> new ResouceNotFoundException("Cliente no encontrado"));

        // PROCESO PEDIDO - 02 (CREAMOS LA CABECERA DEL PEDIDO)
        Pedido pedido = new Pedido();
        pedido.setPersona(persona);
        pedido.setEstado("PENDIENTE");
        pedido.setFechaPedido(LocalDateTime.now());

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal totalPedido = BigDecimal.ZERO;


        // PROCESAMOS CADA ITEM DEL CARRITO - 03
        for (ItemVentaDTO item : dto.items()) {
            Producto producto = productoRepository.findById(item.idProducto())
                    .orElseThrow(() -> new ResouceNotFoundException("Producto" + item.idProducto() + "NO EXISTE"));

        // VALIDAR STOCK - 04
        if (producto.getStock() < item.cantidad()) {
            throw new InsufficienteStockException("Stock insuficiente:   " + producto.getNombre());
        }

        // ACTUALIZAR EL STOCK EN BD - 05
        producto.setStock(producto.getStock() - item.cantidad());
        productoRepository.save(producto);

        // CREAMOS EL DETALLE | PEDIDO - 06
        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(item.cantidad());
        detalle.setPrecioUnitario(producto.getPrecio());

        // CALCULO DE PRODUCTO | DetallePedido
        BigDecimal subTotal = producto.getPrecio().multiply(new BigDecimal((item.cantidad())));
        detalle.setSubTotal(subTotal);

        // AGREGAR DETALLES, SEGÚN EL CALCULO DEL PEDIDO
        detalles.add(detalle);
        totalPedido = totalPedido.add(subTotal);

        }

        // GUARDAR: Pedido y detalles - 07
        pedido.setTotal(totalPedido);
        pedido.setDetalle(detalles);
        Pedido pedidoguardado = pedidoRepository.save(pedido);

        // CONVERTIMOS LOS DETALLES GUARDADOS (MAPEAMOS) A DTO´S PARA LA RESPUESTA - 08
        List<DetalleResponseDTO>productosResponse = pedidoguardado.getDetalle().stream()
                .map(d->new DetalleResponseDTO(
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubTotal()
                )).toList();

        // DEVOLVER LA RESPUESTA COMPLETA - 09
        return new PedidoResponseDTO(
                pedidoguardado.getIdPedido(),
                persona.getNombre(),
                pedidoguardado.getTotal(),
                pedidoguardado.getEstado(),
                pedidoguardado.getFechaPedido(),
                productosResponse
        );

    }

}

