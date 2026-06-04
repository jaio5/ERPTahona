package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Vehiculo;
import alicanteweb.erp.service.VehiculoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class VehiculoFormController {
    private static final Logger log = LoggerFactory.getLogger(VehiculoFormController.class);

    @FXML private TextField txtMatricula;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtConsumo;
    @FXML private TextArea txtObservaciones;

    private final VehiculoService vehiculoService;
    private Vehiculo vehiculo;

    public VehiculoFormController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @FXML
    public void initialize() {
        if (cmbTipo != null) {
            cmbTipo.getItems().addAll("FURGONETA", "CAMION", "TURISMO", "MOTO", "BICICLETA", "OTRO");
        }
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
        if (vehiculo != null) {
            txtMatricula.setText(vehiculo.getMatricula());
            txtMarca.setText(vehiculo.getMarca());
            txtModelo.setText(vehiculo.getModelo());
            cmbTipo.setValue(vehiculo.getTipo());
            txtCapacidad.setText(vehiculo.getCapacidadKg() != null ? vehiculo.getCapacidadKg().toString() : "");
            txtConsumo.setText(vehiculo.getConsumoMedio() != null ? vehiculo.getConsumoMedio().toString() : "");
            txtObservaciones.setText(vehiculo.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (vehiculo == null) vehiculo = new Vehiculo();
            vehiculo.setMatricula(txtMatricula.getText());
            vehiculo.setMarca(txtMarca.getText());
            vehiculo.setModelo(txtModelo.getText());
            vehiculo.setTipo(cmbTipo.getValue());
            vehiculo.setCapacidadKg(parseDecimal(txtCapacidad.getText()));
            vehiculo.setConsumoMedio(parseDecimal(txtConsumo.getText()));
            vehiculo.setObservaciones(txtObservaciones.getText());

            vehiculoService.save(vehiculo);
            mostrarExito("Vehículo guardado");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando vehículo", e);
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
        Stage stage = (Stage) txtMatricula.getScene().getWindow();
        stage.close();
    }
}
