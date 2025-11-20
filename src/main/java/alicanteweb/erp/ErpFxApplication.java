package alicanteweb.erp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import java.io.IOException;

public class ErpFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Inicializa Spring sin arrancar servidor web
        springContext = new SpringApplicationBuilder(ErpApplication.class)
                .properties("spring.main.web-application-type=none")
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar FXML y usar Spring como controller factory
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(beanClass -> springContext.getBean(beanClass));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar FXML", e);
        }

        primaryStage.setTitle("ERP");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Cerrar contexto Spring correctamente al salir
        if (springContext != null) {
            springContext.close();
        }
        javafx.application.Platform.exit();
    }
}
