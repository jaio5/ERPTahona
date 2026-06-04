package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.service.HorneadaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class HorneadaFormController {
    private static final Logger log = LoggerFactory.getLogger(HorneadaFormController.class);

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtTempInicial;
    @FXML private TextField txtTempFinal;
    @FXML private TextField txtHumedad;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtMerma;
    @FXML private ComboBox<String> cmbResultado;
    @FXML private TextArea txtObservaciones;

    private final HorneadaService horneadaService;
    private Horneada horneada;

    public HorneadaFormController(HorneadaService horneadaService) {
        this.horneadaService = horneadaService;
    }

    @FXML
    public void initialize() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
        if (cmbTipo != null) {
            cmbTipo.getItems().addAll("NORMAL", "TURBO", "VAPOR", "FERMENTACION", "PRECOCIDO");
            cmbTipo.setValue("NORMAL");
        }
        if (cmbResultado != null) {
            cmbResultado.getItems().addAll("OK", "QUEMADO", "CRUDO", "DEFORMADO", "REPETIR");
            cmbResultado.setValue("OK");
        }
    }

    public void setHorneada(Horneada horneada) {
        this.horneada = horneada;
        if (horneada != null) {
            dpFecha.setValue(horneada.getFecha());
            cmbTipo.setValue(horneada.getTipoHorneada());
            txtTempInicial.setText(horneada.getTemperaturaInicial() != null ? horneada.getTemperaturaInicial().toString() : "");
            txtTempFinal.setText(horneada.getTemperaturaFinal() != null ? horneada.getTemperaturaFinal().toString() : "");
            txtHumedad.setText(horneada.getHumedadInicial() != null ? horneada.getHumedadInicial().toString() : "");
            txtCantidad.setText(horneada.getCantidadProducida() != null ? horneada.getCantidadProducida().toString() : "");
            txtMerma.setText(horneada.getMerma() != null ? horneada.getMerma().toString() : "");
            cmbResultado.setValue(horneada.getResultado());
            txtObservaciones.setText(horneada.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (horneada == null) horneada = new Horneada();
            horneada.setFecha(dpFecha.getValue());
            horneada.setTipoHorneada(cmbTipo.getValue());
            horneada.setTemperaturaInicial(parseInt(txtTempInicial.getText()));
            horneada.setTemperaturaFinal(parseInt(txtTempFinal.getText()));
            horneada.setHumedadInicial(parseInt(txtHumedad.getText()));
            horneada.setCantidadProducida(parseDecimal(txtCantidad.getText()));
            horneada.setMerma(parseDecimal(txtMerma.getText()));
            horneada.setResultado(cmbResultado.getValue());
            horneada.setObservaciones(txtObservaciones.getText());

            horneadaService.save(horneada);
            mostrarExito("Horneada registrada");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando horneada", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() { cerrarVentana(); }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

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
        Stage stage = (Stage) cmbTipo.getScene().getWindow();
        stage.close();
    }
}
