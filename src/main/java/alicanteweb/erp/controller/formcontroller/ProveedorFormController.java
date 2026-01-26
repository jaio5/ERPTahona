package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.DialogUtils;

import java.util.Arrays;

/**
 * Controlador para el formulario de creación/edición de proveedores
 */
@Controller
public class ProveedorFormController {
    private static final Logger log = LoggerFactory.getLogger(ProveedorFormController.class);

    // Header
    @FXML private Label lblTitulo;

    // Datos principales
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCIF;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private Button btnGuardarProveedor;
    @FXML private Label lblErrNombre;
    @FXML private Label lblErrCIF;
    @FXML private Label lblErrEmail;

    // Dirección
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtPoblacion;
    @FXML private ComboBox<String> cbProvincia;

    // Datos comerciales
    @FXML private ComboBox<String> cbFormaPago;
    @FXML private TextField txtDiasCredito;
    @FXML private TextField txtDescuento;
    @FXML private TextArea txtNotas;
    @FXML private CheckBox chkActivo;

    private final ProveedorService proveedorService;
    private Proveedor proveedorActual;
    private boolean modoEdicion = false;

    public ProveedorFormController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        log.info("✅ ProveedorFormController inicializado");

        configurarProvincias();
        configurarFormasPago();
        configurarValidaciones();

