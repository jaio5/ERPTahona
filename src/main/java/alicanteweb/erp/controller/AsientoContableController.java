package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.service.AsientoContableService;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controlador para la gestión de Asientos Contables
 */
@Controller
public class AsientoContableController {
    private static final Logger log = LoggerFactory.getLogger(AsientoContableController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<AsientoContable> tableAsientos;
    @FXML private TableColumn<AsientoContable, String> colNumero; // numero es String en la entidad
    @FXML private TableColumn<AsientoContable, LocalDate> colFecha;
    @FXML private TableColumn<AsientoContable, String> colConcepto;
    @FXML private TableColumn<AsientoContable, String> colDebe;
    @FXML private TableColumn<AsientoContable, String> colHaber;
    @FXML private TableColumn<AsientoContable, String> colDescuadre; // añadido

    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    private final AsientoContableService asientoContableService;

    public AsientoContableController(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AsientoContableController");
        configurarColumnas();
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAsientos(newV));
        }

        // Aplicar estilo a la tabla
        if (tableAsientos != null) {
            tableAsientos.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }
    }

    private void configurarColumnas() {
        if (colNumero != null) {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            // Formatear fecha
            colFecha.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DATE_FORMATTER));
                    }
                }
            });
        }
        if (colConcepto != null) {
            colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        }
        if (colDebe != null) {
            colDebe.setCellValueFactory(cellData -> {
                AsientoContable asiento = cellData.getValue();
                BigDecimal totalDebe = asientoContableService.calcularTotalDebe(asiento);
                return new SimpleStringProperty(String.format("%.2f €", totalDebe));
            });
        }
        if (colHaber != null) {
            colHaber.setCellValueFactory(cellData -> {
                AsientoContable asiento = cellData.getValue();
                BigDecimal totalHaber = asientoContableService.calcularTotalHaber(asiento);
                return new SimpleStringProperty(String.format("%.2f €", totalHaber));
            });
        }
        if (colDescuadre != null) {
            colDescuadre.setCellValueFactory(cellData -> {
                AsientoContable asiento = cellData.getValue();
                BigDecimal desc = asiento.getDescuadre();
                if (desc == null) {
                    asiento.calcularDescuadre();
                    desc = asiento.getDescuadre();
                }
                return new SimpleStringProperty(String.format("%.2f €", desc != null ? desc : BigDecimal.ZERO));
            });
        }
    }

    private void cargarDatos() {
        try {
            var asientos = asientoContableService.findAll();
            if (tableAsientos != null) {
                tableAsientos.setItems(FXCollections.observableArrayList(asientos));
            }
            if (lblTotal != null) {
                lblTotal.setText(asientos.size() + " asientos");
            }
            log.info("Asientos contables cargados: {}", asientos.size());
        } catch (Exception e) {
            log.error("Error cargando asientos contables", e);
            mostrarError("Error al cargar asientos: " + e.getMessage());
        }
    }

    private void filtrarAsientos(String busqueda) {
        try {
            var asientos = asientoContableService.findAll();

            if (busqueda != null && !busqueda.isEmpty()) {
                String search = busqueda.toLowerCase();
                asientos = asientos.stream()
                    .filter(a -> (a.getConcepto() != null && a.getConcepto().toLowerCase().contains(search)) ||
                                (a.getNumero() != null && a.getNumero().contains(search)))
                    .toList();
            }

            if (tableAsientos != null) {
                tableAsientos.setItems(FXCollections.observableArrayList(asientos));
            }
            if (lblTotal != null) {
                lblTotal.setText(asientos.size() + " asientos");
            }
        } catch (Exception e) {
            log.error("Error filtrando asientos", e);
        }
    }

    @FXML
    public void onBuscar() {
        try {
            var asientos = asientoContableService.findAll();

            // Filtrar por fechas si están seleccionadas
            if (dpFechaDesde != null && dpFechaDesde.getValue() != null &&
                dpFechaHasta != null && dpFechaHasta.getValue() != null) {
                asientos = asientoContableService.findByFechaBetween(
                    dpFechaDesde.getValue(),
                    dpFechaHasta.getValue()
                );
            }

            // Aplicar filtro de texto
            String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
            if (!busqueda.isEmpty()) {
                String search = busqueda.toLowerCase();
                asientos = asientos.stream()
                    .filter(a -> (a.getConcepto() != null && a.getConcepto().toLowerCase().contains(search)) ||
                                (a.getNumero() != null && a.getNumero().contains(search)))
                    .toList();
            }

            if (tableAsientos != null) {
                tableAsientos.setItems(FXCollections.observableArrayList(asientos));
            }
            if (lblTotal != null) {
                lblTotal.setText(asientos.size() + " asientos");
            }
        } catch (Exception e) {
            log.error("Error buscando asientos", e);
            mostrarError("Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nuevo asiento contable");
        mostrarAlerta("Función en desarrollo: Crear nuevo asiento contable\n\n" +
                     "El siguiente número de asiento sería: " +
                     asientoContableService.obtenerSiguienteNumero());
    }

    @FXML
    public void onVer() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            mostrarAlerta("Selecciona un asiento primero");
            return;
        }
        log.info("Ver asiento: {}", asiento.getNumero());

        // Calcular totales
        BigDecimal totalDebe = asientoContableService.calcularTotalDebe(asiento);
        BigDecimal totalHaber = asientoContableService.calcularTotalHaber(asiento);
        boolean cuadrado = asientoContableService.validarAsiento(asiento);

        String info = String.format(
            "Asiento Nº: %s\n" +
            "Fecha: %s\n" +
            "Concepto: %s\n\n" +
            "Total Debe: %.2f €\n" +
            "Total Haber: %.2f €\n" +
            "Descuadre: %.2f €\n\n" +
            "Estado: %s",
            asiento.getNumero(),
            asiento.getFecha().format(DATE_FORMATTER),
            asiento.getConcepto(),
            totalDebe,
            totalHaber,
            asiento.getDescuadre(),
            cuadrado ? "✅ CUADRADO" : "❌ DESCUADRADO"
        );

        mostrarInfo(info);
    }

    @FXML
    public void onEditar() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            mostrarAlerta("Selecciona un asiento para editar");
            return;
        }
        log.info("Editar asiento: {}", asiento.getNumero());
        mostrarAlerta("Función en desarrollo: Editar asiento contable");
    }

    @FXML
    public void onEliminar() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            mostrarAlerta("Selecciona un asiento para eliminar");
            return;
        }

        if (mostrarConfirmacion("¿Deseas eliminar este asiento?\n\n" +
                                "Número: " + asiento.getNumero() + "\n" +
                                "Concepto: " + asiento.getConcepto())) {
            try {
                asientoContableService.deleteById(asiento.getId());
                cargarDatos();
                mostrarExito("Asiento eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando asiento", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando asientos contables");
        cargarDatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información del Asiento");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
