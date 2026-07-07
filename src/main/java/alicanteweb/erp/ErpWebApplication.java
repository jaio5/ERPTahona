package alicanteweb.erp;

import alicanteweb.erp.config.VerifactuProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.Desktop;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackages = "alicanteweb.erp")
@EnableConfigurationProperties(VerifactuProperties.class)
@EnableScheduling
public class ErpWebApplication {

    private static final Logger log = LoggerFactory.getLogger(ErpWebApplication.class);
    private static ConfigurableApplicationContext springContext;

    @Value("${erp.web.open-browser.enabled:true}")
    private boolean openBrowserEnabled;

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }

    public static void main(String[] args) {
        log.info("Iniciando ERP Tahona en modo web...");
        springContext = crearContextoWeb(args, false);
    }

    private static ConfigurableApplicationContext crearContextoWeb(String[] args, boolean modoFallback) {
        Map<String, String> propiedadesPrevias = null;
        try {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(ErpWebApplication.class)
                    .headless(true)
                    .web(WebApplicationType.SERVLET);

            if (modoFallback) {
                log.warn("Arrancando ERP web en modo fallback con H2 en memoria");
                propiedadesPrevias = aplicarPropiedadesFallbackH2();
                builder.profiles("dev");
            }

            return builder.run(args);
        } catch (Exception e) {
            if (!modoFallback && permitirFallbackH2()) {
                log.error("Error iniciando ERP web con la base de datos configurada, reintentando con H2: {}", e.getMessage());
                return crearContextoWeb(args, true);
            }
            throw e;
        } finally {
            if (modoFallback && springContext == null && propiedadesPrevias != null) {
                restaurarPropiedades(propiedadesPrevias);
            }
        }
    }

    private static Map<String, String> aplicarPropiedadesFallbackH2() {
        Map<String, String> propiedades = new LinkedHashMap<>();
        propiedades.put("spring.datasource.url", "jdbc:h2:mem:webdev;DB_CLOSE_DELAY=-1;MODE=MySQL");
        propiedades.put("spring.datasource.driver-class-name", "org.h2.Driver");
        propiedades.put("spring.datasource.username", "sa");
        propiedades.put("spring.datasource.password", "");
        propiedades.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        propiedades.put("spring.jpa.hibernate.ddl-auto", "update");
        propiedades.put("spring.flyway.enabled", "false");

        Map<String, String> anteriores = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : propiedades.entrySet()) {
            anteriores.put(entry.getKey(), System.getProperty(entry.getKey()));
            System.setProperty(entry.getKey(), entry.getValue());
        }
        return anteriores;
    }

    private static void restaurarPropiedades(Map<String, String> propiedadesPrevias) {
        for (Map.Entry<String, String> entry : propiedadesPrevias.entrySet()) {
            if (entry.getValue() == null) {
                System.clearProperty(entry.getKey());
            } else {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private static boolean permitirFallbackH2() {
        String profiles = firstNonBlank(
                System.getProperty("spring.profiles.active"),
                System.getenv("SPRING_PROFILES_ACTIVE"),
                System.getProperty("spring.profiles.default"),
                System.getenv("SPRING_PROFILES_DEFAULT"),
                "dev"
        );

        String normalized = profiles.toLowerCase();
        boolean prod = normalized.contains("prod") || normalized.contains("production");
        if (prod) {
            log.error("Fallback H2 deshabilitado porque el perfil activo es de produccion: {}", profiles);
            return false;
        }

        String fallbackEnabled = firstNonBlank(
                System.getProperty("erp.fallback-h2.enabled"),
                System.getenv("ERP_FALLBACK_H2_ENABLED")
        );
        if (!fallbackEnabled.isBlank() && !Boolean.parseBoolean(fallbackEnabled)) {
            log.error("Fallback H2 deshabilitado por configuracion. Configura MySQL o elimina ERP_FALLBACK_H2_ENABLED=false.");
            return false;
        }

        log.warn("Fallback H2 permitido para perfil no productivo: {}", profiles);
        return true;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    @EventListener(ApplicationReadyEvent.class)
    public void abrirWebAlArrancar(ApplicationReadyEvent event) {
        if (!openBrowserEnabled) {
            return;
        }
        ApplicationContext applicationContext = event.getApplicationContext();
        if (!(applicationContext instanceof ServletWebServerApplicationContext webServerContext)) {
            return;
        }
        // En un servidor/contenedor (headless) no hay navegador que abrir: el
        // lanzador de escritorio (iniciar-erp.cmd) es quien abre el navegador.
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        int port = webServerContext.getWebServer().getPort();
        String url = "http://localhost:" + port + "/web";
        try {
            boolean esWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else if (esWindows) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else {
                log.info("Interfaz web disponible en {}", url);
                return;
            }
            log.info("Interfaz web abierta en {}", url);
        } catch (Exception e) {
            log.warn("No se pudo abrir el navegador automaticamente. Abre manualmente {}", url);
        }
    }
}
