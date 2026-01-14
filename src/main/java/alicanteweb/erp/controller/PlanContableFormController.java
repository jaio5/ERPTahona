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
    @FXML private ComboBox<String> cmbTipo;
    @FXML private CheckBox chkActivo;

    private final PlanContableService planContableService;
    private PlanContable planContable;

    public PlanContableFormController(PlanContableService planContableService) {
        this.planContableService = planContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PlanContableFormController");
        if (cmbTipo != null) {
            cmbTipo.getItems().addAll("ACTIVO", "PASIVO", "PATRIMONIO_NETO", "INGRESOS", "GASTOS");
        }
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
            if (cmbTipo != null && planContable.getTipo() != null) cmbTipo.setValue(planContable.getTipo());
            if (chkActivo != null) chkActivo.setSelected(planContable.getActiva() != null ? planContable.getActiva() : true);
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            if (planContable == null) {
                planContable = new PlanContable();
            }

            planContable.setCodigo(txtCodigo.getText().trim());
            planContable.setNombre(txtNombre.getText().trim());
            planContable.setTipo(cmbTipo != null ? cmbTipo.getValue() : null);
            planContable.setActiva(chkActivo != null ? chkActivo.isSelected() : true);

            planContableService.guardar(planContable);

            mostrarExito("Cuenta guardada correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando cuenta", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
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

