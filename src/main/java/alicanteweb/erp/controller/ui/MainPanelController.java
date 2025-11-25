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
import java.util.Map;
import java.util.Objects;

@Controller
public class MainPanelController {

    private static final Logger log = LoggerFactory.getLogger(MainPanelController.class);

    private final ApplicationContext context;

    @FXML
    private StackPane contentPane;

    // Mapear recursos FXML sin fx:controller a sus controladores Spring
    private static final Map<String, Class<?>> CONTROLLER_MAP = Map.of(
            "/ui/facturas.fxml", alicanteweb.erp.controller.FacturaController.class,
            "/ui/factura-lineas.fxml", alicanteweb.erp.controller.FacturaLineaController.class,
            "/ui/factura-albaran.fxml", alicanteweb.erp.controller.FacturaAlbaranController.class,
            "/ui/direccionesenvio.fxml", alicanteweb.erp.controller.DireccionesenvioNewController.class,
            "/ui/pedidos.fxml", alicanteweb.erp.controller.PedidoController.class,
            "/ui/pedido-lineas.fxml", alicanteweb.erp.controller.PedidoLineaController.class,
            "/ui/proveedores.fxml", alicanteweb.erp.controller.ProveedoreController.class,
            "/ui/verifactu-evidence.fxml", alicanteweb.erp.controller.VerifactuEvidenceController.class
    );

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

            // Si sabemos que el fxml no define fx:controller, asignamos el bean correspondiente explícitamente
            Class<?> controllerClass = CONTROLLER_MAP.get(resource);
            if (controllerClass != null) {
                Object controllerBean = context.getBean(controllerClass);
                // si el controller implementa MainControllerAware, pasar la referencia
                if (controllerBean instanceof alicanteweb.erp.controller.MainControllerAware aware) {
                    aware.setMainPanelController(this);
                }
                loader.setController(controllerBean);
            } else {
                // Dejar que FXMLLoader use la factory para inyectar controladores declarados en el FXML
                loader.setControllerFactory(context::getBean);
            }

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
}
