package alicanteweb.erp.controller;

import alicanteweb.erp.entities.MovimientoCaja;
import alicanteweb.erp.service.MovimientoCajaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import alicanteweb.erp.ui.Dialogs;

/**
 * Controlador para la gestión de Movimientos de Caja
 */
@Controller
public class CajaController {
    private static final Logger log = LoggerFactory.getLogger(CajaController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<MovimientoCaja> tableMovimientos;
    @FXML private TableColumn<MovimientoCaja, LocalDate> colFecha;
    @FXML private TableColumn<MovimientoCaja, String> colConcepto;
    @FXML private TableColumn<MovimientoCaja, String> colTipo;
    @FXML private TableColumn<MovimientoCaja, BigDecimal> colImporte;
    @FXML private TableColumn<MovimientoCaja, String> colSaldo;

    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblSaldo;

    private final MovimientoCajaService movimientoCajaService;

    public CajaController(MovimientoCajaService movimientoCajaService) {
        this.movimientoCajaService = movimientoCajaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando CajaController");
        configurarColumnas();
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarMovimientos(newV));
        }

        // Aplicar estilo a la tabla
        if (tableMovimientos != null) {
            tableMovimientos.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        // Actualizar saldo
        actualizarSaldo();
    }

    private void configurarColumnas() {
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        }
        if (colConcepto != null) {
            colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        }
        if (colTipo != null) {
            colTipo.setCellValueFactory(cellData -> {
                MovimientoCaja movimiento = cellData.getValue();
                String tipo = movimiento.getTipo();
                String tipoFormateado = tipo.equals("INGRESO") ? "✅ " + tipo : "❌ " + tipo;
                return new SimpleStringProperty(tipoFormateado);
            });
        }
        if (colImporte != null) {
            colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
            // Formatear importe con color según tipo
            colImporte.setCellFactory(column -> new TableCell<MovimientoCaja, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        MovimientoCaja movimiento = getTableView().getItems().get(getIndex());
                        setText(String.format("%.2f €", item));
                        if (movimiento.getTipo().equals("INGRESO")) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }
        if (colSaldo != null) {
            // Columna calculada de saldo acumulado
            colSaldo.setCellValueFactory(cellData -> {
                // Por ahora mostrar "-" hasta implementar cálculo acumulado
                return new SimpleStringProperty("-");
            });
        }
    }

    private void cargarDatos() {
        try {
            var movimientos = movimientoCajaService.findAll();
            if (tableMovimientos != null) {
                tableMovimientos.setItems(FXCollections.observableArrayList(movimientos));
            }
            log.info("Movimientos de caja cargados: {}", movimientos.size());
            actualizarSaldo();
        } catch (Exception e) {
            log.error("Error cargando movimientos de caja", e);
            mostrarError("Error al cargar movimientos: " + e.getMessage());
        }
    }

    private void actualizarSaldo() {
        try {
            BigDecimal saldo = movimientoCajaService.calcularSaldoActual();
            if (lblSaldo != null) {
                lblSaldo.setText(String.format("Saldo: %.2f €", saldo));
                // Color según el saldo
                if (saldo.compareTo(BigDecimal.ZERO) >= 0) {
                    lblSaldo.setStyle("-fx-text-fill: #0d6efd; -fx-font-size: 16; -fx-font-weight: 600;");
                } else {
                    lblSaldo.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-font-weight: 600;");
                }
            }
        } catch (Exception e) {
            log.error("Error calculando saldo", e);
        }
    }

    private void filtrarMovimientos(String busqueda) {
        try {
            var movimientos = movimientoCajaService.findAll();

            if (busqueda != null && !busqueda.isEmpty()) {
                String search = busqueda.toLowerCase();
                movimientos = movimientos.stream()
                    .filter(m -> (m.getConcepto() != null && m.getConcepto().toLowerCase().contains(search)) ||
                                (m.getTipo() != null && m.getTipo().toLowerCase().contains(search)))
                    .toList();
            }

            if (tableMovimientos != null) {
                tableMovimientos.setItems(FXCollections.observableArrayList(movimientos));
            }
        } catch (Exception e) {
            log.error("Error filtrando movimientos", e);
        }
    }

    @FXML
    public void onBuscar() {
        try {
            var movimientos = movimientoCajaService.findAll();

            // Filtrar por fechas si están seleccionadas
            if (dpFechaDesde != null && dpFechaDesde.getValue() != null &&
                dpFechaHasta != null && dpFechaHasta.getValue() != null) {
                movimientos = movimientoCajaService.findByFechaBetween(
                    dpFechaDesde.getValue(),
                    dpFechaHasta.getValue()
                );
            }

            // Aplicar filtro de texto
            String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
            if (!busqueda.isEmpty()) {
                String search = busqueda.toLowerCase();
                movimientos = movimientos.stream()
                    .filter(m -> (m.getConcepto() != null && m.getConcepto().toLowerCase().contains(search)) ||
                                (m.getTipo() != null && m.getTipo().toLowerCase().contains(search)))
                    .toList();
            }

            if (tableMovimientos != null) {
                tableMovimientos.setItems(FXCollections.observableArrayList(movimientos));
            }
        } catch (Exception e) {
            log.error("Error buscando movimientos", e);
            mostrarError("Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nuevo movimiento de caja");
        mostrarAlerta("Función en desarrollo: Crear nuevo movimiento");
    }

    @FXML
    public void onVer() {
        MovimientoCaja movimiento = tableMovimientos.getSelectionModel().getSelectedItem();
        if (movimiento == null) {
            mostrarAlerta("Selecciona un movimiento primero");
            return;
        }
        log.info("Ver movimiento: {}", movimiento.getConcepto());
        mostrarAlerta("Funcion en desarrollo: Ver detalle del movimiento");
    }

    @FXML
    public void onEditar() {
        MovimientoCaja movimiento = tableMovimientos.getSelectionModel().getSelectedItem();
        if (movimiento == null) {
            mostrarAlerta("Selecciona un movimiento para editar");
            return;
        }
        log.info("Editar movimiento: {}", movimiento.getConcepto());
        mostrarAlerta("Funcion en desarrollo: Editar movimiento");
    }

    @FXML
    public void onEliminar() {
        MovimientoCaja movimiento = tableMovimientos.getSelectionModel().getSelectedItem();
        if (movimiento == null) {
            mostrarAlerta("Selecciona un movimiento para eliminar");
            return;
        }

        if (mostrarConfirmacion("¿Deseas eliminar este movimiento?\n\n" +
                                movimiento.getConcepto() + "\n" +
                                movimiento.getImporte() + " €")) {
            try {
                movimientoCajaService.deleteById(movimiento.getId());
                cargarDatos();
                mostrarExito("Movimiento eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando movimiento", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando movimientos de caja");
        cargarDatos();
    }


    private void mostrarAlerta(String msg) { Dialogs.showWarn(msg); }
    private void mostrarError(String msg) { Dialogs.showError(msg); }
    private void mostrarExito(String msg) { Dialogs.showInfo(msg); }
    private boolean mostrarConfirmacion(String msg) { return Dialogs.showConfirm(msg); }
}

