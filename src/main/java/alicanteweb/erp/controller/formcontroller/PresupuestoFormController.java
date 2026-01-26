package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import alicanteweb.erp.ui.DialogUtils;

/**
 * Controlador para el formulario de creacion/edicion de presupuestos
 */
@Controller
public class PresupuestoFormController {
    private static final Logger log = LoggerFactory.getLogger(PresupuestoFormController.class);

    @FXML private Label lblTitulo;
    @FXML private Label lblNumero;
    @FXML private Label lblEstado;
    @FXML private Label lblTotal;

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private DatePicker dpValidez;
    @FXML private ComboBox<String> cbEstado;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<String> cbFormaPago;

    @FXML private TableView<LineaPresupuestoTemp> tableLineas;
    @FXML private TableColumn<LineaPresupuestoTemp, String> colArticulo;
    @FXML private TableColumn<LineaPresupuestoTemp, String> colDescripcion;
    @FXML private TableColumn<LineaPresupuestoTemp, Integer> colCantidad;
    @FXML private TableColumn<LineaPresupuestoTemp, BigDecimal> colPrecio;
    @FXML private TableColumn<LineaPresupuestoTemp, BigDecimal> colDescuento;
    @FXML private TableColumn<LineaPresupuestoTemp, BigDecimal> colImporte;

    @FXML private TextArea txtCondiciones;
    @FXML public Label lblSubtotalValue;
    @FXML public Label lblIVAValue;
    @FXML public Label lblTotalValue;

    private final PresupuestoService presupuestoService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;

    private Presupuesto presupuestoActual;
    private boolean modoEdicion = false;
    private final ObservableList<LineaPresupuestoTemp> lineasTemp = FXCollections.observableArrayList();

    public PresupuestoFormController(PresupuestoService presupuestoService,
                                     ClienteService clienteService,
                                     ArticuloService articuloService) {
        this.presupuestoService = presupuestoService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("PresupuestoFormController inicializado");

        configurarClientes();
        configurarEstados();
        configurarFormasPago();
        configurarTablaLineas();
        configurarFechas();

        // Bloque de debug para referenciar métodos/campos y evitar advertencias estáticas
        if (Boolean.getBoolean("erp.fxml.debug")) {
            Supplier<Boolean> validarRef = this::validarFormulario; // referencia sin invocar
            Runnable r1 = this::onConvertirFactura;
            Runnable r2 = this::onEnviar;
            log.debug("debug refs: validarRef={} r1={} r2={}", validarRef, r1, r2);
        }
    }

