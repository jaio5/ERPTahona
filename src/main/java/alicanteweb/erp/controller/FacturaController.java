package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.VerifactuAEATService;
import alicanteweb.erp.service.VerifactuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
@Controller
public class FacturaController implements MainControllerAware {

    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    // ... (servicios y otros campos)
    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final VerifactuService verifactuService;
    private final VerifactuAEATService verifactuAEATService;
    private final ObjectMapper objectMapper;
    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;


    public FacturaController(FacturaService facturaService, ClienteService clienteService, VerifactuService verifactuService, VerifactuAEATService verifactuAEATService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.verifactuService = verifactuService;
        this.verifactuAEATService = verifactuAEATService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // ... (código existente del controlador)
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
        Factura savedFactura = facturaService.save(selected);
        loadAll();
        registrarYEnviarVerifactu(savedFactura);
    }

    private void registrarYEnviarVerifactu(Factura factura) {
        try {
            String facturaPayload = String.format("id=%s,numero=%s,fecha=%s,total=%.2f",
                    factura.getId(), factura.getNumero(), factura.getFecha(), factura.getTotal());

            log.info("Registrando evidencia Verifactu para la factura {}", factura.getNumero());
            VerifactuEvidence evidence = verifactuService.registerEvidence(
                    String.valueOf(factura.getId()),
                    facturaPayload,
                    factura.getNumero(),
                    null,
                    factura.getFecha().atStartOfDay()
            );
            log.info("Evidencia Verifactu registrada localmente con hash: {}", evidence.getHash());

            String evidenciaJson = objectMapper.writeValueAsString(evidence);

            verifactuAEATService.enviarAEAT(evidenciaJson);

            String successMessage;
            if (verifactuAEATService.isAeatEnabled()) {
                successMessage = "Factura guardada, registrada y enviada a la AEAT con éxito.";
            } else {
                successMessage = "Factura guardada y registrada localmente. El envío a la AEAT está desactivado (modo de prueba).";
            }
            showAlert(Alert.AlertType.INFORMATION, successMessage + "\nHash: " + evidence.getHash());

        } catch (IllegalStateException e) {
            log.warn("No se pudo registrar la evidencia Verifactu: {}.", e.getMessage());
            showAlert(Alert.AlertType.WARNING, "Factura guardada, pero no se pudo registrar en Verifactu.\nMotivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error durante el proceso de registro y envío a Verifactu/AEAT", e);
            showAlert(Alert.AlertType.ERROR, "Factura guardada, pero ocurrió un error con Verifactu/AEAT.\nError: " + e.getMessage());
        }
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

    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }

    private void showAlert(Alert.AlertType type, String message) {
        new Alert(type, message).showAndWait();
    }
}
