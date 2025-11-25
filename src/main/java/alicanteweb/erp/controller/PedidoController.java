package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.PedidoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class PedidoController implements MainControllerAware {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;

    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @FXML
    private TableView<Pedido> tablePedidos;

    @FXML
    private TableColumn<Pedido, String> colNumero;

    @FXML
    private TableColumn<Pedido, String> colFecha;

    @FXML
    private TableColumn<Pedido, String> colCliente;

    @FXML
    private TableColumn<Pedido, String> colEstado;

    @FXML
    private ComboBox<Cliente> cboCliente;

    @FXML
    private TextField txtNumero;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<String> cboEstado;

    @FXML
    private TextField txtSearch;

    private final ObservableList<Pedido> pedidosObservable = FXCollections.observableArrayList();
    private final ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList();

    public PedidoController(PedidoService pedidoService, ClienteService clienteService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        if (tablePedidos != null) tablePedidos.setItems(pedidosObservable);
        if (cboCliente != null) {
            clientesObservable.addAll(clienteService.findAll());
            cboCliente.setItems(clientesObservable);
        }
        if (cboEstado != null) {
            cboEstado.getItems().addAll("Pendiente", "Enviado", "Recibido", "Cancelado");
        }

        if (colNumero != null) colNumero.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNumero()));
        if (colFecha != null) colFecha.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFecha() != null ? p.getValue().getFecha().toString() : ""));
        if (colCliente != null) colCliente.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCliente() != null ? p.getValue().getCliente().getNombre() : ""));
        if (colEstado != null) colEstado.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getEstado()));

        if (tablePedidos != null) {
            tablePedidos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtNumero != null) txtNumero.setText(newSel.getNumero() != null ? newSel.getNumero() : "");
                    if (dpFecha != null) dpFecha.setValue(newSel.getFecha());
                    if (cboCliente != null && newSel.getCliente() != null) cboCliente.getSelectionModel().select(newSel.getCliente());
                    if (cboEstado != null) cboEstado.getSelectionModel().select(newSel.getEstado());
                } else {
                    if (txtNumero != null) txtNumero.setText("");
                    if (dpFecha != null) dpFecha.setValue(LocalDate.now());
                    if (cboCliente != null) cboCliente.getSelectionModel().clearSelection();
                    if (cboEstado != null) cboEstado.getSelectionModel().clearSelection();
                }
            });
        }

        loadAll();
    }

    private void loadAll() {
        pedidosObservable.clear();
        List<Pedido> all = pedidoService.findAll();
        pedidosObservable.addAll(all);
    }

    @FXML
    public void handleNuevo() {
        if (txtNumero != null) txtNumero.setText("");
        if (dpFecha != null) dpFecha.setValue(LocalDate.now());
        if (cboCliente != null) cboCliente.getSelectionModel().clearSelection();
        if (cboEstado != null) cboEstado.getSelectionModel().select("Pendiente");
        if (tablePedidos != null) tablePedidos.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Pedido selected = null;
        if (tablePedidos != null) selected = tablePedidos.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Pedido();

        selected.setNumero(txtNumero != null ? txtNumero.getText() : null);
        selected.setFecha(dpFecha != null ? dpFecha.getValue() : null);
        selected.setCliente(cboCliente != null ? cboCliente.getSelectionModel().getSelectedItem() : null);
        selected.setEstado(cboEstado != null ? cboEstado.getSelectionModel().getSelectedItem() : null);

        // unicidad de numero
        String numero = selected.getNumero();
        if (numero != null && !numero.isBlank()) {
            Optional<Pedido> existe = pedidoService.findByNumero(numero);
            if (existe.isPresent()) {
                Pedido p = existe.get();
                if (selected.getId() == null || !p.getId().equals(selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "El número de pedido ya existe").showAndWait();
                    return;
                }
            }
        }

        pedidoService.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        Pedido selected = null;
        if (tablePedidos != null) selected = tablePedidos.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        if (selected.getPedidoLineas() != null && !selected.getPedidoLineas().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No se puede eliminar: existen líneas de pedido asociadas").showAndWait();
            return;
        }
        pedidoService.deleteById(selected.getId());
        loadAll();
    }

    @FXML
    public void handleSearch() {
        // Implementación básica de búsqueda
        String search = txtSearch.getText();
        List<Pedido> pedidos;
        if (search == null || search.isBlank()) {
            pedidos = pedidoService.findAll();
        } else {
            pedidos = pedidoService.findByNumeroOrCliente(search);
        }
        tablePedidos.setItems(FXCollections.observableArrayList(pedidos));
    }

    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }
}
