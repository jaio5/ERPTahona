package alicanteweb.erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de seguridad para la aplicación
 * Incluye un encoder PBKDF2 personalizado para evitar problemas con la implementación por defecto
 */
@Configuration
public class SecurityConfig {

    // Secret opcional para PBKDF2 (no obligatorio). No lo almacenes en el repo.
    @Value("${security.pbkdf2.secret:}")
    private String secret;

    // Iteraciones recomendadas (ajustar según política y rendimiento)
    @Value("${security.pbkdf2.iterations:185000}")
    private int iterations;

    // Longitud del hash en bits
    @Value("${security.pbkdf2.hashWidth:256}")
    private int hashWidth;

    /**
     * Bean de PasswordEncoder basado en PBKDF2 (implementación local segura)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Default: pbkdf2 (CustomPbkdf2PasswordEncoder)
        String idForEncode = "pbkdf2";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", new CustomPbkdf2PasswordEncoder(secret, iterations, hashWidth));
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder(idForEncode, encoders);
        // Establecer BCrypt como encoder por defecto para matches de contraseñas no prefijadas (legacy)
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegating;
    }
}
