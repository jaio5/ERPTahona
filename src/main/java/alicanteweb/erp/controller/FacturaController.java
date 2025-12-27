package alicanteweb.erp.controller;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import alicanteweb.erp.controller.dto.FacturaLineaDTO;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class FacturaController {
    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    @FXML private TableView<Factura> tableFacturas;
    @FXML private TableColumn<Factura, Long> colId;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colTotal;
    @FXML private TableColumn<Factura, String> colPagado;
    @FXML private TableColumn<Factura, String> colEstado;
    @FXML private TextField txtBuscar;

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final alicanteweb.erp.service.PrintService printService;
    private final VerifactuService verifactuService;
    private final ObservableList<Factura> facturasList = FXCollections.observableArrayList();

    public FacturaController(FacturaService facturaService, ClienteService clienteService,
                            ArticuloService articuloService,
                            alicanteweb.erp.service.PrintService printService,
                            VerifactuService verifactuService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.printService = printService;
        this.verifactuService = verifactuService;
    }

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colNumero != null) colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getNumero()).orElse("")));
        if (colFecha != null) colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().toString()));
        if (colCliente != null) colCliente.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() == null || cell.getValue().getCliente() == null ? "" : Optional.ofNullable(cell.getValue().getCliente().getNombre()).orElse("")
        ));
        if (colTotal != null) colTotal.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getTotal() == null ? "" : cell.getValue().getTotal().toString()));
        if (colPagado != null) colPagado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getPagado() == null ? "" : cell.getValue().getPagado().toString()));
        // Actualizado: mostrar el estado real de la factura (BORRADOR, REVISION, EMITIDA, ANULADA)
        if (colEstado != null) colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() == null ? "" : (cell.getValue().getEstado() != null ? cell.getValue().getEstado() : "BORRADOR")
        ));

        if (tableFacturas != null) tableFacturas.setItems(facturasList);
        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarFacturas(newV));
        }
    }

    private void loadAll() {
        try {
            List<Factura> todas = facturaService.findAll();
            facturasList.setAll(todas);
        } catch (Exception e) {
            log.error("Error cargando facturas", e);
            mostrarError("Error cargando facturas: " + e.getMessage());
        }
    }

    private void filtrarFacturas(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Factura> todas = facturaService.findAll();
            List<Factura> encontradas = todas.stream()
                .filter(f -> f.getNumero() != null && f.getNumero().toLowerCase().contains(filtro.toLowerCase()))
                .toList();
            facturasList.setAll(encontradas);
        } catch (Exception e) {
            log.error("Error filtrando facturas", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarFormularioFactura(null);
    }

    private void mostrarFormularioFactura(Factura factura) {
        try {
            // Cargar FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/factura_form.fxml"));
            VBox formRoot = loader.load();

            // Obtener controles
            TextField txtNumero = (TextField) formRoot.lookup("#txtNumero");
            DatePicker dpFecha = (DatePicker) formRoot.lookup("#dpFecha");
            ComboBox<Cliente> cbCliente = (ComboBox<Cliente>) formRoot.lookup("#cbCliente");
            Button btnBuscarCliente = (Button) formRoot.lookup("#btnBuscarCliente");
            Button btnAgregarLinea = (Button) formRoot.lookup("#btnAgregarLinea");
            Button btnEliminarLinea = (Button) formRoot.lookup("#btnEliminarLinea");
            TableView<FacturaLineaDTO> tableLineas = (TableView<FacturaLineaDTO>) formRoot.lookup("#tableLineas");
            Label lblBaseImponible = (Label) formRoot.lookup("#lblBaseImponible");
            Label lblIva = (Label) formRoot.lookup("#lblIva");
            Label lblTotal = (Label) formRoot.lookup("#lblTotal");
            Button btnGuardar = (Button) formRoot.lookup("#btnGuardar");
            Button btnCancelar = (Button) formRoot.lookup("#btnCancelar");

            // Configurar tabla de líneas
            ObservableList<FacturaLineaDTO> lineas = FXCollections.observableArrayList();
            configurarTablaLineas(tableLineas, lineas, lblBaseImponible, lblIva, lblTotal);

            // Cargar clientes
            List<Cliente> clientes = clienteService.findAll().stream()
                .filter(c -> c.getActivo() == null || c.getActivo())
                .toList();
            cbCliente.setItems(FXCollections.observableArrayList(clientes));
            cbCliente.setConverter(new StringConverter<Cliente>() {
                @Override
                public String toString(Cliente cliente) {
                    return cliente == null ? "" : cliente.getCodigo() + " - " + cliente.getNombre();
                }
                @Override
                public Cliente fromString(String string) { return null; }
            });

            // Establecer valores por defecto
            boolean esNueva = (factura == null);
            if (esNueva) {
                txtNumero.setText(generarNumeroFactura());
                dpFecha.setValue(LocalDate.now());
            }

            // Crear ventana modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(esNueva ? "Nueva Factura" : "Editar Factura");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(formRoot));

            // Botón agregar línea
            btnAgregarLinea.setOnAction(e -> agregarLineaFactura(tableLineas, lineas, lblBaseImponible, lblIva, lblTotal));

            // Botón eliminar línea
            btnEliminarLinea.setOnAction(e -> {
                FacturaLineaDTO selected = tableLineas.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    lineas.remove(selected);
                    calcularTotales(lineas, lblBaseImponible, lblIva, lblTotal);
                }
            });

            // Botón guardar
            btnGuardar.setOnAction(e -> {
                if (validarFactura(txtNumero, cbCliente, lineas)) {
                    guardarFactura(txtNumero.getText(), dpFecha.getValue(), cbCliente.getValue(), lineas, stage);
                }
            });

            // Botón cancelar
            btnCancelar.setOnAction(e -> stage.close());

            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error mostrando formulario de factura", e);
            mostrarError("Error al abrir el formulario: " + e.getMessage());
        }
    }

    private void configurarTablaLineas(TableView<FacturaLineaDTO> table, ObservableList<FacturaLineaDTO> lineas,
                                      Label lblBase, Label lblIva, Label lblTotal) {
        table.setItems(lineas);
        table.setEditable(true);

        TableColumn<FacturaLineaDTO, String> colArticulo = (TableColumn<FacturaLineaDTO, String>) table.getColumns().get(0);
        TableColumn<FacturaLineaDTO, String> colDescripcion = (TableColumn<FacturaLineaDTO, String>) table.getColumns().get(1);
        TableColumn<FacturaLineaDTO, BigDecimal> colCantidad = (TableColumn<FacturaLineaDTO, BigDecimal>) table.getColumns().get(2);
        TableColumn<FacturaLineaDTO, BigDecimal> colPrecio = (TableColumn<FacturaLineaDTO, BigDecimal>) table.getColumns().get(3);
        TableColumn<FacturaLineaDTO, BigDecimal> colIva = (TableColumn<FacturaLineaDTO, BigDecimal>) table.getColumns().get(4);
        TableColumn<FacturaLineaDTO, BigDecimal> colSubtotal = (TableColumn<FacturaLineaDTO, BigDecimal>) table.getColumns().get(5);
        TableColumn<FacturaLineaDTO, BigDecimal> colTotal = (TableColumn<FacturaLineaDTO, BigDecimal>) table.getColumns().get(6);

        colArticulo.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getArticulo() != null ? cell.getValue().getArticulo().getCodigo() : ""));
        colDescripcion.setCellValueFactory(cell -> cell.getValue().descripcionProperty());
        colCantidad.setCellValueFactory(cell -> cell.getValue().cantidadProperty());
        colPrecio.setCellValueFactory(cell -> cell.getValue().precioProperty());
        colIva.setCellValueFactory(cell -> cell.getValue().ivaProperty());
        colSubtotal.setCellValueFactory(cell -> cell.getValue().subtotalProperty());
        colTotal.setCellValueFactory(cell -> cell.getValue().totalProperty());

        // Hacer editable cantidad y precio
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
    }

    private void agregarLineaFactura(TableView<FacturaLineaDTO> table, ObservableList<FacturaLineaDTO> lineas,
                                     Label lblBase, Label lblIva, Label lblTotal) {
        // Mostrar diálogo para seleccionar artículo
        List<Articulo> articulos = articuloService.findAll().stream()
            .filter(a -> a.getActivo() == null || a.getActivo())
            .toList();

        if (articulos.isEmpty()) {
            mostrarError("No hay artículos disponibles. Crea artículos primero.");
            return;
        }

        ChoiceDialog<Articulo> dialog = new ChoiceDialog<>(articulos.get(0), articulos);
        dialog.setTitle("Seleccionar Artículo");
        dialog.setHeaderText("Agregar línea a la factura");
        dialog.setContentText("Selecciona un artículo:");

        // Configurar el converter para mostrar código y descripción
        ComboBox<Articulo> comboBox = (ComboBox<Articulo>) dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setConverter(new StringConverter<Articulo>() {
                @Override
                public String toString(Articulo articulo) {
                    if (articulo == null) return "";
                    return articulo.getCodigo() + " - " + articulo.getDescripcion() +
                           " (" + (articulo.getPvp() != null ? String.format("%.2f â‚¬", articulo.getPvp()) : "0.00 â‚¬") + ")";
                }
                @Override
                public Articulo fromString(String string) { return null; }
            });
        }

        dialog.showAndWait().ifPresent(articulo -> {
            FacturaLineaDTO linea = new FacturaLineaDTO(articulo);
            lineas.add(linea);
            calcularTotales(lineas, lblBase, lblIva, lblTotal);
        });
    }

    private void calcularTotales(ObservableList<FacturaLineaDTO> lineas, Label lblBase, Label lblIva, Label lblTotal) {
        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        for (FacturaLineaDTO linea : lineas) {
            baseImponible = baseImponible.add(linea.getSubtotal() != null ? linea.getSubtotal() : BigDecimal.ZERO);
            BigDecimal importeIva = linea.getTotal() != null && linea.getSubtotal() != null
                ? linea.getTotal().subtract(linea.getSubtotal())
                : BigDecimal.ZERO;
            totalIva = totalIva.add(importeIva);
        }

        BigDecimal total = baseImponible.add(totalIva);

        lblBase.setText(String.format("%.2f â‚¬", baseImponible));
        lblIva.setText(String.format("%.2f â‚¬", totalIva));
        lblTotal.setText(String.format("%.2f â‚¬", total));
    }

    private boolean validarFactura(TextField txtNumero, ComboBox<Cliente> cbCliente, ObservableList<FacturaLineaDTO> lineas) {
        if (txtNumero.getText() == null || txtNumero.getText().trim().isEmpty()) {
            mostrarError("El número de factura es obligatorio");
            return false;
        }
        if (cbCliente.getValue() == null) {
            mostrarError("Debes seleccionar un cliente");
            return false;
        }
        if (lineas.isEmpty()) {
            mostrarError("Debes agregar al menos una línea a la factura");
            return false;
        }
        return true;
    }

    private void guardarFactura(String numero, LocalDate fecha, Cliente cliente,
                                ObservableList<FacturaLineaDTO> lineasDTO, javafx.stage.Stage stage) {
        try {
            // Crear factura
            Factura factura = new Factura();
            factura.setNumero(numero);
            factura.setFecha(fecha);
            factura.setCliente(cliente);
            factura.setPagada(false);
            factura.setPagado(BigDecimal.ZERO);

            // Calcular total
            BigDecimal total = lineasDTO.stream()
                .map(l -> l.getTotal() != null ? l.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            factura.setTotal(total);

            // Guardar factura
            Factura facturaSaved = facturaService.save(factura);

            // Crear y guardar líneas
            for (FacturaLineaDTO dto : lineasDTO) {
                FacturaLinea linea = new FacturaLinea();
                linea.setFactura(facturaSaved);
                linea.setArticulo(dto.getArticulo());
                linea.setCantidad(dto.getCantidad());
                linea.setPrecio(dto.getPrecio());
                linea.setIva(dto.getIva());
                // Las líneas se guardan en cascada o se necesita un servicio específico
            }

            loadAll();
            mostrarInfo("Factura creada correctamente: " + numero);
            stage.close();

        } catch (Exception e) {
            log.error("Error guardando factura", e);
            mostrarError("Error al guardar la factura: " + e.getMessage());
        }
    }

    private String generarNumeroFactura() {
        try {
            List<Factura> todas = facturaService.findAll();
            if (todas.isEmpty()) {
                return "FAC001";
            }

            int maxNumero = 0;
            for (Factura f : todas) {
                String numero = f.getNumero();
                if (numero != null && numero.startsWith("FAC")) {
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

            return String.format("FAC%03d", maxNumero + 1);
        } catch (Exception e) {
            log.error("Error generando número de factura", e);
            return "FAC001";
        }
    }

    // Clase auxiliar para convertir BigDecimal a String
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

    @FXML
    public void onEdit() {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una factura para ver detalles");
            return;
        }
        mostrarInfo("Funcionalidad de ver detalles no implementada");
    }

    @FXML
    public void onPrint() {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una factura para imprimir");
            return;
        }

        // Mostrar diálogo para seleccionar diseño
        ChoiceDialog<alicanteweb.erp.service.PrintService.PrintDesign> dialog = new ChoiceDialog<>(
            alicanteweb.erp.service.PrintService.PrintDesign.CLASICO,
            alicanteweb.erp.service.PrintService.PrintDesign.values()
        );
        dialog.setTitle("Seleccionar Diseño de Impresión");
        dialog.setHeaderText("Elige el diseño para imprimir la factura");
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
                // Obtener líneas de la factura (simulación - en producción usar servicio)
                List<FacturaLinea> lineas = new java.util.ArrayList<>();
                // TODO: Cargar líneas reales de la base de datos

                // Generar HTML de impresión
                java.io.File htmlFile = printService.generarImpresionFactura(seleccionada, lineas, design);

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
    public void onRefresh() {
        loadAll();
    }

    /**
     * Envía una factura a estado de REVISION
     */
    @FXML
    public void onEnviarARevision() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura para enviar a revisión");
            return;
        }

        if (!"BORRADOR".equals(factura.getEstado())) {
            mostrarError("Solo las facturas en estado BORRADOR pueden enviarse a revisión.\nEstado actual: " + factura.getEstado());
            return;
        }

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Envío a Revisión");
        confirmacion.setHeaderText("Â¿Enviar factura " + factura.getNumero() + " a revisión?");
        confirmacion.setContentText("La factura quedará pendiente de aprobación antes de emitirse a la AEAT.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    factura.setEstado("REVISION");
                    factura.setObservacionesRevision("Enviada a revisión el " + LocalDate.now());
                    facturaService.save(factura);
                    loadAll();
                    mostrarInfo("Factura " + factura.getNumero() + " enviada a revisión correctamente");
                    log.info("Factura {} cambiada a estado REVISION", factura.getNumero());
                } catch (Exception e) {
                    log.error("Error enviando factura a revisión", e);
                    mostrarError("Error al enviar a revisión: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Aprueba y emite una factura a Verifactu/AEAT
     */
    @FXML
    public void onAprobarYEmitir() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura para aprobar y emitir");
            return;
        }

        if (!"REVISION".equals(factura.getEstado())) {
            mostrarError("Solo las facturas en estado REVISION pueden ser emitidas.\nEstado actual: " + factura.getEstado());
            return;
        }

        // Confirmar acción con advertencia
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("Confirmar Emisión a AEAT");
        confirmacion.setHeaderText("Â¿Aprobar y emitir factura " + factura.getNumero() + " a Verifactu/AEAT?");
        confirmacion.setContentText("Esta acción enviará la factura a la Agencia Tributaria con los datos de GRUPO BABO.\n" +
                                   "Una vez emitida, NO se podrá modificar.\n\n" +
                                   "Â¿Deseas continuar?");
        confirmacion.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    log.info("Iniciando emisión de factura {} a Verifactu", factura.getNumero());

                    // Enviar a Verifactu
                    verifactuService.enviarFacturaVerifactu(factura);

                    // Actualizar estado de factura
                    factura.setEstado("EMITIDA");
                    factura.setVerifactuEnviada(true);
                    factura.setFechaEmisionVerifactu(java.time.LocalDateTime.now());
                    factura.setObservacionesRevision((factura.getObservacionesRevision() != null ? factura.getObservacionesRevision() + "\n" : "") +
                                                    "Emitida a Verifactu el " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    facturaService.save(factura);

                    loadAll();

                    // Mostrar mensaje de éxito con detalles
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("âœ… Factura Emitida Correctamente");
                    success.setHeaderText("Factura " + factura.getNumero() + " emitida a AEAT");
                    success.setContentText("La factura ha sido enviada correctamente a Verifactu con los datos de:\n\n" +
                                         "GRUPO BABO, S.Coop.V.L.\n" +
                                         "CIF: F54059985\n\n" +
                                         "Fecha de emisión: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    success.showAndWait();

                    log.info("âœ… Factura {} emitida correctamente a Verifactu", factura.getNumero());

                } catch (Exception e) {
                    log.error("âŒ Error emitiendo factura a Verifactu", e);

                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error al Emitir Factura");
                    error.setHeaderText("No se pudo emitir la factura " + factura.getNumero());
                    error.setContentText("Error: " + e.getMessage() + "\n\n" +
                                       "La factura permanece en estado REVISION.\n" +
                                       "Revisa la configuración de Verifactu y vuelve a intentarlo.");
                    error.showAndWait();
                }
            }
        });
    }

    /**
     * Anula una factura emitida
     */
    @FXML
    public void onAnular() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura para anular");
            return;
        }

        if (!"EMITIDA".equals(factura.getEstado())) {
            mostrarError("Solo las facturas EMITIDAS pueden ser anuladas.\nEstado actual: " + factura.getEstado());
            return;
        }

        // Pedir motivo de anulación
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular Factura");
        dialog.setHeaderText("Anular factura " + factura.getNumero());
        dialog.setContentText("Motivo de anulación:");

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo != null && !motivo.trim().isEmpty()) {
                try {
                    factura.setEstado("ANULADA");
                    factura.setObservacionesRevision((factura.getObservacionesRevision() != null ? factura.getObservacionesRevision() + "\n" : "") +
                                                    "ANULADA el " + LocalDate.now() + ". Motivo: " + motivo);
                    facturaService.save(factura);
                    loadAll();
                    mostrarInfo("Factura " + factura.getNumero() + " anulada correctamente");
                    log.info("Factura {} anulada. Motivo: {}", factura.getNumero(), motivo);
                } catch (Exception e) {
                    log.error("Error anulando factura", e);
                    mostrarError("Error al anular factura: " + e.getMessage());
                }
            } else {
                mostrarError("Debes indicar el motivo de anulación");
            }
        });
    }

    /**
     * Vuelve una factura de REVISION a BORRADOR
     */
    @FXML
    public void onVolverABorrador() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura");
            return;
        }

        if (!"REVISION".equals(factura.getEstado())) {
            mostrarError("Solo las facturas en REVISION pueden volver a BORRADOR.\nEstado actual: " + factura.getEstado());
            return;
        }

        try {
            factura.setEstado("BORRADOR");
            factura.setObservacionesRevision((factura.getObservacionesRevision() != null ? factura.getObservacionesRevision() + "\n" : "") +
                                            "Devuelta a BORRADOR el " + LocalDate.now());
            facturaService.save(factura);
            loadAll();
            mostrarInfo("Factura " + factura.getNumero() + " devuelta a BORRADOR");
            log.info("Factura {} devuelta a estado BORRADOR", factura.getNumero());
        } catch (Exception e) {
            log.error("Error devolviendo factura a borrador", e);
            mostrarError("Error: " + e.getMessage());
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

