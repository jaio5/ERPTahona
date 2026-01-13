package alicanteweb.erp.controller;

import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.service.FacturaCompraService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controlador para la gestión de Facturas de Compra
 */
@Controller
public class FacturaCompraController {
    private static final Logger log = LoggerFactory.getLogger(FacturaCompraController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<FacturaCompra> tableFacturas;
    @FXML private TableColumn<FacturaCompra, String> colNumero;
    @FXML private TableColumn<FacturaCompra, LocalDate> colFecha;
    @FXML private TableColumn<FacturaCompra, String> colProveedor;
    @FXML private TableColumn<FacturaCompra, BigDecimal> colBase;
    @FXML private TableColumn<FacturaCompra, BigDecimal> colTotal;
    @FXML private TableColumn<FacturaCompra, String> colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    private final FacturaCompraService facturaCompraService;

    public FacturaCompraController(FacturaCompraService facturaCompraService) {
        this.facturaCompraService = facturaCompraService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando FacturaCompraController");
        configurarColumnas();
        configurarComboEstado();
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarFacturas());
        }

        // Aplicar estilo a la tabla
        if (tableFacturas != null) {
            tableFacturas.setStyle("-fx-background-color: white; -fx-text-fill: #212529;");

            // Estilo de filas
            tableFacturas.setRowFactory(tv -> {
                TableRow<FacturaCompra> row = new TableRow<>();
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
            if (currentStyle.contains("#28a745")) {
                button.setStyle(currentStyle.replace("#28a745", "#218838"));
            } else if (currentStyle.contains("#007bff")) {
                button.setStyle(currentStyle.replace("#007bff", "#0056b3"));
            } else if (currentStyle.contains("#dc3545")) {
                button.setStyle(currentStyle.replace("#dc3545", "#c82333"));
            } else if (currentStyle.contains("#6c757d")) {
                button.setStyle(currentStyle.replace("#6c757d", "#5a6268"));
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
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
            colNumero.setCellFactory(column -> new TableCell<>() {
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
            colFecha.setCellFactory(column -> new TableCell<>() {
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

        if (colProveedor != null) {
            colProveedor.setCellValueFactory(cellData -> {
                FacturaCompra factura = cellData.getValue();
                String nombreProveedor = "";
                if (factura != null && factura.getProveedor() != null) {
                    nombreProveedor = factura.getProveedor().getNombre();
                }
                return new SimpleStringProperty(nombreProveedor);
            });
            colProveedor.setCellFactory(column -> new TableCell<>() {
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

        if (colBase != null) {
            colBase.setCellValueFactory(new PropertyValueFactory<>("baseImponible"));
            colBase.setCellFactory(column -> new TableCell<>() {
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
            colTotal.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f €", item));
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
                    }
                }
            });
        }

        if (colEstado != null) {
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            colEstado.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String emoji = switch (item) {
                            case "PENDIENTE" -> "⏳";
                            case "PAGADA" -> "✅";
                            case "CONTABILIZADA" -> "📊";
                            case "ANULADA" -> "❌";
                            default -> "❓";
                        };
                        setText(emoji + " " + item);

                        String color = switch (item) {
                            case "PAGADA" -> "-fx-text-fill: #28a745;";
                            case "CONTABILIZADA" -> "-fx-text-fill: #007bff;";
                            case "ANULADA" -> "-fx-text-fill: #dc3545;";
                            default -> "-fx-text-fill: #fd7e14;";
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
                "PENDIENTE",
                "PAGADA",
                "CONTABILIZADA",
                "ANULADA"
            ));
            cmbEstado.setValue("TODOS");
        }
    }

    private void cargarDatos() {
        try {
            var facturas = facturaCompraService.obtenerTodas();
            if (tableFacturas != null) {
                tableFacturas.setItems(FXCollections.observableArrayList(facturas));
            }
            if (lblTotal != null) {
                lblTotal.setText(facturas.size() + " facturas");
            }
            log.info("Facturas de compra cargadas: {}", facturas.size());
        } catch (Exception e) {
            log.error("Error cargando facturas de compra", e);
            mostrarError("Error al cargar facturas de compra: " + e.getMessage());
        }
    }

    private void filtrarFacturas() {
        try {
            var facturas = facturaCompraService.obtenerTodas();

            // Filtrar por estado
            if (cmbEstado != null && cmbEstado.getValue() != null &&
                !cmbEstado.getValue().equals("TODOS")) {
                String estadoFiltro = cmbEstado.getValue();
                facturas = facturas.stream()
                    .filter(f -> f.getEstado() != null && f.getEstado().equals(estadoFiltro))
                    .toList();
            }

            // Filtrar por texto de búsqueda
            if (txtBuscar != null && txtBuscar.getText() != null && !txtBuscar.getText().isEmpty()) {
                String search = txtBuscar.getText().toLowerCase();
                facturas = facturas.stream()
                    .filter(f -> (f.getNumero() != null && f.getNumero().toLowerCase().contains(search)) ||
                                (f.getProveedor() != null && f.getProveedor().getNombre() != null &&
                                 f.getProveedor().getNombre().toLowerCase().contains(search)))
                    .toList();
            }

            // Filtrar por fechas
            if (dpFechaDesde != null && dpFechaDesde.getValue() != null) {
                LocalDate fechaDesde = dpFechaDesde.getValue();
                facturas = facturas.stream()
                    .filter(f -> f.getFecha() != null && !f.getFecha().isBefore(fechaDesde))
                    .toList();
            }

            if (dpFechaHasta != null && dpFechaHasta.getValue() != null) {
                LocalDate fechaHasta = dpFechaHasta.getValue();
                facturas = facturas.stream()
                    .filter(f -> f.getFecha() != null && !f.getFecha().isAfter(fechaHasta))
                    .toList();
            }

            if (tableFacturas != null) {
                tableFacturas.setItems(FXCollections.observableArrayList(facturas));
            }
            if (lblTotal != null) {
                lblTotal.setText(facturas.size() + " facturas");
            }
        } catch (Exception e) {
            log.error("Error filtrando facturas", e);
        }
    }

    @FXML
    public void onBuscar() {
        filtrarFacturas();
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nueva factura de compra");
        mostrarAlerta("Función en desarrollo: Crear nueva factura de compra");
    }

    @FXML
    public void onVer() {
        FacturaCompra factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarAlerta("Selecciona una factura primero");
            return;
        }
        log.info("Ver factura: {}", factura.getNumero());

        String info = String.format(
            "Factura: %s\n" +
            "Fecha: %s\n" +
            "Proveedor: %s\n" +
            "Base Imponible: %.2f €\n" +
            "IVA: %.2f €\n" +
            "Total: %.2f €\n" +
            "Estado: %s",
            factura.getNumero(),
            factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "-",
            factura.getProveedor() != null ? factura.getProveedor().getNombre() : "-",
            factura.getBaseImponible() != null ? factura.getBaseImponible() : BigDecimal.ZERO,
            factura.getImporteIva() != null ? factura.getImporteIva() : BigDecimal.ZERO,
            factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO,
            factura.getEstado()
        );

        mostrarInfo(info);
    }

    @FXML
    public void onEditar() {
        FacturaCompra factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarAlerta("Selecciona una factura para editar");
            return;
        }
        log.info("Editar factura: {}", factura.getNumero());
        mostrarAlerta("Función en desarrollo: Editar factura");
    }

    @FXML
    public void onContabilizar() {
        FacturaCompra factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarAlerta("Selecciona una factura para contabilizar");
            return;
        }

        if (factura.getEstado().equals("CONTABILIZADA")) {
            mostrarAlerta("Esta factura ya está contabilizada");
            return;
        }

        if (mostrarConfirmacion("¿Deseas contabilizar esta factura?\n\n" +
                                factura.getNumero() + " - " + factura.getProveedor().getNombre())) {
            try {
                factura.setEstado("CONTABILIZADA");
                facturaCompraService.guardar(factura);
                cargarDatos();
                mostrarExito("Factura contabilizada correctamente");
            } catch (Exception e) {
                log.error("Error contabilizando factura", e);
                mostrarError("Error al contabilizar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando facturas de compra");
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
        alert.setTitle("Información de Factura de Compra");
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

