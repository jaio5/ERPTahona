package alicanteweb.erp.controller;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.VerifactuEvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class VerifactuEvidenceController implements MainControllerAware {

    private static final Logger log = LoggerFactory.getLogger(VerifactuEvidenceController.class);

    /**
     * Controlador FXML que muestra y gestiona las evidencias de Verifactu.
     *
     * He aplicado técnicas de clean code para mantener métodos pequeños y claros,
     * evitar duplicación y encapsular las operaciones repetitivas en helpers.
     */

    private final VerifactuEvidenceService evidenceService;
    private alicanteweb.erp.controller.ui.MainPanelController mainPanelController;

    // Campos FXML (no pueden ser final porque JavaFX los inyecta)
    @FXML
    private TableView<VerifactuEvidence> tableEvidence;

    @FXML
    private TableColumn<VerifactuEvidence, String> colFacturaId;

    @FXML
    private TableColumn<VerifactuEvidence, String> colSerie;

    @FXML
    private TableColumn<VerifactuEvidence, String> colNumero;

    @FXML
    private TableColumn<VerifactuEvidence, String> colFechaEmision;

    @FXML
    private TableColumn<VerifactuEvidence, String> colHash;
    @FXML
    private TableColumn<VerifactuEvidence, String> colHashAnterior;
    @FXML
    private TableColumn<VerifactuEvidence, String> colCertFingerprint;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtFacturaId;

    @FXML
    private TextField txtSerie;

    @FXML
    private TextField txtNumero;

    @FXML
    private TextField txtHash;

    @FXML
    private TextField txtHashAnterior;

    @FXML
    private TextField txtCertFingerprint;

    @FXML
    private TextArea txtMetadata;

    @FXML
    private TextField txtFechaEmision;

    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;

    // ObservableList para conectar la lista de la UI (TableView) con los datos.
    private final ObservableList<VerifactuEvidence> evidenceObservable = FXCollections.observableArrayList();

    // Inyección por constructor: forma recomendada para que Spring pueda proveer el servicio.
    public VerifactuEvidenceController(VerifactuEvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    // Método que permite al MainPanelController pasar una referencia (patrón "aware").
    @Override
    public void setMainPanelController(alicanteweb.erp.controller.ui.MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    /**
     * initialize() es llamado por el FXMLLoader después de que los campos @FXML
     * han sido inyectados. Se usa para configurar columnas, listeners y cargar datos.
     */
    @FXML
    public void initialize() {
        configureTableColumns();
        bindTableData();
        configureSelectionListener();
        loadAll();
    }

    // Configura los cellValueFactory de las columnas (pequeños extractos para claridad).
    private void configureTableColumns() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (colFacturaId != null) colFacturaId.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getFacturaId()));
        if (colSerie != null) colSerie.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getSerie()));
        if (colNumero != null) colNumero.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getNumero()));
        if (colFechaEmision != null) colFechaEmision.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getFechaEmision() != null ? df.format(e.getValue().getFechaEmision()) : ""));
        if (colHash != null) colHash.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getHash()));
        if (colHashAnterior != null) colHashAnterior.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getHashAnterior()));
        if (colCertFingerprint != null) colCertFingerprint.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getCertFingerprint()));
    }

    private void bindTableData() {
        if (tableEvidence != null) tableEvidence.setItems(evidenceObservable);
    }

    private void configureSelectionListener() {
        if (tableEvidence == null) return;
        tableEvidence.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateDetails(newSel);
            } else {
                clearDetails();
            }
        });
    }

    // Rellena los campos de detalle con los datos de la evidencia.
    private void populateDetails(VerifactuEvidence v) {
        setText(txtFacturaId, v.getFacturaId());
        setText(txtSerie, v.getSerie());
        setText(txtNumero, v.getNumero());
        setText(txtHash, v.getHash());
        setText(txtHashAnterior, v.getHashAnterior());
        setText(txtCertFingerprint, v.getCertFingerprint());
        setText(txtMetadata, v.getMetadata());
        setText(txtFechaEmision, v.getFechaEmision() != null ? v.getFechaEmision().toString() : "");
    }

    // Limpia los campos de detalle.
    private void clearDetails() {
        setText(txtFacturaId, "");
        setText(txtSerie, "");
        setText(txtNumero, "");
        setText(txtHash, "");
        setText(txtHashAnterior, "");
        setText(txtCertFingerprint, "");
        setText(txtMetadata, "");
        setText(txtFechaEmision, "");
    }

    // Helpers seguros para leer/escribir TextInputControls sin repetir null-checks.
    private String getText(TextInputControl control) {
        return control == null ? "" : Optional.ofNullable(control.getText()).orElse("");
    }

    private void setText(TextInputControl control, String value) {
        if (control != null) control.setText(value == null ? "" : value);
    }

    // Carga todas las evidencias desde la BD y las añade al ObservableList.
    private void loadAll() {
        evidenceObservable.clear();
        List<VerifactuEvidence> all = evidenceService.findAll();
        evidenceObservable.addAll(all);
    }

    /**
     * Método conectado al botón de búsqueda (onAction) en el FXML.
     * Realiza búsqueda por facturaId o por serie.
     */
    @FXML
    public void handleSearch() {
        String q = getText(txtSearch).trim();
        if (q.isEmpty()) {
            loadAll();
            return;
        }
        performSearch(q);
    }

    // Alias para compatibilidad con FXML que use otro nombre.
    @FXML
    public void handleBuscar() {
        handleSearch();
    }

    // Ejecuta la búsqueda concreta.
    private void performSearch(String q) {
        evidenceObservable.clear();
        evidenceService.findByFacturaId(q).ifPresent(evidenceObservable::add);
        evidenceObservable.addAll(evidenceService.findBySerie(q));
    }

    // Prepara la UI para crear una nueva evidencia vacía.
    @FXML
    public void handleNuevo() {
        tableClearSelection();
        clearDetails();
    }

    private void tableClearSelection() {
        if (tableEvidence != null) tableEvidence.getSelectionModel().clearSelection();
    }

    /**
     * Guarda la evidencia actual (inserta o actualiza). Valida unicidad por facturaId.
     */
    @FXML
    public void handleGuardar() {
        VerifactuEvidence v = getSelectedOrNew();
        fillFromUi(v);
        if (!isFacturaIdUniqueOrSame(v)) {
            showAlert(Alert.AlertType.ERROR, "Ya existe evidencia para esta factura");
            return;
        }
        evidenceService.save(v);
        loadAll();
    }

    // Obtiene la entidad seleccionada en la tabla o crea una nueva.
    private VerifactuEvidence getSelectedOrNew() {
        if (tableEvidence == null) return new VerifactuEvidence();
        VerifactuEvidence sel = tableEvidence.getSelectionModel().getSelectedItem();
        return sel == null ? new VerifactuEvidence() : sel;
    }

    // Rellena la entidad con datos de la UI.
    private void fillFromUi(VerifactuEvidence v) {
        v.setFacturaId(getText(txtFacturaId));
        v.setSerie(getText(txtSerie));
        v.setNumero(getText(txtNumero));
        v.setHash(getText(txtHash));
        v.setHashAnterior(getText(txtHashAnterior));
        v.setCertFingerprint(getText(txtCertFingerprint));
        v.setMetadata(getText(txtMetadata));
    }

    // Valida unicidad por facturaId: si ya existe otra entidad con el mismo facturaId, retorna false.
    private boolean isFacturaIdUniqueOrSame(VerifactuEvidence v) {
        String facturaId = v.getFacturaId();
        if (facturaId == null || facturaId.isBlank()) return true;
        Optional<VerifactuEvidence> existe = evidenceService.findByFacturaId(facturaId);
        return existe.isEmpty() || (v.getId() != null && existe.get().getId().equals(v.getId()));
    }

    // Elimina la evidencia seleccionada.
    @FXML
    public void handleEliminar() {
        VerifactuEvidence v = getSelectedOrNull();
        if (v == null || v.getId() == null) return;
        evidenceService.deleteById(v.getId());
        loadAll();
    }

    private VerifactuEvidence getSelectedOrNull() {
        if (tableEvidence == null) return null;
        return tableEvidence.getSelectionModel().getSelectedItem();
    }

    // Vuelve a la vista principal del panel (usando la referencia pasada desde MainPanelController).
    @FXML
    public void handleVolver() {
        if (mainPanelController != null) mainPanelController.showHome();
    }

    /**
     * Método que invoca la lógica para registrar la evidencia y enviarla a la AEAT.
     * - En esta implementación, el envío se simula en `VerifactuAEATService`.
     * - El método muestra alertas con el resultado o el error.
     */
    @FXML
    public void handleRegistrarAEAT() {
        try {
            String datosFactura = getText(txtFacturaId);
            String serie = getText(txtSerie);
            String numero = getText(txtNumero);
            evidenceService.registrarEvidenciaAEAT(datosFactura, serie, numero);
            loadAll();
            showAlert(Alert.AlertType.INFORMATION, "Evidencia registrada y enviada a la AEAT correctamente.");
        } catch (Exception ex) {
            log.error("Error al registrar evidencia AEAT", ex);
            showAlert(Alert.AlertType.ERROR, "Error al registrar evidencia: " + ex.getMessage());
        }
    }

    // Helper para mostrar alertas de forma centralizada.
    private void showAlert(Alert.AlertType type, String message) {
        new Alert(type, message).showAndWait();
    }
}
