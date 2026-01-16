package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.service.AsientoContableService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Controlador para el formulario de Asiento Contable
 */
@Controller
public class AsientoFormController {
    private static final Logger log = LoggerFactory.getLogger(AsientoFormController.class);

    @FXML private Label lblTitulo; // declarado en FXML
    @FXML private Label lblBalance; // declarado en FXML

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtConcepto;
    @FXML private TextArea txtDescripcion;

    // Campos de la tabla de apuntes (no obligatorios aquí si no se manejan aún)
    @FXML private TableView<?> tableApuntes;
    @FXML private TableColumn<?, ?> colCuenta;
    @FXML private TableColumn<?, ?> colNombreCuenta;
    @FXML private TableColumn<?, ?> colConcepto;
    @FXML private TableColumn<?, ?> colDebe;
    @FXML private TableColumn<?, ?> colHaber;
    @FXML private TableColumn<?, ?> colAcciones;

    @FXML private Label lblTotalDebe;
    @FXML private Label lblTotalHaber;
    @FXML private Label lblDiferencia;

    private final AsientoContableService asientoContableService;
    private AsientoContable asientoContable;

    public AsientoFormController(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AsientoFormController");
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
        }
    }

    public void setAsientoContable(AsientoContable asientoContable) {
        this.asientoContable = asientoContable;
        cargarDatos();
    }

    private void cargarDatos() {
        if (asientoContable != null) {
            if (txtNumero != null) txtNumero.setText(asientoContable.getNumero());
            if (dpFecha != null && asientoContable.getFecha() != null) dpFecha.setValue(asientoContable.getFecha());
            if (txtConcepto != null) txtConcepto.setText(asientoContable.getConcepto());
            if (txtDescripcion != null) txtDescripcion.setText(asientoContable.getDescripcion());

            // Actualizar labels de totales si es necesario
            if (lblTotalDebe != null) lblTotalDebe.setText(String.format("Total Debe: %.2f €", asientoContable.getDebe() != null ? asientoContable.getDebe() : 0.0));
            if (lblTotalHaber != null) lblTotalHaber.setText(String.format("Total Haber: %.2f €", asientoContable.getHaber() != null ? asientoContable.getHaber() : 0.0));
            if (lblDiferencia != null) lblDiferencia.setText(String.format("Diferencia: %.2f €", asientoContable.getDescuadre() != null ? asientoContable.getDescuadre() : 0.0));

            if (lblTitulo != null) lblTitulo.setText(asientoContable.getId() != null ? "✏️ Editar Asiento Contable" : "✏️ Nuevo Asiento Contable");
        } else {
            if (lblTitulo != null) lblTitulo.setText("✏️ Nuevo Asiento Contable");
        }
    }

    @FXML
    public void onAgregarApunte() {
        // Placeholder: abrir diálogo para añadir apunte. Por ahora, mostrar alerta.
        mostrarAlerta("Función en desarrollo: Añadir apunte contable (abrir mini-form)");
    }

    @FXML
    public void onGuardarBorrador() {
        try {
            if (asientoContable == null) {
                asientoContable = new AsientoContable();
            }

            // Rellenar campos básicos
            asientoContable.setNumero(txtNumero != null ? txtNumero.getText().trim() : null);
            asientoContable.setFecha(dpFecha != null ? dpFecha.getValue() : null);
            asientoContable.setConcepto(txtConcepto != null ? txtConcepto.getText() : null);
            asientoContable.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);

            asientoContableService.save(asientoContable);
            mostrarExito("Asiento guardado como borrador");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando borrador de asiento contable", e);
            mostrarError("Error al guardar borrador: " + e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            if (asientoContable == null) {
                asientoContable = new AsientoContable();
            }

            asientoContable.setNumero(txtNumero != null ? txtNumero.getText().trim() : null);
            asientoContable.setFecha(dpFecha.getValue());
            asientoContable.setConcepto(txtConcepto != null ? txtConcepto.getText() : null);
            asientoContable.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : null);

            asientoContableService.save(asientoContable);

            mostrarExito("Asiento contable guardado correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando asiento contable", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (dpFecha == null || dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        if (dpFecha != null && dpFecha.getScene() != null) {
            Stage stage = (Stage) dpFecha.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
