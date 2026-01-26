package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.service.VerifacturAEATService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.VerifactuEvidenceService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.DialogUtils;

import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de VeriFacTur (AEAT)
 */
@Controller
public class VerifactuController {
    private static final Logger log = LoggerFactory.getLogger(VerifactuController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Configuración
    @FXML private Label lblEstado;
    @FXML private CheckBox chkHabilitado;
    @FXML private TextField txtCertificado;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNif;
    @FXML private Label lblInfo;

    // Controles de interacción
    @FXML private Button btnEnviar;
    @FXML private Button btnGuardar;
    @FXML private Button btnProbar;
    @FXML private ProgressIndicator progressIndicator;

    // Tabla
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

    private final VerifacturAEATService verifacturService;
    private final FacturaService facturaService;
    private final EmpresaConfigService empresaConfigService;
    private final VerifactuEvidenceService evidenceService;
    private final ObservableList<VerifactuEvidence> listaEnvios = FXCollections.observableArrayList();
    private boolean verifactuHabilitado = false;

    public VerifactuController(VerifacturAEATService verifacturService, FacturaService facturaService,
                               EmpresaConfigService empresaConfigService, VerifactuEvidenceService evidenceService) {
        this.verifacturService = verifacturService;
        this.facturaService = facturaService;
        this.empresaConfigService = empresaConfigService;
        this.evidenceService = evidenceService;
    }

    @FXML
    public void initialize() {
        log.info("✅ VerifactuController inicializado");
        configurarTabla();
        // Asegurar que txtBuscar existe (si FXMLLoader no lo inyectó, crearlo)
        if (txtBuscar == null) {
            txtBuscar = new TextField();
        }
        txtBuscar.setPromptText("Buscar factura...");
        cargarConfiguracion();
        cargarDatos();
    }

    private void configurarTabla() {
        // Configurar columnas
        colFecha.setCellValueFactory(cellData -> {
            VerifactuEvidence e = cellData.getValue();
            String fecha = "-";
            if (e.getFechaEnvio() != null) {
                fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(e.getFechaEnvio());
            } else if (e.getFechaEmision() != null) {
                fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(e.getFechaEmision());
            }
            return new SimpleStringProperty(fecha);
        });

        colFactura.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getNumero() != null ? cellData.getValue().getNumero() : "-"));

        colCliente.setCellValueFactory(cellData -> {
            VerifactuEvidence e = cellData.getValue();
            String cliente = "-";
            try {
                if (e.getFacturaId() != null && !e.getFacturaId().isEmpty()) {
                    try {
                        long fid = Long.parseLong(e.getFacturaId());
                        var opt = facturaService.findById(fid);
                        if (opt.isPresent() && opt.get().getCliente() != null) cliente = opt.get().getCliente().getNombre();
                    } catch (NumberFormatException ex) {
                        // facturaId may be not numeric; ignore
                    }
                }
            } catch (Exception ex) {
                log.debug("No se pudo obtener cliente para evidencia {}: {}", e.getId(), ex.getMessage());
            }
            return new SimpleStringProperty(cliente);
        });

        colImporte.setCellValueFactory(cellData -> {
            VerifactuEvidence e = cellData.getValue();
            String importe = "0.00 €";
            try {
                if (e.getFacturaId() != null && !e.getFacturaId().isEmpty()) {
                    try {
                        long fid = Long.parseLong(e.getFacturaId());
                        var opt = facturaService.findById(fid);
                        if (opt.isPresent() && opt.get().getTotal() != null) importe = String.format("%.2f €", opt.get().getTotal());
                    } catch (NumberFormatException ex) { }
                }
            } catch (Exception ex) { log.debug("No se pudo obtener importe para evidencia {}: {}", e.getId(), ex.getMessage()); }
            return new SimpleStringProperty(importe);
        });

