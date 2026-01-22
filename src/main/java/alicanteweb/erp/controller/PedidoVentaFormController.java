package alicanteweb.erp.controller;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el formulario de pedidos de venta
 */
@Controller
public class PedidoVentaFormController {
    private static final Logger log = LoggerFactory.getLogger(PedidoVentaFormController.class);

    @FXML private Label lblTitulo;
    @FXML private Label lblNumero;
    @FXML private Label lblTotal;

    @FXML private Label lblBaseImponible;
    @FXML private Label lblIVA;
    @FXML private Label txtTotal;

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardarPedido;

    @FXML private TableView<LineaPedidoTemp> tableLineas;
    @FXML private TableColumn<LineaPedidoTemp, String> colArticulo;
    @FXML private TableColumn<LineaPedidoTemp, String> colDescripcion;
    @FXML private TableColumn<LineaPedidoTemp, Integer> colCantidad;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colPrecio;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colDescuento;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colImporte;

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;

    private Pedido pedidoActual;
    private boolean modoEdicion = false;
    private final ObservableList<LineaPedidoTemp> lineasTemp = FXCollections.observableArrayList();

    public PedidoVentaFormController(PedidoService pedidoService,
                                     ClienteService clienteService,
                                     ArticuloService articuloService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("PedidoVentaFormController inicializado");

        configurarClientes();
        configurarEstados();
        configurarTablaLineas();
        configurarFechas();

        // Bind botón Guardar: habilitar solo si cliente seleccionado, fecha existe y hay al menos una línea
        try {
            if (btnGuardarPedido != null) {
                btnGuardarPedido.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(() ->
                        cbCliente.getValue() == null || dpFecha.getValue() == null || lineasTemp.isEmpty(),
                        cbCliente.valueProperty(), dpFecha.valueProperty(), lineasTemp
                    )
                );
            }
        } catch (Exception e) {
            log.debug("No se pudo bindear btnGuardarPedido: {}", e.getMessage());
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
                "PENDIENTE", "CONFIRMADO", "EN_PROCESO", "ENVIADO", "ENTREGADO", "CANCELADO"
            ));
            cbEstado.setValue("PENDIENTE");
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
            lineasTemp.addListener((javafx.collections.ListChangeListener.Change<? extends LineaPedidoTemp> c) -> calcularTotales());
        }
    }

    private void configurarFechas() {
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
    }

    public void setPedido(Pedido pedido) {
        this.pedidoActual = pedido;
        this.modoEdicion = (pedido != null && pedido.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                lblTitulo.setText("Editar Pedido");
                cargarDatos(pedido);
            } else {
                lblTitulo.setText("Nuevo Pedido de Venta");
                limpiarFormulario();
                generarNumero();
            }
        });
    }

    private void cargarDatos(Pedido pedido) {
        txtNumero.setText(pedido.getNumero());
        dpFecha.setValue(pedido.getFecha());
        cbCliente.setValue(pedido.getCliente());
        cbEstado.setValue(pedido.getEstado());
        txtObservaciones.setText(pedido.getObservaciones());
        lblNumero.setText("Numero: " + pedido.getNumero());
        calcularTotales();
    }

    private void limpiarFormulario() {
        txtNumero.setText("");
        dpFecha.setValue(LocalDate.now());
        cbCliente.setValue(null);
        if (cbEstado != null) cbEstado.setValue("PENDIENTE");
        if (txtObservaciones != null) txtObservaciones.setText("");
        lineasTemp.clear();
        lblNumero.setText("Numero: Pendiente");
        calcularTotales();
    }

    private void generarNumero() {
        String numero = "PV-" + LocalDate.now().getYear() + "-" + String.format("%06d", System.currentTimeMillis() % 1000000);
        txtNumero.setText(numero);
        lblNumero.setText("Numero: " + numero);
    }

    @FXML
    public void onAgregarLinea() {
        Dialog<LineaPedidoTemp> dialog = new Dialog<>();
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

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAgregar) {
                Articulo articulo = cbArticulo.getValue();
                if (articulo != null) {
                    try {
                        int cantidad = Integer.parseInt(txtCantidad.getText());
                        BigDecimal precio = new BigDecimal(txtPrecio.getText());
                        BigDecimal importe = precio.multiply(BigDecimal.valueOf(cantidad));

                        return new LineaPedidoTemp(
                            articulo.getNombre(),
                            cantidad,
                            precio,
                            importe.setScale(2, RoundingMode.HALF_UP)
                        );
                    } catch (Exception e) {
                        mostrarError("Valores invalidos");
                    }
                }
            }
            return null;
        });

        Optional<LineaPedidoTemp> result = dialog.showAndWait();
        result.ifPresent(lineasTemp::add);
    }

    @FXML
    public void onEliminarLinea() {
        LineaPedidoTemp selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lineasTemp.remove(selected);
        } else {
            mostrarAdvertencia("Selecciona una linea para eliminar");
        }
    }

    private void calcularTotales() {
        BigDecimal total = lineasTemp.stream()
            .map(LineaPedidoTemp::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotal.setText("Total: " + total.setScale(2, RoundingMode.HALF_UP) + " EUR");
        // actualizar labels de resumen si existen
        try {
            if (lblBaseImponible != null) lblBaseImponible.setText(total.setScale(2, RoundingMode.HALF_UP) + " €");
            if (lblIVA != null) lblIVA.setText("0.00 €");
            if (txtTotal != null) txtTotal.setText(total.setScale(2, RoundingMode.HALF_UP) + " €");
        } catch (Exception ignored) {}
    }

    @FXML
    public void onGuardar() {
        if (!validarFormulario()) return;

        try {
            if (pedidoActual == null) {
                pedidoActual = new Pedido();
            }

            pedidoActual.setNumero(txtNumero.getText());
            pedidoActual.setFecha(dpFecha.getValue());
            pedidoActual.setCliente(cbCliente.getValue());
            pedidoActual.setEstado(cbEstado.getValue());
            pedidoActual.setObservaciones(txtObservaciones != null ? txtObservaciones.getText() : "");

            BigDecimal total = lineasTemp.stream()
                .map(LineaPedidoTemp::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            pedidoActual.setTotal(total);

            // Convertir lineas temporales a entidades PedidoLinea
            pedidoActual.getLineas().clear();
            for (LineaPedidoTemp lt : lineasTemp) {
                PedidoLinea linea = new PedidoLinea();
                // intentar obtener articulo por nombre (mejor usar id en una mejora futura)
                if (lt.getArticulo() != null) {
                    Articulo a = articuloService.findAll().stream()
                        .filter(x -> x.getNombre().equals(lt.getArticulo()))
                        .findFirst().orElse(null);
                    linea.setArticulo(a);
                }
                linea.setDescripcion(null);
                linea.setCantidad(new BigDecimal(lt.getCantidad()));
                linea.setPrecio(lt.getPrecio());
                linea.setDescuento(BigDecimal.ZERO);
                linea.setIva(new BigDecimal("21"));
                linea.setPedido(pedidoActual);
                pedidoActual.getLineas().add(linea);
            }

            pedidoService.save(pedidoActual);
            mostrarExito("Pedido guardado correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando pedido", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (cbCliente.getValue() == null) {
            mostrarAdvertencia("Debes seleccionar un cliente");
            return false;
        }
        if (dpFecha.getValue() == null) {
            mostrarAdvertencia("Debes seleccionar una fecha");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atencion");
        alert.setHeaderText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class LineaPedidoTemp {
        private String articulo;
        private String descripcion;
        private int cantidad;
        private BigDecimal precio;
        private BigDecimal descuento;
        private BigDecimal importe;

        public LineaPedidoTemp(String articulo, int cantidad, BigDecimal precio, BigDecimal importe) {
            this.articulo = articulo;
            this.descripcion = "";
            this.cantidad = cantidad;
            this.precio = precio;
            this.descuento = BigDecimal.ZERO;
            this.importe = importe;
        }

        public String getArticulo() { return articulo; }
        public void setArticulo(String articulo) { this.articulo = articulo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
        public BigDecimal getDescuento() { return descuento; }
        public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
        public BigDecimal getImporte() { return importe; }
        public void setImporte(BigDecimal importe) { this.importe = importe; }
    }
}

