package alicanteweb.erp.controller.ui;

import alicanteweb.erp.service.AutenticacionService;
import alicanteweb.erp.service.GitUpdateService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Controlador principal del panel de navegación del ERP.
 * Gestiona la carga dinámica de vistas FXML en el área de contenido central.
 */
@Controller
public class MainPanelController {

    private static final Logger log = LoggerFactory.getLogger(MainPanelController.class);

    private final ApplicationContext springContext;
    private final AutenticacionService autenticacionService;
    private final GitUpdateService gitUpdateService;

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblEstado;
    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Button btnActualizarApp;

    // Controles de menú con visibilidad controlada por rol
    @FXML private Button btnBackups;
    @FXML private Button btnUsuarios;
    @FXML private Button btnAuditoria;
    @FXML private Button btnAsientos;
    @FXML private Button btnModelo347;

    public MainPanelController(ApplicationContext springContext,
                               AutenticacionService autenticacionService,
                               GitUpdateService gitUpdateService) {
        this.springContext = springContext;
        this.autenticacionService = autenticacionService;
        this.gitUpdateService = gitUpdateService;
    }

    @FXML
    public void initialize() {
        if (contentArea != null) {
            cargarVista("/ui/dashboard.fxml");
        } else {
            log.warn("contentArea no fue inyectado por FXML");
        }
        aplicarVisibilidadPorRol();
        actualizarInfoUsuario();
        comprobarActualizacionEnSegundoPlano();
    }

    /**
     * Oculta elementos del menú que el usuario actual no tiene permiso de ver.
     * Los nodos de menú marcados como {@code @FXML} deben tener el mismo fx:id
     * que el atributo del campo (btnUsuarios, btnAuditoria, etc.).
     */
    private void aplicarVisibilidadPorRol() {
        boolean esAdmin = autenticacionService.esAdministrador();
        boolean verContabilidad = esAdmin || autenticacionService.tienePermiso("contabilidad", "ver");
        // Botones en barra superior (son Node — visible+managed)
        setVisible(btnBackups, esAdmin);
        setVisible(btnActualizarApp, esAdmin && btnActualizarApp != null && btnActualizarApp.isVisible());
        // Elementos de menú desplegable (MenuItem — solo visible)
        setVisible(btnUsuarios, esAdmin);
        setVisible(btnAuditoria, esAdmin);
        setVisible(btnAsientos, verContabilidad);
        setVisible(btnModelo347, verContabilidad);
    }

    private void setVisible(Node nodo, boolean visible) {
        if (nodo != null) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
    }

    private void actualizarInfoUsuario() {
        if (lblUsuario != null) {
            lblUsuario.setText(autenticacionService.getNombreUsuarioActual());
        }
    }

