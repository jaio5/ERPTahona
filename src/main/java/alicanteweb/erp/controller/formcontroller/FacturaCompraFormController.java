package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import alicanteweb.erp.ui.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para el formulario de Factura de Compra
 */
@Controller
public class FacturaCompraFormController {
    private static final Logger log = LoggerFactory.getLogger(FacturaCompraFormController.class);

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Proveedor> cbProveedor; // coincide con FXML
    @FXML private TextField txtNumFacturaProveedor; // campo del FXML
    @FXML private TextField txtTotal;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardarFactura;

    // Líneas
    @FXML private TableView<FacturaCompraLinea> tableLineas;
    @FXML private TableColumn<FacturaCompraLinea, String> colArticulo;
    @FXML private TableColumn<FacturaCompraLinea, BigDecimal> colCantidad;
    @FXML private TableColumn<FacturaCompraLinea, BigDecimal> colPrecio;

    private final FacturaCompraService facturaCompraService;
    private final ProveedorService proveedorService;
    private FacturaCompra facturaCompra;

    private final ObservableList<FacturaCompraLinea> lineas = FXCollections.observableArrayList();

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
        // asegurar que el área de observaciones se inicializa para evitar advertencias estáticas
        if (txtObservaciones != null) {
            txtObservaciones.setPromptText("Notas internas sobre la factura...");
        } else {
            // si FXML no inyectó (escenario de análisis estático), creamos uno temporal
            txtObservaciones = new TextArea();
            txtObservaciones.setPromptText("Notas internas sobre la factura...");
        }
        cargarProveedores();
        configurarTablaLineas();
        configurarCalculoAutomatico();

