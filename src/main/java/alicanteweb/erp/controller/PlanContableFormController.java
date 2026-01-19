package alicanteweb.erp.controller;

import alicanteweb.erp.entities.PlanContable;
import alicanteweb.erp.service.PlanContableService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para el formulario de Plan Contable
 */
@Controller
public class PlanContableFormController {
    private static final Logger log = LoggerFactory.getLogger(PlanContableFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cbGrupo;
    @FXML private ComboBox<String> cbTipoCuenta;
    @FXML private TextField txtSaldoInicial;
    @FXML private TextField txtNivel;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkImputable;
    @FXML private CheckBox chkAuxiliar;
    @FXML private CheckBox chkActivo;

    private final PlanContableService planContableService;
    private PlanContable planContable;
    // Valores originales para detectar cambios
    private String originalCodigo = "";
    private String originalNombre = "";
    private String originalTipo = "";
    private String originalGrupo = "";
    private boolean originalActivo = true;

    public PlanContableFormController(PlanContableService planContableService) {
        this.planContableService = planContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PlanContableFormController");
        if (cbTipoCuenta != null && cbTipoCuenta.getItems().isEmpty()) {
            cbTipoCuenta.getItems().addAll("ACTIVO", "PASIVO", "PATRIMONIO_NETO", "INGRESOS", "GASTOS");
        }

        // Inicializar grupos (primer dígito de la cuenta) para facilitar selección
        if (cbGrupo != null && cbGrupo.getItems().isEmpty()) {
            for (int i = 1; i <= 9; i++) cbGrupo.getItems().add(String.valueOf(i));
            cbGrupo.setValue("4"); // sugerir grupo 4 (clientes) por defecto
        }

        // Si hay un selector adicional para clasificación distinta, asegúrate de que exista (ya inicializamos cbTipoCuenta)

        // Valores por defecto para campos opcionales
        if (txtSaldoInicial != null && (txtSaldoInicial.getText() == null || txtSaldoInicial.getText().isBlank())) txtSaldoInicial.setText("0.00");
        if (txtNivel != null && (txtNivel.getText() == null || txtNivel.getText().isBlank())) txtNivel.setText("0");
        if (txtDescripcion != null && (txtDescripcion.getText() == null)) txtDescripcion.setText("");
        if (chkImputable != null) chkImputable.setSelected(true);
        if (chkAuxiliar != null) chkAuxiliar.setSelected(false);
        if (lblTitulo != null && (lblTitulo.getText() == null || lblTitulo.getText().isBlank())) lblTitulo.setText("✏️ Nueva Cuenta Contable");

        if (chkActivo != null) {
            chkActivo.setSelected(true);
        }
    }

    public void setPlanContable(PlanContable planContable) {
        this.planContable = planContable;
        cargarDatos();
    }

    private void cargarDatos() {
        if (planContable != null) {
            if (txtCodigo != null) txtCodigo.setText(planContable.getCodigo());
            if (txtNombre != null) txtNombre.setText(planContable.getNombre());
            if (cbTipoCuenta != null && planContable.getTipo() != null) cbTipoCuenta.setValue(planContable.getTipo());
            if (chkActivo != null) chkActivo.setSelected(planContable.getActiva() != null ? planContable.getActiva() : true);

            // Guardar valores originales
            originalCodigo = planContable.getCodigo() != null ? planContable.getCodigo() : "";
            originalNombre = planContable.getNombre() != null ? planContable.getNombre() : "";
            originalTipo = planContable.getTipo() != null ? planContable.getTipo() : "";
            originalGrupo = (cbGrupo != null && cbGrupo.getValue() != null) ? cbGrupo.getValue() : "";
            originalActivo = chkActivo != null ? chkActivo.isSelected() : true;
        } else {
            // Nuevo
            originalCodigo = "";
            originalNombre = "";
            originalTipo = "";
            originalGrupo = cbGrupo != null && cbGrupo.getValue() != null ? cbGrupo.getValue() : "";
            originalActivo = chkActivo != null ? chkActivo.isSelected() : true;
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            String codigo = txtCodigo != null ? txtCodigo.getText().trim() : "";

            // Validación: código numérico de 1 a 6 dígitos
            if (!codigo.matches("\\d{1,6}")) {
                mostrarError("El código debe ser numérico y tener entre 1 y 6 dígitos (ej: 430001 o 43)");
                return;
            }

            // No permitir ceros a la izquierda en códigos multi-dígito
            if (codigo.length() > 1 && codigo.startsWith("0")) {
                mostrarError("El código no puede comenzar con 0");
                return;
            }

            // Para códigos de más de un dígito, exigir existencia de cuenta padre (prefijo)
            if (codigo.length() > 1) {
                String parent = findExistingParentPrefix(codigo);
                if (parent == null) {
                    String sugerido = codigo.substring(0, codigo.length() - 1);
                    mostrarError("No existe una cuenta padre. Crea primero la cuenta padre con código: " + sugerido);
                    return;
                }
            }

            // Comprobar duplicados (si no estamos editando la misma cuenta)
            var existente = planContableService.obtenerPorCodigo(codigo);
            if (existente.isPresent()) {
                PlanContable found = existente.get();
                if (planContable == null || !found.getId().equals(planContable.getId())) {
                    mostrarError("Ya existe una cuenta con el código: " + codigo);
                    return;
                }
            }

            if (planContable == null) {
                planContable = new PlanContable();
            }

            planContable.setCodigo(codigo);
            planContable.setNombre(txtNombre != null ? txtNombre.getText().trim() : "");
            planContable.setTipo(cbTipoCuenta != null ? cbTipoCuenta.getValue() : null);

            // Calcular nivel automáticamente a partir del código (longitud)
            try {
                planContable.setNivel(codigo.length());
            } catch (Exception ex) {
                planContable.setNivel(0);
            }

            // Opcional: asignar grupo desde el primer dígito
            if (!codigo.isEmpty()) {
                String grupo = String.valueOf(codigo.charAt(0));
                if (cbGrupo != null && cbGrupo.getItems() != null && cbGrupo.getItems().contains(grupo)) {
                    cbGrupo.setValue(grupo);
                }
            }

            planContable.setActiva(chkActivo == null || chkActivo.isSelected());

            planContableService.guardar(planContable);

            mostrarExito("Cuenta guardada correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando cuenta", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    // Helper: buscar prefijo existente (cuenta padre) recortando desde la derecha
    private String findExistingParentPrefix(String codigo) {
        for (int i = codigo.length() - 1; i >= 1; i--) {
            String prefix = codigo.substring(0, i);
            try {
                if (planContableService.obtenerPorCodigo(prefix).isPresent()) return prefix;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @FXML
    public void onCancelar() {
        if (formularioModificado()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar");
            confirm.setHeaderText("Hay cambios sin guardar");
            confirm.setContentText("¿Desea salir sin guardar los cambios?");
            var res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                cerrarVentana();
            }
        } else {
            cerrarVentana();
        }
    }

    private boolean formularioModificado() {
        String codigo = txtCodigo != null && txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        String nombre = txtNombre != null && txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        String tipo = cbTipoCuenta != null && cbTipoCuenta.getValue() != null ? cbTipoCuenta.getValue() : "";
        String grupo = cbGrupo != null && cbGrupo.getValue() != null ? cbGrupo.getValue() : "";
        boolean activo = chkActivo != null ? chkActivo.isSelected() : true;

        if (!codigo.equals(originalCodigo)) return true;
        if (!nombre.equals(originalNombre)) return true;
        if (!tipo.equals(originalTipo)) return true;
        if (!grupo.equals(originalGrupo)) return true;
        if (activo != originalActivo) return true;
        return false;
    }

    private boolean validarFormulario() {
        if (txtCodigo == null || txtCodigo.getText().trim().isEmpty()) {
            mostrarError("El codigo es obligatorio");
            return false;
        }
        if (txtNombre == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        if (txtCodigo != null && txtCodigo.getScene() != null) {
            Stage stage = (Stage) txtCodigo.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
