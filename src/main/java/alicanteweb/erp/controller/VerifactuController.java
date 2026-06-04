package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.FacturacionEventoService;
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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class VerifactuController {
    private static final Logger log = LoggerFactory.getLogger(VerifactuController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter EXPORT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

    @FXML private Label lblEstado;
    @FXML private TextField txtNif;
    @FXML private Label lblInfo;
    @FXML private Label lblModoLegal;
    @FXML private Label lblFechaInicio;
    @FXML private Label lblFechaRenuncia;

    @FXML private Button btnEnviar;
    @FXML private Button btnGuardar;
    @FXML private Button btnProbar;
    @FXML private Button btnIniciar;
    @FXML private Button btnRenuncia;
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
    private final FacturacionEventoService facturacionEventoService;
    private final ObservableList<VerifactuEvidence> listaEnvios = FXCollections.observableArrayList();
    private boolean verifactuOperativo = false;

    public VerifactuController(VerifactuService verifactuService,
                               FacturaService facturaService,
                               EmpresaConfigService empresaConfigService,
                               VerifactuEvidenceService evidenceService,
                               FacturacionEventoService facturacionEventoService) {
        this.verifactuService = verifactuService;
        this.facturaService = facturaService;
        this.empresaConfigService = empresaConfigService;
        this.evidenceService = evidenceService;
        this.facturacionEventoService = facturacionEventoService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando VerifactuController");
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

        colFactura.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getNumero() != null ? cellData.getValue().getNumero() : "-"
        ));

        colCliente.setCellValueFactory(cellData -> {
            VerifactuEvidence evidencia = cellData.getValue();
            String cliente = "-";
            try {
                long facturaId = Long.parseLong(evidencia.getFacturaId());
                var factura = facturaService.findById(facturaId);
                if (factura.isPresent() && factura.get().getCliente() != null) {
                    cliente = factura.get().getCliente().getNombre();
                }
            } catch (Exception e) {
                log.debug("No se pudo resolver cliente de evidencia {}: {}", evidencia.getFacturaId(), e.getMessage());
            }
            return new SimpleStringProperty(cliente);
        });

        colImporte.setCellValueFactory(cellData -> {
            VerifactuEvidence evidencia = cellData.getValue();
            String importe = "0.00 EUR";
            try {
                long facturaId = Long.parseLong(evidencia.getFacturaId());
                var factura = facturaService.findById(facturaId);
                if (factura.isPresent() && factura.get().getTotal() != null) {
                    importe = String.format("%.2f EUR", factura.get().getTotal());
                }
            } catch (Exception e) {
                log.debug("No se pudo resolver importe de evidencia {}: {}", evidencia.getFacturaId(), e.getMessage());
            }
            return new SimpleStringProperty(importe);
        });

        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getEstado() != null ? cellData.getValue().getEstado() : "-"
        ));

        colReferencia.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getCodigoRespuestaAEAT() != null
                ? cellData.getValue().getCodigoRespuestaAEAT()
                : (cellData.getValue().getHuellaRegistro() != null ? cellData.getValue().getHuellaRegistro() : "-")
        ));

        colMensaje.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getErrorMessage() != null ? cellData.getValue().getErrorMessage() : ""
        ));

        tableEnvios.setItems(listaEnvios);
    }

    private void cargarConfiguracion() {
        try {
            empresaConfigService.getConfiguracionActiva().ifPresent(cfg -> {
                verifactuOperativo = empresaConfigService.isFuncionamientoVerifactuVigente(cfg);
                if (txtNif != null && cfg.getVerifactuNifEmisor() != null) {
                    txtNif.setText(cfg.getVerifactuNifEmisor());
                }
                if (lblFechaInicio != null) {
                    lblFechaInicio.setText(cfg.getVerifactuFechaInicio() != null ? cfg.getVerifactuFechaInicio().toString() : "-");
                }
                if (lblFechaRenuncia != null) {
                    lblFechaRenuncia.setText(cfg.getVerifactuFechaRenuncia() != null ? cfg.getVerifactuFechaRenuncia().toString() : "-");
                }
            });
        } catch (Exception e) {
            log.warn("No se pudo cargar configuracion de empresa para Verifactu: {}", e.getMessage());
        }
        actualizarEstado();
    }

    private void actualizarEstado() {
        if (lblEstado == null) {
            return;
        }
        if (verifactuOperativo) {
            lblEstado.setText(verifactuService.isAeatAvailable() ? "Activo y listo" : "Configurado sin AEAT disponible");
            lblEstado.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
        } else {
            lblEstado.setText("NO VERI*FACTU");
            lblEstado.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
        if (lblModoLegal != null) {
            lblModoLegal.setText(verifactuOperativo ? "VERI*FACTU" : "NO VERI*FACTU");
        }
        if (btnIniciar != null) {
            btnIniciar.setDisable(verifactuOperativo);
        }
        if (btnRenuncia != null) {
            btnRenuncia.setDisable(!verifactuOperativo);
        }
    }

    private void cargarDatos() {
        try {
            List<VerifactuEvidence> evidencias = evidenceService.findAll();
            listaEnvios.clear();
            listaEnvios.addAll(evidencias);

            int pendientes = 0;
            for (Factura factura : facturaService.findAll()) {
                if ("REVISION".equalsIgnoreCase(factura.getEstado()) && !Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
                    pendientes++;
                }
            }

            if (lblTotal != null) {
                lblTotal.setText(listaEnvios.size() + " registros");
            }
            if (lblInfo != null) {
                lblInfo.setText(pendientes + " facturas en revision pendientes de emitir");
            }
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
                (e.getNumero() != null && e.getNumero().toLowerCase().contains(query)) ||
                (e.getEstado() != null && e.getEstado().toLowerCase().contains(query)) ||
                (e.getTipoRegistro() != null && e.getTipoRegistro().toLowerCase().contains(query)) ||
                (e.getFacturaId() != null && e.getFacturaId().toLowerCase().contains(query))
            ));
        }
        if (lblTotal != null) {
            lblTotal.setText(tableEnvios.getItems().size() + " registros");
        }
    }

    @FXML
    public void onCambiarEstado() {
        DialogUtils.showWarning("El modo VERI*FACTU no se cambia con un interruptor. Usa iniciar funcionamiento o programar renuncia.");
        actualizarEstado();
    }

    @FXML
    public void onGuardar() {
        if (!verificarPermiso("enviar")) {
            return;
        }
        String nif = txtNif != null ? txtNif.getText() : "";
        if (verifactuOperativo && (nif == null || nif.isBlank())) {
            DialogUtils.showWarning("Por favor, introduce el NIF del emisor.");
            return;
        }

        try {
            empresaConfigService.guardarDatosVerifactu(nif);
            cargarConfiguracion();
            actualizarEstado();
            DialogUtils.showSuccess("Datos VERI*FACTU guardados.\nLa ruta y password del certificado se gestionan por properties o variables de entorno.");
        } catch (Exception e) {
            log.error("Error guardando configuracion de Verifactu", e);
            DialogUtils.showError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onIniciarVerifactu() {
        if (!verificarPermiso("enviar")) {
            return;
        }
        String nif = txtNif != null ? txtNif.getText() : "";
        if (nif == null || nif.isBlank()) {
            DialogUtils.showWarning("Introduce el NIF del emisor antes de iniciar VERI*FACTU.");
            return;
        }
        if (!verifactuService.isAeatAvailable()) {
            DialogUtils.showWarning("No se puede iniciar VERI*FACTU sin AEAT y certificado disponibles. Revisa keystore, alias, password y verifactu.aeat.enabled.");
            return;
        }
        if (!DialogUtils.showConfirm("Iniciar funcionamiento VERI*FACTU para esta empresa?\n\nDesde este momento se remitiran los registros de facturacion a la AEAT y debera mantenerse, como minimo, hasta el 31 de diciembre del anio en curso.")) {
            return;
        }

        try {
            empresaConfigService.iniciarFuncionamientoVerifactu(nif);
            cargarConfiguracion();
            DialogUtils.showSuccess("Funcionamiento VERI*FACTU iniciado para esta empresa.");
        } catch (Exception e) {
            log.error("Error iniciando VERI*FACTU", e);
            DialogUtils.showError("No se pudo iniciar VERI*FACTU: " + e.getMessage());
        }
    }

    @FXML
    public void onProgramarRenuncia() {
        if (!verificarPermiso("enviar")) {
            return;
        }
        if (!verifactuOperativo) {
            DialogUtils.showWarning("La empresa no esta funcionando actualmente como VERI*FACTU.");
            return;
        }
        if (!DialogUtils.showConfirm("Programar renuncia a VERI*FACTU para el 31 de diciembre del anio en curso?\n\nHasta esa fecha el sistema seguira funcionando como VERI*FACTU.")) {
            return;
        }

        try {
            empresaConfigService.programarRenunciaVerifactuFinDeAnio();
            cargarConfiguracion();
            DialogUtils.showSuccess("Renuncia VERI*FACTU programada para fin de anio.");
        } catch (Exception e) {
            log.error("Error programando renuncia VERI*FACTU", e);
            DialogUtils.showError("No se pudo programar la renuncia: " + e.getMessage());
        }
    }

    @FXML
    public void onProbar() {
        if (verifactuService.isAeatAvailable()) {
            DialogUtils.showSuccess("AEAT y certificado estan disponibles para emitir.");
        } else {
            DialogUtils.showWarning("El servicio no esta listo para emitir contra AEAT.\nRevisa keystore, alias, password y verifactu.aeat.enabled.");
        }
        actualizarEstado();
    }

    @FXML
    public void onEnviar() {
        if (!verificarPermiso("enviar")) {
            return;
        }
        if (!verifactuOperativo) {
            DialogUtils.showWarning("La empresa no esta en funcionamiento VERI*FACTU. Inicia el funcionamiento antes de emitir.");
            return;
        }

        if (!DialogUtils.showConfirm("Deseas emitir a AEAT todas las facturas en revision pendientes?")) {
            return;
        }

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

        setUiBusy(true, "Emitiendo facturas a AEAT...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int enviados = 0;
                int errores = 0;
                for (int i = 0; i < pendientes.size(); i++) {
                    Factura factura = pendientes.get(i);
                    try {
                        facturaService.aprobarYEmitir(factura.getId());
                        enviados++;
                    } catch (Exception e) {
                        errores++;
                        log.error("Error emitiendo factura {}", factura.getNumero(), e);
                    }
                    updateProgress(i + 1, pendientes.size());
                }

                final int totalEnviados = enviados;
                final int totalErrores = errores;
                Platform.runLater(() -> {
                    cargarDatos();
                    DialogUtils.showSuccess(String.format(
                        "Proceso completado:\n\nEmitidas: %d\nErrores: %d",
                        totalEnviados,
                        totalErrores
                    ));
                });
                return null;
            }
        };

        if (progressIndicator != null) {
            progressIndicator.progressProperty().bind(task.progressProperty());
        }

        task.setOnSucceeded(ev -> setUiBusy(false, "Proceso finalizado"));
        task.setOnFailed(ev -> setUiBusy(false, "Error durante la emision"));
        task.setOnCancelled(ev -> setUiBusy(false, "Emision cancelada"));

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
        cargarDatos();
    }

    @FXML
    public void onVerDetalles() {
        VerifactuEvidence selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Por favor, selecciona un registro");
            return;
        }

        String detalle = "DETALLE DE REGISTRO VERIFACTU\n\n" +
            "Factura: " + (selected.getNumero() != null ? selected.getNumero() : selected.getFacturaId()) + "\n" +
            "Tipo registro: " + (selected.getTipoRegistro() != null ? selected.getTipoRegistro() : "-") + "\n" +
            "Fecha generacion: " + (selected.getFechaGeneracionRegistro() != null ? DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(selected.getFechaGeneracionRegistro()) : "-") + "\n" +
            "Estado: " + (selected.getEstado() != null ? selected.getEstado() : "-") + "\n" +
            "Referencia: " + (selected.getCodigoRespuestaAEAT() != null ? selected.getCodigoRespuestaAEAT() : selected.getHuellaRegistro()) + "\n" +
            "Error: " + (selected.getErrorMessage() != null ? selected.getErrorMessage() : "");

        DialogUtils.showInfo(detalle);
    }

    @FXML
    public void onReenviar() {
        if (!verificarPermiso("enviar")) {
            return;
        }
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
        if (!verificarPermiso("exportar")) {
            return;
        }
        List<VerifactuEvidence> registros = new ArrayList<>(tableEnvios.getItems());
        if (registros.isEmpty()) {
            DialogUtils.showWarning("No hay registros VeriFactu para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar registros VeriFactu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP", "*.zip"));
        fileChooser.setInitialFileName("verifactu_export_" + EXPORT_TIMESTAMP.format(Instant.now()) + ".zip");
        File destino = fileChooser.showSaveDialog(tableEnvios.getScene().getWindow());
        if (destino == null) {
            return;
        }

        try {
            exportarRegistrosZip(registros, destino);
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("registros", registros.size());
            metadata.put("archivo", destino.getAbsolutePath());
            metadata.put("generadoEn", Instant.now().toString());
            facturacionEventoService.registrarEvento(
                    FacturacionEventoService.AMBITO_FACTURAS,
                    "EXPORTACION_REGISTROS_FACTURACION",
                    destino.getName(),
                    metadata
            );
            DialogUtils.showSuccess("Registros VeriFactu exportados:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            log.error("Error exportando registros VeriFactu", e);
            DialogUtils.showError("No se pudo exportar VeriFactu: " + e.getMessage());
        }
    }

    private void exportarRegistrosZip(List<VerifactuEvidence> registros, File destino) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new java.io.FileOutputStream(destino), StandardCharsets.UTF_8)) {
            StringBuilder manifest = new StringBuilder();
            manifest.append("id;factura_id;serie;numero;tipo_registro;fecha_generacion;estado;hash;hash_anterior;huella;nif_emisor;xml\n");
            for (VerifactuEvidence registro : registros) {
                String xmlPath = "xml/registro_" + registro.getId() + ".xml";
                manifest.append(csv(registro.getId()))
                        .append(';').append(csv(registro.getFacturaId()))
                        .append(';').append(csv(registro.getSerie()))
                        .append(';').append(csv(registro.getNumero()))
                        .append(';').append(csv(registro.getTipoRegistro()))
                        .append(';').append(csv(registro.getFechaGeneracionRegistro()))
                        .append(';').append(csv(registro.getEstado()))
                        .append(';').append(csv(registro.getHash()))
                        .append(';').append(csv(registro.getHashAnterior()))
                        .append(';').append(csv(registro.getHuellaRegistro()))
                        .append(';').append(csv(registro.getNifEmisor()))
                        .append(';').append(csv(xmlPath))
                        .append('\n');

                zip.putNextEntry(new ZipEntry(xmlPath));
                String xml = registro.getXmlGenerado() != null ? registro.getXmlGenerado() : "";
                zip.write(xml.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }

            zip.putNextEntry(new ZipEntry("manifest.csv"));
            zip.write(manifest.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }

    private String csv(Object value) {
        String text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    private void setUiBusy(boolean busy, String infoText) {
        if (btnEnviar != null) btnEnviar.setDisable(busy);
        if (btnGuardar != null) btnGuardar.setDisable(busy);
        if (btnProbar != null) btnProbar.setDisable(busy);
        if (btnIniciar != null) btnIniciar.setDisable(busy || verifactuOperativo);
        if (btnRenuncia != null) btnRenuncia.setDisable(busy || !verifactuOperativo);
        if (progressIndicator != null) {
            if (!busy) {
                progressIndicator.progressProperty().unbind();
            }
            progressIndicator.setVisible(busy);
            progressIndicator.setProgress(busy ? ProgressIndicator.INDETERMINATE_PROGRESS : 0);
        }
        if (lblInfo != null) {
            lblInfo.setText(infoText);
        }
    }

    private boolean verificarPermiso(String accion) {
        if (empresaConfigService == null || accion == null) {
            return false;
        }
        try {
            var auth = alicanteweb.erp.ErpLauncher.getSpringContext().getBean(alicanteweb.erp.service.AutenticacionService.class);
            if (auth.tienePermiso("verifactu", accion)) {
                return true;
            }
        } catch (Exception e) {
            log.warn("No se pudo verificar permiso VERI*FACTU {}", accion, e);
        }
        DialogUtils.showWarning("No tiene permisos para esta accion de VERI*FACTU.");
        return false;
    }
}
