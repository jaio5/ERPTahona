package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.AlbaranVentaFactura;
import alicanteweb.erp.entities.AlbaranVentaFacturaId;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.FacturaLineaService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.AlbaranVentaService;
import alicanteweb.erp.service.AlbaranVentaFacturaService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * Controlador para el formulario de creación/edición de facturas
 */
@Controller
public class FacturaFormController {
    private static final Logger log = LoggerFactory.getLogger(FacturaFormController.class);

    // Header
    @FXML private Label lblTitulo;
    @FXML private Label lblNumeroFactura;
    @FXML private Label lblEstado;
    @FXML private Label lblTotal;

    // Datos principales
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private DatePicker dpFechaEmision;
    @FXML private DatePicker dpFechaVencimiento;
    @FXML private ComboBox<String> cbFormaPago;
    @FXML private TextField txtObservaciones;

    // Tabla de líneas
    @FXML private TableView<LineaFacturaTemp> tableLineas;
    @FXML private TableColumn<LineaFacturaTemp, String> colArticulo;
    @FXML private TableColumn<LineaFacturaTemp, String> colDescripcion;
    @FXML private TableColumn<LineaFacturaTemp, Integer> colCantidad;
    @FXML private TableColumn<LineaFacturaTemp, BigDecimal> colPrecio;
    @FXML private TableColumn<LineaFacturaTemp, BigDecimal> colIVA;
    @FXML private TableColumn<LineaFacturaTemp, BigDecimal> colSubtotal;

    // Totales
    @FXML private Label lblBaseImponible;
    @FXML private Label lblIVA;
    @FXML private Label lblTotalFactura;

    // Albaranes relacionados (UI)
    @FXML private TextField txtBuscarAlbaran;
    @FXML private TableView<AlbaranVenta> tableAlbaranesDisponibles;
    @FXML private TableColumn<AlbaranVenta, String> colAlbNumero;
    @FXML private TableColumn<AlbaranVenta, LocalDate> colAlbFecha;
    @FXML private TableColumn<AlbaranVenta, BigDecimal> colAlbTotal;

    @FXML private TableView<AlbaranVenta> tableAlbaranesVinculados;
    @FXML private TableColumn<AlbaranVenta, String> colVincNumero;
    @FXML private TableColumn<AlbaranVenta, LocalDate> colVincFecha;

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final FacturaLineaService facturaLineaService;
    private final AlbaranVentaService albaranVentaService;
    private final AlbaranVentaFacturaService albaranVentaFacturaService;

    private Factura facturaActual;
    private boolean modoEdicion = false;
    private final ObservableList<LineaFacturaTemp> lineasTemp = FXCollections.observableArrayList();

    public FacturaFormController(FacturaService facturaService,
                                 ClienteService clienteService,
                                 ArticuloService articuloService,
                                 FacturaLineaService facturaLineaService,
                                 AlbaranVentaService albaranVentaService,
                                 AlbaranVentaFacturaService albaranVentaFacturaService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.facturaLineaService = facturaLineaService;
        this.albaranVentaService = albaranVentaService;
        this.albaranVentaFacturaService = albaranVentaFacturaService;
    }

