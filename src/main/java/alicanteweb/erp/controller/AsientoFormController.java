package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.service.AsientoContableService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Controlador para el formulario de Asiento Contable
 */
@Controller
public class AsientoFormController {
    private static final Logger log = LoggerFactory.getLogger(AsientoFormController.class);

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtConcepto;
    @FXML private TextArea txtDescripcion;

    private final AsientoContableService asientoContableService;
    private AsientoContable asientoContable;

    public AsientoFormController(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AsientoFormController");
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
        }
    }

    public void setAsientoContable(AsientoContable asientoContable) {
        this.asientoContable = asientoContable;
        cargarDatos();
    }

    private void cargarDatos() {
        if (asientoContable != null) {
            if (txtNumero != null) txtNumero.setText(asientoContable.getNumero());
            if (dpFecha != null && asientoContable.getFecha() != null) dpFecha.setValue(asientoContable.getFecha());
            if (txtConcepto != null) txtConcepto.setText(asientoContable.getConcepto());
            if (txtDescripcion != null) txtDescripcion.setText(asientoContable.getDescripcion());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            if (asientoContable == null) {
                asientoContable = new AsientoContable();
            }

            asientoContable.setNumero(txtNumero != null ? txtNumero.getText().trim() : null);
            asientoContable.setFecha(dpFecha.getValue());
            asientoContable.setConcepto(txtConcepto != null ? txtConcepto.getText() : null);
            asientoContable.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);

            asientoContableService.save(asientoContable);

            mostrarExito("Asiento contable guardado correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando asiento contable", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (dpFecha == null || dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        if (dpFecha != null && dpFecha.getScene() != null) {
            Stage stage = (Stage) dpFecha.getScene().getWindow();
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

