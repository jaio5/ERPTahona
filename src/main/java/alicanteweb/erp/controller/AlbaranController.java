package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.service.AlbaranVentaService;
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
 * Controlador para la gestión de Albaranes de Venta
 */
@Controller
public class AlbaranController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranController.class);

    @FXML private TableView<AlbaranVenta> tableAlbaranes;
    @FXML private TableColumn<AlbaranVenta, Long> colId;
    @FXML private TableColumn<AlbaranVenta, String> colNumero;
    @FXML private TableColumn<AlbaranVenta, String> colFecha;
    @FXML private TableColumn<AlbaranVenta, String> colCliente;
    @FXML private TextField txtBuscar;

    private final AlbaranVentaService albaranVentaService;
    private final ObservableList<AlbaranVenta> albaranesList = FXCollections.observableArrayList();

    public AlbaranController(AlbaranVentaService albaranVentaService) {
        this.albaranVentaService = albaranVentaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AlbaranController");
        configurarColumnas();
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlbaranes(newV));
        }
    }

    private void configurarColumnas() {
        if (colId != null) {
            colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        }
        if (colNumero != null) {
            colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).orElse("")));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFecha()).map(Object::toString).orElse("")));
        }
        if (colCliente != null) {
            colCliente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : ""));
        }
        if (tableAlbaranes != null) {
            tableAlbaranes.setItems(albaranesList);
        }
    }

    private void cargarDatos() {
        try {
            albaranesList.clear();
            log.info("Albaranes cargados: {}", albaranesList.size());
            javafx.application.Platform.runLater(() -> {
                if (tableAlbaranes != null) tableAlbaranes.refresh();
            });
        } catch (Exception e) {
            log.error("Error cargando albaranes", e);
            mostrarError("Error al cargar albaranes: " + e.getMessage());
        }
    }

    private void filtrarAlbaranes(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            cargarDatos();
            return;
        }
        String search = busqueda.toLowerCase();
        ObservableList<AlbaranVenta> filtered = FXCollections.observableArrayList(
            albaranesList.stream()
                .filter(a -> (a.getNumero() != null && a.getNumero().toLowerCase().contains(search)))
                .toList()
        );
        tableAlbaranes.setItems(filtered);
    }

    @FXML
    public void onNuevo() {
        mostrarAlerta("Crear nuevo albaran en desarrollo");
    }

    @FXML
    public void onEditar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Seleccione un albaran para editar");
            return;
        }
        mostrarAlerta("Edición de albaran en desarrollo");
    }

    @FXML
    public void onEliminar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Seleccione un albaran para eliminar");
            return;
        }
        mostrarAlerta("Eliminación de albaranes no permitida en este momento");
    }

    @FXML
    public void onRefresh() {
        cargarDatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

