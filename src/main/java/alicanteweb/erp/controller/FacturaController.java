package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Controller;

@Controller
public class FacturaController {
    @FXML private TableView<Factura> tableFacturas;
    @FXML private TableColumn<Factura, Long> colId;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colCliente;

    private final FacturaService facturaService;
    private final ObservableList<Factura> facturasList = FXCollections.observableArrayList();

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        if (colNumero != null) colNumero.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumero()));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFecha() != null ? cell.getValue().getFecha().toString() : ""));
        if (colCliente != null) colCliente.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getCliente() != null ? cell.getValue().getCliente().getNombre() : ""
        ));
        cargarFacturas();
    }

    private void cargarFacturas() {
        facturasList.setAll(facturaService.findAll());
        tableFacturas.setItems(facturasList);
    }

    @FXML
    public void handleNuevo() {
        Factura nueva = new Factura();
        facturaService.save(nueva);
        cargarFacturas();
        mostrarInfo("Factura creada correctamente (simulado)");
    }

    @FXML
    public void handleEditar() {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una factura para editar");
            return;
        }
        mostrarInfo("Funcionalidad de edición no implementada");
    }

    @FXML
    public void handleEliminar() {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una factura para eliminar");
            return;
        }
        facturaService.deleteById(seleccionada.getId());
        cargarFacturas();
        mostrarInfo("Factura eliminada correctamente");
    }

    @FXML
    public void handleRefrescar() {
        cargarFacturas();
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
