package pe.edu.upeu.lp2_gp2.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MercadoPagoClientTests {
    @Test
    void sendsCheckoutPreferenceAndReadsProviderResponses() {
        var builder = RestClient.builder();
        var mock = MockRestServiceServer.bindTo(builder).build();
        var client = new MercadoPagoClient(builder, "test-token", "test-secret", "123", "https://example.test/");
        UUID reference = UUID.randomUUID();
        mock.expect(requestTo("https://api.mercadopago.com/checkout/preferences"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(jsonPath("$.external_reference").value(reference.toString()))
                .andExpect(jsonPath("$.items[0].currency_id").value("PEN"))
                .andExpect(jsonPath("$.items[0].unit_price").value(12.90))
                .andExpect(jsonPath("$.back_urls.success").value("https://example.test/pedido/8"))
                .andExpect(jsonPath("$.expiration_date_from").exists())
                .andExpect(jsonPath("$.expiration_date_to").value("2030-08-12T10:00:00.000+00:00"))
                .andExpect(jsonPath("$.notification_url").value(
                        "https://example.test/lp2_gp2/api/v1/pagos/mercadopago/webhook?source_news=webhooks"))
                .andRespond(withSuccess("""
                        {"id":"pref-123","init_point":"https://www.mercadopago.com.pe/checkout/v1/redirect"}
                        """, MediaType.APPLICATION_JSON));
        mock.expect(requestTo("https://api.mercadopago.com/v1/payments/123456"))
                .andExpect(method(HttpMethod.GET)).andRespond(withSuccess("""
                        {"id":123456,"status":"approved","currency_id":"PEN","collector_id":123,
                         "transaction_amount":25.80,"transaction_amount_refunded":0}
                        """, MediaType.APPLICATION_JSON));
        mock.expect(requestTo("https://api.mercadopago.com/v1/payments/123456/refunds"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("X-Idempotency-Key",
                        UUID.nameUUIDFromBytes("refund:123456".getBytes(StandardCharsets.UTF_8)).toString()))
                .andRespond(withSuccess("{\"status\":\"approved\"}", MediaType.APPLICATION_JSON));

        var preference = client.createPreference(reference, 8, Instant.parse("2030-08-12T10:00:00Z"),
                List.of(new MercadoPagoClient.Line("1", "Ceviche", 2, new BigDecimal("12.90"), "PEN")));
        assertThat(preference.id()).isEqualTo("pref-123");
        assertThat(client.belongsToMerchant(client.getPayment("123456"))).isTrue();
        assertThat(client.refund("123456")).isTrue();
        mock.verify();
    }
}