    @FXML
    public void initialize() {
        log.info("✅ FacturaFormController inicializado");

        configurarClientes();
        configurarFormasPago();
        configurarTablaLineas();
        configurarFechas();
        // Configurar columnas de albaranes
        try {
            if (colAlbNumero != null) colAlbNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
            if (colAlbFecha != null) {
                colAlbFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
                colAlbFecha.setCellFactory(col -> new TableCell<AlbaranVenta, LocalDate>() {
                    private final java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.format(fmt));
                    }
                });
            }
            if (colAlbTotal != null) {
                colAlbTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
                colAlbTotal.setCellFactory(col -> new TableCell<AlbaranVenta, BigDecimal>() {
                    @Override
                    protected void updateItem(BigDecimal item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : String.format("%.2f", item));
                        setStyle("-fx-alignment: CENTER-RIGHT;");
                    }
                });
            }

            if (colVincNumero != null) colVincNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
            if (colVincFecha != null) {
                colVincFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
                colVincFecha.setCellFactory(col -> new TableCell<AlbaranVenta, LocalDate>() {
                    private final java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.format(fmt));
                    }
                });
            }
            // Inicializar tablas vacías para evitar NPEs en la UI
            if (tableAlbaranesDisponibles != null && tableAlbaranesDisponibles.getItems() == null) tableAlbaranesDisponibles.setItems(FXCollections.observableArrayList());
            if (tableAlbaranesVinculados != null && tableAlbaranesVinculados.getItems() == null) tableAlbaranesVinculados.setItems(FXCollections.observableArrayList());
        } catch (Exception e) {
            log.debug("No se pudieron configurar columnas de albaranes: {}", e.getMessage());
        }
    }

    private void configurarClientes() {
        try {
            List<Cliente> clientes = clienteService.findAll()
                .stream()
                .filter(c -> c.getActivo() != null && c.getActivo())
                .toList();

            if (cbCliente != null) {
                cbCliente.setItems(FXCollections.observableArrayList(clientes));

                // Configurar cómo se muestra el cliente
                cbCliente.setCellFactory(param -> new ListCell<Cliente>() {
                    @Override
                    protected void updateItem(Cliente item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getCodigo() + " - " + item.getNombre());
                        }
                    }
                });

                cbCliente.setButtonCell(new ListCell<Cliente>() {
                    @Override
                    protected void updateItem(Cliente item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getCodigo() + " - " + item.getNombre());
                        }
                    }
                });
            }

        } catch (Exception e) {
            log.error("Error cargando clientes", e);
        }
    }

    private void configurarFormasPago() {
        if (cbFormaPago != null) {
            cbFormaPago.setItems(FXCollections.observableArrayList(Arrays.asList(
                "Efectivo",
                "Transferencia",
                "Tarjeta",
                "Pagare",
                "Contado",
                "30 dias",
                "60 dias",
                "90 dias"
            )));
            cbFormaPago.setValue("Contado");
        }
    }

    private void configurarTablaLineas() {
        // Evitar NPE si las columnas no están presentes en FXML
        if (colArticulo != null) colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colIVA != null) colIVA.setCellValueFactory(new PropertyValueFactory<>("iva"));
        if (colSubtotal != null) colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        if (tableLineas != null) {
            tableLineas.setItems(lineasTemp);
        }

        // Listener para recalcular totales
        lineasTemp.addListener((javafx.collections.ListChangeListener<LineaFacturaTemp>) c -> calcularTotales());
    }

    private void configurarFechas() {
        if (dpFechaEmision != null) dpFechaEmision.setValue(LocalDate.now());
        if (dpFechaVencimiento != null) dpFechaVencimiento.setValue(LocalDate.now().plusDays(30));
    }

    public void setFactura(Factura factura) {
        this.facturaActual = factura;
        this.modoEdicion = (factura != null && factura.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion && factura != null) {
                if (lblTitulo != null) lblTitulo.setText("Editar Factura");
                cargarDatosFactura(factura);
            } else {
                if (lblTitulo != null) lblTitulo.setText("Nueva Factura");
                limpiarFormulario();
            }
        });
    }

    private void cargarDatosFactura(Factura factura) {
        if (factura == null) return;

        if (factura.getCliente() != null && cbCliente != null) {
            cbCliente.setValue(factura.getCliente());
        }

        if (factura.getFecha() != null && dpFechaEmision != null) {
            dpFechaEmision.setValue(factura.getFecha());
        }

        if (factura.getFechaVencimiento() != null && dpFechaVencimiento != null) {
            dpFechaVencimiento.setValue(factura.getFechaVencimiento());
        }

        if (txtObservaciones != null) txtObservaciones.setText(factura.getObservaciones() != null ? factura.getObservaciones() : "");
        if (lblNumeroFactura != null) lblNumeroFactura.setText("Numero: " + (factura.getNumero() != null ? factura.getNumero() : "Pendiente"));
        if (lblEstado != null) lblEstado.setText(factura.getEstado() != null ? factura.getEstado() : "BORRADOR");

        // Las líneas se cargarían aquí si tuviéramos la relación en la entidad
        // Por ahora, en modo crear empezamos sin líneas

        calcularTotales();
        // Cargar albaranes relacionados y disponibles
        cargarAlbaranesDisponibles("");
        cargarAlbaranesVinculados();
    }

    // ---------------- Albaranes relacionados ----------------
    private void cargarAlbaranesDisponibles(String filtro) {
        try {
            List<AlbaranVenta> albs = albaranVentaService.findAll();
            if (filtro != null && !filtro.isBlank()) {
                String low = filtro.toLowerCase();
                albs = albs.stream().filter(a -> a.getNumero() != null && a.getNumero().toLowerCase().contains(low)).toList();
            }
            if (tableAlbaranesDisponibles != null) tableAlbaranesDisponibles.setItems(FXCollections.observableArrayList(albs));
        } catch (Exception e) {
            log.error("Error cargando albaranes disponibles", e);
        }
    }

    private void cargarAlbaranesVinculados() {
        try {
            if (facturaActual == null || facturaActual.getId() == null) {
                if (tableAlbaranesVinculados != null) tableAlbaranesVinculados.setItems(FXCollections.observableArrayList());
                return;
            }

            List<AlbaranVentaFactura> enlaces = albaranVentaFacturaService.findAll();
            List<AlbaranVenta> vinculados = enlaces.stream()
                .filter(e -> e.getId() != null && e.getId().getFacturasId() != null && e.getId().getFacturasId().equals(facturaActual.getId()))
                .map(e -> albaranVentaService.findById(e.getId().getAlbaranesventasId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

            if (tableAlbaranesVinculados != null) tableAlbaranesVinculados.setItems(FXCollections.observableArrayList(vinculados));
        } catch (Exception e) {
            log.error("Error cargando albaranes vinculados", e);
        }
    }

    @FXML
    public void onBuscarAlbaran() {
        String term = txtBuscarAlbaran != null ? txtBuscarAlbaran.getText() : "";
        cargarAlbaranesDisponibles(term);
    }

    @FXML
    public void onVincularAlbaran() {
        if (facturaActual == null || facturaActual.getId() == null) { mostrarAdvertencia("Guarda la factura antes de vincular albaranes"); return; }
        AlbaranVenta sel = tableAlbaranesDisponibles != null ? tableAlbaranesDisponibles.getSelectionModel().getSelectedItem() : null;
        if (sel == null) { mostrarAdvertencia("Selecciona un albarán para vincular"); return; }
        try {
            AlbaranVentaFacturaId id = new AlbaranVentaFacturaId();
            id.setAlbaranesventasId(sel.getId());
            id.setFacturasId(facturaActual.getId());
            AlbaranVentaFactura avf = new AlbaranVentaFactura();
            avf.setId(id);
            avf.setFacturas(facturaActual);
            albaranVentaFacturaService.save(avf);
            cargarAlbaranesVinculados();
            mostrarExito("Albarán vinculado");
        } catch (Exception e) {
            log.error("Error vinculando albaran", e);
            mostrarError("Error al vincular albarán: " + e.getMessage());
        }
    }

    @FXML
    public void onDesvincularAlbaran() {
        AlbaranVenta sel = tableAlbaranesVinculados != null ? tableAlbaranesVinculados.getSelectionModel().getSelectedItem() : null;
        if (sel == null) { mostrarAdvertencia("Selecciona un albarán vinculado para desvincular"); return; }
        try {
            AlbaranVentaFacturaId id = new AlbaranVentaFacturaId();
            id.setAlbaranesventasId(sel.getId());
            id.setFacturasId(facturaActual.getId());
            albaranVentaFacturaService.deleteById(id);
            cargarAlbaranesVinculados();
            mostrarExito("Albarán desvinculado");
        } catch (Exception e) {
            log.error("Error desvinculando albaran", e);
            mostrarError("Error al desvincular albarán: " + e.getMessage());
        }
    }
    // ---------------- end albaranes ----------------

    private void limpiarFormulario() {
        if (cbCliente != null) cbCliente.setValue(null);
        if (dpFechaEmision != null) dpFechaEmision.setValue(LocalDate.now());
        if (dpFechaVencimiento != null) dpFechaVencimiento.setValue(LocalDate.now().plusDays(30));
        if (cbFormaPago != null) cbFormaPago.setValue("Contado");
        if (txtObservaciones != null) txtObservaciones.clear();
        lineasTemp.clear();
        if (lblNumeroFactura != null) lblNumeroFactura.setText("Numero: Pendiente");
        if (lblEstado != null) lblEstado.setText("BORRADOR");
        calcularTotales();
    }

    @FXML
    public void onAgregarLinea() {
        // Crear diálogo para agregar línea
        Dialog<LineaFacturaTemp> dialog = new Dialog<>();
        dialog.setTitle("Agregar Articulo");
        dialog.setHeaderText("Selecciona un articulo y la cantidad");

        // Botones
        ButtonType btnAgregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgregar, ButtonType.CANCEL);

        // Contenido
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Articulo> cbArticulo = new ComboBox<>();
        TextField txtCantidad = new TextField("1");
        TextField txtPrecio = new TextField();
        TextField txtIva = new TextField("21");

        // Cargar artículos
        try {
            List<Articulo> articulos = articuloService.findAll()
                .stream()
                .filter(a -> a.getActivo() != null && a.getActivo())
                .toList();
            cbArticulo.setItems(FXCollections.observableArrayList(articulos));

            cbArticulo.setCellFactory(param -> new ListCell<Articulo>() {
                @Override
                protected void updateItem(Articulo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String codigo = item.getCodigo() != null ? item.getCodigo() : "???";
                        String nombre = item.getNombre() != null && !item.getNombre().trim().isEmpty()
                            ? item.getNombre() : "[SIN NOMBRE]";
                        setText(codigo + " - " + nombre);
                    }
                }
            });

            cbArticulo.setButtonCell(new ListCell<Articulo>() {
                @Override
                protected void updateItem(Articulo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String codigo = item.getCodigo() != null ? item.getCodigo() : "???";
                        String nombre = item.getNombre() != null && !item.getNombre().trim().isEmpty()
                            ? item.getNombre() : "[SIN NOMBRE]";
                        setText(codigo + " - " + nombre);
                    }
                }
            });

            // Al seleccionar artículo, cargar precio e IVA
            cbArticulo.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    txtPrecio.setText(newVal.getPvp() != null ? newVal.getPvp().toString() : "0.00");
                    txtIva.setText(newVal.getIva() != null ? newVal.getIva().toString() : "21");
                }
            });

        } catch (Exception e) {
            log.error("Error cargando artículos", e);
        }

        grid.add(new Label("Articulo:"), 0, 0);
        grid.add(cbArticulo, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(txtPrecio, 1, 2);
        grid.add(new Label("IVA %:"), 0, 3);
        grid.add(txtIva, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAgregar) {
                Articulo articuloSeleccionado = cbArticulo.getValue();
                if (articuloSeleccionado != null) {
                    LineaFacturaTemp linea = new LineaFacturaTemp();

                    // Guardar el ID del artículo para facilitar el guardado posterior
                    linea.setArticuloId(articuloSeleccionado.getId());

                    // Obtener nombre del artículo (con validación)
                    String nombreArticulo = articuloSeleccionado.getNombre();
                    if (nombreArticulo == null || nombreArticulo.trim().isEmpty()) {
                        // Si el nombre es null, usar el código como fallback
                        nombreArticulo = articuloSeleccionado.getCodigo() != null ?
                            articuloSeleccionado.getCodigo() : "SIN NOMBRE";
                        log.warn("⚠️ Artículo sin nombre. ID: {}, usando código: {}",
                            articuloSeleccionado.getId(), nombreArticulo);
                    }

                    linea.setArticulo(nombreArticulo);

                    // Descripción con fallback
                    String descripcion = articuloSeleccionado.getDescripcion();
                    if (descripcion == null || descripcion.trim().isEmpty()) {
                        descripcion = nombreArticulo;
                    }
                    linea.setDescripcion(descripcion);

                    // Validar y parsear cantidad, precio e IVA de forma segura
                    try {
                        int cantidadVal = Integer.parseInt(txtCantidad.getText().trim());
                        linea.setCantidad(cantidadVal);
                    } catch (Exception ex) {
                        mostrarError("Cantidad inválida: " + txtCantidad.getText());
                        return null;
                    }

                    try {
                        BigDecimal precioVal = new BigDecimal(txtPrecio.getText().trim());
                        linea.setPrecio(precioVal);
                    } catch (Exception ex) {
                        mostrarError("Precio inválido: " + txtPrecio.getText());
                        return null;
                    }

                    try {
                        BigDecimal ivaVal = new BigDecimal(txtIva.getText().trim());
                        linea.setIva(ivaVal);
                    } catch (Exception ex) {
                        mostrarError("IVA inválido: " + txtIva.getText());
                        return null;
                    }

                    linea.calcularSubtotal();

                    log.info("✅ Línea agregada: {} (ID:{}) x{} = {}",
                        nombreArticulo, articuloSeleccionado.getId(), linea.getCantidad(), linea.getSubtotal());

                    return linea;
                } else {
                    mostrarAdvertencia("Seleccione un artículo antes de agregar.");
                }
            }
            return null;
        });

        Optional<LineaFacturaTemp> result = dialog.showAndWait();
        result.ifPresent(linea -> {
            lineasTemp.add(linea);
            calcularTotales();
        });
    }

    @FXML
    public void onEliminarLinea() {
        LineaFacturaTemp selected = tableLineas != null ? tableLineas.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            lineasTemp.remove(selected);
            calcularTotales();
        } else {
            mostrarAdvertencia("Selecciona una linea para eliminar");
        }
    }

    private void calcularTotales() {
        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIVA = BigDecimal.ZERO;

        for (LineaFacturaTemp linea : lineasTemp) {
            BigDecimal subtotal = linea.getSubtotal() != null ? linea.getSubtotal() : BigDecimal.ZERO;
            baseImponible = baseImponible.add(subtotal);

            BigDecimal ivaLinea = BigDecimal.ZERO;
            if (linea.getIva() != null) {
                ivaLinea = subtotal.multiply(linea.getIva())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            totalIVA = totalIVA.add(ivaLinea);
        }

        BigDecimal total = baseImponible.add(totalIVA);

        // Actualizar UI sólo si los labels existen
        if (lblBaseImponible != null) lblBaseImponible.setText(String.format("%.2f EUR", baseImponible));
        if (lblIVA != null) lblIVA.setText(String.format("%.2f EUR", totalIVA));
        if (lblTotalFactura != null) lblTotalFactura.setText(String.format("%.2f EUR", total));
        if (lblTotal != null) lblTotal.setText(String.format("Total: %.2f EUR", total));
    }

    @FXML
    public void onGuardarBorrador() {
        guardarFactura("BORRADOR");
    }

    @FXML
    public void onEmitir() {
        if (!validarFormulario()) {
            return;
        }

        // Guardar primero en estado REVISION para que Verifactu valide el estado y las líneas
        guardarFactura("REVISION");

        // facturaActual debe tener ahora un ID
        if (facturaActual == null || facturaActual.getId() == null) {
            mostrarError("No se pudo obtener el ID de la factura tras guardarla. No se puede emitir.");
            return;
        }

        // Preparar tarea en background para aprobar y emitir (envío obligatorio a AEAT)
        Task<Void> tareaEmitir = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Llamada que lanza excepción si el envío a AEAT falla
                facturaService.aprobarYEmitir(facturaActual.getId());
                return null;
            }
        };

        // Mostrar indicador de progreso modal
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(80, 80);

        Platform.runLater(() -> {
            Stage stage = (Stage) lblTitulo.getScene().getWindow();
            Dialog<Void> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle("Emitir factura");
            dialog.getDialogPane().setContent(pi);
            dialog.getDialogPane().getButtonTypes().clear();
            dialog.setResizable(false);
            dialog.show();

            tareaEmitir.setOnSucceeded(ev -> {
                dialog.close();
                mostrarExito("Factura emitida y enviada a AEAT correctamente");
                // Cerrar ventana del formulario
                cerrarVentana();
            });

            tareaEmitir.setOnFailed(ev -> {
                dialog.close();
                Throwable ex = tareaEmitir.getException();
                String msg = ex != null ? ex.getMessage() : "Error desconocido al emitir";
                mostrarError("Error al emitir la factura: " + msg);
            });

            new Thread(tareaEmitir, "emitir-factura-thread").start();
        });
    }

    private void guardarFactura(String estado) {
        try {
            if (facturaActual == null) {
                facturaActual = new Factura();
            }

            // Datos básicos
            if (cbCliente != null) facturaActual.setCliente(cbCliente.getValue());
            if (dpFechaEmision != null) facturaActual.setFecha(dpFechaEmision.getValue());
            if (dpFechaVencimiento != null) facturaActual.setFechaVencimiento(dpFechaVencimiento.getValue());
            if (txtObservaciones != null) facturaActual.setObservaciones(txtObservaciones.getText());
            facturaActual.setEstado(estado);
            facturaActual.setSerie(resolverSerieFactura());

            // Forma de pago
            if (cbFormaPago != null && cbFormaPago.getValue() != null) {
                facturaActual.setMedioCobro(cbFormaPago.getValue());
            }

            // Calcular totales
            BigDecimal baseImponible = BigDecimal.ZERO;
            BigDecimal totalIVA = BigDecimal.ZERO;

            for (LineaFacturaTemp linea : lineasTemp) {
                baseImponible = baseImponible.add(linea.getSubtotal() != null ? linea.getSubtotal() : BigDecimal.ZERO);
                BigDecimal ivaLinea = BigDecimal.ZERO;
                if (linea.getIva() != null && linea.getSubtotal() != null) {
                    ivaLinea = linea.getSubtotal().multiply(linea.getIva())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
                totalIVA = totalIVA.add(ivaLinea);
            }

            facturaActual.setBaseImponible(baseImponible);
            facturaActual.setTotalIva(totalIVA);
            facturaActual.setTotal(baseImponible.add(totalIVA));

            // Generar número de factura si es nueva
            if (facturaActual.getNumero() == null || facturaActual.getNumero().isEmpty()) {
                String numeroFactura = facturaService.generarSiguienteNumero(
                    facturaActual.getSerie(),
                    facturaActual.getFecha(),
                    "RECTIFICATIVA".equalsIgnoreCase(facturaActual.getTipoFactura())
                );
                facturaActual.setNumero(numeroFactura);
            }

            // Guardar factura primero
            Factura guardada = facturaService.save(facturaActual);
            log.info("✅ Factura guardada: {} - Estado: {}", guardada.getId(), estado);

            // Guardar líneas de factura
            if (!lineasTemp.isEmpty()) {
                log.info("💾 Guardando {} líneas de factura...", lineasTemp.size());

                for (LineaFacturaTemp lineaTemp : lineasTemp) {
                    FacturaLinea linea = new FacturaLinea();
                    linea.setFactura(guardada);

                    // Usar el ID del artículo que guardamos anteriormente
                    if (lineaTemp.getArticuloId() != null) {
                        Articulo articulo = articuloService.findById(lineaTemp.getArticuloId())
                            .orElse(null);
                        if (articulo != null) {
                            linea.setArticulo(articulo);
                        } else {
                            log.warn("⚠️ Artículo no encontrado con ID: {}", lineaTemp.getArticuloId());
                        }
                    }

                    linea.setCantidad(new BigDecimal(lineaTemp.getCantidad()));
                    linea.setPrecio(lineaTemp.getPrecio());
                    linea.setIva(lineaTemp.getIva());

                    facturaLineaService.save(linea);
                    log.info("  ✓ Línea guardada: {} x{} = {}",
                        lineaTemp.getArticulo(), lineaTemp.getCantidad(), lineaTemp.getSubtotal());
                }

                log.info("✅ {} líneas guardadas correctamente", lineasTemp.size());
            }

            mostrarExito("Factura " + (estado.equals("EMITIDA") ? "emitida" : "guardada") + " correctamente\n" +
                "Número: " + guardada.getNumero() + "\n" +
                "Total: " + guardada.getTotal() + " EUR");
            // No cerrar la ventana si la factura queda en REVISION (se lanzará el envío en background)
            if (!"REVISION".equals(estado)) {
                cerrarVentana();
            }

        } catch (Exception e) {
            log.error("❌ Error guardando factura", e);
            mostrarError("Error al guardar la factura: " + e.getMessage());
        }
    }

    private String resolverSerieFactura() {
        Cliente cliente = cbCliente != null ? cbCliente.getValue() : null;
        String serieBase = "GEN";

        if (cliente != null && cliente.getCodigo() != null && !cliente.getCodigo().isBlank()) {
            serieBase = cliente.getCodigo().trim();
        }

        return facturaService.normalizarSerie(serieBase);
    }

    @FXML
    public void onCancelar() {
        if (formularioModificado()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar");
            confirmacion.setHeaderText("Descartar cambios?");
            confirmacion.setContentText("Hay cambios sin guardar. Desea salir sin guardar?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                cerrarVentana();
            }
        } else {
            cerrarVentana();
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (cbCliente == null || cbCliente.getValue() == null) {
            errores.append("Debe seleccionar un cliente\n");
        }

        if (dpFechaEmision == null || dpFechaEmision.getValue() == null) {
            errores.append("La fecha de emision es obligatoria\n");
        }

        if (lineasTemp.isEmpty()) {
            errores.append("Debe agregar al menos una linea a la factura\n");
        }

        String erroresStr = errores.toString();
        if (!erroresStr.isEmpty()) {
            mostrarAlerta("Por favor, corrija los siguientes errores:\n\n" + erroresStr);
            return false;
        }

        return true;
    }

    private boolean formularioModificado() {
        return (cbCliente != null && cbCliente.getValue() != null) || !lineasTemp.isEmpty();
    }

    private void cerrarVentana() {
        if (lblTitulo == null) return;
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atencion");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarExito(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exito");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarAdvertencia(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    // Clase interna para las líneas temporales
    @Getter
    @Setter
    public static class LineaFacturaTemp {
        private Long articuloId;  // ID del artículo para facilitar el guardado
        private String articulo;
        private String descripcion;
        private Integer cantidad;
        private BigDecimal precio;
        private BigDecimal iva;
        private BigDecimal subtotal;

        public void calcularSubtotal() {
            if (cantidad != null && precio != null) {
                this.subtotal = precio.multiply(new BigDecimal(cantidad));
            } else {
                this.subtotal = BigDecimal.ZERO;
            }
        }
    }
}
