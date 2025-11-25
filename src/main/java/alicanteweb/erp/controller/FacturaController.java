package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.FacturaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class FacturaController {

    private final FacturaService facturaService;
    private final ClienteService clienteService;

    @FXML
    private TableView<Factura> tableFacturas;

    @FXML
    private TableColumn<Factura, String> colNumero;

    @FXML
    private TableColumn<Factura, String> colFecha;

    @FXML
    private TableColumn<Factura, String> colCliente;

    @FXML
    private TableColumn<Factura, String> colTotal;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtNumero;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<Cliente> cboCliente;

    @FXML
    private TextField txtTotal;

    @FXML
    private TextField txtPagado;

    private final ObservableList<Factura> facturasObservable = FXCollections.observableArrayList();
    private final ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList();

    public FacturaController(FacturaService facturaService, ClienteService clienteService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (colNumero != null) colNumero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero()));
        if (colFecha != null) colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha() != null ? df.format(c.getValue().getFecha()) : ""));
        if (colCliente != null) colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente() != null ? c.getValue().getCliente().getNombre() : ""));
        if (colTotal != null) colTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotal() != null ? c.getValue().getTotal().toString() : ""));

        if (tableFacturas != null) tableFacturas.setItems(facturasObservable);
        if (cboCliente != null) {
            clientesObservable.addAll(clienteService.findAll());
            cboCliente.setItems(clientesObservable);
        }

        if (tableFacturas != null) {
            tableFacturas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtNumero != null) txtNumero.setText(newSel.getNumero() != null ? newSel.getNumero() : "");
                    if (dpFecha != null) dpFecha.setValue(newSel.getFecha());
                    if (cboCliente != null && newSel.getCliente() != null) cboCliente.getSelectionModel().select(newSel.getCliente());
                    if (txtTotal != null) txtTotal.setText(newSel.getTotal() != null ? newSel.getTotal().toString() : "");
                    if (txtPagado != null) txtPagado.setText(newSel.getPagado() != null ? newSel.getPagado().toString() : "");
                } else {
                    if (txtNumero != null) txtNumero.setText("");
                    if (dpFecha != null) dpFecha.setValue(null);
                    if (cboCliente != null) cboCliente.getSelectionModel().clearSelection();
                    if (txtTotal != null) txtTotal.setText("");
                    if (txtPagado != null) txtPagado.setText("");
                }
            });
        }

        loadAll();
    }

    private void loadAll() {
        facturasObservable.clear();
        List<Factura> todos = facturaService.findAll();
        facturasObservable.addAll(todos);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        facturasObservable.clear();
        facturaService.findByNumero(q).ifPresentOrElse(facturasObservable::add, this::loadAll);
    }

    @FXML
    public void handleNuevo() {
        if (txtNumero != null) txtNumero.setText("");
        if (dpFecha != null) dpFecha.setValue(null);
        if (cboCliente != null) cboCliente.getSelectionModel().clearSelection();
        if (txtTotal != null) txtTotal.setText("");
        if (txtPagado != null) txtPagado.setText("");
        if (tableFacturas != null) tableFacturas.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Factura selected = null;
        if (tableFacturas != null) selected = tableFacturas.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Factura();

        selected.setNumero(txtNumero != null ? txtNumero.getText() : null);
        selected.setFecha(dpFecha != null ? dpFecha.getValue() : null);
        selected.setCliente(cboCliente != null ? cboCliente.getSelectionModel().getSelectedItem() : null);
        try { if (txtTotal != null && !txtTotal.getText().isBlank()) selected.setTotal(new java.math.BigDecimal(txtTotal.getText())); } catch (Exception e) { }
        try { if (txtPagado != null && !txtPagado.getText().isBlank()) selected.setPagado(new java.math.BigDecimal(txtPagado.getText())); } catch (Exception e) { }

        // unicidad numero
        String numero = selected.getNumero();
        if (numero != null && !numero.isBlank()) {
            Optional<Factura> existe = facturaService.findByNumero(numero);
            if (existe.isPresent()) {
                Factura f = existe.get();
                if (selected.getId() == null || !f.getId().equals(selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "El número de factura ya existe").showAndWait();
                    return;
                }
            }
        }

        facturaService.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        Factura selected = null;
        if (tableFacturas != null) selected = tableFacturas.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        facturaService.deleteById(selected.getId());
        loadAll();
    }
}

