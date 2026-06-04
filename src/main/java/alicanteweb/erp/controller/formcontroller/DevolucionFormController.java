package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Devolucion;
import alicanteweb.erp.service.DevolucionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class DevolucionFormController {
    private static final Logger log = LoggerFactory.getLogger(DevolucionFormController.class);

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtMotivo;
    @FXML private TextArea txtObservaciones;

    private final DevolucionService devolucionService;
    private Devolucion devolucion;

    public DevolucionFormController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @FXML
    public void initialize() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
    }

    public void setDevolucion(Devolucion devolucion) {
        this.devolucion = devolucion;
        if (devolucion != null) {
            txtNumero.setText(devolucion.getNumero());
            dpFecha.setValue(devolucion.getFecha());
            txtMotivo.setText(devolucion.getMotivo());
            txtObservaciones.setText(devolucion.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (devolucion == null) devolucion = new Devolucion();
            devolucion.setNumero(txtNumero.getText());
            devolucion.setFecha(dpFecha.getValue());
            devolucion.setMotivo(txtMotivo.getText());
            devolucion.setObservaciones(txtObservaciones.getText());

            devolucionService.save(devolucion);
            mostrarExito("Devolución guardada");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando devolución", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() { cerrarVentana(); }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito"); a.setContentText(msg); a.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }
}
