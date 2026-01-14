package alicanteweb.erp.controller;

import alicanteweb.erp.service.Modelo347Service;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para el formulario del Modelo 347
 */
@Controller
public class Modelo347FormController {
    private static final Logger log = LoggerFactory.getLogger(Modelo347FormController.class);

    @FXML private ComboBox<Integer> cmbEjercicio;
    @FXML private TextField txtNifDeclarante;
    @FXML private TextField txtNombreDeclarante;
    @FXML private CheckBox chkComplementaria;
    @FXML private CheckBox chkSustitutiva;
    @FXML private TextArea txtObservaciones;

    private final Modelo347Service modelo347Service;

    public Modelo347FormController(Modelo347Service modelo347Service) {
        this.modelo347Service = modelo347Service;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando Modelo347FormController");
        if (cmbEjercicio != null) {
            int anioActual = java.time.LocalDate.now().getYear();
            for (int i = anioActual; i >= anioActual - 5; i--) {
                cmbEjercicio.getItems().add(i);
            }
            cmbEjercicio.setValue(anioActual - 1); // Por defecto el año anterior
        }
    }

    @FXML
    public void onGenerar() {
        try {
            if (!validarFormulario()) return;

            int ejercicio = cmbEjercicio.getValue();
            log.info("Generando Modelo 347 para ejercicio {}", ejercicio);

            // Generar el modelo
            modelo347Service.generarModelo347(ejercicio);

            mostrarExito("Modelo 347 generado correctamente para el ejercicio " + ejercicio);

        } catch (Exception e) {
            log.error("Error generando Modelo 347", e);
            mostrarError("Error al generar: " + e.getMessage());
        }
    }

    @FXML
    public void onExportar() {
        try {
            if (cmbEjercicio.getValue() == null) {
                mostrarError("Seleccione un ejercicio");
                return;
            }

            mostrarInfo("Exportacion del Modelo 347 - Funcionalidad en desarrollo");
        } catch (Exception e) {
            log.error("Error exportando Modelo 347", e);
            mostrarError("Error al exportar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (cmbEjercicio == null || cmbEjercicio.getValue() == null) {
            mostrarError("Debe seleccionar un ejercicio");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        if (cmbEjercicio != null && cmbEjercicio.getScene() != null) {
            Stage stage = (Stage) cmbEjercicio.getScene().getWindow();
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

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacion");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

