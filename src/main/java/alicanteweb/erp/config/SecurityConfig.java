package alicanteweb.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuración de seguridad para la aplicación
 * Incluye el encoder de contraseñas BCrypt
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean de BCryptPasswordEncoder para cifrado de contraseñas
     * Fuerza 12 (más seguro que el default de 10)
     *
     * Uso:
     * - Cifrar: passwordEncoder.encode("password")
     * - Verificar: passwordEncoder.matches("password", hash)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}


