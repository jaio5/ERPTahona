package alicanteweb.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controlador para la vista de cuentas contables.
 * Explicación para estudiantes DAM:
 * - Este controlador gestiona la lógica de la UI FXML de cuentas contables.
 * - Los métodos @FXML se enlazan con los botones y acciones del FXML.
 * - Se recomienda usar servicios y repositorios para la lógica de negocio y acceso a datos.
 */
public class CuentaContableController {
    @FXML
    private TableView<?> tableCuentas;
    @FXML
    private TableColumn<?, ?> colCodigo;
    @FXML
    private TableColumn<?, ?> colNombre;
    @FXML
    private TableColumn<?, ?> colTipo;
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtTipo;

    // Acción para el botón Volver
    @FXML
    private void handleVolver() {
        // Aquí iría la lógica para volver al panel principal
    }

    // Acción para el botón Nuevo
    @FXML
    private void handleNuevo() {
        txtCodigo.clear();
        txtNombre.clear();
        txtTipo.clear();
        // Aquí puedes preparar la UI para crear una nueva cuenta
    }

    // Acción para el botón Guardar
    @FXML
    private void handleGuardar() {
        // Aquí iría la lógica para guardar la cuenta contable
        // Normalmente se llamaría a un Service que use un Repository JPA
    }

    // Acción para el botón Eliminar
    @FXML
    private void handleEliminar() {
        // Aquí iría la lógica para eliminar la cuenta seleccionada
    }
}

