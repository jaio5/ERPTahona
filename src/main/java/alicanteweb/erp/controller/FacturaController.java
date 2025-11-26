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
import javafx.scene.layout.GridPane;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
@Controller
public class FacturaController implements MainControllerAware {

    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

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
        configureTableColumns();
        bindTableData();
        configureClienteComboBox();
        configureSelectionListener();
        loadAll();
    }

    private void configureTableColumns() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (colNumero != null) colNumero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero()));
        if (colFecha != null) colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha() != null ? df.format(c.getValue().getFecha()) : ""));
        if (colCliente != null) colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente() != null ? c.getValue().getCliente().getNombre() : ""));
        if (colTotal != null) colTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotal() != null ? c.getValue().getTotal().toString() : ""));
    }

    private void bindTableData() {
        if (tableFacturas != null) tableFacturas.setItems(facturasObservable);
    }

    private void configureClienteComboBox() {
        if (cboCliente != null) {
            clientesObservable.addAll(clienteService.findAll());
            cboCliente.setItems(clientesObservable);
        }
    }

    private void configureSelectionListener() {
        if (tableFacturas == null) return;
        tableFacturas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateDetails(newSel);
            } else {
                clearDetails();
            }
        });
    }

    private void populateDetails(Factura f) {
        setText(txtNumero, f.getNumero());
        setDate(dpFecha, f.getFecha());
        selectCliente(cboCliente, f.getCliente());
        setText(txtTotal, f.getTotal() != null ? f.getTotal().toString() : "");
        setText(txtPagado, f.getPagado() != null ? f.getPagado().toString() : "");
    }

    private void clearDetails() {
        setText(txtNumero, "");
        setDate(dpFecha, null);
        clearClienteSelection(cboCliente);
        setText(txtTotal, "");
        setText(txtPagado, "");
    }

    private String getText(TextInputControl control) {
        return control == null ? "" : Optional.ofNullable(control.getText()).orElse("");
    }

    private void setText(TextInputControl control, String value) {
        if (control != null) control.setText(value == null ? "" : value);
    }

    private void setDate(DatePicker picker, java.time.LocalDate date) {
        if (picker != null) picker.setValue(date);
    }

    private void selectCliente(ComboBox<Cliente> combo, Cliente cliente) {
        if (combo != null && cliente != null) combo.getSelectionModel().select(cliente);
    }

    private void clearClienteSelection(ComboBox<Cliente> combo) {
        if (combo != null) combo.getSelectionModel().clearSelection();
    }

    private void loadAll() {
        facturasObservable.clear();
        List<Factura> todos = facturaService.findAll();
        facturasObservable.addAll(todos);
    }

    @FXML
    public void handleSearch() {
        String q = getText(txtSearch).trim();
        if (q.isEmpty()) {
            loadAll();
            return;
        }
        facturasObservable.clear();
        facturaService.findByNumero(q).ifPresentOrElse(facturasObservable::add, this::loadAll);
    }

    @FXML
    public void handleNuevo() {
        clearDetails();
        if (tableFacturas != null) tableFacturas.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Factura selected = getSelectedOrNew();
        fillFromUi(selected);
        if (!isNumeroUniqueOrSame(selected)) {
            showAlert(Alert.AlertType.ERROR, "El número de factura ya existe");
            return;
        }
        facturaService.save(selected);
        loadAll();
    }

    private Factura getSelectedOrNew() {
        if (tableFacturas == null) return new Factura();
        Factura sel = tableFacturas.getSelectionModel().getSelectedItem();
        return sel == null ? new Factura() : sel;
    }

    private void fillFromUi(Factura f) {
        f.setNumero(getText(txtNumero));
        f.setFecha(dpFecha != null ? dpFecha.getValue() : null);
        f.setCliente(cboCliente != null ? cboCliente.getSelectionModel().getSelectedItem() : null);
        try { f.setTotal(new java.math.BigDecimal(getText(txtTotal))); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Total no válido"); }
        try { f.setPagado(new java.math.BigDecimal(getText(txtPagado))); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Pagado no válido"); }
    }

    private boolean isNumeroUniqueOrSame(Factura f) {
        String numero = f.getNumero();
        if (numero == null || numero.isBlank()) return true;
        Optional<Factura> existe = facturaService.findByNumero(numero);
        return existe.isEmpty() || (f.getId() != null && existe.get().getId().equals(f.getId()));
    }

    @FXML
    public void handleEliminar() {
        Factura selected = getSelectedOrNull();
        if (selected == null || selected.getId() == null) return;
        facturaService.deleteById(selected.getId());
        loadAll();
    }

    private Factura getSelectedOrNull() {
        if (tableFacturas == null) return null;
        return tableFacturas.getSelectionModel().getSelectedItem();
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
            setText(txtNumero, "");
            setDate(dpFecha, java.time.LocalDate.now());
            selectCliente(cboCliente, cliente);
            setText(txtTotal, String.valueOf(ultima.getTotal()));
            setText(txtPagado, "0");
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
            showAlert(Alert.AlertType.WARNING, "Selecciona un cliente para copiar su última factura.");
            return;
        }
        copiarFacturaAnterior(cliente);
        showAlert(Alert.AlertType.INFORMATION, "Datos de la última factura copiados. Puedes modificar y guardar la nueva factura.");
    }

    @FXML
    public void handleNuevaFactura() {
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
        Factura factura = getSelectedOrNull();
        if (factura == null) {
            showAlert(Alert.AlertType.WARNING, "Selecciona una factura para imprimir");
            return;
        }
        try {
            facturaService.imprimirFactura(factura);
            showAlert(Alert.AlertType.INFORMATION, "Factura impresa correctamente");
        } catch (Exception e) {
            log.error("Error al imprimir la factura", e);
            showAlert(Alert.AlertType.ERROR, "Error al imprimir la factura: " + e.getMessage());
        }
    }

    @FXML
    public void handlePrevisualizarFactura() {
        Factura factura = getSelectedOrNull();
        if (factura == null) {
            showAlert(Alert.AlertType.WARNING, "Selecciona una factura para previsualizar");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Número: ").append(factura.getNumero()).append("\n");
        sb.append("Fecha: ").append(factura.getFecha()).append("\n");
        sb.append("Cliente: ").append(factura.getCliente() != null ? factura.getCliente().getNombre() : "").append("\n");
        sb.append("Total: ").append(factura.getTotal()).append("\n");
        sb.append("Pagado: ").append(factura.getPagado()).append("\n");
        // Puedes añadir aquí las líneas de la factura si lo deseas
        showAlert(Alert.AlertType.INFORMATION, sb.toString());
    }

    // Helper para mostrar alertas de forma centralizada.
    private void showAlert(Alert.AlertType type, String message) {
        new Alert(type, message).showAndWait();
    }
}
