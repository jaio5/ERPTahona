package alicanteweb.erp.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Utilidad para mostrar diálogos en la UI
 */
public final class Dialogs {

    private Dialogs() {}

    private static void runBlocking(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    r.run();
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void showInfo(String title, String message) {
        runBlocking(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title == null ? "Información" : title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showInfo(String message) {
        showInfo("Información", message);
    }

    public static void showError(String title, String message) {
        runBlocking(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title == null ? "Error" : title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showError(String message) {
        showError("Error", message);
    }

    public static void showWarn(String message) {
        runBlocking(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atención");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static boolean showConfirm(String message) {
        final boolean[] resultHolder = new boolean[1];
        runBlocking(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText(null);
            alert.setContentText(message);
            Optional<ButtonType> result = alert.showAndWait();
            resultHolder[0] = result.isPresent() && result.get() == ButtonType.OK;
        });
        return resultHolder[0];
    }
}
