package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Controlador para el formulario de creación/edición de proveedores
 */
@Controller
public class ProveedorFormController {
    private static final Logger log = LoggerFactory.getLogger(ProveedorFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCIF;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtPoblacion;
    @FXML private TextField txtProvincia;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final ProveedorService proveedorService;
    private Proveedor proveedorActual;

    public ProveedorFormController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        log.info("ProveedorFormController inicializado");
    }


    public void setProveedor(Proveedor proveedor) {
        this.proveedorActual = proveedor;
        if (proveedor != null) {
            txtCodigo.setText(proveedor.getCodigo());
            txtNombre.setText(proveedor.getNombre());
            txtCIF.setText(proveedor.getCif());
            txtDireccion.setText(proveedor.getDireccion());
            txtCodigoPostal.setText(proveedor.getCodigoPostal());
            txtPoblacion.setText(proveedor.getPoblacion());
            txtProvincia.setText(proveedor.getProvincia());
            if (proveedor.getEmail() != null) {
                txtEmail.setText(proveedor.getEmail());
            }
            if (proveedor.getTelefono() != null) {
                txtTelefono.setText(proveedor.getTelefono());
            }
            if (proveedor.getNotas() != null) {
                txtNotas.setText(proveedor.getNotas());
            }
        }
    }

    @FXML
    public void onGuardar() {
        if (validar()) {
            try {
                if (proveedorActual == null) {
                    proveedorActual = new Proveedor();
                }

                proveedorActual.setCodigo(txtCodigo.getText());
                proveedorActual.setNombre(txtNombre.getText());
                proveedorActual.setCif(txtCIF.getText());
                proveedorActual.setDireccion(txtDireccion.getText());
                proveedorActual.setCodigoPostal(txtCodigoPostal.getText());
                proveedorActual.setPoblacion(txtPoblacion.getText());
                proveedorActual.setProvincia(txtProvincia.getText());
                proveedorActual.setEmail(txtEmail.getText());
                proveedorActual.setTelefono(txtTelefono.getText());
                proveedorActual.setNotas(txtNotas.getText());
                proveedorActual.setActivo(true);

                proveedorService.save(proveedorActual);
                log.info("Proveedor guardado: {}", proveedorActual.getId());

                mostrarExito("Proveedor guardado correctamente");

                btnCancelar.getScene().getWindow().hide();
            } catch (Exception e) {
                log.error("Error guardando proveedor", e);
                mostrarError("Error al guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onCancelar() {
        btnCancelar.getScene().getWindow().hide();
    }

    private boolean validar() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            mostrarAlerta("Código y nombre son obligatorios");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

