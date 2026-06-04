package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.RutaReparto;
import alicanteweb.erp.service.RutaRepartoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class RutaRepartoFormController {
    private static final Logger log = LoggerFactory.getLogger(RutaRepartoFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtConductor;
    @FXML private TextField txtDistanciaKm;
    @FXML private TextField txtTiempoMinutos;

    private final RutaRepartoService rutaService;
    private RutaReparto ruta;

    public RutaRepartoFormController(RutaRepartoService rutaService) {
        this.rutaService = rutaService;
    }

    @FXML
    public void initialize() {}

    public void setRutaReparto(RutaReparto ruta) {
        this.ruta = ruta;
        if (ruta != null) {
            txtCodigo.setText(ruta.getCodigo());
            txtNombre.setText(ruta.getNombre());
            txtDescripcion.setText(ruta.getDescripcion());
            txtConductor.setText(ruta.getConductor());
            txtDistanciaKm.setText(ruta.getDistanciaTotalKm() != null ? ruta.getDistanciaTotalKm().toString() : "");
            txtTiempoMinutos.setText(ruta.getTiempoEstimadoMinutos() != null ? ruta.getTiempoEstimadoMinutos().toString() : "");
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (ruta == null) ruta = new RutaReparto();
            ruta.setCodigo(txtCodigo.getText());
            ruta.setNombre(txtNombre.getText());
            ruta.setDescripcion(txtDescripcion.getText());
            ruta.setConductor(txtConductor.getText());
            ruta.setDistanciaTotalKm(parseDecimal(txtDistanciaKm.getText()));
            ruta.setTiempoEstimadoMinutos(parseInt(txtTiempoMinutos.getText()));

            rutaService.save(ruta);
            mostrarExito("Ruta guardada");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando ruta", e);
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
