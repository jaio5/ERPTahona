package alicanteweb.erp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TestLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label l = new Label("ERP - Test UI OK");
        StackPane root = new StackPane(l);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ERP - Test Launcher");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

