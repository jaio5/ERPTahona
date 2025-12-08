package alicanteweb.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.service.PedidoCompraService;
import org.springframework.stereotype.Controller;
import javafx.concurrent.Task;
import java.util.List;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PedidoCompraController {

    private static final Logger log = LoggerFactory.getLogger(PedidoCompraController.class);

    @FXML private TableView<PedidoCompra> tablePedidosCompra;
    @FXML private TableColumn<PedidoCompra, String> colNumero;
    @FXML private TableColumn<PedidoCompra, Object> colFecha; // fecha es LocalDate en la entidad
    @FXML private TableColumn<PedidoCompra, String> colProveedor;
    @FXML private TableColumn<PedidoCompra, String> colEstado;
    @FXML private TextField txtSearch;
    @FXML private ProgressIndicator progressIndicator; // opcional en el FXML

    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cboProveedor;
    @FXML private ComboBox<String> cboEstado;

    private final PedidoCompraService pedidoCompraService;
    private final ObservableList<PedidoCompra> pedidosList = FXCollections.observableArrayList();

    // Evitar recargas concurrentes
    private volatile boolean loading = false;

    public PedidoCompraController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    @FXML
    public void initialize() {
        tablePedidosCompra.setItems(pedidosList);
        // Configurar las celdas de la tabla (PropertyValueFactory usa el nombre del getter/prop)
        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colProveedor != null) colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Ocultar el indicador si existe hasta que se use
        if (progressIndicator != null) progressIndicator.setVisible(false);

        cargarPedidos();
    }

    private void cargarPedidos() {
        if (loading) return; // ya hay una carga en curso
        loading = true;

        // Marcar UI como cargando
        if (progressIndicator != null) progressIndicator.setVisible(true);
        if (tablePedidosCompra != null) tablePedidosCompra.setDisable(true);
        if (txtSearch != null) txtSearch.setDisable(true);

        Task<List<PedidoCompra>> task = new Task<List<PedidoCompra>>() {
            @Override
            protected List<PedidoCompra> call() {
                return pedidoCompraService.findAll();
            }
        };

        task.setOnSucceeded(ev -> {
            pedidosList.setAll(task.getValue());
            if (tablePedidosCompra != null) {
                tablePedidosCompra.getSelectionModel().clearSelection();
                tablePedidosCompra.setDisable(false);
            }
            if (txtSearch != null) txtSearch.setDisable(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
            loading = false;
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            log.error("Error cargando pedidos", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error cargando pedidos: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
            if (tablePedidosCompra != null) tablePedidosCompra.setDisable(false);
            if (txtSearch != null) txtSearch.setDisable(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
            loading = false;
        });

        new Thread(task, "cargarPedidos-thread").start();
    }

    @FXML
    public void handleSearch() {
        String filtro = txtSearch.getText();
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarPedidos();
            return;
        }

        if (loading) return; // evitar búsquedas concurrentes
        loading = true;
        if (progressIndicator != null) progressIndicator.setVisible(true);
        if (tablePedidosCompra != null) tablePedidosCompra.setDisable(true);
        if (txtSearch != null) txtSearch.setDisable(true);

        Task<List<PedidoCompra>> task = new Task<List<PedidoCompra>>() {
            @Override
            protected List<PedidoCompra> call() {
                return pedidoCompraService.buscarPorFiltro(filtro);
            }
        };

        task.setOnSucceeded(ev -> {
            pedidosList.setAll(task.getValue());
            if (tablePedidosCompra != null) {
                tablePedidosCompra.getSelectionModel().clearSelection();
                tablePedidosCompra.setDisable(false);
            }
            if (txtSearch != null) txtSearch.setDisable(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
            loading = false;
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            log.error("Error buscando pedidos", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error buscando pedidos: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
            if (tablePedidosCompra != null) tablePedidosCompra.setDisable(false);
            if (txtSearch != null) txtSearch.setDisable(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
            loading = false;
        });

        new Thread(task, "buscarPedidos-thread").start();
    }

    @FXML
    public void handleNuevo() {
        if (txtNumero != null) txtNumero.clear();
        if (dpFecha != null) dpFecha.setValue(null);
        if (cboProveedor != null) cboProveedor.getSelectionModel().clearSelection();
        if (cboEstado != null) cboEstado.getSelectionModel().clearSelection();
        if (tablePedidosCompra != null) tablePedidosCompra.getSelectionModel().clearSelection();
        // No se crea ningún registro vacío automáticamente.
        // Aquí deberías abrir un formulario para introducir los datos del nuevo pedido y guardarlo si se confirma.
    }

    @FXML
    public void handleGuardar() {
        // En una app real, aquí se validaría la entrada.
        PedidoCompra pedido = new PedidoCompra();
        pedido.setNumero(txtNumero.getText());
        pedido.setFecha(dpFecha.getValue());
        // Asignar el proveedor correctamente (requiere buscar el objeto Proveedor por nombre)
        // pedido.setIdProveedor(buscarProveedorPorNombre(cboProveedor.getValue()));
        pedido.setEstado(cboEstado.getValue());
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

    //refrescar
    @FXML
    public void handleRefrescar(){
        // Limpiar el filtro de búsqueda si existe
        if (txtSearch != null) {
            txtSearch.clear();
        }
        // Limpiar la selección de la tabla si existe
        if (tablePedidosCompra != null) {
            tablePedidosCompra.getSelectionModel().clearSelection();
        }
        // Limpiar campos del formulario si existen
        if (txtNumero != null) {
            txtNumero.clear();
        }
        if (dpFecha != null) {
            dpFecha.setValue(null);
        }
        if (cboProveedor != null) {
            cboProveedor.getSelectionModel().clearSelection();
        }
        if (cboEstado != null) {
            cboEstado.getSelectionModel().clearSelection();
        }

        // Recargar los pedidos (cargarPedidos ya corre en un Task en background)
        cargarPedidos();
    }
}
