package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.service.AlbaranVentaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controlador para la gestión de Albaranes de Venta
 */
@Controller
public class AlbaranController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranController.class);

    @FXML private TableView<AlbaranVenta> tableAlbaranes;
    @FXML private TableColumn<AlbaranVenta, String> colNumero;
    @FXML private TableColumn<AlbaranVenta, LocalDate> colFecha;
    @FXML private TableColumn<AlbaranVenta, String> colCliente;
    @FXML private TableColumn<AlbaranVenta, BigDecimal> colTotal;
    @FXML private TableColumn<AlbaranVenta, String> colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    private final AlbaranVentaService albaranVentaService;

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

        // Aplicar estilo a la tabla
        if (tableAlbaranes != null) {
            tableAlbaranes.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }
    }

    private void configurarColumnas() {
        if (colNumero != null) {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        }
        if (colCliente != null) {
            colCliente.setCellValueFactory(cellData -> {
                AlbaranVenta albaran = cellData.getValue();
                String nombreCliente = "";
                if (albaran != null && albaran.getCliente() != null) {
                    nombreCliente = albaran.getCliente().getNombre();
                }
                return new SimpleStringProperty(nombreCliente);
            });
        }
        if (colTotal != null) {
            colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        }
        if (colEstado != null) {
            colEstado.setCellValueFactory(cellData -> new SimpleStringProperty("Pendiente"));
        }
    }

    private void cargarDatos() {
        try {
            var albaranes = albaranVentaService.findAll();
            if (tableAlbaranes != null) {
                tableAlbaranes.setItems(FXCollections.observableArrayList(albaranes));
            }
            if (lblTotal != null) {
                lblTotal.setText(albaranes.size() + " albaranes");
            }
            log.info("Albaranes cargados: {}", albaranes.size());
        } catch (Exception e) {
            log.error("Error cargando albaranes", e);
            mostrarError("Error al cargar albaranes: " + e.getMessage());
        }
    }

    private void filtrarAlbaranes(String busqueda) {
        try {
            var albaranes = albaranVentaService.findAll();

            if (busqueda != null && !busqueda.isEmpty()) {
                String search = busqueda.toLowerCase();
                albaranes = albaranes.stream()
                    .filter(a -> (a.getNumero() != null && a.getNumero().toLowerCase().contains(search)) ||
                                (a.getCliente() != null && a.getCliente().getNombre() != null &&
                                 a.getCliente().getNombre().toLowerCase().contains(search)))
                    .toList();
            }

            if (tableAlbaranes != null) {
                tableAlbaranes.setItems(FXCollections.observableArrayList(albaranes));
            }
            if (lblTotal != null) {
                lblTotal.setText(albaranes.size() + " albaranes");
            }
        } catch (Exception e) {
            log.error("Error filtrando albaranes", e);
        }
    }

    @FXML
    public void onBuscar() {
        String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
        filtrarAlbaranes(busqueda);
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nuevo albarán");
        mostrarAlerta("Función en desarrollo: Crear nuevo albarán");
    }

    @FXML
    public void onVer() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán primero");
            return;
        }
        log.info("Ver albarán: {}", albaran.getNumero());
        mostrarAlerta("Función en desarrollo: Ver albarán");
    }

    @FXML
    public void onEditar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para editar");
            return;
        }
        log.info("Editar albarán: {}", albaran.getNumero());
        mostrarAlerta("Función en desarrollo: Editar albarán");
    }

    @FXML
    public void onImprimir() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para imprimir");
            return;
        }
        log.info("Imprimir albarán: {}", albaran.getNumero());
        mostrarAlerta("Función en desarrollo: Imprimir albarán");
    }

    @FXML
    public void onFacturar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para facturar");
            return;
        }
        log.info("Facturar albarán: {}", albaran.getNumero());
        mostrarAlerta("Función en desarrollo: Convertir albarán en factura");
    }

    @FXML
    public void onEliminar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para eliminar");
            return;
        }
        log.info("Eliminar albarán: {}", albaran.getNumero());
        mostrarAlerta("Eliminación de albaranes no permitida en este momento");
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando lista de albaranes");
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

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
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

