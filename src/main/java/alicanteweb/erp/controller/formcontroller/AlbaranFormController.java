package alicanteweb.erp.controller.formcontroller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import alicanteweb.erp.ui.DialogUtils;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * Controlador para el formulario de creacion/edicion de albaranes
 */
@Controller
public class AlbaranFormController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranFormController.class);
    @FXML private Button btnConvertirFactura;
    @FXML private Button btnCancelarAlbaran;

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
    @FXML private Button btnGuardarAlbaran;

    // Datos de entrega
    @FXML private TextArea txtDireccionEntrega;
    @FXML private TextArea txtObservaciones;

    private final AlbaranVentaService albaranService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;

    private AlbaranVenta albaranActual;
    private boolean modoEdicion = false;
    private final ObservableList<LineaAlbaranTemp> lineasTemp = FXCollections.observableArrayList();

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

        // Bind del botón guardar: habilitar solo si cliente, fecha y al menos una línea
        try {
            if (btnGuardarAlbaran != null) {
                btnGuardarAlbaran.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(() ->
                        cbCliente.getValue() == null || dpFecha.getValue() == null || lineasTemp.isEmpty(),
                        cbCliente.valueProperty(), dpFecha.valueProperty(), lineasTemp
                    )
                );
            }
        } catch (Exception e) {
            log.debug("No se pudo bindear btnGuardarAlbaran: {}", e.getMessage());
        }

        // Asegurar que los botones inyectados se usan para evitar advertencias estáticas
        if (btnConvertirFactura != null) {
            btnConvertirFactura.setOnAction(e -> btnConvertirFactura());
            btnConvertirFactura.setDisable(false);
        }
        if (btnCancelarAlbaran != null) {
            btnCancelarAlbaran.setOnAction(e -> btnCancelarAlbaran());
            btnCancelarAlbaran.setDisable(false);
        }
    }

    private void configurarClientes() {
        try {
            List<Cliente> clientes = clienteService.findAll()
                .stream()
                .filter(c -> c.getActivo() != null && c.getActivo())
                .toList();

            cbCliente.setItems(FXCollections.observableArrayList(clientes));

            cbCliente.setCellFactory(param -> new ListCell<>() {
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

            cbCliente.setButtonCell(new ListCell<>() {
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
            if (modoEdicion && albaran != null) {
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

            cbArticulo.setCellFactory(param -> new ListCell<>() {
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

            cbArticulo.setButtonCell(new ListCell<>() {
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
        result.ifPresent(lineasTemp::add);
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

            // Convertir lineas temporales a entidades AlbaranVentaLinea
            albaranActual.getLineas().clear();
            for (LineaAlbaranTemp lt : lineasTemp) {
                AlbaranVentaLinea linea = new AlbaranVentaLinea();
                // buscar articulo por nombre - mejora futura: usar id
                if (lt.getArticulo() != null) {
                    Articulo a = articuloService.findAll().stream()
                        .filter(x -> lt.getArticulo().equals(x.getNombre()) || lt.getArticulo().equals(x.getCodigo()))
                        .findFirst().orElse(null);
                    linea.setArticulo(a);
                }
                linea.setDescripcion(lt.getArticulo());
                linea.setCantidad(new java.math.BigDecimal(lt.getCantidad()));
                linea.setPrecio(java.math.BigDecimal.ZERO);
                linea.setDescuento(java.math.BigDecimal.ZERO);
                linea.setIva(new java.math.BigDecimal("21"));
                linea.setAlbaran(albaranActual);
                albaranActual.getLineas().add(linea);
            }

            albaranService.save(albaranActual);

            mostrarExito();
            cerrarVentana();

        } catch (Exception e) {
            log.error("Error guardando albaran", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onConvertirFactura() {
        mostrarInfo();
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    // Métodos puente para compatibilidad con FXML que use otros nombres
    @FXML
    public void btnConvertirFactura() {
        onConvertirFactura();
    }

    @FXML
    public void btnCancelarAlbaran() {
        onCancelar();
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

    private void mostrarError(String mensaje) { DialogUtils.showError(mensaje); }
    private void mostrarAdvertencia(String mensaje) { DialogUtils.showWarning(mensaje); }
    private void mostrarExito() { DialogUtils.showSuccess("Albaran guardado correctamente"); }
    private void mostrarInfo() { DialogUtils.showInfo("Funcionalidad de conversion a factura en desarrollo"); }

    /**
     * Clase temporal para las lineas de albaran en la tabla
     */
    @Getter
    @Setter
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
    }
}