        // Bind del botón guardar: habilitar solo si hay proveedor seleccionado y al menos una línea
        try {
            if (btnGuardarFactura != null) {
                btnGuardarFactura.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(() ->
                        cbProveedor == null || cbProveedor.getValue() == null || lineas.isEmpty(),
                        cbProveedor == null ? null : cbProveedor.valueProperty(), lineas
                    )
                );
            }
        } catch (Exception e) {
            log.debug("No se pudo bindear btnGuardarFactura: {}", e.getMessage());
        }
    }

    private void cargarProveedores() {
        try {
            if (cbProveedor != null) {
                List<Proveedor> proveedores = proveedorService.findAll();
                cbProveedor.getItems().addAll(proveedores);
            }
        } catch (Exception e) {
            log.error("Error cargando proveedores", e);
        }
    }

    private void configurarTablaLineas() {
        if (tableLineas != null) {
            tableLineas.setItems(lineas);
        }
        if (colArticulo != null) {
            colArticulo.setCellValueFactory(cell -> {
                var linea = cell.getValue();
                String desc = "";
                if (linea != null) {
                    if (linea.getArticulo() != null) desc = linea.getArticulo().getNombre();
                    else if (linea.getDescripcion() != null) desc = linea.getDescripcion();
                }
                return new javafx.beans.property.SimpleStringProperty(desc);
            });
        }
        if (colCantidad != null) {
            colCantidad.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cantidad"));
        }
        if (colPrecio != null) {
            colPrecio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("precioUnitario"));
        }
    }

    private void configurarCalculoAutomatico() {
        // recalcular total cuando cambian las líneas
        lineas.addListener((javafx.collections.ListChangeListener<FacturaCompraLinea>) c -> calcularTotal());
        // listener para selección y acciones en tablas de albaranes si es necesario
    }

    private void calcularTotal() {
        try {
            BigDecimal sumaBase = BigDecimal.ZERO;
            BigDecimal sumaIva = BigDecimal.ZERO;
            for (FacturaCompraLinea l : lineas) {
                if (l == null) continue;
                BigDecimal importe = l.getImporte() != null ? l.getImporte() : BigDecimal.ZERO;
                BigDecimal tipoIva = l.getTipoIva() != null ? l.getTipoIva() : BigDecimal.ZERO;
                sumaBase = sumaBase.add(importe);
                BigDecimal ivaLinea = importe.multiply(tipoIva).divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP);
                sumaIva = sumaIva.add(ivaLinea);
            }
            BigDecimal total = sumaBase.add(sumaIva);
            if (txtTotal != null) txtTotal.setText(String.format("%.2f", total));
        } catch (Exception e) {
            log.warn("Error calculando total", e);
        }
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
        cargarDatos();
    }

    private void cargarDatos() {
        if (facturaCompra != null) {
            if (txtNumero != null) txtNumero.setText(facturaCompra.getNumero());
            if (dpFecha != null && facturaCompra.getFecha() != null) dpFecha.setValue(facturaCompra.getFecha());
            if (cbProveedor != null && facturaCompra.getProveedor() != null) cbProveedor.setValue(facturaCompra.getProveedor());
            if (txtNumFacturaProveedor != null && facturaCompra.getNumeroSerie() != null) txtNumFacturaProveedor.setText(facturaCompra.getNumeroSerie());

            // Cargar líneas
            if (facturaCompra.getLineas() != null) {
                lineas.clear();
                lineas.addAll(facturaCompra.getLineas());
            }

            if (txtObservaciones != null) txtObservaciones.setText(facturaCompra.getObservaciones());
            calcularTotal();
        }
    }

    @FXML
    public void onAgregarLinea() {
        // Crear línea vacía y añadir
        FacturaCompraLinea linea = new FacturaCompraLinea();
        linea.setCantidad(java.math.BigDecimal.ONE);
        linea.setPrecioUnitario(java.math.BigDecimal.ZERO);
        linea.calcularImporte();
        lineas.add(linea);
        // seleccionar la nueva línea
        if (tableLineas != null) tableLineas.getSelectionModel().select(linea);
    }

    @FXML
    public void onEliminarLinea() {
        var sel = tableLineas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            DialogUtils.showError("Selecciona una línea para eliminar");
            return;
        }
        lineas.remove(sel);
    }

    @FXML
    public void onGuardar() {
        try {
            if (!validarFormulario()) return;

            if (facturaCompra == null) facturaCompra = new FacturaCompra();

            facturaCompra.setNumero(txtNumero != null ? txtNumero.getText().trim() : null);
            facturaCompra.setFecha(dpFecha != null ? dpFecha.getValue() : LocalDate.now());
            facturaCompra.setProveedor(cbProveedor != null ? cbProveedor.getValue() : null);
            facturaCompra.setNumeroSerie(txtNumFacturaProveedor != null ? txtNumFacturaProveedor.getText().trim() : null);

            // Asignar líneas
            facturaCompra.getLineas().clear();
            facturaCompra.getLineas().addAll(lineas);

            // Calcular importes
            BigDecimal sumaBase = BigDecimal.ZERO;
            BigDecimal sumaIva = BigDecimal.ZERO;
            for (FacturaCompraLinea l : lineas) {
                if (l == null) continue;
                l.calcularImporte();
                BigDecimal importe = l.getImporte() != null ? l.getImporte() : BigDecimal.ZERO;
                BigDecimal tipoIva = l.getTipoIva() != null ? l.getTipoIva() : BigDecimal.ZERO;
                sumaBase = sumaBase.add(importe);
                BigDecimal ivaLinea = importe.multiply(tipoIva).divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP);
                sumaIva = sumaIva.add(ivaLinea);
            }
            facturaCompra.setBaseImponible(sumaBase);
            facturaCompra.setImporteIva(sumaIva);
            facturaCompra.setTotal(sumaBase.add(sumaIva));

            facturaCompra.setObservaciones(txtObservaciones != null ? txtObservaciones.getText() : null);

            facturaCompraService.guardar(facturaCompra);
            DialogUtils.showSuccess("Factura de compra guardada correctamente");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando factura de compra", e);
            DialogUtils.showError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onRegistrar() {
        // Guardar y marcar como registrada/contabilizada si aplica
        onGuardar();
        // Aquí se podría cambiar el estado o realizar acciones adicionales
        DialogUtils.showSuccess("Factura registrada");
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (cbProveedor == null || cbProveedor.getValue() == null) {
            DialogUtils.showError("Debe seleccionar un proveedor");
            return false;
        }
        // Permitimos que el número se autogenere en el servicio si está vacío
        return true;
    }

    /**
     * Alias usado por BaseController (reflexión) para inyectar el item en el formulario
     */
    public void setFactura(FacturaCompra factura) {
        setFacturaCompra(factura);
    }

    private void cerrarVentana() {
        if (txtNumero != null && txtNumero.getScene() != null) {
            Stage stage = (Stage) txtNumero.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

}
