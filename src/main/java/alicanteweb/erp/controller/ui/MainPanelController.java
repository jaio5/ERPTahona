package alicanteweb.erp.controller.ui;

import alicanteweb.erp.controller.MainControllerAware;
import javafx.application.Platform;
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
    public void initialize() {
        // Puedes añadir aquí la lógica de inicialización si es necesaria
    }

    // --- Métodos de Navegación --- 
    @FXML public void onNavFactura() { loadView("/ui/facturas.fxml"); }
    @FXML public void onNavClientes() { loadView("/ui/clientes.fxml"); }
    @FXML public void onNavArticulos() { loadView("/ui/articulos.fxml"); }
    @FXML public void onNavProveedores() { loadView("/ui/proveedores.fxml"); }
    @FXML public void onNavAlmacenes() { loadView("/ui/almacenes.fxml"); }
    @FXML public void onNavAlbaranesVenta() { loadView("/ui/albaranes-venta.fxml"); }
    @FXML public void onNavFacturaLineas() { loadView("/ui/factura-lineas.fxml"); }
    @FXML public void onNavPedidos() { loadView("/ui/pedidos.fxml"); }
    @FXML public void onNavPedidoLineas() { loadView("/ui/pedido-lineas.fxml"); }
    @FXML public void onNavDireccionesEnvio() { loadView("/ui/direccionesenvio.fxml"); }
    @FXML public void onNavVerifactuEvidence() { loadView("/ui/verifactu-evidence.fxml"); }

    // --- Métodos de Menú --- 
    @FXML public void onExit() { Platform.exit(); }
    @FXML public void onAbout() { showInfo("ERP Tahona - Versión 1.0", "Acerca de"); }
    @FXML public void onConfiguracion() { showInfo("Función de configuración no implementada.", "Aviso"); }
    @FXML public void onManual() { showInfo("El manual de usuario estará disponible en futuras versiones.", "Aviso"); }

    // --- Métodos de Carga de Vistas --- 
    public void showHome() {
        if (contentPane.getChildren().isEmpty()) return; // Ya está en Home
        Platform.runLater(() -> contentPane.getChildren().clear());
    }

    private void loadView(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resourcePath)));
            loader.setControllerFactory(context::getBean); // Dejar que Spring cree los controladores
            Node viewNode = loader.load();

            // Inyectar el controlador principal a los sub-controladores que lo necesiten
            Object controller = loader.getController();
            if (controller instanceof MainControllerAware) {
                ((MainControllerAware) controller).setMainPanelController(this);
            }
            
            // Actualizar la UI en el hilo de JavaFX para evitar problemas de concurrencia
            Platform.runLater(() -> contentPane.getChildren().setAll(viewNode));

        } catch (IOException e) {
            log.error("Fallo al cargar la vista FXML: {}", resourcePath, e);
            showError("Error al cargar la vista: " + resourcePath + ". Causa: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al cargar el controlador para: {}", resourcePath, e);
            showError("No se pudo inicializar el controlador para la vista: " + resourcePath + ". Verifique las dependencias del controlador.");
        }
    }

    // --- Helpers de UI ---
    private void showInfo(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Aplicación");
        alert.setHeaderText("Ha ocurrido un error grave");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
