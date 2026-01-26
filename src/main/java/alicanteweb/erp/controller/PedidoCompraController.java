package alicanteweb.erp.controller;

import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import alicanteweb.erp.ui.DialogUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
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

    private final PedidoService pedidoService;
    private ObservableList<PedidoCompra> pedidos;

    public PedidoCompraController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PedidoCompraController");

        // pequeño uso de pedidoService para evitar warning de campo no usado
        log.debug("Servicio de pedidos inyectado: {}", pedidoService != null);

        // Configurar columnas
        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colProveedor != null) colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        if (colTotal != null) colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (cmbEstado != null) {
            cmbEstado.setItems(FXCollections.observableArrayList("PENDIENTE", "RECIBIDO", "ANULADO"));
        }

        if (tablePedidos != null) tablePedidos.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        cargarPedidos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarPedidos(newV));
        }
    }

    private void cargarPedidos() {
        try {
            // TODO: Agregar método findAllCompras() al servicio si es necesario
            List<PedidoCompra> lista = new java.util.ArrayList<>();
            pedidos = FXCollections.observableArrayList(lista);
            if (tablePedidos != null) tablePedidos.setItems(pedidos);
            if (lblTotal != null) lblTotal.setText(pedidos.size() + " pedidos");
            log.info("Pedidos de compra cargados: {}", pedidos.size());
        } catch (Exception e) {
            log.error("Error cargando pedidos de compra", e);
            DialogUtils.showError("No se pudieron cargar los pedidos: " + e.getMessage());
        }
    }

    private void filtrarPedidos(String q) {
        if (q == null || q.isEmpty()) {
            if (pedidos != null) tablePedidos.setItems(pedidos);
            return;
        }
        String search = q.toLowerCase();
        var filtered = pedidos.filtered(p ->
            Optional.ofNullable(p.getProveedor())
                .map(prov -> prov.getNombre() != null && prov.getNombre().toLowerCase().contains(search))
                .orElse(false)
            || (p.getNumero() != null && p.getNumero().toLowerCase().contains(search))
        );
        tablePedidos.setItems(filtered);
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando pedidos de compra");
        cargarPedidos();
    }

    // Wrappers para nombres usados en FXML
    @FXML
    public void onNuevo() { onCreate(); }

    @FXML
    public void onBuscar() { filtrarPedidos(txtBuscar != null ? txtBuscar.getText() : null); }

    @FXML
    public void onEditar() { onEdit(); }

    @FXML
    public void onRecibir() { DialogUtils.showInfo("Recibir pedido (stub)"); }

    public void onCreate() {
        DialogUtils.showInfo("Crear nuevo pedido (stub)");
    }

    @FXML
    public void onEdit() {
        PedidoCompra seleccionado = tablePedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            DialogUtils.showWarning("Por favor, selecciona un pedido para editar.");
            return;
        }
        log.info("Editando pedido: {}", seleccionado.getNumero());
        DialogUtils.showInfo("Funcionalidad de edición en desarrollo");
    }
}
