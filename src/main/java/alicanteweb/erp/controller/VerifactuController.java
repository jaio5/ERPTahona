package alicanteweb.erp.controller;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.FacturacionEventoService;
import alicanteweb.erp.service.FiscalComplianceService;
import alicanteweb.erp.service.VerifactuEvidenceService;
import alicanteweb.erp.service.VerifactuService;
import alicanteweb.erp.ui.DialogUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VerifactuController {
    private static final Logger log = LoggerFactory.getLogger(VerifactuController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblEstado;
    @FXML private CheckBox chkHabilitado;
    @FXML private TextField txtNif;
    @FXML private Label lblInfo;
    @FXML private Button btnEnviar;
    @FXML private Button btnGuardar;
    @FXML private Button btnProbar;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TableView<VerifactuEvidence> tableEnvios;
    @FXML private TableColumn<VerifactuEvidence, String> colFecha;
    @FXML private TableColumn<VerifactuEvidence, String> colFactura;
    @FXML private TableColumn<VerifactuEvidence, String> colCliente;
    @FXML private TableColumn<VerifactuEvidence, String> colImporte;
    @FXML private TableColumn<VerifactuEvidence, String> colEstado;
    @FXML private TableColumn<VerifactuEvidence, String> colReferencia;
    @FXML private TableColumn<VerifactuEvidence, String> colMensaje;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final VerifactuService verifactuService;
    private final FacturaService facturaService;
    private final EmpresaConfigService empresaConfigService;
    private final VerifactuEvidenceService evidenceService;
    private final FiscalComplianceService fiscalComplianceService;
    private final FacturacionEventoService facturacionEventoService;
    private final ObservableList<VerifactuEvidence> listaEnvios = FXCollections.observableArrayList();
    private boolean verifactuHabilitado = false;

    public VerifactuController(VerifactuService verifactuService,
                               FacturaService facturaService,
                               EmpresaConfigService empresaConfigService,
                               VerifactuEvidenceService evidenceService,
                               FiscalComplianceService fiscalComplianceService,
                               FacturacionEventoService facturacionEventoService) {
        this.verifactuService = verifactuService;
        this.facturaService = facturaService;
        this.empresaConfigService = empresaConfigService;
        this.evidenceService = evidenceService;
        this.fiscalComplianceService = fiscalComplianceService;
        this.facturacionEventoService = facturacionEventoService;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldValue, newValue) -> filtrarEnvios(newValue));
        }
        cargarConfiguracion();
        cargarDatos();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(cellData -> {
            VerifactuEvidence evidencia = cellData.getValue();
            String fecha = "-";
            if (evidencia.getFechaGeneracionRegistro() != null) {
                fecha = DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(evidencia.getFechaGeneracionRegistro());
            } else if (evidencia.getFechaEnvio() != null) {
                fecha = DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(evidencia.getFechaEnvio());
            } else if (evidencia.getFechaEmision() != null) {
                fecha = DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(evidencia.getFechaEmision());
            }
            return new SimpleStringProperty(fecha);
        });

        colFactura.setCellValueFactory(cellData -> new SimpleStringProperty(valor(cellData.getValue().getNumero())));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(valor(cellData.getValue().getEstado())));
        colReferencia.setCellValueFactory(cellData -> new SimpleStringProperty(referencia(cellData.getValue())));
        colMensaje.setCellValueFactory(cellData -> new SimpleStringProperty(valor(cellData.getValue().getErrorMessage())));
        colCliente.setCellValueFactory(cellData -> new SimpleStringProperty(cliente(cellData.getValue())));
        colImporte.setCellValueFactory(cellData -> new SimpleStringProperty(importe(cellData.getValue())));
        tableEnvios.setItems(listaEnvios);
    }

    private void cargarConfiguracion() {
        try {
            empresaConfigService.getConfiguracionActiva().ifPresent(cfg -> {
                verifactuHabilitado = Boolean.TRUE.equals(cfg.getVerifactuHabilitado());
                if (chkHabilitado != null) chkHabilitado.setSelected(verifactuHabilitado);
                if (txtNif != null && cfg.getVerifactuNifEmisor() != null) txtNif.setText(cfg.getVerifactuNifEmisor());
            });
        } catch (Exception e) {
            log.warn("No se pudo cargar configuracion de empresa para Verifactu: {}", e.getMessage());
        }
        actualizarEstado();
    }

    private void actualizarEstado() {
        if (lblEstado == null) return;
        FiscalComplianceService.FiscalComplianceReport report = fiscalComplianceService.diagnosticar();
        if (report.listoProduccion()) {
            lblEstado.setText("Listo para emitir - " + report.modalidad());
            lblEstado.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
        } else if (verifactuHabilitado) {
            lblEstado.setText("Pendiente de compliance");
            lblEstado.setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
        } else {
            lblEstado.setText("Deshabilitado");
            lblEstado.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
    }

    private void cargarDatos() {
        try {
            listaEnvios.setAll(evidenceService.findAll());
            long pendientes = facturaService.findAll().stream()
                .filter(f -> "REVISION".equalsIgnoreCase(f.getEstado()) && !Boolean.TRUE.equals(f.getVerifactuEnviada()))
                .count();
            if (lblTotal != null) lblTotal.setText(listaEnvios.size() + " registros");
            if (lblInfo != null) lblInfo.setText(pendientes + " facturas en revision pendientes");
        } catch (Exception e) {
            log.error("Error cargando datos de Verifactu", e);
            DialogUtils.showError("Error al cargar datos: " + e.getMessage());
        }
    }

    private void filtrarEnvios(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            tableEnvios.setItems(listaEnvios);
        } else {
            String query = busqueda.toLowerCase();
            tableEnvios.setItems(listaEnvios.filtered(e ->
                contiene(e.getNumero(), query)
                    || contiene(e.getEstado(), query)
                    || contiene(e.getTipoRegistro(), query)
                    || contiene(e.getFacturaId(), query)
            ));
        }
        if (lblTotal != null) lblTotal.setText(tableEnvios.getItems().size() + " registros");
    }

    @FXML
    public void onCambiarEstado() {
        verifactuHabilitado = chkHabilitado.isSelected();
        actualizarEstado();
    }

    @FXML
    public void onGuardar() {
        String nif = txtNif != null ? txtNif.getText() : "";
        if (verifactuHabilitado && (nif == null || nif.isBlank())) {
            DialogUtils.showWarning("Introduce el NIF del emisor.");
            return;
        }

        try {
            EmpresaConfig cfg = empresaConfigService.getConfiguracionActiva().orElseGet(EmpresaConfig::new);
            cfg.setVerifactuHabilitado(verifactuHabilitado);
            if (nif != null && !nif.isBlank()) cfg.setVerifactuNifEmisor(nif);
            empresaConfigService.save(cfg);
            actualizarEstado();
            DialogUtils.showSuccess("Configuracion guardada.");
        } catch (Exception e) {
            log.error("Error guardando configuracion de Verifactu", e);
            DialogUtils.showError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onProbar() {
        onDiagnosticoFiscal();
    }

    @FXML
    public void onDiagnosticoFiscal() {
        FiscalComplianceService.FiscalComplianceReport report = fiscalComplianceService.diagnosticar();
        StringBuilder detalle = new StringBuilder();
        detalle.append("Estado: ").append(report.listoProduccion() ? "LISTO" : "PENDIENTE").append("\n");
        detalle.append("Modalidad: ").append(report.modalidad()).append("\n\n");
        for (FiscalComplianceService.FiscalComplianceCheck check : report.checks()) {
            detalle.append(check.ok() ? "[OK] " : "[PENDIENTE] ")
                .append(check.codigo())
                .append(" - ")
                .append(check.descripcion())
                .append("\n");
        }
        DialogUtils.showInfo(detalle.toString());
        actualizarEstado();
    }

    @FXML
    public void onEnviar() {
        if (!DialogUtils.showConfirm("Deseas emitir las facturas en revision pendientes?")) return;

        List<Factura> pendientes = new ArrayList<>();
        for (Factura factura : facturaService.findAll()) {
            if ("REVISION".equalsIgnoreCase(factura.getEstado()) && !Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
                pendientes.add(factura);
            }
        }

        if (pendientes.isEmpty()) {
            DialogUtils.showInfo("No hay facturas en revision pendientes de emitir.");
            return;
        }

        setUiBusy(true, "Emitiendo facturas...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int enviados = 0;
                int errores = 0;
                for (int i = 0; i < pendientes.size(); i++) {
                    try {
                        facturaService.aprobarYEmitir(pendientes.get(i).getId());
                        enviados++;
                    } catch (Exception e) {
                        errores++;
                        log.error("Error emitiendo factura {}", pendientes.get(i).getNumero(), e);
                    }
                    updateProgress(i + 1, pendientes.size());
                }
                int totalEnviados = enviados;
                int totalErrores = errores;
                Platform.runLater(() -> {
                    cargarDatos();
                    DialogUtils.showSuccess("Proceso completado:\nEmitidas: " + totalEnviados + "\nErrores: " + totalErrores);
                });
                return null;
            }
        };

        if (progressIndicator != null) progressIndicator.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(ev -> setUiBusy(false, "Proceso finalizado"));
        task.setOnFailed(ev -> setUiBusy(false, "Error durante la emision"));
        Thread thread = new Thread(task, "verifactu-emision-thread");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void onVerificar() {
        VerifactuEvidence selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Selecciona un registro para verificar.");
            return;
        }
        VerifactuEvidence evidencia = evidenceService.verificarEstadoAEAT(selected.getId());
        cargarDatos();
        DialogUtils.showInfo("Estado actual: " + evidencia.getEstado());
    }

    @FXML
    public void onRefresh() {
        cargarConfiguracion();
        cargarDatos();
    }

    @FXML
    public void onVerDetalles() {
        VerifactuEvidence selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Selecciona un registro.");
            return;
        }

        String detalle = "DETALLE DE REGISTRO VERIFACTU\n\n"
            + "Factura: " + valor(selected.getNumero()) + "\n"
            + "Tipo registro: " + valor(selected.getTipoRegistro()) + "\n"
            + "Estado: " + valor(selected.getEstado()) + "\n"
            + "Referencia: " + referencia(selected) + "\n"
            + "Hash anterior: " + valor(selected.getHashAnterior()) + "\n"
            + "Hash: " + valor(selected.getHash()) + "\n"
            + "Error: " + valor(selected.getErrorMessage());
        DialogUtils.showInfo(detalle);
    }

    @FXML
    public void onReenviar() {
        VerifactuEvidence selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Selecciona un registro.");
            return;
        }
        evidenceService.reenviarEvidencia(selected.getId());
        cargarDatos();
        DialogUtils.showSuccess("Reintento registrado.");
    }

    @FXML
    public void onExportar() {
        try {
            Path archivo = facturacionEventoService.exportarEventosCsv(FacturacionEventoService.AMBITO_FACTURAS, null);
            DialogUtils.showSuccess("Eventos fiscales exportados:\n" + archivo.toAbsolutePath());
        } catch (Exception e) {
            log.error("Error exportando eventos fiscales", e);
            DialogUtils.showError("Error exportando eventos: " + e.getMessage());
        }
    }

    private void setUiBusy(boolean busy, String infoText) {
        if (btnEnviar != null) btnEnviar.setDisable(busy);
        if (btnGuardar != null) btnGuardar.setDisable(busy);
        if (btnProbar != null) btnProbar.setDisable(busy);
        if (progressIndicator != null) {
            if (!busy) progressIndicator.progressProperty().unbind();
            progressIndicator.setVisible(busy);
            progressIndicator.setProgress(busy ? ProgressIndicator.INDETERMINATE_PROGRESS : 0);
        }
        if (lblInfo != null) lblInfo.setText(infoText);
    }

    private String cliente(VerifactuEvidence evidencia) {
        try {
            if (evidencia.getFacturaId() == null) return "-";
            long facturaId = Long.parseLong(evidencia.getFacturaId());
            return facturaService.findById(facturaId)
                .filter(f -> f.getCliente() != null)
                .map(f -> f.getCliente().getNombre())
                .orElse("-");
        } catch (Exception ignored) {
            return "-";
        }
    }

    private String importe(VerifactuEvidence evidencia) {
        try {
            if (evidencia.getFacturaId() == null) return "0.00 EUR";
            long facturaId = Long.parseLong(evidencia.getFacturaId());
            return facturaService.findById(facturaId)
                .filter(f -> f.getTotal() != null)
                .map(f -> String.format("%.2f EUR", f.getTotal()))
                .orElse("0.00 EUR");
        } catch (Exception ignored) {
            return "0.00 EUR";
        }
    }

    private String referencia(VerifactuEvidence evidence) {
        if (evidence.getCodigoRespuestaAEAT() != null) return evidence.getCodigoRespuestaAEAT();
        if (evidence.getHuellaRegistro() != null) return evidence.getHuellaRegistro();
        return "-";
    }

    private boolean contiene(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private String valor(Object value) {
        return value != null ? value.toString() : "-";
    }
}
