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

    @FXML private TextField txtUsername;
    @FXML private Label lblTitulo;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox chkEnabled;
    @FXML private CheckBox chkBloqueado;

    public UsuarioFormController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando UsuarioFormController");
        
        // Configurar roles
        if (cbRole != null) {
            cbRole.getItems().addAll("ADMIN", "USUARIO", "GESTOR", "VENDEDOR");
            cbRole.setValue("USUARIO");
        }

        // Por defecto habilitado
        if (chkEnabled != null) {
            chkEnabled.setSelected(true);
        }

        // Validaciones en tiempo real
        if (txtUsername != null) {
            txtUsername.textProperty().addListener((obs, oldV, newV) -> {
                if (newV != null && !newV.matches("[A-Za-z0-9_]{0,30}")) {
                    txtUsername.setText(oldV);
                }
            });
        }

        if (txtEmail != null) {
            txtEmail.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) { // lost focus
                    String email = txtEmail.getText();
                    if (email != null && !email.isBlank() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        mostrarError("Formato de email inválido");
                    }
                }
            });
        }

        if (txtPassword != null && txtConfirmPassword != null) {
            txtConfirmPassword.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) {
                    String p = txtPassword.getText() != null ? txtPassword.getText() : "";
                    String c = txtConfirmPassword.getText() != null ? txtConfirmPassword.getText() : "";
                    if (!p.isBlank() && p.length() < 6) {
                        mostrarError("La contraseña debe tener al menos 6 caracteres");
                    } else if (!c.isBlank() && !p.equals(c)) {
                        mostrarError("Las contraseñas no coinciden");
                    }
                }
            });
        }
    }

    @Override
    protected void cargarDatos() {
        if (item != null) {
            txtUsername.setText(item.getUsername());
            txtUsername.setDisable(true); // No permitir cambiar el username en edición
            txtNombre.setText(item.getNombre());
            txtEmail.setText(item.getEmail());
            cbRole.setValue(item.getRole());
            chkEnabled.setSelected(item.getEnabled());
            if (chkBloqueado != null) chkBloqueado.setSelected(item.getBloqueado());

            // En modo edición, la contraseña es opcional
            txtPassword.setPromptText("Dejar vacío para mantener la actual");
            txtConfirmPassword.setPromptText("Dejar vacío para mantener la actual");
        }
    }

    @Override
    protected boolean validar() {
        if (txtUsername.getText().trim().isEmpty()) {
            mostrarError("El nombre de usuario es obligatorio");
            return false;
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarError("El email es obligatorio");
            return false;
        }
        
        if (cbRole.getValue() == null) {
            mostrarError("Debe seleccionar un rol");
            return false;
        }
        
        // Validar contraseña solo si se está creando o si se ha introducido una nueva
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (item == null) { // Modo creación
            if (password.isEmpty()) {
                mostrarError("La contraseña es obligatoria");
                return false;
            }
        }
        
        // Si se ha introducido contraseña, validar
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                mostrarError("La contraseña debe tener al menos 6 caracteres");
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

            usuario.setUsername(txtUsername.getText().trim());
            usuario.setNombre(txtNombre.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setRole(cbRole.getValue());
            usuario.setEnabled(chkEnabled != null && chkEnabled.isSelected());
            if (chkBloqueado != null) usuario.setBloqueado(chkBloqueado.isSelected());

            String password = txtPassword.getText();

            if (item == null) {
                // Crear nuevo usuario
                usuarioService.crearUsuario(usuario, password);
            } else {
                // Actualizar usuario existente
                usuarioService.actualizarUsuario(usuario);

                // Si se cambió la contraseña, actualizarla como administrador (sin pedir la antigua)
                if (password != null && !password.isEmpty()) {
                    usuarioService.cambiarPasswordAdmin(usuario.getId(), password);
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
