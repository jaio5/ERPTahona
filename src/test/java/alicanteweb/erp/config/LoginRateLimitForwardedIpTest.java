package alicanteweb.erp.config;

import alicanteweb.erp.ErpWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el supuesto documentado en deploy/Caddyfile: detrás del reverse proxy,
 * con server.forward-headers-strategy=native (RemoteIpValve de Tomcat), el
 * LoginRateLimitFilter ve la IP real del cliente (X-Forwarded-For) y no la del
 * proxy. Sin esto, todos los clientes compartirían el mismo contador y el
 * bloqueo de uno bloquearía a todos.
 *
 * El test habla con un Tomcat real (RANDOM_PORT) desde 127.0.0.1, que cae en la
 * lista de proxies internos de confianza del RemoteIpValve, igual que la red
 * interna del compose desde la que conecta Caddy.
 */
@SpringBootTest(classes = ErpWebApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.forward-headers-strategy=native")
class LoginRateLimitForwardedIpTest {

    private static final int MAX_ATTEMPTS = 10;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void elRateLimiterCuentaPorLaIpDelXForwardedFor() {
        CsrfSession csrf = obtenerCsrf();

        // 10 intentos "del mismo cliente" (misma X-Forwarded-For): pasan todos
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            ResponseEntity<String> respuesta = intentoLogin(csrf, "203.0.113.10");
            assertThat(respuesta.getStatusCode().value())
                    .as("intento %d dentro del límite no debe ser 429", i)
                    .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        }

        // El intento 11 del mismo cliente queda bloqueado
        assertThat(intentoLogin(csrf, "203.0.113.10").getStatusCode().value())
                .as("el intento %d del mismo cliente debe ser 429", MAX_ATTEMPTS + 1)
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        // Otro cliente (otra X-Forwarded-For) desde el mismo socket (el proxy)
        // NO está bloqueado: el filtro distingue clientes, no ve la IP del proxy
        assertThat(intentoLogin(csrf, "203.0.113.11").getStatusCode().value())
                .as("otro cliente detrás del mismo proxy no debe estar bloqueado")
                .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    private ResponseEntity<String> intentoLogin(CsrfSession csrf, String ipCliente) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.COOKIE, csrf.cookie());
        headers.add("X-Forwarded-For", ipCliente);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "usuario-inexistente");
        form.add("password", "clave-incorrecta");
        form.add("_csrf", csrf.token());

        return rest.postForEntity("/web/login", new HttpEntity<>(form, headers), String.class);
    }

    private CsrfSession obtenerCsrf() {
        ResponseEntity<String> pagina = rest.getForEntity("/web/login", String.class);
        String setCookie = pagina.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(c -> c.startsWith("XSRF-TOKEN="))
                .findFirst()
                .orElse(null);
        assertThat(setCookie)
                .as("el GET del login debe emitir la cookie XSRF-TOKEN")
                .isNotNull();
        String cookie = setCookie.split(";", 2)[0];
        String token = cookie.substring(cookie.indexOf('=') + 1);
        return new CsrfSession(cookie, token);
    }

    private record CsrfSession(String cookie, String token) {
    }
}
