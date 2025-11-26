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
    public void onNavFactura() { loadView("/ui/facturas.fxml"); }
    @FXML
    public void onNavClientes() { loadView("/ui/clientes.fxml"); }
    @FXML
    public void onNavArticulos() { loadView("/ui/articulos.fxml"); }
    @FXML
    public void onNavProveedores() { loadView("/ui/proveedores.fxml"); }
    @FXML
    public void onNavAlmacenes() { loadView("/ui/almacenes.fxml"); }
    @FXML
    public void onNavAlbaranesVenta() { loadView("/ui/albaranes-venta.fxml"); }
    @FXML
    public void onNavFacturaLineas() { loadView("/ui/factura-lineas.fxml"); }
    @FXML
    public void onNavPedidos() { loadView("/ui/pedidos.fxml"); }
    @FXML
    public void onNavPedidoLineas() { loadView("/ui/pedido-lineas.fxml"); }
    @FXML
    public void onNavDireccionesEnvio() { loadView("/ui/direccionesenvio.fxml"); }
    @FXML
    public void onNavVerifactuEvidence() { loadView("/ui/verifactu-evidence.fxml"); }

    @FXML
    public void onExit() { System.exit(0); }

    @FXML
    public void onAbout() { showInfo("ERP Tahona - Versión de escritorio", "Acerca de"); }
    @FXML
    public void onConfiguracion() { showInfo("Configuración no implementada todavía.", "Configuración"); }
    @FXML
    public void onManual() { showInfo("Manual de usuario no disponible todavía.", "Manual de Usuario"); }

    public void showHome() {
        if (contentPane != null) contentPane.getChildren().clear();
    }

    private void loadView(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resource)));
            loader.setControllerFactory(context::getBean);
            Node node = loader.load();
            Object ctl = loader.getController();
            if (ctl instanceof alicanteweb.erp.controller.MainControllerAware awareCtl) {
                awareCtl.setMainPanelController(this);
            }
            if (contentPane != null) {
                contentPane.getChildren().setAll(node);
            }
        } catch (IOException e) {
            log.error("No se pudo cargar la vista: {}", resource, e);
            showError("No se pudo cargar la vista: " + resource + "\n" + e.getMessage(), "Error al cargar vista");
        }
    }

    private void showInfo(String message, String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private void showError(String message, String header) {
        Alert a = new Alert(Alert.AlertType.ERROR, message);
        a.setHeaderText(header);
        a.showAndWait();
    }
}
