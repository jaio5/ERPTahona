package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.Optional;

/**
 * Controlador para el formulario de creación/edición de clientes
 */
@Controller
public class ClienteFormController {
    private static final Logger log = LoggerFactory.getLogger(ClienteFormController.class);

    // Campos básicos
    @FXML private Label lblTitulo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCIF;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

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

    private final ClienteService clienteService;
    private Cliente clienteActual;
    private boolean modoEdicion = false;

    public ClienteFormController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        log.info("✅ ClienteFormController inicializado");

        // Configurar provincias españolas
        configurarProvincias();

        // Configurar formas de pago
        configurarFormasPago();

        // Configurar validaciones
        configurarValidaciones();
    }

    private void configurarProvincias() {
        if (cbProvincia != null) {
            cbProvincia.getItems().addAll(Arrays.asList(
                "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila",
                "Badajoz", "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria",
                "Castellón", "Ciudad Real", "Córdoba", "Cuenca", "Gerona",
                "Granada", "Guadalajara", "Guipúzcoa", "Huelva", "Huesca",
                "Islas Baleares", "Jaén", "La Coruña", "La Rioja", "Las Palmas",
                "León", "Lérida", "Lugo", "Madrid", "Málaga", "Murcia",
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
                "Pagaré",
                "Recibo",
                "Contado",
                "30 días",
                "60 días",
                "90 días"
            );
            cbFormaPago.setValue("Contado");
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

    public void setCliente(Cliente cliente) {
        this.clienteActual = cliente;
        this.modoEdicion = (cliente != null && cliente.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                lblTitulo.setText("Editar Cliente");
                cargarDatosCliente(cliente);
            } else {
                lblTitulo.setText("Nuevo Cliente");
                limpiarFormulario();
                generarCodigoAutomatico();
            }
        });
    }

    private void cargarDatosCliente(Cliente cliente) {
        if (txtCodigo != null) txtCodigo.setText(cliente.getCodigo());
        if (txtNombre != null) txtNombre.setText(cliente.getNombre());
        if (txtCIF != null) txtCIF.setText(cliente.getCif());
        if (txtTelefono != null) txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");
        if (txtEmail != null) txtEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");

        if (txtDireccion != null) txtDireccion.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");
        if (txtCodigoPostal != null) txtCodigoPostal.setText(cliente.getCodigoPostal() != null ? cliente.getCodigoPostal() : "");
        if (txtPoblacion != null) txtPoblacion.setText(cliente.getPoblacion() != null ? cliente.getPoblacion() : "");

        if (cbProvincia != null && cliente.getProvincia() != null && cbProvincia.getItems().contains(cliente.getProvincia())) {
            cbProvincia.setValue(cliente.getProvincia());
        }

        if (txtNotas != null) txtNotas.setText(cliente.getNotas() != null ? cliente.getNotas() : "");
        if (chkActivo != null) chkActivo.setSelected(cliente.getActivo() != null ? cliente.getActivo() : true);

        log.info("Datos cargados para cliente: {}", cliente.getId());
    }

    private void limpiarFormulario() {
        if (txtCodigo != null) txtCodigo.clear();
        if (txtNombre != null) txtNombre.clear();
        if (txtCIF != null) txtCIF.clear();
        if (txtTelefono != null) txtTelefono.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtDireccion != null) txtDireccion.clear();
        if (txtCodigoPostal != null) txtCodigoPostal.clear();
        if (txtPoblacion != null) txtPoblacion.clear();
        if (cbProvincia != null) cbProvincia.setValue("Alicante");
        if (cbFormaPago != null) cbFormaPago.setValue("Contado");
        if (txtDiasCredito != null) txtDiasCredito.setText("0");
        if (txtDescuento != null) txtDescuento.setText("0.00");
        if (txtNotas != null) txtNotas.clear();
        if (chkActivo != null) chkActivo.setSelected(true);
    }

    private void generarCodigoAutomatico() {
        try {
            // Generar código basado en el número de clientes existentes
            long count = clienteService.findAll().size();
            long nextId = count + 1;
            String codigo = String.format("CLI%04d", nextId);
            txtCodigo.setText(codigo);
        } catch (Exception e) {
            log.warn("No se pudo generar código automático", e);
            txtCodigo.setText("CLI0001");
        }
    }

    @FXML
    public void onGuardar() {
        log.info("🔍 Iniciando guardado de cliente...");

        if (!validarFormulario()) {
            log.warn("⚠️ Validación fallida");
            return;
        }

        try {
            if (clienteActual == null) {
                clienteActual = new Cliente();
                log.info("Creando nuevo cliente");
            } else {
                log.info("Actualizando cliente ID: {}", clienteActual.getId());
            }

            // Datos básicos (obligatorios)
            clienteActual.setCodigo(txtCodigo != null ? txtCodigo.getText().trim() : "");
            clienteActual.setNombre(txtNombre != null ? txtNombre.getText().trim() : "");
            clienteActual.setCif(txtCIF != null ? txtCIF.getText().trim().toUpperCase() : "");

            // Campos opcionales con validación de null
            if (txtTelefono != null) {
                String telefono = txtTelefono.getText().trim();
                clienteActual.setTelefono(telefono.isEmpty() ? null : telefono);
            }

            if (txtEmail != null) {
                String email = txtEmail.getText().trim();
                clienteActual.setEmail(email.isEmpty() ? null : email);
            }

            // Dirección
            if (txtDireccion != null) {
                String direccion = txtDireccion.getText().trim();
                clienteActual.setDireccion(direccion.isEmpty() ? null : direccion);
            }

            if (txtCodigoPostal != null) {
                String cp = txtCodigoPostal.getText().trim();
                clienteActual.setCodigoPostal(cp.isEmpty() ? null : cp);
            }

            if (txtPoblacion != null) {
                String poblacion = txtPoblacion.getText().trim();
                clienteActual.setPoblacion(poblacion.isEmpty() ? null : poblacion);
            }

            if (cbProvincia != null) {
                clienteActual.setProvincia(cbProvincia.getValue());
            }

            // Datos adicionales
            if (txtNotas != null) {
                String notas = txtNotas.getText().trim();
                clienteActual.setNotas(notas.isEmpty() ? null : notas);
            }

            if (chkActivo != null) {
                clienteActual.setActivo(chkActivo.isSelected());
            } else {
                clienteActual.setActivo(true); // Default si el checkbox no existe
            }

            log.info("📝 Datos del cliente preparados: Código={}, Nombre={}, CIF={}, Activo={}",
                clienteActual.getCodigo(),
                clienteActual.getNombre(),
                clienteActual.getCif(),
                clienteActual.getActivo());

            // Guardar
            Cliente guardado = clienteService.save(clienteActual);
            log.info("✅ Cliente guardado exitosamente: ID={}, Código={}, Nombre={}",
                guardado.getId(),
                guardado.getCodigo(),
                guardado.getNombre());

            mostrarExito(modoEdicion ?
                "Cliente actualizado correctamente" :
                "Cliente creado correctamente");

            // Cerrar el diálogo
            cerrarVentana();

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("❌ Error de integridad de datos", e);
            String mensaje = "Error: ";
            if (e.getMessage().contains("codigo")) {
                mensaje += "Ya existe un cliente con ese código";
            } else if (e.getMessage().contains("cif")) {
                mensaje += "Ya existe un cliente con ese CIF";
            } else {
                mensaje += "Datos duplicados o inválidos";
            }
            mostrarError(mensaje);
        } catch (jakarta.validation.ConstraintViolationException e) {
            log.error("❌ Error de validación de constraints", e);
            StringBuilder errores = new StringBuilder("Errores de validación:\n");
            e.getConstraintViolations().forEach(cv ->
                errores.append("• ").append(cv.getMessage()).append("\n")
            );
            mostrarError(errores.toString());
        } catch (Exception e) {
            log.error("❌ Error inesperado guardando cliente", e);
            mostrarError("Error al guardar el cliente:\n" +
                e.getClass().getSimpleName() + ": " +
                (e.getMessage() != null ? e.getMessage() : "Error desconocido") +
                "\n\nRevise los logs para más detalles.");
        }
    }

    @FXML
    public void onCancelar() {
        if (formularioModificado()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar");
            confirmacion.setHeaderText("¿Descartar cambios?");
            confirmacion.setContentText("Hay cambios sin guardar. ¿Desea salir sin guardar?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                cerrarVentana();
            }
        } else {
            cerrarVentana();
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar campos obligatorios con verificación de null
        if (txtNombre == null || txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }

        if (txtCIF == null || txtCIF.getText() == null || txtCIF.getText().trim().isEmpty()) {
            errores.append("• El CIF/NIF es obligatorio\n");
        }

        // Validar formato CIF/NIF (básico) solo si existe
        if (txtCIF != null && txtCIF.getText() != null) {
            String cif = txtCIF.getText().trim();
            if (!cif.isEmpty() && !cif.matches("[A-Z]?\\d{7,8}[A-Z0-9]")) {
                errores.append("• El formato del CIF/NIF no es válido\n");
            }
        }

        // Validar email si está presente y no es null
        if (txtEmail != null && txtEmail.getText() != null) {
            String email = txtEmail.getText().trim();
            if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errores.append("• El formato del email no es válido\n");
            }
        }

        // Validar código postal si está presente y no es null
        if (txtCodigoPostal != null && txtCodigoPostal.getText() != null) {
            String cp = txtCodigoPostal.getText().trim();
            if (!cp.isEmpty() && !cp.matches("\\d{5}")) {
                errores.append("• El código postal debe tener 5 dígitos\n");
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta("Por favor, corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean formularioModificado() {
        // Verificar si hay cambios en el formulario con validación de null
        boolean nombreModificado = txtNombre != null && txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty();
        boolean cifModificado = txtCIF != null && txtCIF.getText() != null && !txtCIF.getText().trim().isEmpty();
        boolean direccionModificada = txtDireccion != null && txtDireccion.getText() != null && !txtDireccion.getText().trim().isEmpty();

        return nombreModificado || cifModificado || direccionModificada;
    }

    private void cerrarVentana() {
        try {
            if (txtNombre != null && txtNombre.getScene() != null) {
                Stage stage = (Stage) txtNombre.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo cerrar la ventana correctamente", e);
        }
    }

    private void mostrarAlerta(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atención");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarExito(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}

