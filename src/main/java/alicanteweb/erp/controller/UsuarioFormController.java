package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

/**
 * Controlador para el formulario de Usuario
 */
@Slf4j
@Controller
public class UsuarioFormController extends BaseFormController<Usuario> {

    private final UsuarioService usuarioService;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private CheckBox enabledCheckBox;

    public UsuarioFormController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando UsuarioFormController");
        
        // Configurar roles
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("ADMIN", "USUARIO", "GESTOR", "VENDEDOR");
            roleComboBox.setValue("USUARIO");
        }

        // Por defecto habilitado
        if (enabledCheckBox != null) {
            enabledCheckBox.setSelected(true);
        }
    }

    @Override
    protected void cargarDatos() {
        if (item != null) {
            usernameField.setText(item.getUsername());
            usernameField.setDisable(true); // No permitir cambiar el username en edición
            nombreField.setText(item.getNombre());
            emailField.setText(item.getEmail());
            roleComboBox.setValue(item.getRole());
            enabledCheckBox.setSelected(item.getEnabled());

            // En modo edición, la contraseña es opcional
            passwordField.setPromptText("Dejar vacío para mantener la actual");
            confirmPasswordField.setPromptText("Dejar vacío para mantener la actual");
        }
    }

    @Override
    protected boolean validar() {
        if (usernameField.getText().trim().isEmpty()) {
            mostrarError("El nombre de usuario es obligatorio");
            return false;
        }
        
        if (nombreField.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            mostrarError("El email es obligatorio");
            return false;
        }
        
        if (roleComboBox.getValue() == null) {
            mostrarError("Debe seleccionar un rol");
            return false;
        }
        
        // Validar contraseña solo si se está creando o si se ha introducido una nueva
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (item == null) { // Modo creación
            if (password.isEmpty()) {
                mostrarError("La contraseña es obligatoria");
                return false;
            }
        }
        
        // Si se ha introducido contraseña, validar
        if (!password.isEmpty()) {
            if (password.length() < 4) {
                mostrarError("La contraseña debe tener al menos 4 caracteres");
                return false;
            }
            
            if (!password.equals(confirmPassword)) {
                mostrarError("Las contraseñas no coinciden");
                return false;
            }
        }
        
        return true;
    }

    @Override
    protected void guardarItem() {
        try {
            Usuario usuario = item != null ? item : new Usuario();

            usuario.setUsername(usernameField.getText().trim());
            usuario.setNombre(nombreField.getText().trim());
            usuario.setEmail(emailField.getText().trim());
            usuario.setRole(roleComboBox.getValue());
            usuario.setEnabled(enabledCheckBox.isSelected());
            
            String password = passwordField.getText();

            if (item == null) {
                // Crear nuevo usuario
                usuarioService.crearUsuario(usuario, password);
            } else {
                // Actualizar usuario existente
                usuarioService.actualizarUsuario(usuario);

                // Si se cambió la contraseña, actualizarla
                if (!password.isEmpty()) {
                    usuarioService.cambiarPassword(usuario.getId(), password, password);
                }
            }

            mostrarInfo("Usuario guardado correctamente");

        } catch (Exception e) {
            log.error("Error al guardar usuario", e);
            throw new RuntimeException("Error al guardar: " + e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

