package alicanteweb.erp.controller;

import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.FacturaCompraService;
import alicanteweb.erp.service.ProveedorService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controlador para el formulario de Factura de Compra
 */
@Controller
public class FacturaCompraFormController {
    private static final Logger log = LoggerFactory.getLogger(FacturaCompraFormController.class);

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private TextField txtBase;
    @FXML private TextField txtIva;
    @FXML private TextField txtTotal;
    @FXML private TextArea txtObservaciones;

    private final FacturaCompraService facturaCompraService;
    private final ProveedorService proveedorService;
    private FacturaCompra facturaCompra;

    public FacturaCompraFormController(FacturaCompraService facturaCompraService,
                                       ProveedorService proveedorService) {
        this.facturaCompraService = facturaCompraService;
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando FacturaCompraFormController");
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
        }
        cargarProveedores();
        configurarCalculoAutomatico();
    }

    private void cargarProveedores() {
        try {
            if (cmbProveedor != null) {
                cmbProveedor.getItems().addAll(proveedorService.findAll());
            }
        } catch (Exception e) {
            log.error("Error cargando proveedores", e);
        }
    }

    private void configurarCalculoAutomatico() {
        if (txtBase != null && txtIva != null && txtTotal != null) {
            txtBase.textProperty().addListener((obs, old, newVal) -> calcularTotal());
            txtIva.textProperty().addListener((obs, old, newVal) -> calcularTotal());
        }
    }

    private void calcularTotal() {
        try {
            BigDecimal base = parseBigDecimal(txtBase.getText());
            BigDecimal iva = parseBigDecimal(txtIva.getText());
            BigDecimal total = base.add(iva);
            txtTotal.setText(String.format("%.2f", total));
        } catch (Exception e) {
            // Ignorar errores de formato durante la edicion
        }
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.trim().isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(text.replace(",", ".").trim());
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
        cargarDatos();
    }

    private void cargarDatos() {
        if (facturaCompra != null) {
            if (txtNumero != null) txtNumero.setText(facturaCompra.getNumero());
            if (dpFecha != null && facturaCompra.getFecha() != null) dpFecha.setValue(facturaCompra.getFecha());
            if (cmbProveedor != null && facturaCompra.getProveedor() != null) cmbProveedor.setValue(facturaCompra.getProveedor());
            if (txtBase != null && facturaCompra.getBaseImponible() != null) txtBase.setText(facturaCompra.getBaseImponible().toString());
            if (txtIva != null && facturaCompra.getImporteIva() != null) txtIva.setText(facturaCompra.getImporteIva().toString());
            if (txtTotal != null && facturaCompra.getTotal() != null) txtTotal.setText(facturaCompra.getTotal().toString());
            if (txtObservaciones != null) txtObservaciones.setText(facturaCompra.getObservaciones());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            if (facturaCompra == null) {
                facturaCompra = new FacturaCompra();
            }

            facturaCompra.setNumero(txtNumero.getText().trim());
            facturaCompra.setFecha(dpFecha.getValue());
            facturaCompra.setProveedor(cmbProveedor.getValue());
            facturaCompra.setBaseImponible(parseBigDecimal(txtBase.getText()));
            facturaCompra.setImporteIva(parseBigDecimal(txtIva.getText()));
            facturaCompra.setTotal(parseBigDecimal(txtTotal.getText()));
            facturaCompra.setObservaciones(txtObservaciones != null ? txtObservaciones.getText() : null);

            facturaCompraService.guardar(facturaCompra);

            mostrarExito("Factura de compra guardada correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando factura de compra", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (txtNumero == null || txtNumero.getText().trim().isEmpty()) {
            mostrarError("El numero de factura es obligatorio");
            return false;
        }
        if (cmbProveedor == null || cmbProveedor.getValue() == null) {
            mostrarError("Debe seleccionar un proveedor");
            return false;
        }
        if (dpFecha == null || dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        if (txtNumero != null && txtNumero.getScene() != null) {
            Stage stage = (Stage) txtNumero.getScene().getWindow();
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
}

