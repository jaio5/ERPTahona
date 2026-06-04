package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.OrdenProduccion;
import alicanteweb.erp.service.OrdenProduccionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class OrdenProduccionFormController {
    private static final Logger log = LoggerFactory.getLogger(OrdenProduccionFormController.class);

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtObservaciones;

    private final OrdenProduccionService ordenProduccionService;
    private OrdenProduccion orden;

    public OrdenProduccionFormController(OrdenProduccionService ordenProduccionService) {
        this.ordenProduccionService = ordenProduccionService;
    }

    @FXML
    public void initialize() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
    }

    public void setOrdenProduccion(OrdenProduccion orden) {
        this.orden = orden;
        if (orden != null) {
            txtNumero.setText(orden.getNumero());
            dpFecha.setValue(orden.getFecha());
            txtCantidad.setText(orden.getCantidadPlanificada() != null ? orden.getCantidadPlanificada().toString() : "");
            txtObservaciones.setText(orden.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (orden == null) orden = new OrdenProduccion();
            if (orden.getNumero() == null || orden.getNumero().isBlank()) {
                orden.setNumero(ordenProduccionService.generarNumero());
            } else {
                orden.setNumero(txtNumero.getText());
            }
            orden.setFecha(dpFecha.getValue());
            orden.setCantidadPlanificada(parseDecimal(txtCantidad.getText()));
            orden.setObservaciones(txtObservaciones.getText());
            if (orden.getEstado() == null) orden.setEstado("PLANIFICADA");

            ordenProduccionService.save(orden);
            mostrarExito("Orden guardada");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando orden", e);
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
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }
}
