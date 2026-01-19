package alicanteweb.erp.controller;

import alicanteweb.erp.ErpLauncher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import alicanteweb.erp.ui.Dialogs;

/**
 * Controlador base para todos los módulos de la aplicación
 * Proporciona métodos reutilizables para CRUD
 */
@Controller
public abstract class BaseController<T> {
    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

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
            log.info("📋 Abriendo formulario: {}", getRutaFormulario());

            // Obtener el contexto de Spring
            ApplicationContext springContext = ErpLauncher.getSpringContext();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(getRutaFormulario()));
            loader.setControllerFactory(springContext::getBean);

            Parent parent = loader.load();
            Object controller = loader.getController();

            log.info("✅ Formulario cargado. Controlador: {}", controller.getClass().getSimpleName());

            // Intentar configurar el item usando reflexión
            if (item != null) {
                try {
                    // Buscar método setCliente, setArticulo, setItem, etc.
                    Method setMethod = findSetMethod(controller, item);
                    if (setMethod != null) {
                        setMethod.invoke(controller, item);
                        log.info("✅ Item configurado en el formulario");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ No se pudo configurar el item: {}", e.getMessage());
                }
            } else {
                // Modo crear - intentar llamar setCliente(null) o similar
                try {
                    Method setMethod = findSetMethod(controller, null);
                    if (setMethod != null) {
                        setMethod.invoke(controller, (Object) null);
                        log.info("✅ Modo crear configurado");
                    }
                } catch (Exception e) {
                    log.debug("Método set no encontrado o no necesario");
                }
            }

            // Crear y mostrar stage
            Stage stage = new Stage();
            stage.setTitle(item == null ? "Nuevo " + getNombreModulo() : "Editar " + getNombreModulo());
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos después de cerrar el formulario
            cargarDatos();
            log.info("✅ Formulario cerrado, datos recargados");

        } catch (IOException e) {
            log.error("❌ Error abriendo formulario", e);
            mostrarError("Error abriendo formulario: " + e.getMessage());
        }
    }

    /**
     * Buscar método set apropiado usando reflexión
     */
    private Method findSetMethod(Object controller, T item) {
        Class<?> controllerClass = controller.getClass();

        // Intentar nombres comunes de métodos
        String[] methodNames = {
            "setItem",
            "set" + getNombreModulo(),
            "setCliente",
            "setArticulo",
            "setProveedor",
            "setFactura",
            "setAlbaran"
        };

        for (String methodName : methodNames) {
            try {
                // Buscar método con un parámetro
                Method[] methods = controllerClass.getMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                        return method;
                    }
                }
            } catch (Exception e) {
                // Continuar buscando
            }
        }

        return null;
    }

    // === MÉTODOS DE NOTIFICACIÓN ===

    protected void mostrarError(String mensaje) { Dialogs.showError(mensaje); }
    protected void mostrarAdvertencia(String mensaje) { Dialogs.showWarn(mensaje); }
    protected void mostrarExito(String mensaje) { Dialogs.showInfo(mensaje); }
    protected void mostrarInfo(String mensaje) { Dialogs.showInfo(mensaje); }
    protected boolean mostrarConfirmacion(String mensaje) { return Dialogs.showConfirm(mensaje); }
}
