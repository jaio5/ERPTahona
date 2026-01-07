package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.service.AsientoAutomaticoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AsientoContableController {
    private static final Logger log = LoggerFactory.getLogger(AsientoContableController.class);

    @FXML private TableView<AsientoContable> tableAsientos;
    @FXML private TableColumn<AsientoContable, Long> colId;
    @FXML private TableColumn<AsientoContable, Integer> colNumero;
    @FXML private TableColumn<AsientoContable, String> colFecha;
    @FXML private TableColumn<AsientoContable, String> colConcepto;
    @FXML private TableColumn<AsientoContable, String> colTipo;
    @FXML private TextField txtBuscar;

    private final AsientoAutomaticoService asientoService;
    private final ObservableList<AsientoContable> asientosList = FXCollections.observableArrayList();

    public AsientoContableController(AsientoAutomaticoService asientoService) {
        this.asientoService = asientoService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AsientoContableController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAsientos(newV));
        }
    }

    private void configurarColumnas() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        }
        if (colNumero != null) {
            colNumero.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getNumero()));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFecha()).map(f -> f.toString()).orElse("")));
        }
        if (colConcepto != null) {
            colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getConcepto()).orElse("")));
        }
        if (colTipo != null) {
            colTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getTipo()).orElse("")));
        }
        if (tableAsientos != null) {
            tableAsientos.setItems(asientosList);
        }
    }

    private void cargarDatos() {
        try {
            asientosList.clear();
            log.info("Asientos cargados: {}", asientosList.size());
            javafx.application.Platform.runLater(() -> {
                if (tableAsientos != null) tableAsientos.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando asientos", e);
            mostrarError("Error al cargar asientos: " + e.getMessage());
        }
    }

    private void filtrarAsientos(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<AsientoContable> filtered = FXCollections.observableArrayList(
            asientosList.stream()
                .filter(a -> (a.getConcepto() != null && a.getConcepto().toLowerCase().contains(search)) ||
                             (a.getTipo() != null && a.getTipo().toLowerCase().contains(search)))
                .toList()
        );
        tableAsientos.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        mostrarInfo("Crear nuevo asiento contable");
        cargarDatos();
    }

    @FXML
    public void onEditar() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            mostrarAlerta("Seleccione un asiento para editar");
            return;
        }
        mostrarInfo("Editando asiento: " + asiento.getConcepto());
    }

    @FXML
    public void onEliminar() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            mostrarAlerta("Seleccione un asiento para eliminar");
            return;
        }
        if (mostrarConfirmacion("¿Desea eliminar este asiento?")) {
            mostrarExito("Asiento eliminado");
            cargarDatos();
        }
    }

    @FXML
    public void onRefresh() {
        cargarDatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
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

