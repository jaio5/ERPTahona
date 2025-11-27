package alicanteweb.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.service.PedidoCompraService;

/**
 * Controlador para la vista de gestión de pedidos de compra.
 * Conecta los elementos FXML con la lógica de la aplicación.
 * Ejemplo para DAM: aquí se enlazan los componentes visuales con la lógica de negocio usando JPA y servicios.
 */
public class PedidoCompraController {
    // Elementos de la tabla y filtros
    @FXML private TableView<PedidoCompra> tablePedidosCompra;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, String> colFecha;
    @FXML private TableColumn<PedidoCompra, String> colProveedor;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TextField txtSearch;

    // Elementos del formulario de pedido
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cboProveedor;
    @FXML private ComboBox<String> cboEstado;

    // Servicio para acceder a la base de datos (JPA)
    private final PedidoCompraService pedidoCompraService;
    private final ObservableList<PedidoCompra> pedidosList = FXCollections.observableArrayList();

    // Constructor para inyección manual (sin @Autowired)
    public PedidoCompraController() {
        this.pedidoCompraService = new PedidoCompraService(); // En producción, usaría un gestor de dependencias
    }

    // Inicialización automática al cargar el FXML
    @FXML
    public void initialize() {
        tablePedidosCompra.setItems(pedidosList);
        cargarPedidos();
        // Configura columnas si es necesario
    }

    private void cargarPedidos() {
        pedidosList.clear();
        pedidosList.addAll(pedidoCompraService.findAll());
    }

    // Acciones principales
    @FXML public void handleVolver() {
        // Implementa la lógica para volver al menú principal
    }
    @FXML public void handleSearch() {
        // Implementa la lógica de búsqueda y limpieza de filtros
        String filtro = txtSearch.getText();
        pedidosList.clear();
        pedidosList.addAll(pedidoCompraService.buscarPorFiltro(filtro));
    }
    @FXML public void handleNuevo() {
        // Limpia el formulario para crear un nuevo pedido
        txtNumero.clear();
        dpFecha.setValue(null);
        cboProveedor.getSelectionModel().clearSelection();
        cboEstado.getSelectionModel().clearSelection();
    }
    @FXML public void handleGuardar() {
        // Guarda el pedido en la base de datos
        PedidoCompra pedido = new PedidoCompra();
        pedido.setNumero(txtNumero.getText());
        pedido.setFecha(dpFecha.getValue());
        pedido.setProveedor(cboProveedor.getValue());
        pedido.setEstado(cboEstado.getValue());
        pedidoCompraService.save(pedido);
        cargarPedidos();
    }
    @FXML public void handleEliminar() {
        // Elimina el pedido seleccionado
        PedidoCompra seleccionado = tablePedidosCompra.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            pedidoCompraService.delete(seleccionado);
            cargarPedidos();
        }
    }
}
