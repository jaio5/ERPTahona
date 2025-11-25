package alicanteweb.erp.controller;

import alicanteweb.erp.entities.PedidoLinea;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.service.PedidoLineaService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.PedidoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("unused")
@Controller
public class PedidoLineaController implements MainControllerAware {

    private final PedidoLineaService service;
    private final ArticuloService articuloService;
    private final PedidoService pedidoService;

    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @FXML
    private TableView<PedidoLinea> tableLineas;

    @FXML
    private TableColumn<PedidoLinea, String> colId;

    @FXML
    private TableColumn<PedidoLinea, String> colArticulo;

    @FXML
    private TableColumn<PedidoLinea, String> colCantidad;

    @FXML
    private TableColumn<PedidoLinea, String> colPrecio;

    @FXML
    private ChoiceBox<Articulo> choiceArticulo;

    @FXML
    private ChoiceBox<Pedido> choicePedido;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtSearch;

    private final ObservableList<PedidoLinea> lineasObservable = FXCollections.observableArrayList();
    private final ObservableList<Articulo> articulosObservable = FXCollections.observableArrayList();
    private final ObservableList<Pedido> pedidosObservable = FXCollections.observableArrayList();

    public PedidoLineaController(PedidoLineaService service, ArticuloService articuloService, PedidoService pedidoService) {
        this.service = service;
        this.articuloService = articuloService;
        this.pedidoService = pedidoService;
    }

    @FXML
    public void initialize() {
        if (tableLineas != null) tableLineas.setItems(lineasObservable);
        if (choiceArticulo != null) choiceArticulo.setItems(articulosObservable);
        if (choicePedido != null) choicePedido.setItems(pedidosObservable);

        if (colId != null) colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId() != null ? cell.getValue().getId().toString() : ""));
        if (colArticulo != null) colArticulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticulo() != null ? cell.getValue().getArticulo().getDescripcion() : ""));
        if (colCantidad != null) colCantidad.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCantidad() != null ? cell.getValue().getCantidad().toString() : ""));
        if (colPrecio != null) colPrecio.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrecio() != null ? cell.getValue().getPrecio().toString() : ""));

        loadAll();

        if (tableLineas != null) {
            tableLineas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (choiceArticulo != null) choiceArticulo.setValue(newSel.getArticulo());
                    if (choicePedido != null) choicePedido.setValue(newSel.getPedido());
                    if (txtCantidad != null) txtCantidad.setText(newSel.getCantidad() != null ? newSel.getCantidad().toString() : "");
                    if (txtPrecio != null) txtPrecio.setText(newSel.getPrecio() != null ? newSel.getPrecio().toString() : "");
                } else {
                    if (choiceArticulo != null) choiceArticulo.setValue(null);
                    if (choicePedido != null) choicePedido.setValue(null);
                    if (txtCantidad != null) txtCantidad.setText("");
                    if (txtPrecio != null) txtPrecio.setText("");
                }
            });
        }
    }

    private void loadAll() {
        lineasObservable.clear();
        articulosObservable.clear();
        pedidosObservable.clear();

        List<PedidoLinea> all = service.findAll();
        List<Articulo> arts = articuloService.findAll();
        List<Pedido> peds = pedidoService.findAll();

        lineasObservable.addAll(all);
        articulosObservable.addAll(arts);
        pedidosObservable.addAll(peds);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        // Buscar en descripciones de artículos
        lineasObservable.clear();
        List<Articulo> filtered = articuloService.searchByDescripcion(q);
        for (Articulo a : filtered) {
            lineasObservable.addAll(service.findByArticuloId(a.getId()));
        }
    }

    @FXML
    public void handleNuevo() {
        if (choiceArticulo != null) choiceArticulo.setValue(null);
        if (choicePedido != null) choicePedido.setValue(null);
        if (txtCantidad != null) txtCantidad.setText("");
        if (txtPrecio != null) txtPrecio.setText("");
        if (tableLineas != null) tableLineas.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        PedidoLinea selected = null;
        if (tableLineas != null) selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new PedidoLinea();

        Articulo art = choiceArticulo != null ? choiceArticulo.getValue() : null;
        Pedido ped = choicePedido != null ? choicePedido.getValue() : null;

        selected.setArticulo(art);
        selected.setPedido(ped);

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

        if (art == null || ped == null) {
            new Alert(Alert.AlertType.ERROR, "Seleccione artículo y pedido").showAndWait();
            return;
        }

        service.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        PedidoLinea selected = null;
        if (tableLineas != null) selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        service.deleteById(selected.getId());
        loadAll();
    }

    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }
}
