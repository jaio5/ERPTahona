package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Controlador para el formulario de creación/edición de clientes
 */
@Controller
public class ClienteFormController {
    private static final Logger log = LoggerFactory.getLogger(ClienteFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCIF;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtPoblacion;
    @FXML private TextField txtProvincia;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final ClienteService clienteService;
    private Cliente clienteActual;

    public ClienteFormController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        log.info("ClienteFormController inicializado");
    }


    public void setCliente(Cliente cliente) {
        this.clienteActual = cliente;
        if (cliente != null) {
            txtCodigo.setText(cliente.getCodigo());
            txtNombre.setText(cliente.getNombre());
            txtCIF.setText(cliente.getCif());
            txtDireccion.setText(cliente.getDireccion());
            txtCodigoPostal.setText(cliente.getCodigoPostal());
            txtPoblacion.setText(cliente.getPoblacion());
            txtProvincia.setText(cliente.getProvincia());
            txtNotas.setText(cliente.getNotas());
        }
    }

    @FXML
    public void onGuardar() {
        if (validar()) {
            try {
                if (clienteActual == null) {
                    clienteActual = new Cliente();
                }

                clienteActual.setCodigo(txtCodigo.getText());
                clienteActual.setNombre(txtNombre.getText());
                clienteActual.setCif(txtCIF.getText());
                clienteActual.setDireccion(txtDireccion.getText());
                clienteActual.setCodigoPostal(txtCodigoPostal.getText());
                clienteActual.setPoblacion(txtPoblacion.getText());
                clienteActual.setProvincia(txtProvincia.getText());
                clienteActual.setNotas(txtNotas.getText());
                clienteActual.setActivo(true);

                clienteService.save(clienteActual);
                log.info("Cliente guardado: {}", clienteActual.getId());

                mostrarExito("Cliente guardado correctamente");


                // Cerrar el diálogo
                btnCancelar.getScene().getWindow().hide();
            } catch (Exception e) {
                log.error("Error guardando cliente", e);
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

