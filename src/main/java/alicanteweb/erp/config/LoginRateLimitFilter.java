package alicanteweb.erp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting para el endpoint de login.
 * 10 intentos por minuto por IP.
 * Superado ese límite, se bloquea la IP durante 1 minuto.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000;   // 1 minuto
    private static final long COOLDOWN_MS = 60_000;  // 1 minuto de bloqueo

    private final ConcurrentHashMap<String, AttemptWindow> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"/web/login".equals(request.getServletPath()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        long now = System.currentTimeMillis();
        AttemptWindow window = attemptsByIp.computeIfAbsent(ip, k -> new AttemptWindow(now));

        synchronized (window) {
            if (now - window.blockedUntil > 0) {
                // Ventana caducada o cooldown terminado: resetear
                window.reset(now);
            }
            if (window.count >= MAX_ATTEMPTS) {
                if (window.blockedUntil == 0) {
                    window.blockedUntil = now + COOLDOWN_MS;
                }
                if (now < window.blockedUntil) {
                    response.setStatus(429);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiados intentos. Espera un minuto.\"}");
                    return;
                }
                // Cooldown terminado: resetear
                window.reset(now);
            }
            window.count++;
            window.lastAttempt = now;
            if (now - window.windowStart > WINDOW_MS) {
                window.windowStart = now;
                window.count = 1;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptWindow {
        long windowStart;
        int count;
        long lastAttempt;
        long blockedUntil;

        AttemptWindow(long now) {
            this.windowStart = now;
            this.count = 0;
            this.lastAttempt = now;
            this.blockedUntil = 0;
        }

        void reset(long now) {
            this.windowStart = now;
            this.count = 0;
            this.lastAttempt = now;
            this.blockedUntil = 0;
        }
    }
}
