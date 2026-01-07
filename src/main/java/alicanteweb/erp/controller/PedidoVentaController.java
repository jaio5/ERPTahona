package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.service.PedidoVentaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Controller
public class PedidoVentaController {
    private static final Logger log = LoggerFactory.getLogger(PedidoVentaController.class);

    @FXML private TableView<Pedido> tablePedidos;
    @FXML private TableColumn<Pedido, Long> colId;
    @FXML private TableColumn<Pedido, String> colNumero;
    @FXML private TableColumn<Pedido, String> colFecha;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private TextField txtBuscar;

    private final PedidoVentaService pedidoVentaService;
    private final ObservableList<Pedido> pedidosList = FXCollections.observableArrayList();

    public PedidoVentaController(PedidoVentaService pedidoVentaService) {
        this.pedidoVentaService = pedidoVentaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PedidoVentaController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarPedidos(newV));
        }
    }

    private void configurarColumnas() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        }
        if (colNumero != null) {
            colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).map(Object::toString).orElse("")));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFecha()).map(Object::toString).orElse("")));
        }
        if (colCliente != null) {
            colCliente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCliente()).map(c -> c.getNombre()).orElse("")));
        }
        if (colEstado != null) {
            colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getEstado()).orElse("")));
        }
        if (tablePedidos != null) {
            tablePedidos.setItems(pedidosList);
        }
    }

    private void cargarDatos() {
        try {
            pedidosList.clear();
            pedidosList.addAll(pedidoVentaService.obtenerTodos());
            log.info("Pedidos de venta cargados: {}", pedidosList.size());
            javafx.application.Platform.runLater(() -> {
                if (tablePedidos != null) tablePedidos.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando pedidos", e);
            mostrarError("Error al cargar pedidos: " + e.getMessage());
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
        mostrarAlerta("Crear nuevo pedido de venta en desarrollo");
    }

    @FXML
    public void onEditar() {
        Pedido pedido = tablePedidos.getSelectionModel().getSelectedItem();
        if (pedido == null) {
            mostrarAlerta("Seleccione un pedido para editar");
            return;
        }
        mostrarAlerta("Edición de pedido en desarrollo");
    }

    @FXML
    public void onEliminar() {
        Pedido pedido = tablePedidos.getSelectionModel().getSelectedItem();
        if (pedido == null) {
            mostrarAlerta("Seleccione un pedido para eliminar");
            return;
        }
        if (mostrarConfirmacion("¿Desea eliminar este pedido?")) {
            try {
                pedidoVentaService.eliminar(pedido.getId());
                cargarDatos();
                mostrarExito("Pedido eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando pedido", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
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

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

