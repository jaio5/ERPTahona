package alicanteweb.erp.controller;

import alicanteweb.erp.entities.MovimientoCaja;
import alicanteweb.erp.service.MovimientoCajaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import alicanteweb.erp.ui.DialogUtils;

/**
 * Controlador para la gestión de Movimientos de Caja
 */
@Controller
public class CajaController {
    private static final Logger log = LoggerFactory.getLogger(CajaController.class);
    // No se usa DATE_FORMATTER actualmente

    @FXML private TableView<MovimientoCaja> tableMovimientos;
    @FXML private TableColumn<MovimientoCaja, LocalDate> colFecha;
    @FXML private TableColumn<MovimientoCaja, String> colConcepto;
    @FXML private TableColumn<MovimientoCaja, String> colTipo;
    @FXML private TableColumn<MovimientoCaja, BigDecimal> colImporte;
    private final TableColumn<MovimientoCaja, String> colSaldo = new TableColumn<>("Saldo");

    // Campos añadidos para resolver unresolved fx:id
    @FXML private TableColumn<MovimientoCaja, String> colCategoria;
    @FXML private TableColumn<MovimientoCaja, String> colDocumento;
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblSaldo;
    @FXML private Label lblTotalIngresos;
    @FXML private Label lblTotalGastos;
    @FXML private ComboBox<String> cmbTipo;

    private final MovimientoCajaService movimientoCajaService;

