package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Presupuesto;
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

    @FXML private TableView<Presupuesto> tablePresupuestos;
    @FXML private TableColumn<Presupuesto, String> colNumero;
    @FXML private TableColumn<Presupuesto, LocalDate> colFecha;
    @FXML private TableColumn<Presupuesto, String> colCliente;
    @FXML private TableColumn<Presupuesto, BigDecimal> colBaseImponible;
    @FXML private TableColumn<Presupuesto, BigDecimal> colTotal;
    @FXML private TableColumn<Presupuesto, String> colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblEstado;

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
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

        // Aplicar estilo a la tabla
        if (tablePresupuestos != null) {
            tablePresupuestos.setStyle("-fx-background-color: white; -fx-text-fill: #212529;");

            // Estilo de filas
            tablePresupuestos.setRowFactory(tv -> {
                TableRow<Presupuesto> row = new TableRow<>();
                row.setOnMouseEntered(event -> {
                    if (!row.isEmpty()) {
                        row.setStyle("-fx-background-color: #e9ecef; -fx-cursor: hand;");
                    }
                });
                row.setOnMouseExited(event -> {
                    row.setStyle("");
                });
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
        if (colNumero != null) {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
            colNumero.setCellFactory(column -> new TableCell<Presupuesto, String>() {
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
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            colFecha.setCellFactory(column -> new TableCell<Presupuesto, LocalDate>() {
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
        if (colCliente != null) {
            colCliente.setCellValueFactory(cellData -> {
                Presupuesto presupuesto = cellData.getValue();
                String nombreCliente = "";
                if (presupuesto != null && presupuesto.getCliente() != null) {
                    nombreCliente = presupuesto.getCliente().getNombre();
                }
                return new SimpleStringProperty(nombreCliente);
            });
            colCliente.setCellFactory(column -> new TableCell<Presupuesto, String>() {
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
        if (colBaseImponible != null) {
            colBaseImponible.setCellValueFactory(cellData -> {
                Presupuesto presupuesto = cellData.getValue();
                BigDecimal base = presupuesto.getTotal() != null ?
                    presupuesto.getTotal().divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
                return new javafx.beans.property.SimpleObjectProperty<>(base);
            });
            colBaseImponible.setCellFactory(column -> new TableCell<Presupuesto, BigDecimal>() {
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
        if (colTotal != null) {
            colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
            colTotal.setCellFactory(column -> new TableCell<Presupuesto, BigDecimal>() {
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
        if (colEstado != null) {
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            colEstado.setCellFactory(column -> new TableCell<Presupuesto, String>() {
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
            if (tablePresupuestos != null) {
                tablePresupuestos.setItems(FXCollections.observableArrayList(presupuestos));
            }
            if (lblEstado != null) {
                lblEstado.setText(presupuestos.size() + " presupuestos");
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

            if (tablePresupuestos != null) {
                tablePresupuestos.setItems(FXCollections.observableArrayList(presupuestos));
            }
            if (lblEstado != null) {
                lblEstado.setText(presupuestos.size() + " presupuestos");
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
        mostrarAlerta("Función en desarrollo: Crear nuevo presupuesto");
    }

    @FXML
    public void onVer() {
        Presupuesto presupuesto = tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto primero");
            return;
        }
        log.info("Ver presupuesto: {}", presupuesto.getNumero());

        String info = String.format(
            "Presupuesto: %s\n" +
            "Fecha: %s\n" +
            "Cliente: %s\n" +
            "Total: %.2f €\n" +
            "Estado: %s\n\n" +
            "Observaciones: %s",
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
        Presupuesto presupuesto = tablePresupuestos.getSelectionModel().getSelectedItem();
        if (presupuesto == null) {
            mostrarAlerta("Selecciona un presupuesto para editar");
            return;
        }
        log.info("Editar presupuesto: {}", presupuesto.getNumero());
        mostrarAlerta("Función en desarrollo: Editar presupuesto");
    }

    @FXML
    public void onAceptar() {
        Presupuesto presupuesto = tablePresupuestos.getSelectionModel().getSelectedItem();
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
        Presupuesto presupuesto = tablePresupuestos.getSelectionModel().getSelectedItem();
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
}

