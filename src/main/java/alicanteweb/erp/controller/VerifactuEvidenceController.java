package alicanteweb.erp.controller;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.VerifactuEvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
public class VerifactuEvidenceController {

    private final VerifactuEvidenceService evidenceService;

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

    private final ObservableList<VerifactuEvidence> evidenceObservable = FXCollections.observableArrayList();

    public VerifactuEvidenceController(VerifactuEvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (colFacturaId != null) colFacturaId.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getFacturaId()));
        if (colSerie != null) colSerie.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getSerie()));
        if (colNumero != null) colNumero.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getNumero()));
        if (colFechaEmision != null) colFechaEmision.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getFechaEmision() != null ? df.format(e.getValue().getFechaEmision()) : ""));

        if (tableEvidence != null) tableEvidence.setItems(evidenceObservable);

        if (tableEvidence != null) {
            tableEvidence.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtFacturaId != null) txtFacturaId.setText(newSel.getFacturaId());
                    if (txtSerie != null) txtSerie.setText(newSel.getSerie());
                    if (txtNumero != null) txtNumero.setText(newSel.getNumero());
                    if (txtHash != null) txtHash.setText(newSel.getHash());
                    if (txtHashAnterior != null) txtHashAnterior.setText(newSel.getHashAnterior());
                    if (txtCertFingerprint != null) txtCertFingerprint.setText(newSel.getCertFingerprint());
                    if (txtMetadata != null) txtMetadata.setText(newSel.getMetadata());
                } else {
                    if (txtFacturaId != null) txtFacturaId.setText("");
                    if (txtSerie != null) txtSerie.setText("");
                    if (txtNumero != null) txtNumero.setText("");
                    if (txtHash != null) txtHash.setText("");
                    if (txtHashAnterior != null) txtHashAnterior.setText("");
                    if (txtCertFingerprint != null) txtCertFingerprint.setText("");
                    if (txtMetadata != null) txtMetadata.setText("");
                }
            });
        }

        loadAll();
    }

    private void loadAll() {
        evidenceObservable.clear();
        List<VerifactuEvidence> all = evidenceService.findAll();
        evidenceObservable.addAll(all);
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        // Buscar por facturaId o por serie
        evidenceObservable.clear();
        evidenceService.findByFacturaId(q).ifPresent(evidenceObservable::add);
        evidenceObservable.addAll(evidenceService.findBySerie(q));
    }

    @FXML
    public void handleNuevo() {
        if (txtFacturaId != null) txtFacturaId.setText("");
        if (txtSerie != null) txtSerie.setText("");
        if (txtNumero != null) txtNumero.setText("");
        if (txtHash != null) txtHash.setText("");
        if (txtHashAnterior != null) txtHashAnterior.setText("");
        if (txtCertFingerprint != null) txtCertFingerprint.setText("");
        if (txtMetadata != null) txtMetadata.setText("");
        if (tableEvidence != null) tableEvidence.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        VerifactuEvidence v = null;
        if (tableEvidence != null) v = tableEvidence.getSelectionModel().getSelectedItem();
        if (v == null) v = new VerifactuEvidence();

        v.setFacturaId(txtFacturaId != null ? txtFacturaId.getText() : null);
        v.setSerie(txtSerie != null ? txtSerie.getText() : null);
        v.setNumero(txtNumero != null ? txtNumero.getText() : null);
        v.setHash(txtHash != null ? txtHash.getText() : null);
        v.setHashAnterior(txtHashAnterior != null ? txtHashAnterior.getText() : null);
        v.setCertFingerprint(txtCertFingerprint != null ? txtCertFingerprint.getText() : null);
        v.setMetadata(txtMetadata != null ? txtMetadata.getText() : null);

        // unicidad facturaId
        String facturaId = v.getFacturaId();
        if (facturaId != null && !facturaId.isBlank()) {
            Optional<VerifactuEvidence> existe = evidenceService.findByFacturaId(facturaId);
            if (existe.isPresent() && (v.getId() == null || !existe.get().getId().equals(v.getId()))) {
                new Alert(Alert.AlertType.ERROR, "Ya existe evidencia para esta factura").showAndWait();
                return;
            }
        }

        evidenceService.save(v);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        VerifactuEvidence v = null;
        if (tableEvidence != null) v = tableEvidence.getSelectionModel().getSelectedItem();
        if (v == null || v.getId() == null) return;
        evidenceService.deleteById(v.getId());
        loadAll();
    }
}

