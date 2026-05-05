package alicanteweb.erp.controller;

import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.service.MovimientoBancoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import alicanteweb.erp.ui.DialogUtils;

/**
 * Controlador para la gestión de Movimientos Bancarios.
 * Tesorería - Movimientos de bancos.
 */
@Controller
public class MovimientoBancoController {
    private static final Logger log = LoggerFactory.getLogger(MovimientoBancoController.class);

    @FXML private TableView<MovimientoBanco> tableMovimientos;
    @FXML private TableColumn<MovimientoBanco, String> colFecha;
    @FXML private TableColumn<MovimientoBanco, String> colCuenta;
    @FXML private TableColumn<MovimientoBanco, String> colConcepto;
    @FXML private TableColumn<MovimientoBanco, String> colImporte;
    @FXML private TableColumn<MovimientoBanco, String> colSaldo;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCuenta;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    private final ObservableList<MovimientoBanco> movimientosList = FXCollections.observableArrayList();
    private final MovimientoBancoService movimientoBancoService;

    public MovimientoBancoController(MovimientoBancoService movimientoBancoService) {
        this.movimientoBancoService = movimientoBancoService;
    }

    @FXML
    public void initialize() {
        log.info("=== INICIALIZANDO MovimientoBancoController ===");

        if (colFecha != null) colFecha.setCellValueFactory(this::fechaCellValue);
        if (colCuenta != null) colCuenta.setCellValueFactory(this::bancoCellValue);
        if (colConcepto != null) colConcepto.setCellValueFactory(this::conceptoCellValue);
        if (colImporte != null) colImporte.setCellValueFactory(this::importeCellValue);
        if (colSaldo != null) {
            colSaldo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue() != null ? formatoImporte(cell.getValue().getSaldoResultante()) : ""));
        }

        if (tableMovimientos != null) {
            tableMovimientos.setItems(movimientosList);
        }

        // Inicializar filtros opcionales para evitar warnings 'assigned but never accessed'
        if (cmbCuenta != null) {
            cmbCuenta.setItems(FXCollections.observableArrayList(
                movimientoBancoService.findBancosActivos().stream().map(Banco::getNombre).toList()
            ));
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
        String v = m == null ? "" : formatoImporte(m.getImporte());
        return new SimpleStringProperty(v);
    }

    private void loadAll() {
        try {
            List<MovimientoBanco> movimientos = movimientoBancoService.findAll();
            if (cmbCuenta != null && cmbCuenta.getValue() != null && !cmbCuenta.getValue().isBlank()) {
                String cuenta = cmbCuenta.getValue();
                movimientos = movimientos.stream()
                    .filter(m -> m.getBanco() != null && cuenta.equals(m.getBanco().getNombre()))
                    .toList();
            }
            if (dpFechaDesde != null && dpFechaDesde.getValue() != null) {
                var desde = dpFechaDesde.getValue();
                movimientos = movimientos.stream().filter(m -> m.getFecha() != null && !m.getFecha().isBefore(desde)).toList();
            }
            if (dpFechaHasta != null && dpFechaHasta.getValue() != null) {
                var hasta = dpFechaHasta.getValue();
                movimientos = movimientos.stream().filter(m -> m.getFecha() != null && !m.getFecha().isAfter(hasta)).toList();
            }
            movimientosList.setAll(movimientos);
            log.info("Cargados {} movimientos bancarios", movimientos.size());

            javafx.application.Platform.runLater(() -> {
                if (tableMovimientos != null) {
                    tableMovimientos.refresh();
                }
                if (lblTotal != null) {
                    lblTotal.setText(movimientosList.size() + " movimientos");
                }
            });
        } catch (Exception e) {
            log.error("Error cargando movimientos bancarios", e);
            mostrarError("Error cargando movimientos: " + e.getMessage());
        }
    }

    private void filtrarMovimientos(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            tableMovimientos.setItems(movimientosList);
            return;
        }

        String search = busqueda.toLowerCase();
        List<MovimientoBanco> filtrados = movimientosList.stream()
                .filter(m -> (m.getBanco() != null && m.getBanco().getNombre().toLowerCase().contains(search)) ||
                        (m.getConcepto() != null && m.getConcepto().toLowerCase().contains(search)))
                .toList();

        tableMovimientos.setItems(FXCollections.observableArrayList(filtrados));
        if (lblTotal != null) {
            lblTotal.setText(filtrados.size() + " movimientos");
        }
    }

    @FXML
    public void onCreate() {
        log.info("Abriendo formulario para crear nuevo movimiento");
        abrirDialogoMovimiento(null);
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

    @FXML
    public void onVer() {
        MovimientoBanco movimiento = tableMovimientos != null ? tableMovimientos.getSelectionModel().getSelectedItem() : null;
        if (movimiento == null) {
            mostrarInfo("Selecciona un movimiento");
            return;
        }
        mostrarInfo(detalleMovimiento(movimiento));
    }

    @FXML
    public void onConciliar() {
        MovimientoBanco movimiento = tableMovimientos != null ? tableMovimientos.getSelectionModel().getSelectedItem() : null;
        if (movimiento == null) {
            mostrarInfo("Selecciona un movimiento");
            return;
        }
        try {
            movimientoBancoService.conciliar(movimiento.getId());
            loadAll();
            mostrarInfo("Movimiento conciliado correctamente");
        } catch (Exception e) {
            log.error("Error conciliando movimiento bancario", e);
            mostrarError("Error al conciliar: " + e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        DialogUtils.showInfo(mensaje);
    }

    private void mostrarError(String mensaje) {
        DialogUtils.showError(mensaje);
    }

    private void abrirDialogoMovimiento(MovimientoBanco movimiento) {
        Dialog<MovimientoBanco> dialog = new Dialog<>();
        dialog.setTitle(movimiento == null ? "Nuevo movimiento bancario" : "Editar movimiento bancario");
        dialog.setHeaderText(null);

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        ComboBox<Banco> banco = new ComboBox<>(FXCollections.observableArrayList(movimientoBancoService.findBancosActivos()));
        banco.setCellFactory(param -> bancoCell());
        banco.setButtonCell(bancoCell());
        banco.setValue(movimiento != null ? movimiento.getBanco() : null);
        DatePicker fecha = new DatePicker(movimiento != null ? movimiento.getFecha() : java.time.LocalDate.now());
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("INGRESO", "GASTO", "TRASPASO"));
        tipo.setValue(movimiento != null ? movimiento.getTipo() : "INGRESO");
        TextField importe = new TextField(movimiento != null ? valorDecimal(movimiento.getImporte()) : "0.00");
        TextField concepto = new TextField(movimiento != null ? movimiento.getConcepto() : "");
        TextArea observaciones = new TextArea(movimiento != null ? movimiento.getObservaciones() : "");
        observaciones.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Cuenta"), banco);
        grid.addRow(1, new Label("Fecha"), fecha);
        grid.addRow(2, new Label("Tipo"), tipo);
        grid.addRow(3, new Label("Importe"), importe);
        grid.addRow(4, new Label("Concepto"), concepto);
        grid.addRow(5, new Label("Observaciones"), observaciones);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != guardar) return null;
            MovimientoBanco resultado = movimiento != null ? movimiento : new MovimientoBanco();
            resultado.setBanco(banco.getValue());
            resultado.setFecha(fecha.getValue());
            resultado.setTipo(tipo.getValue());
            resultado.setImporte(parseImporte(importe.getText()));
            resultado.setConcepto(concepto.getText());
            resultado.setObservaciones(observaciones.getText());
            return resultado;
        });

        dialog.showAndWait().ifPresent(resultado -> {
            if (resultado.getBanco() == null || resultado.getFecha() == null || resultado.getImporte() == null) {
                DialogUtils.showWarning("Cuenta, fecha e importe son obligatorios");
                return;
            }
            movimientoBancoService.save(resultado);
            loadAll();
            mostrarInfo("Movimiento bancario guardado correctamente");
        });
    }

    private ListCell<Banco> bancoCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Banco item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        };
    }

    private String detalleMovimiento(MovimientoBanco movimiento) {
        return "Cuenta: " + (movimiento.getBanco() != null ? movimiento.getBanco().toString() : "") + "\n"
            + "Fecha: " + movimiento.getFecha() + "\n"
            + "Tipo: " + valor(movimiento.getTipo()) + "\n"
            + "Concepto: " + valor(movimiento.getConcepto()) + "\n"
            + "Importe: " + formatoImporte(movimiento.getImporte()) + "\n"
            + "Saldo resultante: " + formatoImporte(movimiento.getSaldoResultante()) + "\n"
            + "Conciliado: " + (Boolean.TRUE.equals(movimiento.getConciliado()) ? "Sí" : "No") + "\n"
            + "Observaciones: " + valor(movimiento.getObservaciones());
    }

    private BigDecimal parseImporte(String value) {
        return new BigDecimal(value.replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
    }

    private String valorDecimal(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatoImporte(BigDecimal value) {
        return valorDecimal(value) + " EUR";
    }

    private String valor(String value) {
        return value != null ? value : "";
    }
}
