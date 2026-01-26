package alicanteweb.erp.controller;

import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.entities.Banco;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import alicanteweb.erp.ui.DialogUtils;

/**
 * Controlador para la gestión de Movimientos Bancarios
 * Tesorería - Movimientos de bancos
 */
@Controller
public class MovimientoBancoController {
    private static final Logger log = LoggerFactory.getLogger(MovimientoBancoController.class);

    @FXML private TableView<MovimientoBanco> tableMovimientos;
    // Columnas declaradas en el FXML: colFecha, colCuenta, colConcepto, colImporte, colSaldo
    @FXML private TableColumn<MovimientoBanco, String> colFecha;
    @FXML private TableColumn<MovimientoBanco, String> colCuenta;
    @FXML private TableColumn<MovimientoBanco, String> colConcepto;
    @FXML private TableColumn<MovimientoBanco, String> colImporte;
    @FXML private TableColumn<MovimientoBanco, String> colSaldo;

    // Controles del FXML
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCuenta; // opcional: filtro por cuenta
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal; // muestra total de movimientos

    private final ObservableList<MovimientoBanco> movimientosList = FXCollections.observableArrayList();

    public MovimientoBancoController() {
    }

    @FXML
    public void initialize() {
        log.info("=== INICIALIZANDO MovimientoBancoController ===");

        if (colFecha != null) colFecha.setCellValueFactory(this::fechaCellValue);
        if (colCuenta != null) colCuenta.setCellValueFactory(this::bancoCellValue);
        if (colConcepto != null) colConcepto.setCellValueFactory(this::conceptoCellValue);
        if (colImporte != null) colImporte.setCellValueFactory(this::importeCellValue);
        if (colSaldo != null) colSaldo.setCellValueFactory(cell -> new SimpleStringProperty("-")); // placeholder

        if (tableMovimientos != null) {
            tableMovimientos.setItems(movimientosList);
        }

        // Inicializar filtros opcionales para evitar warnings 'assigned but never accessed'
        if (cmbCuenta != null) {
            cmbCuenta.setItems(FXCollections.observableArrayList());
            cmbCuenta.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadAll());
        }
        if (dpFechaDesde != null) {
            dpFechaDesde.valueProperty().addListener((obs, oldV, newV) -> onBuscar());
        }
        if (dpFechaHasta != null) {
            dpFechaHasta.valueProperty().addListener((obs, oldV, newV) -> onBuscar());
        }

        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarMovimientos(newV));
        }

        log.info("=== FINALIZÓ INICIALIZACIÓN MovimientoBancoController ===");
    }

    private ObservableValue<String> fechaCellValue(CellDataFeatures<MovimientoBanco, String> cell) {
        MovimientoBanco m = cell.getValue();
        String v = m == null ? "" : Optional.ofNullable(m.getFecha()).map(Object::toString).orElse("");
        return new SimpleStringProperty(v);
    }

    private ObservableValue<String> bancoCellValue(CellDataFeatures<MovimientoBanco, String> cell) {
        MovimientoBanco m = cell.getValue();
        String v = m == null ? "" : Optional.ofNullable(m.getBanco()).map(Banco::getNombre).orElse("");
        return new SimpleStringProperty(v);
    }

    private ObservableValue<String> conceptoCellValue(CellDataFeatures<MovimientoBanco, String> cell) {
        MovimientoBanco m = cell.getValue();
        String v = m == null ? "" : Optional.ofNullable(m.getConcepto()).orElse("");
        return new SimpleStringProperty(v);
    }

    private ObservableValue<String> importeCellValue(CellDataFeatures<MovimientoBanco, String> cell) {
        MovimientoBanco m = cell.getValue();
        String v = m == null ? "" : Optional.ofNullable(m.getImporte()).map(Object::toString).orElse("");
        return new SimpleStringProperty(v);
    }

    private void loadAll() {
        try {
            log.info("Cargando movimientos bancarios (preparado)");
            // TODO: cargar movimientos desde el servicio o repositorio
            // movimientosList.setAll(repository.findAll());

            javafx.application.Platform.runLater(() -> {
                if (tableMovimientos != null) {
                    tableMovimientos.refresh();
                }
                if (lblTotal != null) {
                    lblTotal.setText(movimientosList.size() + " movimientos");
                }
            });
        } catch (Exception e) {
            log.error("Error cargando movimientos", e);
            mostrarError("Error cargando movimientos: " + e.getMessage());
        }
    }

    private void filtrarMovimientos(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            loadAll();
            return;
        }

        String search = busqueda.toLowerCase();
        movimientosList.stream()
                .filter(m -> (m.getBanco() != null && m.getBanco().getNombre().toLowerCase().contains(search)) ||
                        (m.getConcepto() != null && m.getConcepto().toLowerCase().contains(search)))
                .forEach(System.out::println);
    }

    @FXML
    public void onCreate() {
        log.info("Abriendo formulario para crear nuevo movimiento");
        mostrarInfo("Funcionalidad no implementada aún");
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    // Nuevo handler: onNuevo (wrapper de onCreate) requerido por FXML
    @FXML
    public void onNuevo() {
        onCreate();
    }

    // Nuevo handler: onBuscar (invoca filtrarMovimientos con el texto del campo)
    @FXML
    public void onBuscar() {
        filtrarMovimientos(txtBuscar != null ? txtBuscar.getText() : null);
    }

    // Nuevo handler: onVer (stub)
    @FXML
    public void onVer() {
        mostrarInfo("Ver movimiento (stub)");
    }

    // Nuevo handler: onConciliar (stub)
    @FXML
    public void onConciliar() {
        mostrarInfo("Conciliación (stub)");
    }

    private void mostrarInfo(String mensaje) {
        DialogUtils.showInfo(mensaje);
    }

    private void mostrarError(String mensaje) {
        DialogUtils.showError(mensaje);
    }
}
