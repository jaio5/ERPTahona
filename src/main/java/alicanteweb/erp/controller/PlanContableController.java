package alicanteweb.erp.controller;

import alicanteweb.erp.entities.PlanContable;
import alicanteweb.erp.service.PlanContableService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Controller
public class PlanContableController {
    private static final Logger log = LoggerFactory.getLogger(PlanContableController.class);

    @FXML private TableView<PlanContable> tableCuentas;
    @FXML private TableColumn<PlanContable, Long> colId;
    @FXML private TableColumn<PlanContable, String> colCodigo;
    @FXML private TableColumn<PlanContable, String> colNombre;
    @FXML private TableColumn<PlanContable, String> colTipo;
    @FXML private TableColumn<PlanContable, Integer> colNivel;
    @FXML private TextField txtBuscar;

    private final PlanContableService planContableService;
    private final ObservableList<PlanContable> cuentasList = FXCollections.observableArrayList();

    public PlanContableController(PlanContableService planContableService) {
        this.planContableService = planContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PlanContableController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarCuentas(newV));
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
        if (colTipo != null) {
            colTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getTipo()).orElse("")));
        }
        if (colNivel != null) {
            colNivel.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getNivel()));
        }
        if (tableCuentas != null) {
            tableCuentas.setItems(cuentasList);
        }
    }

    private void cargarDatos() {
        try {
            cuentasList.clear();
            cuentasList.addAll(planContableService.obtenerTodas());
            log.info("Cuentas contables cargadas: {}", cuentasList.size());
            javafx.application.Platform.runLater(() -> {
                if (tableCuentas != null) tableCuentas.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando plan contable", e);
            mostrarError("Error al cargar plan contable: " + e.getMessage());
        }
    }

    private void filtrarCuentas(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<PlanContable> filtered = FXCollections.observableArrayList(
            cuentasList.stream()
                .filter(c -> (c.getCodigo() != null && c.getCodigo().toLowerCase().contains(search)) ||
                             (c.getNombre() != null && c.getNombre().toLowerCase().contains(search)))
                .toList()
        );
        tableCuentas.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        mostrarAlerta("Función de crear nueva cuenta en desarrollo");
    }

    @FXML
    public void onEditar() {
        PlanContable cuenta = tableCuentas.getSelectionModel().getSelectedItem();
        if (cuenta == null) {
            mostrarAlerta("Seleccione una cuenta para editar");
            return;
        }
        mostrarAlerta("Edición de cuenta en desarrollo");
    }

    @FXML
    public void onEliminar() {
        PlanContable cuenta = tableCuentas.getSelectionModel().getSelectedItem();
        if (cuenta == null) {
            mostrarAlerta("Seleccione una cuenta para eliminar");
            return;
        }
        if (mostrarConfirmacion("¿Desea eliminar esta cuenta contable?")) {
            try {
                planContableService.eliminar(cuenta.getId());
                cargarDatos();
                mostrarExito("Cuenta eliminada correctamente");
            } catch (Exception e) {
                log.error("Error eliminando cuenta", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
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

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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

