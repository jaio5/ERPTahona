package alicanteweb.erp.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utilidades comunes para mostrar diálogos en la UI.
 * Minimiza duplicación de métodos mostrarInfo/mostrarError/mostrarAdvertencia/mostrarExito
 */
public final class DialogUtils {
    private DialogUtils() {}

    public static void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showWarning(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atención");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Muestra un diálogo de confirmación y devuelve true si el usuario confirma (OK).
     * Nota: se ejecuta en el hilo de aplicación JavaFX mediante Platform.runLater, por lo que
     * devolverá false si se llama desde un hilo distinto y la ejecución es asíncrona.
     * Para usos que requieren bloqueo sin problemas de hilo, considera llamar directamente
     * a Alert.showAndWait() en el hilo de UI.
     */
    public static boolean showConfirm(String message) {
        // Ejecutar directamente en el hilo actual - esperamos que se llame desde JavaFX
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
