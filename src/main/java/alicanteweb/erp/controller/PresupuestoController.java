package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.service.PresupuestoService;
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
            tablePresupuestos.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }
    }

    private void configurarColumnas() {
        if (colNumero != null) {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            // Formatear fecha
            colFecha.setCellFactory(column -> new TableCell<Presupuesto, LocalDate>() {
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
        if (colCliente != null) {
            colCliente.setCellValueFactory(cellData -> {
                Presupuesto presupuesto = cellData.getValue();
                String nombreCliente = "";
                if (presupuesto != null && presupuesto.getCliente() != null) {
                    nombreCliente = presupuesto.getCliente().getNombre();
                }
                return new SimpleStringProperty(nombreCliente);
            });
        }
        if (colBaseImponible != null) {
            // Calcular base imponible (total sin IVA)
            colBaseImponible.setCellValueFactory(cellData -> {
                Presupuesto presupuesto = cellData.getValue();
                // Por ahora mostrar el total (falta calcular base sin IVA)
                BigDecimal base = presupuesto.getTotal() != null ? presupuesto.getTotal() : BigDecimal.ZERO;
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
                    }
                }
            });
        }
        if (colEstado != null) {
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            // Formatear con emojis y colores
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
                            case "ACEPTADO" -> "-fx-text-fill: green;";
                            case "RECHAZADO" -> "-fx-text-fill: red;";
                            case "CONVERTIDO" -> "-fx-text-fill: blue;";
                            case "ENVIADO" -> "-fx-text-fill: orange;";
                            default -> "-fx-text-fill: gray;";
                        };
                        setStyle(color + " -fx-font-weight: bold;");
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

