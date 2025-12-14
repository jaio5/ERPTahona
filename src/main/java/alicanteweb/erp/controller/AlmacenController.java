package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
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
public class AlmacenController {
    private static final Logger log = LoggerFactory.getLogger(AlmacenController.class);

    @FXML private TableView<Almacen> tableAlmacenes;
    @FXML private TableColumn<Almacen, Long> colId;
    @FXML private TableColumn<Almacen, String> colCodigo;
    @FXML private TableColumn<Almacen, String> colNombre;
    @FXML private TextField txtBuscar;

    private final AlmacenService almacenService;
    private final ObservableList<Almacen> almacenesList = FXCollections.observableArrayList();

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigo()).orElse("")));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> {
            Almacen a = cell.getValue();
            if (a == null) return new SimpleStringProperty("");
            String nombre = Optional.ofNullable(a.getNombre()).orElse("");
            // Agregar indicador si está inactivo
            if (a.getActivo() != null && !a.getActivo()) {
                nombre = "❌ " + nombre + " (INACTIVO)";
            }
            return new SimpleStringProperty(nombre);
        });

        if (tableAlmacenes != null) tableAlmacenes.setItems(almacenesList);
        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlmacenes(newV));
        }
    }

    private void loadAll() {
        try {
            log.info("Cargando almacenes desde la base de datos...");
            List<Almacen> todos = almacenService.findAll();
            log.info("Se encontraron {} almacenes en la base de datos", todos.size());
            almacenesList.setAll(todos);
            log.info("Almacenes cargados en la lista: {}", almacenesList.size());

            // Forzar actualización de la tabla en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                if (tableAlmacenes != null) {
                    tableAlmacenes.refresh();
                    log.info("Tabla de almacenes refrescada. Items: {}", tableAlmacenes.getItems().size());
                }
            });
        } catch (Exception e) {
            log.error("Error cargando almacenes", e);
            mostrarError("Error cargando almacenes: " + e.getMessage());
        }
    }

    private void filtrarAlmacenes(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Almacen> todos = almacenService.findAll();
            List<Almacen> encontrados = todos.stream()
                .filter(a -> (a.getCodigo() != null && a.getCodigo().toLowerCase().contains(filtro.toLowerCase())) ||
                            (a.getNombre() != null && a.getNombre().toLowerCase().contains(filtro.toLowerCase())))
                .toList();
            almacenesList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error filtrando almacenes", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarFormulario(null);
    }

    @FXML
    public void onEdit() {
        Almacen seleccionado = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un almacén para editar");
            return;
        }
        mostrarFormulario(seleccionado);
    }

    private void mostrarFormulario(Almacen almacen) {
        try {
            // Cargar el FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/almacen_form.fxml"));
            VBox formRoot = loader.load();

            // Obtener los campos del formulario
            TextField txtCodigo = (TextField) formRoot.lookup("#txtCodigo");
            TextField txtNombre = (TextField) formRoot.lookup("#txtNombre");
            Button btnGuardar = (Button) formRoot.lookup("#btnGuardar");
            Button btnCancelar = (Button) formRoot.lookup("#btnCancelar");

            // Si estamos editando, rellenar los campos
            boolean esNuevo = (almacen == null || almacen.getId() == null);
            Almacen almacenEditar = esNuevo ? new Almacen() : almacen;

            if (!esNuevo) {
                txtCodigo.setText(almacen.getCodigo());
                txtNombre.setText(almacen.getNombre());
            } else {
                // Generar código automáticamente para nuevo almacén
                txtCodigo.setText(generarNuevoCodigo());
            }

            // Crear Stage modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(esNuevo ? "Nuevo Almacén" : "Editar Almacén");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(formRoot));

            // Configurar botones
            btnGuardar.setOnAction(e -> {
                if (validarFormulario(txtCodigo, txtNombre)) {
                    almacenEditar.setCodigo(txtCodigo.getText().trim());
                    almacenEditar.setNombre(txtNombre.getText().trim());

                    try {
                        almacenService.save(almacenEditar);
                        loadAll();
                        mostrarInfo(esNuevo ? "Almacén creado correctamente" : "Almacén actualizado correctamente");
                        stage.close();
                    } catch (Exception ex) {
                        log.error("Error guardando almacén", ex);
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

    @FXML
    public void onDelete() {
        if (tableAlmacenes == null) { mostrarError("Tabla no disponible"); return; }
        Almacen sel = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarInfo("Selecciona un almacén"); return; }
        if (sel.getId() == null) { mostrarError("El almacén seleccionado no tiene id"); return; }

        // Verificar estado actual
        boolean estaActivo = sel.getActivo() == null || sel.getActivo();
        String accion = estaActivo ? "dar de baja" : "activar";
        String mensaje = estaActivo ?
            "¿Estás seguro de dar de baja el almacén '" + sel.getNombre() + "'?" :
            "¿Estás seguro de activar el almacén '" + sel.getNombre() + "'?";

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar " + accion);
        confirmacion.setHeaderText(mensaje);
        confirmacion.setContentText("Esta operación cambiará el estado del almacén.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Cambiar estado
                    sel.setActivo(!estaActivo);
                    almacenService.save(sel);
                    loadAll();
                    mostrarInfo("Almacén " + (estaActivo ? "dado de baja" : "activado") + " correctamente");
                } catch (Exception e) {
                    log.error("Error cambiando estado del almacén", e);
                    mostrarError("Error cambiando estado: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private boolean validarFormulario(TextField txtCodigo, TextField txtNombre) {
        String codigo = txtCodigo.getText();
        String nombre = txtNombre.getText();
        if (codigo == null || codigo.trim().isEmpty()) {
            mostrarError("El código es obligatorio");
            return false;
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        return true;
    }

    private String generarNuevoCodigo() {
        try {
            List<Almacen> todos = almacenService.findAll();
            if (todos.isEmpty()) {
                return "ALM001";
            }

            // Buscar el código más alto
            int maxNumero = 0;
            for (Almacen a : todos) {
                String codigo = a.getCodigo();
                if (codigo != null && codigo.startsWith("ALM")) {
                    try {
                        String numeroStr = codigo.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                        }
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        // Ignorar códigos que no sigan el patrón ALMxxx
                    }
                }
            }

            // Incrementar y formatear
            return String.format("ALM%03d", maxNumero + 1);
        } catch (Exception e) {
            log.error("Error generando código automático", e);
            return "ALM001";
        }
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
