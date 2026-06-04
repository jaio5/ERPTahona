package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.service.LoteService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class LoteFormController {
    private static final Logger log = LoggerFactory.getLogger(LoteFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private DatePicker dpFechaProduccion;
    @FXML private DatePicker dpFechaCaducidad;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtRegistroSanitario;
    @FXML private TextArea txtObservaciones;

    private final LoteService loteService;
    private Lote lote;

    public LoteFormController(LoteService loteService) {
        this.loteService = loteService;
    }

    @FXML
    public void initialize() {
        if (dpFechaProduccion != null) dpFechaProduccion.setValue(LocalDate.now());
        if (dpFechaCaducidad != null) dpFechaCaducidad.setValue(LocalDate.now().plusDays(7));
    }

    public void setLote(Lote lote) {
        this.lote = lote;
        if (lote != null) {
            txtCodigo.setText(lote.getCodigo());
            dpFechaProduccion.setValue(lote.getFechaProduccion());
            dpFechaCaducidad.setValue(lote.getFechaCaducidad());
            txtCantidad.setText(lote.getCantidadInicial() != null ? lote.getCantidadInicial().toString() : "");
            txtOrigen.setText(lote.getOrigen());
            txtRegistroSanitario.setText(lote.getNumeroRegistroSanitario());
            txtObservaciones.setText(lote.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (lote == null) lote = new Lote();
            lote.setCodigo(txtCodigo.getText());
            lote.setFechaProduccion(dpFechaProduccion.getValue());
            lote.setFechaCaducidad(dpFechaCaducidad.getValue());
            lote.setCantidadInicial(parseDecimal(txtCantidad.getText()));
            lote.setOrigen(txtOrigen.getText());
            lote.setNumeroRegistroSanitario(txtRegistroSanitario.getText());
            lote.setObservaciones(txtObservaciones.getText());

            loteService.save(lote);
            mostrarExito("Lote guardado");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando lote", e);
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
        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.close();
    }
}
