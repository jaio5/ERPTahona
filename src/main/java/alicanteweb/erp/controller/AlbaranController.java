package alicanteweb.erp.controller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import alicanteweb.erp.controller.dto.AlbaranLineaDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class AlbaranController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranController.class);

    @FXML private TableView<AlbaranVenta> tableAlbaranes;
    @FXML private TableColumn<AlbaranVenta, Long> colId;
    @FXML private TableColumn<AlbaranVenta, String> colNumero;
    @FXML private TableColumn<AlbaranVenta, String> colFecha;
    @FXML private TableColumn<AlbaranVenta, String> colCliente;
    @FXML private TableColumn<AlbaranVenta, String> colAlmacen;
    @FXML private TableColumn<AlbaranVenta, String> colTotal;
    @FXML private TableColumn<AlbaranVenta, String> colObservaciones;
    @FXML private TextField txtBuscar;

    private final AlbaranVentaService albaranService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final AlmacenService almacenService;
    private final alicanteweb.erp.service.PrintService printService;
    private final alicanteweb.erp.service.AlbaranVentaLineaService albaranLineaService;
    private final ObservableList<AlbaranVenta> albaranesList = FXCollections.observableArrayList();

    public AlbaranController(AlbaranVentaService albaranService, ClienteService clienteService, 
                            ArticuloService articuloService, AlmacenService almacenService,
                            alicanteweb.erp.service.PrintService printService,
                            alicanteweb.erp.service.AlbaranVentaLineaService albaranLineaService) {
        this.albaranService = albaranService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.printService = printService;
        this.albaranLineaService = albaranLineaService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colNumero != null) colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).orElse("")));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().toString()));
        if (colCliente != null) colCliente.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() == null || cell.getValue().getCliente() == null ? "" : Optional.ofNullable(cell.getValue().getCliente().getNombre()).orElse("")
        ));
        if (colAlmacen != null) colAlmacen.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() == null || cell.getValue().getAlmacen() == null ? "" : Optional.ofNullable(cell.getValue().getAlmacen().getNombre()).orElse("")
        ));
        if (colTotal != null) colTotal.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getTotal() == null ? "" : String.format("%.2f €", cell.getValue().getTotal())));
        if (colObservaciones != null) colObservaciones.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getObservaciones()).orElse("")));

        if (tableAlbaranes != null) tableAlbaranes.setItems(albaranesList);
        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlbaranes(newV));
        }
    }

    private void loadAll() {
        try {
            log.info("Cargando albaranes...");
            List<AlbaranVenta> todos = albaranService.findAll();
            albaranesList.setAll(todos);
            log.info("Se cargaron {} albaranes", todos.size());
        } catch (Exception e) {
            log.error("Error cargando albaranes", e);
            mostrarError("Error cargando albaranes: " + e.getMessage());
        }
    }

    private void filtrarAlbaranes(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<AlbaranVenta> todos = albaranService.findAll();
            List<AlbaranVenta> encontrados = todos.stream()
                .filter(a -> a.getNumero() != null && a.getNumero().toLowerCase().contains(filtro.toLowerCase()))
                .toList();
            albaranesList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error filtrando albaranes", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarFormularioAlbaran(null);
    }

    @FXML
    public void onEdit() {
        AlbaranVenta seleccionado = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un albarán para ver detalles");
            return;
        }
        mostrarFormularioAlbaran(seleccionado);
    }

    @FXML
    public void onPrint() {
        AlbaranVenta seleccionado = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un albarán para imprimir");
            return;
        }

        // Mostrar diálogo para seleccionar diseño
        ChoiceDialog<alicanteweb.erp.service.PrintService.PrintDesign> dialog = new ChoiceDialog<>(
            alicanteweb.erp.service.PrintService.PrintDesign.CLASICO,
            alicanteweb.erp.service.PrintService.PrintDesign.values()
        );
        dialog.setTitle("Seleccionar Diseño de Impresión");
        dialog.setHeaderText("Elige el diseño para imprimir el albarán");
        dialog.setContentText("Diseño:");

        // Configurar converter para mostrar nombres legibles
        ComboBox<alicanteweb.erp.service.PrintService.PrintDesign> comboBox =
            (ComboBox<alicanteweb.erp.service.PrintService.PrintDesign>) dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(alicanteweb.erp.service.PrintService.PrintDesign design) {
                    return design == null ? "" : design.getNombre() + " - " + design.getDescripcion();
                }
                @Override
                public alicanteweb.erp.service.PrintService.PrintDesign fromString(String string) { return null; }
            });
        }

        dialog.showAndWait().ifPresent(design -> {
            try {
                // Cargar líneas reales del albarán desde la base de datos
                List<AlbaranVentaLinea> lineas = albaranLineaService.findByAlbaranId(seleccionado.getId());

                if (lineas.isEmpty()) {
                    mostrarError("Este albarán no tiene líneas para imprimir");
                    return;
                }

                // Generar HTML de impresión
                java.io.File htmlFile = printService.generarImpresionAlbaran(seleccionado, lineas, design);

                // Abrir en navegador predeterminado
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(htmlFile.toURI());
                    mostrarInfo("Documento de impresión generado. Se abrirá en tu navegador.");
                } else {
                    mostrarInfo("Archivo generado en: " + htmlFile.getAbsolutePath());
                }

            } catch (Exception e) {
                log.error("Error generando impresión", e);
                mostrarError("Error al generar documento de impresión: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onDelete() {
        AlbaranVenta seleccionado = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un albarán para eliminar");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar el albarán " + seleccionado.getNumero() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    albaranService.deleteById(seleccionado.getId());
                    loadAll();
                    mostrarInfo("Albarán eliminado correctamente");
                } catch (Exception e) {
                    log.error("Error eliminando albarán", e);
                    mostrarError("Error al eliminar: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void mostrarFormularioAlbaran(AlbaranVenta albaran) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/albaran_form.fxml"));
            VBox formRoot = loader.load();
            
            // Obtener controles
            TextField txtNumero = (TextField) formRoot.lookup("#txtNumero");
            DatePicker dpFecha = (DatePicker) formRoot.lookup("#dpFecha");
            ComboBox<Cliente> cbCliente = (ComboBox<Cliente>) formRoot.lookup("#cbCliente");
            ComboBox<Almacen> cbAlmacen = (ComboBox<Almacen>) formRoot.lookup("#cbAlmacen");
            TextArea txtObservaciones = (TextArea) formRoot.lookup("#txtObservaciones");
            Button btnAgregarLinea = (Button) formRoot.lookup("#btnAgregarLinea");
            Button btnEliminarLinea = (Button) formRoot.lookup("#btnEliminarLinea");
            TableView<AlbaranLineaDTO> tableLineas = (TableView<AlbaranLineaDTO>) formRoot.lookup("#tableLineas");
            Label lblBaseImponible = (Label) formRoot.lookup("#lblBaseImponible");
            Label lblIva = (Label) formRoot.lookup("#lblIva");
            Label lblTotal = (Label) formRoot.lookup("#lblTotal");
            Button btnGuardar = (Button) formRoot.lookup("#btnGuardar");
            Button btnCancelar = (Button) formRoot.lookup("#btnCancelar");
            
            // Configurar tabla de líneas
            ObservableList<AlbaranLineaDTO> lineas = FXCollections.observableArrayList();
            configurarTablaLineas(tableLineas, lineas, lblBaseImponible, lblIva, lblTotal);
            
            // Cargar clientes y almacenes
            List<Cliente> clientes = clienteService.findAll().stream()
                .filter(c -> c.getActivo() == null || c.getActivo())
                .toList();
            cbCliente.setItems(FXCollections.observableArrayList(clientes));
            cbCliente.setConverter(new StringConverter<>() {
                @Override
                public String toString(Cliente cliente) {
                    return cliente == null ? "" : cliente.getCodigo() + " - " + cliente.getNombre();
                }
                @Override
                public Cliente fromString(String string) { return null; }
            });
            
            List<Almacen> almacenes = almacenService.findAll();
            cbAlmacen.setItems(FXCollections.observableArrayList(almacenes));
            cbAlmacen.setConverter(new StringConverter<>() {
                @Override
                public String toString(Almacen almacen) {
                    return almacen == null ? "" : almacen.getCodigo() + " - " + almacen.getNombre();
                }
                @Override
                public Almacen fromString(String string) { return null; }
            });
            
            // Establecer valores
            boolean esNuevo = (albaran == null);
            if (esNuevo) {
                txtNumero.setText(generarNumeroAlbaran());
                dpFecha.setValue(LocalDate.now());
            }
            
            // Crear ventana modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(esNuevo ? "Nuevo Albarán" : "Editar Albarán");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(formRoot));
            
            // Botón agregar línea
            btnAgregarLinea.setOnAction(e -> agregarLineaAlbaran(tableLineas, lineas, lblBaseImponible, lblIva, lblTotal));
            
            // Botón eliminar línea
            btnEliminarLinea.setOnAction(e -> {
                AlbaranLineaDTO selected = tableLineas.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    lineas.remove(selected);
                    calcularTotales(lineas, lblBaseImponible, lblIva, lblTotal);
                }
            });
            
            // Botón guardar
            btnGuardar.setOnAction(e -> {
                if (validarAlbaran(txtNumero, cbCliente, cbAlmacen, lineas)) {
                    guardarAlbaran(txtNumero.getText(), dpFecha.getValue(), cbCliente.getValue(), 
                                 cbAlmacen.getValue(), txtObservaciones.getText(), lineas, stage);
                }
            });
            
            // Botón cancelar
            btnCancelar.setOnAction(e -> stage.close());
            
            stage.showAndWait();
            
        } catch (Exception e) {
            log.error("Error mostrando formulario de albarán", e);
            mostrarError("Error al abrir el formulario: " + e.getMessage());
        }
    }

    private void configurarTablaLineas(TableView<AlbaranLineaDTO> table, ObservableList<AlbaranLineaDTO> lineas,
                                      Label lblBase, Label lblIva, Label lblTotal) {
        table.setItems(lineas);
        table.setEditable(true);
        
        TableColumn<AlbaranLineaDTO, String> colArticulo = (TableColumn<AlbaranLineaDTO, String>) table.getColumns().get(0);
        TableColumn<AlbaranLineaDTO, String> colDescripcion = (TableColumn<AlbaranLineaDTO, String>) table.getColumns().get(1);
        TableColumn<AlbaranLineaDTO, BigDecimal> colCantidad = (TableColumn<AlbaranLineaDTO, BigDecimal>) table.getColumns().get(2);
        TableColumn<AlbaranLineaDTO, BigDecimal> colPrecio = (TableColumn<AlbaranLineaDTO, BigDecimal>) table.getColumns().get(3);
        TableColumn<AlbaranLineaDTO, BigDecimal> colDescuento = (TableColumn<AlbaranLineaDTO, BigDecimal>) table.getColumns().get(4);
        TableColumn<AlbaranLineaDTO, BigDecimal> colIva = (TableColumn<AlbaranLineaDTO, BigDecimal>) table.getColumns().get(5);
        TableColumn<AlbaranLineaDTO, BigDecimal> colSubtotal = (TableColumn<AlbaranLineaDTO, BigDecimal>) table.getColumns().get(6);
        TableColumn<AlbaranLineaDTO, BigDecimal> colTotalCol = (TableColumn<AlbaranLineaDTO, BigDecimal>) table.getColumns().get(7);
        
        colArticulo.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getArticulo() != null ? cell.getValue().getArticulo().getCodigo() : ""));
        colDescripcion.setCellValueFactory(cell -> cell.getValue().descripcionProperty());
        colCantidad.setCellValueFactory(cell -> cell.getValue().cantidadProperty());
        colPrecio.setCellValueFactory(cell -> cell.getValue().precioProperty());
        colDescuento.setCellValueFactory(cell -> cell.getValue().descuentoProperty());
        colIva.setCellValueFactory(cell -> cell.getValue().ivaProperty());
        colSubtotal.setCellValueFactory(cell -> cell.getValue().subtotalProperty());
        colTotalCol.setCellValueFactory(cell -> cell.getValue().totalProperty());
        
        // Hacer editables
        colCantidad.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colCantidad.setOnEditCommit(e -> {
            e.getRowValue().setCantidad(e.getNewValue());
            calcularTotales(lineas, lblBase, lblIva, lblTotal);
        });
        
        colPrecio.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colPrecio.setOnEditCommit(e -> {
            e.getRowValue().setPrecio(e.getNewValue());
            calcularTotales(lineas, lblBase, lblIva, lblTotal);
        });
        
        colDescuento.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colDescuento.setOnEditCommit(e -> {
            e.getRowValue().setDescuento(e.getNewValue());
            calcularTotales(lineas, lblBase, lblIva, lblTotal);
        });
    }

    private void agregarLineaAlbaran(TableView<AlbaranLineaDTO> table, ObservableList<AlbaranLineaDTO> lineas,
                                    Label lblBase, Label lblIva, Label lblTotal) {
        List<Articulo> articulos = articuloService.findAll().stream()
            .filter(a -> a.getActivo() == null || a.getActivo())
            .toList();
        
        if (articulos.isEmpty()) {
            mostrarError("No hay artículos disponibles");
            return;
        }
        
        ChoiceDialog<Articulo> dialog = new ChoiceDialog<>(articulos.get(0), articulos);
        dialog.setTitle("Seleccionar Artículo");
        dialog.setHeaderText("Agregar línea al albarán");
        dialog.setContentText("Selecciona un artículo:");
        
        ComboBox<Articulo> comboBox = (ComboBox<Articulo>) dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Articulo articulo) {
                    if (articulo == null) return "";
                    return articulo.getCodigo() + " - " + articulo.getDescripcion() + 
                           " (" + (articulo.getPvp() != null ? String.format("%.2f €", articulo.getPvp()) : "0.00 €") + ")";
                }
                @Override
                public Articulo fromString(String string) { return null; }
            });
        }
        
        dialog.showAndWait().ifPresent(articulo -> {
            AlbaranLineaDTO linea = new AlbaranLineaDTO(articulo);
            lineas.add(linea);
            calcularTotales(lineas, lblBase, lblIva, lblTotal);
        });
    }

    private void calcularTotales(ObservableList<AlbaranLineaDTO> lineas, Label lblBase, Label lblIva, Label lblTotal) {
        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        
        for (AlbaranLineaDTO linea : lineas) {
            baseImponible = baseImponible.add(linea.getSubtotal() != null ? linea.getSubtotal() : BigDecimal.ZERO);
            BigDecimal importeIva = linea.getTotal() != null && linea.getSubtotal() != null 
                ? linea.getTotal().subtract(linea.getSubtotal()) 
                : BigDecimal.ZERO;
            totalIva = totalIva.add(importeIva);
        }
        
        BigDecimal total = baseImponible.add(totalIva);
        
        lblBase.setText(String.format("%.2f €", baseImponible));
        lblIva.setText(String.format("%.2f €", totalIva));
        lblTotal.setText(String.format("%.2f €", total));
    }

    private boolean validarAlbaran(TextField txtNumero, ComboBox<Cliente> cbCliente, 
                                  ComboBox<Almacen> cbAlmacen, ObservableList<AlbaranLineaDTO> lineas) {
        if (txtNumero.getText() == null || txtNumero.getText().trim().isEmpty()) {
            mostrarError("El número de albarán es obligatorio");
            return false;
        }
        if (cbCliente.getValue() == null) {
            mostrarError("Debes seleccionar un cliente");
            return false;
        }
        if (cbAlmacen.getValue() == null) {
            mostrarError("Debes seleccionar un almacén");
            return false;
        }
        if (lineas.isEmpty()) {
            mostrarError("Debes agregar al menos una línea al albarán");
            return false;
        }
        return true;
    }

    private void guardarAlbaran(String numero, LocalDate fecha, Cliente cliente, Almacen almacen,
                               String observaciones, ObservableList<AlbaranLineaDTO> lineasDTO, 
                               javafx.stage.Stage stage) {
        try {
            AlbaranVenta albaran = new AlbaranVenta();
            albaran.setNumero(numero);
            albaran.setFecha(fecha);
            albaran.setCliente(cliente);
            albaran.setAlmacen(almacen);
            albaran.setObservaciones(observaciones);
            
            BigDecimal total = lineasDTO.stream()
                .map(l -> l.getTotal() != null ? l.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            albaran.setTotal(total);
            
            // Guardar albarán primero
            AlbaranVenta albaranGuardado = albaranService.save(albaran);

            // Guardar líneas del albarán
            for (AlbaranLineaDTO dto : lineasDTO) {
                AlbaranVentaLinea linea = new AlbaranVentaLinea();
                linea.setAlbaran(albaranGuardado);
                linea.setArticulo(dto.getArticulo());
                linea.setCantidad(dto.getCantidad());
                linea.setPrecio(dto.getPrecio());
                linea.setDescuento(dto.getDescuento());
                linea.setIva(dto.getIva());

                albaranLineaService.save(linea);
            }

            loadAll();
            mostrarInfo("Albarán creado correctamente: " + numero);
            stage.close();
            
        } catch (Exception e) {
            log.error("Error guardando albarán", e);
            mostrarError("Error al guardar el albarán: " + e.getMessage());
        }
    }

    private String generarNumeroAlbaran() {
        try {
            List<AlbaranVenta> todos = albaranService.findAll();
            if (todos.isEmpty()) {
                return "ALB001";
            }
            
            int maxNumero = 0;
            for (AlbaranVenta a : todos) {
                String numero = a.getNumero();
                if (numero != null && numero.startsWith("ALB")) {
                    try {
                        String numeroStr = numero.substring(3);
                        int num = Integer.parseInt(numeroStr);
                        if (num > maxNumero) {
                            maxNumero = num;
                        }
                    } catch (Exception e) {
                        // Ignorar
                    }
                }
            }
            
            return String.format("ALB%03d", maxNumero + 1);
        } catch (Exception e) {
            log.error("Error generando número de albarán", e);
            return "ALB001";
        }
    }

    private static class BigDecimalStringConverter extends StringConverter<BigDecimal> {
        @Override
        public String toString(BigDecimal value) {
            return value == null ? "0" : value.toString();
        }
        
        @Override
        public BigDecimal fromString(String string) {
            try {
                return new BigDecimal(string);
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        }
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.setHeaderText("Información");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}

