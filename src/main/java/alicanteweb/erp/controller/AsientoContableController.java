package alicanteweb.erp.controller;

import alicanteweb.erp.controller.formcontroller.AsientoFormController;
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
import alicanteweb.erp.ui.DialogUtils;

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
            DialogUtils.showError("Error al cargar asientos: " + e.getMessage());
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
            DialogUtils.showError("Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    public void onNuevo() {
        try {
            log.info("Abrir formulario nuevo asiento contable");
            // Crear asiento con número preasignado
            var siguiente = asientoContableService.obtenerSiguienteNumero();
            alicanteweb.erp.entities.AsientoContable nuevo = new alicanteweb.erp.entities.AsientoContable();
            nuevo.setNumero(String.valueOf(siguiente));

            // Cargar FXML del formulario usando Spring context
            var springContext = alicanteweb.erp.ErpLauncher.getSpringContext();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ui/asiento_form.fxml"));
            loader.setControllerFactory(springContext::getBean);
            javafx.scene.Parent parent = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AsientoFormController) {
                ((AsientoFormController) controller).setAsientoContable(nuevo);
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Nuevo Asiento Contable");
            stage.setScene(new javafx.scene.Scene(parent));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de asiento", e);
            DialogUtils.showError("Error abriendo formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onVer() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            DialogUtils.showWarning("Selecciona un asiento primero");
            return;
        }
        log.info("Ver asiento: {}", asiento.getNumero());

        // Calcular totales
        BigDecimal totalDebe = asientoContableService.calcularTotalDebe(asiento);
        BigDecimal totalHaber = asientoContableService.calcularTotalHaber(asiento);
        boolean cuadrado = asientoContableService.validarAsiento(asiento);

        String template = """
            Asiento Nº: %s
            Fecha: %s
            Concepto: %s
            Total Debe: %.2f €
            Total Haber: %.2f €
            Descuadre: %.2f €
            Estado: %s
            """;

        String info = String.format(template,
            asiento.getNumero(),
            asiento.getFecha().format(DATE_FORMATTER),
            asiento.getConcepto(),
            totalDebe,
            totalHaber,
            asiento.getDescuadre(),
            cuadrado ? "✅ CUADRADO" : "❌ DESCUADRADO"
        );

        DialogUtils.showInfo(info);
    }

    @FXML
    public void onEditar() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            DialogUtils.showWarning("Selecciona un asiento para editar");
            return;
        }
        log.info("Editar asiento: {}", asiento.getNumero());
        DialogUtils.showWarning("Función en desarrollo: Editar asiento contable");
    }

    @FXML
    public void onEliminar() {
        AsientoContable asiento = tableAsientos.getSelectionModel().getSelectedItem();
        if (asiento == null) {
            DialogUtils.showWarning("Selecciona un asiento para eliminar");
            return;
        }

        if (DialogUtils.showConfirm("¿Deseas eliminar este asiento?\n\n" +
                                "Número: " + asiento.getNumero() + "\n" +
                                "Concepto: " + asiento.getConcepto())) {
            try {
                asientoContableService.deleteById(asiento.getId());
                cargarDatos();
                DialogUtils.showSuccess("Asiento eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando asiento", e);
                DialogUtils.showError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando asientos contables");
        cargarDatos();
    }

}
