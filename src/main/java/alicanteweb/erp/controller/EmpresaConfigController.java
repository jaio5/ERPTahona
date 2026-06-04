package alicanteweb.erp.controller;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.SifModalidad;
import alicanteweb.erp.service.DeclaracionResponsableService;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.FiscalComplianceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para la Configuración de Empresa
 * Datos fiscales y administrativos de la empresa
 */
@Controller
public class EmpresaConfigController {
    private static final Logger log = LoggerFactory.getLogger(EmpresaConfigController.class);

    @FXML private TextField txtNombre;
    @FXML private TextField txtCif;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtPoblacion;
    @FXML private TextField txtProvincia;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // Campos adicionales definidos en el FXML
    @FXML private ComboBox<String> cmbRegimenIVA;
    @FXML private TextField txtIRPF;
    @FXML private TextField txtIBAN;
    @FXML private TextField txtBanco;
    @FXML private TextField txtSwift;
    @FXML private ComboBox<String> cmbModalidadSif;
    @FXML private TextField txtSifNombre;
    @FXML private TextField txtSifVersion;
    @FXML private TextField txtSifInstalacion;
    @FXML private TextField txtProductorSoftware;
    @FXML private TextField txtNifProductorSoftware;
    @FXML private Label lblDeclaracion;

    private EmpresaConfig empresaActual;

    private final EmpresaConfigService empresaConfigService;
    private final FiscalComplianceService fiscalComplianceService;
    private final DeclaracionResponsableService declaracionResponsableService;

    // Spring injecta el servicio
    public EmpresaConfigController(EmpresaConfigService empresaConfigService,
                                   FiscalComplianceService fiscalComplianceService,
                                   DeclaracionResponsableService declaracionResponsableService) {
        this.empresaConfigService = empresaConfigService;
        this.fiscalComplianceService = fiscalComplianceService;
        this.declaracionResponsableService = declaracionResponsableService;
    }

    @FXML
    public void initialize() {
        log.info("=== INICIALIZANDO EmpresaConfigController ===");
        // Inicializar valores por defecto de controles que pueden ser nulos en diseño
        if (cmbRegimenIVA != null) {
            cmbRegimenIVA.getItems().addAll("General", "Recargo", "Exento");
            cmbRegimenIVA.setValue("General");
        }
        if (cmbModalidadSif != null) {
            cmbModalidadSif.getItems().setAll(SifModalidad.VERIFACTU.name(), SifModalidad.NO_VERIFACTU.name());
            cmbModalidadSif.setValue(SifModalidad.VERIFACTU.name());
        }
        // Asegurarnos de que los botones estén habilitados por defecto
        if (btnGuardar != null) btnGuardar.setDisable(false);
        if (btnCancelar != null) btnCancelar.setDisable(false);

        cargarConfiguracion();
        log.info("=== FINALIZÓ INICIALIZACIÓN EmpresaConfigController ===");
    }

