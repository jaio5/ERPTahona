package alicanteweb.erp.controller.formcontroller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.DialogUtils;

/**
 * Controlador base para todos los formularios de la aplicación
 */
@Controller
public abstract class BaseFormController<T> {
    
    protected T item;
    @Setter
    protected Runnable callback;
    /** Stage asociado al formulario (set por el invocador) */
    @Setter
    protected Stage stage;

    /**
     * Establecer el item a editar (null = crear nuevo)
     */
    public void setItem(T item) {
        this.item = item;
        if (item != null) {
            cargarDatos();
        }
    }
    
    /**
     * Cargar datos en campos si es edición
     */
    protected abstract void cargarDatos();
    
    /**
     * Validar datos del formulario
     */
    protected abstract boolean validar();
    
    /**
     * Guardar item en BD
     */
    protected abstract void guardarItem();
    
    /**
     * Manejar clic en botón OK
     */
    @FXML
    protected final void handleOk() {
        if (!validar()) return;
        
        try {
            guardarItem();
            
            if (callback != null) {
                callback.run();
            }
            
            cerrar();
        } catch (Exception e) {
            mostrarError("Error guardando: " + e.getMessage());
        }
    }
    
    /**
     * Manejar clic en botón Cancelar
     */
    @FXML
    protected final void handleCancel() {
        cerrar();
    }
    
    /**
     * Cerrar ventana del formulario
     */
    protected void cerrar() {
        // Preferir cerrar mediante el Stage asociado si está disponible
        try {
            if (stage != null) {
                stage.close();
                return;
            }

            // Intentar cerrar la ventana activa (la que tiene el foco)
            java.util.Optional<javafx.stage.Window> maybeWindow = javafx.stage.Window.getWindows().stream()
                    .filter(javafx.stage.Window::isFocused)
                    .findFirst();
            if (maybeWindow.isPresent() && maybeWindow.get() instanceof Stage focusedStage) {
                focusedStage.close();
                return;
            }

            // Fallback: cerrar la primera ventana visible
            maybeWindow = javafx.stage.Window.getWindows().stream().filter(javafx.stage.Window::isShowing).findFirst();
            if (maybeWindow.isPresent() && maybeWindow.get() instanceof Stage) {
                ((Stage) maybeWindow.get()).close();
            }
        } catch (Exception e) {
            // Si todo falla, no bloquear la UI
        }
    }
    
    // === MÉTODOS DE NOTIFICACIÓN ===
    
    protected void mostrarError(String mensaje) {
        DialogUtils.showError(mensaje);
    }
    
    protected void mostrarExito(String mensaje) {
        DialogUtils.showSuccess(mensaje);
    }
}
