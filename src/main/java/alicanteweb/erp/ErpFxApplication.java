package alicanteweb.erp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;

public class ErpFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        // Espera y obtiene el contexto iniciado por ErpLauncher
        context = ErpLauncher.getContext();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ajusta la ruta al FXML principal según tu proyecto (ej: /fxml/Main.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        // Permite que los controladores sean beans Spring
        loader.setControllerFactory(context::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ERP");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Cierra Spring y la plataforma JavaFX
        if (context != null) {
            SpringApplication.exit(context, () -> 0);
            context.close();
        }
        Platform.exit();
    }
}