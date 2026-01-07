package alicanteweb.erp.controller;

import alicanteweb.erp.entities.EmpresaConfig;
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

    private EmpresaConfig empresaActual;

    public EmpresaConfigController() {
    }

    @FXML
    public void initialize() {
        log.info("=== INICIALIZANDO EmpresaConfigController ===");
        cargarConfiguracion();
        log.info("=== FINALIZÓ INICIALIZACIÓN EmpresaConfigController ===");
    }

    private void cargarConfiguracion() {
        try {
            log.info("Cargando configuración de empresa...");
            // TODO: Cargar desde servicio
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
            // TODO: Guardar configuración
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

