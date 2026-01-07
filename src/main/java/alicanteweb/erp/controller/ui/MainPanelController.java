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

    @FXML private StackPane contentArea;
    @FXML private javafx.scene.control.Label lblUsuarioHeader;
    @FXML private javafx.scene.control.Label lblUsuario;
    @FXML private javafx.scene.control.Label lblEstado;
    @FXML private javafx.scene.control.Label lblFecha;
    @FXML private javafx.scene.control.Label lblHora;

    public MainPanelController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        log.info("MainPanelController inicializado correctamente");
        if (contentArea != null) {
            log.info("✅ contentArea cargado correctamente");
            // Cargar dashboard por defecto
            cargarVistaModulo("/ui/dashboard.fxml");
        } else {
            log.warn("⚠️ contentArea es null");
        }
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

            log.info("Reemplazando contenido de contentArea...");
            contentArea.getChildren().setAll(view);

            log.info("=====================================");
            log.info("VISTA CARGADA EXITOSAMENTE: {}", fxmlPath);
            log.info("Elementos en contentArea: {}", contentArea.getChildren().size());
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

    @FXML
    public void onFacturasCompra() {
        log.info(">>> BOTÓN FACTURAS DE COMPRA PRESIONADO <<<");
        cargarVistaModulo("/ui/facturas_compra_panel.fxml");
    }

    @FXML
    public void onPedidosCompra() {
        log.info(">>> BOTÓN PEDIDOS DE COMPRA PRESIONADO <<<");
        cargarVistaModulo("/ui/pedidos_compra_panel.fxml");
    }

    @FXML
    public void onUsuarios() {
        log.info(">>> BOTÓN USUARIOS PRESIONADO <<<");
        cargarVistaModulo("/ui/usuarios_panel.fxml");
    }

    @FXML
    public void onAuditoria() {
        log.info(">>> BOTÓN AUDITORÍA PRESIONADO <<<");
        cargarVistaModulo("/ui/auditoria_panel.fxml");
    }

    @FXML
    public void onAsientos() {
        log.info(">>> BOTÓN ASIENTOS CONTABLES PRESIONADO <<<");
        cargarVistaModulo("/ui/asientos_panel.fxml");
    }

    @FXML
    public void onPlanContable() {
        log.info(">>> BOTÓN PLAN CONTABLE PRESIONADO <<<");
        cargarVistaModulo("/ui/plan_contable_panel.fxml");
    }

    @FXML
    public void onModelo347() {
        log.info(">>> BOTÓN MODELO 347 PRESIONADO <<<");
        cargarVistaModulo("/ui/modelo347_panel.fxml");
    }

    @FXML
    public void onPresupuestos() {
        log.info(">>> BOTÓN PRESUPUESTOS PRESIONADO <<<");
        cargarVistaModulo("/ui/presupuestos_panel.fxml");
    }

    @FXML
    public void onPedidosVenta() {
        log.info(">>> BOTÓN PEDIDOS DE VENTA PRESIONADO <<<");
        cargarVistaModulo("/ui/pedidos_venta_panel.fxml");
    }

    @FXML
    public void onCaja() {
        log.info(">>> BOTÓN CAJA PRESIONADO <<<");
        cargarVistaModulo("/ui/caja_panel.fxml");
    }

    @FXML
    public void onMovimientosBanco() {
        log.info(">>> BOTÓN MOVIMIENTOS BANCARIOS PRESIONADO <<<");
        cargarVistaModulo("/ui/movimientos_banco_panel.fxml");
    }

    @FXML
    public void onEmpresaConfig() {
        log.info(">>> BOTÓN CONFIGURACIÓN DE EMPRESA PRESIONADO <<<");
        cargarVistaModulo("/ui/empresa_config_panel.fxml");
    }

    @FXML
    public void onSalir() {
        log.info(">>> BOTÓN SALIR PRESIONADO <<<");
        javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
        stage.close();
    }
}
