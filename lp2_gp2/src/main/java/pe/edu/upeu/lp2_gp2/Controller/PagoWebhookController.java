package pe.edu.upeu.lp2_gp2.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.lp2_gp2.payment.MercadoPagoClient;
import pe.edu.upeu.lp2_gp2.services.PedidoServiceImpl;

@RestController
@RequestMapping("/api/v1/pagos/mercadopago")
public class PagoWebhookController {
    private final MercadoPagoClient provider;
    private final PedidoServiceImpl orders;

    public PagoWebhookController(MercadoPagoClient provider, PedidoServiceImpl orders) {
        this.provider = provider;
        this.orders = orders;
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void webhook(@RequestParam(name = "data.id", required = false) String id,
                        @RequestParam(name = "type", required = false) String type,
                        @RequestHeader(name = "x-request-id", required = false) String requestId,
                        @RequestHeader(name = "x-signature", required = false) String signature) {
        if (!provider.validSignature(id, requestId, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Firma de notificación inválida.");
        }
        if (type == null || "payment".equals(type)) {
            orders.applyPayment(provider.getPayment(id));
        }
    }
}
