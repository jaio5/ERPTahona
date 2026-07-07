package alicanteweb.erp.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de serialización JSON (Jackson).
 */
@Configuration
public class JacksonConfig {

    /**
     * Registra el módulo Hibernate de Jackson. Con {@code spring.jpa.open-in-view=false},
     * serializar una entidad JPA con una asociación o colección {@code LAZY} sin
     * inicializar lanzaba {@code LazyInitializationException} (HTTP 500). Con este
     * módulo, esos proxies no inicializados se serializan como {@code null} (no se
     * fuerza su carga: {@code FORCE_LAZY_LOADING} queda desactivado), evitando el
     * error en toda la API REST que devuelve entidades. Spring Boot detecta y
     * registra automáticamente cualquier bean de tipo {@code Module} en el ObjectMapper.
     */
    @Bean
    public Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }
}
