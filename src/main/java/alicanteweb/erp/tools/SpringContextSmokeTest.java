package alicanteweb.erp.tools;

import alicanteweb.erp.ErpLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringContextSmokeTest {
    private static final Logger log = LoggerFactory.getLogger(SpringContextSmokeTest.class);

    public static void main(String[] args) {
        log.info("Iniciando verificación de contexto Spring (sin JavaFX)...");
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ErpLauncher.class)
                .headless(true)
                .web(WebApplicationType.NONE)
                .run(args)) {
            log.info("Contexto Spring iniciado correctamente. Beans cargados: {}", ctx.getBeanDefinitionCount());
            System.out.println("SPRING_OK");
        } catch (Throwable t) {
            log.error("Fallo iniciando contexto Spring: ", t);
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
