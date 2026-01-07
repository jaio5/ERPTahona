package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Controlador para el formulario de creación/edición de almacenes
 */
@Controller
public class AlmacenFormController {
    private static final Logger log = LoggerFactory.getLogger(AlmacenFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

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
                almacenActual.setActivo(true);

                almacenService.save(almacenActual);
                log.info("Almacén guardado: {}", almacenActual.getId());

                mostrarExito("Almacén guardado correctamente");

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
            mostrarAlerta("Código y nombre son obligatorios");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