    private void cargarConfiguracion() {
        try {
            log.info("Cargando configuración de empresa...");
            // Intentar cargar configuración activa desde servicio
            empresaActual = empresaConfigService.getConfiguracionActiva().orElse(null);

            // Si no hay configuración en BD, inicializamos con valores por defecto
            if (empresaActual == null) {
                empresaActual = new EmpresaConfig();
                empresaActual.setNombreEmpresa("Mi Empresa");
                empresaActual.setCif("X0000000X");
                empresaActual.setDireccion("");
                empresaActual.setCodigoPostal("");
                empresaActual.setCiudad("");
                empresaActual.setProvincia("");
                empresaActual.setTelefono("");
                empresaActual.setEmail("");
                empresaActual.setActivo(true);
            }

            // Rellenar controles si existen
            if (txtNombre != null) txtNombre.setText(empresaActual.getNombreEmpresa());
            if (txtCif != null) txtCif.setText(empresaActual.getCif());
            if (txtDireccion != null) txtDireccion.setText(empresaActual.getDireccion());
            if (txtCodigo != null) txtCodigo.setText(empresaActual.getId() != null ? empresaActual.getId().toString() : "");
            if (txtCodigoPostal != null) txtCodigoPostal.setText(empresaActual.getCodigoPostal());
            if (txtPoblacion != null) txtPoblacion.setText(empresaActual.getCiudad());
            if (txtProvincia != null) txtProvincia.setText(empresaActual.getProvincia());
            if (txtTelefono != null) txtTelefono.setText(empresaActual.getTelefono());
            if (txtEmail != null) txtEmail.setText(empresaActual.getEmail());

            // Campos bancarios/fiscales opcionales: si vienen de BD, mostrarlos
            if (txtIRPF != null) txtIRPF.setText(empresaActual.getRegistroSanitario() != null ? empresaActual.getRegistroSanitario() : "");
            if (txtIBAN != null) txtIBAN.setText(empresaActual.getWeb() != null ? empresaActual.getWeb() : "");
            if (txtBanco != null) txtBanco.setText("");
            if (txtSwift != null) txtSwift.setText("");
            if (txtNotas != null) txtNotas.setText("");
            if (cmbModalidadSif != null) cmbModalidadSif.setValue(empresaActual.getSifModalidad() != null ? empresaActual.getSifModalidad().name() : SifModalidad.VERIFACTU.name());
            if (txtSifNombre != null) txtSifNombre.setText(empresaActual.getVerifactuNombreSistema());
            if (txtSifVersion != null) txtSifVersion.setText(empresaActual.getVerifactuVersionSistema());
            if (txtSifInstalacion != null) txtSifInstalacion.setText(empresaActual.getVerifactuIdDispositivo());
            if (txtProductorSoftware != null) txtProductorSoftware.setText(empresaActual.getProductorSoftware());
            if (txtNifProductorSoftware != null) txtNifProductorSoftware.setText(empresaActual.getNifProductorSoftware());
            if (lblDeclaracion != null) {
                lblDeclaracion.setText(Boolean.TRUE.equals(empresaActual.getDeclaracionResponsableEmitida())
                    ? "Declaracion emitida: " + valor(empresaActual.getDeclaracionResponsableVersion()) + " | " + valor(empresaActual.getDeclaracionResponsableHash())
                    : "Declaracion responsable pendiente");
            }

            log.info("Configuración cargada");
        } catch (Exception e) {
            log.error("Error cargando configuración", e);
            mostrarError("Error cargando configuración: " + e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            log.info("Guardando configuración de empresa...");

            // Crear o actualizar empresaActual desde controles
            if (empresaActual == null) empresaActual = new EmpresaConfig();
            if (txtNombre != null) empresaActual.setNombreEmpresa(txtNombre.getText());
            if (txtCif != null) empresaActual.setCif(txtCif.getText());
            if (txtDireccion != null) empresaActual.setDireccion(txtDireccion.getText());
            if (txtCodigoPostal != null) empresaActual.setCodigoPostal(txtCodigoPostal.getText());
            if (txtPoblacion != null) empresaActual.setCiudad(txtPoblacion.getText());
            if (txtProvincia != null) empresaActual.setProvincia(txtProvincia.getText());
            if (txtTelefono != null) empresaActual.setTelefono(txtTelefono.getText());
            if (txtEmail != null) empresaActual.setEmail(txtEmail.getText());
            if (txtCodigo != null) log.info("Código visible: {}", txtCodigo.getText());

            // Registrar valores de campos opcionales
            if (txtNotas != null) empresaActual.setRegistroMercantil(txtNotas.getText());
            if (cmbRegimenIVA != null) log.info("Régimen IVA seleccionado: {}", cmbRegimenIVA.getValue());
            if (txtIRPF != null) log.info("IRPF: {}", txtIRPF.getText());
            if (txtIBAN != null) log.info("IBAN: {}", txtIBAN.getText());
            if (txtBanco != null) log.info("Banco: {}", txtBanco.getText());
            if (txtSwift != null) log.info("SWIFT: {}", txtSwift.getText());
            if (cmbModalidadSif != null && cmbModalidadSif.getValue() != null) {
                empresaActual.setSifModalidad(SifModalidad.valueOf(cmbModalidadSif.getValue()));
            }
            if (txtSifNombre != null) empresaActual.setVerifactuNombreSistema(txtSifNombre.getText());
            if (txtSifVersion != null) empresaActual.setVerifactuVersionSistema(txtSifVersion.getText());
            if (txtSifInstalacion != null) empresaActual.setVerifactuIdDispositivo(txtSifInstalacion.getText());
            if (txtProductorSoftware != null) empresaActual.setProductorSoftware(txtProductorSoftware.getText());
            if (txtNifProductorSoftware != null) empresaActual.setNifProductorSoftware(txtNifProductorSoftware.getText());

            // Persistir empresaActual usando EmpresaConfigService
            EmpresaConfig saved = empresaConfigService.save(empresaActual);
            empresaActual = saved; // actualizar referencia

            log.info("Empresa guardada con id: {}", saved.getId());

            mostrarInfo("Configuración guardada exitosamente");
        } catch (Exception e) {
            log.error("Error guardando configuración", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cargarConfiguracion();
        mostrarInfo("Cambios cancelados");
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
        mostrarInfo(detalle.toString());
    }

    @FXML
    public void onGenerarDeclaracionResponsable() {
        try {
            onGuardar();
            java.io.File pdf = declaracionResponsableService.generarDeclaracionResponsable();
            cargarConfiguracion();
            mostrarInfo("Declaracion responsable generada:\n" + pdf.getAbsolutePath());
        } catch (Exception e) {
            log.error("Error generando declaracion responsable", e);
            mostrarError(e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error en la operación");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String valor(Object value) {
        return value != null ? value.toString() : "";
    }
}