    /**
     * Carga una vista FXML en el área de contenido central.
     *
     * @param fxmlPath ruta al recurso FXML (p.ej. "/ui/clientes_panel.fxml")
     */
    public void cargarVista(String fxmlPath) {
        log.debug("Cargando vista: {}", fxmlPath);
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IllegalArgumentException("Recurso FXML no encontrado: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(springContext::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            log.debug("Vista cargada: {}", fxmlPath);
        } catch (Exception e) {
            log.error("Error cargando vista '{}': {}", fxmlPath, e.getMessage(), e);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de navegación");
                alert.setHeaderText("No se pudo cargar la vista");
                alert.setContentText(fxmlPath + "\n" + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void cargarVistaAutorizada(String modulo, String fxmlPath) {
        if (!autenticacionService.tienePermiso(modulo, "ver")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permiso denegado");
            alert.setHeaderText(null);
            alert.setContentText("No tiene permisos para acceder a este módulo.");
            alert.showAndWait();
            return;
        }
        cargarVista(fxmlPath);
    }

    // ── Handlers de navegación ──────────────────────────────────────────────

    @FXML public void onDashboard()         { cargarVistaAutorizada("dashboard", "/ui/dashboard.fxml"); }
    @FXML public void onClientes()          { cargarVistaAutorizada("clientes", "/ui/clientes_panel.fxml"); }
    @FXML public void onProveedores()       { cargarVistaAutorizada("proveedores", "/ui/proveedores_panel.fxml"); }
    @FXML public void onArticulos()         { cargarVistaAutorizada("articulos", "/ui/articulos_panel.fxml"); }
    @FXML public void onAlbaranes()         { cargarVistaAutorizada("ventas", "/ui/albaranes_panel.fxml"); }
    @FXML public void onFacturas()          { cargarVistaAutorizada("ventas", "/ui/facturas_panel.fxml"); }
    @FXML public void onAlmacenes()         { cargarVistaAutorizada("almacen", "/ui/almacenes_panel.fxml"); }
    @FXML public void onVerifactu()         { cargarVistaAutorizada("verifactu", "/ui/verifactu_panel.fxml"); }
    @FXML public void onFacturasCompra()    { cargarVistaAutorizada("compras", "/ui/facturas_compra_panel.fxml"); }
    @FXML public void onPedidosCompra()     { cargarVistaAutorizada("compras", "/ui/pedidos_compra_panel.fxml"); }
    @FXML public void onPedidosVenta()      { cargarVistaAutorizada("ventas", "/ui/pedidos_venta_panel.fxml"); }
    @FXML public void onPresupuestos()      { cargarVistaAutorizada("ventas", "/ui/presupuestos_panel.fxml"); }
    @FXML public void onUsuarios()          { cargarVistaAutorizada("usuarios", "/ui/usuarios_panel.fxml"); }
    @FXML public void onAuditoria()         { cargarVistaAutorizada("auditoria", "/ui/auditoria_panel.fxml"); }
    @FXML public void onBackups()           { cargarVistaAutorizada("backup", "/ui/backup_panel.fxml"); }
    @FXML public void onAsientos()          { cargarVistaAutorizada("contabilidad", "/ui/asientos_panel.fxml"); }
    @FXML public void onPlanContable()      { cargarVistaAutorizada("contabilidad", "/ui/plan_contable_panel.fxml"); }
    @FXML public void onModelo347()         { cargarVistaAutorizada("fiscal", "/ui/modelo347_panel.fxml"); }
    @FXML public void onCaja()              { cargarVistaAutorizada("tesoreria", "/ui/caja_panel.fxml"); }
    @FXML public void onMovimientosBanco()  { cargarVistaAutorizada("tesoreria", "/ui/movimientos_banco_panel.fxml"); }
    @FXML public void onEmpresaConfig()     { cargarVistaAutorizada("configuracion", "/ui/empresa_config_panel.fxml"); }
    @FXML public void onConfiguracion()     { cargarVistaAutorizada("configuracion", "/ui/empresa_config_panel.fxml"); }

    @FXML
    public void onActualizarApp() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Actualizar aplicación");
        confirmacion.setHeaderText("Se descargarán los últimos cambios de producción.");
        confirmacion.setContentText("La aplicación deberá reiniciarse para cargar la versión actualizada.");
        confirmacion.showAndWait()
            .filter(r -> r == ButtonType.OK)
            .ifPresent(r -> ejecutarActualizacionEnSegundoPlano());
    }

    // ── Sesión ──────────────────────────────────────────────────────────────

    @FXML
    public void onCerrarSesion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar Sesión");
        confirmacion.setHeaderText("¿Desea cerrar sesión?");
        confirmacion.setContentText("Se cerrará la sesión actual.");
        confirmacion.showAndWait()
            .filter(r -> r == ButtonType.OK)
            .ifPresent(r -> cerrarVentana());
    }

    @FXML
    public void onSalir() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    private void comprobarActualizacionEnSegundoPlano() {
        if (btnActualizarApp != null) {
            btnActualizarApp.setVisible(false);
            btnActualizarApp.setManaged(false);
        }

        Task<GitUpdateService.UpdateStatus> task = new Task<>() {
            @Override
            protected GitUpdateService.UpdateStatus call() {
                return gitUpdateService.checkForUpdates();
            }
        };

        task.setOnSucceeded(e -> {
            GitUpdateService.UpdateStatus status = task.getValue();
            boolean visible = autenticacionService.esAdministrador() && status.isAvailable();
            if (btnActualizarApp != null) {
                btnActualizarApp.setVisible(visible);
                btnActualizarApp.setManaged(visible);
                if (visible) {
                    btnActualizarApp.setText("Actualizar (" + status.getCommitsBehind() + ")");
                }
            }
            if (visible && lblEstado != null) {
                lblEstado.setText(status.getMessage());
                lblEstado.getStyleClass().removeAll("badge-success");
                if (!lblEstado.getStyleClass().contains("badge-warning")) {
                    lblEstado.getStyleClass().add("badge-warning");
                }
            }
        });

        task.setOnFailed(e -> log.debug("No se pudo comprobar actualización", task.getException()));
        Thread thread = new Thread(task, "git-update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private void ejecutarActualizacionEnSegundoPlano() {
        if (btnActualizarApp != null) {
            btnActualizarApp.setDisable(true);
            btnActualizarApp.setText("Actualizando...");
        }

        Task<GitUpdateService.UpdateResult> task = new Task<>() {
            @Override
            protected GitUpdateService.UpdateResult call() {
                return gitUpdateService.update();
            }
        };

        task.setOnSucceeded(e -> {
            GitUpdateService.UpdateResult result = task.getValue();
            if (btnActualizarApp != null) {
                btnActualizarApp.setDisable(false);
            }
            Alert alert = new Alert(result.isUpdated() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.setTitle("Actualización");
            alert.setHeaderText(result.isUpdated() ? "Actualización aplicada" : "No se pudo actualizar");
            alert.setContentText(result.getMessage()
                + (result.isUpdated() ? "\n\nCierra y vuelve a abrir la aplicación para usar la nueva versión." : ""));
            alert.showAndWait();
            comprobarActualizacionEnSegundoPlano();
        });

        task.setOnFailed(e -> {
            if (btnActualizarApp != null) {
                btnActualizarApp.setDisable(false);
            }
            Throwable ex = task.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de actualización");
            alert.setHeaderText("No se pudo actualizar la aplicación");
            alert.setContentText(ex != null ? ex.getMessage() : "Error desconocido");
            alert.showAndWait();
        });

        Thread thread = new Thread(task, "git-update-pull");
        thread.setDaemon(true);
        thread.start();
    }
}
