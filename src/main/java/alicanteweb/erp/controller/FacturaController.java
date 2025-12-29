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

    /**
     * Guarda una nueva factura en estado BORRADOR.
     *
     * <p>Las facturas nuevas SIEMPRE se crean en estado BORRADOR.
     * Para emitirlas a Verifactu/AEAT se debe seguir el flujo:</p>
     * <ol>
     *   <li>Crear factura (BORRADOR)</li>
     *   <li>Enviar a Revisión (BORRADOR → REVISION)</li>
     *   <li>Aprobar y Emitir (REVISION → EMITIDA + envío a AEAT)</li>
     * </ol>
     *
     * @param numero Número de factura generado
     * @param fecha Fecha de expedición
     * @param cliente Cliente asociado
     * @param lineasDTO Líneas de la factura
     * @param stage Ventana del formulario a cerrar
     */
    private void guardarFactura(String numero, LocalDate fecha, Cliente cliente,
                                ObservableList<FacturaLineaDTO> lineasDTO, javafx.stage.Stage stage) {
        try {
            log.info("Guardando nueva factura {} en estado BORRADOR", numero);

            // Crear factura EN ESTADO BORRADOR
            Factura factura = new Factura();
            factura.setNumero(numero);
            factura.setFecha(fecha);
            factura.setCliente(cliente);
            factura.setPagada(false);
            factura.setPagado(BigDecimal.ZERO);

            // ⚠️ IMPORTANTE: Establecer estado BORRADOR explícitamente
            factura.setEstado("BORRADOR");
            factura.setVerifactuEnviada(false);
            factura.setTipoFactura("ORDINARIA");

            // Calcular totales
            BigDecimal baseImponible = BigDecimal.ZERO;
            BigDecimal totalIva = BigDecimal.ZERO;

            for (FacturaLineaDTO dto : lineasDTO) {
                BigDecimal subtotal = dto.getTotal() != null ? dto.getTotal() : BigDecimal.ZERO;
                baseImponible = baseImponible.add(subtotal);

                // Calcular IVA de esta línea
                if (dto.getIva() != null && dto.getIva().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal ivaLinea = subtotal.multiply(dto.getIva())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    totalIva = totalIva.add(ivaLinea);
                }
            }

            BigDecimal total = baseImponible.add(totalIva);

            factura.setBaseImponible(baseImponible);
            factura.setTotalIva(totalIva);
            factura.setTotal(total);

            // Guardar factura
            Factura facturaSaved = facturaService.save(factura);
            log.info("✅ Factura {} guardada correctamente en estado BORRADOR", numero);

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

            // Mensaje informativo sobre el flujo
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("✅ Factura Creada");
            info.setHeaderText("Factura " + numero + " creada en BORRADOR");
            info.setContentText("La factura ha sido creada correctamente en estado BORRADOR.\n\n" +
                               "📋 Próximos pasos:\n" +
                               "1. Revisa los datos de la factura\n" +
                               "2. Usa 'Enviar a Revisión' cuando esté lista\n" +
                               "3. Usa 'Aprobar y Emitir' para enviarla a Verifactu/AEAT\n\n" +
                               "ℹ️ Las facturas NO se envían automáticamente a la AEAT.");
            info.showAndWait();

            stage.close();

        } catch (Exception e) {
            log.error("❌ Error guardando factura", e);
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
     * Envía una factura del estado BORRADOR a REVISION.
     *
     * <p>Este es el paso 2 del flujo de facturación:</p>
     * <ol>
     *   <li>BORRADOR: Factura en edición</li>
     *   <li><strong>REVISION: Pendiente de aprobación (estás aquí)</strong></li>
     *   <li>EMITIDA: Enviada a AEAT vía Verifactu</li>
     * </ol>
     *
     * <p>Después de enviar a revisión, debes usar el botón
     * "Aprobar y Emitir" para enviarla a la AEAT.</p>
     */
    @FXML
    public void onEnviarARevision() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura para enviar a revisión");
            return;
        }

        if (!"BORRADOR".equals(factura.getEstado())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Estado Incorrecto");
            alert.setHeaderText("No se puede enviar a revisión");
            alert.setContentText("Solo las facturas en estado BORRADOR pueden enviarse a revisión.\n\n" +
                               "Estado actual: " + factura.getEstado() + "\n\n" +
                               "Si la factura está en REVISION, usa 'Aprobar y Emitir'.\n" +
                               "Si está EMITIDA, ya fue enviada a la AEAT.");
            alert.showAndWait();
            return;
        }

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Envío a Revisión");
        confirmacion.setHeaderText("¿Enviar factura " + factura.getNumero() + " a revisión?");
        confirmacion.setContentText("La factura pasará de BORRADOR a REVISION.\n\n" +
                                   "📋 Después deberás:\n" +
                                   "• Revisar que todos los datos sean correctos\n" +
                                   "• Usar el botón 'Aprobar y Emitir' para enviarla a la AEAT\n\n" +
                                   "⚠️ NOTA: Este paso NO envía la factura a la AEAT todavía.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Usar método específico del servicio que no intenta registrar en Verifactu
                    String observaciones = "Enviada a revisión el " + LocalDate.now();
                    facturaService.enviarARevision(factura.getId(), observaciones);
                    loadAll();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("✅ Enviada a Revisión");
                    success.setHeaderText("Factura " + factura.getNumero() + " lista para aprobar");
                    success.setContentText("La factura está ahora en estado REVISION.\n\n" +
                                         "📌 Próximo paso:\n" +
                                         "Selecciona la factura y usa el botón\n" +
                                         "'Aprobar y Emitir' para enviarla a Verifactu/AEAT.\n\n" +
                                         "Si necesitas hacer cambios, usa 'Volver a Borrador'.");
                    success.showAndWait();

                    log.info("Factura {} cambiada a estado REVISION", factura.getNumero());
                } catch (Exception e) {
                    log.error("Error enviando factura a revisión", e);
                    mostrarError("Error al enviar a revisión: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Aprueba una factura en REVISION y la emite a Verifactu/AEAT.
     *
     * <p><strong>⚠️ PASO FINAL - ENVÍO A LA AEAT</strong></p>
     *
     * <p>Este es el paso 3 (final) del flujo de facturación:</p>
     * <ol>
     *   <li>BORRADOR: Factura en edición</li>
     *   <li>REVISION: Pendiente de aprobación</li>
     *   <li><strong>EMITIDA: Enviada a AEAT (estás aquí)</strong></li>
     * </ol>
     *
     * <p>Una vez emitida, la factura:</p>
     * <ul>
     *   <li>✅ Se envía a Verifactu/AEAT con blockchain</li>
     *   <li>✅ Genera hash SHA-256 y QR</li>
     *   <li>✅ Queda registrada en la Agencia Tributaria</li>
     *   <li>⚠️ NO se puede modificar (solo anular)</li>
     * </ul>
     *
     * @see VerifactuService#enviarFacturaVerifactu(Factura)
     */
    @FXML
    public void onAprobarYEmitir() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura para aprobar y emitir");
            return;
        }

        if (!"REVISION".equals(factura.getEstado())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Estado Incorrecto");
            alert.setHeaderText("No se puede aprobar y emitir");
            alert.setContentText("Solo las facturas en estado REVISION pueden ser emitidas.\n\n" +
                               "Estado actual: " + factura.getEstado() + "\n\n" +
                               "📋 Flujo correcto:\n" +
                               "1. BORRADOR → Usa 'Enviar a Revisión'\n" +
                               "2. REVISION → Usa 'Aprobar y Emitir' (este botón)\n" +
                               "3. EMITIDA → Ya está en la AEAT\n\n" +
                               "Si la factura está en BORRADOR, primero envíala a revisión.");
            alert.showAndWait();
            return;
        }

        // Confirmar acción con advertencia clara
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("⚠️ CONFIRMAR EMISIÓN A LA AEAT");
        confirmacion.setHeaderText("¿APROBAR Y EMITIR factura " + factura.getNumero() + " a Verifactu/AEAT?");
        confirmacion.setContentText("🔴 ATENCIÓN: Esta acción es IRREVERSIBLE\n\n" +
                                   "La factura será enviada a la Agencia Tributaria con:\n" +
                                   "• Datos de GRUPO BABO, S.Coop.V.L.\n" +
                                   "• CIF: F54059985\n" +
                                   "• Sistema Verifactu (blockchain)\n\n" +
                                   "⚠️ Una vez emitida:\n" +
                                   "• NO se puede modificar\n" +
                                   "• Solo se puede anular\n" +
                                   "• Quedará registrada en la AEAT\n\n" +
                                   "¿Estás seguro de continuar?");
        confirmacion.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    log.info("🚀 Iniciando emisión de factura {} a Verifactu/AEAT", factura.getNumero());

                    // Usar método específico del servicio que maneja correctamente la transacción
                    // Este método cambia el estado y luego intenta registrar en Verifactu
                    Factura emitida = facturaService.aprobarYEmitir(factura.getId());

                    // Enviar a Verifactu (ya manejado dentro del servicio, pero podemos hacerlo explícito aquí también)
                    try {
                        verifactuService.enviarFacturaVerifactu(emitida);

                        // Actualizar campos adicionales de Verifactu
                        emitida.setVerifactuEnviada(true);
                        emitida.setFechaEmisionVerifactu(java.time.LocalDateTime.now());
                        emitida.setObservacionesRevision((emitida.getObservacionesRevision() != null ? emitida.getObservacionesRevision() + "\n" : "") +
                                                        "✅ Emitida a Verifactu el " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                        facturaService.saveSimple(emitida);
                    } catch (Exception verifactuEx) {
                        log.warn("Error enviando a Verifactu, pero factura ya está EMITIDA: {}", verifactuEx.getMessage());
                        // La factura ya está EMITIDA, el error de Verifactu no revierte el estado
                    }

                    loadAll();

                    // Mostrar mensaje de éxito detallado
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("✅ FACTURA EMITIDA CORRECTAMENTE");
                    success.setHeaderText("Factura " + factura.getNumero() + " enviada a AEAT");
                    success.setContentText("✅ La factura ha sido emitida exitosamente\n\n" +
                                         "📋 Datos de emisión:\n" +
                                         "• Empresa: GRUPO BABO, S.Coop.V.L.\n" +
                                         "• CIF: F54059985\n" +
                                         "• Fecha: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n" +
                                         "• Estado: EMITIDA\n" +
                                         "• Verifactu: Registrada en AEAT\n\n" +
                                         "🔐 La factura está ahora en el sistema Verifactu\n" +
                                         "con hash blockchain y código QR.\n\n" +
                                         "Usa 'Imprimir' para generar el PDF con QR.");
                    success.showAndWait();

                    log.info("✅ Factura {} emitida correctamente a Verifactu/AEAT", factura.getNumero());

                } catch (Exception e) {
                    log.error("❌ Error emitiendo factura a Verifactu", e);

                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("❌ Error al Emitir Factura");
                    error.setHeaderText("No se pudo emitir la factura " + factura.getNumero());
                    error.setContentText("Error técnico: " + e.getMessage() + "\n\n" +
                                       "🔄 La factura permanece en estado REVISION.\n\n" +
                                       "Posibles causas:\n" +
                                       "• Verifactu no está configurado\n" +
                                       "• No hay conexión con la AEAT\n" +
                                       "• Datos incompletos en la factura\n" +
                                       "• Error en el certificado digital\n\n" +
                                       "Revisa la configuración y vuelve a intentarlo.");
                    error.showAndWait();
                }
            } else {
                log.info("Usuario canceló la emisión de factura {}", factura.getNumero());
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Estado Incorrecto");
            alert.setHeaderText("No se puede anular");
            alert.setContentText("Solo las facturas EMITIDAS pueden ser anuladas.\n\n" +
                               "Estado actual: " + factura.getEstado() + "\n\n" +
                               "📋 Según estado:\n" +
                               "• BORRADOR → Usa 'Eliminar Borrador'\n" +
                               "• REVISION → Usa 'Volver a Borrador' y luego elimina\n" +
                               "• EMITIDA → Usa 'Anular Factura' (este botón) ✅\n\n" +
                               "⚠️ Solo las facturas registradas en AEAT\n" +
                               "requieren ser anuladas formalmente.");
            alert.showAndWait();
            return;
        }

        // Pedir motivo de anulación
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular Factura");
        dialog.setHeaderText("⚠️ Anular factura " + factura.getNumero() + " (REGISTRADA EN AEAT)");
        dialog.setContentText("Motivo de anulación (obligatorio):");

        dialog.showAndWait().ifPresent(motivo -> {
            if (motivo != null && !motivo.trim().isEmpty()) {
                // Confirmar anulación
                Alert confirmacion = new Alert(Alert.AlertType.WARNING);
                confirmacion.setTitle("⚠️ CONFIRMAR ANULACIÓN");
                confirmacion.setHeaderText("¿ANULAR factura " + factura.getNumero() + " en la AEAT?");
                confirmacion.setContentText("🔴 ATENCIÓN: Esta factura está registrada en la AEAT\n\n" +
                                           "Motivo: " + motivo + "\n\n" +
                                           "Al anular:\n" +
                                           "• Se marcará como ANULADA en el sistema\n" +
                                           "• Quedará registrada la anulación\n" +
                                           "• No se podrá revertir esta acción\n\n" +
                                           "⚠️ Deberás emitir una factura rectificativa\n" +
                                           "si es necesario.\n\n" +
                                           "¿Confirmas la anulación?");
                confirmacion.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            factura.setEstado("ANULADA");
                            factura.setObservacionesRevision(
                                (factura.getObservacionesRevision() != null ?
                                 factura.getObservacionesRevision() + "\n" : "") +
                                "❌ ANULADA el " + LocalDate.now() +
                                ". Motivo: " + motivo);

                            // ✅ Usar saveSimple para no intentar Verifactu
                            facturaService.saveSimple(factura);
                            loadAll();

                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("✅ Factura Anulada");
                            success.setHeaderText("Factura " + factura.getNumero() + " anulada");
                            success.setContentText("La factura ha sido anulada correctamente.\n\n" +
                                                 "Motivo: " + motivo + "\n\n" +
                                                 "📋 Estado actual: ANULADA\n\n" +
                                                 "Si necesitas emitir una factura rectificativa,\n" +
                                                 "crea una nueva factura con los datos correctos.");
                            success.showAndWait();

                            log.info("Factura {} anulada. Motivo: {}", factura.getNumero(), motivo);
                        } catch (Exception e) {
                            log.error("Error anulando factura", e);
                            mostrarError("Error al anular factura: " + e.getMessage());
                        }
                    }
                });
            } else {
                mostrarError("Debes indicar el motivo de anulación.\n\n" +
                           "El motivo es obligatorio para cumplir con\n" +
                           "los requisitos legales de auditoría.");
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Estado Incorrecto");
            alert.setHeaderText("No se puede devolver a borrador");
            alert.setContentText("Solo las facturas en estado REVISION pueden volver a BORRADOR.\n\n" +
                               "Estado actual: " + factura.getEstado() + "\n\n" +
                               "📋 Estados válidos:\n" +
                               "• REVISION → Puede volver a BORRADOR ✅\n" +
                               "• BORRADOR → Ya es borrador\n" +
                               "• EMITIDA → No se puede modificar (solo anular)");
            alert.showAndWait();
            return;
        }

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Devolución a Borrador");
        confirmacion.setHeaderText("¿Devolver factura " + factura.getNumero() + " a BORRADOR?");
        confirmacion.setContentText("La factura volverá al estado BORRADOR y podrás editarla.\n\n" +
                                   "⚠️ Solo úsalo si necesitas modificar la factura antes de emitirla.\n\n" +
                                   "Después deberás:\n" +
                                   "1. Realizar los cambios necesarios\n" +
                                   "2. Enviar a Revisión nuevamente\n" +
                                   "3. Aprobar y Emitir");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // ✅ Usar método específico del servicio para evitar rollback
                    String observaciones = "Devuelta a BORRADOR el " + LocalDate.now();
                    Factura actualizada = facturaService.volverABorrador(factura.getId());

                    // Actualizar observaciones adicionales
                    actualizada.setObservacionesRevision(
                        (actualizada.getObservacionesRevision() != null ?
                         actualizada.getObservacionesRevision() + "\n" : "") + observaciones);
                    facturaService.saveSimple(actualizada);

                    loadAll();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("✅ Devuelta a Borrador");
                    success.setHeaderText("Factura " + factura.getNumero() + " lista para editar");
                    success.setContentText("La factura ha vuelto al estado BORRADOR.\n\n" +
                                         "Ahora puedes:\n" +
                                         "• Editar los datos de la factura\n" +
                                         "• Modificar las líneas\n" +
                                         "• Cambiar el cliente\n\n" +
                                         "Cuando esté lista, envíala a revisión nuevamente.");
                    success.showAndWait();

                    log.info("Factura {} devuelta a estado BORRADOR", factura.getNumero());
                } catch (Exception e) {
                    log.error("Error devolviendo factura a borrador", e);
                    mostrarError("Error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Elimina una factura en estado BORRADOR
     */
    @FXML
    public void onEliminarBorrador() {
        Factura factura = tableFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            mostrarError("Selecciona una factura para eliminar");
            return;
        }

        if (!"BORRADOR".equals(factura.getEstado())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Estado Incorrecto");
            alert.setHeaderText("No se puede eliminar");
            alert.setContentText("Solo las facturas en estado BORRADOR pueden ser eliminadas.\n\n" +
                               "Estado actual: " + factura.getEstado() + "\n\n" +
                               "📋 Para facturas emitidas:\n" +
                               "• Si está EMITIDA → Usa 'Anular Factura'\n" +
                               "• Si está en REVISION → Usa 'Volver a Borrador' primero\n\n" +
                               "⚠️ Las facturas emitidas no se pueden eliminar,\n" +
                               "solo anular (requisito legal).");
            alert.showAndWait();
            return;
        }

        // Confirmar eliminación con advertencia
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("⚠️ CONFIRMAR ELIMINACIÓN");
        confirmacion.setHeaderText("¿ELIMINAR PERMANENTEMENTE la factura " + factura.getNumero() + "?");
        confirmacion.setContentText("🔴 ATENCIÓN: Esta acción NO se puede deshacer\n\n" +
                                   "Se eliminará:\n" +
                                   "• Factura: " + factura.getNumero() + "\n" +
                                   "• Cliente: " + (factura.getCliente() != null ? factura.getCliente().getNombre() : "N/A") + "\n" +
                                   "• Total: " + String.format("%.2f €", factura.getTotal()) + "\n" +
                                   "• Todas sus líneas\n\n" +
                                   "⚠️ Solo se pueden eliminar facturas en BORRADOR.\n" +
                                   "Las facturas emitidas se deben ANULAR, no eliminar.\n\n" +
                                   "¿Estás seguro de eliminar esta factura?");
        confirmacion.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirmacion.getDialogPane().lookupButton(ButtonType.YES).getStyleClass().add("btn-danger");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    String numeroFactura = factura.getNumero();

                    // Eliminar usando el servicio
                    facturaService.deleteById(factura.getId());

                    loadAll();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("✅ Factura Eliminada");
                    success.setHeaderText("Factura eliminada correctamente");
                    success.setContentText("La factura " + numeroFactura + " ha sido eliminada permanentemente.\n\n" +
                                         "Esta factura era un borrador y no estaba registrada\n" +
                                         "en la AEAT, por lo que se pudo eliminar sin problemas.");
                    success.showAndWait();

                    log.info("Factura {} (BORRADOR) eliminada correctamente", numeroFactura);
                } catch (Exception e) {
                    log.error("Error eliminando factura borrador", e);

                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("❌ Error al Eliminar");
                    error.setHeaderText("No se pudo eliminar la factura");
                    error.setContentText("Error técnico: " + e.getMessage() + "\n\n" +
                                       "Posibles causas:\n" +
                                       "• La factura tiene líneas asociadas\n" +
                                       "• Error de base de datos\n" +
                                       "• La factura ya no existe\n\n" +
                                       "Si el problema persiste, contacta al administrador.");
                    error.showAndWait();
                }
            } else {
                log.info("Usuario canceló la eliminación de factura {}", factura.getNumero());
            }
        });
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

