package alicanteweb.erp.controller;

import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.service.FacturaCompraService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import alicanteweb.erp.ErpLauncher;
import org.springframework.context.ApplicationContext;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import alicanteweb.erp.ui.DialogUtils;

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
            DialogUtils.showError("Error al cargar facturas de compra: " + e.getMessage());
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
        try {
            ApplicationContext spring = ErpLauncher.getSpringContext();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/factura_compra_form.fxml"));
            loader.setControllerFactory(spring::getBean);
            Parent parent = loader.load();

            Object controller = loader.getController();
            // Intentar configurar modo creación (setFactura null)
            try {
                var m = controller.getClass().getMethod("setFactura", alicanteweb.erp.entities.FacturaCompra.class);
                m.invoke(controller, (Object) null);
            } catch (NoSuchMethodException ignored) {
                // no pasa nada si no existe
            }

            Stage stage = new Stage();
            stage.setTitle("Nueva Factura de Compra");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos
            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario factura de compra", e);
            DialogUtils.showError("Error abriendo formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onVer() {
        FacturaCompra factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            DialogUtils.showWarning("Selecciona una factura primero");
            return;
        }
        log.info("Ver factura: {}", factura.getNumero());

        String info = String.format("""
            Factura: %s
            Fecha: %s
            Proveedor: %s
            Base Imponible: %.2f €
            IVA: %.2f €
            Total: %.2f €
            Estado: %s
            """,
            factura.getNumero(),
            factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "-",
            factura.getProveedor() != null ? factura.getProveedor().getNombre() : "-",
            factura.getBaseImponible() != null ? factura.getBaseImponible() : BigDecimal.ZERO,
            factura.getImporteIva() != null ? factura.getImporteIva() : BigDecimal.ZERO,
            factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO,
            factura.getEstado()
        );

        DialogUtils.showInfo(info);
    }

    @FXML
    public void onEditar() {
        FacturaCompra factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            DialogUtils.showWarning("Selecciona una factura para editar");
            return;
        }
        log.info("Editar factura: {}", factura.getNumero());
        try {
            ApplicationContext spring = ErpLauncher.getSpringContext();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/factura_compra_form.fxml"));
            loader.setControllerFactory(spring::getBean);
            Parent parent = loader.load();

            Object controller = loader.getController();
            // Intentar pasar la factura al formulario
            try {
                var m = controller.getClass().getMethod("setFactura", alicanteweb.erp.entities.FacturaCompra.class);
                m.invoke(controller, factura);
            } catch (NoSuchMethodException ignored) {
                try {
                    var m2 = controller.getClass().getMethod("setFacturaCompra", alicanteweb.erp.entities.FacturaCompra.class);
                    m2.invoke(controller, factura);
                } catch (NoSuchMethodException ex) {
                    log.debug("El controlador del formulario no expone setFactura/setFacturaCompra");
                }
            }

            Stage stage = new Stage();
            stage.setTitle("Editar Factura de Compra");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario factura de compra", e);
            DialogUtils.showError("Error abriendo formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onContabilizar() {
        FacturaCompra factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            DialogUtils.showWarning("Selecciona una factura para contabilizar");
            return;
        }

        if (factura.getEstado().equals("CONTABILIZADA")) {
            DialogUtils.showWarning("Esta factura ya está contabilizada");
            return;
        }

        if (DialogUtils.showConfirm("¿Deseas contabilizar esta factura?\n\n" +
                                factura.getNumero() + " - " + factura.getProveedor().getNombre())) {
            try {
                factura.setEstado("CONTABILIZADA");
                facturaCompraService.guardar(factura);
                cargarDatos();
                DialogUtils.showInfo("Factura contabilizada correctamente");
             } catch (Exception e) {
                log.error("Error contabilizando factura", e);
                DialogUtils.showError("Error al contabilizar: " + e.getMessage());
             }
         }
     }

    @FXML
    public void onRefresh() {
        log.info("Refrescando facturas de compra");
        cargarDatos();
    }
}
