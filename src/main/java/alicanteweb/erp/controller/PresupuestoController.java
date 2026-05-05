package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.controller.formcontroller.PresupuestoFormController;
import alicanteweb.erp.service.PresupuestoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controlador para la gestión de Presupuestos
 */
@Controller
public class PresupuestoController {
    private static final Logger log = LoggerFactory.getLogger(PresupuestoController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<Presupuesto> pres_tablePresupuestos;
    @FXML private TableColumn<Presupuesto, String> pres_colNumero;
    @FXML private TableColumn<Presupuesto, LocalDate> pres_colFecha;
    @FXML private TableColumn<Presupuesto, String> pres_colCliente;
    @FXML private TableColumn<Presupuesto, BigDecimal> pres_colBaseImponible;
    @FXML private TableColumn<Presupuesto, BigDecimal> pres_colTotal;
    @FXML private TableColumn<Presupuesto, LocalDate> pres_colValidez;
    @FXML private TableColumn<Presupuesto, String> pres_colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label pres_lblTotal;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;

    private final PresupuestoService presupuestoService;
    private final ApplicationContext applicationContext;

    public PresupuestoController(PresupuestoService presupuestoService, ApplicationContext applicationContext) {
        this.presupuestoService = presupuestoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando PresupuestoController");
        configurarColumnas();
        configurarComboEstado();
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarPresupuestos(newV));
        }

        // Listeners para los datepickers: re-filtrar cuando cambian
        if (dpFechaDesde == null) {
            // Si la inyección falla (p. ej. en tests), crear uno defensivamente
            dpFechaDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
        } else if (dpFechaDesde.getValue() == null) {
            dpFechaDesde.setValue(LocalDate.now().withDayOfMonth(1));
        }
        dpFechaDesde.valueProperty().addListener((obs, oldV, newV) -> filtrarPresupuestos(txtBuscar != null ? txtBuscar.getText() : ""));

        if (dpFechaHasta == null) {
            dpFechaHasta = new DatePicker(LocalDate.now());
        } else if (dpFechaHasta.getValue() == null) {
            dpFechaHasta.setValue(LocalDate.now());
        }
        dpFechaHasta.valueProperty().addListener((obs, oldV, newV) -> filtrarPresupuestos(txtBuscar != null ? txtBuscar.getText() : ""));

