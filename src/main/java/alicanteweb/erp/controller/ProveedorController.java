package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Controller;

@Controller
public class ProveedorController {
    @FXML private TableView<Proveedor> tableProveedores;
    @FXML private TableColumn<Proveedor, Long> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colCif;
    @FXML private TableColumn<Proveedor, String> colDireccion;

    private final ProveedorService proveedorService;
    private final ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList();

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId().longValue()));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));
        if (colCif != null) colCif.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCif()));
        if (colDireccion != null) colDireccion.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDireccion()));
        cargarProveedores();
    }

    private void cargarProveedores() {
        proveedoresList.setAll(proveedorService.findAll());
        tableProveedores.setItems(proveedoresList);
    }

    @FXML
    public void handleNuevo() {
        Proveedor nuevo = new Proveedor();
        proveedorService.save(nuevo);
        cargarProveedores();
        mostrarInfo("Proveedor creado correctamente (simulado)");
    }

    @FXML
    public void handleEditar() {
        Proveedor seleccionado = tableProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor para editar");
            return;
        }
        mostrarInfo("Funcionalidad de edición no implementada");
    }

    @FXML
    public void handleEliminar() {
        Proveedor seleccionado = tableProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor para eliminar");
            return;
        }
        proveedorService.deleteById(seleccionado.getId().longValue());
        cargarProveedores();
        mostrarInfo("Proveedor eliminado correctamente");
    }

    @FXML
    public void handleRefrescar() {
        cargarProveedores();
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
