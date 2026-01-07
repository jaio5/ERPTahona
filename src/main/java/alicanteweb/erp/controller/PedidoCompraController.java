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

import java.util.List;

@Slf4j
@Component
public class PedidoCompraController {

    @FXML private TableView<PedidoCompra> tablePedidos;
    @FXML private TableColumn<PedidoCompra, Long> colId;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, String> colFecha;
    @FXML private TableColumn<PedidoCompra, String> colProveedor;
    @FXML private TableColumn<PedidoCompra, String> colTotal;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TextField txtBuscar;

    private final PedidoService pedidoService;
    private ObservableList<PedidoCompra> pedidos;

    public PedidoCompraController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PedidoCompraController");

        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablePedidos.setStyle("-fx-background-color: #2b2b2b;");

        cargarPedidos();
    }

    private void cargarPedidos() {
        try {
            // TODO: Agregar método findAllCompras() al servicio si es necesario
            // Por ahora usamos una lista vacía
            List<PedidoCompra> lista = new java.util.ArrayList<>();
            pedidos = FXCollections.observableArrayList(lista);
            tablePedidos.setItems(pedidos);
            log.info("Pedidos de compra cargados: {}", pedidos.size());
        } catch (Exception e) {
            log.error("Error cargando pedidos de compra", e);
            mostrarError("Error", "No se pudieron cargar los pedidos: " + e.getMessage());
        }
    }

    @FXML
    private void onCreate() {
        log.info("Creando nuevo pedido de compra");
        mostrarInfo("Información", "Funcionalidad de creación de pedidos en desarrollo");
    }

    @FXML
    private void onEdit() {
        PedidoCompra seleccionado = tablePedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Por favor, selecciona un pedido para editar.");
            return;
        }
        log.info("Editando pedido: {}", seleccionado.getNumero());
        mostrarInfo("Información", "Funcionalidad de edición en desarrollo");
    }

    @FXML
    private void onDelete() {
        PedidoCompra seleccionado = tablePedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Por favor, selecciona un pedido para eliminar.");
            return;
        }
        mostrarInfo("Información", "Funcionalidad de eliminación en desarrollo");
    }

    @FXML
    private void onRefresh() {
        log.info("Refrescando pedidos de compra");
        cargarPedidos();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

