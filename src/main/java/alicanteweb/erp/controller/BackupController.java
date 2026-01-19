package alicanteweb.erp.controller;

import alicanteweb.erp.service.BackupService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class BackupController {
    private static final Logger log = LoggerFactory.getLogger(BackupController.class);

    @FXML private TableView<BackupService.BackupInfo> tableBackups;
    @FXML private TableColumn<BackupService.BackupInfo, String> colNombre;
    @FXML private TableColumn<BackupService.BackupInfo, Long> colTamano;
    @FXML private TableColumn<BackupService.BackupInfo, String> colFecha;
    @FXML private TableColumn<BackupService.BackupInfo, Void> colAcciones;

    @FXML private Label lblInfo;

    private final BackupService backupService;
    private final ObservableList<BackupService.BackupInfo> backups = FXCollections.observableArrayList();

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando BackupController");
        configurarColumnas();
        cargarBackups();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().nombre()));
        colTamano.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTamanoMB()));
        colFecha.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFechaFormateada()));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("🗑️");
            private final Button btnRestore = new Button("🔄");

            {
                btnDelete.setOnAction(e -> {
                    BackupService.BackupInfo info = getTableRow() != null ? getTableRow().getItem() : null;
                    if (info != null) {
                        confirmAndDelete(info);
                    }
                });

                btnRestore.setOnAction(e -> {
                    BackupService.BackupInfo info = getTableRow() != null ? getTableRow().getItem() : null;
                    if (info != null) {
                        confirmAndRestore(info);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, btnRestore, btnDelete);
                    setGraphic(box);
                }
            }
        });

        tableBackups.setItems(backups);
    }

    private void cargarBackups() {
        try {
            List<BackupService.BackupInfo> list = backupService.listarBackups();
            backups.clear();
            backups.addAll(list);
            lblInfo.setText(backups.size() + " backups encontrados");
        } catch (IOException e) {
            log.error("Error cargando backups", e);
            mostrarError("Error cargando backups: " + e.getMessage());
        }
    }

    // ----------------------------------
    // Operaciones en background
    // ----------------------------------
    private void runInBackground(Task<?> task, String inicioMsg, String finMsg) {
        // Deshabilitar UI mínima
        setUiDisabled(true);
        lblInfo.setText(inicioMsg);

        task.setOnSucceeded(evt -> {
            setUiDisabled(false);
            lblInfo.setText(finMsg);
            // si la tarea devolvió algo y queremos refrescar
            cargarBackups();
        });

        task.setOnFailed(evt -> {
            setUiDisabled(false);
            Throwable ex = task.getException();
            log.error("Tarea background falló", ex);
            mostrarError("Error: " + (ex != null ? ex.getMessage() : "desconocido"));
            cargarBackups();
        });

        Thread th = new Thread(task, "backup-task");
        th.setDaemon(true);
        th.start();
    }

    private void setUiDisabled(boolean disabled) {
        Platform.runLater(() -> {
            if (tableBackups != null) tableBackups.setDisable(disabled);
        });
    }

    // ----------------------------------
    // Acciones públicas (vinculadas al FXML)
    // ----------------------------------
    @FXML
    public void onCrearBackup() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return backupService.realizarBackup();
            }
        };

        task.setOnSucceeded(e -> {
            String ruta = task.getValue();
            Platform.runLater(() -> mostrarAlerta("Backup creado: " + ruta));
        });

        runInBackground(task, "Creando backup...", "Backup creado");
    }

    @FXML
    public void onRestaurarBackup() {
        BackupService.BackupInfo selected = tableBackups.getSelectionModel().getSelectedItem();
        if (selected == null) { mostrarAlerta("Seleccione un backup para restaurar"); return; }
        confirmAndRestore(selected);
    }

    @FXML
    public void onEliminarBackup() {
        BackupService.BackupInfo selected = tableBackups.getSelectionModel().getSelectedItem();
        if (selected == null) { mostrarAlerta("Seleccione un backup para eliminar"); return; }
        confirmAndDelete(selected);
    }

    @FXML
    public void onRefresh() { cargarBackups(); }

    @FXML
    public void onVerDetalle() {
        BackupService.BackupInfo selected = tableBackups.getSelectionModel().getSelectedItem();
        if (selected == null) { mostrarAlerta("Seleccione un backup para ver detalle"); return; }
        String content = "Nombre: " + selected.nombre() + "\n"
            + "Ruta: " + selected.rutaCompleta() + "\n"
            + "Tamaño: " + selected.getTamanoMB() + " MB\n"
            + "Fecha: " + selected.getFechaFormateada() + "\n";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle Backup"); alert.setHeaderText("Información del backup"); alert.setContentText(content);
        alert.getDialogPane().setMinWidth(480); alert.showAndWait();
    }

    // ----------------------------------
    // Confirmaciones y helpers
    // ----------------------------------
    private void confirmAndDelete(BackupService.BackupInfo info) {
        Platform.runLater(() -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar backup");
            confirm.setHeaderText("¿Desea eliminar este backup?");
            confirm.setContentText(info.nombre());

            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return backupService.deleteBackup(info.rutaCompleta());
                    }
                };

                task.setOnSucceeded(e -> {
                    boolean ok = task.getValue();
                    if (ok) Platform.runLater(() -> mostrarAlerta("Backup eliminado"));
                });

                runInBackground(task, "Eliminando backup...", "Backup eliminado");
            }
        });
    }

    private void confirmAndRestore(BackupService.BackupInfo info) {
        Platform.runLater(() -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Restaurar backup");
            confirm.setHeaderText("¿Desea restaurar la base de datos desde este backup?\nEsto sobrescribirá los datos actuales");
            confirm.setContentText(info.nombre());

            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        backupService.restaurarBackup(info.rutaCompleta());
                        return null;
                    }
                };

                task.setOnSucceeded(e -> Platform.runLater(() -> mostrarAlerta("Restauración completada (ver logs para detalles)")));

                runInBackground(task, "Restaurando backup...", "Restauración finalizada");
            }
        });
    }

    // ----------------------------------
    // UI helpers
    // ----------------------------------
    private void mostrarAlerta(String msg) { Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle("Atención"); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait(); }
    private void mostrarError(String msg) { Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle("Error"); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait(); }
}