        // Bind botón Guardar y validación en tiempo real
        try {
            if (btnGuardarProveedor != null) {
                setupInlineValidation();
                btnGuardarProveedor.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(() -> !isFormValid(),
                        txtNombre.textProperty(), txtCIF.textProperty(), txtEmail.textProperty())
                );
            }
        } catch (Exception e) {
            log.debug("No se pudo bindear btnGuardar: {}", e.getMessage());
        }
    }

    private void configurarProvincias() {
        if (cbProvincia != null) {
            cbProvincia.getItems().addAll(Arrays.asList(
                "Alava", "Albacete", "Alicante", "Almeria", "Asturias", "Avila",
                "Badajoz", "Barcelona", "Burgos", "Caceres", "Cadiz", "Cantabria",
                "Castellon", "Ciudad Real", "Cordoba", "Cuenca", "Gerona",
                "Granada", "Guadalajara", "Guipuzcoa", "Huelva", "Huesca",
                "Islas Baleares", "Jaen", "La Coruna", "La Rioja", "Las Palmas",
                "Leon", "Lerida", "Lugo", "Madrid", "Malaga", "Murcia",
                "Navarra", "Orense", "Palencia", "Pontevedra", "Salamanca",
                "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria",
                "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid",
                "Vizcaya", "Zamora", "Zaragoza"
            ));
            cbProvincia.setValue("Alicante");
        }
    }

    private void configurarFormasPago() {
        if (cbFormaPago != null) {
            cbFormaPago.getItems().addAll(
                "Efectivo",
                "Transferencia",
                "Tarjeta",
                "Pagare",
                "Recibo",
                "Contado",
                "30 dias",
                "60 dias",
                "90 dias"
            );
            cbFormaPago.setValue("30 dias");
        }
    }

    private void configurarValidaciones() {
        // Validar código postal (solo números, 5 dígitos)
        if (txtCodigoPostal != null) {
            txtCodigoPostal.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.matches("\\d{0,5}")) {
                    txtCodigoPostal.setText(oldVal);
                }
            });
        }

        // Validar teléfono (solo números y espacios)
        if (txtTelefono != null) {
            txtTelefono.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.matches("[0-9 ]{0,15}")) {
                    txtTelefono.setText(oldVal);
                }
            });
        }

        // Validar días de crédito (solo números)
        if (txtDiasCredito != null) {
            txtDiasCredito.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.matches("\\d{0,3}")) {
                    txtDiasCredito.setText(oldVal);
                }
            });
        }

        // Validar descuento (números con decimales)
        if (txtDescuento != null) {
            txtDescuento.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.matches("\\d{0,2}(\\.\\d{0,2})?")) {
                    txtDescuento.setText(oldVal);
                }
            });
        }
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedorActual = proveedor;
        this.modoEdicion = (proveedor != null && proveedor.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                lblTitulo.setText("Editar Proveedor");
                if (proveedor != null) cargarDatosProveedor(proveedor);
            } else {
                lblTitulo.setText("Nuevo Proveedor");
                limpiarFormulario();
                generarCodigoAutomatico();
                Platform.runLater(() -> { if (txtNombre != null) txtNombre.requestFocus(); });
            }
        });
    }

    private void cargarDatosProveedor(Proveedor proveedor) {
        txtCodigo.setText(proveedor.getCodigo());
        txtNombre.setText(proveedor.getNombre());
        txtCIF.setText(proveedor.getCif());
        txtTelefono.setText(proveedor.getTelefono() != null ? proveedor.getTelefono() : "");
        txtEmail.setText(proveedor.getEmail() != null ? proveedor.getEmail() : "");

        txtDireccion.setText(proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
        txtCodigoPostal.setText(proveedor.getCodigoPostal() != null ? proveedor.getCodigoPostal() : "");
        txtPoblacion.setText(proveedor.getPoblacion() != null ? proveedor.getPoblacion() : "");

        if (proveedor.getProvincia() != null && cbProvincia.getItems().contains(proveedor.getProvincia())) {
            cbProvincia.setValue(proveedor.getProvincia());
        }

        txtNotas.setText(proveedor.getNotas() != null ? proveedor.getNotas() : "");
        chkActivo.setSelected(proveedor.getActivo() != null ? proveedor.getActivo() : true);

        log.info("Datos cargados para proveedor: {}", proveedor.getId());
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCIF.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtCodigoPostal.clear();
        txtPoblacion.clear();
        cbProvincia.setValue("Alicante");
        cbFormaPago.setValue("30 dias");
        txtDiasCredito.setText("30");
        txtDescuento.setText("0.00");
        txtNotas.clear();
        chkActivo.setSelected(true);
    }

    private void generarCodigoAutomatico() {
        try {
            long count = proveedorService.findAll().size();
            long nextId = count + 1;
            String codigo = String.format("PROV%04d", nextId);
            txtCodigo.setText(codigo);
        } catch (Exception e) {
            log.warn("No se pudo generar código automático", e);
            txtCodigo.setText("PROV0001");
        }
    }

    @FXML
    public void onGuardar() {
        if (!validarFormulario()) {
            return;
        }

        try {
            if (proveedorActual == null) {
                proveedorActual = new Proveedor();
            }

            // Datos básicos
            proveedorActual.setCodigo(txtCodigo.getText().trim());
            proveedorActual.setNombre(txtNombre.getText().trim());
            proveedorActual.setCif(txtCIF.getText().trim().toUpperCase());
            proveedorActual.setTelefono(txtTelefono.getText().trim());
            proveedorActual.setEmail(txtEmail.getText().trim());

            // Dirección
            proveedorActual.setDireccion(txtDireccion.getText().trim());
            proveedorActual.setCodigoPostal(txtCodigoPostal.getText().trim());
            proveedorActual.setPoblacion(txtPoblacion.getText().trim());
            proveedorActual.setProvincia(cbProvincia.getValue());

            // Datos adicionales
            proveedorActual.setNotas(txtNotas.getText().trim());
            proveedorActual.setActivo(chkActivo.isSelected());

            // Guardar
            Proveedor guardado = proveedorService.save(proveedorActual);
            log.info("✅ Proveedor guardado: {} - {}", guardado.getId(), guardado.getNombre());

            DialogUtils.showSuccess(modoEdicion ?
                "Proveedor actualizado correctamente" :
                "Proveedor creado correctamente");

            cerrarVentana();

        } catch (IllegalArgumentException e) {
            log.warn("Validación al guardar proveedor: {}", e.getMessage());
            DialogUtils.showWarning("No se pudo guardar: " + e.getMessage());
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("codigo") && txtCodigo != null) txtCodigo.requestFocus();
            else if (msg.contains("cif") && txtCIF != null) txtCIF.requestFocus();
            else if (txtNombre != null) txtNombre.requestFocus();
        } catch (Exception e) {
            log.error("❌ Error guardando proveedor", e);
            DialogUtils.showError("Error al guardar el proveedor: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        if (formularioModificado()) {
            boolean ok = alicanteweb.erp.ui.DialogUtils.showConfirm("Hay cambios sin guardar. ¿Desea salir sin guardar?");
            if (ok) cerrarVentana();
        } else {
            cerrarVentana();
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar campos obligatorios
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("El nombre es obligatorio\n");
        }

        if (txtCIF.getText().trim().isEmpty()) {
            errores.append("El CIF/NIF es obligatorio\n");
        }

        // Validar formato CIF/NIF (básico)
        String cif = txtCIF.getText().trim();
        if (!cif.isEmpty() && !cif.matches("[A-Z]?\\d{7,8}[A-Z0-9]")) {
            errores.append("El formato del CIF/NIF no es valido\n");
        }

        // Validar email si está presente
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errores.append("El formato del email no es valido\n");
        }

        // Validar código postal si está presente
        String cp = txtCodigoPostal.getText().trim();
        if (!cp.isEmpty() && !cp.matches("\\d{5}")) {
            errores.append("El codigo postal debe tener 5 digitos\n");
        }

        String erroresStr = errores.toString();
        if (!erroresStr.isEmpty()) {
            DialogUtils.showWarning("Por favor, corrija los siguientes errores:\n\n" + erroresStr);
            return false;
        }

        return true;
    }

    private boolean formularioModificado() {
        return !txtNombre.getText().trim().isEmpty() ||
               !txtCIF.getText().trim().isEmpty() ||
               !txtDireccion.getText().trim().isEmpty();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void setupInlineValidation() {
        if (txtNombre != null && lblErrNombre != null) {
            txtNombre.textProperty().addListener((obs, oldV, newV) -> {
                if (newV == null || newV.trim().isEmpty()) {
                    lblErrNombre.setText("El nombre es obligatorio");
                    txtNombre.setStyle("-fx-border-color: #e55353;");
                } else {
                    lblErrNombre.setText("");
                    txtNombre.setStyle("");
                }
            });
        }

        if (txtCIF != null && lblErrCIF != null) {
            txtCIF.textProperty().addListener((obs, oldV, newV) -> {
                if (newV == null || newV.trim().isEmpty()) {
                    lblErrCIF.setText("El CIF/NIF es obligatorio");
                    txtCIF.setStyle("-fx-border-color: #e55353;");
                } else if (!newV.trim().matches("[A-Z]?\\d{7,8}[A-Z0-9]")) {
                    lblErrCIF.setText("Formato de CIF/NIF inválido");
                    txtCIF.setStyle("-fx-border-color: #e55353;");
                } else {
                    lblErrCIF.setText("");
                    txtCIF.setStyle("");
                }
            });
        }

        if (txtEmail != null && lblErrEmail != null) {
            txtEmail.textProperty().addListener((obs, oldV, newV) -> {
                if (newV == null || newV.trim().isEmpty()) {
                    lblErrEmail.setText("");
                    txtEmail.setStyle("");
                } else if (!newV.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    lblErrEmail.setText("Formato de email inválido");
                    txtEmail.setStyle("-fx-border-color: #e55353;");
                } else {
                    lblErrEmail.setText("");
                    txtEmail.setStyle("");
                }
            });
        }
    }

    private boolean isFormValid() {
        boolean nameOk = txtNombre != null && txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty();
        boolean cifOk = txtCIF != null && txtCIF.getText() != null && txtCIF.getText().trim().matches("[A-Z]?\\d{7,8}[A-Z0-9]");
        boolean emailOk = txtEmail == null || txtEmail.getText() == null || txtEmail.getText().trim().isEmpty() || txtEmail.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$");
        return nameOk && cifOk && emailOk;
    }
}
