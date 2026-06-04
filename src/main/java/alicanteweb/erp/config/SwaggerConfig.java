package alicanteweb.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ERP Tahona API")
                .description("API REST para ERP de panadería y obrador. Endpoints para producción, trazabilidad, reparto y devoluciones.")
                .version("1.0.0"));
    }
}
