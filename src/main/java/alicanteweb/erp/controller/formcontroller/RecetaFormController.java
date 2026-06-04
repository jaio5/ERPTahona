package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Receta;
import alicanteweb.erp.service.RecetaService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class RecetaFormController {
    private static final Logger log = LoggerFactory.getLogger(RecetaFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtTiempoPrep;
    @FXML private TextField txtTiempoHorneado;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtRendimiento;
    @FXML private TextField txtUnidadRendimiento;
    @FXML private TextField txtAlergenos;

    private final RecetaService recetaService;
    private Receta receta;

    public RecetaFormController(RecetaService recetaService) {
        this.recetaService = recetaService;
    }

    @FXML
    public void initialize() {}

    public void setReceta(Receta receta) {
        this.receta = receta;
        if (receta != null) {
            txtCodigo.setText(receta.getCodigo());
            txtNombre.setText(receta.getNombre());
            txtDescripcion.setText(receta.getDescripcion());
            txtTiempoPrep.setText(receta.getTiempoPreparacion() != null ? receta.getTiempoPreparacion().toString() : "");
            txtTiempoHorneado.setText(receta.getTiempoHorneado() != null ? receta.getTiempoHorneado().toString() : "");
            txtTemperatura.setText(receta.getTemperaturaHorneado() != null ? receta.getTemperaturaHorneado().toString() : "");
            txtRendimiento.setText(receta.getRendimientoCantidad() != null ? receta.getRendimientoCantidad().toString() : "");
            txtUnidadRendimiento.setText(receta.getUnidadRendimiento());
            if (txtAlergenos != null) txtAlergenos.setText(receta.getAlergenos());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (receta == null) receta = new Receta();
            receta.setCodigo(txtCodigo.getText());
            receta.setNombre(txtNombre.getText());
            receta.setDescripcion(txtDescripcion.getText());
            receta.setTiempoPreparacion(parseInt(txtTiempoPrep.getText()));
            receta.setTiempoHorneado(parseInt(txtTiempoHorneado.getText()));
            receta.setTemperaturaHorneado(parseInt(txtTemperatura.getText()));
            receta.setRendimientoCantidad(parseDecimal(txtRendimiento.getText()));
            receta.setUnidadRendimiento(txtUnidadRendimiento.getText());
            if (txtAlergenos != null) receta.setAlergenos(txtAlergenos.getText());

            recetaService.save(receta);
            mostrarExito("Receta guardada");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando receta", e);
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
        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.close();
    }
}
