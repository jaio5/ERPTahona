package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Controller;

@Controller
public class ClienteController {
    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colCif;
    @FXML private TableColumn<Cliente, String> colDireccion;

    private final ClienteService clienteService;
    private final ObservableList<Cliente> clientesList = FXCollections.observableArrayList();

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));
        if (colCif != null) colCif.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCif()));
        if (colDireccion != null) colDireccion.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDireccion()));
        cargarClientes();
    }

    private void cargarClientes() {
        clientesList.setAll(clienteService.findAll());
        tableClientes.setItems(clientesList);
    }

    @FXML
    public void handleNuevo() {
        Cliente nuevo = new Cliente();
        clienteService.save(nuevo);
        cargarClientes();
        mostrarInfo("Cliente creado correctamente (simulado)");
    }

    @FXML
    public void handleEditar() {
        Cliente seleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un cliente para editar");
            return;
        }
        mostrarInfo("Funcionalidad de edición no implementada");
    }

    @FXML
    public void handleEliminar() {
        Cliente seleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un cliente para eliminar");
            return;
        }
        clienteService.deleteById(seleccionado.getId());
        cargarClientes();
        mostrarInfo("Cliente eliminado correctamente");
    }

    @FXML
    public void handleRefrescar() {
        cargarClientes();
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
