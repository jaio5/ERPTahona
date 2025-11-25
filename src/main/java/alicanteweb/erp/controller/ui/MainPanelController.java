package alicanteweb.erp.controller.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Objects;

@Controller
public class MainPanelController {

    private static final Logger log = LoggerFactory.getLogger(MainPanelController.class);

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

    public void showHome() {
        if (contentPane != null) contentPane.getChildren().clear();
    }

    private void loadView(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resource)));
            // Usar siempre la factory de Spring para inyectar el controlador
            loader.setControllerFactory(context::getBean);

            Node node = loader.load();

            // Después de cargar, obtener el controlador y pasar la referencia al main si implementa MainControllerAware
            Object ctl = loader.getController();
            if (ctl instanceof alicanteweb.erp.controller.MainControllerAware awareCtl) {
                awareCtl.setMainPanelController(this);
            }

            if (contentPane != null) {
                contentPane.getChildren().setAll(node);
            }
        } catch (IOException e) {
            log.error("No se pudo cargar la vista: {}", resource, e);
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo cargar la vista: " + resource + "\n" + e.getMessage());
            a.setHeaderText("Error al cargar vista");
            a.showAndWait();
        }
    }

    @FXML
    public void onNavProveedores() {
        loadView("/ui/proveedores.fxml");
    }

    @FXML
    public void onNavAlmacenes() {
        loadView("/ui/almacenes.fxml");
    }

    @FXML
    public void onNavAlbaranesVenta() {
        loadView("/ui/albaranes-venta.fxml");
    }

    @FXML
    public void onNavFacturaLineas() {
        loadView("/ui/factura-lineas.fxml");
    }

    @FXML
    public void onNavPedidos() {
        loadView("/ui/pedidos.fxml");
    }

    @FXML
    public void onNavPedidoLineas() {
        loadView("/ui/pedido-lineas.fxml");
    }

    @FXML
    public void onNavDireccionesEnvio() {
        loadView("/ui/direccionesenvio.fxml");
    }

    @FXML
    public void onNavVerifactuEvidence() {
        loadView("/ui/verifactu-evidence.fxml");
    }

    @FXML
    public void onConfiguracion() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Configuración no implementada todavía.");
        a.setHeaderText("Configuración");
        a.showAndWait();
    }

    @FXML
    public void onManual() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Manual de usuario no disponible todavía.");
        a.setHeaderText("Manual de Usuario");
        a.showAndWait();
    }
}
