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
import alicanteweb.erp.ui.DialogUtils;
import alicanteweb.erp.service.AutenticacionService;

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
    public void onNuevo() {
        if (!verificarPermisoAccion("crear")) {
            return;
        }
        abrirFormulario(null);
    }

    @FXML
    public void onEditar() {
        if (!verificarPermisoAccion("editar")) {
            return;
        }
        T seleccionado = table.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un elemento para editar");
            return;
        }
        abrirFormulario(seleccionado);
    }

    @FXML
    public void onEliminar() {
        if (!verificarPermisoAccion("eliminar")) {
            return;
        }
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
    public void onRefresh() {
        cargarDatos();
        mostrarInfo("Datos actualizados");
    }

    @FXML
    public void onBuscar() {
        // El filtrado se hace automáticamente con el listener del txtBuscar
        // Este método está aquí para el botón de buscar en el FXML
        String termino = txtBuscar != null ? txtBuscar.getText() : "";
        filtrar(termino);
    }

    @FXML
    public void onVer() {
        T seleccionado = table.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un elemento para ver");
            return;
        }
        // Por defecto, abrir el formulario en modo lectura
        abrirFormulario(seleccionado);
    }

    @FXML
    public void onDarBaja() {
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
                    Method setMethod = findSetMethod(controller);
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
                    Method setMethod = findSetMethod(controller);
                    if (setMethod != null) {
                        setMethod.invoke(controller, (Object) null);
                        log.info("✅ Modo crear configurado");
                    }
                } catch (Exception e) {
                    log.debug("Método set no encontrado o no necesario");
                }
            }

            // Si el parent no es un ScrollPane, envolverlo para permitir scroll automático
            Parent rootForScene = parent;
            if (!(parent instanceof javafx.scene.control.ScrollPane)) {
                javafx.scene.control.ScrollPane wrapper = new javafx.scene.control.ScrollPane();
                wrapper.setContent(parent);
                wrapper.setFitToWidth(true);
                wrapper.setFitToHeight(false);
                // Opcional: agregar estilos para mantener consistencia
                wrapper.setStyle("-fx-background-color: transparent;");
                rootForScene = wrapper;
            }

            // Crear y mostrar stage
            Stage stage = new Stage();
            stage.setTitle(item == null ? "Nuevo " + getNombreModulo() : "Editar " + getNombreModulo());
            stage.setScene(new Scene(rootForScene));
            // Hacer que el diálogo sea redimensionable y establecer tamaños mínimos razonables
            stage.setResizable(true);
            stage.setMinWidth(640);
            stage.setMinHeight(480);
            // Vincular el tamaño del contenido principal al tamaño del stage.
            // Preferimos enlazar el ScrollPane si existe; si no, enlazamos la Region principal.
            javafx.scene.layout.Region regionToBind = null;
            if (rootForScene instanceof javafx.scene.control.ScrollPane) {
                regionToBind = (javafx.scene.layout.Region) rootForScene;
            } else if (rootForScene instanceof javafx.scene.layout.Region) {
                regionToBind = (javafx.scene.layout.Region) rootForScene;
            }
            if (regionToBind != null) {
                regionToBind.prefWidthProperty().bind(stage.widthProperty());
                regionToBind.prefHeightProperty().bind(stage.heightProperty());
            }

            // Si el controller es un BaseFormController, pasarle el stage para permitir cerrar desde el controller
            if (controller instanceof alicanteweb.erp.controller.formcontroller.BaseFormController<?> baseFormController) {
                try {
                    baseFormController.setStage(stage);
                } catch (Exception ignored) {
                    // No bloquear si no acepta stage
                }
            }

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
    private Method findSetMethod(Object controller) {
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

    protected void mostrarError(String mensaje) { DialogUtils.showError(mensaje); }
    protected void mostrarAdvertencia(String mensaje) { DialogUtils.showWarning(mensaje); }
    protected void mostrarExito(String mensaje) { DialogUtils.showSuccess(mensaje); }
    protected void mostrarInfo(String mensaje) { DialogUtils.showInfo(mensaje); }
    protected boolean mostrarConfirmacion(String mensaje) { return DialogUtils.showConfirm(mensaje); }

    protected String getModuloPermisos() {
        return switch (getNombreModulo()) {
            case "Cliente" -> "clientes";
            case "Proveedor" -> "proveedores";
            case "ArtÃ­culo", "Articulo" -> "articulos";
            case "Factura" -> "ventas";
            default -> getNombreModulo().toLowerCase();
        };
    }

    protected boolean verificarPermisoAccion(String accion) {
        try {
            AutenticacionService auth = ErpLauncher.getSpringContext().getBean(AutenticacionService.class);
            if (auth.tienePermiso(getModuloPermisos(), accion)) {
                return true;
            }
            mostrarAdvertencia("No tiene permisos para " + accion + " en este modulo.");
            return false;
        } catch (Exception e) {
            log.warn("No se pudo verificar permiso {} en {}", accion, getModuloPermisos(), e);
            mostrarAdvertencia("No se pudo verificar la autorizacion de la accion.");
            return false;
        }
    }
}
