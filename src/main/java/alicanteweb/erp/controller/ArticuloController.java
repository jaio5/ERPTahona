package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class ArticuloController implements MainControllerAware {

    private final ArticuloService articuloService;
    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @FXML
    private TableView<Articulo> tableArticulos;

    @FXML
    private TableColumn<Articulo, String> colCodigo;

    @FXML
    private TableColumn<Articulo, String> colDescripcion;

    @FXML
    private TableColumn<Articulo, String> colFamilia;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TextField txtFamilia;

    @FXML
    private TextField txtUnidad;

    @FXML
    private TextField txtIva;

    @FXML
    private TextField txtPvp;

    @FXML
    private TextField txtCoste;

    private final ObservableList<Articulo> articulosObservable = FXCollections.observableArrayList();

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        if (tableArticulos != null) tableArticulos.setItems(articulosObservable);

        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodigo()));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescripcion()));
        if (colFamilia != null) colFamilia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFamilia()));

        loadAll();

        if (tableArticulos != null) {
            tableArticulos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtCodigo != null) txtCodigo.setText(newSel.getCodigo() != null ? newSel.getCodigo() : "");
                    if (txtDescripcion != null) txtDescripcion.setText(newSel.getDescripcion() != null ? newSel.getDescripcion() : "");
                    if (txtFamilia != null) txtFamilia.setText(newSel.getFamilia() != null ? newSel.getFamilia() : "");
                    if (txtUnidad != null) txtUnidad.setText(newSel.getUnidad() != null ? newSel.getUnidad() : "");
                    if (txtIva != null) txtIva.setText(newSel.getIva() != null ? newSel.getIva().toString() : "");
                    if (txtPvp != null) txtPvp.setText(newSel.getPvp() != null ? newSel.getPvp().toString() : "");
                    if (txtCoste != null) txtCoste.setText(newSel.getCoste() != null ? newSel.getCoste().toString() : "");
                } else {
                    if (txtCodigo != null) txtCodigo.setText("");
                    if (txtDescripcion != null) txtDescripcion.setText("");
                    if (txtFamilia != null) txtFamilia.setText("");
                    if (txtUnidad != null) txtUnidad.setText("");
                    if (txtIva != null) txtIva.setText("");
                    if (txtPvp != null) txtPvp.setText("");
                    if (txtCoste != null) txtCoste.setText("");
                }
            });
        }
    }

    private void loadAll() {
        articulosObservable.clear();
        List<Articulo> todos = articuloService.findAll();
        articulosObservable.addAll(todos);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }

        articulosObservable.clear();
        articulosObservable.addAll(articuloService.searchByDescripcion(q));
    }

    @FXML
    public void handleNuevo() {
        if (txtCodigo != null) txtCodigo.setText("");
        if (txtDescripcion != null) txtDescripcion.setText("");
        if (txtFamilia != null) txtFamilia.setText("");
        if (txtUnidad != null) txtUnidad.setText("");
        if (txtIva != null) txtIva.setText("");
        if (txtPvp != null) txtPvp.setText("");
        if (txtCoste != null) txtCoste.setText("");
        if (tableArticulos != null) tableArticulos.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Articulo selected = null;
        if (tableArticulos != null) selected = tableArticulos.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Articulo();

        selected.setCodigo(txtCodigo != null ? txtCodigo.getText() : null);
        selected.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);
        selected.setFamilia(txtFamilia != null ? txtFamilia.getText() : null);
        selected.setUnidad(txtUnidad != null ? txtUnidad.getText() : null);
        // parse BigDecimal fields defensively
        try {
            if (txtIva != null && !txtIva.getText().isBlank()) selected.setIva(new java.math.BigDecimal(txtIva.getText()));
        } catch (Exception e) { /* ignore, leave null */ }
        try {
            if (txtPvp != null && !txtPvp.getText().isBlank()) selected.setPvp(new java.math.BigDecimal(txtPvp.getText()));
        } catch (Exception e) { /* ignore, leave null */ }
        try {
            if (txtCoste != null && !txtCoste.getText().isBlank()) selected.setCoste(new java.math.BigDecimal(txtCoste.getText()));
        } catch (Exception e) { /* ignore, leave null */ }

        // unicidad de codigo
        String codigo = selected.getCodigo();
        if (codigo != null && !codigo.isBlank()) {
            Optional<Articulo> existente = articuloService.findByCodigo(codigo);
            if (existente.isPresent()) {
                Articulo existing = existente.get();
                if (selected.getId() == null || !existing.getId().equals(selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "El código ya existe").showAndWait();
                    return;
                }
            }
        }

        articuloService.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        Articulo selected = null;
        if (tableArticulos != null) selected = tableArticulos.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        if (selected.getAlbaranVentaLineas() != null && !selected.getAlbaranVentaLineas().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No se puede eliminar: existen líneas de albarán asociadas").showAndWait();
            return;
        }
        articuloService.deleteById(selected.getId());
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

    @FXML
    public void handleFilterFamilia() {
        // Lógica de filtrado por familia (puedes implementar aquí si lo necesitas)
    }
}