         // Aplicar estilo a la tabla
         if (pres_tablePresupuestos != null) {
             pres_tablePresupuestos.setStyle("-fx-background-color: white; -fx-text-fill: #212529;");

             // Estilo de filas
             pres_tablePresupuestos.setRowFactory(tv -> {
                 TableRow<Presupuesto> row = new TableRow<>();
                 row.setOnMouseEntered(event -> {
                     if (!row.isEmpty()) {
                         row.setStyle("-fx-background-color: #e9ecef; -fx-cursor: hand;");
                     }
                 });
                 row.setOnMouseExited(event -> row.setStyle(""));
                 return row;
             });
         }
    }

    /**
     * Maneja el efecto hover de los botones
     */
    @FXML
    public void handleButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button button) {
            String currentStyle = button.getStyle();
            if (currentStyle.contains("#28a745")) { // Verde
                button.setStyle(currentStyle + "-fx-background-color: #218838;");
            } else if (currentStyle.contains("#007bff")) { // Azul
                button.setStyle(currentStyle + "-fx-background-color: #0056b3;");
            } else if (currentStyle.contains("#dc3545")) { // Rojo
                button.setStyle(currentStyle + "-fx-background-color: #c82333;");
            } else if (currentStyle.contains("#6c757d")) { // Gris
                button.setStyle(currentStyle + "-fx-background-color: #5a6268;");
            }
        }
    }

    /**
     * Restaura el estilo original del botón
     */
    @FXML
    public void handleButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button button) {
            String currentStyle = button.getStyle();
            if (currentStyle.contains("#218838")) {
                button.setStyle(currentStyle.replace("#218838", "#28a745"));
            } else if (currentStyle.contains("#0056b3")) {
                button.setStyle(currentStyle.replace("#0056b3", "#007bff"));
            } else if (currentStyle.contains("#c82333")) {
                button.setStyle(currentStyle.replace("#c82333", "#dc3545"));
            } else if (currentStyle.contains("#5a6268")) {
                button.setStyle(currentStyle.replace("#5a6268", "#6c757d"));
            }
        }
    }

    private void configurarColumnas() {
        if (pres_colNumero != null) {
            pres_colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
            pres_colNumero.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
                    }
                }
            });
        }
        if (pres_colFecha != null) {
            pres_colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            pres_colFecha.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DATE_FORMATTER));
                        setStyle("-fx-text-fill: #495057;");
                    }
                }
            });
        }
        if (pres_colCliente != null) {
            pres_colCliente.setCellValueFactory(cellData -> {
                Presupuesto presupuesto = cellData.getValue();
                String nombreCliente = "";
                if (presupuesto != null && presupuesto.getCliente() != null) {
                    nombreCliente = presupuesto.getCliente().getNombre();
                }
                return new SimpleStringProperty(nombreCliente);
            });
            pres_colCliente.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #212529;");
                    }
                }
            });
        }
        if (pres_colBaseImponible != null) {
            pres_colBaseImponible.setCellValueFactory(cellData -> {
                Presupuesto presupuesto = cellData.getValue();
                BigDecimal base = presupuesto.getTotal() != null ?
                    presupuesto.getTotal().divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
                return new javafx.beans.property.SimpleObjectProperty<>(base);
            });
            pres_colBaseImponible.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f €", item));
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #495057;");
                    }
                }
            });
        }
        if (pres_colTotal != null) {
            pres_colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
            pres_colTotal.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f €", item));
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #28a745;");
                    }
                }
            });
        }
        if (pres_colValidez != null) {
            pres_colValidez.setCellValueFactory(new PropertyValueFactory<>("fechaValidez"));
            pres_colValidez.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DATE_FORMATTER));
                        setStyle("-fx-text-fill: #495057;");
                    }
                }
            });
        }
        if (pres_colEstado != null) {
            pres_colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            pres_colEstado.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String emoji = switch (item) {
                            case "BORRADOR" -> "📝";
                            case "ENVIADO" -> "📧";
                            case "ACEPTADO" -> "✅";
                            case "RECHAZADO" -> "❌";
                            case "CONVERTIDO" -> "🔄";
                            default -> "❓";
                        };
                        setText(emoji + " " + item);

                        String color = switch (item) {
                            case "ACEPTADO" -> "-fx-text-fill: #28a745;";
                            case "RECHAZADO" -> "-fx-text-fill: #dc3545;";
                            case "CONVERTIDO" -> "-fx-text-fill: #007bff;";
                            case "ENVIADO" -> "-fx-text-fill: #fd7e14;";
                            default -> "-fx-text-fill: #6c757d;";
                        };
                        setStyle(color + " -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            });
        }
    }

    private void configurarComboEstado() {
        if (cmbEstado != null) {
            cmbEstado.setItems(FXCollections.observableArrayList(
                "TODOS",
                "BORRADOR",
                "ENVIADO",
                "ACEPTADO",
                "RECHAZADO",
                "CONVERTIDO"
            ));
            cmbEstado.setValue("TODOS");
        }
    }

    private void cargarDatos() {
        try {
            var presupuestos = presupuestoService.obtenerTodos();
            if (pres_tablePresupuestos != null) {
                pres_tablePresupuestos.setItems(FXCollections.observableArrayList(presupuestos));
            }
            if (pres_lblTotal != null) {
                pres_lblTotal.setText(presupuestos.size() + " presupuestos");
            }
            log.info("Presupuestos cargados: {}", presupuestos.size());
        } catch (Exception e) {
            log.error("Error cargando presupuestos", e);
            mostrarError("Error al cargar presupuestos: " + e.getMessage());
        }
    }

    private void filtrarPresupuestos(String busqueda) {
        try {
            var presupuestos = presupuestoService.obtenerTodos();

            // Filtrar por rango de fechas si están seleccionadas
            LocalDate desde = dpFechaDesde != null ? dpFechaDesde.getValue() : null;
            LocalDate hasta = dpFechaHasta != null ? dpFechaHasta.getValue() : null;

            if (desde != null) {
                presupuestos = presupuestos.stream()
                    .filter(p -> p.getFecha() != null && !p.getFecha().isBefore(desde))
                    .toList();
            }
            if (hasta != null) {
                presupuestos = presupuestos.stream()
                    .filter(p -> p.getFecha() != null && !p.getFecha().isAfter(hasta))
                    .toList();
            }

             // Filtrar por estado si está seleccionado
             if (cmbEstado != null && cmbEstado.getValue() != null &&
                 !cmbEstado.getValue().equals("TODOS")) {
                 String estadoFiltro = cmbEstado.getValue();
                 presupuestos = presupuestos.stream()
                     .filter(p -> p.getEstado() != null && p.getEstado().equals(estadoFiltro))
                     .toList();
             }

             // Filtrar por texto de búsqueda
             if (busqueda != null && !busqueda.isEmpty()) {
                 String search = busqueda.toLowerCase();
                 presupuestos = presupuestos.stream()
                     .filter(p -> (p.getNumero() != null && p.getNumero().toLowerCase().contains(search)) ||
                                 (p.getCliente() != null && p.getCliente().getNombre() != null &&
                                  p.getCliente().getNombre().toLowerCase().contains(search)))
                     .toList();
             }

             if (pres_tablePresupuestos != null) {
                 pres_tablePresupuestos.setItems(FXCollections.observableArrayList(presupuestos));
             }
             if (pres_lblTotal != null) {
                 pres_lblTotal.setText(presupuestos.size() + " presupuestos");
             }
         } catch (Exception e) {
             log.error("Error filtrando presupuestos", e);
         }
     }

    @FXML
    public void onBuscar() {
        String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
        filtrarPresupuestos(busqueda);
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nuevo presupuesto");
        abrirFormulario(null);
    }

    @FXML
    public void onVer() {
        Presupuesto presupuesto = pres_tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto primero");
            return;
        }
        log.info("Ver presupuesto: {}", presupuesto.getNumero());

        String info = String.format("""
            Presupuesto: %s
            Fecha: %s
            Cliente: %s
            Total: %.2f €
            Estado: %s

            Observaciones: %s
            """,
            presupuesto.getNumero(),
            presupuesto.getFecha().format(DATE_FORMATTER),
            presupuesto.getCliente() != null ? presupuesto.getCliente().getNombre() : "-",
            presupuesto.getTotal(),
            presupuesto.getEstado(),
            presupuesto.getObservaciones() != null ? presupuesto.getObservaciones() : "-"
        );

        mostrarInfo(info);
    }

    @FXML
    public void onEditar() {
        Presupuesto presupuesto = pres_tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto para editar");
            return;
        }
        log.info("Editar presupuesto: {}", presupuesto.getNumero());
        abrirFormulario(presupuesto);
    }

    @FXML
    public void onEliminar() {
        Presupuesto presupuesto = pres_tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto para eliminar");
            return;
        }

        if (!presupuesto.getEstado().equals("BORRADOR")) {
            mostrarAlerta("Solo se pueden eliminar presupuestos en estado BORRADOR");
            return;
        }

        if (mostrarConfirmacion("¿Deseas eliminar este presupuesto?\n\n" +
                                presupuesto.getNumero())) {
            try {
                presupuestoService.eliminar(presupuesto.getId());
                cargarDatos();
                mostrarExito("Presupuesto eliminado correctamente");
            } catch (Exception e) {
                log.error("Error eliminando presupuesto", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onAceptar() {
        Presupuesto presupuesto = pres_tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto para aceptar");
            return;
        }

        if (!presupuesto.getEstado().equals("ENVIADO")) {
            mostrarAlerta("Solo se pueden aceptar presupuestos en estado ENVIADO");
            return;
        }

        if (mostrarConfirmacion("¿Deseas aceptar este presupuesto?\n\n" +
                                presupuesto.getNumero() + " - " + presupuesto.getCliente().getNombre())) {
            try {
                presupuesto.setEstado("ACEPTADO");
                presupuestoService.guardar(presupuesto);
                cargarDatos();
                mostrarExito("Presupuesto aceptado correctamente");
            } catch (Exception e) {
                log.error("Error aceptando presupuesto", e);
                mostrarError("Error al aceptar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRechazar() {
        Presupuesto presupuesto = pres_tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto para rechazar");
            return;
        }

        if (!presupuesto.getEstado().equals("ENVIADO")) {
            mostrarAlerta("Solo se pueden rechazar presupuestos en estado ENVIADO");
            return;
        }

        if (mostrarConfirmacion("¿Deseas rechazar este presupuesto?\n\n" +
                                presupuesto.getNumero() + " - " + presupuesto.getCliente().getNombre())) {
            try {
                presupuesto.setEstado("RECHAZADO");
                presupuestoService.guardar(presupuesto);
                cargarDatos();
                mostrarExito("Presupuesto rechazado");
            } catch (Exception e) {
                log.error("Error rechazando presupuesto", e);
                mostrarError("Error al rechazar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando presupuestos");
        cargarDatos();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
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

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información del Presupuesto");
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

    private void abrirFormulario(Presupuesto presupuesto) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ui/presupuesto_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            javafx.scene.Parent root = loader.load();

            if (loader.getController() instanceof PresupuestoFormController controller) {
                controller.setPresupuesto(presupuesto);
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(presupuesto == null ? "Nuevo Presupuesto" : "Editar Presupuesto");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de presupuesto", e);
            mostrarError("Error al abrir el formulario: " + e.getMessage());
        }
    }
}
