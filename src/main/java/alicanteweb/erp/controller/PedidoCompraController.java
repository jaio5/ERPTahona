package alicanteweb.erp.controller;

import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.controller.formcontroller.PedidoCompraFormController;
import alicanteweb.erp.service.PedidoCompraService;
import alicanteweb.erp.ui.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de Pedidos de Compra.
 */
@Slf4j
@Controller
public class PedidoCompraController {

    @FXML private TableView<PedidoCompra> tablePedidos;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, String> colFecha;
    @FXML private TableColumn<PedidoCompra, String> colProveedor;
    @FXML private TableColumn<PedidoCompra, String> colTotal;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblTotal;

    private final PedidoCompraService pedidoCompraService;
    private final ApplicationContext applicationContext;
    private ObservableList<PedidoCompra> pedidos;

    public PedidoCompraController(PedidoCompraService pedidoCompraService, ApplicationContext applicationContext) {
        this.pedidoCompraService = pedidoCompraService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        if (colNumero   != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha    != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colProveedor!= null) colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        if (colTotal    != null) colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colEstado   != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (cmbEstado != null) {
            cmbEstado.setItems(FXCollections.observableArrayList("TODOS", "PENDIENTE", "RECIBIDO", "ANULADO"));
            cmbEstado.setValue("TODOS");
        }

        cargarPedidos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarPedidos(newV));
        }
    }

    private void cargarPedidos() {
        try {
            List<PedidoCompra> lista = pedidoCompraService.findAll();
            pedidos = FXCollections.observableArrayList(lista);
            if (tablePedidos != null) tablePedidos.setItems(pedidos);
            if (lblTotal != null) lblTotal.setText(lista.size() + " pedidos");
            log.info("Pedidos de compra cargados: {}", lista.size());
        } catch (Exception e) {
            log.error("Error cargando pedidos de compra", e);
            DialogUtils.showError("No se pudieron cargar los pedidos: " + e.getMessage());
        }
    }

    private void filtrarPedidos(String q) {
        if (pedidos == null) return;
        if (q == null || q.isEmpty()) {
            tablePedidos.setItems(pedidos);
            return;
        }
        String search = q.toLowerCase();
        tablePedidos.setItems(pedidos.filtered(p ->
            Optional.ofNullable(p.getProveedor())
                .map(prov -> prov.getNombre() != null && prov.getNombre().toLowerCase().contains(search))
                .orElse(false)
            || (p.getNumero() != null && p.getNumero().toLowerCase().contains(search))
        ));
    }

    @FXML public void onRefresh()  { cargarPedidos(); }
    @FXML public void onNuevo()    { onCreate(); }
    @FXML public void onBuscar()   { filtrarPedidos(txtBuscar != null ? txtBuscar.getText() : null); }
    @FXML public void onEditar()   { onEdit(); }
    @FXML public void onRecibir()  {
        if (tablePedidos == null) return;
        PedidoCompra seleccionado = tablePedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            DialogUtils.showWarning("Selecciona un pedido para recibir.");
            return;
        }
        try {
            pedidoCompraService.cambiarEstado(seleccionado.getId(), "RECIBIDO");
            cargarPedidos();
            DialogUtils.showInfo("Pedido recibido correctamente.");
        } catch (Exception e) {
            log.error("Error recibiendo pedido de compra {}", seleccionado.getNumero(), e);
            DialogUtils.showError("Error al recibir pedido: " + e.getMessage());
        }
    }

    public void onCreate() {
        abrirFormulario(null);
    }

    @FXML
    public void onEdit() {
        if (tablePedidos == null) return;
        PedidoCompra seleccionado = tablePedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            DialogUtils.showWarning("Selecciona un pedido para editar.");
            return;
        }
        log.info("Editando pedido de compra: {}", seleccionado.getNumero());
        abrirFormulario(seleccionado);
    }

    private void abrirFormulario(PedidoCompra pedido) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ui/pedido_compra_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            javafx.scene.Parent root = loader.load();
            if (loader.getController() instanceof PedidoCompraFormController controller) {
                controller.setPedido(pedido);
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(pedido == null ? "Nuevo Pedido de Compra" : "Editar Pedido de Compra");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarPedidos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de pedido de compra", e);
            DialogUtils.showError("Error al abrir formulario: " + e.getMessage());
        }
    }
}
