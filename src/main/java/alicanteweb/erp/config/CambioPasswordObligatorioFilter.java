package alicanteweb.erp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Mientras el usuario tenga pendiente el cambio de contraseña obligatorio
 * (marcado en sesión al iniciar sesión), bloquea la navegación redirigiendo
 * a /web/cambiar-password. El atributo se limpia al completar el cambio.
 */
@Component
public class CambioPasswordObligatorioFilter extends OncePerRequestFilter {

    private static final String[] RUTAS_PERMITIDAS = {
            "/web/cambiar-password", "/web/login", "/web/logout",
            "/css/", "/js/", "/webjars/", "/favicon", "/error",
            "/app.css", "/app.js", "/app-config.js", "/erp-core.js", "/erp-crud.js"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("requiereCambioPassword"))
                && !rutaPermitida(request.getRequestURI())) {
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Debe cambiar su contraseña antes de continuar\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/web/cambiar-password");
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean rutaPermitida(String uri) {
        for (String ruta : RUTAS_PERMITIDAS) {
            if (uri.startsWith(ruta)) {
                return true;
            }
        }
        return false;
    }
}
