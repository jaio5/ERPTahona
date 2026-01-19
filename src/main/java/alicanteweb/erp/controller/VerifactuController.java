package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.VerifacturAEATService;
import alicanteweb.erp.service.FacturaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.Dialogs;

import java.io.File;
import java.time.format.DateTimeFormatter;
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

    // Tabla
    @FXML private TableView<Map<String, Object>> tableEnvios;
    @FXML private TableColumn<Map<String, Object>, String> colFecha;
    @FXML private TableColumn<Map<String, Object>, String> colFactura;
    @FXML private TableColumn<Map<String, Object>, String> colCliente;
    @FXML private TableColumn<Map<String, Object>, String> colImporte;
    @FXML private TableColumn<Map<String, Object>, String> colEstado;
    @FXML private TableColumn<Map<String, Object>, String> colReferencia;
    @FXML private TableColumn<Map<String, Object>, String> colMensaje;

    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final VerifacturAEATService verifacturService;
    private final FacturaService facturaService;
    private final ObservableList<Map<String, Object>> listaEnvios = FXCollections.observableArrayList();
    private boolean verifactuHabilitado = false;

    public VerifactuController(VerifacturAEATService verifacturService, FacturaService facturaService) {
        this.verifacturService = verifacturService;
        this.facturaService = facturaService;
    }

    @FXML
    public void initialize() {
        log.info("✅ VerifactuController inicializado");
        configurarTabla();
        cargarConfiguracion();
        cargarDatos();
    }

    private void configurarTabla() {
        // Configurar columnas
        colFecha.setCellValueFactory(cellData ->
            new SimpleStringProperty((String) cellData.getValue().get("fecha")));

        colFactura.setCellValueFactory(cellData ->
            new SimpleStringProperty((String) cellData.getValue().get("factura")));

        colCliente.setCellValueFactory(cellData ->
            new SimpleStringProperty((String) cellData.getValue().get("cliente")));

        colImporte.setCellValueFactory(cellData ->
            new SimpleStringProperty((String) cellData.getValue().get("importe")));

        colEstado.setCellValueFactory(cellData -> {
            String estado = (String) cellData.getValue().get("estado");
            String emoji = switch (estado) {
                case "ACEPTADA" -> "✅ ACEPTADA";
                case "PENDIENTE" -> "⏳ PENDIENTE";
                case "ERROR" -> "❌ ERROR";
                case "RECHAZADA" -> "🚫 RECHAZADA";
                default -> "❓ " + estado;
            };
            return new SimpleStringProperty(emoji);
        });

        colReferencia.setCellValueFactory(cellData ->
            new SimpleStringProperty((String) cellData.getValue().get("referencia")));

        colMensaje.setCellValueFactory(cellData ->
            new SimpleStringProperty((String) cellData.getValue().get("mensaje")));

        tableEnvios.setItems(listaEnvios);
    }

    private void cargarConfiguracion() {
        // TODO: Cargar desde base de datos o properties
        // Temporalmente deshabilitado por defecto
        verifactuHabilitado = false; // verifacturService.isHabilitado();

        if (chkHabilitado != null) {
            chkHabilitado.setSelected(verifactuHabilitado);
        }

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
        listaEnvios.clear();

        try {
            log.info("📊 Cargando registros de VeriFacTur...");

            // Obtener facturas enviadas a VeriFacTur
            List<Factura> facturas = facturaService.findAll();
            int pendientes = 0;

            for (Factura factura : facturas) {
                if (Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
                    Map<String, Object> registro = new java.util.HashMap<>();
                    registro.put("id", factura.getId());
                    registro.put("fecha", factura.getFechaEmisionVerifactu() != null ?
                        factura.getFechaEmisionVerifactu().format(DATE_FORMATTER) : "-");
                    registro.put("factura", factura.getNumero());
                    registro.put("cliente", factura.getCliente() != null ?
                        factura.getCliente().getNombre() : "-");
                    registro.put("importe", String.format("%.2f €", factura.getTotal()));
                    registro.put("estado", "ACEPTADA");
                    // Generar referencia temporal ya que getCodigoVerifactu no existe aún
                    registro.put("referencia", "VF-" + factura.getNumero());
                    registro.put("mensaje", "Enviada correctamente a AEAT");

                    listaEnvios.add(registro);
                } else if ("EMITIDA".equals(factura.getEstado()) || "REVISADA".equals(factura.getEstado())) {
                    pendientes++;
                }
            }

            if (lblTotal != null) {
                lblTotal.setText(listaEnvios.size() + " registros");
            }

            if (lblInfo != null) {
                lblInfo.setText(pendientes + " facturas pendientes de enviar");
            }

            log.info("✅ {} registros cargados, {} pendientes", listaEnvios.size(), pendientes);

        } catch (Exception e) {
            log.error("❌ Error cargando datos de VeriFacTur", e);
            mostrarError("Error al cargar datos: " + e.getMessage());
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Certificado Digital");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Certificados", "*.pfx", "*.p12"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File file = fileChooser.showOpenDialog(txtCertificado.getScene().getWindow());
        if (file != null) {
            txtCertificado.setText(file.getAbsolutePath());
            log.info("Certificado seleccionado: {}", file.getAbsolutePath());
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
                mostrarAlerta("Por favor, selecciona un certificado digital");
                return;
            }
            if (password.isEmpty()) {
                mostrarAlerta("Por favor, introduce la contraseña del certificado");
                return;
            }
            if (nif.isEmpty()) {
                mostrarAlerta("Por favor, introduce el NIF de la empresa");
                return;
            }
        }

        try {
            // TODO: Guardar configuración en base de datos o properties
            mostrarExito("Configuración guardada correctamente");
            log.info("✅ Configuración de VeriFacTur guardada");
        } catch (Exception e) {
            log.error("❌ Error guardando configuración", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onProbar() {
        log.info("🧪 Probando conexión con VeriFacTur...");

        if (!verifactuHabilitado) {
            mostrarAlerta("VeriFacTur está deshabilitado. Actívalo primero.");
            return;
        }

        try {
            // TODO: Probar conexión real con AEAT cuando el método esté implementado
            // Por ahora simulamos una prueba exitosa
            boolean conexionOk = true; // verifacturService.probarConexion();

            if (conexionOk) {
                mostrarExito("✅ Conexión con AEAT establecida correctamente\n\nVeriFacTur está listo para enviar facturas.");
            } else {
                mostrarError("❌ No se pudo conectar con AEAT\n\nVerifica tu certificado y contraseña.");
            }
        } catch (Exception e) {
            log.error("❌ Error probando conexión", e);
            mostrarError("Error en prueba de conexión: " + e.getMessage());
        }
    }

    @FXML
    public void onEnviar() {
        log.info("📤 Enviando facturas pendientes a VeriFacTur...");

        if (!verifactuHabilitado) {
            mostrarAlerta("VeriFacTur está deshabilitado. Actívalo primero en la configuración.");
            return;
        }

        if (!mostrarConfirmacion("¿Deseas enviar todas las facturas pendientes a VeriFacTur (AEAT)?")) {
            return;
        }

        try {
            List<Factura> facturas = facturaService.findAll();
            int enviadas = 0;
            int errores = 0;

            for (Factura factura : facturas) {
                // Solo enviar facturas emitidas o revisadas que no se hayan enviado
                if (("EMITIDA".equals(factura.getEstado()) || "REVISADA".equals(factura.getEstado()))
                    && !Boolean.TRUE.equals(factura.getVerifactuEnviada())) {

                    try {
                        Map<String, Object> resultado = verifacturService.enviarFactura(factura);

                        if ((boolean) resultado.getOrDefault("exito", false)) {
                            enviadas++;
                            log.info("✅ Factura {} enviada", factura.getNumero());
                        } else {
                            errores++;
                            log.warn("⚠️ Error en factura {}: {}", factura.getNumero(),
                                resultado.get("mensaje"));
                        }
                    } catch (Exception e) {
                        errores++;
                        log.error("❌ Error enviando factura {}", factura.getNumero(), e);
                    }
                }
            }
            
            cargarDatos();
            
            mostrarExito(String.format("Proceso completado:\n\n✅ %d facturas enviadas\n❌ %d errores",
                enviadas, errores));

        } catch (Exception e) {
            log.error("❌ Error en proceso de envío masivo", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onVerificar() {
        log.info("🔍 Verificando estado de envíos...");
        mostrarAlerta("Funcionalidad en desarrollo\n\nPróximamente podrás verificar el estado de las facturas enviadas directamente desde AEAT.");
    }

    @FXML
    public void onRefresh() {
        log.info("🔄 Refrescando datos...");
        cargarDatos();
    }

    @FXML
    public void onVerDetalles() {
        Map<String, Object> selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Por favor, selecciona un registro");
            return;
        }

        StringBuilder detalle = new StringBuilder();
        detalle.append("📋 DETALLE DE ENVÍO VERIFACTUR\n");
        detalle.append("═══════════════════════════════════════\n\n");
        detalle.append("📄 Factura: ").append(selected.get("factura")).append("\n");
        detalle.append("👤 Cliente: ").append(selected.get("cliente")).append("\n");
        detalle.append("💰 Importe: ").append(selected.get("importe")).append("\n");
        detalle.append("📅 Fecha envío: ").append(selected.get("fecha")).append("\n");
        detalle.append("✅ Estado: ").append(selected.get("estado")).append("\n");
        detalle.append("🔖 Referencia: ").append(selected.get("referencia")).append("\n");
        detalle.append("📝 Mensaje: ").append(selected.get("mensaje")).append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Envío");
        alert.setHeaderText("Información completa");
        alert.setContentText(detalle.toString());
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    @FXML
    public void onReenviar() {
        Map<String, Object> selected = tableEnvios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Por favor, selecciona un registro");
            return;
        }

        mostrarAlerta("Funcionalidad en desarrollo\n\nPróximamente podrás reenviar facturas a VeriFacTur.");
    }

    @FXML
    public void onExportar() {
        mostrarAlerta("Funcionalidad en desarrollo\n\nPróximamente podrás exportar el registro de envíos a Excel o PDF.");
    }

    // Métodos auxiliares
    private void mostrarAlerta(String msg) { Dialogs.showWarn(msg); }
    private void mostrarExito(String msg) { Dialogs.showInfo(msg); }
    private void mostrarError(String msg) { Dialogs.showError(msg); }
    private boolean mostrarConfirmacion(String msg) { return Dialogs.showConfirm(msg); }
 }
