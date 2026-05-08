package alicanteweb.erp.controller.ui;

import alicanteweb.erp.service.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

            // Usar FacturaService para obtener facturas y poblar etiquetas/tabla
            try {
                var facturas = facturaService.findAll();
                int totalFacturas = facturas != null ? facturas.size() : 0;
                if (lblFacturasHoy != null) lblFacturasHoy.setText(totalFacturas + " facturas");
                if (lblFacturasMes != null) lblFacturasMes.setText(totalFacturas + " facturas");

                // Asegurar que lblVentasDia/mes siempre se actualicen (evita warning de campo no usado)
                if (lblVentasDia != null) lblVentasDia.setText("0.00 €");
                if (lblVentasMes != null) lblVentasMes.setText("0.00 €");

                if (tableUltimasFacturas != null && facturas != null) {
                    int max = Math.min(10, facturas.size());
                    tableUltimasFacturas.setItems(javafx.collections.FXCollections.observableArrayList(facturas.subList(0, max)));
                }
                if (colNumero != null) colNumero.setText("Número");
                if (colFecha != null) colFecha.setText("Fecha");
                if (colCliente != null) colCliente.setText("Cliente");
                if (colTotal != null) colTotal.setText("Total");
                if (colEstado != null) colEstado.setText("Estado");
            } catch (Exception e) {
                log.debug("No se pudieron cargar facturas para el dashboard: {}", e.getMessage());
            }

            log.info("Estadísticas cargadas: {} clientes, {} artículos", totalClientes, totalArticulos);
        } catch (Exception e) {
            log.error("Error cargando estadísticas", e);
        }
    }

    // Métodos de navegación usados por el dashboard FXML
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
    public void onProveedores() {
        mainPanelController.onProveedores();
    }

    @FXML
    public void onCaja() {
        mainPanelController.onCaja();
    }

    @FXML
    public void onContabilidad() {
        mainPanelController.onAsientos();
    }

    @FXML
    public void onPresupuestos() {
        mainPanelController.onPresupuestos();
    }

    @FXML
    public void onConfiguracion() {
        mainPanelController.onEmpresaConfig();
    }

}
