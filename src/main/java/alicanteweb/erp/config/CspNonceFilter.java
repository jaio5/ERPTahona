package alicanteweb.erp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CspNonceFilter extends OncePerRequestFilter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        byte[] nonceBytes = new byte[18];
        RANDOM.nextBytes(nonceBytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);

        request.setAttribute("cspNonce", nonce);
        // Sin hosts externos: todos los assets (bootstrap, tom-select, alpine, chart.js, Inter)
        // se sirven como webjars. 'unsafe-eval' sigue siendo necesario porque el build CDN de
        // Alpine.js evalúa expresiones; solo aplica a scripts ya permitidos por 'self'/nonce.
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; "
                        + "script-src 'self' 'nonce-" + nonce + "' 'unsafe-eval'; "
                        + "style-src 'self' 'unsafe-inline'; "
                        + "font-src 'self'; "
                        + "img-src 'self' data:; "
                        + "connect-src 'self'; "
                        + "object-src 'none'; "
                        + "base-uri 'self'; "
                        + "form-action 'self'; "
                        + "frame-ancestors 'none'");
        filterChain.doFilter(request, response);
    }
}
