package pe.edu.upeu.lp2_gp2.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class MercadoPagoClient {
    private static final Logger LOG = LoggerFactory.getLogger(MercadoPagoClient.class);
    private static final String WEBHOOK_PATH = "/lp2_gp2/api/v1/pagos/mercadopago/webhook";
    private static final DateTimeFormatter MERCADO_DATE = DateTimeFormatter
            .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSxxx", Locale.ROOT).withZone(ZoneOffset.UTC);
    private final RestClient client;
    private final String secret;
    private final String collectorId;
    private final String publicUrl;
    private final boolean configured;

    @Autowired
    public MercadoPagoClient(@Value("${payment.mercado-pago.access-token}") String token,
                             @Value("${payment.mercado-pago.webhook-secret}") String secret,
                             @Value("${payment.mercado-pago.collector-id}") String collectorId,
                             @Value("${payment.mercado-pago.public-url}") String publicUrl) {
        this(clientBuilder(), token, secret, collectorId, publicUrl);
    }

    MercadoPagoClient(RestClient.Builder builder, String token, String secret, String collectorId, String publicUrl) {
        this.secret = secret;
        this.collectorId = collectorId;
        this.publicUrl = publicUrl.replaceAll("/+$", "");
        this.configured = !token.isBlank() && !secret.isBlank() && collectorId.matches("[0-9]+")
                && isHttpsOrigin(this.publicUrl);

        this.client = builder.baseUrl("https://api.mercadopago.com")
                .defaultHeaders(headers -> headers.setBearerAuth(token)).build();
    }

    private static RestClient.Builder clientBuilder() {
        var timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(Duration.ofSeconds(4));
        timeouts.setReadTimeout(Duration.ofSeconds(6));
        return RestClient.builder().requestFactory(timeouts);
    }

    public void requireConfigured() {
        if (!configured) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El pago en línea no está disponible en este momento.");
        }
    }

    public Preference createPreference(UUID reference, long orderId, Instant expiresAt, List<Line> lines) {
        requireConfigured();
        String backUrl = publicUrl + "/pedido/" + orderId;
        var payload = Map.of(
                "items", lines,
                "external_reference", reference.toString(),
                "back_urls", Map.of("success", backUrl, "pending", backUrl, "failure", backUrl),
                "auto_return", "approved",
                "notification_url", publicUrl + WEBHOOK_PATH + "?source_news=webhooks",
                "expires", true,
                "expiration_date_from", MERCADO_DATE.format(Instant.now().minusSeconds(60)),
                "expiration_date_to", MERCADO_DATE.format(expiresAt),
                "payment_methods", Map.of("excluded_payment_types", List.of(Map.of("id", "ticket"), Map.of("id", "atm"))));
        try {
            var response = client.post().uri("/checkout/preferences")
                    .header("X-Idempotency-Key", reference.toString()).body(payload)
                    .retrieve().body(PreferenceResponse.class);
            String url = response == null ? null : response.initPoint();
            if (response == null || response.id() == null || !isCheckoutUrl(url)) {
                throw new RestClientException("Respuesta de preferencia inválida");
            }
            return new Preference(response.id(), url);
        } catch (RestClientException ex) {
            throw unavailable("crear la preferencia", ex);
        }
    }

    public Payment getPayment(String id) {
        requireConfigured();
        try {
            Payment payment = client.get().uri("/v1/payments/{id}", id).retrieve().body(Payment.class);
            if (payment == null || !id.equals(payment.id())) {
                throw new RestClientException("Respuesta de pago inválida");
            }
            return payment;
        } catch (RestClientException ex) {
            throw unavailable("consultar el pago", ex);
        }
    }

    public boolean belongsToMerchant(Payment payment) {
        return collectorId.equals(payment.collectorId()) && "PEN".equals(payment.currency());
    }

    public boolean refund(String paymentId) {
        String key = UUID.nameUUIDFromBytes(("refund:" + paymentId).getBytes(StandardCharsets.UTF_8)).toString();
        try {
            Refund response = client.post().uri("/v1/payments/{id}/refunds", paymentId)
                    .header("X-Idempotency-Key", key).body(Map.of()).retrieve().body(Refund.class);
            if (response == null || (!"approved".equals(response.status()) && !"in_process".equals(response.status())
                    && !"pending".equals(response.status()))) {
                throw new RestClientException("Respuesta de reembolso inválida");
            }
            return "approved".equals(response.status());
        } catch (RestClientException ex) {
            throw unavailable("reembolsar el pago", ex);
        }
    }

    public boolean validSignature(String paymentId, String requestId, String signature) {
        requireConfigured();
        if (paymentId == null || !paymentId.matches("[0-9]{1,24}") || requestId == null
                || !requestId.matches("[a-zA-Z0-9_-]{1,128}") || signature == null || signature.length() > 512) {
            return false;
        }
        Map<String, String> fields = new HashMap<>();
        for (String part : signature.split(",")) {
            String[] entry = part.strip().split("=", 2);
            if (entry.length != 2 || fields.putIfAbsent(entry[0].strip(), entry[1].strip()) != null) {
                return false;
            }
        }
        String ts = fields.getOrDefault("ts", "");
        String v1 = fields.getOrDefault("v1", "");
        if (!ts.matches("[0-9]{10,13}") || !v1.matches("[a-fA-F0-9]{64}")) {
            return false;
        }
        // El estado se vuelve a consultar en Mercado Pago y se aplica de forma idempotente.
        String manifest = "id:" + paymentId + ";request-id:" + requestId + ";ts:" + ts + ";";
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expected, HexFormat.of().parseHex(v1));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC-SHA256 no disponible", ex);
        }
    }

    private static boolean isHttpsOrigin(String value) {
        try {
            URI uri = URI.create(value);
            return secureUri(uri) && uri.getRawPath().isEmpty() && uri.getRawQuery() == null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean isCheckoutUrl(String value) {
        if (value == null || value.length() > 1800) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            return secureUri(uri) && uri.getHost().toLowerCase(Locale.ROOT)
                    .matches("([a-z0-9-]+\\.)*mercadopago\\.com(\\.pe)?");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean secureUri(URI uri) {
        return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                && uri.getRawUserInfo() == null && uri.getRawFragment() == null;
    }

    private static ResponseStatusException unavailable(String operation, RestClientException ex) {
        LOG.warn("Mercado Pago no pudo {} ({})", operation, ex.getClass().getSimpleName());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Mercado Pago no respondió correctamente. Intenta nuevamente en unos minutos.");
    }

    public record Line(String id, String title, int quantity,
                       @JsonProperty("unit_price") BigDecimal unitPrice,
                       @JsonProperty("currency_id") String currency) {}

    public record Preference(String id, String url) {}

    private record PreferenceResponse(String id, @JsonProperty("init_point") String initPoint) {}

    public record Payment(String id, String status,
                          @JsonProperty("external_reference") String reference,
                          @JsonProperty("transaction_amount") BigDecimal amount,
                          @JsonProperty("transaction_amount_refunded") BigDecimal refundedAmount,
                          @JsonProperty("currency_id") String currency,
                          @JsonProperty("collector_id") String collectorId) {}

    private record Refund(String status) {}
}
