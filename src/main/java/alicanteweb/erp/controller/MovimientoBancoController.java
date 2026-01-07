package alicanteweb.erp.controller;

import alicanteweb.erp.entities.MovimientoBanco;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controlador para la gestión de Movimientos Bancarios
 * Tesorería - Movimientos de bancos
 */
@Controller
public class MovimientoBancoController {
    private static final Logger log = LoggerFactory.getLogger(MovimientoBancoController.class);

    @FXML private TableView<MovimientoBanco> tableMovimientos;
    @FXML private TableColumn<MovimientoBanco, Long> colId;
    @FXML private TableColumn<MovimientoBanco, String> colFecha;
    @FXML private TableColumn<MovimientoBanco, String> colBanco;
    @FXML private TableColumn<MovimientoBanco, String> colTipo;
    @FXML private TableColumn<MovimientoBanco, String> colImporte;
    @FXML private TableColumn<MovimientoBanco, String> colConcepto;
    @FXML private TextField txtBuscar;

    private final ObservableList<MovimientoBanco> movimientosList = FXCollections.observableArrayList();

    public MovimientoBancoController() {
    }

    @FXML
    public void initialize() {
        log.info("=== INICIALIZANDO MovimientoBancoController ===");

        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFecha()).map(Object::toString).orElse("")));
        if (colBanco != null) colBanco.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getBanco()).map(b -> b.getNombre()).orElse("")));
        if (colTipo != null) colTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getTipo()).orElse("")));
        if (colImporte != null) colImporte.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getImporte()).map(Object::toString).orElse("")));
        if (colConcepto != null) colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getConcepto()).orElse("")));

        if (tableMovimientos != null) {
            tableMovimientos.setItems(movimientosList);
        }

        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarMovimientos(newV));
        }

        log.info("=== FINALIZÓ INICIALIZACIÓN MovimientoBancoController ===");
    }

    private void loadAll() {
        try {
            log.info("Cargando movimientos bancarios desde la base de datos...");
            movimientosList.clear();
            log.info("Movimientos cargados: {}", movimientosList.size());

            javafx.application.Platform.runLater(() -> {
                if (tableMovimientos != null) {
                    tableMovimientos.refresh();
                }
            });
        } catch (Exception e) {
            log.error("Error cargando movimientos", e);
            mostrarError("Error cargando movimientos: " + e.getMessage());
        }
    }

    private void filtrarMovimientos(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            loadAll();
            return;
        }

        String search = busqueda.toLowerCase();
        movimientosList.stream()
                .filter(m -> (m.getBanco() != null && m.getBanco().getNombre().toLowerCase().contains(search)) ||
                        (m.getConcepto() != null && m.getConcepto().toLowerCase().contains(search)))
                .forEach(System.out::println);
    }

    @FXML
    public void onCreate() {
        log.info("Abriendo formulario para crear nuevo movimiento");
        mostrarInfo("Funcionalidad no implementada aún");
    }

    @FXML
    public void onEdit() {
        MovimientoBanco selected = tableMovimientos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Seleccione un movimiento");
            return;
        }

        log.info("Editando movimiento: {}", selected.getId());
        mostrarInfo("Funcionalidad no implementada aún");
    }

    @FXML
    public void onDelete() {
        MovimientoBanco selected = tableMovimientos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Seleccione un movimiento");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar movimiento?");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            log.info("Eliminando movimiento: {}", selected.getId());
            mostrarInfo("Funcionalidad no implementada aún");
        }
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error en la operación");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

