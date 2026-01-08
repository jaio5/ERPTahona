package alicanteweb.erp.controller.ui;

import alicanteweb.erp.service.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para el Dashboard inicial
 */
@Controller
public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label lblVentasDia;
    @FXML private Label lblVentasMes;
    @FXML private Label lblClientes;
    @FXML private Label lblArticulos;
    @FXML private Label lblFacturasHoy;
    @FXML private Label lblFacturasMes;
    @FXML private javafx.scene.control.TableView<Object> tableUltimasFacturas;
    @FXML private javafx.scene.control.TableColumn<Object, String> colNumero;
    @FXML private javafx.scene.control.TableColumn<Object, String> colFecha;
    @FXML private javafx.scene.control.TableColumn<Object, String> colCliente;
    @FXML private javafx.scene.control.TableColumn<Object, String> colTotal;
    @FXML private javafx.scene.control.TableColumn<Object, String> colEstado;

    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final FacturaService facturaService;
    private final MainPanelController mainPanelController;

    public DashboardController(ClienteService clienteService, ArticuloService articuloService,
                              FacturaService facturaService, MainPanelController mainPanelController) {
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.facturaService = facturaService;
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando DashboardController");
        cargarEstadisticas();
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando dashboard");
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        try {
            // Cargar total de clientes
            long totalClientes = clienteService.findAll().size();
            if (lblClientes != null) {
                lblClientes.setText(String.valueOf(totalClientes));
            }

            // Cargar total de artículos
            long totalArticulos = articuloService.findAll().size();
            if (lblArticulos != null) {
                lblArticulos.setText(String.valueOf(totalArticulos));
            }

            // Cargar facturas (simulado por ahora)
            if (lblVentasDia != null) lblVentasDia.setText("0.00 €");
            if (lblVentasMes != null) lblVentasMes.setText("0.00 €");
            if (lblFacturasHoy != null) lblFacturasHoy.setText("0 facturas");
            if (lblFacturasMes != null) lblFacturasMes.setText("0 facturas");

            log.info("Estadísticas cargadas: {} clientes, {} artículos", totalClientes, totalArticulos);
        } catch (Exception e) {
            log.error("Error cargando estadísticas", e);
        }
    }


    // Métodos de navegación
    @FXML
    public void onNuevaFactura() {
        mainPanelController.onFacturas();
    }

    @FXML
    public void onNuevoAlbaran() {
        mainPanelController.onAlbaranes();
    }

    @FXML
    public void onNuevoCliente() {
        mainPanelController.onClientes();
    }

    @FXML
    public void onNuevoArticulo() {
        mainPanelController.onArticulos();
    }

    @FXML
    public void onContabilidad() {
        mainPanelController.onAsientos();
    }

    @FXML
    public void onConfiguracion() {
        mainPanelController.onEmpresaConfig();
    }

    @FXML
    public void onClientes() {
        mainPanelController.onClientes();
    }

    @FXML
    public void onPresupuestos() {
        mainPanelController.onPresupuestos();
    }

    @FXML
    public void onPedidosVenta() {
        mainPanelController.onPedidosVenta();
    }

    @FXML
    public void onAlbaranes() {
        mainPanelController.onAlbaranes();
    }

    @FXML
    public void onFacturas() {
        mainPanelController.onFacturas();
    }

    @FXML
    public void onProveedores() {
        mainPanelController.onProveedores();
    }

    @FXML
    public void onPedidosCompra() {
        mainPanelController.onPedidosCompra();
    }

    @FXML
    public void onFacturasCompra() {
        mainPanelController.onFacturasCompra();
    }

    @FXML
    public void onArticulos() {
        mainPanelController.onArticulos();
    }

    @FXML
    public void onAlmacenes() {
        mainPanelController.onAlmacenes();
    }

    @FXML
    public void onAsientos() {
        mainPanelController.onAsientos();
    }

    @FXML
    public void onPlanContable() {
        mainPanelController.onPlanContable();
    }

    @FXML
    public void onCaja() {
        mainPanelController.onCaja();
    }

    @FXML
    public void onMovimientosBanco() {
        mainPanelController.onMovimientosBanco();
    }

    @FXML
    public void onModelo347() {
        mainPanelController.onModelo347();
    }

    @FXML
    public void onVerifactu() {
        mainPanelController.onVerifactu();
    }

    @FXML
    public void onUsuarios() {
        mainPanelController.onUsuarios();
    }

    @FXML
    public void onAuditoria() {
        mainPanelController.onAuditoria();
    }
}

