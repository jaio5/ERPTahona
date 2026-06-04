package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.AppccControl;
import alicanteweb.erp.service.AppccControlService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class AppccFormController {
    private static final Logger log = LoggerFactory.getLogger(AppccFormController.class);

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbPuntoCritico;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtLimiteCritico;
    @FXML private ComboBox<String> cmbResultado;
    @FXML private TextArea txtAccionCorrectiva;
    @FXML private TextField txtResponsable;

    private final AppccControlService appccService;
    private AppccControl control;

    public AppccFormController(AppccControlService appccService) {
        this.appccService = appccService;
    }

    @FXML
    public void initialize() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
        if (cmbPuntoCritico != null) {
            cmbPuntoCritico.getItems().addAll(
                "Temperatura horneado", "Temperatura almacenamiento frío",
                "Temperatura almacenamiento seco", "Temperatura transporte",
                "Humedad obrador", "Limpieza y desinfección",
                "Control de plagas", "Potabilidad del agua",
                "Trazabilidad materias primas", "Control de caducidades"
            );
        }
        if (cmbResultado != null) {
            cmbResultado.getItems().addAll("CONFORME", "NO CONFORME", "CORREGIDO");
            cmbResultado.setValue("CONFORME");
        }
    }

    public void setAppccControl(AppccControl control) {
        this.control = control;
        if (control != null) {
            dpFecha.setValue(control.getFecha());
            cmbPuntoCritico.setValue(control.getPuntoCritico());
            txtDescripcion.setText(control.getDescripcion());
            txtTemperatura.setText(control.getTemperatura() != null ? control.getTemperatura().toString() : "");
            txtLimiteCritico.setText(control.getLimiteCritico() != null ? control.getLimiteCritico().toString() : "");
            cmbResultado.setValue(control.getResultado());
            txtAccionCorrectiva.setText(control.getAccionCorrectiva());
            txtResponsable.setText(control.getResponsable());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (control == null) control = new AppccControl();
            control.setFecha(dpFecha.getValue());
            control.setPuntoCritico(cmbPuntoCritico.getValue());
            control.setDescripcion(txtDescripcion.getText());
            control.setTemperatura(parseDecimal(txtTemperatura.getText()));
            control.setLimiteCritico(parseDecimal(txtLimiteCritico.getText()));
            control.setResultado(cmbResultado.getValue());
            control.setAccionCorrectiva(txtAccionCorrectiva.getText());
            control.setResponsable(txtResponsable.getText());

            appccService.save(control);
            mostrarExito("Control APPCC guardado");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando control APPCC", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() { cerrarVentana(); }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (Exception e) { return null; }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito"); a.setContentText(msg); a.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) cmbPuntoCritico.getScene().getWindow();
        stage.close();
    }
}
