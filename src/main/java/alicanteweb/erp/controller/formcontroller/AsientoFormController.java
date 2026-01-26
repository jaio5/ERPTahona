package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.entities.LineaAsiento;
import alicanteweb.erp.service.AsientoContableService;
import alicanteweb.erp.ui.DialogUtils;
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
    private final ObservableList<LineaAsiento> lineasObservable = FXCollections.observableArrayList();

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

            // Configurar columnas declaradas en FXML
            if (colCuenta != null) {
                colCuenta.setCellValueFactory(cell -> {
                    if (cell.getValue() == null || cell.getValue().getCuenta() == null) return new ReadOnlyStringWrapper("");
                    return new ReadOnlyStringWrapper(cell.getValue().getCuenta().getCodigo());
                });
                colCuenta.setSortable(false);
            }

            if (colNombreCuenta != null) {
                colNombreCuenta.setCellValueFactory(cell -> {
                    if (cell.getValue() == null || cell.getValue().getCuenta() == null) return new ReadOnlyStringWrapper("");
                    return new ReadOnlyStringWrapper(cell.getValue().getCuenta().getNombre());
                });
                colNombreCuenta.setSortable(false);
            }

            if (colConcepto != null) {
                colConcepto.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue() != null ? cell.getValue().getConcepto() : ""));
                colConcepto.setCellFactory(TextFieldTableCell.forTableColumn());
                colConcepto.setOnEditCommit(evt -> {
                    LineaAsiento linea = evt.getRowValue();
                    if (linea != null) linea.setConcepto(evt.getNewValue());
                });
                colConcepto.setSortable(false);
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

            if (colDebe != null) {
                colDebe.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue() != null ? cell.getValue().getDebe() : BigDecimal.ZERO));
                colDebe.setCellFactory(column -> new TextFieldTableCell<>(bigDecimalConverter));
                colDebe.setOnEditCommit(evt -> {
                    LineaAsiento linea = evt.getRowValue();
                    if (linea != null) linea.setDebe(evt.getNewValue());
                    recalcularTotales();
                });
                colDebe.setSortable(false);
            }

            if (colHaber != null) {
                colHaber.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue() != null ? cell.getValue().getHaber() : BigDecimal.ZERO));
                colHaber.setCellFactory(column -> new TextFieldTableCell<>(bigDecimalConverter));
                colHaber.setOnEditCommit(evt -> {
                    LineaAsiento linea = evt.getRowValue();
                    if (linea != null) linea.setHaber(evt.getNewValue());
                    recalcularTotales();
                });
                colHaber.setSortable(false);
            }

            if (colAcciones != null) {
                colAcciones.setCellFactory(col -> new TableCell<>() {
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
                colAcciones.setSortable(false);
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

    @FXML
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

    private void mostrarError(String mensaje) { DialogUtils.showError(mensaje); }
    private void mostrarExito(String mensaje) { DialogUtils.showInfo(mensaje); }
 }
