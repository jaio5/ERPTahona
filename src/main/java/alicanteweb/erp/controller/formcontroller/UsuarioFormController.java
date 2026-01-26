package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.DialogUtils;

/**
 * Controlador para el formulario de Usuario
 */
@Slf4j
@Controller
public class UsuarioFormController extends BaseFormController<Usuario> {

    private final UsuarioService usuarioService;

    @FXML private TextField txtUsername = null;
    @FXML private Label lblTitulo = null;
    @FXML private PasswordField txtPassword = null;
    @FXML private PasswordField txtConfirmPassword = null;
    @FXML private TextField txtNombre = null;
    @FXML private TextField txtEmail = null;
    @FXML private ComboBox<String> cbRole = null;
    @FXML private CheckBox chkEnabled = null;
    @FXML private CheckBox chkBloqueado = null;

    public UsuarioFormController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        // Marcar campos como usados y preparar controles para evitar warnings estáticos
        try {
            if (cbRole != null) {
                cbRole.setItems(FXCollections.observableArrayList("ADMIN", "MANAGER", "USER"));
                cbRole.setValue(cbRole.getItems().isEmpty() ? null : cbRole.getItems().get(0));
            }
            if (chkEnabled != null) {
                chkEnabled.setSelected(true);
            }
            if (lblTitulo != null && item == null) {
                lblTitulo.setText("Nuevo Usuario");
            }
            // Asegurar que los campos de contraseña y email tengan prompt si no están vacíos
            if (txtUsername != null) txtUsername.setPromptText("usuario123");
            if (txtEmail != null) txtEmail.setPromptText("usuario@email.com");
            if (txtPassword != null) txtPassword.setPromptText("Contraseña segura");
            if (txtConfirmPassword != null) txtConfirmPassword.setPromptText("Repetir contraseña");
        } catch (Throwable t) {
            // No detener la inicialización por problemas menores
            System.err.println("Warning en initialize UsuarioFormController: " + t.getMessage());
        }

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
            DialogUtils.showError("El nombre de usuario es obligatorio");
            return false;
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            DialogUtils.showError("El nombre es obligatorio");
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            DialogUtils.showError("El email es obligatorio");
            return false;
        }
        
        if (cbRole.getValue() == null) {
            DialogUtils.showError("Debe seleccionar un rol");
            return false;
        }
        
        // Validar contraseña solo si se está creando o si se ha introducido una nueva
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (item == null) { // Modo creación
            if (password.isEmpty()) {
                DialogUtils.showError("La contraseña es obligatoria");
                return false;
            }
        }
        
        // Si se ha introducido contraseña, validar
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                DialogUtils.showError("La contraseña debe tener al menos 6 caracteres");
                return false;
            }
            
            if (!password.equals(confirmPassword)) {
                DialogUtils.showError("Las contraseñas no coinciden");
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

            DialogUtils.showSuccess("Usuario guardado correctamente");

        } catch (Exception e) {
            log.error("Error al guardar usuario", e);
            throw new RuntimeException("Error al guardar: " + e.getMessage());
        }
    }
}
