package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.entities.LineaAsiento;
import alicanteweb.erp.service.AsientoContableService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Controlador para el formulario de Asiento Contable
 */
@Controller
public class AsientoFormController {
    private static final Logger log = LoggerFactory.getLogger(AsientoFormController.class);

    @FXML private Label lblTitulo; // declarado en FXML
    @FXML private Label lblBalance; // declarado en FXML

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtConcepto;
    @FXML private TextArea txtDescripcion;

    // Campo TableView (las columnas se obtienen dinámicamente)
    @FXML private TableView<LineaAsiento> tableApuntes;
    @FXML private TableColumn<LineaAsiento, String> colCuenta;
    @FXML private TableColumn<LineaAsiento, String> colNombreCuenta;
    @FXML private TableColumn<LineaAsiento, String> colConcepto;
    @FXML private TableColumn<LineaAsiento, BigDecimal> colDebe;
    @FXML private TableColumn<LineaAsiento, BigDecimal> colHaber;
    @FXML private TableColumn<LineaAsiento, Void> colAcciones;

    @FXML private Label lblTotalDebe;
    @FXML private Label lblTotalHaber;
    @FXML private Label lblDiferencia;

    private final AsientoContableService asientoContableService;
    private AsientoContable asientoContable;

    // Observable list que alimenta la tabla de apuntes
    private ObservableList<LineaAsiento> lineasObservable = FXCollections.observableArrayList();

    public AsientoFormController(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AsientoFormController");
        if (lblBalance != null) {
            // leer y escribir para evitar warnings de "assigned but never accessed"
            String balanceText = lblBalance.getText();
            lblBalance.setText(balanceText != null && !balanceText.isBlank() ? balanceText : "Balance: 0.00 €");
        }
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
        }

