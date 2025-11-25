package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AlbaranesVentaFactura;
import alicanteweb.erp.entities.AlbaranesVentaFacturaId;
import alicanteweb.erp.service.AlbaranesVentaFacturaService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class AlbaranesVentaFacturaController {
    private final AlbaranesVentaFacturaService service;
    @FXML private TableView<AlbaranesVentaFactura> tableView;
    @FXML private TextField textFieldId;
    @FXML private TextField textFieldData;

    public AlbaranesVentaFacturaController(AlbaranesVentaFacturaService service) { this.service = service; }

    @FXML
    public void initialize() {
        List<AlbaranesVentaFactura> allData = service.findAll();
        tableView.getItems().setAll(allData);
    }

    @FXML
    public void handleSave() {
        AlbaranesVentaFactura avf = new AlbaranesVentaFactura();
        // Assume there's a method to set values from text fields to the entity
        // setValuesFromFields(avf);
        service.save(avf);
        tableView.getItems().add(avf);
    }

    @FXML
    public void handleDelete() {
        AlbaranesVentaFacturaId id = new AlbaranesVentaFacturaId();
        // Assume there's a method to get the selected item and its ID
        // id = getSelectedItemId();
        service.deleteById(id);
        tableView.getItems().removeIf(avf -> avf.getId().equals(id));
    }
}
