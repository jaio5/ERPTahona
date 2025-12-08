//nos servirá para ver las facturas emitidas y registradas en Verifactu

package alicanteweb.erp.controller;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.VerifactuEvidenceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller
public class VerifactuController {
    private static final Logger log = LoggerFactory.getLogger(VerifactuController.class);

    @FXML private TableView<VerifactuEvidence> tableEvidencias;
    @FXML private TableColumn<VerifactuEvidence, Long> colId;
    @FXML private TableColumn<VerifactuEvidence, String> colFactura;
    @FXML private TableColumn<VerifactuEvidence, String> colFecha;
    @FXML private TableColumn<VerifactuEvidence, String> colEstado;
    @FXML private TableColumn<VerifactuEvidence, String> colHash;

    @FXML private Label lblTotalVerificaciones;
    @FXML private Label lblPendientes;
    @FXML private Label lblErrores;

    private final VerifactuEvidenceService verifactuEvidenceService;
    private final ObservableList<VerifactuEvidence> evidenciasList = FXCollections.observableArrayList();

    public VerifactuController(VerifactuEvidenceService verifactuEvidenceService) {
        this.verifactuEvidenceService = verifactuEvidenceService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colFactura != null) colFactura.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).orElse("")));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getFechaEmision() == null ? "" : cell.getValue().getFechaEmision().toString()));
        if (colEstado != null) colEstado.setCellValueFactory(cell -> new SimpleStringProperty("")); // Estado no disponible en la entidad
        if (colHash != null) colHash.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getHash()).orElse("")));

        if (tableEvidencias != null) tableEvidencias.setItems(evidenciasList);
        loadAll();
        updateStats();
    }

    private void loadAll() {
        try {
            List<VerifactuEvidence> todas = verifactuEvidenceService.findAll();
            evidenciasList.setAll(todas);
            updateStats();
        } catch (Exception e) {
            log.error("Error cargando evidencias", e);
            mostrarError("Error cargando evidencias: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            long total = evidenciasList.size();
            // Por ahora mostramos 0 en pendientes y errores ya que no hay campo de estado
            long pendientes = 0;
            long errores = 0;

            if (lblTotalVerificaciones != null) lblTotalVerificaciones.setText(String.valueOf(total));
            if (lblPendientes != null) lblPendientes.setText(String.valueOf(pendientes));
            if (lblErrores != null) lblErrores.setText(String.valueOf(errores));
        } catch (Exception e) {
            log.error("Error actualizando estadísticas", e);
        }
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.setHeaderText("Información");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}
