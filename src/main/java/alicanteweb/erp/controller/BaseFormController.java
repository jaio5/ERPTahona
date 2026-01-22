package alicanteweb.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

/**
 * Controlador base para todos los formularios de la aplicación
 */
@Controller
public abstract class BaseFormController<T> {
    
    protected T item;
    protected Runnable callback;
    
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
     * Establecer callback para refrescar lista cuando se guarda
     */
    public void setCallback(Runnable callback) {
        this.callback = callback;
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
        // Intentar cerrar la ventana activa (la que tiene el foco)
        try {
            java.util.Optional<javafx.stage.Window> maybeWindow = javafx.stage.Window.getWindows().stream()
                    .filter(javafx.stage.Window::isFocused)
                    .findFirst();
            if (maybeWindow.isPresent() && maybeWindow.get() instanceof Stage) {
                Stage stage = (Stage) maybeWindow.get();
                stage.close();
                return;
            }

            // Fallback: cerrar la primera ventana visible
            maybeWindow = javafx.stage.Window.getWindows().stream().filter(javafx.stage.Window::isShowing).findFirst();
            if (maybeWindow.isPresent() && maybeWindow.get() instanceof Stage) {
                ((Stage) maybeWindow.get()).close();
                return;
            }
        } catch (Exception e) {
            // Si todo falla, lanzar excepción silenciosa para no bloquear la UI
            // (las formas normales de cierre deberían funcionar en la mayoría de casos)
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
        alert.setTitle("Validación");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    protected void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
