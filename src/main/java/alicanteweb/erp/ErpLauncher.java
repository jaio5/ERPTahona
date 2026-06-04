package alicanteweb.erp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Punto de entrada del ERP Panadería Tahona.
 *
 * <p>Integra Spring Boot 3 con JavaFX 21. El ciclo de vida es:</p>
 * <ol>
 *   <li>{@link #init()} — arranca el contexto de Spring Boot</li>
 *   <li>{@link #start(Stage)} — carga la pantalla de login</li>
 *   <li>{@link #stop()} — cierra el contexto al salir</li>
 * </ol>
 *
 * <p>Si la conexión a la base de datos falla, se intenta un fallback con H2
 * en memoria para permitir arranque en entornos sin MySQL.</p>
 *
 * @see SpringBootApplication
 * @see Application
 */
public class ErpLauncher extends Application {

    private static final Logger log = LoggerFactory.getLogger(ErpLauncher.class);
    private static final String LOGIN_FXML = "/ui/login.fxml";

    private static ConfigurableApplicationContext springContext;

    /** Expone el contexto de Spring para que los controladores JavaFX puedan obtener beans. */
    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }

    private static boolean isJavaFxMode(String[] args) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if ("--javafx".equalsIgnoreCase(arg) || "--desktop".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init() {
        log.info("Iniciando contexto de Spring Boot...");
        springContext = crearContexto(false);
        log.info("Contexto de Spring Boot listo");
    }

    /**
     * Crea el contexto de Spring. Si falla con la BD configurada, reintenta en modo
     * H2 en memoria para permitir ejecución local sin MySQL.
     */
    private ConfigurableApplicationContext crearContexto(boolean modoFallback) {
        Map<String, String> propiedadesPrevias = null;
        try {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(ErpWebApplication.class)
                    .headless(false)
                    .web(WebApplicationType.NONE);

            if (modoFallback) {
                log.warn("Arrancando en modo fallback con H2 en memoria");
                propiedadesPrevias = aplicarPropiedadesFallbackH2();
                builder.profiles("dev");
            }

            return builder.run();
        } catch (Exception e) {
            if (!modoFallback && permitirFallbackH2()) {
                log.error("Error iniciando contexto principal, intentando fallback H2: {}", e.getMessage());
                return crearContexto(true);
            }
            if (!modoFallback) {
                log.error("Error iniciando contexto principal sin fallback permitido", e);
            }
            log.error("Error en modo fallback, la aplicación no puede continuar", e);
            throw e;
        } finally {
            if (modoFallback && springContext == null && propiedadesPrevias != null) {
                restaurarPropiedades(propiedadesPrevias);
            }
        }
    }

    private Map<String, String> aplicarPropiedadesFallbackH2() {
        Map<String, String> propiedades = new LinkedHashMap<>();
        propiedades.put("spring.datasource.url", "jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
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

    private void restaurarPropiedades(Map<String, String> propiedadesPrevias) {
        for (Map.Entry<String, String> entry : propiedadesPrevias.entrySet()) {
            if (entry.getValue() == null) {
                System.clearProperty(entry.getKey());
            } else {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean permitirFallbackH2() {
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("Cargando pantalla de login...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            stage.setTitle("ERP Panadería Tahona — Iniciar Sesión");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            log.info("Aplicación iniciada");
        } catch (Throwable t) {
            log.error("Error crítico al iniciar la interfaz gráfica", t);
            guardarTrazaError(t);
            throw t;
        }
    }

    @Override
    public void stop() {
        log.info("Cerrando aplicación...");
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        if (isJavaFxMode(args)) {
            Application.launch(ErpLauncher.class, args);
            return;
        }

        log.info("Iniciando ERP Tahona en modo web...");
        ErpWebApplication.main(args);
    }

    /** Guarda la traza del error en {@code run_error.log} para diagnóstico. */
    private void guardarTrazaError(Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            Path logPath = Path.of("run_error.log");
            Files.writeString(logPath, sw.toString());
            log.info("Traza de error guardada en: {}", logPath.toAbsolutePath());
        } catch (IOException ioe) {
            log.warn("No se pudo escribir run_error.log: {}", ioe.getMessage());
        }
    }
}
