package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
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

/**
 * Controlador para la gestión de Almacenes
 */
@Controller
public class AlmacenController {
    private static final Logger log = LoggerFactory.getLogger(AlmacenController.class);

    @FXML private TableView<Almacen> tableAlmacenes;
    @FXML private TableColumn<Almacen, Long> colId;
    @FXML private TableColumn<Almacen, String> colCodigo;
    @FXML private TableColumn<Almacen, String> colNombre;
    @FXML private TextField txtBuscar;

    private final AlmacenService almacenService;
    private final ObservableList<Almacen> almacenesList = FXCollections.observableArrayList();

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AlmacenController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlmacenes(newV));
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
        if (tableAlmacenes != null) {
            tableAlmacenes.setItems(almacenesList);
        }
    }

    private void cargarDatos() {
        try {
            almacenesList.clear();
            almacenesList.addAll(almacenService.findAll());
            log.info("Almacenes cargados: {}", almacenesList.size());
            javafx.application.Platform.runLater(() -> {
                if (tableAlmacenes != null) tableAlmacenes.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando almacenes", e);
            mostrarError("Error al cargar almacenes: " + e.getMessage());
        }
    }

    private void filtrarAlmacenes(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<Almacen> filtered = FXCollections.observableArrayList(
            almacenesList.stream()
                .filter(a -> (a.getCodigo() != null && a.getCodigo().toLowerCase().contains(search)) ||
                             (a.getNombre() != null && a.getNombre().toLowerCase().contains(search)))
                .toList()
        );
        tableAlmacenes.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        mostrarAlerta("Crear nuevo almacen en desarrollo");
    }

    @FXML
    public void onEditar() {
        Almacen almacen = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (almacen == null) {
            mostrarAlerta("Seleccione un almacen para editar");
            return;
        }
        mostrarAlerta("Edición de almacen en desarrollo");
    }

    @FXML
    public void onEliminar() {
        Almacen almacen = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (almacen == null) {
            mostrarAlerta("Seleccione un almacen para eliminar");
            return;
        }
        if (mostrarConfirmacion("¿Desea eliminar este almacen?")) {
            try {
                almacenService.deleteById(almacen.getId());
                cargarDatos();
                mostrarExito("Almacen eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando almacen", e);
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

