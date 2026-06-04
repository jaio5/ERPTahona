package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.service.HojaRutaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class HojaRutaFormController {
    private static final Logger log = LoggerFactory.getLogger(HojaRutaFormController.class);

    @FXML private DatePicker dpFecha;
    @FXML private TextField txtConductor;
    @FXML private TextArea txtObservaciones;

    private final HojaRutaService hojaRutaService;
    private HojaRuta hojaRuta;

    public HojaRutaFormController(HojaRutaService hojaRutaService) {
        this.hojaRutaService = hojaRutaService;
    }

    @FXML
    public void initialize() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
    }

    public void setHojaRuta(HojaRuta hojaRuta) {
        this.hojaRuta = hojaRuta;
        if (hojaRuta != null) {
            dpFecha.setValue(hojaRuta.getFecha());
            txtConductor.setText(hojaRuta.getConductor());
            txtObservaciones.setText(hojaRuta.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (hojaRuta == null) hojaRuta = new HojaRuta();
            hojaRuta.setFecha(dpFecha.getValue());
            hojaRuta.setConductor(txtConductor.getText());
            hojaRuta.setObservaciones(txtObservaciones.getText());

            hojaRutaService.save(hojaRuta);
            mostrarExito("Hoja de ruta guardada");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando hoja de ruta", e);
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
        Stage stage = (Stage) txtConductor.getScene().getWindow();
        stage.close();
    }
}
