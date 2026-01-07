package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controlador para la gestión de Usuarios
 */
@Controller
public class UsuarioController {
    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, Long> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRole;
    @FXML private TextField txtBuscar;

    private final UsuarioService usuarioService;
    private final ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando UsuarioController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarUsuarios(newV));
        }
    }

    private void configurarColumnas() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        }
        if (colUsername != null) {
            colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getUsername()).orElse("")));
        }
        if (colNombre != null) {
            colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNombre()).orElse("")));
        }
        if (colEmail != null) {
            colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getEmail()).orElse("")));
        }
        if (colRole != null) {
            colRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getRole()).map(Object::toString).orElse("")));
        }
        if (tableUsuarios != null) {
            tableUsuarios.setItems(usuariosList);
        }
    }

    private void cargarDatos() {
        try {
            usuariosList.clear();
            usuariosList.addAll(usuarioService.listarTodos());
            log.info("Usuarios cargados: {}", usuariosList.size());
            javafx.application.Platform.runLater(() -> {
                if (tableUsuarios != null) tableUsuarios.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando usuarios", e);
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void filtrarUsuarios(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<Usuario> filtered = FXCollections.observableArrayList(
            usuariosList.stream()
                .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(search)) ||
                             (u.getNombre() != null && u.getNombre().toLowerCase().contains(search)))
                .toList()
        );
        tableUsuarios.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        mostrarAlerta("Crear nuevo usuario en desarrollo");
    }

    @FXML
    public void onEditar() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Seleccione un usuario para editar");
            return;
        }
        mostrarAlerta("Edición de usuario en desarrollo");
    }

    @FXML
    public void onEliminar() {
        Usuario usuario = tableUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Seleccione un usuario para eliminar");
            return;
        }
        mostrarAlerta("Eliminación de usuarios no permitida en este momento");
    }

    @FXML
    public void onRefresh() {
        cargarDatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

