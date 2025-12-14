//Controlador para ver las facturas emitidas y registradas en Verifactu

package alicanteweb.erp.controller;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.VerifactuEvidenceService;
import alicanteweb.erp.service.VerifactuService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
public class VerifactuController {
    private static final Logger log = LoggerFactory.getLogger(VerifactuController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<VerifactuEvidence> tableEvidencias;
    @FXML private TableColumn<VerifactuEvidence, Long> colId;
    @FXML private TableColumn<VerifactuEvidence, String> colFactura;
    @FXML private TableColumn<VerifactuEvidence, String> colFecha;
    @FXML private TableColumn<VerifactuEvidence, String> colEstado;
    @FXML private TableColumn<VerifactuEvidence, String> colHash;

    @FXML private Label lblTotalVerificaciones;
    @FXML private Label lblPendientes;
    @FXML private Label lblErrores;
    @FXML private Label lblEstadoSistema;

    @FXML private TextArea txtDetalles;
    @FXML private Button btnReenviar;
    @FXML private Button btnVerificar;
    @FXML private Button btnExportar;
    @FXML private Button btnValidarCadena;

    private final VerifactuEvidenceService verifactuEvidenceService;
    private final VerifactuService verifactuService;
    private final ObservableList<VerifactuEvidence> evidenciasList = FXCollections.observableArrayList();

    public VerifactuController(VerifactuEvidenceService verifactuEvidenceService, VerifactuService verifactuService) {
        this.verifactuEvidenceService = verifactuEvidenceService;
        this.verifactuService = verifactuService;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        configurarListeners();
        cargarEstadoSistema();
        loadAll();
        updateStats();
    }

    private void configurarTabla() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(
                cell.getValue() == null ? null : cell.getValue().getId()));
        }

        if (colFactura != null) {
            colFactura.setCellValueFactory(cell -> {
                if (cell.getValue() == null) return new SimpleStringProperty("");
                String serie = cell.getValue().getSerie() != null ? cell.getValue().getSerie() : "";
                String numero = cell.getValue().getNumero() != null ? cell.getValue().getNumero() : "";
                return new SimpleStringProperty(serie + "/" + numero);
            });
        }

        if (colFecha != null) {
            colFecha.setCellValueFactory(cell -> {
                if (cell.getValue() == null || cell.getValue().getFechaEmision() == null) {
                    return new SimpleStringProperty("");
                }
                return new SimpleStringProperty(
                    DATE_FORMATTER.format(cell.getValue().getFechaEmision().atZone(java.time.ZoneId.systemDefault())));
            });
        }

        if (colEstado != null) {
            colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getEstado()).orElse("N/A")));

            // Colorear estados
            colEstado.setCellFactory(column -> new TableCell<VerifactuEvidence, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        switch (item) {
                            case "ENVIADO", "VERIFICADO" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            case "ERROR" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            case "PENDIENTE" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            default -> setStyle("");
                        }
                    }
                }
            });
        }

        if (colHash != null) {
            colHash.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getHash()).orElse("")));
        }

        if (tableEvidencias != null) {
            tableEvidencias.setItems(evidenciasList);
        }
    }

    private void configurarListeners() {
        if (tableEvidencias != null) {
            tableEvidencias.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        mostrarDetalles(newSelection);
                        actualizarBotonesAccion(newSelection);
                    }
                }
            );
        }
    }

    private void cargarEstadoSistema() {
        if (lblEstadoSistema != null) {
            boolean enabled = verifactuService.isEnabled();
            lblEstadoSistema.setText(enabled ? "✓ Sistema activo" : "⚠ Sistema deshabilitado");
            lblEstadoSistema.setStyle(enabled ?
                "-fx-text-fill: green; -fx-font-weight: bold;" :
                "-fx-text-fill: orange; -fx-font-weight: bold;");
        }
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
            long pendientes = verifactuEvidenceService.countByEstado("PENDIENTE");
            long errores = verifactuEvidenceService.countByEstado("ERROR");

            if (lblTotalVerificaciones != null) lblTotalVerificaciones.setText(String.valueOf(total));
            if (lblPendientes != null) lblPendientes.setText(String.valueOf(pendientes));
            if (lblErrores != null) lblErrores.setText(String.valueOf(errores));
        } catch (Exception e) {
            log.error("Error actualizando estadísticas", e);
        }
    }

    private void mostrarDetalles(VerifactuEvidence evidencia) {
        if (txtDetalles == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("  DETALLES DE LA EVIDENCIA\n");
        sb.append("═══════════════════════════════════════════\n\n");

        sb.append("ID: ").append(evidencia.getId()).append("\n");
        sb.append("Factura: ").append(evidencia.getSerie()).append("/").append(evidencia.getNumero()).append("\n");
        sb.append("Estado: ").append(evidencia.getEstado() != null ? evidencia.getEstado() : "N/A").append("\n");
        sb.append("Fecha Emisión: ").append(evidencia.getFechaEmision() != null ?
            DATE_FORMATTER.format(evidencia.getFechaEmision().atZone(java.time.ZoneId.systemDefault())) : "N/A").append("\n");
        sb.append("Fecha Envío: ").append(evidencia.getFechaEnvio() != null ?
            DATE_FORMATTER.format(evidencia.getFechaEnvio().atZone(java.time.ZoneId.systemDefault())) : "N/A").append("\n");
        sb.append("\n");

        sb.append("Hash: ").append(evidencia.getHash() != null ? evidencia.getHash() : "N/A").append("\n");
        sb.append("Hash Anterior: ").append(evidencia.getHashAnterior() != null ? evidencia.getHashAnterior() : "N/A").append("\n");
        sb.append("\n");

        sb.append("Certificado Fingerprint: ").append(evidencia.getCertFingerprint() != null ?
            evidencia.getCertFingerprint() : "N/A").append("\n");
        sb.append("\n");

        if (evidencia.getSignature() != null) {
            sb.append("Firma (Base64): ").append(Base64.getEncoder().encodeToString(evidencia.getSignature())
                .substring(0, Math.min(100, Base64.getEncoder().encodeToString(evidencia.getSignature()).length())))
                .append("...\n");
        }
        sb.append("\n");

        if (evidencia.getErrorMessage() != null) {
            sb.append("Error: ").append(evidencia.getErrorMessage()).append("\n\n");
        }

        if (evidencia.getMetadata() != null && !evidencia.getMetadata().isEmpty()) {
            sb.append("Metadata:\n");
            evidencia.getMetadata().forEach((key, value) ->
                sb.append("  - ").append(key).append(": ").append(value).append("\n"));
        }

        txtDetalles.setText(sb.toString());
    }

    private void actualizarBotonesAccion(VerifactuEvidence evidencia) {
        if (btnReenviar != null) {
            btnReenviar.setDisable(evidencia.getEstado() == null ||
                evidencia.getEstado().equals("ENVIADO") || evidencia.getEstado().equals("VERIFICADO"));
        }
        if (btnVerificar != null) {
            btnVerificar.setDisable(evidencia.getEstado() == null || evidencia.getEstado().equals("PENDIENTE"));
        }
    }

    @FXML
    public void onRefresh() {
        loadAll();
        mostrarInfo("Datos actualizados correctamente");
    }

    @FXML
    public void onReenviar() {
        VerifactuEvidence selected = tableEvidencias.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarError("Selecciona una evidencia para reenviar");
            return;
        }

        if (selected.getEstado() != null && (selected.getEstado().equals("ENVIADO") || selected.getEstado().equals("VERIFICADO"))) {
            mostrarError("Esta evidencia ya fue enviada exitosamente");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Reenvío");
        confirmDialog.setHeaderText("¿Reenviar evidencia a AEAT?");
        confirmDialog.setContentText("Factura: " + selected.getSerie() + "/" + selected.getNumero());

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    verifactuEvidenceService.reenviarEvidencia(selected.getId());
                    mostrarInfo("Evidencia reenviada correctamente");
                    loadAll();
                } catch (Exception e) {
                    log.error("Error reenviando evidencia", e);
                    mostrarError("Error reenviando evidencia: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onVerificar() {
        VerifactuEvidence selected = tableEvidencias.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarError("Selecciona una evidencia para verificar");
            return;
        }

        try {
            verifactuEvidenceService.verificarEstadoAEAT(selected.getId());
            mostrarInfo("Estado verificado en AEAT");
            loadAll();
        } catch (Exception e) {
            log.error("Error verificando estado en AEAT", e);
            mostrarError("Error verificando estado: " + e.getMessage());
        }
    }

    @FXML
    public void onExportar() {
        if (evidenciasList.isEmpty()) {
            mostrarError("No hay evidencias para exportar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Evidencias");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"));
        fileChooser.setInitialFileName("evidencias_verifactu.csv");

        File file = fileChooser.showSaveDialog(tableEvidencias.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Cabecera
                writer.write("ID,Serie,Numero,Fecha,Estado,Hash,HashAnterior\n");

                // Datos
                for (VerifactuEvidence ev : evidenciasList) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                        ev.getId(),
                        ev.getSerie() != null ? ev.getSerie() : "",
                        ev.getNumero() != null ? ev.getNumero() : "",
                        ev.getFechaEmision() != null ? ev.getFechaEmision().toString() : "",
                        ev.getEstado() != null ? ev.getEstado() : "",
                        ev.getHash() != null ? ev.getHash() : "",
                        ev.getHashAnterior() != null ? ev.getHashAnterior() : ""
                    ));
                }

                mostrarInfo("Evidencias exportadas correctamente a: " + file.getName());
            } catch (Exception e) {
                log.error("Error exportando evidencias", e);
                mostrarError("Error exportando: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onValidarCadena() {
        // Obtener series únicas
        var series = evidenciasList.stream()
            .map(VerifactuEvidence::getSerie)
            .filter(s -> s != null && !s.isEmpty())
            .distinct()
            .toList();

        if (series.isEmpty()) {
            mostrarError("No hay evidencias para validar");
            return;
        }

        StringBuilder resultado = new StringBuilder();
        resultado.append("VALIDACIÓN DE CADENA DE BLOQUES\n\n");

        for (String serie : series) {
            boolean valida = verifactuEvidenceService.validarCadenaIntegridad(serie);
            resultado.append("Serie ").append(serie).append(": ")
                .append(valida ? "✓ VÁLIDA" : "✗ ROTA").append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Validación de Cadena");
        alert.setHeaderText("Resultado de Validación");
        alert.setContentText(resultado.toString());
        alert.showAndWait();
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
