package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class FacturaController extends BaseController<Factura> {
    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    private final FacturaService facturaService;
    private final ApplicationContext applicationContext;

    @FXML private TableView<Factura> tableFacturas;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, LocalDate> colFecha;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, BigDecimal> colBase;
    @FXML private TableColumn<Factura, BigDecimal> colIVA;
    @FXML private TableColumn<Factura, BigDecimal> colTotal;
    @FXML private TableColumn<Factura, String> colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    public FacturaController(FacturaService facturaService, ApplicationContext applicationContext) {
        this.facturaService = facturaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        log.info("✅ Inicializando FacturaController");

        // Asignar la tabla del FXML a la tabla base
        this.table = tableFacturas;
        this.lblEstado = lblTotal;

        // Configurar columnas
        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colCliente != null) {
            colCliente.setCellValueFactory(cellData -> {
                Factura factura = cellData.getValue();
                String clienteNombre = factura.getCliente() != null ?
                    factura.getCliente().getNombre() : "Sin cliente";
                return new javafx.beans.property.SimpleStringProperty(clienteNombre);
            });
        }
        if (colBase != null) colBase.setCellValueFactory(new PropertyValueFactory<>("baseImponible"));
        if (colIVA != null) colIVA.setCellValueFactory(new PropertyValueFactory<>("totalIva"));
        if (colTotal != null) colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configurar ComboBox de estado
        if (cmbEstado != null) {
            cmbEstado.getItems().addAll("Todas", "BORRADOR", "EMITIDA", "PAGADA", "ANULADA");
            cmbEstado.setValue("Todas");
        }

        // Aplicar estilo a la tabla
        if (tableFacturas != null) {
            tableFacturas.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        // Inicializar controlador base
        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Factura> facturas = facturaService.findAll();
            actualizarTabla(facturas);
            log.info("✅ Cargadas {} facturas", facturas.size());
        } catch (Exception e) {
            log.error("❌ Error cargando facturas", e);
            mostrarError("Error al cargar facturas: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Factura";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/factura_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Factura item, String termino) {
        if (item == null || termino == null) return false;

        String t = termino.toLowerCase();
        return (item.getNumero() != null && item.getNumero().toLowerCase().contains(t)) ||
               (item.getCliente() != null && item.getCliente().getNombre() != null &&
                item.getCliente().getNombre().toLowerCase().contains(t)) ||
               (item.getEstado() != null && item.getEstado().toLowerCase().contains(t)) ||
               (item.getObservaciones() != null && item.getObservaciones().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Factura item) {
        if (item != null && item.getId() != null) {
            // Anular factura en lugar de eliminar
            item.setEstado("ANULADA");
            facturaService.save(item);
            log.info("✅ Factura anulada: {}", item.getNumero());
        }
    }

    @FXML
    public void onBuscar() {
        String termino = txtBuscar != null ? txtBuscar.getText() : "";
        filtrar(termino);
    }

    @FXML
    public void onVer() {
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Ver factura: {}", selected.getNumero());
            mostrarInfo("Ver detalles de factura en desarrollo");
        } else {
            mostrarAdvertencia("Selecciona una factura primero");
        }
    }

    @FXML
    public void onAnular() {
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (mostrarConfirmacion("¿Está seguro de anular la factura " + selected.getNumero() + "?")) {
                eliminarItem(selected);
                cargarDatos();
            }
        } else {
            mostrarAdvertencia("Selecciona una factura primero");
        }
    }

    @FXML
    public void onImprimir() {
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAdvertencia("Selecciona una factura primero");
            return;
        }

        try {
            log.info("Imprimiendo factura: {}", selected.getNumero());

            // Generar e imprimir PDF con VeriFacTu
            impresionService.imprimirFactura(selected, true);

            mostrarExito("Factura impresa correctamente.\nPDF generado en: " +
                impresionService.getDirectorioImpresiones());

        } catch (Exception e) {
            log.error("Error imprimiendo factura", e);
            mostrarError("Error al imprimir factura: " + e.getMessage());
        }
    }

    @FXML
    public void onEnviarAeat() {
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Enviar factura a AEAT: {}", selected.getNumero());
            mostrarInfo("Envío a AEAT en desarrollo");
        } else {
            mostrarAdvertencia("Selecciona una factura primero");
        }
    }

    @FXML
    public void onCrearRectificativa() {
        try {
            log.info("Abriendo formulario de factura rectificativa...");

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/ui/factura_rectificativa_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Crear Factura Rectificativa");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescar tabla después de cerrar
            cargarFacturas();

        } catch (Exception e) {
            log.error("Error abriendo formulario de factura rectificativa", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para recargar las facturas
     */
    private void cargarFacturas() {
        cargarDatos();
    }
}

