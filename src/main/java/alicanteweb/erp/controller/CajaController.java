package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Caja;
import alicanteweb.erp.service.CajaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class CajaController {
    private static final Logger log = LoggerFactory.getLogger(CajaController.class);

    @FXML private TableView<Caja> tableCaja;
    @FXML private TableColumn<Caja, Long> colId;
    @FXML private TableColumn<Caja, String> colCodigo;
    @FXML private TableColumn<Caja, String> colNombre;
    @FXML private TableColumn<Caja, BigDecimal> colSaldo;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private VBox formularioPanel;

    private final CajaService cajaService;
    private final ObservableList<Caja> cajaList = FXCollections.observableArrayList();
    private Caja cajaActual = null;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando CajaController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarCaja(newV));
        }
    }

    private void configurarColumnas() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        }
        if (colCodigo != null) {
            colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigo()).orElse("")));
        }
        if (colNombre != null) {
            colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNombre()).orElse("")));
        }
        if (colSaldo != null) {
            colSaldo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? BigDecimal.ZERO : cell.getValue().getSaldoActual()));
        }
        if (tableCaja != null) {
            tableCaja.setItems(cajaList);
        }
    }

    private void cargarDatos() {
        try {
            cajaList.clear();
            cajaList.addAll(cajaService.obtenerTodas());
            log.info("Cajas cargadas: {}", cajaList.size());
            javafx.application.Platform.runLater(() -> {
                if (tableCaja != null) tableCaja.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando cajas", e);
            mostrarError("Error al cargar cajas: " + e.getMessage());
        }
    }

    private void filtrarCaja(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<Caja> filtered = FXCollections.observableArrayList(
            cajaList.stream()
                .filter(c -> (c.getCodigo() != null && c.getCodigo().toLowerCase().contains(search)) ||
                             (c.getNombre() != null && c.getNombre().toLowerCase().contains(search)))
                .toList()
        );
        tableCaja.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        cajaActual = new Caja();
        limpiarFormulario();
        mostrarFormulario(true);
    }

    @FXML
    public void onEditar() {
        Caja caja = tableCaja.getSelectionModel().getSelectedItem();
        if (caja == null) {
            mostrarAlerta("Seleccione una caja para editar");
            return;
        }
        cajaActual = caja;
        if (txtCodigo != null) txtCodigo.setText(caja.getCodigo());
        if (txtNombre != null) txtNombre.setText(caja.getNombre());
        if (txtObservaciones != null) txtObservaciones.setText(Optional.ofNullable(caja.getObservaciones()).orElse(""));
        mostrarFormulario(true);
    }

    @FXML
    public void onEliminar() {
        Caja caja = tableCaja.getSelectionModel().getSelectedItem();
        if (caja == null) {
            mostrarAlerta("Seleccione una caja para eliminar");
            return;
        }
        if (mostrarConfirmacion("¿Desea eliminar esta caja?")) {
            try {
                cajaService.eliminar(caja.getId());
                cargarDatos();
                mostrarExito("Caja eliminada correctamente");
            } catch (Exception e) {
                log.error("Error eliminando caja", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onGuardar() {
        if (txtCodigo == null || txtCodigo.getText().isEmpty() ||
            txtNombre == null || txtNombre.getText().isEmpty()) {
            mostrarAlerta("Complete los campos obligatorios");
            return;
        }
        try {
            cajaActual.setCodigo(txtCodigo.getText());
            cajaActual.setNombre(txtNombre.getText());
            cajaActual.setObservaciones(txtObservaciones != null ? txtObservaciones.getText() : "");
            if (cajaActual.getSaldoInicial() == null) {
                cajaActual.setSaldoInicial(BigDecimal.ZERO);
            }
            if (cajaActual.getSaldoActual() == null) {
                cajaActual.setSaldoActual(cajaActual.getSaldoInicial());
            }
            cajaService.guardar(cajaActual);
            cargarDatos();
            mostrarFormulario(false);
            mostrarExito("Caja guardada correctamente");
        } catch (Exception e) {
            log.error("Error guardando caja", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        mostrarFormulario(false);
        limpiarFormulario();
    }

    @FXML
    public void onRefresh() {
        cargarDatos();
    }

    private void mostrarFormulario(boolean mostrar) {
        if (formularioPanel != null) formularioPanel.setVisible(mostrar);
        if (btnGuardar != null) btnGuardar.setDisable(!mostrar);
        if (btnCancelar != null) btnCancelar.setDisable(!mostrar);
    }

    private void limpiarFormulario() {
        if (txtCodigo != null) txtCodigo.clear();
        if (txtNombre != null) txtNombre.clear();
        if (txtObservaciones != null) txtObservaciones.clear();
        cajaActual = null;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

