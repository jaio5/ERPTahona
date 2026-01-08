package alicanteweb.erp.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador base para todos los módulos de la aplicación
 * Proporciona métodos reutilizables para CRUD
 */
@Controller
public abstract class BaseController<T> {

    @FXML
    protected TextField txtBuscar;

    @FXML
    protected TableView<T> table;

    @FXML
    protected Label lblEstado;

    protected List<T> datosCompletos;

    /**
     * Inicializar controlador
     * Llamar desde initialize() de cada controlador
     */
    protected void initController() {
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrar(newVal));
        }
        cargarDatos();
    }

    /**
     * Cargar datos desde BD
     * Implementar en cada controlador
     */
    protected abstract void cargarDatos();

    /**
     * Obtener nombre del módulo para títulos
     */
    protected abstract String getNombreModulo();

    /**
     * Obtener ruta del formulario FXML
     */
    protected abstract String getRutaFormulario();

    /**
     * Filtrar datos por término de búsqueda
     */
    protected abstract boolean coincideConBusqueda(T item, String termino);

    /**
     * Actualizar tabla con datos
     */
    protected void actualizarTabla(List<T> datos) {
        this.datosCompletos = datos;
        table.setItems(FXCollections.observableArrayList(datos));
        if (lblEstado != null) {
            lblEstado.setText(String.format("Total: %d registros", datos.size()));
        }
    }

    /**
     * Filtrar datos en tiempo real
     */
    protected void filtrar(String termino) {
        if (termino == null || termino.isEmpty()) {
            table.setItems(FXCollections.observableArrayList(datosCompletos));
            return;
        }

        List<T> filtrados = datosCompletos.stream()
            .filter(item -> coincideConBusqueda(item, termino.toLowerCase()))
            .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtrados));
    }

    /**
     * Abrir formulario de creación/edición
     */
    @FXML
    protected void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    protected void onEditar() {
        T seleccionado = table.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un elemento para editar");
            return;
        }
        abrirFormulario(seleccionado);
    }

    @FXML
    protected void onEliminar() {
        T seleccionado = table.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un elemento para eliminar");
            return;
        }

        if (mostrarConfirmacion("¿Estás seguro de que deseas eliminar este elemento?")) {
            try {
                eliminarItem(seleccionado);
                cargarDatos();
                mostrarExito("Elemento eliminado correctamente");
            } catch (Exception e) {
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onRefresh() {
        cargarDatos();
        mostrarInfo("Datos actualizados");
    }

    @FXML
    protected void onBuscar() {
        // El filtrado se hace automáticamente con el listener del txtBuscar
        // Este método está aquí para el botón de buscar en el FXML
        String termino = txtBuscar != null ? txtBuscar.getText() : "";
        filtrar(termino);
    }

    @FXML
    protected void onVer() {
        T seleccionado = table.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un elemento para ver");
            return;
        }
        // Por defecto, abrir el formulario en modo lectura
        abrirFormulario(seleccionado);
    }

    @FXML
    protected void onDarBaja() {
        // Alias para onEliminar
        onEliminar();
    }

    /**
     * Eliminar item de BD
     * Implementar en cada controlador
     */
    protected abstract void eliminarItem(T item);

    /**
     * Abrir formulario modal
     */
    protected void abrirFormulario(T item) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(getRutaFormulario())
            );
            Parent parent = loader.load();

            // Obtener controlador del formulario
            Object controllerObj = loader.getController();

            // Si es un BaseFormController, configurarlo
            if (controllerObj instanceof BaseFormController) {
                @SuppressWarnings("unchecked")
                BaseFormController<T> controller = (BaseFormController<T>) controllerObj;
                if (item != null) {
                    controller.setItem(item);
                }
                controller.setCallback(this::cargarDatos);
            }

            Stage stage = new Stage();
            stage.setTitle(item == null ? "Nuevo " + getNombreModulo() : "Editar " + getNombreModulo());
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            mostrarError("Error abriendo formulario: " + e.getMessage());
        }
    }

    // === MÉTODOS DE NOTIFICACIÓN ===

    protected void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    protected void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    protected void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    protected void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    protected boolean mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setContentText(mensaje);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