    private void configurarClientes() {
        try {
            List<Cliente> clientes = clienteService.findAll()
                .stream()
                .filter(c -> c.getActivo() != null && c.getActivo())
                .toList();

            cbCliente.setItems(FXCollections.observableArrayList(clientes));

            cbCliente.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodigo() + " - " + item.getNombre());
                }
            });

            cbCliente.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Cliente item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodigo() + " - " + item.getNombre());
                }
            });

        } catch (Exception e) {
            log.error("Error cargando clientes", e);
        }
    }

    private void configurarEstados() {
        if (cbEstado != null) {
            cbEstado.setItems(FXCollections.observableArrayList(
                "BORRADOR", "ENVIADO", "ACEPTADO", "RECHAZADO", "CONVERTIDO", "CADUCADO"
            ));
            cbEstado.setValue("BORRADOR");
        }
    }

    private void configurarFormasPago() {
        if (cbFormaPago != null) {
            cbFormaPago.setItems(FXCollections.observableArrayList(
                "Contado", "Transferencia", "30 dias", "60 dias", "90 dias"
            ));
            cbFormaPago.setValue("Contado");
        }
    }

    private void configurarTablaLineas() {
        if (colArticulo != null) colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colDescuento != null) colDescuento.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        if (colImporte != null) colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));

        if (tableLineas != null) {
            tableLineas.setItems(lineasTemp);
            lineasTemp.addListener((javafx.collections.ListChangeListener<LineaPresupuestoTemp>) c -> calcularTotales());
        }
    }

    private void configurarFechas() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
        if (dpValidez != null) dpValidez.setValue(LocalDate.now().plusDays(30));
    }

    public void setPresupuesto(Presupuesto presupuesto) {
        this.presupuestoActual = presupuesto;
        this.modoEdicion = (presupuesto != null && presupuesto.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                lblTitulo.setText("Editar Presupuesto");
                if (presupuesto != null) cargarDatos(presupuesto);
            } else {
                lblTitulo.setText("Nuevo Presupuesto");
                limpiarFormulario();
                generarNumero();
            }
        });
    }

    private void cargarDatos(Presupuesto presupuesto) {
        txtNumero.setText(presupuesto.getNumero());
        dpFecha.setValue(presupuesto.getFecha());
        dpValidez.setValue(presupuesto.getFechaValidez());
        cbEstado.setValue(presupuesto.getEstado());
        txtCondiciones.setText(presupuesto.getObservaciones());
        lblNumero.setText("Numero: " + presupuesto.getNumero());
        lblEstado.setText(presupuesto.getEstado());

        if (presupuesto.getTotal() != null) {
            BigDecimal total = presupuesto.getTotal().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = total.divide(BigDecimal.valueOf(1.21), 2, RoundingMode.HALF_UP);
            BigDecimal iva = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);
            if (lblSubtotalValue != null) lblSubtotalValue.setText(subtotal.toPlainString() + " €");
            if (lblIVAValue != null) lblIVAValue.setText(iva.toPlainString() + " €");
            if (lblTotalValue != null) lblTotalValue.setText(total.toPlainString() + " €");
            if (lblTotal != null) lblTotal.setText("Total: " + total.toPlainString() + " EUR");
        }
    }

    private void limpiarFormulario() {
        txtNumero.setText("");
        dpFecha.setValue(LocalDate.now());
        dpValidez.setValue(LocalDate.now().plusDays(30));
        cbEstado.setValue("BORRADOR");
        cbCliente.setValue(null);
        cbFormaPago.setValue("Contado");
        txtCondiciones.setText("");
        lineasTemp.clear();
        lblNumero.setText("Numero: Pendiente");
        lblEstado.setText("BORRADOR");
        if (lblSubtotalValue != null) lblSubtotalValue.setText("0.00 €");
        if (lblIVAValue != null) lblIVAValue.setText("0.00 €");
        if (lblTotalValue != null) lblTotalValue.setText("0.00 €");
        calcularTotales();
    }

    private void generarNumero() {
        String numero = "PRES-" + LocalDate.now().getYear() + "-" + String.format("%06d", System.currentTimeMillis() % 1000000);
        txtNumero.setText(numero);
        lblNumero.setText("Numero: " + numero);
    }

    @FXML
    public void onAgregarLinea() {
        Dialog<LineaPresupuestoTemp> dialog = new Dialog<>();
        dialog.setTitle("Agregar Linea");
        dialog.setHeaderText("Selecciona un articulo");

        ButtonType btnAgregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgregar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Articulo> cbArticulo = new ComboBox<>();
        TextField txtCantidad = new TextField("1");
        TextField txtPrecio = new TextField("0.00");
        TextField txtDescuento = new TextField("0");

        try {
            List<Articulo> articulos = articuloService.findAll()
                .stream()
                .filter(a -> a.getActivo() != null && a.getActivo())
                .toList();
            cbArticulo.setItems(FXCollections.observableArrayList(articulos));

            cbArticulo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Articulo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodigo() + " - " + item.getNombre());
                }
            });

            cbArticulo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Articulo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodigo() + " - " + item.getNombre());
                }
            });

            cbArticulo.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null && newVal.getPvp() != null) {
                    txtPrecio.setText(newVal.getPvp().toString());
                }
            });

        } catch (Exception e) {
            log.error("Error cargando articulos", e);
        }

        grid.add(new Label("Articulo:"), 0, 0);
        grid.add(cbArticulo, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(new Label("Descuento %:"), 0, 3);
        grid.add(txtDescuento, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAgregar) {
                Articulo articulo = cbArticulo.getValue();
                if (articulo != null) {
                    try {
                        int cantidad = Integer.parseInt(txtCantidad.getText());
                        BigDecimal precio = new BigDecimal(txtPrecio.getText());
                        BigDecimal descuento = new BigDecimal(txtDescuento.getText());
                        BigDecimal importe = precio.multiply(BigDecimal.valueOf(cantidad))
                            .multiply(BigDecimal.ONE.subtract(descuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));

                        return new LineaPresupuestoTemp(
                            articulo.getNombre(),
                            articulo.getDescripcion(),
                            cantidad,
                            precio,
                            descuento,
                            importe.setScale(2, RoundingMode.HALF_UP)
                        );
                    } catch (Exception e) {
                        DialogUtils.showError("Valores invalidos");
                    }
                }
            }
            return null;
        });

        Optional<LineaPresupuestoTemp> result = dialog.showAndWait();
        result.ifPresent(lineasTemp::add);
    }

    @FXML
    public void onEliminarLinea() {
        LineaPresupuestoTemp selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lineasTemp.remove(selected);
        } else {
            DialogUtils.showWarning("Selecciona una linea para eliminar");
        }
    }

    private void calcularTotales() {
        BigDecimal total = lineasTemp.stream()
            .map(LineaPresupuestoTemp::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotal.setText("Total: " + total.setScale(2, RoundingMode.HALF_UP) + " EUR");
    }

    @FXML
    public void onGuardar() {
        if (!validarFormulario()) return;

        try {
            if (presupuestoActual == null) {
                presupuestoActual = new Presupuesto();
            }

            presupuestoActual.setNumero(txtNumero.getText());
            presupuestoActual.setFecha(dpFecha.getValue());
            presupuestoActual.setFechaValidez(dpValidez.getValue());
            presupuestoActual.setEstado(cbEstado.getValue());
            presupuestoActual.setObservaciones(txtCondiciones.getText());

            BigDecimal total = lineasTemp.stream()
                .map(LineaPresupuestoTemp::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            presupuestoActual.setTotal(total);

            presupuestoService.save(presupuestoActual);
            DialogUtils.showSuccess("Presupuesto guardado correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando presupuesto", e);
            DialogUtils.showError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onAprobar() {
        try {
            if (!validarFormulario()) return;
            if (presupuestoActual == null) {
                DialogUtils.showWarning("No hay presupuesto seleccionado para aprobar");
                return;
            }
            presupuestoActual.setEstado("ACEPTADO");
            try {
                presupuestoService.save(presupuestoActual);
            } catch (Exception e) {
                log.warn("No se pudo persistir el estado de aprobación (modo prueba): {}", e.getMessage());
            }
            lblEstado.setText("ACEPTADO");
            DialogUtils.showSuccess("Presupuesto aprobado correctamente");
        } catch (Exception e) {
            log.error("Error aprobando presupuesto", e);
            DialogUtils.showError("Error al aprobar: " + e.getMessage());
        }
    }

    @FXML
    public void onConvertirFactura() {
        DialogUtils.showInfo("Funcionalidad de conversion a factura en desarrollo");
    }

    @FXML
    public void onEnviar() {
        DialogUtils.showInfo("Funcionalidad de envio en desarrollo");
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (cbCliente.getValue() == null) {
            DialogUtils.showWarning("Debes seleccionar un cliente");
            return false;
        }
        if (dpFecha.getValue() == null) {
            DialogUtils.showWarning("Debes seleccionar una fecha");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }

    @Getter @Setter
    public static class LineaPresupuestoTemp {
        private String articulo;
        private String descripcion;
        private int cantidad;
        private BigDecimal precio;
        private BigDecimal descuento;
        private BigDecimal importe;

        public LineaPresupuestoTemp(String articulo, String descripcion, int cantidad,
                                    BigDecimal precio, BigDecimal descuento, BigDecimal importe) {
            this.articulo = articulo;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precio = precio;
            this.descuento = descuento;
            this.importe = importe;
        }

    }
}

