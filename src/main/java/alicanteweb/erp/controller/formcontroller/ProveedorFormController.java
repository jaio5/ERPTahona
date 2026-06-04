package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ClienteDatosExternosService;
import alicanteweb.erp.service.ProveedorService;
import alicanteweb.erp.service.dto.ClienteDatosExternos;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.DialogUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @FXML private Button btnBuscarDatosEmpresa;
    @FXML private ComboBox<Proveedor> cbCoincidenciasProveedor;
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
    private final ClienteDatosExternosService clienteDatosExternosService;
    private Proveedor proveedorActual;
    private boolean modoEdicion = false;

    public ProveedorFormController(ProveedorService proveedorService,
                                   ClienteDatosExternosService clienteDatosExternosService) {
        this.proveedorService = proveedorService;
        this.clienteDatosExternosService = clienteDatosExternosService;
    }

    @FXML
    public void initialize() {
        log.info("✅ ProveedorFormController inicializado");

        configurarProvincias();
        configurarFormasPago();
        configurarValidaciones();
        configurarCoincidencias();

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

    private void configurarCoincidencias() {
        if (cbCoincidenciasProveedor == null) {
            return;
        }
        cbCoincidenciasProveedor.setVisible(false);
        cbCoincidenciasProveedor.setManaged(false);
        cbCoincidenciasProveedor.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoCoincidencia(item));
            }
        });
        cbCoincidenciasProveedor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoCoincidencia(item));
            }
        });
        cbCoincidenciasProveedor.valueProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null && (proveedorActual == null || proveedorActual.getId() == null
                || !seleccionado.getId().equals(proveedorActual.getId()))) {
                cargarProveedorExistente(seleccionado);
            }
        });

        if (txtNombre != null) {
            txtNombre.textProperty().addListener((obs, oldV, newV) -> actualizarCoincidenciasLocales());
        }
        if (txtCIF != null) {
            txtCIF.textProperty().addListener((obs, oldV, newV) -> actualizarCoincidenciasLocales());
        }
    }

    private void actualizarCoincidenciasLocales() {
        if (cbCoincidenciasProveedor == null) {
            return;
        }
        String cif = txtCIF != null && txtCIF.getText() != null ? txtCIF.getText().trim() : "";
        String nombre = txtNombre != null && txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        try {
            List<Proveedor> coincidencias = !cif.isBlank()
                ? proveedorService.findByCif(cif).stream().toList()
                : nombre.length() >= 3 ? proveedorService.searchByNombre(nombre) : List.of();
            if (proveedorActual != null && proveedorActual.getId() != null) {
                coincidencias = coincidencias.stream()
                    .filter(p -> p.getId() == null || !p.getId().equals(proveedorActual.getId()))
                    .toList();
            }
            cbCoincidenciasProveedor.setItems(FXCollections.observableArrayList(coincidencias.stream().limit(8).toList()));
            boolean mostrar = !coincidencias.isEmpty();
            cbCoincidenciasProveedor.setVisible(mostrar);
            cbCoincidenciasProveedor.setManaged(mostrar);
            if (mostrar && cbCoincidenciasProveedor.getScene() != null && !cbCoincidenciasProveedor.isShowing()) {
                cbCoincidenciasProveedor.show();
            }
        } catch (Exception e) {
            log.debug("No se pudieron cargar coincidencias de proveedores: {}", e.getMessage());
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
    public void onBuscarDatosEmpresa() {
        String cif = txtCIF != null ? txtCIF.getText().trim().toUpperCase().replace(" ", "") : "";
        String nombre = txtNombre != null ? txtNombre.getText().trim() : "";
        boolean buscarPorCif = !cif.isEmpty();

        if (cif.isEmpty() && nombre.isEmpty()) {
            DialogUtils.showWarning("Introduce primero el CIF/NIF o la razon social para buscar los datos del proveedor.");
            if (txtCIF != null) txtCIF.requestFocus();
            return;
        }

        if (buscarPorCif && !isValidCif(cif)) {
            DialogUtils.showWarning("El formato del CIF/NIF no es valido.");
            if (txtCIF != null) txtCIF.requestFocus();
            return;
        }

        Optional<Proveedor> existente = buscarPorCif ? proveedorService.findByCif(cif) : proveedorService.findByNombreExacto(nombre);
        if (existente.isPresent() && (proveedorActual == null || proveedorActual.getId() == null
            || !existente.get().getId().equals(proveedorActual.getId()))) {
            cargarProveedorExistente(existente.get());
            return;
        }

        Task<List<ClienteDatosExternos>> task = new Task<>() {
            @Override
            protected List<ClienteDatosExternos> call() {
                return buscarPorCif
                    ? clienteDatosExternosService.buscarCoincidenciasPorCif(cif)
                    : clienteDatosExternosService.buscarCoincidenciasPorNombre(nombre);
            }
        };

        task.setOnRunning(e -> {
            if (btnBuscarDatosEmpresa != null) {
                btnBuscarDatosEmpresa.setDisable(true);
                btnBuscarDatosEmpresa.setText("Buscando...");
            }
        });
        task.setOnSucceeded(e -> {
            restaurarBotonBusqueda();
            List<ClienteDatosExternos> datos = task.getValue();
            if (datos == null || datos.isEmpty()) {
                DialogUtils.showInfo("No se encontraron datos para ese CIF/NIF.");
                return;
            }
            ClienteDatosExternos datosExternos = seleccionarDatosExternos(datos).orElse(null);
            if (datosExternos == null) {
                return;
            }
            if (datosExternos.getCif() != null) {
                Optional<Proveedor> existentePorCif = proveedorService.findByCif(datosExternos.getCif());
                if (existentePorCif.isPresent() && (proveedorActual == null || proveedorActual.getId() == null
                    || !existentePorCif.get().getId().equals(proveedorActual.getId()))) {
                    cargarProveedorExistente(existentePorCif.get());
                    return;
                }
            }
            aplicarDatosExternos(datosExternos);
            DialogUtils.showInfo("Datos del proveedor cargados desde la API. Revisa la ficha antes de guardar.");
        });
        task.setOnFailed(e -> {
            restaurarBotonBusqueda();
            Throwable ex = task.getException();
            log.warn("No se pudieron cargar datos externos del proveedor", ex);
            DialogUtils.showError(ex != null ? ex.getMessage() : "No se pudo consultar la API de empresas");
        });

        Thread thread = new Thread(task, "proveedor-datos-externos");
        thread.setDaemon(true);
        thread.start();
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

    private void cargarProveedorExistente(Proveedor existente) {
        proveedorActual = existente;
        modoEdicion = true;
        if (cbCoincidenciasProveedor != null) {
            cbCoincidenciasProveedor.hide();
            cbCoincidenciasProveedor.setVisible(false);
            cbCoincidenciasProveedor.setManaged(false);
            cbCoincidenciasProveedor.getSelectionModel().clearSelection();
        }
        if (lblTitulo != null) lblTitulo.setText("Editar Proveedor");
        cargarDatosProveedor(existente);
        DialogUtils.showInfo("Este CIF/NIF ya existe en la base de datos. Se ha cargado la ficha existente para evitar duplicados.");
    }

    private Optional<ClienteDatosExternos> seleccionarDatosExternos(List<ClienteDatosExternos> resultados) {
        if (resultados.size() == 1) {
            return Optional.of(resultados.get(0));
        }
        ChoiceDialog<ClienteDatosExternos> dialog = new ChoiceDialog<>(resultados.get(0), resultados);
        dialog.setTitle("Seleccionar empresa");
        dialog.setHeaderText("Se encontraron varias coincidencias");
        dialog.setContentText("Elige la ficha que quieres cargar:");
        return dialog.showAndWait();
    }

    private void aplicarDatosExternos(ClienteDatosExternos datos) {
        if (datos.getCif() != null && txtCIF != null) txtCIF.setText(datos.getCif().trim().toUpperCase());
        if (datos.getNombre() != null && txtNombre != null) txtNombre.setText(datos.getNombre());
        if (datos.getDireccion() != null && txtDireccion != null) txtDireccion.setText(datos.getDireccion());
        if (datos.getCodigoPostal() != null && txtCodigoPostal != null) txtCodigoPostal.setText(datos.getCodigoPostal());
        if (datos.getPoblacion() != null && txtPoblacion != null) txtPoblacion.setText(datos.getPoblacion());
        if (datos.getProvincia() != null && cbProvincia != null) seleccionarProvincia(datos.getProvincia());
        if (datos.getTelefono() != null && txtTelefono != null) txtTelefono.setText(datos.getTelefono());
        if (datos.getEmail() != null && txtEmail != null) txtEmail.setText(datos.getEmail());
        if (datos.getEstado() != null && chkActivo != null) {
            chkActivo.setSelected(!datos.getEstado().equalsIgnoreCase("INACTIVA")
                && !datos.getEstado().equalsIgnoreCase("EXTINGUIDA")
                && !datos.getEstado().equalsIgnoreCase("DISUELTA"));
        }
    }

    private void seleccionarProvincia(String provincia) {
        String normalizada = provincia.trim();
        cbProvincia.getItems().stream()
            .filter(p -> p.equalsIgnoreCase(normalizada))
            .findFirst()
            .ifPresentOrElse(cbProvincia::setValue, () -> cbProvincia.setValue(normalizada));
    }

    private void restaurarBotonBusqueda() {
        if (btnBuscarDatosEmpresa != null) {
            btnBuscarDatosEmpresa.setDisable(false);
            btnBuscarDatosEmpresa.setText("Buscar datos");
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

    private boolean isValidCif(String cif) {
        return cif != null && cif.matches("[A-Z]?\\d{7,8}[A-Z0-9]");
    }

    private String textoCoincidencia(Proveedor proveedor) {
        String cif = proveedor.getCif() == null || proveedor.getCif().isBlank() ? "" : " (" + proveedor.getCif() + ")";
        String poblacion = proveedor.getPoblacion() == null || proveedor.getPoblacion().isBlank() ? "" : " - " + proveedor.getPoblacion();
        return proveedor.getNombre() + cif + poblacion;
    }
}
