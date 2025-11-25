package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
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
public class ClienteController {

    private final ClienteService clienteService;

    @FXML
    private TableView<Cliente> tableClientes;

    @FXML
    private TableColumn<Cliente, String> colCodigo;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colPoblacion;

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

    private final ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList();

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        if (tableClientes != null) tableClientes.setItems(clientesObservable);

        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodigo()));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPoblacion()));

        loadAll();

        if (tableClientes != null) {
            tableClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
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
    }

    private void loadAll() {
        clientesObservable.clear();
        List<Cliente> all = clienteService.findAll();
        clientesObservable.addAll(all);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        clientesObservable.clear();
        clientesObservable.addAll(clienteService.searchByNombre(q));
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
        if (tableClientes != null) tableClientes.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Cliente selected = null;
        if (tableClientes != null) selected = tableClientes.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Cliente();

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
            Optional<Cliente> existente = clienteService.findByCodigo(codigo);
            if (existente.isPresent()) {
                Cliente existing = existente.get();
                if (selected.getId() == null || !existing.getId().equals(selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "El código ya existe").showAndWait();
                    return;
                }
            }
        }

        clienteService.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        Cliente selected = null;
        if (tableClientes != null) selected = tableClientes.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        if (selected.getAlbaranesVentas() != null && !selected.getAlbaranesVentas().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No se puede eliminar: existen albaranes asociadas").showAndWait();
            return;
        }
        clienteService.deleteById(selected.getId());
        loadAll();
    }
}

