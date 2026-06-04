package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.AutenticacionService;
import alicanteweb.erp.service.UsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private javafx.scene.layout.HBox errorContainer;

    @FXML
    private Button loginButton;

    private final AutenticacionService autenticacionService;
    private final UsuarioService usuarioService;
    private final ConfigurableApplicationContext springContext;

    public LoginController(AutenticacionService autenticacionService,
                          UsuarioService usuarioService,
                          ConfigurableApplicationContext springContext) {
        this.autenticacionService = autenticacionService;
        this.usuarioService = usuarioService;
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        log.info("LoginController inicializado");

        // Limpiar error al escribir
        usernameField.textProperty().addListener((obs, old, newVal) -> hideError());
        passwordField.textProperty().addListener((obs, old, newVal) -> hideError());

        // Focus en username al iniciar
        Platform.runLater(() -> usernameField.requestFocus());
    }

    /**
     * Maneja eventos de teclado (Enter para navegar entre campos)
     */
    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.getSource() == usernameField) {
                // Si está en usuario, pasar a contraseña
                passwordField.requestFocus();
            } else if (event.getSource() == passwordField) {
                // Si está en contraseña, hacer login
                handleLogin();
            }
            event.consume();
        }
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        log.info("Intento de login: {}", username);

        // Validar campos vacíos
        if (username == null || username.trim().isEmpty()) {
            showError("Por favor, introduce tu usuario");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Por favor, introduce tu contraseña");
            passwordField.requestFocus();
            return;
        }

        // Deshabilitar botón durante autenticación
        loginButton.setDisable(true);
        loginButton.setText("Autenticando...");

        try {
            // Autenticar
            Usuario usuario = autenticacionService.login(username, password);

            if (usuario != null) {
                log.info("✅ Login exitoso: {}", username);

                if (Boolean.TRUE.equals(usuario.getRequiereCambioPassword()) && !forzarCambioPassword(usuario)) {
                    autenticacionService.logout();
                    loginButton.setDisable(false);
                    loginButton.setText("Iniciar SesiÃ³n");
                    return;
                }

                // Login exitoso - abrir panel principal
                Platform.runLater(() -> {
                    try {
                        abrirPanelPrincipal(usuario);
                    } catch (Exception e) {
                        log.error("Error al abrir panel principal", e);
                        showError("Error al cargar la aplicación: " + e.getMessage());
                        loginButton.setDisable(false);
                        loginButton.setText("Iniciar Sesión");
                    }
                });

            } else {
                log.warn("❌ Login fallido: {}", username);
                showError("Usuario o contraseña incorrectos");
                passwordField.clear();
                passwordField.requestFocus();
                loginButton.setDisable(false);
                loginButton.setText("Iniciar Sesión");
            }

        } catch (Exception e) {
            log.error("❌ Error en autenticación", e);
            showError("Error en el servidor. Intenta de nuevo.");
            loginButton.setDisable(false);
            loginButton.setText("Iniciar Sesión");
        }
    }

    private void abrirPanelPrincipal(Usuario usuario) throws Exception {
        log.info("Abriendo panel principal para: {}", usuario.getUsername());

        // Cargar panel principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_panel.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();

        // Crear nueva escena
        Scene scene = new Scene(root, 1400, 900);

        // Obtener el stage actual
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setTitle("ERP Panadería Tahona - " + usuario.getNombre());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.centerOnScreen();

        log.info("✅ Panel principal cargado exitosamente");
    }

    private boolean forzarCambioPassword(Usuario usuario) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cambio de contrasena obligatorio");
        dialog.setHeaderText("Debe cambiar la contrasena temporal antes de entrar.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField nueva = new PasswordField();
        nueva.setPromptText("Nueva contrasena");
        PasswordField confirmar = new PasswordField();
        confirmar.setPromptText("Confirmar contrasena");
        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(8,
                new Label("Nueva contrasena:"), nueva,
                new Label("Confirmar contrasena:"), confirmar));

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String password = nueva.getText() != null ? nueva.getText() : "";
            if (password.length() < 12) {
                showError("La nueva contrasena debe tener al menos 12 caracteres");
                event.consume();
                return;
            }
            if (!password.equals(confirmar.getText())) {
                showError("Las contrasenas no coinciden");
                event.consume();
            }
        });

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            usuarioService.cambiarPasswordAdmin(usuario.getId(), nueva.getText());
            return true;
        }
        return false;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
        if (errorContainer != null) {
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
        }
    }

    private void hideError() {
        if (errorContainer != null) {
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
        }
    }
}


