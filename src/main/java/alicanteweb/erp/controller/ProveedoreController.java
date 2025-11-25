package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Proveedore;
import alicanteweb.erp.service.ProveedoreService;
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
public class ProveedoreController implements MainControllerAware {

    private final ProveedoreService proveedoreService;
    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @FXML
    private TableView<Proveedore> tableProveedores;

    @FXML
    private TableColumn<Proveedore, String> colCodigo;

    @FXML
    private TableColumn<Proveedore, String> colNombre;

    @FXML
    private TableColumn<Proveedore, String> colPoblacion;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtCif;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtPoblacion;

    @FXML
    private TextField txtCodigoPostal;

    @FXML
    private TextField txtProvincia;

    @FXML
    private TextArea txtNotas;

    private final ObservableList<Proveedore> proveedoresObservable = FXCollections.observableArrayList();

    public ProveedoreController(ProveedoreService proveedoreService) {
        this.proveedoreService = proveedoreService;
    }

    @FXML
    public void initialize() {
        if (tableProveedores != null) tableProveedores.setItems(proveedoresObservable);

        if (colCodigo != null) colCodigo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCodigo()));
        if (colNombre != null) colNombre.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNombre()));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getPoblacion()));

        if (tableProveedores != null) {
            tableProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtCodigo != null) txtCodigo.setText(newSel.getCodigo() != null ? newSel.getCodigo() : "");
                    if (txtNombre != null) txtNombre.setText(newSel.getNombre() != null ? newSel.getNombre() : "");
                    if (txtCif != null) txtCif.setText(newSel.getCif() != null ? newSel.getCif() : "");
                    if (txtDireccion != null) txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
                    if (txtPoblacion != null) txtPoblacion.setText(newSel.getPoblacion() != null ? newSel.getPoblacion() : "");
                    if (txtCodigoPostal != null) txtCodigoPostal.setText(newSel.getCodigoPostal() != null ? newSel.getCodigoPostal() : "");
                    if (txtProvincia != null) txtProvincia.setText(newSel.getProvincia() != null ? newSel.getProvincia() : "");
                    if (txtNotas != null) txtNotas.setText(newSel.getNotas() != null ? newSel.getNotas() : "");
                } else {
                    if (txtCodigo != null) txtCodigo.setText("");
                    if (txtNombre != null) txtNombre.setText("");
                    if (txtCif != null) txtCif.setText("");
                    if (txtDireccion != null) txtDireccion.setText("");
                    if (txtPoblacion != null) txtPoblacion.setText("");
                    if (txtCodigoPostal != null) txtCodigoPostal.setText("");
                    if (txtProvincia != null) txtProvincia.setText("");
                    if (txtNotas != null) txtNotas.setText("");
                }
            });
        }

        loadAll();
    }

    private void loadAll() {
        proveedoresObservable.clear();
        List<Proveedore> all = proveedoreService.findAll();
        proveedoresObservable.addAll(all);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        proveedoresObservable.clear();
        proveedoresObservable.addAll(proveedoreService.searchByNombre(q));
    }

    @FXML
    public void handleNuevo() {
        if (txtCodigo != null) txtCodigo.setText("");
        if (txtNombre != null) txtNombre.setText("");
        if (txtCif != null) txtCif.setText("");
        if (txtDireccion != null) txtDireccion.setText("");
        if (txtPoblacion != null) txtPoblacion.setText("");
        if (txtCodigoPostal != null) txtCodigoPostal.setText("");
        if (txtProvincia != null) txtProvincia.setText("");
        if (txtNotas != null) txtNotas.setText("");
        if (tableProveedores != null) tableProveedores.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Proveedore selected = null;
        if (tableProveedores != null) selected = tableProveedores.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Proveedore();

        selected.setCodigo(txtCodigo != null ? txtCodigo.getText() : null);
        selected.setNombre(txtNombre != null ? txtNombre.getText() : null);
        selected.setCif(txtCif != null ? txtCif.getText() : null);
        selected.setDireccion(txtDireccion != null ? txtDireccion.getText() : null);
        selected.setPoblacion(txtPoblacion != null ? txtPoblacion.getText() : null);
        selected.setCodigoPostal(txtCodigoPostal != null ? txtCodigoPostal.getText() : null);
        selected.setProvincia(txtProvincia != null ? txtProvincia.getText() : null);
        selected.setNotas(txtNotas != null ? txtNotas.getText() : null);

        // unicidad de codigo
        String codigo = selected.getCodigo();
        if (codigo != null && !codigo.isBlank()) {
            Optional<Proveedore> existente = proveedoreService.findByCodigo(codigo);
            if (existente.isPresent()) {
                Proveedore existing = existente.get();
                if (selected.getId() == null || !existing.getId().equals(selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "El código ya existe").showAndWait();
                    return;
                }
            }
        }

        proveedoreService.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        Proveedore selected = null;
        if (tableProveedores != null) selected = tableProveedores.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        proveedoreService.deleteById(selected.getId());
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
