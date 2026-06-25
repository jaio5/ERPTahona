package alicanteweb.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CspNonceFilterTest {

    @Test
    void generaNoncePorPeticionYUnaPoliticaSinDirectivasInseguras() throws Exception {
        CspNonceFilter filter = new CspNonceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            String nonce = (String) req.getAttribute("cspNonce");
            assertThat(nonce).isNotBlank();
        });

        String policy = response.getHeader("Content-Security-Policy");
        String scriptPolicy = policy.substring(
                policy.indexOf("script-src"),
                policy.indexOf(';', policy.indexOf("script-src")));
        assertThat(policy)
                .contains("script-src 'self' 'nonce-")
                .contains("object-src 'none'");
        // 'unsafe-eval' is required for Alpine.js CDN build (uses eval() internally)
        // 'unsafe-inline' is still prohibited; nonce mechanism is used instead
        assertThat(scriptPolicy)
                .doesNotContain("'unsafe-inline'")
                .contains("'unsafe-eval'");
    }
}
