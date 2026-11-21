package pe.edu.upeu.lp2_gp2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pe.edu.upeu.lp2_gp2.entity.Producto;
import pe.edu.upeu.lp2_gp2.payment.MercadoPagoClient;
import pe.edu.upeu.lp2_gp2.repository.PedidoRepository;
import pe.edu.upeu.lp2_gp2.repository.ProductoRepository;
import pe.edu.upeu.lp2_gp2.services.PedidoServiceImpl;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "server.servlet.session.cookie.secure=false"
})
@AutoConfigureMockMvc
class Lp2Gp2ApplicationTests {
    private static final String AUTH = "/api/v1/auth";
    private static final String ORDERS = "/api/v1/pedidos";
    private static final String WEBHOOK = "/api/v1/pagos/mercadopago/webhook";
    private static final String PASSWORD = "Clave de prueba 123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductoRepository products;
    @Autowired PedidoRepository orders;
    @Autowired PedidoServiceImpl service;
    @MockitoBean MercadoPagoClient gateway;

    @Test
    void registersLogsInRestoresAndRevokesSession() throws Exception {
        mvc.perform(post(AUTH + "/login").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        register("susanap");
        Cookie csrf = csrf();
        mvc.perform(post(AUTH + "/login").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType("application/json").content(body(Map.of("usuario", "susanap", "password", "otra"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos."));

        User customer = login("susanap");
        mvc.perform(get(AUTH + "/session").session(customer.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario").value("susanap"))
                .andExpect(jsonPath("$.token").doesNotExist());
        mvc.perform(post(AUTH + "/logout").session(customer.session()).cookie(customer.csrf())
                        .header("X-XSRF-TOKEN", customer.csrf().getValue()))
                .andExpect(status().isNoContent());
        assertThat(customer.session().isInvalid()).isTrue();
        mvc.perform(get(ORDERS)).andExpect(status().isUnauthorized());
    }

    @Test
    void limitsRepeatedLoginAttempts() throws Exception {
        Cookie csrf = csrf();
        for (int count = 1; count <= 9; count++) {
            mvc.perform(post(AUTH + "/login").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                            .contentType("application/json").content(body(Map.of("usuario", "pruebas_auth", "password", "otra"))))
                    .andExpect(status().is(count <= 8 ? 401 : 429));
        }
    }

    @Test
    void checksOrderOwnershipAndConfirmsOnlyVerifiedPayments() throws Exception {
        User first = customer("primercliente");
        User second = customer("segundocliente");
        long product = product();
        stubPreference();
        var request = checkoutRequest(product);

        MvcResult result = checkout(first, request).andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(25.80))
                .andExpect(jsonPath("$.estado").value("PENDIENTE")).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).path("idPedido").asLong();
        checkout(first, request).andExpect(status().isCreated()).andExpect(jsonPath("$.idPedido").value(id));
        assertThat(products.findById(product).orElseThrow().getStock()).isEqualTo(3);
        verify(gateway, times(1)).createPreference(any(), anyLong(), any(), anyList());
        mvc.perform(get(ORDERS + "/" + id).session(second.session())).andExpect(status().isNotFound());
        mvc.perform(get(ORDERS).session(second.session())).andExpect(jsonPath("$.length()").value(0));

        String reference = orders.findById(id).orElseThrow().getReferenciaPago();
        mvc.perform(post(WEBHOOK).param("data.id", "987").header("x-request-id", "event-1")
                        .header("x-signature", "incorrecta"))
                .andExpect(status().isUnauthorized());
        verify(gateway, never()).getPayment(anyString());

        payment("987", reference, "24.80", "approved");
        webhook("987");
        mvc.perform(get(ORDERS + "/" + id).session(first.session())).andExpect(jsonPath("$.estado").value("PENDIENTE"));

        payment("987", reference, "25.80", "approved");
        webhook("987");
        webhook("987");
        mvc.perform(get(ORDERS + "/" + id).session(first.session())).andExpect(jsonPath("$.estado").value("PAGADO"));
        assertThat(products.findById(product).orElseThrow().getStock()).isEqualTo(3);
        verify(gateway, never()).refund("987");

        payment("988", reference, "25.80", "approved");
        when(gateway.refund("988")).thenReturn(true);
        webhook("988");
        verify(gateway).refund("988");
        mvc.perform(get(ORDERS + "/" + id).session(first.session())).andExpect(jsonPath("$.estado").value("PAGADO"));
    }

    @Test
    void releasesExpiredReservationsAndRefundsLatePayments() throws Exception {
        User customer = customer("clientevence");
        long product = product();
        stubPreference();
        MvcResult result = checkout(customer, checkoutRequest(product)).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).path("idPedido").asLong();
        var order = orders.findById(id).orElseThrow();
        order.setVenceEn(Instant.now().minusSeconds(180));
        orders.save(order);
        service.expireReservations();
        service.expireReservations();
        assertThat(products.findById(product).orElseThrow().getStock()).isEqualTo(5);

        payment("989", order.getReferenciaPago(), "25.80", "approved");
        when(gateway.refund("989")).thenReturn(true);
        webhook("989");
        webhook("989");
        verify(gateway).refund("989");
        mvc.perform(get(ORDERS + "/" + id).session(customer.session()))
                .andExpect(jsonPath("$.estado").value("REEMBOLSADO"));
    }

    private User customer(String username) throws Exception {
        register(username);
        return login(username);
    }

    private void register(String username) throws Exception {
        Cookie csrf = csrf();
        mvc.perform(post(AUTH + "/registro").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType("application/json").content(body(Map.of("usuario", username, "password", PASSWORD,
                                "nombre", "Cliente de pruebas", "email", username + "@example.test", "telefono", "912345678"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    private User login(String username) throws Exception {
        Cookie csrf = csrf();
        MvcResult result = mvc.perform(post(AUTH + "/login").cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType("application/json").content(body(Map.of("usuario", username, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn();
        Cookie rotated = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(rotated.getValue()).isNotEqualTo(csrf.getValue());
        return new User((MockHttpSession) result.getRequest().getSession(false), rotated);
    }

    private Cookie csrf() throws Exception {
        return mvc.perform(get(AUTH + "/csrf")).andExpect(status().isNoContent())
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
    }

    private long product() {
        return products.save(new Producto(0, "Ceviche", "Plato de prueba", new BigDecimal("12.90"), 5, null)).getIdProducto();
    }

    private void stubPreference() {
        when(gateway.createPreference(any(), anyLong(), any(), anyList())).thenReturn(
                new MercadoPagoClient.Preference("pref-test", "https://www.mercadopago.com.pe/checkout/v1/redirect"));
    }

    private String checkoutRequest(long product) throws Exception {
        return body(Map.of("intento", UUID.randomUUID().toString(),
                "items", java.util.List.of(Map.of("idProducto", product, "cantidad", 2))));
    }

    private org.springframework.test.web.servlet.ResultActions checkout(User user, String request) throws Exception {
        return mvc.perform(post(ORDERS).session(user.session()).cookie(user.csrf())
                .header("X-XSRF-TOKEN", user.csrf().getValue()).contentType("application/json").content(request));
    }

    private void payment(String id, String reference, String amount, String status) {
        var payment = new MercadoPagoClient.Payment(id, status, reference, new BigDecimal(amount),
                BigDecimal.ZERO, "PEN", "123");
        when(gateway.getPayment(id)).thenReturn(payment);
        when(gateway.belongsToMerchant(payment)).thenReturn(true);
        when(gateway.validSignature(id, "event-1", "firma-valida")).thenReturn(true);
    }

    private void webhook(String id) throws Exception {
        mvc.perform(post(WEBHOOK).param("data.id", id).param("type", "payment")
                        .header("x-request-id", "event-1").header("x-signature", "firma-valida"))
                .andExpect(status().isOk());
    }

    private String body(Map<String, ?> data) throws Exception {
        return json.writeValueAsString(data);
    }

    private record User(MockHttpSession session, Cookie csrf) {}
}
