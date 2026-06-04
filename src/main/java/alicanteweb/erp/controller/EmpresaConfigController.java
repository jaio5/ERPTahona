package alicanteweb.erp.controller;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.service.EmpresaConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @FXML private TextField txtRegistroSanitario;
    @FXML private TextField txtRegistroMercantil;
    @FXML private TextField txtWeb;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private ComboBox<String> cmbRegimenIVA;
    @FXML private TextField txtIRPF;
    @FXML private TextField txtIBAN;
    @FXML private TextField txtBanco;
    @FXML private TextField txtSwift;

    private EmpresaConfig empresaActual;
    private final EmpresaConfigService empresaConfigService;

    public EmpresaConfigController(EmpresaConfigService empresaConfigService) {
        this.empresaConfigService = empresaConfigService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando EmpresaConfigController");
        if (cmbRegimenIVA != null) {
            cmbRegimenIVA.getItems().addAll("General", "Recargo", "Exento");
            cmbRegimenIVA.setValue("General");
        }
        if (btnGuardar != null) btnGuardar.setDisable(false);
        if (btnCancelar != null) btnCancelar.setDisable(false);
        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
        try {
            empresaActual = empresaConfigService.getConfiguracionActiva().orElse(null);
            if (empresaActual == null) {
                empresaActual = new EmpresaConfig();
                empresaActual.setNombreEmpresa("Mi Empresa");
                empresaActual.setCif("X0000000X");
                empresaActual.setActivo(true);
            }

            if (txtNombre != null) txtNombre.setText(empresaActual.getNombreEmpresa());
            if (txtCif != null) txtCif.setText(empresaActual.getCif());
            if (txtDireccion != null) txtDireccion.setText(empresaActual.getDireccion());
            if (txtCodigo != null) txtCodigo.setText(empresaActual.getId() != null ? empresaActual.getId().toString() : "");
            if (txtCodigoPostal != null) txtCodigoPostal.setText(empresaActual.getCodigoPostal());
            if (txtPoblacion != null) txtPoblacion.setText(empresaActual.getCiudad());
            if (txtProvincia != null) txtProvincia.setText(empresaActual.getProvincia());
            if (txtTelefono != null) txtTelefono.setText(empresaActual.getTelefono());
            if (txtEmail != null) txtEmail.setText(empresaActual.getEmail());
            if (txtWeb != null) txtWeb.setText(empresaActual.getWeb());
            if (txtRegistroSanitario != null) txtRegistroSanitario.setText(empresaActual.getRegistroSanitario());
            if (txtRegistroMercantil != null) txtRegistroMercantil.setText(empresaActual.getRegistroMercantil());
            if (txtNotas != null) txtNotas.setText("");
            if (txtIRPF != null) txtIRPF.setText("");
            if (txtIBAN != null) txtIBAN.setText("");
            if (txtBanco != null) txtBanco.setText("");
            if (txtSwift != null) txtSwift.setText("");

            log.info("Configuración cargada: {}", empresaActual.getNombreEmpresa());
        } catch (Exception e) {
            log.error("Error cargando configuración", e);
            mostrarError("Error cargando configuración: " + e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            if (empresaActual == null) empresaActual = new EmpresaConfig();
            if (txtNombre != null) empresaActual.setNombreEmpresa(txtNombre.getText());
            if (txtCif != null) empresaActual.setCif(txtCif.getText());
            if (txtDireccion != null) empresaActual.setDireccion(txtDireccion.getText());
            if (txtCodigoPostal != null) empresaActual.setCodigoPostal(txtCodigoPostal.getText());
            if (txtPoblacion != null) empresaActual.setCiudad(txtPoblacion.getText());
            if (txtProvincia != null) empresaActual.setProvincia(txtProvincia.getText());
            if (txtTelefono != null) empresaActual.setTelefono(txtTelefono.getText());
            if (txtEmail != null) empresaActual.setEmail(txtEmail.getText());
            if (txtWeb != null) empresaActual.setWeb(txtWeb.getText());
            if (txtRegistroSanitario != null) empresaActual.setRegistroSanitario(txtRegistroSanitario.getText());
            if (txtRegistroMercantil != null) empresaActual.setRegistroMercantil(txtRegistroMercantil.getText());

            EmpresaConfig saved = empresaConfigService.save(empresaActual);
            empresaActual = saved;
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
}
