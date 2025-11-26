package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class ArticuloController implements MainControllerAware {

    private static final Logger log = LoggerFactory.getLogger(ArticuloController.class);

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
        configureTableColumns();
        bindTableData();
        configureSelectionListener();
        loadAll();
    }

    private void configureTableColumns() {
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodigo()));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescripcion()));
        if (colFamilia != null) colFamilia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFamilia()));
    }

    private void bindTableData() {
        if (tableArticulos != null) tableArticulos.setItems(articulosObservable);
    }

    private void configureSelectionListener() {
        if (tableArticulos == null) return;
        tableArticulos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateDetails(newSel);
            } else {
                clearDetails();
            }
        });
    }

    private void populateDetails(Articulo a) {
        setText(txtCodigo, a.getCodigo());
        setText(txtDescripcion, a.getDescripcion());
        setText(txtFamilia, a.getFamilia());
        setText(txtUnidad, a.getUnidad());
        setText(txtIva, a.getIva() != null ? a.getIva().toString() : "");
        setText(txtPvp, a.getPvp() != null ? a.getPvp().toString() : "");
        setText(txtCoste, a.getCoste() != null ? a.getCoste().toString() : "");
    }

    private void clearDetails() {
        setText(txtCodigo, "");
        setText(txtDescripcion, "");
        setText(txtFamilia, "");
        setText(txtUnidad, "");
        setText(txtIva, "");
        setText(txtPvp, "");
        setText(txtCoste, "");
    }

    private String getText(TextInputControl control) {
        return control == null ? "" : Optional.ofNullable(control.getText()).orElse("");
    }

    private void setText(TextInputControl control, String value) {
        if (control != null) control.setText(value == null ? "" : value);
    }

    private void loadAll() {
        articulosObservable.clear();
        List<Articulo> todos = articuloService.findAll();
        articulosObservable.addAll(todos);
    }

    @FXML
    public void handleSearch() {
        String q = getText(txtSearch).trim();
        if (q.isEmpty()) {
            loadAll();
            return;
        }
        articulosObservable.clear();
        articulosObservable.addAll(articuloService.searchByDescripcion(q));
    }

    @FXML
    public void handleNuevo() {
        clearDetails();
        if (tableArticulos != null) tableArticulos.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Articulo selected = getSelectedOrNew();
        fillFromUi(selected);
        if (!isCodigoUniqueOrSame(selected)) {
            showAlert("El código ya existe");
            return;
        }
        articuloService.save(selected);
        loadAll();
    }

    private Articulo getSelectedOrNew() {
        if (tableArticulos == null) return new Articulo();
        Articulo sel = tableArticulos.getSelectionModel().getSelectedItem();
        return sel == null ? new Articulo() : sel;
    }

    private void fillFromUi(Articulo a) {
        a.setCodigo(getText(txtCodigo));
        a.setDescripcion(getText(txtDescripcion));
        a.setFamilia(getText(txtFamilia));
        a.setUnidad(getText(txtUnidad));
        try { a.setIva(new java.math.BigDecimal(getText(txtIva))); } catch (Exception e) { showAlert("IVA no válido"); }
        try { a.setPvp(new java.math.BigDecimal(getText(txtPvp))); } catch (Exception e) { showAlert("PVP no válido"); }
        try { a.setCoste(new java.math.BigDecimal(getText(txtCoste))); } catch (Exception e) { showAlert("Coste no válido"); }
    }

    private boolean isCodigoUniqueOrSame(Articulo a) {
        String codigo = a.getCodigo();
        if (codigo == null || codigo.isBlank()) return true;
        Optional<Articulo> existente = articuloService.findByCodigo(codigo);
        return existente.isEmpty() || (a.getId() != null && existente.get().getId().equals(a.getId()));
    }

    @FXML
    public void handleEliminar() {
        Articulo selected = getSelectedOrNull();
        if (selected == null || selected.getId() == null) return;
        if (selected.getAlbaranVentaLineas() != null && !selected.getAlbaranVentaLineas().isEmpty()) {
            showAlert("No se puede eliminar: existen líneas de albarán asociadas");
            return;
        }
        articuloService.deleteById(selected.getId());
        loadAll();
    }

    private Articulo getSelectedOrNull() {
        if (tableArticulos == null) return null;
        return tableArticulos.getSelectionModel().getSelectedItem();
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

    // Helper para mostrar alertas de error de forma centralizada.
    private void showAlert(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
