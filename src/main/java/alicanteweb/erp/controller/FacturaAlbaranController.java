package alicanteweb.erp.controller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.util.List;

@SuppressWarnings("unused")
@Controller
public class FacturaAlbaranController {

    private final FacturaAlbaranService facturaAlbaranService;
    private final FacturaService facturaService;
    private final AlbaranesVentaService albaranesVentaService;

    @FXML
    private TableView<FacturaAlbaran> tableFA;

    @FXML
    private TableColumn<FacturaAlbaran, String> colFactura;

    @FXML
    private TableColumn<FacturaAlbaran, String> colAlbaran;

    @FXML
    private ComboBox<Factura> cboFactura;

    @FXML
    private ComboBox<AlbaranesVenta> cboAlbaran;

    private final ObservableList<FacturaAlbaran> faObservable = FXCollections.observableArrayList();
    private final ObservableList<Factura> facturasObservable = FXCollections.observableArrayList();
    private final ObservableList<AlbaranesVenta> albaranesObservable = FXCollections.observableArrayList();

    public FacturaAlbaranController(FacturaAlbaranService facturaAlbaranService, FacturaService facturaService, AlbaranesVentaService albaranesVentaService) {
        this.facturaAlbaranService = facturaAlbaranService;
        this.facturaService = facturaService;
        this.albaranesVentaService = albaranesVentaService;
    }

    @FXML
    public void initialize() {
        if (tableFA != null) tableFA.setItems(faObservable);
        if (cboFactura != null) {
            facturasObservable.addAll(facturaService.findAll());
            cboFactura.setItems(facturasObservable);
        }
        if (cboAlbaran != null) {
            albaranesObservable.addAll(albaranesVentaService.findAll());
            cboAlbaran.setItems(albaranesObservable);
        }

        if (colFactura != null) colFactura.setCellValueFactory(fa -> new SimpleStringProperty(fa.getValue().getFactura() != null ? fa.getValue().getFactura().getNumero() : ""));
        if (colAlbaran != null) colAlbaran.setCellValueFactory(fa -> new SimpleStringProperty(fa.getValue().getAlbaran() != null ? fa.getValue().getAlbaran().getNumero() : ""));

        loadAll();
    }

    private void loadAll() {
        faObservable.clear();
        List<FacturaAlbaran> all = facturaAlbaranService.findAll();
        faObservable.addAll(all);
    }

    @FXML
    public void handleNuevo() {
        if (cboFactura != null) cboFactura.getSelectionModel().clearSelection();
        if (cboAlbaran != null) cboAlbaran.getSelectionModel().clearSelection();
        if (tableFA != null) tableFA.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        if (cboFactura == null || cboAlbaran == null) return;
        Factura f = cboFactura.getSelectionModel().getSelectedItem();
        AlbaranesVenta a = cboAlbaran.getSelectionModel().getSelectedItem();
        if (f == null || a == null) return;

        FacturaAlbaran fa = new FacturaAlbaran();
        FacturaAlbaranId id = new FacturaAlbaranId();
        id.setFacturaId(f.getId());
        id.setAlbaranId(a.getId());
        fa.setId(id);
        fa.setFactura(f);
        fa.setAlbaran(a);

        facturaAlbaranService.save(fa);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        FacturaAlbaran selected = null;
        if (tableFA != null) selected = tableFA.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        facturaAlbaranService.deleteById(selected.getId());
        loadAll();
    }
}

