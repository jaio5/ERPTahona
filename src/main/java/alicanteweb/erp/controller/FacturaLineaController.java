package alicanteweb.erp.controller;

import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaLineaService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.FacturaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class FacturaLineaController implements MainControllerAware {

    private final FacturaLineaService service;
    private final ArticuloService articuloService;
    private final FacturaService facturaService;

    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @FXML
    private TableView<FacturaLinea> tableLineas;

    @FXML
    private TableColumn<FacturaLinea, String> colId;

    @FXML
    private TableColumn<FacturaLinea, String> colArticulo;

    @FXML
    private TableColumn<FacturaLinea, String> colCantidad;

    @FXML
    private TableColumn<FacturaLinea, String> colPrecio;

    @FXML
    private TableColumn<FacturaLinea, String> colIva;

    @FXML
    private ChoiceBox<Articulo> choiceArticulo;

    @FXML
    private ChoiceBox<Factura> choiceFactura;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtIva;

    @FXML
    private TextField txtSearch;

    private final ObservableList<FacturaLinea> lineasObservable = FXCollections.observableArrayList();
    private final ObservableList<Articulo> articulosObservable = FXCollections.observableArrayList();
    private final ObservableList<Factura> facturasObservable = FXCollections.observableArrayList();

    public FacturaLineaController(FacturaLineaService service, ArticuloService articuloService, FacturaService facturaService) {
        this.service = service;
        this.articuloService = articuloService;
        this.facturaService = facturaService;
    }

    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }

    @FXML
    public void initialize() {
        if (tableLineas != null) tableLineas.setItems(lineasObservable);
        if (choiceArticulo != null) choiceArticulo.setItems(articulosObservable);
        if (choiceFactura != null) choiceFactura.setItems(facturasObservable);

        if (colId != null) colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId() != null ? cell.getValue().getId().toString() : ""));
        if (colArticulo != null) colArticulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticulo() != null ? cell.getValue().getArticulo().getDescripcion() : ""));
        if (colCantidad != null) colCantidad.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCantidad() != null ? cell.getValue().getCantidad().toString() : ""));
        if (colPrecio != null) colPrecio.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrecio() != null ? cell.getValue().getPrecio().toString() : ""));
        if (colIva != null) colIva.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIva() != null ? cell.getValue().getIva().toString() : ""));

        loadAll();

        if (tableLineas != null) {
            tableLineas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (choiceArticulo != null) choiceArticulo.setValue(newSel.getArticulo());
                    if (choiceFactura != null) choiceFactura.setValue(newSel.getFactura());
                    if (txtCantidad != null) txtCantidad.setText(newSel.getCantidad() != null ? newSel.getCantidad().toString() : "");
                    if (txtPrecio != null) txtPrecio.setText(newSel.getPrecio() != null ? newSel.getPrecio().toString() : "");
                    if (txtIva != null) txtIva.setText(newSel.getIva() != null ? newSel.getIva().toString() : "");
                } else {
                    if (choiceArticulo != null) choiceArticulo.setValue(null);
                    if (choiceFactura != null) choiceFactura.setValue(null);
                    if (txtCantidad != null) txtCantidad.setText("");
                    if (txtPrecio != null) txtPrecio.setText("");
                    if (txtIva != null) txtIva.setText("");
                }
            });
        }
    }

    private void loadAll() {
        lineasObservable.clear();
        articulosObservable.clear();
        facturasObservable.clear();

        List<FacturaLinea> all = service.findAll();
        List<Articulo> arts = articuloService.findAll();
        List<Factura> facs = facturaService.findAll();

        lineasObservable.addAll(all);
        articulosObservable.addAll(arts);
        facturasObservable.addAll(facs);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }

        lineasObservable.clear();

        // intentar buscar por factura número
        Optional<Factura> fOpt = facturaService.findByNumero(q);
        if (fOpt.isPresent()) {
            List<FacturaLinea> porFactura = service.findByFacturaId(fOpt.get().getId());
            lineasObservable.addAll(porFactura);
        }

        // buscar por descripción de artículo
        List<Articulo> filtered = articuloService.searchByDescripcion(q);
        for (Articulo a : filtered) {
            lineasObservable.addAll(service.findByArticuloId(a.getId()));
        }
    }

    @FXML
    public void handleNuevo() {
        if (choiceArticulo != null) choiceArticulo.setValue(null);
        if (choiceFactura != null) choiceFactura.setValue(null);
        if (txtCantidad != null) txtCantidad.setText("");
        if (txtPrecio != null) txtPrecio.setText("");
        if (txtIva != null) txtIva.setText("");
        if (tableLineas != null) tableLineas.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        FacturaLinea selected = null;
        if (tableLineas != null) selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new FacturaLinea();

        Articulo art = choiceArticulo != null ? choiceArticulo.getValue() : null;
        Factura fac = choiceFactura != null ? choiceFactura.getValue() : null;

        selected.setArticulo(art);
        selected.setFactura(fac);

        try {
            selected.setCantidad(txtCantidad != null && !txtCantidad.getText().isBlank() ? new BigDecimal(txtCantidad.getText()) : null);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Cantidad no válida").showAndWait();
            return;
        }

        try {
            selected.setPrecio(txtPrecio != null && !txtPrecio.getText().isBlank() ? new BigDecimal(txtPrecio.getText()) : null);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Precio no válido").showAndWait();
            return;
        }

        try {
            selected.setIva(txtIva != null && !txtIva.getText().isBlank() ? new BigDecimal(txtIva.getText()) : null);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "IVA no válido").showAndWait();
            return;
        }

        if (art == null || fac == null) {
            new Alert(Alert.AlertType.ERROR, "Seleccione artículo y factura").showAndWait();
            return;
        }

        service.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        FacturaLinea selected = null;
        if (tableLineas != null) selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        service.deleteById(selected.getId());
        loadAll();
    }
}
