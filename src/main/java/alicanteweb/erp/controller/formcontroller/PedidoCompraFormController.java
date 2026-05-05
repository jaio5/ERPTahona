package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.entities.PedidoCompraLinea;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.PedidoCompraService;
import alicanteweb.erp.service.ProveedorService;
import alicanteweb.erp.ui.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Controller
public class PedidoCompraFormController {
    private static final Logger log = LoggerFactory.getLogger(PedidoCompraFormController.class);
    private static final BigDecimal IVA_DEFECTO = new BigDecimal("21.00");

    @FXML private Label lblTitulo;
    @FXML private Label lblNumero;
    @FXML private Label lblTotal;
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Proveedor> cbProveedor;
    @FXML private TextArea txtObservaciones;
    @FXML private TableView<LineaPedidoTemp> tableLineas;
    @FXML private TableColumn<LineaPedidoTemp, String> colArticulo;
    @FXML private TableColumn<LineaPedidoTemp, String> colDescripcion;
    @FXML private TableColumn<LineaPedidoTemp, Integer> colCantidad;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colPrecio;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colDescuento;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colImporte;
    @FXML private Label lblBaseImponible;
    @FXML private Label lblIVA;
    @FXML private Label txtTotal;

    private final PedidoCompraService pedidoCompraService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;
    private final ObservableList<LineaPedidoTemp> lineas = FXCollections.observableArrayList();

    private PedidoCompra pedidoActual;

    public PedidoCompraFormController(PedidoCompraService pedidoCompraService,
                                      ProveedorService proveedorService,
                                      ArticuloService articuloService) {
        this.pedidoCompraService = pedidoCompraService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarProveedor();
        if (tableLineas != null) {
            tableLineas.setItems(lineas);
        }
        if (dpFecha != null && dpFecha.getValue() == null) {
            dpFecha.setValue(LocalDate.now());
        }
        actualizarCabeceraNuevo();
        calcularTotales();
    }

    public void setPedido(Object pedido) {
        if (pedido instanceof PedidoCompra compra && compra.getId() != null) {
            pedidoActual = pedidoCompraService.findByIdWithLineas(compra.getId()).orElse(compra);
            cargarPedido(pedidoActual);
        } else {
            pedidoActual = null;
            actualizarCabeceraNuevo();
        }
    }

