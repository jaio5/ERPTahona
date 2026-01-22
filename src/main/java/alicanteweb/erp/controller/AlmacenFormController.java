package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
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
public class AlmacenFormController {
    private static final Logger log = LoggerFactory.getLogger(AlmacenFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtDisponible;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtResponsable;

    private final AlmacenService almacenService;
    private Almacen almacen;

    public AlmacenFormController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @FXML
    public void initialize() {
        // nothing
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
        if (almacen != null) {
            txtCodigo.setText(almacen.getCodigo());
            txtNombre.setText(almacen.getNombre());
            txtDescripcion.setText(almacen.getDescripcion());
            txtCapacidad.setText(almacen.getCapacidad() != null ? almacen.getCapacidad().toString() : "");
            txtDisponible.setText(almacen.getDisponible() != null ? almacen.getDisponible().toString() : "");
            txtLocalidad.setText(almacen.getLocalidad());
            txtResponsable.setText(almacen.getResponsable());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (almacen == null) almacen = new Almacen();
            almacen.setCodigo(txtCodigo.getText());
            almacen.setNombre(txtNombre.getText());
            almacen.setDescripcion(txtDescripcion.getText());
            almacen.setCapacidad(parseDecimal(txtCapacidad.getText()));
            almacen.setDisponible(parseDecimal(txtDisponible.getText()));
            almacen.setLocalidad(txtLocalidad.getText());
            almacen.setResponsable(txtResponsable.getText());

            // Validaciones simples
            if (almacen.getCapacidad() != null && almacen.getCapacidad().signum() < 0) throw new IllegalArgumentException("capacidad negativa");
            if (almacen.getDisponible() != null && almacen.getDisponible().signum() < 0) throw new IllegalArgumentException("disponible negativa");
            if (almacen.getCapacidad() != null && almacen.getDisponible() != null && almacen.getDisponible().compareTo(almacen.getCapacidad()) > 0)
                throw new IllegalArgumentException("disponible mayor que capacidad");

            almacenService.save(almacen);
            String ok = "Almacén guardado";
            mostrarExito(ok);
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando almacen", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (Exception e) { return null; }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Éxito");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.close();
    }
}
