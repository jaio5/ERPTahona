package alicanteweb.erp.controller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import alicanteweb.erp.ui.Dialogs;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el formulario de creacion/edicion de albaranes
 */
@Controller
public class AlbaranFormController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranFormController.class);

    // Datos principales
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<String> cbProcedencia;

    // Tabla de lineas
    @FXML private TableView<LineaAlbaranTemp> tableLineas;
    @FXML private TableColumn<LineaAlbaranTemp, String> colArticulo;
    @FXML private TableColumn<LineaAlbaranTemp, Integer> colCantidad;
    @FXML private TableColumn<LineaAlbaranTemp, String> colUnidad;
    @FXML private TableColumn<LineaAlbaranTemp, String> colLote;

    // Datos de entrega
    @FXML private TextArea txtDireccionEntrega;
    @FXML private TextArea txtObservaciones;

    private final AlbaranVentaService albaranService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;

    private AlbaranVenta albaranActual;
    private boolean modoEdicion = false;
    private ObservableList<LineaAlbaranTemp> lineasTemp = FXCollections.observableArrayList();

    public AlbaranFormController(AlbaranVentaService albaranService,
                                 ClienteService clienteService,
                                 ArticuloService articuloService) {
        this.albaranService = albaranService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("AlbaranFormController inicializado");

        configurarClientes();
        configurarProcedencia();
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

    private void configurarProcedencia() {
        if (cbProcedencia != null) {
            cbProcedencia.setItems(FXCollections.observableArrayList(
                "Pedido",
                "Presupuesto",
                "Manual"
            ));
            cbProcedencia.setValue("Manual");
        }
    }

    private void configurarTablaLineas() {
        if (colArticulo != null) colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        if (colUnidad != null) colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        if (colLote != null) colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));

        if (tableLineas != null) {
            tableLineas.setItems(lineasTemp);
        }
    }

    private void configurarFechas() {
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
        }
    }

    public void setAlbaran(AlbaranVenta albaran) {
        this.albaranActual = albaran;
        this.modoEdicion = (albaran != null && albaran.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                cargarDatosAlbaran(albaran);
            } else {
                limpiarFormulario();
                generarNumeroAutomatico();
            }
        });
    }

    private void cargarDatosAlbaran(AlbaranVenta albaran) {
        txtNumero.setText(albaran.getNumero());
        dpFecha.setValue(albaran.getFecha() != null ? albaran.getFecha() : LocalDate.now());
        cbCliente.setValue(albaran.getCliente());
        txtObservaciones.setText(albaran.getObservaciones() != null ? albaran.getObservaciones() : "");
    }

    private void limpiarFormulario() {
        txtNumero.setText("");
        dpFecha.setValue(LocalDate.now());
        cbCliente.setValue(null);
        cbProcedencia.setValue("Manual");
        txtDireccionEntrega.setText("");
        txtObservaciones.setText("");
        lineasTemp.clear();
    }

    private void generarNumeroAutomatico() {
        try {
            String numero = albaranService.generarNumeroAlbaran();
            txtNumero.setText(numero);
        } catch (Exception e) {
            log.error("Error generando numero de albaran", e);
            txtNumero.setText("ALB-" + System.currentTimeMillis());
        }
    }

    @FXML
    public void onAgregarLinea() {
        Dialog<LineaAlbaranTemp> dialog = new Dialog<>();
        dialog.setTitle("Agregar Linea");
        dialog.setHeaderText("Selecciona un articulo y la cantidad");

        ButtonType btnAgregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgregar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Articulo> cbArticulo = new ComboBox<>();
        TextField txtCantidad = new TextField("1");
        TextField txtLote = new TextField("");

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
                        setText(item.getCodigo() + " - " + item.getNombre());
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
                        setText(item.getCodigo() + " - " + item.getNombre());
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error cargando articulos", e);
        }

        grid.add(new Label("Articulo:"), 0, 0);
        grid.add(cbArticulo, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);
        grid.add(new Label("Lote/Serie:"), 0, 2);
        grid.add(txtLote, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAgregar) {
                Articulo articulo = cbArticulo.getValue();
                if (articulo != null) {
                    try {
                        int cantidad = Integer.parseInt(txtCantidad.getText());
                        String nombre = articulo.getNombre() != null ? articulo.getNombre() : articulo.getCodigo();
                        String unidad = articulo.getUnidad() != null ? articulo.getUnidad() : "Und";
                        return new LineaAlbaranTemp(nombre, cantidad, unidad, txtLote.getText());
                    } catch (NumberFormatException e) {
                        mostrarError("La cantidad debe ser un numero valido");
                    }
                }
            }
            return null;
        });

        Optional<LineaAlbaranTemp> result = dialog.showAndWait();
        result.ifPresent(linea -> lineasTemp.add(linea));
    }

    @FXML
    public void onEliminarLinea() {
        LineaAlbaranTemp selected = tableLineas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lineasTemp.remove(selected);
        } else {
            mostrarAdvertencia("Selecciona una linea para eliminar");
        }
    }

    @FXML
    public void onGuardar() {
        if (!validarFormulario()) {
            return;
        }

        try {
            if (albaranActual == null) {
                albaranActual = new AlbaranVenta();
            }

            albaranActual.setNumero(txtNumero.getText());
            albaranActual.setFecha(dpFecha.getValue());
            albaranActual.setCliente(cbCliente.getValue());
            albaranActual.setObservaciones(txtObservaciones.getText());

            albaranService.save(albaranActual);

            mostrarExito("Albaran guardado correctamente");
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando albaran", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onConvertirFactura() {
        mostrarInfo("Funcionalidad de conversion a factura en desarrollo");
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (cbCliente.getValue() == null) {
            mostrarAdvertencia("Debes seleccionar un cliente");
            return false;
        }

        if (dpFecha.getValue() == null) {
            mostrarAdvertencia("Debes seleccionar una fecha");
            return false;
        }

        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNumero.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) { Dialogs.showError(mensaje); }
    private void mostrarAdvertencia(String mensaje) { Dialogs.showWarn(mensaje); }
    private void mostrarExito(String mensaje) { Dialogs.showInfo(mensaje); }
    private void mostrarInfo(String mensaje) { Dialogs.showInfo(mensaje); }

    /**
     * Clase temporal para las lineas de albaran en la tabla
     */
    public static class LineaAlbaranTemp {
        private String articulo;
        private int cantidad;
        private String unidad;
        private String lote;

        public LineaAlbaranTemp(String articulo, int cantidad, String unidad, String lote) {
            this.articulo = articulo;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.lote = lote;
        }

        public String getArticulo() { return articulo; }
        public void setArticulo(String articulo) { this.articulo = articulo; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public String getUnidad() { return unidad; }
        public void setUnidad(String unidad) { this.unidad = unidad; }
        public String getLote() { return lote; }
        public void setLote(String lote) { this.lote = lote; }
    }
}

