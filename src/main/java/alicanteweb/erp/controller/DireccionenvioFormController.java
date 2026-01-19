package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.DireccionenvioNew;
import alicanteweb.erp.service.DireccionenvioNewService;
import alicanteweb.erp.ui.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class DireccionenvioFormController {
    private static final Logger log = LoggerFactory.getLogger(DireccionenvioFormController.class);

    @FXML private Label lblTitulo;
    @FXML private TextField txtCodigoDireccion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtPoblacion;
    @FXML private TextField txtProvincia;
    @FXML private TextField txtCP;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtNotas;

    private final DireccionenvioNewService service;
    private DireccionenvioNew direccion;
    private Long clienteId = null;

    public DireccionenvioFormController(DireccionenvioNewService service) { this.service = service; }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public void setDireccion(DireccionenvioNew d) { this.direccion = d; cargarDatos(); }

    private void cargarDatos() {
        if (direccion != null) {
            if (txtCodigoDireccion != null && direccion.getCodigoDireccion() != null) txtCodigoDireccion.setText(String.valueOf(direccion.getCodigoDireccion()));
            if (txtNombre != null) txtNombre.setText(direccion.getNombre());
            if (txtDireccion != null) txtDireccion.setText(direccion.getDireccion());
            if (txtPoblacion != null) txtPoblacion.setText(direccion.getPoblacion());
            if (txtProvincia != null) txtProvincia.setText(direccion.getProvincia());
            if (txtCP != null) txtCP.setText(direccion.getCp());
            if (txtTelefono != null) txtTelefono.setText(direccion.getTelefono());
            if (txtNotas != null) txtNotas.setText(direccion.getNotas());
            if (lblTitulo != null) lblTitulo.setText(direccion.getId() != null ? "Editar Dirección" : "Nueva Dirección");
        } else {
            if (lblTitulo != null) lblTitulo.setText("Nueva Dirección");
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validar()) return;
            if (direccion == null) direccion = new DireccionenvioNew();
            // asociar cliente si se abrió desde cliente
            if (this.clienteId != null) {
                Cliente c = new Cliente();
                c.setId(this.clienteId);
                direccion.setCliente(c);
            }
            if (txtCodigoDireccion != null && !txtCodigoDireccion.getText().isBlank()) {
                try { direccion.setCodigoDireccion(Integer.valueOf(txtCodigoDireccion.getText().trim())); } catch (NumberFormatException ignored) {}
            }
            direccion.setNombre(txtNombre != null ? txtNombre.getText().trim() : null);
            direccion.setDireccion(txtDireccion != null ? txtDireccion.getText().trim() : null);
            direccion.setPoblacion(txtPoblacion != null ? txtPoblacion.getText().trim() : null);
            direccion.setProvincia(txtProvincia != null ? txtProvincia.getText().trim() : null);
            direccion.setCp(txtCP != null ? txtCP.getText().trim() : null);
            direccion.setTelefono(txtTelefono != null ? txtTelefono.getText().trim() : null);
            direccion.setNotas(txtNotas != null ? txtNotas.getText().trim() : null);

            service.save(direccion);
            mostrarExito("Guardado");
            cerrar();
        } catch (Exception e) { log.error("Error guardando dirección", e); Dialogs.showError(e.getMessage()); }
    }

    @FXML
    public void onCancelar() { cerrar(); }

    private boolean validar() {
        if (txtNombre == null || txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) { mostrarError("El nombre es obligatorio"); return false; }
        return true;
    }

    private void cerrar() { if (txtNombre != null && txtNombre.getScene() != null) { Stage s = (Stage) txtNombre.getScene().getWindow(); s.close(); } }

    private void mostrarError(String msg) { Dialogs.showError(msg); }
    private void mostrarExito(String msg) { Dialogs.showInfo(msg); }
}
