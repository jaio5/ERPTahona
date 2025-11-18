package alicanteweb.erp.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        VBox leftMenu = new VBox(8);
        leftMenu.setPadding(new Insets(10));
        Label title = new Label("ERP Tahona - Cliente UI");
        Button clientesBtn = new Button("Clientes");
        Button articulosBtn = new Button("Artículos");
        Button smokeBtn = new Button("Test API (smoke)");

        clientesBtn.setMaxWidth(Double.MAX_VALUE);
        articulosBtn.setMaxWidth(Double.MAX_VALUE);
        smokeBtn.setMaxWidth(Double.MAX_VALUE);

        clientesBtn.setOnAction(e -> ClienteView.show());
        articulosBtn.setOnAction(e -> ArticuloView.show());
        smokeBtn.setOnAction(e -> {
            // Ejecutar prueba de humo en una nueva ventana (no bloqueante)
            new Thread(() -> {
                try {
                    ApiSmokeTest.main(new String[0]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        leftMenu.getChildren().addAll(title, clientesBtn, articulosBtn, smokeBtn);

        root.setLeft(leftMenu);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ERP JavaFX Client - Tahona");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
