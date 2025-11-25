package alicanteweb.erp.controller;

import alicanteweb.erp.entities.DireccionesenvioNew;
import alicanteweb.erp.service.DireccionesenvioNewService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.util.List;

@SuppressWarnings("unused")
@Controller
public class DireccionesenvioNewController implements MainControllerAware {
    private final DireccionesenvioNewService service;

    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }

    @FXML
    private TableView<DireccionesenvioNew> tableDirecciones;

    @FXML
    private TableColumn<DireccionesenvioNew, String> colId;

    @FXML
    private TableColumn<DireccionesenvioNew, String> colNombre;

    @FXML
    private TableColumn<DireccionesenvioNew, String> colPoblacion;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtCodigoDireccion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtDireccion2;

    @FXML
    private TextField txtPoblacion;

    @FXML
    private TextField txtProvincia;

    @FXML
    private TextField txtCp;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextArea txtNotas;

    private final ObservableList<DireccionesenvioNew> direccionesObservable = FXCollections.observableArrayList();

    public DireccionesenvioNewController(DireccionesenvioNewService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        if (tableDirecciones != null) tableDirecciones.setItems(direccionesObservable);

        if (colId != null) colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId() != null ? cell.getValue().getId().toString() : ""));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre() != null ? cell.getValue().getNombre() : ""));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPoblacion() != null ? cell.getValue().getPoblacion() : ""));

        loadAll();

        if (tableDirecciones != null) {
            tableDirecciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtCodigoDireccion != null) txtCodigoDireccion.setText(newSel.getCodigoDireccion() != null ? newSel.getCodigoDireccion().toString() : "");
                    if (txtNombre != null) txtNombre.setText(newSel.getNombre() != null ? newSel.getNombre() : "");
                    if (txtDireccion != null) txtDireccion.setText(newSel.getDireccion() != null ? newSel.getDireccion() : "");
                    if (txtDireccion2 != null) txtDireccion2.setText(newSel.getDireccion2() != null ? newSel.getDireccion2() : "");
                    if (txtPoblacion != null) txtPoblacion.setText(newSel.getPoblacion() != null ? newSel.getPoblacion() : "");
                    if (txtProvincia != null) txtProvincia.setText(newSel.getProvincia() != null ? newSel.getProvincia() : "");
                    if (txtCp != null) txtCp.setText(newSel.getCp() != null ? newSel.getCp() : "");
                    if (txtTelefono != null) txtTelefono.setText(newSel.getTelefono() != null ? newSel.getTelefono() : "");
                    if (txtNotas != null) txtNotas.setText(newSel.getNotas() != null ? newSel.getNotas() : "");
                } else {
                    if (txtCodigoDireccion != null) txtCodigoDireccion.setText("");
                    if (txtNombre != null) txtNombre.setText("");
                    if (txtDireccion != null) txtDireccion.setText("");
                    if (txtDireccion2 != null) txtDireccion2.setText("");
                    if (txtPoblacion != null) txtPoblacion.setText("");
                    if (txtProvincia != null) txtProvincia.setText("");
                    if (txtCp != null) txtCp.setText("");
                    if (txtTelefono != null) txtTelefono.setText("");
                    if (txtNotas != null) txtNotas.setText("");
                }
            });
        }
    }

    private void loadAll() {
        direccionesObservable.clear();
        List<DireccionesenvioNew> all = service.findAll();
        direccionesObservable.addAll(all);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        direccionesObservable.clear();
        direccionesObservable.addAll(service.searchByPoblacion(q));
    }

    @FXML
    public void handleNuevo() {
        if (txtCodigoDireccion != null) txtCodigoDireccion.setText("");
        if (txtNombre != null) txtNombre.setText("");
        if (txtDireccion != null) txtDireccion.setText("");
        if (txtDireccion2 != null) txtDireccion2.setText("");
        if (txtPoblacion != null) txtPoblacion.setText("");
        if (txtProvincia != null) txtProvincia.setText("");
        if (txtCp != null) txtCp.setText("");
        if (txtTelefono != null) txtTelefono.setText("");
        if (txtNotas != null) txtNotas.setText("");
        if (tableDirecciones != null) tableDirecciones.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        DireccionesenvioNew selected = null;
        if (tableDirecciones != null) selected = tableDirecciones.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new DireccionesenvioNew();

        selected.setCodigoDireccion(txtCodigoDireccion != null && !txtCodigoDireccion.getText().isBlank() ? Integer.valueOf(txtCodigoDireccion.getText()) : null);
        selected.setNombre(txtNombre != null ? txtNombre.getText() : null);
        selected.setDireccion(txtDireccion != null ? txtDireccion.getText() : null);
        selected.setDireccion2(txtDireccion2 != null ? txtDireccion2.getText() : null);
        selected.setPoblacion(txtPoblacion != null ? txtPoblacion.getText() : null);
        selected.setProvincia(txtProvincia != null ? txtProvincia.getText() : null);
        selected.setCp(txtCp != null ? txtCp.getText() : null);
        selected.setTelefono(txtTelefono != null ? txtTelefono.getText() : null);
        selected.setNotas(txtNotas != null ? txtNotas.getText() : null);

        service.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        DireccionesenvioNew selected = null;
        if (tableDirecciones != null) selected = tableDirecciones.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        service.deleteById(selected.getId());
        loadAll();
    }
}