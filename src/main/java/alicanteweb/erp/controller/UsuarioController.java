package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controlador para la gestión de Usuarios
 */
@Controller
public class UsuarioController {
    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRole;
    @FXML private TableColumn<Usuario, String> colActivo;
    @FXML private TableColumn<Usuario, String> colUltimoAcceso;
    @FXML private TableColumn<Usuario, Void> colAcciones;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroRol;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Label lblTotal;
    @FXML private Label lblSeleccion;

    private final UsuarioService usuarioService;
    private final ApplicationContext context;
    private final ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuariosFilteredList = FXCollections.observableArrayList();

    public UsuarioController(UsuarioService usuarioService, ApplicationContext context) {
        this.usuarioService = usuarioService;
        this.context = context;
    }

    @FXML
    public void initialize() {
        log.info("✅ Inicializando UsuarioController");
        configurarColumnas();
        configurarFiltros();
        configurarListeners();
        cargarDatos();
    }

    private void configurarColumnas() {
        // Configurar columnas básicas
        colUsername.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getUsername()));

        colNombre.setCellValueFactory(cellData ->
            new SimpleStringProperty(Optional.ofNullable(cellData.getValue().getNombre()).orElse("-")));

        colEmail.setCellValueFactory(cellData ->
            new SimpleStringProperty(Optional.ofNullable(cellData.getValue().getEmail()).orElse("-")));

        colRole.setCellValueFactory(cellData -> {
            String role = cellData.getValue().getRole();
            String roleText = role != null ? role : "N/A";
            String emoji = switch (roleText.toUpperCase()) {
                case "ADMIN" -> "👑 ADMIN";
                case "MANAGER" -> "👔 MANAGER";
                case "USER" -> "👤 USUARIO";
                default -> "❓ " + roleText;
            };
            return new SimpleStringProperty(emoji);
        });

        colActivo.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String estado;
            if (Boolean.TRUE.equals(usuario.getBloqueado())) {
                estado = "🔒 Bloqueado";
            } else if (Boolean.TRUE.equals(usuario.getEnabled())) {
                estado = "✅ Activo";
            } else {
                estado = "❌ Inactivo";
            }
            return new SimpleStringProperty(estado);
        });

        colUltimoAcceso.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getUltimoAcceso();
            String texto = fecha != null ? fecha.format(DATE_FORMATTER) : "Nunca";
            return new SimpleStringProperty(texto);
        });

        // Configurar columna de acciones con botones
        configurarColumnaAcciones();

        tableUsuarios.setItems(usuariosFilteredList);
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnEditar = new Button("✏️");

            {
                btnVer.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");
                btnEditar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 5;");

                btnVer.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    mostrarDetalleUsuario(usuario);
                });

                btnEditar.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    editarUsuario(usuario);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, btnVer, btnEditar);
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void configurarFiltros() {
        // Configurar ComboBox de roles
        if (cmbFiltroRol != null) {
            cmbFiltroRol.setItems(FXCollections.observableArrayList(
                "Todos los roles", "👑 ADMIN", "👔 MANAGER", "👤 USUARIO"
            ));
            cmbFiltroRol.setValue("Todos los roles");
        }

        // Configurar ComboBox de estados
        if (cmbFiltroEstado != null) {
            cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos los estados", "✅ Activos", "❌ Inactivos", "🔒 Bloqueados"
            ));
            cmbFiltroEstado.setValue("Todos los estados");
        }
    }

    private void configurarListeners() {
        // Listener para búsqueda en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }

        // Listeners para filtros
        if (cmbFiltroRol != null) {
            cmbFiltroRol.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }
        if (cmbFiltroEstado != null) {
            cmbFiltroEstado.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }

        // Listener para selección de tabla
        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (lblSeleccion != null) {
                if (newV != null) {
                    lblSeleccion.setText("Seleccionado: " + newV.getUsername() + " (" + newV.getNombre() + ")");
                } else {
                    lblSeleccion.setText("Ningún usuario seleccionado");
                }
            }
        });
    }

    private void cargarDatos() {
        try {
            log.info("📊 Cargando usuarios desde la base de datos...");
            usuariosList.clear();
            usuariosList.addAll(usuarioService.listarTodos());
            aplicarFiltros();
            actualizarContador();
            log.info("✅ {} usuarios cargados correctamente", usuariosList.size());
        } catch (Exception e) {
            log.error("❌ Error cargando usuarios", e);
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        usuariosFilteredList.clear();

        String busqueda = txtBuscar != null ? txtBuscar.getText().toLowerCase() : "";
        String filtroRol = cmbFiltroRol != null ? cmbFiltroRol.getValue() : "Todos los roles";
        String filtroEstado = cmbFiltroEstado != null ? cmbFiltroEstado.getValue() : "Todos los estados";

        usuariosFilteredList.addAll(usuariosList.stream()
            .filter(u -> {
                // Filtro de búsqueda
                if (!busqueda.isEmpty()) {
                    String username = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                    String nombre = u.getNombre() != null ? u.getNombre().toLowerCase() : "";
                    String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                    if (!username.contains(busqueda) && !nombre.contains(busqueda) && !email.contains(busqueda)) {
                        return false;
                    }
                }

                // Filtro de rol
                if (filtroRol != null && !filtroRol.equals("Todos los roles")) {
                    String rolUsuario = u.getRole() != null ? u.getRole() : "";
                    if (filtroRol.contains("ADMIN") && !rolUsuario.equals("ADMIN")) return false;
                    if (filtroRol.contains("MANAGER") && !rolUsuario.equals("MANAGER")) return false;
                    if (filtroRol.contains("USUARIO") && !rolUsuario.equals("USER")) return false;
                }

                // Filtro de estado
                if (filtroEstado != null && !filtroEstado.equals("Todos los estados")) {
                    if (filtroEstado.contains("Activos") && !Boolean.TRUE.equals(u.getEnabled())) return false;
                    if (filtroEstado.contains("Inactivos") && Boolean.TRUE.equals(u.getEnabled())) return false;
                    return !filtroEstado.contains("Bloqueados") || Boolean.TRUE.equals(u.getBloqueado());
                }

                return true;
            })
            .toList()
        );

        actualizarContador();
        Platform.runLater(() -> tableUsuarios.refresh());
    }

    private void actualizarContador() {
        if (lblTotal != null) {
            int total = usuariosList.size();
            int mostrados = usuariosFilteredList.size();
            if (total == mostrados) {
                lblTotal.setText(total + " usuarios registrados");
            } else {
                lblTotal.setText(mostrados + " de " + total + " usuarios");
            }
        }
    }

    @FXML
    public void onNuevo() {
        try {
            log.info("🆕 Abriendo formulario de nuevo usuario");
            abrirFormulario(null);
        } catch (Exception e) {
            log.error("❌ Error abriendo formulario de usuario", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onEditar() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Por favor, seleccione un usuario para editar");
            return;
        }
        editarUsuario(usuario);
    }

    private void editarUsuario(Usuario usuario) {
        try {
            log.info("✏️ Editando usuario: {}", usuario.getUsername());
            abrirFormulario(usuario);
        } catch (Exception e) {
            log.error("❌ Error editando usuario", e);
            mostrarError("Error al editar usuario: " + e.getMessage());
        }
    }

    @FXML
    public void onCambiarPassword() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Por favor, seleccione un usuario");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Cambiar contraseña de: " + usuario.getUsername());
        dialog.setContentText("Nueva contraseña:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (password.length() < 6) {
                mostrarError("La contraseña debe tener al menos 6 caracteres");
                return;
            }
            try {
                // Cambiar password (como admin, no necesitamos la contraseña anterior)
                usuarioService.cambiarPassword(usuario.getId(), null, password);
                mostrarExito("Contraseña cambiada correctamente");
                log.info("✅ Contraseña cambiada para usuario: {}", usuario.getUsername());
            } catch (Exception e) {
                log.error("❌ Error cambiando contraseña", e);
                mostrarError("Error al cambiar contraseña: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onDesbloquear() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Por favor, seleccione un usuario");
            return;
        }

        if (!Boolean.TRUE.equals(usuario.getBloqueado())) {
            mostrarAlerta("El usuario no está bloqueado");
            return;
        }

        if (mostrarConfirmacion("¿Desbloquear usuario " + usuario.getUsername() + "?")) {
            try {
                usuario.setBloqueado(false);
                usuario.setIntentosFallidos(0);
                usuario.setFechaBloqueo(null);
                usuarioService.actualizarUsuario(usuario);
                cargarDatos();
                mostrarExito("Usuario desbloqueado correctamente");
                log.info("✅ Usuario desbloqueado: {}", usuario.getUsername());
            } catch (Exception e) {
                log.error("❌ Error desbloqueando usuario", e);
                mostrarError("Error al desbloquear usuario: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onDesactivar() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Por favor, seleccione un usuario");
            return;
        }

        String accion = Boolean.TRUE.equals(usuario.getEnabled()) ? "desactivar" : "activar";
        if (mostrarConfirmacion("¿" + accion.substring(0, 1).toUpperCase() + accion.substring(1) + " usuario " + usuario.getUsername() + "?")) {
            try {
                usuario.setEnabled(!Boolean.TRUE.equals(usuario.getEnabled()));
                usuarioService.actualizarUsuario(usuario);
                cargarDatos();
                mostrarExito("Usuario " + accion + "do correctamente");
                log.info("✅ Usuario {}: {}", accion, usuario.getUsername());
            } catch (Exception e) {
                log.error("❌ Error {} usuario", accion, e);
                mostrarError("Error al " + accion + " usuario: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onEliminar() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Por favor, seleccione un usuario para eliminar");
            return;
        }

        if ("ADMIN".equals(usuario.getRole())) {
            mostrarError("No se puede eliminar un usuario administrador");
            return;
        }

        if (mostrarConfirmacion("⚠️ ¿Está seguro de eliminar el usuario " + usuario.getUsername() + "?\n\nEsta acción no se puede deshacer.")) {
            try {
                usuarioService.eliminarUsuario(usuario.getId());
                cargarDatos();
                mostrarExito("Usuario eliminado correctamente");
                log.info("✅ Usuario eliminado: {}", usuario.getUsername());
            } catch (Exception e) {
                log.error("❌ Error eliminando usuario", e);
                mostrarError("Error al eliminar usuario: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("🔄 Refrescando lista de usuarios");
        cargarDatos();
    }

    private void mostrarDetalleUsuario(Usuario usuario) {
        StringBuilder detalle = new StringBuilder();
        detalle.append("👤 Usuario: ").append(usuario.getUsername()).append("\n\n");
        detalle.append("📝 Nombre: ").append(Optional.ofNullable(usuario.getNombre()).orElse("-")).append("\n");
        detalle.append("📧 Email: ").append(Optional.ofNullable(usuario.getEmail()).orElse("-")).append("\n");
        detalle.append("👔 Rol: ").append(usuario.getRole()).append("\n");
        detalle.append("✅ Estado: ").append(Boolean.TRUE.equals(usuario.getEnabled()) ? "Activo" : "Inactivo").append("\n");
        detalle.append("🔒 Bloqueado: ").append(Boolean.TRUE.equals(usuario.getBloqueado()) ? "Sí" : "No").append("\n");
        detalle.append("🔢 Intentos fallidos: ").append(usuario.getIntentosFallidos()).append("\n");

        if (usuario.getUltimoAcceso() != null) {
            detalle.append("🕐 Último acceso: ").append(usuario.getUltimoAcceso().format(DATE_FORMATTER)).append("\n");
        }
        if (usuario.getFechaCreacion() != null) {
            detalle.append("📅 Fecha creación: ").append(usuario.getFechaCreacion().format(DATE_FORMATTER)).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del Usuario");
        alert.setHeaderText("Información completa");
        alert.setContentText(detalle.toString());
        alert.showAndWait();
    }

    private void abrirFormulario(Usuario usuario) {
        try {
            log.info("Abriendo formulario de usuario: {}", usuario != null ? usuario.getUsername() : "nuevo");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/usuario_form.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            UsuarioFormController controller = loader.getController();
            controller.setItem(usuario);
            controller.setCallback(this::cargarDatos);

            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error al abrir formulario de usuario", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }


    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

