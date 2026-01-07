package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.service.PresupuestoService;
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
public class PresupuestoController {
    private static final Logger log = LoggerFactory.getLogger(PresupuestoController.class);

    @FXML private TableView<Presupuesto> tablePresupuestos;
    @FXML private TableColumn<Presupuesto, Long> colId;
    @FXML private TableColumn<Presupuesto, String> colNumero;
    @FXML private TableColumn<Presupuesto, String> colFecha;
    @FXML private TableColumn<Presupuesto, String> colCliente;
    @FXML private TableColumn<Presupuesto, String> colEstado;
    @FXML private TextField txtBuscar;

    private final PresupuestoService presupuestoService;
    private final ObservableList<Presupuesto> presupuestosList = FXCollections.observableArrayList();

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PresupuestoController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarPresupuestos(newV));
        }
    }

    private void configurarColumnas() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        }
        if (colNumero != null) {
            colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).map(Object::toString).orElse("")));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFecha()).map(Object::toString).orElse("")));
        }
        if (colCliente != null) {
            colCliente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCliente()).map(c -> c.getNombre()).orElse("")));
        }
        if (colEstado != null) {
            colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getEstado()).orElse("")));
        }
        if (tablePresupuestos != null) {
            tablePresupuestos.setItems(presupuestosList);
        }
    }

    private void cargarDatos() {
        try {
            presupuestosList.clear();
            presupuestosList.addAll(presupuestoService.obtenerTodos());
            log.info("Presupuestos cargados: {}", presupuestosList.size());
            javafx.application.Platform.runLater(() -> {
                if (tablePresupuestos != null) tablePresupuestos.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando presupuestos", e);
            mostrarError("Error al cargar presupuestos: " + e.getMessage());
        }
    }

    private void filtrarPresupuestos(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<Presupuesto> filtered = FXCollections.observableArrayList(
            presupuestosList.stream()
                .filter(p -> (p.getCliente() != null && p.getCliente().getNombre().toLowerCase().contains(search)))
                .toList()
        );
        tablePresupuestos.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        mostrarAlerta("Crear nuevo presupuesto en desarrollo");
    }

    @FXML
    public void onEditar() {
        Presupuesto presupuesto = tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Seleccione un presupuesto para editar");
            return;
        }
        mostrarAlerta("Edición de presupuesto en desarrollo");
    }

    @FXML
    public void onEliminar() {
        Presupuesto presupuesto = tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Seleccione un presupuesto para eliminar");
            return;
        }
        if (mostrarConfirmacion("¿Desea eliminar este presupuesto?")) {
            try {
                presupuestoService.eliminar(presupuesto.getId());
                cargarDatos();
                mostrarExito("Presupuesto eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando presupuesto", e);
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