    @FXML
    public void onGuardar() {
        if (!validarFormulario()) {
            return;
        }

        try {
            PedidoCompra pedido = pedidoActual != null ? pedidoActual : new PedidoCompra();
            pedido.setFecha(dpFecha.getValue());
            pedido.setProveedor(cbProveedor.getValue());
            pedido.setObservaciones(txtObservaciones != null ? txtObservaciones.getText() : null);
            if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
                pedido.setEstado("BORRADOR");
            }

            if (txtNumero != null && txtNumero.getText() != null && !txtNumero.getText().isBlank()) {
                pedido.setNumero(txtNumero.getText().trim());
            }

            pedido.getLineas().clear();
            int orden = 1;
            for (LineaPedidoTemp temp : lineas) {
                PedidoCompraLinea linea = new PedidoCompraLinea();
                linea.setPedidoCompra(pedido);
                linea.setArticulo(temp.getArticulo());
                linea.setDescripcion(temp.getDescripcion());
                linea.setCantidad(BigDecimal.valueOf(temp.getCantidad()));
                linea.setCantidadRecibida(BigDecimal.ZERO);
                linea.setPrecioUnitario(temp.getPrecio());
                linea.setDescuento(temp.getDescuento());
                linea.setTipoIva(temp.getTipoIva());
                linea.setImporte(temp.getImporte());
                linea.setOrden(orden++);
                pedido.getLineas().add(linea);
            }

            pedido.setTotal(calcularTotalConIva());
            pedidoCompraService.save(pedido);
            DialogUtils.showSuccess("Pedido de compra guardado correctamente");
            cerrarVentana();
        } catch (Exception e) {
            log.error("Error guardando pedido de compra", e);
            DialogUtils.showError("Error al guardar pedido de compra: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    @FXML
    public void onAgregarLinea() {
        Dialog<LineaPedidoTemp> dialog = new Dialog<>();
        dialog.setTitle("Agregar línea");
        dialog.setHeaderText("Selecciona artículo y cantidades");

        ButtonType agregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(agregar, ButtonType.CANCEL);

        ComboBox<Articulo> cbArticulo = new ComboBox<>();
        TextField txtDescripcion = new TextField();
        TextField txtCantidad = new TextField("1");
        TextField txtPrecio = new TextField("0.00");
        TextField txtDescuento = new TextField("0.00");
        TextField txtIva = new TextField(IVA_DEFECTO.toPlainString());

        List<Articulo> articulos = articuloService.findAll().stream()
            .filter(a -> a.getActivo() == null || Boolean.TRUE.equals(a.getActivo()))
            .toList();
        cbArticulo.setItems(FXCollections.observableArrayList(articulos));
        configurarArticuloCombo(cbArticulo);
        cbArticulo.valueProperty().addListener((obs, oldVal, articulo) -> {
            if (articulo != null) {
                txtDescripcion.setText(firstNonBlank(articulo.getNombre(), articulo.getDescripcion()));
                txtPrecio.setText(valorMonetario(articulo.getCoste() != null ? articulo.getCoste() : articulo.getPvp()));
                txtIva.setText(valorMonetario(articulo.getIva() != null ? articulo.getIva() : IVA_DEFECTO));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Artículo"), cbArticulo);
        grid.addRow(1, new Label("Descripción"), txtDescripcion);
        grid.addRow(2, new Label("Cantidad"), txtCantidad);
        grid.addRow(3, new Label("Precio"), txtPrecio);
        grid.addRow(4, new Label("Dto. %"), txtDescuento);
        grid.addRow(5, new Label("IVA %"), txtIva);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != agregar) {
                return null;
            }
            Articulo articulo = cbArticulo.getValue();
            BigDecimal cantidad = parseDecimal(txtCantidad.getText(), "cantidad");
            BigDecimal precio = parseDecimal(txtPrecio.getText(), "precio");
            BigDecimal descuento = parseDecimal(txtDescuento.getText(), "descuento");
            BigDecimal iva = parseDecimal(txtIva.getText(), "IVA");
            return new LineaPedidoTemp(
                articulo,
                txtDescripcion.getText(),
                cantidad.intValue(),
                precio,
                descuento,
                iva
            );
        });

        dialog.showAndWait().ifPresent(linea -> {
            lineas.add(linea);
            calcularTotales();
        });
    }

    @FXML
    public void onEliminarLinea() {
        LineaPedidoTemp seleccionada = tableLineas != null ? tableLineas.getSelectionModel().getSelectedItem() : null;
        if (seleccionada == null) {
            DialogUtils.showWarning("Selecciona una línea para eliminar");
            return;
        }
        lineas.remove(seleccionada);
        calcularTotales();
    }

    private void configurarColumnas() {
        if (colArticulo != null) {
            colArticulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticuloTexto()));
        }
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colDescuento != null) colDescuento.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        if (colImporte != null) colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
    }

    private void configurarProveedor() {
        if (cbProveedor == null) {
            return;
        }
        cbProveedor.setItems(FXCollections.observableArrayList(proveedorService.findActivos()));
        cbProveedor.setCellFactory(param -> proveedorCell());
        cbProveedor.setButtonCell(proveedorCell());
    }

    private ListCell<Proveedor> proveedorCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCodigo() + " - " + item.getNombre());
            }
        };
    }

    private void configurarArticuloCombo(ComboBox<Articulo> cbArticulo) {
        cbArticulo.setCellFactory(param -> articuloCell());
        cbArticulo.setButtonCell(articuloCell());
    }

    private ListCell<Articulo> articuloCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Articulo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCodigo() + " - " + firstNonBlank(item.getNombre(), item.getDescripcion()));
            }
        };
    }

    private void cargarPedido(PedidoCompra pedido) {
        if (lblTitulo != null) lblTitulo.setText("Editar Pedido de Compra");
        if (txtNumero != null) txtNumero.setText(pedido.getNumero());
        if (lblNumero != null) lblNumero.setText("Número: " + pedido.getNumero());
        if (dpFecha != null) dpFecha.setValue(pedido.getFecha());
        if (cbProveedor != null) cbProveedor.setValue(pedido.getProveedor());
        if (txtObservaciones != null) txtObservaciones.setText(pedido.getObservaciones());

        lineas.clear();
        if (pedido.getLineas() != null) {
            pedido.getLineas().stream()
                .sorted(java.util.Comparator.comparing(PedidoCompraLinea::getOrden, java.util.Comparator.nullsLast(Integer::compareTo)))
                .map(LineaPedidoTemp::fromEntity)
                .forEach(lineas::add);
        }
        calcularTotales();
    }

    private void actualizarCabeceraNuevo() {
        if (lblTitulo != null) lblTitulo.setText("Nuevo Pedido de Compra");
        if (txtNumero != null) txtNumero.setText("");
        if (lblNumero != null) lblNumero.setText("Número: Pendiente");
        if (txtObservaciones != null && txtObservaciones.getText() == null) txtObservaciones.setText("");
        lineas.clear();
        calcularTotales();
    }

    private boolean validarFormulario() {
        if (dpFecha == null || dpFecha.getValue() == null) {
            DialogUtils.showWarning("Selecciona una fecha");
            return false;
        }
        if (cbProveedor == null || cbProveedor.getValue() == null) {
            DialogUtils.showWarning("Selecciona un proveedor");
            return false;
        }
        if (lineas.isEmpty()) {
            DialogUtils.showWarning("Añade al menos una línea al pedido");
            return false;
        }
        return true;
    }

    private void calcularTotales() {
        BigDecimal base = lineas.stream()
            .map(LineaPedidoTemp::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = lineas.stream()
            .map(LineaPedidoTemp::getImporteIva)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(iva).setScale(2, RoundingMode.HALF_UP);

        if (lblBaseImponible != null) lblBaseImponible.setText(valorMonetario(base) + " EUR");
        if (lblIVA != null) lblIVA.setText(valorMonetario(iva) + " EUR");
        if (txtTotal != null) txtTotal.setText(valorMonetario(total) + " EUR");
        if (lblTotal != null) lblTotal.setText("Total: " + valorMonetario(total) + " EUR");
    }

    private BigDecimal calcularTotalConIva() {
        return lineas.stream()
            .map(linea -> linea.getImporte().add(linea.getImporteIva()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal parseDecimal(String value, String campo) {
        try {
            return new BigDecimal(value.replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new IllegalArgumentException("Valor inválido para " + campo + ": " + value);
        }
    }

    private String valorMonetario(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private void cerrarVentana() {
        if (txtNumero != null && txtNumero.getScene() != null) {
            ((Stage) txtNumero.getScene().getWindow()).close();
        }
    }

    @Getter
    @Setter
    public static class LineaPedidoTemp {
        private Articulo articulo;
        private String descripcion;
        private Integer cantidad;
        private BigDecimal precio;
        private BigDecimal descuento;
        private BigDecimal tipoIva;
        private BigDecimal importe;

        public LineaPedidoTemp(Articulo articulo, String descripcion, Integer cantidad,
                               BigDecimal precio, BigDecimal descuento, BigDecimal tipoIva) {
            this.articulo = articulo;
            this.descripcion = descripcion;
            this.cantidad = cantidad != null ? cantidad : 1;
            this.precio = precio != null ? precio : BigDecimal.ZERO;
            this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
            this.tipoIva = tipoIva != null ? tipoIva : IVA_DEFECTO;
            recalcularImporte();
        }

        static LineaPedidoTemp fromEntity(PedidoCompraLinea linea) {
            return new LineaPedidoTemp(
                linea.getArticulo(),
                linea.getDescripcion(),
                linea.getCantidad() != null ? linea.getCantidad().intValue() : 1,
                linea.getPrecioUnitario(),
                linea.getDescuento(),
                linea.getTipoIva()
            );
        }

        public String getArticuloTexto() {
            if (articulo == null) {
                return "";
            }
            return articulo.getCodigo() + " - " + (articulo.getNombre() != null ? articulo.getNombre() : articulo.getDescripcion());
        }

        public BigDecimal getImporteIva() {
            return importe.multiply(tipoIva)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        private void recalcularImporte() {
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
            BigDecimal descuentoImporte = subtotal.multiply(descuento)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            importe = subtotal.subtract(descuentoImporte).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
