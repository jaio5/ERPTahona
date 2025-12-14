package alicanteweb.erp.controller.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainPanelController {
    private static final Logger log = LoggerFactory.getLogger(MainPanelController.class);

    private final ApplicationContext springContext;

    @FXML private Button btnClientes;
    @FXML private Button btnProveedores;
    @FXML private Button btnArticulos;
    @FXML private Button btnAlbaranes;
    @FXML private Button btnFacturas;
    @FXML private Button btnAlmacenes;
    @FXML private Button btnVerifactu;
    @FXML private StackPane mainContent;
    @FXML private GridPane gridModulos;

    public MainPanelController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        // Solo añadir animaciones hover, no sobrescribir onAction
        addButtonAnimation(btnClientes);
        addButtonAnimation(btnProveedores);
        addButtonAnimation(btnArticulos);
        addButtonAnimation(btnAlbaranes);
        addButtonAnimation(btnFacturas);
        addButtonAnimation(btnAlmacenes);
        addButtonAnimation(btnVerifactu);
    }

    private void addButtonAnimation(Button button) {
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.08);
            st.setToY(1.08);
            st.play();
        });
        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void cargarVistaModulo(String fxmlPath) {
        try {
            log.info("=====================================");
            log.info("INTENTANDO CARGAR VISTA: {}", fxmlPath);
            log.info("=====================================");

            java.net.URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("No se encontró el archivo: " + fxmlPath);
            }
            log.info("Archivo encontrado en: {}", resourceUrl);

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            loader.setControllerFactory(springContext::getBean);

            log.info("Cargando FXML...");
            Parent view = loader.load();
            log.info("FXML cargado OK. Tipo de vista: {}", view.getClass().getSimpleName());

            log.info("Reemplazando contenido de mainContent...");
            mainContent.getChildren().setAll(view);

            log.info("=====================================");
            log.info("VISTA CARGADA EXITOSAMENTE: {}", fxmlPath);
            log.info("Elementos en mainContent: {}", mainContent.getChildren().size());
            log.info("=====================================");
        } catch (Exception e) {
            log.error("=====================================");
            log.error("ERROR CRÍTICO CARGANDO VISTA: {}", fxmlPath);
            log.error("Tipo de error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            log.error("=====================================", e);

            // Mostrar alert al usuario
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error cargando vista");
                alert.setContentText("No se pudo cargar la vista: " + fxmlPath + "\n" + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void mostrarMenuPrincipal() {
        mainContent.getChildren().setAll(gridModulos);
    }

    @FXML
    public void onClientes() {
        log.info(">>> BOTÓN CLIENTES PRESIONADO <<<");
        cargarVistaModulo("/ui/clientes_panel.fxml");
    }

    @FXML
    public void onProveedores() {
        log.info(">>> BOTÓN PROVEEDORES PRESIONADO <<<");
        cargarVistaModulo("/ui/proveedores_panel.fxml");
    }

    @FXML
    public void onArticulos() {
        log.info(">>> BOTÓN ARTÍCULOS PRESIONADO <<<");
        cargarVistaModulo("/ui/articulos_panel.fxml");
    }

    @FXML
    public void onAlbaranes() {
        log.info(">>> BOTÓN ALBARANES PRESIONADO <<<");
        cargarVistaModulo("/ui/albaranes_panel.fxml");
    }

    @FXML
    public void onFacturas() {
        log.info(">>> BOTÓN FACTURAS PRESIONADO <<<");
        cargarVistaModulo("/ui/facturas_panel.fxml");
    }

    @FXML
    public void onAlmacenes() {
        log.info(">>> BOTÓN ALMACENES PRESIONADO <<<");
        cargarVistaModulo("/ui/almacenes_panel.fxml");
    }

    @FXML
    public void onVerifactu() {
        log.info(">>> BOTÓN VERIFACTU PRESIONADO <<<");
        cargarVistaModulo("/ui/verifactu_panel.fxml");
    }
}
