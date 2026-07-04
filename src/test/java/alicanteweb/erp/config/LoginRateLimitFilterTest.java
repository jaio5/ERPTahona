package alicanteweb.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimitFilterTest {

    private static final int MAX_ATTEMPTS = 10;

    private MockHttpServletRequest loginPost(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/login");
        request.setServletPath("/web/login");
        request.setRemoteAddr(ip);
        return request;
    }

    private int filtrar(LoginRateLimitFilter filter, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void bloqueaTrasSuperarElMaximoDeIntentosPorIp() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            assertThat(filtrar(filter, loginPost("10.0.0.1")))
                    .as("intento %d dentro del límite debe pasar", i)
                    .isEqualTo(200);
        }
        assertThat(filtrar(filter, loginPost("10.0.0.1")))
                .as("intento %d debe ser rechazado", MAX_ATTEMPTS + 1)
                .isEqualTo(429);
        assertThat(filtrar(filter, loginPost("10.0.0.1")))
                .as("los intentos durante el cooldown siguen rechazados")
                .isEqualTo(429);
    }

    @Test
    void elBloqueoDeUnaIpNoAfectaAOtras() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        for (int i = 0; i <= MAX_ATTEMPTS; i++) {
            filtrar(filter, loginPost("10.0.0.1"));
        }
        assertThat(filtrar(filter, loginPost("10.0.0.1"))).isEqualTo(429);
        assertThat(filtrar(filter, loginPost("10.0.0.2"))).isEqualTo(200);
    }

    @Test
    void noLimitaPeticionesQueNoSonPostDeLogin() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        MockHttpServletRequest get = new MockHttpServletRequest("GET", "/web/login");
        get.setServletPath("/web/login");
        get.setRemoteAddr("10.0.0.3");
        for (int i = 0; i < MAX_ATTEMPTS * 2; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(get, response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }
}
