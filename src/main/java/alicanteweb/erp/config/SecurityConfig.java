package alicanteweb.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * ConfiguraciÃ³n de seguridad para la aplicaciÃ³n
 * Incluye el encoder de contraseÃ±as BCrypt
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean de BCryptPasswordEncoder para cifrado de contraseÃ±as
     * Fuerza 12 (mÃ¡s seguro que el default de 10)
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


