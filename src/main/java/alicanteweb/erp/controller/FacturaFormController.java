package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.FacturaLineaService;
import alicanteweb.erp.service.ArticuloService;
import javafx.application.Platform;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final FacturaLineaService facturaLineaService;

    private Factura facturaActual;
    private boolean modoEdicion = false;
    private ObservableList<LineaFacturaTemp> lineasTemp = FXCollections.observableArrayList();

    public FacturaFormController(FacturaService facturaService,
                                 ClienteService clienteService,
                                 ArticuloService articuloService,
                                 FacturaLineaService facturaLineaService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.facturaLineaService = facturaLineaService;
    }

    @FXML
    public void initialize() {
        log.info("✅ FacturaFormController inicializado");

        configurarClientes();
        configurarFormasPago();
        configurarTablaLineas();
        configurarFechas();
    }

    private void configurarClientes() {
        try {
            List<Cliente> clientes = clienteService.findAll()
                .stream()
                .filter(c -> c.getActivo() != null && c.getActivo())
                .toList();

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
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colIVA.setCellValueFactory(new PropertyValueFactory<>("iva"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tableLineas.setItems(lineasTemp);

        // Listener para recalcular totales
        lineasTemp.addListener((javafx.collections.ListChangeListener.Change<? extends LineaFacturaTemp> c) -> {
            calcularTotales();
        });
    }

    private void configurarFechas() {
        dpFechaEmision.setValue(LocalDate.now());
        dpFechaVencimiento.setValue(LocalDate.now().plusDays(30));
    }

    public void setFactura(Factura factura) {
        this.facturaActual = factura;
        this.modoEdicion = (factura != null && factura.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                lblTitulo.setText("Editar Factura");
                cargarDatosFactura(factura);
            } else {
                lblTitulo.setText("Nueva Factura");
                limpiarFormulario();
            }
        });
    }

    private void cargarDatosFactura(Factura factura) {
        if (factura.getCliente() != null) {
            cbCliente.setValue(factura.getCliente());
        }

        if (factura.getFecha() != null) {
            dpFechaEmision.setValue(factura.getFecha());
        }

        if (factura.getFechaVencimiento() != null) {
            dpFechaVencimiento.setValue(factura.getFechaVencimiento());
        }

        txtObservaciones.setText(factura.getObservaciones() != null ? factura.getObservaciones() : "");
        lblNumeroFactura.setText("Numero: " + (factura.getNumero() != null ? factura.getNumero() : "Pendiente"));
        lblEstado.setText(factura.getEstado() != null ? factura.getEstado() : "BORRADOR");

        // Las líneas se cargarían aquí si tuviéramos la relación en la entidad
        // Por ahora, en modo crear empezamos sin líneas

        calcularTotales();
    }

    private void limpiarFormulario() {
        cbCliente.setValue(null);
        dpFechaEmision.setValue(LocalDate.now());
        dpFechaVencimiento.setValue(LocalDate.now().plusDays(30));
        cbFormaPago.setValue("Contado");
        txtObservaciones.clear();
        lineasTemp.clear();
        lblNumeroFactura.setText("Numero: Pendiente");
        lblEstado.setText("BORRADOR");
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

                    linea.setCantidad(Integer.parseInt(txtCantidad.getText()));
                    linea.setPrecio(new BigDecimal(txtPrecio.getText()));
                    linea.setIva(new BigDecimal(txtIva.getText()));
                    linea.calcularSubtotal();

                    log.info("✅ Línea agregada: {} (ID:{}) x{} = {}",
                        nombreArticulo, articuloSeleccionado.getId(), linea.getCantidad(), linea.getSubtotal());

                    return linea;
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
        LineaFacturaTemp selected = tableLineas.getSelectionModel().getSelectedItem();
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
            BigDecimal subtotal = linea.getSubtotal();
            baseImponible = baseImponible.add(subtotal);

            BigDecimal ivaLinea = subtotal.multiply(linea.getIva())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            totalIVA = totalIVA.add(ivaLinea);
        }

        BigDecimal total = baseImponible.add(totalIVA);

        lblBaseImponible.setText(String.format("%.2f EUR", baseImponible));
        lblIVA.setText(String.format("%.2f EUR", totalIVA));
        lblTotalFactura.setText(String.format("%.2f EUR", total));
        lblTotal.setText(String.format("Total: %.2f EUR", total));
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
        guardarFactura("EMITIDA");
    }

    private void guardarFactura(String estado) {
        try {
            if (facturaActual == null) {
                facturaActual = new Factura();
            }

            // Datos básicos
            facturaActual.setCliente(cbCliente.getValue());
            facturaActual.setFecha(dpFechaEmision.getValue());
            facturaActual.setFechaVencimiento(dpFechaVencimiento.getValue());
            facturaActual.setObservaciones(txtObservaciones.getText());
            facturaActual.setEstado(estado);

            // Forma de pago
            if (cbFormaPago.getValue() != null) {
                facturaActual.setMedioCobro(cbFormaPago.getValue());
            }

            // Calcular totales
            BigDecimal baseImponible = BigDecimal.ZERO;
            BigDecimal totalIVA = BigDecimal.ZERO;

            for (LineaFacturaTemp linea : lineasTemp) {
                baseImponible = baseImponible.add(linea.getSubtotal());
                BigDecimal ivaLinea = linea.getSubtotal().multiply(linea.getIva())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                totalIVA = totalIVA.add(ivaLinea);
            }

            facturaActual.setBaseImponible(baseImponible);
            facturaActual.setTotalIva(totalIVA);
            facturaActual.setTotal(baseImponible.add(totalIVA));

            // Generar número de factura si es nueva
            if (facturaActual.getNumero() == null || facturaActual.getNumero().isEmpty()) {
                String numeroFactura = generarNumeroFactura();
                facturaActual.setNumero(numeroFactura);
            }

            // Guardar factura primero
            Factura guardada = facturaService.save(facturaActual);
            log.info("✅ Factura guardada: {} - Estado: {}", guardada.getId(), estado);

            // Guardar líneas de factura
            if (!lineasTemp.isEmpty()) {
                log.info("💾 Guardando {} líneas de factura...", lineasTemp.size());

                // Primero eliminar las líneas existentes si estamos editando
                if (modoEdicion && guardada.getId() != null) {
                    // TODO: Eliminar líneas anteriores si las hubiera
                }

                // Guardar las nuevas líneas
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
            cerrarVentana();

        } catch (Exception e) {
            log.error("❌ Error guardando factura", e);
            mostrarError("Error al guardar la factura: " + e.getMessage());
        }
    }

    private String generarNumeroFactura() {
        // Generar número de factura basado en el año y un contador
        LocalDate hoy = LocalDate.now();
        int año = hoy.getYear();

        // Obtener el último número de factura del año actual
        List<Factura> facturas = facturaService.findAll();
        long numeroMaximo = facturas.stream()
            .filter(f -> f.getNumero() != null && f.getNumero().startsWith("F-" + año))
            .map(f -> {
                try {
                    String[] partes = f.getNumero().split("-");
                    if (partes.length == 3) {
                        return Long.parseLong(partes[2]);
                    }
                } catch (Exception e) {
                    // Ignorar errores de parseo
                }
                return 0L;
            })
            .max(Long::compareTo)
            .orElse(0L);

        long siguienteNumero = numeroMaximo + 1;
        return String.format("F-%d-%04d", año, siguienteNumero);
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

        if (cbCliente.getValue() == null) {
            errores.append("Debe seleccionar un cliente\n");
        }

        if (dpFechaEmision.getValue() == null) {
            errores.append("La fecha de emision es obligatoria\n");
        }

        if (lineasTemp.isEmpty()) {
            errores.append("Debe agregar al menos una linea a la factura\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Por favor, corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean formularioModificado() {
        return cbCliente.getValue() != null || !lineasTemp.isEmpty();
    }

    private void cerrarVentana() {
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Clase interna para las líneas temporales
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
            }
        }

        // Getters y setters
        public Long getArticuloId() { return articuloId; }
        public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

        public String getArticulo() { return articulo; }
        public void setArticulo(String articulo) { this.articulo = articulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
            calcularSubtotal();
        }

        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
            calcularSubtotal();
        }

        public BigDecimal getIva() { return iva; }
        public void setIva(BigDecimal iva) { this.iva = iva; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}

