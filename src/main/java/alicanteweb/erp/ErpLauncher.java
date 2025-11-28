// java
package alicanteweb.erp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(scanBasePackages = "alicanteweb.erp")
public class ErpLauncher extends Application {
    private static ConfigurableApplicationContext springContext;
    @Override
    public void init() {
        springContext = new org.springframework.boot.builder.SpringApplicationBuilder(ErpLauncher.class)
            .headless(false)
            .web(org.springframework.boot.WebApplicationType.NONE)
            .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_panel.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = null;
        try {
            root = loader.load();
        } catch (Throwable t) {
            // Guardar traza en archivo para diagnóstico local
            try {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                String trace = sw.toString();
                Path log = Path.of("run_error.log");
                Files.writeString(log, trace);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            // Re-lanzar para que la aplicación falle con excepción visible
            throw t;
        }
        Scene scene = new Scene(root);
        stage.setTitle("ERP Panadería");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(ErpLauncher.class, args);
    }
}
