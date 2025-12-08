package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
public class FacturaController {
    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    @FXML private TableView<Factura> tableFacturas;
    @FXML private TableColumn<Factura, Long> colId;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colTotal;
    @FXML private TableColumn<Factura, String> colPagado;
    @FXML private TableColumn<Factura, String> colEstado;
    @FXML private TextField txtBuscar;

    private final FacturaService facturaService;
    private final ObservableList<Factura> facturasList = FXCollections.observableArrayList();

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colNumero != null) colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).orElse("")));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().toString()));
        if (colCliente != null) colCliente.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() == null || cell.getValue().getCliente() == null ? "" : Optional.ofNullable(cell.getValue().getCliente().getNombre()).orElse("")
        ));
        if (colTotal != null) colTotal.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getTotal() == null ? "" : cell.getValue().getTotal().toString()));
        if (colPagado != null) colPagado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getPagado() == null ? "" : cell.getValue().getPagado().toString()));
        if (colEstado != null) colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : (cell.getValue().isPagada() ? "Pagada" : "Pendiente")));

        if (tableFacturas != null) tableFacturas.setItems(facturasList);
        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarFacturas(newV));
        }
    }

    private void loadAll() {
        try {
            List<Factura> todas = facturaService.findAll();
            facturasList.setAll(todas);
        } catch (Exception e) {
            log.error("Error cargando facturas", e);
            mostrarError("Error cargando facturas: " + e.getMessage());
        }
    }

    private void filtrarFacturas(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Factura> todas = facturaService.findAll();
            List<Factura> encontradas = todas.stream()
                .filter(f -> f.getNumero() != null && f.getNumero().toLowerCase().contains(filtro.toLowerCase()))
                .toList();
            facturasList.setAll(encontradas);
        } catch (Exception e) {
            log.error("Error filtrando facturas", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarInfo("Funcionalidad de alta de factura: implementa un formulario para crear nuevas facturas.");
    }

    @FXML
    public void onEdit() {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una factura para ver detalles");
            return;
        }
        mostrarInfo("Funcionalidad de ver detalles no implementada");
    }

    @FXML
    public void onPrint() {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una factura para imprimir");
            return;
        }
        mostrarInfo("Funcionalidad de impresión no implementada");
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