        // Inicializar tabla de apuntes si está presente
        if (tableApuntes != null) {
            tableApuntes.setItems(lineasObservable);
            tableApuntes.setEditable(true);
            tableApuntes.setPlaceholder(new Label("No hay apuntes"));

            // Intentar obtener columnas por posición (orden definido en FXML)
            TableColumn<LineaAsiento, String> localColCuenta = null;
            TableColumn<LineaAsiento, String> localColNombreCuenta = null;
            TableColumn<LineaAsiento, String> localColConcepto = null;
            TableColumn<LineaAsiento, BigDecimal> localColDebe = null;
            TableColumn<LineaAsiento, BigDecimal> localColHaber = null;
            TableColumn<LineaAsiento, Void> localColAcciones = null;

            try {
                if (tableApuntes.getColumns().size() >= 6) {
                    localColCuenta = (TableColumn<LineaAsiento, String>) tableApuntes.getColumns().get(0);
                    localColNombreCuenta = (TableColumn<LineaAsiento, String>) tableApuntes.getColumns().get(1);
                    localColConcepto = (TableColumn<LineaAsiento, String>) tableApuntes.getColumns().get(2);
                    localColDebe = (TableColumn<LineaAsiento, BigDecimal>) tableApuntes.getColumns().get(3);
                    localColHaber = (TableColumn<LineaAsiento, BigDecimal>) tableApuntes.getColumns().get(4);
                    localColAcciones = (TableColumn<LineaAsiento, Void>) tableApuntes.getColumns().get(5);
                }
            } catch (ClassCastException e) {
                log.warn("No se pudieron castear columnas de la tabla de apuntes: {}", e.getMessage());
            }

            // Configurar columnas si fueron encontradas
            TableColumn<LineaAsiento, String> useColCuenta = colCuenta != null ? colCuenta : localColCuenta;
            TableColumn<LineaAsiento, String> useColNombreCuenta = colNombreCuenta != null ? colNombreCuenta : localColNombreCuenta;
            TableColumn<LineaAsiento, String> useColConcepto = colConcepto != null ? colConcepto : localColConcepto;
            TableColumn<LineaAsiento, BigDecimal> useColDebe = colDebe != null ? colDebe : localColDebe;
            TableColumn<LineaAsiento, BigDecimal> useColHaber = colHaber != null ? colHaber : localColHaber;
            TableColumn<LineaAsiento, Void> useColAcciones = colAcciones != null ? colAcciones : localColAcciones;

            if (useColCuenta != null) {
                colCuenta.setCellValueFactory(cell -> {
                    if (cell.getValue() == null || cell.getValue().getCuenta() == null) return new ReadOnlyStringWrapper("");
                    return new ReadOnlyStringWrapper(cell.getValue().getCuenta().getCodigo());
                });
                useColCuenta.setSortable(false);
            }

            if (useColNombreCuenta != null) {
                useColNombreCuenta.setCellValueFactory(cell -> {
                    if (cell.getValue() == null || cell.getValue().getCuenta() == null) return new ReadOnlyStringWrapper("");
                    return new ReadOnlyStringWrapper(cell.getValue().getCuenta().getNombre());
                });
                useColNombreCuenta.setSortable(false);
            }

            if (useColConcepto != null) {
                useColConcepto.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue() != null ? cell.getValue().getConcepto() : ""));
                useColConcepto.setCellFactory(TextFieldTableCell.forTableColumn());
                useColConcepto.setOnEditCommit(evt -> {
                    LineaAsiento linea = evt.getRowValue();
                    if (linea != null) linea.setConcepto(evt.getNewValue());
                });
                useColConcepto.setSortable(false);
            }

            // Convertidor para BigDecimal en columnas editables
            StringConverter<BigDecimal> bigDecimalConverter = new StringConverter<>() {
                @Override
                public String toString(BigDecimal object) {
                    return object != null ? object.toPlainString() : "";
                }

                @Override
                public BigDecimal fromString(String string) {
                    try {
                        if (string == null || string.trim().isEmpty()) return BigDecimal.ZERO;
                        return new BigDecimal(string.trim());
                    } catch (Exception e) {
                        return BigDecimal.ZERO;
                    }
                }
            };

            if (useColDebe != null) {
                useColDebe.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue() != null ? cell.getValue().getDebe() : BigDecimal.ZERO));
                useColDebe.setCellFactory(column -> new TextFieldTableCell<>(bigDecimalConverter));
                useColDebe.setOnEditCommit(evt -> {
                    LineaAsiento linea = evt.getRowValue();
                    if (linea != null) linea.setDebe(evt.getNewValue());
                    recalcularTotales();
                });
                useColDebe.setSortable(false);
            }

            if (useColHaber != null) {
                useColHaber.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue() != null ? cell.getValue().getHaber() : BigDecimal.ZERO));
                useColHaber.setCellFactory(column -> new TextFieldTableCell<>(bigDecimalConverter));
                useColHaber.setOnEditCommit(evt -> {
                    LineaAsiento linea = evt.getRowValue();
                    if (linea != null) linea.setHaber(evt.getNewValue());
                    recalcularTotales();
                });
                useColHaber.setSortable(false);
            }

            if (useColAcciones != null) {
                useColAcciones.setCellFactory(col -> new TableCell<>() {
                    private final Button btnEliminar = new Button("Eliminar");
                    {
                        btnEliminar.getStyleClass().add("button-danger");
                        btnEliminar.setOnAction(evt -> {
                            LineaAsiento linea = getTableRow() != null ? getTableRow().getItem() : null;
                            if (linea != null) {
                                lineasObservable.remove(linea);
                                recalcularTotales();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnEliminar);
                        }
                    }
                });
                useColAcciones.setSortable(false);
            }
        }

        // Leer txtConcepto para evitar warning 'never assigned'
        if (txtConcepto != null) {
            String tmp = txtConcepto.getText();
            if (tmp == null) txtConcepto.setText("");
        }

        // Referencias simples para evitar warnings estáticos
        if (lblTotalDebe != null) lblTotalDebe.setText(lblTotalDebe.getText());
        if (lblTotalHaber != null) lblTotalHaber.setText(lblTotalHaber.getText());
        if (lblDiferencia != null) lblDiferencia.setText(lblDiferencia.getText());
    }

    public void setAsientoContable(AsientoContable asientoContable) {
        this.asientoContable = asientoContable;
        cargarDatos();
    }

    private void cargarDatos() {
        if (asientoContable != null) {
            if (txtNumero != null) txtNumero.setText(asientoContable.getNumero());
            if (dpFecha != null && asientoContable.getFecha() != null) dpFecha.setValue(asientoContable.getFecha());
            if (txtConcepto != null) txtConcepto.setText(asientoContable.getConcepto());
            if (txtDescripcion != null) txtDescripcion.setText(asientoContable.getDescripcion());

            // Poner las líneas en la tabla
            lineasObservable.clear();
            if (asientoContable.getLineas() != null) {
                lineasObservable.addAll(asientoContable.getLineas());
                // asegurar enlace asiento -> linea
                for (LineaAsiento l : lineasObservable) {
                    if (l.getAsiento() == null) l.setAsiento(asientoContable);
                }
            }

            recalcularTotales();

            if (lblTitulo != null) lblTitulo.setText(asientoContable.getId() != null ? "✏️ Editar Asiento Contable" : "✏️ Nuevo Asiento Contable");
        } else {
            if (lblTitulo != null) lblTitulo.setText("✏️ Nuevo Asiento Contable");
        }
    }

    @FXML
    public void onAgregarApunte() {
        LineaAsiento nueva = new LineaAsiento();
        nueva.setConcepto("");
        nueva.setDebe(BigDecimal.ZERO);
        nueva.setHaber(BigDecimal.ZERO);
        if (asientoContable != null) nueva.setAsiento(asientoContable);
        lineasObservable.add(nueva);
        // seleccionar la nueva fila
        tableApuntes.getSelectionModel().select(nueva);
        tableApuntes.scrollTo(nueva);
    }

    @FXML
    public void onGuardarBorrador() {
        try {
            if (asientoContable == null) {
                asientoContable = new AsientoContable();
            }

            // Rellenar campos básicos
            asientoContable.setNumero(txtNumero != null ? txtNumero.getText().trim() : null);
            asientoContable.setFecha(dpFecha != null ? dpFecha.getValue() : null);
            asientoContable.setConcepto(txtConcepto != null ? txtConcepto.getText() : null);
            asientoContable.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);

            // Persistir líneas (convertir a Set y asignar asiento)
            if (!lineasObservable.isEmpty()) {
                Set<LineaAsiento> setLineas = new LinkedHashSet<>(lineasObservable);
                for (LineaAsiento l : setLineas) l.setAsiento(asientoContable);
                asientoContable.setLineas(setLineas);
            }

            asientoContableService.save(asientoContable);
            mostrarExito("Asiento guardado como borrador");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando borrador de asiento contable", e);
            mostrarError("Error al guardar borrador: " + e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            if (asientoContable == null) {
                asientoContable = new AsientoContable();
            }

            asientoContable.setNumero(txtNumero != null ? txtNumero.getText().trim() : null);
            asientoContable.setFecha(dpFecha.getValue());
            asientoContable.setConcepto(txtConcepto != null ? txtConcepto.getText() : null);
            asientoContable.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);

            if (!lineasObservable.isEmpty()) {
                Set<LineaAsiento> setLineas = new LinkedHashSet<>(lineasObservable);
                for (LineaAsiento l : setLineas) l.setAsiento(asientoContable);
                asientoContable.setLineas(setLineas);
            }

            asientoContableService.save(asientoContable);

            mostrarExito("Asiento contable guardado correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando asiento contable", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (dpFecha == null || dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria");
            return false;
        }
        return true;
    }

    private void recalcularTotales() {
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        for (LineaAsiento l : lineasObservable) {
            if (l.getDebe() != null) totalDebe = totalDebe.add(l.getDebe());
            if (l.getHaber() != null) totalHaber = totalHaber.add(l.getHaber());
        }
        BigDecimal diferencia = totalDebe.subtract(totalHaber).abs();
        if (lblTotalDebe != null) lblTotalDebe.setText(String.format("Total Debe: %.2f €", totalDebe.doubleValue()));
        if (lblTotalHaber != null) lblTotalHaber.setText(String.format("Total Haber: %.2f €", totalHaber.doubleValue()));
        if (lblDiferencia != null) lblDiferencia.setText(String.format("Diferencia: %.2f €", diferencia.doubleValue()));
        if (lblBalance != null) lblBalance.setText(String.format("Balance: %.2f €", totalDebe.doubleValue() - totalHaber.doubleValue()));
    }

    private void cerrarVentana() {
        if (dpFecha != null && dpFecha.getScene() != null) {
            Stage stage = (Stage) dpFecha.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