        colEstado.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().getEstado() != null ? cellData.getValue().getEstado() : "-";
            String emoji = switch (estado) {
                case "ACEPTADA" -> "✅ ACEPTADA";
                case "PENDIENTE" -> "⏳ PENDIENTE";
                case "ERROR" -> "❌ ERROR";
                case "RECHAZADA" -> "🚫 RECHAZADA";
                default -> "❓ " + estado;
            };
            return new SimpleStringProperty(emoji);
        });

        colReferencia.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getCodigoRespuestaAEAT() != null ? cellData.getValue().getCodigoRespuestaAEAT() : (cellData.getValue().getHash() != null ? cellData.getValue().getHash() : "-")));

        colMensaje.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getErrorMessage() != null ? cellData.getValue().getErrorMessage() : ""));

        tableEnvios.setItems(listaEnvios);
    }

    private void cargarConfiguracion() {
        try {
            // Intentar cargar configuración desde EmpresaConfig si existe
            empresaConfigService.getConfiguracionActiva().ifPresent(cfg -> {
                verifactuHabilitado = Boolean.TRUE.equals(cfg.getVerifactuHabilitado());
                if (chkHabilitado != null) chkHabilitado.setSelected(verifactuHabilitado);
                if (txtNif != null && cfg.getVerifactuNifEmisor() != null) txtNif.setText(cfg.getVerifactuNifEmisor());
            });
        } catch (Exception e) {
            log.warn("No se pudo cargar configuración de empresa para Verifactu: {}", e.getMessage());
        }

        // Actualizar estado visual
        if (chkHabilitado != null) chkHabilitado.setSelected(verifactuHabilitado);
        actualizarEstado();
    }

    private void actualizarEstado() {
        if (lblEstado != null) {
            if (verifactuHabilitado) {
                lblEstado.setText("● Habilitado");
                lblEstado.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
            } else {
                lblEstado.setText("● Deshabilitado");
                lblEstado.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
        }
    }

    private void cargarDatos() {
        try {
            log.info("📊 Cargando registros de VeriFacTur (evidencias)...");

            List<VerifactuEvidence> evidencias = evidenceService.findAll();
            listaEnvios.clear();
            listaEnvios.addAll(evidencias);

            // Contar pendientes a partir de facturas
            int pendientes = 0;
            List<Factura> facturas = facturaService.findAll();
            for (Factura factura : facturas) {
                if (("EMITIDA".equals(factura.getEstado()) || "REVISADA".equals(factura.getEstado()))
                    && !Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
                    pendientes++;
                }
            }

            if (lblTotal != null) lblTotal.setText(listaEnvios.size() + " registros");
            if (lblInfo != null) lblInfo.setText(pendientes + " facturas pendientes de enviar");

            log.info("✅ {} evidencias cargadas, {} pendientes", listaEnvios.size(), pendientes);

        } catch (Exception e) {
            log.error("❌ Error cargando datos de VeriFacTur", e);
            DialogUtils.showError("Error al cargar datos: " + e.getMessage());
        }
    }

    @FXML
    public void onCambiarEstado() {
        verifactuHabilitado = chkHabilitado.isSelected();
        actualizarEstado();
        log.info("VeriFacTur {}", verifactuHabilitado ? "habilitado" : "deshabilitado");
    }

    @FXML
    public void onSeleccionarCertificado() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Certificado Digital");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Certificados", "*.pfx", "*.p12"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );

            // Determinar ventana propietaria de forma segura
            Window owner = null;
            if (txtCertificado != null && txtCertificado.getScene() != null) {
                owner = txtCertificado.getScene().getWindow();
            }

            File file = fileChooser.showOpenDialog(owner);
            if (file != null) {
                txtCertificado.setText(file.getAbsolutePath());
                log.info("Certificado seleccionado: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("No se pudo abrir el selector de archivos: {}", e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        log.info("💾 Guardando configuración de VeriFacTur...");

        String certificado = txtCertificado != null ? txtCertificado.getText() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";
        String nif = txtNif != null ? txtNif.getText() : "";

        if (verifactuHabilitado) {
            if (certificado.isEmpty()) {
                DialogUtils.showWarning("Por favor, selecciona un certificado digital");
                return;
            }
            if (password.isEmpty()) {
                DialogUtils.showWarning("Por favor, introduce la contraseña del certificado");
                return;
            }
            if (nif.isEmpty()) {
                DialogUtils.showWarning("Por favor, introduce el NIF de la empresa");
                return;
            }
        }

        try {
            // Persistir configuración mínima en empresa config
            EmpresaConfig cfg = empresaConfigService.getConfiguracionActiva().orElseGet(EmpresaConfig::new);
            cfg.setVerifactuHabilitado(verifactuHabilitado);
            if (nif != null && !nif.isEmpty()) cfg.setVerifactuNifEmisor(nif);
            empresaConfigService.save(cfg);

            // Inicializar certificado en el servicio (si se ha proporcionado)
            if (certificado != null && !certificado.isEmpty()) {
                verifacturService.inicializarCertificado(certificado, password);
            }

            DialogUtils.showSuccess("Configuración guardada correctamente");
             log.info("✅ Configuración de VeriFacTur guardada");
        } catch (Exception e) {
            log.error("❌ Error guardando configuración", e);
            DialogUtils.showError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onProbar() {
        log.info("🧪 Probando conexión con VeriFacTur...");

        if (!verifactuHabilitado) {
            DialogUtils.showWarning("VeriFacTur está deshabilitado. Actívalo primero.");
            return;
        }

        try {
            boolean conexionOk = verifacturService.probarConexion();

            if (conexionOk) {
                DialogUtils.showSuccess("✅ Conexión con AEAT establecida correctamente\n\nVeriFacTur está listo para enviar facturas.");
            } else {
                DialogUtils.showError("❌ No se pudo conectar con AEAT\n\nVerifica tu certificado y contraseña.");
            }
        } catch (Exception e) {
            log.error("❌ Error probando conexión", e);
            DialogUtils.showError("Error en prueba de conexión: " + e.getMessage());
        }
    }

    @FXML
    public void onEnviar() {
        log.info("📤 Iniciando proceso de envío de facturas pendientes a VeriFacTur...");

        if (!verifactuHabilitado) {
            DialogUtils.showWarning("VeriFacTur está deshabilitado. Actívalo primero en la configuración.");
            return;
        }

        if (!DialogUtils.showConfirm("¿Deseas enviar todas las facturas pendientes a VeriFacTur (AEAT)?")) {
            return;
        }

        // Preparar lista de facturas pendientes para poder mostrar progreso
        List<Factura> todas = facturaService.findAll();
        List<Factura> pendientesList = new ArrayList<>();
        for (Factura factura : todas) {
            if (("EMITIDA".equals(factura.getEstado()) || "REVISADA".equals(factura.getEstado()))
                    && !Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
                pendientesList.add(factura);
            }
        }

        if (pendientesList.isEmpty()) {
            DialogUtils.showInfo("No hay facturas pendientes para enviar.");
            return;
        }

        // Deshabilitar controles y mostrar indicador si existen
        if (btnEnviar != null) btnEnviar.setDisable(true);
        if (btnGuardar != null) btnGuardar.setDisable(true);
        if (btnProbar != null) btnProbar.setDisable(true);
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
            progressIndicator.setProgress(0);
        }
        if (lblInfo != null) lblInfo.setText("Enviando facturas a VeriFacTur...");

        int total = pendientesList.size();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int enviados = 0;
                int errores = 0;
                for (int i = 0; i < pendientesList.size(); i++) {
                    if (isCancelled()) break;
                    Factura factura = pendientesList.get(i);
                    try {
                        Map<String, Object> resultado = verifacturService.enviarFactura(factura);
                        if ((boolean) resultado.getOrDefault("exito", false)) {
                            enviados++;
                            log.info("✅ Factura {} enviada", factura.getNumero());
                        } else {
                            errores++;
                            log.warn("⚠️ Error en factura {}: {}", factura.getNumero(), resultado.get("mensaje"));
                        }
                    } catch (Exception e) {
                        errores++;
                        log.error("❌ Error enviando factura {}", factura.getNumero(), e);
                    }
                    updateProgress(i + 1, total);
                }

                final int fEnviadas = enviados;
                final int fErrores = errores;
                Platform.runLater(() -> {
                    cargarDatos();
                    DialogUtils.showSuccess(String.format("Proceso completado:\n\n✅ %d facturas enviadas\n❌ %d errores", fEnviadas, fErrores));
                });
                return null;
            }
        };

        // Bind progress indicator if present
        if (progressIndicator != null) {
            progressIndicator.progressProperty().bind(task.progressProperty());
        }

        task.setOnSucceeded(ev -> {
            if (progressIndicator != null) {
                progressIndicator.progressProperty().unbind();
                progressIndicator.setVisible(false);
            }
            if (btnEnviar != null) btnEnviar.setDisable(false);
            if (btnGuardar != null) btnGuardar.setDisable(false);
            if (btnProbar != null) btnProbar.setDisable(false);
            if (lblInfo != null) lblInfo.setText("Proceso finalizado");
        });

        task.setOnFailed(ev -> {
            if (progressIndicator != null) {
                progressIndicator.progressProperty().unbind();
                progressIndicator.setVisible(false);
            }
            if (btnEnviar != null) btnEnviar.setDisable(false);
            if (btnGuardar != null) btnGuardar.setDisable(false);
            if (btnProbar != null) btnProbar.setDisable(false);
            if (lblInfo != null) lblInfo.setText("Error durante el envío");
        });

        task.setOnCancelled(ev -> {
            if (progressIndicator != null) {
                progressIndicator.progressProperty().unbind();
                progressIndicator.setVisible(false);
            }
            if (btnEnviar != null) btnEnviar.setDisable(false);
            if (btnGuardar != null) btnGuardar.setDisable(false);
            if (btnProbar != null) btnProbar.setDisable(false);
            if (lblInfo != null) lblInfo.setText("Envío cancelado");
        });

        Thread th = new Thread(task, "Verifactu-Envio-Thread");
        th.setDaemon(true);
        th.start();
    }

    @FXML
    public void onVerificar() {
        log.info("🔍 Verificando estado de envíos...");
        DialogUtils.showWarning("Funcionalidad en desarrollo\n\nPróximamente podrás verificar el estado de las facturas enviadas directamente desde AEAT.");
    }

    @FXML
    public void onRefresh() {
        log.info("🔄 Refrescando datos...");
        cargarDatos();
    }

    @FXML
    public void onVerDetalles() {
        VerifactuEvidence selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Por favor, selecciona un registro");
            return;
        }

        String detalle = "📋 DETALLE DE ENVÍO VERIFACTUR\n" +
                "═══════════════════════════════════════\n\n" +
                "📄 Factura: " + (selected.getNumero() != null ? selected.getNumero() : selected.getFacturaId()) + "\n" +
                "👤 Cliente: " + "(ver detalles)" + "\n" +
                "💰 Importe: " + "(ver detalles)" + "\n" +
                "📅 Fecha envío: " + (selected.getFechaEnvio() != null ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(selected.getFechaEnvio()) : "-") + "\n" +
                "✅ Estado: " + selected.getEstado() + "\n" +
                "🔖 Referencia: " + (selected.getCodigoRespuestaAEAT() != null ? selected.getCodigoRespuestaAEAT() : selected.getHash()) + "\n" +
                "📝 Mensaje: " + (selected.getErrorMessage() != null ? selected.getErrorMessage() : "") + "\n";

        DialogUtils.showInfo(detalle);
    }

    @FXML
    public void onReenviar() {
        VerifactuEvidence selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Por favor, selecciona un registro");
            return;
        }

        DialogUtils.showWarning("Funcionalidad en desarrollo\n\nPróximamente podrás reenviar facturas a VeriFacTur.");
    }

    @FXML
    public void onExportar() {
        DialogUtils.showWarning("Funcionalidad en desarrollo\n\nPróximamente podrás exportar el registro de envíos a Excel o PDF.");
    }
}
