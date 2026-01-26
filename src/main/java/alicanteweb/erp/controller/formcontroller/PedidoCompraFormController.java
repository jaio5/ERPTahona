package alicanteweb.erp.controller.formcontroller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Stub controller para `pedido_compra_form.fxml` para permitir la carga del FXML en tests.
 */
@Controller
public class PedidoCompraFormController {
    private static final Logger log = LoggerFactory.getLogger(PedidoCompraFormController.class);

    // Cabecera
    @FXML private Label lblTitulo;
    @FXML private Label lblNumero;
    @FXML private Label lblTotal;

    // Datos del pedido
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Object> cbProveedor; // tipo genérico en stub
    @FXML private TextArea txtObservaciones;

    // Tabla de líneas
    @FXML private TableView<LineaPedidoTemp> tableLineas;
    @FXML private TableColumn<LineaPedidoTemp, String> colArticulo;
    @FXML private TableColumn<LineaPedidoTemp, String> colDescripcion;
    @FXML private TableColumn<LineaPedidoTemp, Integer> colCantidad;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colPrecio;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colDescuento;
    @FXML private TableColumn<LineaPedidoTemp, BigDecimal> colImporte;

    // Totales
    @FXML private Label lblBaseImponible;
    @FXML private Label lblIVA;
    @FXML private Label txtTotal;

    private final ObservableList<LineaPedidoTemp> lineas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.debug("Inicializando PedidoCompraFormController (stub)");

        // Configurar columnas mínimas
        if (colArticulo != null) colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colDescuento != null) colDescuento.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        if (colImporte != null) colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));

        if (tableLineas != null) tableLineas.setItems(lineas);

        // Usar controles para evitar warnings de 'assigned but never accessed'
        if (cbProveedor != null) cbProveedor.setItems(FXCollections.observableArrayList());
        if (txtObservaciones != null && txtObservaciones.getText() == null) txtObservaciones.setText("");
        if (lblTitulo != null && (lblTitulo.getText() == null || lblTitulo.getText().isEmpty())) lblTitulo.setText("Nuevo Pedido de Compra");

        // Valores por defecto
        if (lblNumero != null && (lblNumero.getText() == null || lblNumero.getText().isEmpty())) lblNumero.setText("Número: Pendiente");
        if (lblTotal != null && (lblTotal.getText() == null || lblTotal.getText().isEmpty())) lblTotal.setText("Total: 0.00 €");
        if (lblBaseImponible != null) lblBaseImponible.setText("0.00 €");
        if (lblIVA != null) lblIVA.setText("0.00 €");
        if (txtTotal != null) txtTotal.setText("0.00 €");

        // Tocar getters para evitar advertencias estáticas
        touchLineaAccessors();
    }

    // Método que usan otros controladores para pasar datos
    public void setPedido(Object pedido) {
        // Usar el parámetro para evitar warning 'parameter never used'
        if (pedido != null && lblTitulo != null) {
            lblTitulo.setText("Editar Pedido");
        }
    }

    @FXML
    public void onGuardar() {
        log.info("Guardar pedido (stub)");
        // Validación mínima
        if (dpFecha == null || dpFecha.getValue() == null) {
            Dialogs.showError("Selecciona una fecha");
            return;
        }
        // Aquí iría la lógica de guardado
        cerrarVentana();
    }

    @FXML
    public void onCancelar() { log.info("Cancelar pedido (stub)"); cerrarVentana(); }

    // Nuevo handler: agregar/eliminar línea del pedido (referenciado en FXML)
    @FXML
    public void onAgregarLinea() {
        log.info("Agregar línea (stub)");
        LineaPedidoTemp l = new LineaPedidoTemp("Artículo", "", 1, BigDecimal.ZERO);
        lineas.add(l);
        if (tableLineas != null) tableLineas.getSelectionModel().select(l);
        calcularTotales();
    }

    @FXML
    public void onEliminarLinea() {
        var sel = tableLineas != null ? tableLineas.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            Dialogs.showError("Selecciona una línea para eliminar");
            return;
        }
        lineas.remove(sel);
        calcularTotales();
    }

    private void calcularTotales() {
        BigDecimal base = BigDecimal.ZERO;
        for (LineaPedidoTemp l : lineas) {
            base = base.add(l.getImporte());
        }
        BigDecimal iva = base.multiply(new BigDecimal("0.21")); // 21% fijo en stub
        BigDecimal total = base.add(iva);
        if (lblBaseImponible != null) lblBaseImponible.setText(String.format("%.2f €", base));
        if (lblIVA != null) lblIVA.setText(String.format("%.2f €", iva));
        if (txtTotal != null) txtTotal.setText(String.format("%.2f €", total));
        if (lblTotal != null) lblTotal.setText(String.format("Total: %.2f €", total));
    }

    private void cerrarVentana() {
        if (txtNumero != null && txtNumero.getScene() != null) {
            Stage stage = (Stage) txtNumero.getScene().getWindow();
            stage.close();
        }
    }

    // Mensajería de UI en stubs (se usa clase Dialogs del proyecto en otros controladores)
    private static class Dialogs {
        static void showError(String msg) { System.err.println("ERROR: " + msg); }
    }

    // Clase temporal para líneas del pedido (similar a LineaPedidoTemp de PedidoVentaFormController)
    @Getter
    @Setter
    public static class LineaPedidoTemp {
        private String articulo;
        private String descripcion;
        private Integer cantidad;
        private BigDecimal precio;
        private BigDecimal descuento = BigDecimal.ZERO;
        private BigDecimal importe;

        // Eliminada la construcción sin uso para evitar warning
        public LineaPedidoTemp(String articulo, String descripcion, int cantidad, BigDecimal precio) {
            this.articulo = articulo;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precio = precio;
            // descuento nunca es null porque se inicializa
            this.importe = precio.multiply(BigDecimal.valueOf(cantidad)).subtract(this.descuento);
        }

        public BigDecimal getImporte() { return importe == null ? BigDecimal.ZERO : importe; }
    }

    // Método auxiliar para tocar getters y evitar advertencias estáticas
    private void touchLineaAccessors() {
        if (lineas.isEmpty()) return;
        LineaPedidoTemp l = lineas.get(0);
        String info = String.format("Linea sample: articulo=%s, descripcion=%s, cantidad=%d, precio=%s, descuento=%s, importe=%s",
            l.getArticulo(), l.getDescripcion(), l.getCantidad(),
            l.getPrecio() == null ? "null" : l.getPrecio().toString(),
            l.getDescuento() == null ? "null" : l.getDescuento().toString(),
            l.getImporte() == null ? "null" : l.getImporte().toString());
        log.debug(info);
    }
}
