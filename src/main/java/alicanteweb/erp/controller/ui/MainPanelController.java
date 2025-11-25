package alicanteweb.erp.controller.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Objects;

@Controller
public class MainPanelController {

    private final ApplicationContext context;

    @FXML
    private StackPane contentPane;

    public MainPanelController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void onNavFactura() {
        loadView("/ui/facturas.fxml");
    }

    @FXML
    public void onNavClientes() {
        loadView("/ui/clientes.fxml");
    }

    @FXML
    public void onNavArticulos() {
        loadView("/ui/articulos.fxml");
    }

    @FXML
    public void onExit() {
        // Close action handled by the platform - keep placeholder
        System.exit(0);
    }

    @FXML
    public void onAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "ERP Tahona - Versión de escritorio");
        a.setHeaderText("Acerca de");
        a.showAndWait();
    }

    private void loadView(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resource)));
            loader.setControllerFactory(context::getBean);
            Node node = loader.load();
            if (contentPane != null) {
                contentPane.getChildren().setAll(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo cargar la vista: " + resource + "\n" + e.getMessage());
            a.setHeaderText("Error al cargar vista");
            a.showAndWait();
        }
    }
}

