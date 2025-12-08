package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

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
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigo()).orElse("")));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNombre()).orElse("")));

        if (tableAlmacenes != null) tableAlmacenes.setItems(almacenesList);
        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlmacenes(newV));
        }
    }

    private void loadAll() {
        try {
            log.info("Cargando almacenes desde la base de datos...");
            List<Almacen> todos = almacenService.findAll();
            log.info("Se encontraron {} almacenes en la base de datos", todos.size());
            almacenesList.setAll(todos);
            log.info("Almacenes cargados en la lista: {}", almacenesList.size());

            // Forzar actualización de la tabla en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                if (tableAlmacenes != null) {
                    tableAlmacenes.refresh();
                    log.info("Tabla de almacenes refrescada. Items: {}", tableAlmacenes.getItems().size());
                }
            });
        } catch (Exception e) {
            log.error("Error cargando almacenes", e);
            mostrarError("Error cargando almacenes: " + e.getMessage());
        }
    }

    private void filtrarAlmacenes(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Almacen> todos = almacenService.findAll();
            List<Almacen> encontrados = todos.stream()
                .filter(a -> (a.getCodigo() != null && a.getCodigo().toLowerCase().contains(filtro.toLowerCase())) ||
                            (a.getNombre() != null && a.getNombre().toLowerCase().contains(filtro.toLowerCase())))
                .toList();
            almacenesList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error filtrando almacenes", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarInfo("Funcionalidad de alta de almacén: implementa un formulario para crear nuevos almacenes.");
    }

    @FXML
    public void onEdit() {
        Almacen seleccionado = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un almacén para editar");
            return;
        }
        mostrarInfo("Funcionalidad de edición no implementada");
    }

    @FXML
    public void onDelete() {
        if (tableAlmacenes == null) { mostrarError("Tabla no disponible"); return; }
        Almacen sel = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarInfo("Selecciona un almacén para eliminar"); return; }
        if (sel.getId() == null) { mostrarError("El almacén seleccionado no tiene id"); return; }
        try {
            almacenService.delete(sel);
            loadAll();
            mostrarInfo("Almacén eliminado");
        } catch (Exception e) {
            log.error("Error eliminando almacén", e);
            mostrarError("Error eliminando almacén: " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.setHeaderText("Información");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}
