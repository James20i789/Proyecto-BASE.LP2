package pe.edu.upeu.lp2_gp2;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.lp2_gp2.payment.MercadoPagoClient;

class MercadoPagoSignatureTests {
    @Test
    void validatesMercadoPagoManifest() {
        var client = new MercadoPagoClient("test-token", "test-webhook-secret", "123",
                "https://example.test");
        String signature = "ts=1732222222,v1=0e1a90dafc53ed1e2fb4fcc349b8ee1c0b0c9ca0248fc4133504659899623a04";
        assertThat(client.validSignature("123456", "request-1", signature)).isTrue();
        assertThat(client.validSignature("123457", "request-1", signature)).isFalse();
        assertThat(client.validSignature("123456", "request-2", signature)).isFalse();
        assertThat(client.validSignature("123456", "request-1", signature + ",ts=1732222222")).isFalse();
    }
}
