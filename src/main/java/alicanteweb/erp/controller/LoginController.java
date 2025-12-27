package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.AutenticacionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private CheckBox rememberMeCheckbox;

    private final AutenticacionService autenticacionService;
    private final ConfigurableApplicationContext springContext;

    public LoginController(AutenticacionService autenticacionService,
                          ConfigurableApplicationContext springContext) {
        this.autenticacionService = autenticacionService;
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        log.info("LoginController inicializado");

        // Enter en password también hace login
        passwordField.setOnAction(event -> handleLogin());

        // Limpiar error al escribir
        usernameField.textProperty().addListener((obs, old, newVal) -> hideError());
        passwordField.textProperty().addListener((obs, old, newVal) -> hideError());
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
                log.info("Login exitoso: {}", username);

                // Login exitoso - abrir panel principal
                Platform.runLater(() -> {
                    try {
                        abrirPanelPrincipal(usuario);
                    } catch (Exception e) {
                        log.error("Error al abrir panel principal", e);
                        showError("Error al cargar la aplicación");
                        loginButton.setDisable(false);
                        loginButton.setText("Iniciar Sesión");
                    }
                });

            } else {
                log.warn("Login fallido: {}", username);
                showError("Usuario o contraseña incorrectos");
                passwordField.clear();
                passwordField.requestFocus();
                loginButton.setDisable(false);
                loginButton.setText("Iniciar Sesión");
            }

        } catch (Exception e) {
            log.error("Error en autenticación", e);
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
        Scene scene = new Scene(root, 1200, 800);

        // Obtener el stage actual
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setTitle("ERP Panadería Tahona - " + usuario.getNombreCompleto());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.centerOnScreen();

        log.info("Panel principal cargado exitosamente");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Efecto de shake (opcional)
        // Aquí podrías agregar una animación
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

