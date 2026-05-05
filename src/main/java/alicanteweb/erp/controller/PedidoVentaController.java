package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.controller.formcontroller.PedidoVentaFormController;
import alicanteweb.erp.service.PedidoService;
import alicanteweb.erp.service.PedidoVentaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

@Controller
public class PedidoVentaController {
    private static final Logger log = LoggerFactory.getLogger(PedidoVentaController.class);

    @FXML private TableView<Pedido> tablePedidos;
    @FXML private TableColumn<Pedido, String> colNumero;
    @FXML private TableColumn<Pedido, String> colFecha;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private TextField txtBuscar;

    @FXML private TableColumn<Pedido, String> colTotal;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblTotal;

    private final PedidoVentaService pedidoVentaService;
    private final PedidoService pedidoService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Pedido> pedidosList = FXCollections.observableArrayList();

    public PedidoVentaController(PedidoVentaService pedidoVentaService, PedidoService pedidoService,
                                 ApplicationContext applicationContext) {
        this.pedidoVentaService = pedidoVentaService;
        this.pedidoService = pedidoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PedidoVentaController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarPedidos(newV));
        }

        if (cmbEstado != null) {
            cmbEstado.setItems(FXCollections.observableArrayList("PENDIENTE", "EN_PROCESO", "SERVIDO", "CANCELADO"));
        }
    }

    private void configurarColumnas() {
        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFecha()).map(Object::toString).orElse("")));
        if (colCliente != null) colCliente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCliente()).map(Cliente::getNombre).orElse("")));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        if (colTotal != null) colTotal.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getTotal()).map(Object::toString).orElse("")));

        if (tablePedidos != null) tablePedidos.setItems(pedidosList);
    }

    private void cargarDatos() {
        try {
            pedidosList.clear();
            pedidosList.addAll(pedidoVentaService.obtenerTodos());
            log.info("Pedidos de venta cargados: {}", pedidosList.size());
            javafx.application.Platform.runLater(() -> {
                if (tablePedidos != null) tablePedidos.refresh();
            });

            if (lblTotal != null) lblTotal.setText(pedidosList.size() + " pedidos");

        } catch (Exception e) {
            log.error("Error cargando pedidos", e);
        }
    }

    private void filtrarPedidos(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<Pedido> filtered = FXCollections.observableArrayList(
            pedidosList.stream()
                .filter(p -> (p.getCliente() != null && p.getCliente().getNombre().toLowerCase().contains(search)))
                .toList()
        );
        tablePedidos.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        abrirFormulario(null);
    }

    // Wrapper para el botón Buscar en el FXML
    @FXML
    public void onBuscar() {
        filtrarPedidos(txtBuscar != null ? txtBuscar.getText() : null);
    }

    @FXML
    public void onEditar() {
        Pedido pedido = tablePedidos.getSelectionModel().getSelectedItem();
        if (pedido == null) { mostrarAlerta("Seleccione un pedido para editar"); return; }
        abrirFormulario(pedido);
    }

    @FXML
    public void onEliminar() {
        Pedido pedido = tablePedidos.getSelectionModel().getSelectedItem();
        if (pedido == null) { mostrarAlerta("Seleccione un pedido para eliminar"); return; }
        if (mostrarConfirmacion()) {
            try {
                pedidoVentaService.eliminar(pedido.getId());
                cargarDatos();
                mostrarExito();
            } catch (Exception e) {
                log.error("Error eliminando pedido", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    // Nuevo handler para el boton 'Servir' en el FXML
    @FXML
    public void onServir() {
        Pedido pedido = tablePedidos.getSelectionModel().getSelectedItem();
        if (pedido == null) {
            mostrarAlerta("Seleccione un pedido para servir");
            return;
        }
        if (!mostrarConfirmacion("¿Desea convertir este pedido en albarán?")) {
            return;
        }
        try {
            AlbaranVenta albaran = pedidoService.convertirAAlbaran(pedido.getId(), null);
            cargarDatos();
            mostrarInfo("Pedido servido. Albarán generado: " + (albaran != null ? albaran.getNumero() : ""));
        } catch (Exception e) {
            log.error("Error sirviendo pedido {}", pedido.getNumero(), e);
            mostrarError("Error al servir pedido: " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        cargarDatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText("Pedido eliminado correctamente");
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion() {
        return mostrarConfirmacion("¿Desea eliminar este pedido?");
    }

    private boolean mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(mensaje);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void abrirFormulario(Pedido pedido) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ui/pedido_venta_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            javafx.scene.Parent root = loader.load();
            if (loader.getController() instanceof PedidoVentaFormController controller) {
                controller.setPedido(pedido);
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(pedido == null ? "Nuevo Pedido de Venta" : "Editar Pedido de Venta");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de pedido de venta", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }
}
