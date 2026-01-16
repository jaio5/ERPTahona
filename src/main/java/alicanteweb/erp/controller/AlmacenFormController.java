package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

/**
 * Controlador para el formulario de creación/edición de almacenes
 */
@Controller
public class AlmacenFormController {
    private static final Logger log = LoggerFactory.getLogger(AlmacenFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private Button btnCancelar;

    // Campos adicionales que aparecen en FXML — añadidos para alinear controller con vista
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtDisponible;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtResponsable;
    @FXML private CheckBox chkActivo;

    private final AlmacenService almacenService;
    private Almacen almacenActual;

    public AlmacenFormController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @FXML
    public void initialize() {
        log.info("AlmacenFormController inicializado");
    }


    public void setAlmacen(Almacen almacen) {
        this.almacenActual = almacen;
        if (almacen != null) {
            txtCodigo.setText(almacen.getCodigo());
            txtNombre.setText(almacen.getNombre());
            if (txtDescripcion != null) txtDescripcion.setText(almacen.getDescripcion() != null ? almacen.getDescripcion() : "");
            if (txtCapacidad != null) txtCapacidad.setText(almacen.getCapacidad() != null ? almacen.getCapacidad().toPlainString() : "");
            if (txtDisponible != null) txtDisponible.setText(almacen.getDisponible() != null ? almacen.getDisponible().toPlainString() : "");
            if (txtLocalidad != null) txtLocalidad.setText(almacen.getLocalidad() != null ? almacen.getLocalidad() : "");
            if (txtResponsable != null) txtResponsable.setText(almacen.getResponsable() != null ? almacen.getResponsable() : "");
            if (chkActivo != null) chkActivo.setSelected(almacen.getActivo() != null ? almacen.getActivo() : true);
        }
    }

    @FXML
    public void onGuardar() {
        if (validar()) {
            try {
                if (almacenActual == null) {
                    almacenActual = new Almacen();
                }

                almacenActual.setCodigo(txtCodigo.getText());
                almacenActual.setNombre(txtNombre.getText());
                almacenActual.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);

                // Parsear capacidad y disponible de manera segura
                if (txtCapacidad != null && !txtCapacidad.getText().isBlank()) {
                    try {
                        almacenActual.setCapacidad(new BigDecimal(txtCapacidad.getText().trim()));
                    } catch (NumberFormatException nfe) {
                        mostrarError("Capacidad no es un número válido");
                        return;
                    }
                } else {
                    almacenActual.setCapacidad(null);
                }

                if (txtDisponible != null && !txtDisponible.getText().isBlank()) {
                    try {
                        almacenActual.setDisponible(new BigDecimal(txtDisponible.getText().trim()));
                    } catch (NumberFormatException nfe) {
                        mostrarError("Disponible no es un número válido");
                        return;
                    }
                } else {
                    almacenActual.setDisponible(null);
                }

                // Validación: disponible no puede ser mayor que capacidad
                if (almacenActual.getCapacidad() != null && almacenActual.getDisponible() != null) {
                    if (almacenActual.getDisponible().compareTo(almacenActual.getCapacidad()) > 0) {
                        mostrarError("El espacio disponible no puede ser mayor que la capacidad total");
                        return;
                    }
                }

                almacenActual.setLocalidad(txtLocalidad != null ? txtLocalidad.getText() : null);
                almacenActual.setResponsable(txtResponsable != null ? txtResponsable.getText() : null);

                // Usar el valor del checkbox si existe en la UI
                if (chkActivo != null) {
                    almacenActual.setActivo(chkActivo.isSelected());
                } else {
                    almacenActual.setActivo(true);
                }

                almacenService.save(almacenActual);
                log.info("Almacén guardado: {}", almacenActual.getId());

                mostrarExito();

                btnCancelar.getScene().getWindow().hide();
            } catch (Exception e) {
                log.error("Error guardando almacén", e);
                mostrarError("Error al guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onCancelar() {
        btnCancelar.getScene().getWindow().hide();
    }

    private boolean validar() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            mostrarAlerta();
            return false;
        }
        return true;
    }

    private void mostrarAlerta() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText("Código y nombre son obligatorios");
        alert.showAndWait();
    }

    private void mostrarExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText("Almacén guardado correctamente");
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
