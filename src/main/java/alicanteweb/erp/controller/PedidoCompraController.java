package alicanteweb.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.service.PedidoCompraService;
import org.springframework.stereotype.Controller;

@Controller
public class PedidoCompraController {

    @FXML private TableView<PedidoCompra> tablePedidosCompra;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, String> colFecha;
    @FXML private TableColumn<PedidoCompra, String> colProveedor;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TextField txtSearch;

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cboProveedor;
    @FXML private ComboBox<String> cboEstado;

    private final PedidoCompraService pedidoCompraService;
    private final ObservableList<PedidoCompra> pedidosList = FXCollections.observableArrayList();

    public PedidoCompraController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    @FXML
    public void initialize() {
        tablePedidosCompra.setItems(pedidosList);
        // Aquí se configurarían las celdas de la tabla (setCellValueFactory)
        cargarPedidos();
    }

    private void cargarPedidos() {
        pedidosList.setAll(pedidoCompraService.findAll());
    }

    @FXML
    public void handleSearch() {
        String filtro = txtSearch.getText();
        pedidosList.setAll(pedidoCompraService.buscarPorFiltro(filtro));
    }

    @FXML
    public void handleNuevo() {
        txtNumero.clear();
        dpFecha.setValue(null);
        cboProveedor.getSelectionModel().clearSelection();
        cboEstado.getSelectionModel().clearSelection();
        tablePedidosCompra.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        // En una app real, aquí se validaría la entrada.
        PedidoCompra pedido = new PedidoCompra();
        pedido.setNumero(txtNumero.getText());
        pedido.setFecha(dpFecha.getValue());
        pedido.setProveedor(cboProveedor.getValue());
        pedido.setEstado(cboEstado.getValue());
        
        // Aquí faltaría la lógica para el total, pero para compilar es suficiente.
        pedido.setTotal(0.0f); 

        pedidoCompraService.save(pedido);
        cargarPedidos();
    }

    @FXML
    public void handleEliminar() {
        PedidoCompra seleccionado = tablePedidosCompra.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            // Aquí iría una confirmación antes de borrar.
            pedidoCompraService.delete(seleccionado);
            cargarPedidos();
        }
    }
    
    @FXML 
    public void handleVolver() {
        // Lógica para volver a la pantalla principal (requiere referencia al MainPanelController)
    }
}
