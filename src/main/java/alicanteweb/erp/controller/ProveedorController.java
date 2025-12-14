package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller
public class ProveedorController {
    private static final Logger log = LoggerFactory.getLogger(ProveedorController.class);

    @FXML private TableView<Proveedor> tableProveedores;
    @FXML private TableColumn<Proveedor, Integer> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colCif;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, String> colDireccion;
    @FXML private TableColumn<Proveedor, String> colCiudad;
    @FXML private TableColumn<Proveedor, String> colProvincia;
    @FXML private TextField txtBuscar;

    private final ProveedorService proveedorService;
    private final ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList();

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNombre()).orElse("")));
        if (colCif != null) colCif.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCif()).orElse("")));
        if (colTelefono != null) colTelefono.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getTelefono()).orElse("")));
        if (colEmail != null) colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getEmail()).orElse("")));
        if (colDireccion != null) colDireccion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getDireccion()).orElse("")));
        if (colCiudad != null) colCiudad.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCiudad()).orElse("")));
        if (colProvincia != null) colProvincia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getProvincia()).orElse("")));

        if (tableProveedores != null) tableProveedores.setItems(proveedoresList);
        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarProveedores(newV));
        }
    }

    private void loadAll() {
        try {
            log.info("Cargando proveedores desde la base de datos...");
            List<Proveedor> todos = proveedorService.findAll();
            log.info("Se encontraron {} proveedores en la base de datos", todos.size());
            proveedoresList.setAll(todos);
            log.info("Proveedores cargados en la lista: {}", proveedoresList.size());

            // Forzar actualización de la tabla en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                if (tableProveedores != null) {
                    tableProveedores.refresh();
                    log.info("Tabla de proveedores refrescada. Items: {}", tableProveedores.getItems().size());
                }
            });
        } catch (Exception e) {
            log.error("Error cargando proveedores", e);
            mostrarError("Error cargando proveedores: " + e.getMessage());
        }
    }

    private void filtrarProveedores(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Proveedor> encontrados = proveedorService.searchByNombre(filtro.trim());
            proveedoresList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error filtrando proveedores", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarFormulario(null);
    }

    @FXML
    public void onEdit() {
        Proveedor seleccionado = tableProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor para editar");
            return;
        }
        mostrarFormulario(seleccionado);
    }

    private void mostrarFormulario(Proveedor proveedor) {
        try {
            // Cargar el FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/proveedor_form.fxml"));
            VBox formRoot = loader.load();

            // Obtener los campos del formulario
            TextField txtNombre = (TextField) formRoot.lookup("#txtNombre");
            TextField txtCif = (TextField) formRoot.lookup("#txtCif");
            TextField txtTelefono = (TextField) formRoot.lookup("#txtTelefono");
            TextField txtEmail = (TextField) formRoot.lookup("#txtEmail");
            TextField txtDireccion = (TextField) formRoot.lookup("#txtDireccion");
            TextField txtCiudad = (TextField) formRoot.lookup("#txtCiudad");
            TextField txtProvincia = (TextField) formRoot.lookup("#txtProvincia");
            TextField txtCp = (TextField) formRoot.lookup("#txtCp");
            TextField txtPais = (TextField) formRoot.lookup("#txtPais");
            Button btnGuardar = (Button) formRoot.lookup("#btnGuardar");
            Button btnCancelar = (Button) formRoot.lookup("#btnCancelar");

            // Si estamos editando, rellenar los campos
            boolean esNuevo = (proveedor == null);
            Proveedor proveedorEditar = esNuevo ? new Proveedor() : proveedor;

            if (!esNuevo) {
                txtNombre.setText(proveedor.getNombre());
                txtCif.setText(proveedor.getCif());
                txtTelefono.setText(proveedor.getTelefono());
                txtEmail.setText(proveedor.getEmail());
                txtDireccion.setText(proveedor.getDireccion());
                txtCiudad.setText(proveedor.getCiudad());
                txtProvincia.setText(proveedor.getProvincia());
                txtCp.setText(proveedor.getCp());
                txtPais.setText(proveedor.getPais());
            }

            // Crear Stage modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(esNuevo ? "Nuevo Proveedor" : "Editar Proveedor");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(formRoot));

            // Configurar botones
            btnGuardar.setOnAction(e -> {
                if (validarFormulario(txtNombre)) {
                    proveedorEditar.setNombre(txtNombre.getText().trim());
                    proveedorEditar.setCif(txtCif.getText() != null ? txtCif.getText().trim() : null);
                    proveedorEditar.setTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : null);
                    proveedorEditar.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : null);
                    proveedorEditar.setDireccion(txtDireccion.getText() != null ? txtDireccion.getText().trim() : null);
                    proveedorEditar.setCiudad(txtCiudad.getText() != null ? txtCiudad.getText().trim() : null);
                    proveedorEditar.setProvincia(txtProvincia.getText() != null ? txtProvincia.getText().trim() : null);
                    proveedorEditar.setCp(txtCp.getText() != null ? txtCp.getText().trim() : null);
                    proveedorEditar.setPais(txtPais.getText() != null ? txtPais.getText().trim() : null);

                    try {
                        proveedorService.save(proveedorEditar);
                        loadAll();
                        mostrarInfo(esNuevo ? "Proveedor creado correctamente" : "Proveedor actualizado correctamente");
                        stage.close();
                    } catch (Exception ex) {
                        log.error("Error guardando proveedor", ex);
                        mostrarError("Error al guardar: " + ex.getMessage());
                    }
                }
            });

            btnCancelar.setOnAction(e -> stage.close());

            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error mostrando formulario", e);
            mostrarError("Error al abrir el formulario: " + e.getMessage());
        }
    }

    private boolean validarFormulario(TextField txtNombre) {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        return true;
    }

    @FXML
    public void onDelete() {
        if (tableProveedores == null) { mostrarError("Tabla no disponible"); return; }
        Proveedor sel = tableProveedores.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarInfo("Selecciona un proveedor"); return; }
        if (sel.getId() == null) { mostrarError("El proveedor seleccionado no tiene id"); return; }

        // Verificar estado actual
        boolean estaActivo = sel.getActivo() == null || sel.getActivo();
        String accion = estaActivo ? "dar de baja" : "activar";
        String mensaje = estaActivo ?
            "¿Estás seguro de dar de baja el proveedor '" + sel.getNombre() + "'?" :
            "¿Estás seguro de activar el proveedor '" + sel.getNombre() + "'?";

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar " + accion);
        confirmacion.setHeaderText(mensaje);
        confirmacion.setContentText("Esta operación cambiará el estado del proveedor.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Cambiar estado
                    sel.setActivo(!estaActivo);
                    proveedorService.save(sel);
                    loadAll();
                    mostrarInfo("Proveedor " + (estaActivo ? "dado de baja" : "activado") + " correctamente");
                } catch (Exception e) {
                    log.error("Error cambiando estado del proveedor", e);
                    mostrarError("Error cambiando estado: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.setHeaderText("Información");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}
