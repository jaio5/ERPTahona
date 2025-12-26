package alicanteweb.erp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(scanBasePackages = "alicanteweb.erp")
public class ErpLauncher extends Application {
    private static final Logger log = LoggerFactory.getLogger(ErpLauncher.class);
    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        log.info("Inicializando contexto de Spring Boot...");
        try {
            springContext = new org.springframework.boot.builder.SpringApplicationBuilder(ErpLauncher.class)
                .headless(false)
                .web(org.springframework.boot.WebApplicationType.NONE)
                .run();
            log.info("Contexto de Spring Boot inicializado correctamente");
        } catch (Exception e) {
            log.error("Error al inicializar el contexto de Spring Boot", e);
            throw e;
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("Iniciando aplicaciÃ³n JavaFX...");
        try {
            // Cargar pantalla de login primero
            log.info("Cargando vista de login: /ui/login.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setTitle("ERP PanaderÃ­a Tahona - Iniciar SesiÃ³n");
            stage.setScene(scene);
            stage.setResizable(false); // Login no redimensionable
            stage.centerOnScreen();
            stage.show();

            log.info("Pantalla de login cargada correctamente");
        } catch (Throwable t) {
            log.error("Error crÃ­tico al iniciar la aplicaciÃ³n JavaFX", t);
            // Guardar traza en archivo para diagnÃ³stico local
            try {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                String trace = sw.toString();
                Path logPath = Path.of("run_error.log");
                Files.writeString(logPath, trace);
                log.error("Traza de error guardada en: {}", logPath.toAbsolutePath());
            } catch (IOException ioe) {
                log.error("Error al escribir run_error.log", ioe);
            }
            // Re-lanzar para que la aplicaciÃ³n falle con excepciÃ³n visible
            throw t;
        }
    }

    @Override
    public void stop() {
        log.info("Deteniendo aplicaciÃ³n...");
        if (springContext != null) {
            springContext.close();
            log.info("Contexto de Spring cerrado");
        }
        Platform.exit();
        log.info("AplicaciÃ³n detenida");
    }

    public static void main(String[] args) {
        log.info("Lanzando aplicaciÃ³n ERP PanaderÃ­a Tahona...");
        Application.launch(ErpLauncher.class, args);
    }
}

