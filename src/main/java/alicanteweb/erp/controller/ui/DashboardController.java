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

    @FXML private Label lblBienvenida;
    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalArticulos;
    @FXML private Label lblTotalFacturas;
    @FXML private Label lblTotalFacturacion;

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
        iniciarReloj();
    }

    private void cargarEstadisticas() {
        try {
            // Cargar total de clientes
            long totalClientes = clienteService.findAll().size();
            lblTotalClientes.setText(String.valueOf(totalClientes));

            // Cargar total de artículos
            long totalArticulos = articuloService.findAll().size();
            lblTotalArticulos.setText(String.valueOf(totalArticulos));

            // Cargar facturas del mes (si existe el método)
            try {
                long totalFacturas = facturaService.findAll().size();
                lblTotalFacturas.setText(String.valueOf(totalFacturas));
            } catch (Exception e) {
                lblTotalFacturas.setText("0");
            }

            // Facturación total (simulado por ahora)
            lblTotalFacturacion.setText("0.00 €");

            log.info("Estadísticas cargadas: {} clientes, {} artículos", totalClientes, totalArticulos);
        } catch (Exception e) {
            log.error("Error cargando estadísticas", e);
        }
    }

    private void iniciarReloj() {
        Thread relojThread = new Thread(() -> {
            while (true) {
                try {
                    LocalDateTime ahora = LocalDateTime.now();
                    String fecha = ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    String hora = ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    Platform.runLater(() -> {
                        if (lblFecha != null) lblFecha.setText(fecha);
                        if (lblHora != null) lblHora.setText(hora);
                    });

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        relojThread.setDaemon(true);
        relojThread.start();
    }

    // Métodos de navegación
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

