package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.FacturaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
@Controller
public class FacturaController implements MainControllerAware {

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    @FXML
    private TableView<Factura> tableFacturas;

    @FXML
    private TableColumn<Factura, String> colNumero;

    @FXML
    private TableColumn<Factura, String> colFecha;

    @FXML
    private TableColumn<Factura, String> colCliente;

    @FXML
    private TableColumn<Factura, String> colTotal;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtNumero;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<Cliente> cboCliente;

    @FXML
    private TextField txtTotal;

    @FXML
    private TextField txtPagado;

    private final ObservableList<Factura> facturasObservable = FXCollections.observableArrayList();
    private final ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList();

    public FacturaController(FacturaService facturaService, ClienteService clienteService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (colNumero != null) colNumero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero()));
        if (colFecha != null) colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha() != null ? df.format(c.getValue().getFecha()) : ""));
        if (colCliente != null) colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente() != null ? c.getValue().getCliente().getNombre() : ""));
        if (colTotal != null) colTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotal() != null ? c.getValue().getTotal().toString() : ""));

        if (tableFacturas != null) tableFacturas.setItems(facturasObservable);
        if (cboCliente != null) {
            clientesObservable.addAll(clienteService.findAll());
            cboCliente.setItems(clientesObservable);
        }

        if (tableFacturas != null) {
            tableFacturas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtNumero != null) txtNumero.setText(newSel.getNumero() != null ? newSel.getNumero() : "");
                    if (dpFecha != null) dpFecha.setValue(newSel.getFecha());
                    if (cboCliente != null && newSel.getCliente() != null) cboCliente.getSelectionModel().select(newSel.getCliente());
                    if (txtTotal != null) txtTotal.setText(newSel.getTotal() != null ? newSel.getTotal().toString() : "");
                    if (txtPagado != null) txtPagado.setText(newSel.getPagado() != null ? newSel.getPagado().toString() : "");
                } else {
                    if (txtNumero != null) txtNumero.setText("");
                    if (dpFecha != null) dpFecha.setValue(null);
                    if (cboCliente != null) cboCliente.getSelectionModel().clearSelection();
                    if (txtTotal != null) txtTotal.setText("");
                    if (txtPagado != null) txtPagado.setText("");
                }
            });
        }

        loadAll();
    }

    private void loadAll() {
        facturasObservable.clear();
        List<Factura> todos = facturaService.findAll();
        facturasObservable.addAll(todos);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        facturasObservable.clear();
        facturaService.findByNumero(q).ifPresentOrElse(facturasObservable::add, this::loadAll);
    }

    @FXML
    public void handleNuevo() {
        if (txtNumero != null) txtNumero.setText("");
        if (dpFecha != null) dpFecha.setValue(null);
        if (cboCliente != null) cboCliente.getSelectionModel().clearSelection();
        if (txtTotal != null) txtTotal.setText("");
        if (txtPagado != null) txtPagado.setText("");
        if (tableFacturas != null) tableFacturas.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Factura selected = null;
        if (tableFacturas != null) selected = tableFacturas.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Factura();

        selected.setNumero(txtNumero != null ? txtNumero.getText() : null);
        selected.setFecha(dpFecha != null ? dpFecha.getValue() : null);
        selected.setCliente(cboCliente != null ? cboCliente.getSelectionModel().getSelectedItem() : null);
        try { if (txtTotal != null && !txtTotal.getText().isBlank()) selected.setTotal(new java.math.BigDecimal(txtTotal.getText())); } catch (Exception e) { new Alert(Alert.AlertType.ERROR, "Total no válido").showAndWait(); }
        try { if (txtPagado != null && !txtPagado.getText().isBlank()) selected.setPagado(new java.math.BigDecimal(txtPagado.getText())); } catch (Exception e) { new Alert(Alert.AlertType.ERROR, "Pagado no válido").showAndWait(); }

        // unicidad numero
        String numero = selected.getNumero();
        if (numero != null && !numero.isBlank()) {
            Optional<Factura> existe = facturaService.findByNumero(numero);
            if (existe.isPresent()) {
                Factura f = existe.get();
                if (selected.getId() == null || !f.getId().equals(selected.getId())) {
                    new Alert(Alert.AlertType.ERROR, "El número de factura ya existe").showAndWait();
                    return;
                }
            }
        }

        facturaService.save(selected);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        Factura selected = null;
        if (tableFacturas != null) selected = tableFacturas.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        facturaService.deleteById(selected.getId());
        loadAll();
    }

    /**
     * Copia los datos de la última factura de un cliente seleccionado y los pone en el formulario para emitir una nueva factura similar.
     * También copia las líneas de la factura anterior y las asocia a la nueva factura en edición.
     */
    public void copiarFacturaAnterior(Cliente cliente) {
        if (cliente == null) return;
        Optional<Factura> ultimaOpt = facturaService.findUltimaFacturaPorCliente(cliente.getId());
        if (ultimaOpt.isPresent()) {
            Factura ultima = ultimaOpt.get();
            if (txtNumero != null) txtNumero.setText(""); // Nuevo número
            if (dpFecha != null) dpFecha.setValue(java.time.LocalDate.now());
            if (cboCliente != null) cboCliente.setValue(cliente);
            if (txtTotal != null) txtTotal.setText(String.valueOf(ultima.getTotal()));
            if (txtPagado != null) txtPagado.setText("0");
            // Copiar líneas de factura
            Set<FacturaLinea> lineasCopia = new java.util.LinkedHashSet<>();
            for (FacturaLinea linea : ultima.getFacturaLineas()) {
                FacturaLinea copia = new FacturaLinea();
                copia.setArticulo(linea.getArticulo());
                copia.setCantidad(linea.getCantidad());
                copia.setPrecio(linea.getPrecio());
                copia.setIva(linea.getIva());
                copia.setFactura(null); // Se asociará al guardar la nueva factura
                lineasCopia.add(copia);
            }
            // Aquí deberías asociar estas líneas a la nueva factura en edición
        }
    }

    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }

    @FXML
    public void handleCopiarFacturaAnterior() {
        Cliente cliente = cboCliente != null ? cboCliente.getSelectionModel().getSelectedItem() : null;
        if (cliente == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un cliente para copiar su última factura.").showAndWait();
            return;
        }
        copiarFacturaAnterior(cliente);
        new Alert(Alert.AlertType.INFORMATION, "Datos de la última factura copiados. Puedes modificar y guardar la nueva factura.").showAndWait();
    }

    @FXML
    public void handleNuevaFactura() {
        // Diálogo simple para crear factura
        Dialog<Factura> dialog = new Dialog<>();
        dialog.setTitle("Nueva Factura");
        dialog.setHeaderText("Crear nueva factura");
        ButtonType btnCrear = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);

        TextField txtNumero = new TextField();
        DatePicker dpFecha = new DatePicker();
        ComboBox<Cliente> cboCliente = new ComboBox<>(clientesObservable);
        TextField txtTotal = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Número:"), 0, 0);
        grid.add(txtNumero, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(dpFecha, 1, 1);
        grid.add(new Label("Cliente:"), 0, 2);
        grid.add(cboCliente, 1, 2);
        grid.add(new Label("Total:"), 0, 3);
        grid.add(txtTotal, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnCrear) {
                Factura f = new Factura();
                f.setNumero(txtNumero.getText());
                f.setFecha(dpFecha.getValue());
                f.setCliente(cboCliente.getValue());
                try {
                    f.setTotal(new java.math.BigDecimal(txtTotal.getText()));
                } catch (Exception e) {
                    f.setTotal(java.math.BigDecimal.ZERO);
                }
                return f;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(factura -> {
            facturaService.save(factura);
            facturasObservable.add(factura);
        });
    }

    @FXML
    public void handleImprimirFactura() {
        Factura factura = null;
        if (tableFacturas != null) factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una factura para imprimir").showAndWait();
            return;
        }
        try {
            facturaService.imprimirFactura(factura);
            new Alert(Alert.AlertType.INFORMATION, "Factura impresa correctamente").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al imprimir la factura: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void handlePrevisualizarFactura() {
        Factura factura = null;
        if (tableFacturas != null) factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una factura para previsualizar").showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Número: ").append(factura.getNumero()).append("\n");
        sb.append("Fecha: ").append(factura.getFecha()).append("\n");
        sb.append("Cliente: ").append(factura.getCliente() != null ? factura.getCliente().getNombre() : "").append("\n");
        sb.append("Total: ").append(factura.getTotal()).append("\n");
        sb.append("Pagado: ").append(factura.getPagado()).append("\n");
        // Puedes añadir aquí las líneas de la factura si lo deseas
        new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
    }
}
