package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Controller;

@Controller
public class AlmacenController {
    @FXML private TableView<Almacen> tableAlmacenes;
    @FXML private TableColumn<Almacen, Long> colId;
    @FXML private TableColumn<Almacen, String> colNombre;
    @FXML private TableColumn<Almacen, String> colCodigo;

    private final AlmacenService almacenService;
    private final ObservableList<Almacen> almacenesList = FXCollections.observableArrayList();

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCodigo()));
        cargarAlmacenes();
    }

    private void cargarAlmacenes() {
        almacenesList.setAll(almacenService.findAll());
        tableAlmacenes.setItems(almacenesList);
    }

    @FXML
    public void handleNuevo() {
        Almacen nuevo = new Almacen();
        almacenService.save(nuevo);
        cargarAlmacenes();
        mostrarInfo("Almacén creado correctamente (simulado)");
    }

    @FXML
    public void handleEditar() {
        Almacen seleccionado = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un almacén para editar");
            return;
        }
        mostrarInfo("Funcionalidad de edición no implementada");
    }

    @FXML
    public void handleEliminar() {
        Almacen seleccionado = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un almacén para eliminar");
            return;
        }
        almacenService.delete(seleccionado);
        cargarAlmacenes();
        mostrarInfo("Almacén eliminado correctamente");
    }

    @FXML
    public void handleRefrescar() {
        cargarAlmacenes();
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