    public CajaController(MovimientoCajaService movimientoCajaService) {
        this.movimientoCajaService = movimientoCajaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando CajaController");
        configurarColumnas();

        // Inicializar combobox de tipo
        if (cmbTipo != null) {
            cmbTipo.setItems(FXCollections.observableArrayList("INGRESO", "GASTO"));
            cmbTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                // al cambiar tipo, aplicar filtro simple
                filtrarPorTipo(newV);
            });
        }

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
                String tipo = movimiento.getTipo() == null ? "" : movimiento.getTipo();
                String tipoFormateado = tipo.equals("INGRESO") ? "✅ " + tipo : "❌ " + tipo;
                return new SimpleStringProperty(tipoFormateado);
            });
        }
        if (colImporte != null) {
            colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
            // Formatear importe con color según tipo
            colImporte.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        MovimientoCaja movimiento = getTableView().getItems().get(getIndex());
                        setText(String.format("%.2f €", item));
                        if (movimiento != null && "INGRESO".equals(movimiento.getTipo())) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }

        // Columnas añadidas desde FXML
        if (colCategoria != null) {
            colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        }
        if (colDocumento != null) {
            colDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        }

        colSaldo.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colSaldo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || tableMovimientos == null || getIndex() >= tableMovimientos.getItems().size()) {
                    setText(null);
                    return;
                }
                BigDecimal saldo = calcularSaldoAcumulado(getIndex());
                setText(formatoImporte(saldo));
            }
        });
        colSaldo.setPrefWidth(90);

        // Añadir colSaldo a la tabla si aún no está incluida
        if (tableMovimientos != null && !tableMovimientos.getColumns().contains(colSaldo)) {
            tableMovimientos.getColumns().add(colSaldo);
        }
    }

    private void cargarDatos() {
        try {
            var movimientos = movimientoCajaService.findAll().stream()
                .sorted(Comparator.comparing(MovimientoCaja::getFecha, Comparator.nullsLast(LocalDate::compareTo))
                    .thenComparing(MovimientoCaja::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
            if (tableMovimientos != null) {
                tableMovimientos.setItems(FXCollections.observableArrayList(movimientos));
            }
            log.info("Movimientos de caja cargados: {}", movimientos.size());
            actualizarSaldo();
            actualizarTotales(movimientos);
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

    private void actualizarTotales(java.util.List<MovimientoCaja> movimientos) {
        if (movimientos == null) return;
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;
        for (MovimientoCaja m : movimientos) {
            if (m == null || m.getImporte() == null) continue;
            if ("INGRESO".equals(m.getTipo())) {
                ingresos = ingresos.add(m.getImporte());
            } else {
                gastos = gastos.add(m.getImporte());
            }
        }
        if (lblTotalIngresos != null) lblTotalIngresos.setText(String.format("Ingresos: %.2f EUR", ingresos));
        if (lblTotalGastos != null) lblTotalGastos.setText(String.format("Gastos: %.2f EUR", gastos));
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
            actualizarTotales(movimientos);
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
            actualizarTotales(movimientos);
        } catch (Exception e) {
            log.error("Error buscando movimientos", e);
            mostrarError("Error en la búsqueda: " + e.getMessage());
        }
    }

    // Filtrar por tipo (desde cmbTipo)
    private void filtrarPorTipo(String tipo) {
        try {
            var movimientos = movimientoCajaService.findAll();
            if (tipo != null && !tipo.isEmpty()) {
                movimientos = movimientos.stream()
                    .filter(m -> tipo.equals(m.getTipo()))
                    .toList();
            }
            if (tableMovimientos != null) tableMovimientos.setItems(FXCollections.observableArrayList(movimientos));
            actualizarTotales(movimientos);
        } catch (Exception e) {
            log.error("Error filtrando por tipo", e);
        }
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nuevo movimiento de caja");
        abrirDialogoMovimiento(null);
    }

    @FXML
    public void onVer() {
        MovimientoCaja movimiento = tableMovimientos.getSelectionModel().getSelectedItem();
        if (movimiento == null) {
            mostrarAlerta("Selecciona un movimiento primero");
            return;
        }
        log.info("Ver movimiento: {}", movimiento.getConcepto());
        DialogUtils.showInfo(detalleMovimiento(movimiento));
    }

    @FXML
    public void onEditar() {
        MovimientoCaja movimiento = tableMovimientos.getSelectionModel().getSelectedItem();
        if (movimiento == null) {
            mostrarAlerta("Selecciona un movimiento para editar");
            return;
        }
        log.info("Editar movimiento: {}", movimiento.getConcepto());
        abrirDialogoMovimiento(movimiento);
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
                mostrarExito();
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


    private void mostrarAlerta(String msg) { DialogUtils.showWarning(msg); }
    private void mostrarError(String msg) { DialogUtils.showError(msg); }
    private void mostrarExito() { DialogUtils.showSuccess("Movimiento eliminado correctamente"); }
    private boolean mostrarConfirmacion(String msg) { return DialogUtils.showConfirm(msg); }

    private void abrirDialogoMovimiento(MovimientoCaja movimiento) {
        Dialog<MovimientoCaja> dialog = new Dialog<>();
        dialog.setTitle(movimiento == null ? "Nuevo movimiento de caja" : "Editar movimiento de caja");
        dialog.setHeaderText(null);

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        DatePicker fecha = new DatePicker(movimiento != null ? movimiento.getFecha() : LocalDate.now());
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("INGRESO", "GASTO"));
        tipo.setValue(movimiento != null ? movimiento.getTipo() : "INGRESO");
        TextField importe = new TextField(movimiento != null ? valorDecimal(movimiento.getImporte()) : "0.00");
        TextField concepto = new TextField(movimiento != null ? movimiento.getConcepto() : "");
        TextField categoria = new TextField(movimiento != null ? movimiento.getCategoria() : "");
        TextField documento = new TextField(movimiento != null ? movimiento.getDocumento() : "");
        TextArea observaciones = new TextArea(movimiento != null ? movimiento.getObservaciones() : "");
        observaciones.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Fecha"), fecha);
        grid.addRow(1, new Label("Tipo"), tipo);
        grid.addRow(2, new Label("Importe"), importe);
        grid.addRow(3, new Label("Concepto"), concepto);
        grid.addRow(4, new Label("Categoría"), categoria);
        grid.addRow(5, new Label("Documento"), documento);
        grid.addRow(6, new Label("Observaciones"), observaciones);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != guardar) return null;
            MovimientoCaja resultado = movimiento != null ? movimiento : new MovimientoCaja();
            resultado.setFecha(fecha.getValue());
            resultado.setTipo(tipo.getValue());
            resultado.setImporte(parseImporte(importe.getText()));
            resultado.setConcepto(concepto.getText());
            resultado.setCategoria(categoria.getText());
            resultado.setDocumento(documento.getText());
            resultado.setObservaciones(observaciones.getText());
            return resultado;
        });

        dialog.showAndWait().ifPresent(resultado -> {
            if (resultado.getFecha() == null || resultado.getTipo() == null || resultado.getConcepto() == null || resultado.getConcepto().isBlank()) {
                DialogUtils.showWarning("Fecha, tipo y concepto son obligatorios");
                return;
            }
            movimientoCajaService.save(resultado);
            cargarDatos();
            DialogUtils.showSuccess("Movimiento guardado correctamente");
        });
    }

    private BigDecimal calcularSaldoAcumulado(int index) {
        BigDecimal saldo = BigDecimal.ZERO;
        for (int i = 0; i <= index; i++) {
            MovimientoCaja movimiento = tableMovimientos.getItems().get(i);
            BigDecimal importe = movimiento.getImporte() != null ? movimiento.getImporte() : BigDecimal.ZERO;
            saldo = "GASTO".equals(movimiento.getTipo()) ? saldo.subtract(importe) : saldo.add(importe);
        }
        return saldo;
    }

    private String detalleMovimiento(MovimientoCaja movimiento) {
        return "Fecha: " + movimiento.getFecha() + "\n"
            + "Tipo: " + movimiento.getTipo() + "\n"
            + "Importe: " + formatoImporte(movimiento.getImporte()) + "\n"
            + "Concepto: " + movimiento.getConcepto() + "\n"
            + "Categoría: " + valor(movimiento.getCategoria()) + "\n"
            + "Documento: " + valor(movimiento.getDocumento()) + "\n"
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
