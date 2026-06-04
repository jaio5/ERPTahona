package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.AlbaranVentaService;
import alicanteweb.erp.service.ImpresionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import javafx.scene.input.KeyCode;

/**
 * Controlador para la gestión de Albaranes de Venta
 */
@Controller
public class AlbaranController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranController.class);

    @FXML private TableView<AlbaranVenta> tableAlbaranes;
    @FXML private TableColumn<AlbaranVenta, String> colNumero;
    @FXML private TableColumn<AlbaranVenta, LocalDate> colFecha;
    @FXML private TableColumn<AlbaranVenta, String> colCliente;
    @FXML private TableColumn<AlbaranVenta, BigDecimal> colTotal;
    @FXML private TableColumn<AlbaranVenta, String> colEstado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    private final AlbaranVentaService albaranVentaService;
    private final AlbaranService albaranService;
    private final ApplicationContext applicationContext;
    private final ImpresionService impresionService;

    public AlbaranController(AlbaranVentaService albaranVentaService, AlbaranService albaranService,
                             ApplicationContext applicationContext, ImpresionService impresionService) {
        this.albaranVentaService = albaranVentaService;
        this.albaranService = albaranService;
        this.applicationContext = applicationContext;
        this.impresionService = impresionService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AlbaranController");
        configurarColumnas();
        configurarFiltros();
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlbaranes(newV));
        }

        // Aplicar estilo a la tabla
        if (tableAlbaranes != null) {
            tableAlbaranes.setStyle("-fx-background-color: white; -fx-text-fill: black;");

            // Añadir menú contextual con acción 'Duplicar' para usar onDuplicar()
            ContextMenu cm = new ContextMenu();
            MenuItem duplicarItem = new MenuItem("Duplicar");
            duplicarItem.setOnAction(e -> onDuplicar());
            cm.getItems().add(duplicarItem);
            tableAlbaranes.setContextMenu(cm);

            // Atajo de teclado: Ctrl+D para duplicar
            tableAlbaranes.setOnKeyPressed(evt -> {
                if (evt.isControlDown() && evt.getCode() == KeyCode.D) {
                    onDuplicar();
                }
            });
        }
    }

    private void configurarColumnas() {
        if (colNumero != null) {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        }
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        }
        if (colCliente != null) {
            colCliente.setCellValueFactory(cellData -> {
                AlbaranVenta albaran = cellData.getValue();
                String nombreCliente = "";
                if (albaran != null && albaran.getCliente() != null) {
                    nombreCliente = albaran.getCliente().getNombre();
                }
                return new SimpleStringProperty(nombreCliente);
            });
        }
        if (colTotal != null) {
            colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        }
        if (colEstado != null) {
            // Mostrar de forma derivada si el albarán tiene observaciones (ya que AlbaranVenta no tiene campo 'estado')
            colEstado.setCellValueFactory(cellData -> {
                AlbaranVenta a = cellData.getValue();
                String estado = "";
                if (a != null) {
                    String obs = a.getObservaciones();
                    estado = (obs != null && !obs.trim().isEmpty()) ? "Con obs" : "Sin obs";
                }
                return new SimpleStringProperty(estado);
            });
        }
    }

    private void configurarFiltros() {
        // Inicializar combo de estados y listeners para filtrar
        if (cmbEstado != null) {
            // Reutilizamos el combo para filtrar por observaciones: Todos / Con observaciones / Sin observaciones
            cmbEstado.setItems(FXCollections.observableArrayList("Todos", "Con observaciones", "Sin observaciones"));
            cmbEstado.getSelectionModel().selectFirst();
            cmbEstado.valueProperty().addListener((obs, oldV, newV) -> filtrarAlbaranes(txtBuscar != null ? txtBuscar.getText() : ""));
        }

        // Añadir listeners a los datepickers para re-filtrar
        if (dpFechaDesde != null) {
            dpFechaDesde.valueProperty().addListener((obs, oldV, newV) -> filtrarAlbaranes(txtBuscar != null ? txtBuscar.getText() : ""));
        }
        if (dpFechaHasta != null) {
            dpFechaHasta.valueProperty().addListener((obs, oldV, newV) -> filtrarAlbaranes(txtBuscar != null ? txtBuscar.getText() : ""));
        }
    }

    private void cargarDatos() {
        try {
            List<AlbaranVenta> albaranes = albaranVentaService.findAll();
            if (tableAlbaranes != null) {
                tableAlbaranes.setItems(FXCollections.observableArrayList(albaranes));
            }
            if (lblTotal != null) {
                lblTotal.setText(albaranes.size() + " albaranes");
            }
            log.info("Albaranes cargados: {}", albaranes.size());
        } catch (Exception e) {
            log.error("Error cargando albaranes", e);
            mostrarError("Error al cargar albaranes: " + e.getMessage());
        }
    }

    private void filtrarAlbaranes(String busqueda) {
        try {
            List<AlbaranVenta> albaranes = albaranVentaService.findAll();

            String search = (busqueda != null) ? busqueda.toLowerCase() : null;

            LocalDate desde = (dpFechaDesde != null) ? dpFechaDesde.getValue() : null;
            LocalDate hasta = (dpFechaHasta != null) ? dpFechaHasta.getValue() : null;
            String estadoSel = (cmbEstado != null && cmbEstado.getValue() != null) ? cmbEstado.getValue() : "Todos";

            if ((search != null && !search.isEmpty()) || desde != null || hasta != null || (estadoSel != null && !"Todos".equalsIgnoreCase(estadoSel))) {
                final String finalSearch = search;
                final LocalDate finalDesde = desde;
                final LocalDate finalHasta = hasta;
                final String finalEstado = estadoSel;

                albaranes = albaranes.stream()
                    .filter(a -> {
                        boolean matchesSearch = true;
                        if (finalSearch != null && !finalSearch.isEmpty()) {
                            matchesSearch = (a.getNumero() != null && a.getNumero().toLowerCase().contains(finalSearch)) ||
                                            (a.getCliente() != null && a.getCliente().getNombre() != null &&
                                             a.getCliente().getNombre().toLowerCase().contains(finalSearch));
                        }

                        boolean matchesDesde = true;
                        if (finalDesde != null && a.getFecha() != null) {
                            matchesDesde = !a.getFecha().isBefore(finalDesde);
                        }

                        boolean matchesHasta = true;
                        if (finalHasta != null && a.getFecha() != null) {
                            matchesHasta = !a.getFecha().isAfter(finalHasta);
                        }

                        // Filtrado por observaciones según selección en cmbEstado
                        boolean matchesEstado = isMatchesEstado(a, finalEstado);

                        return matchesSearch && matchesDesde && matchesHasta && matchesEstado;
                    })
                    .toList();
            }

            if (tableAlbaranes != null) {
                tableAlbaranes.setItems(FXCollections.observableArrayList(albaranes));
            }
            if (lblTotal != null) {
                lblTotal.setText(albaranes.size() + " albaranes");
            }
        } catch (Exception e) {
            log.error("Error filtrando albaranes", e);
        }
    }

    private static boolean isMatchesEstado(AlbaranVenta a, String finalEstado) {
        boolean matchesEstado = true;
        if (finalEstado != null && !"Todos".equalsIgnoreCase(finalEstado)) {
            String obs = a.getObservaciones();
            if ("Con observaciones".equalsIgnoreCase(finalEstado)) {
                matchesEstado = obs != null && !obs.trim().isEmpty();
            } else if ("Sin observaciones".equalsIgnoreCase(finalEstado)) {
                matchesEstado = obs == null || obs.trim().isEmpty();
            }
        }
        return matchesEstado;
    }

    @FXML
    public void onBuscar() {
        String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
        if (busqueda != null && busqueda.trim().startsWith("ALB-")) {
            // búsqueda por número exacto
            albaranService.obtenerPorNumero(busqueda.trim()).ifPresentOrElse(a -> {
                tableAlbaranes.setItems(FXCollections.observableArrayList(java.util.List.of(a)));
                if (lblTotal != null) lblTotal.setText("1 albarán");
            }, () -> mostrarAlerta("No se encontró el albarán: " + busqueda));
            return;
        }

        // si la búsqueda es sólo dígitos, buscar por cliente id
        if (busqueda != null && busqueda.matches("^\\d+$")) {
            try {
                Long clienteId = Long.parseLong(busqueda);
                List<AlbaranVenta> porCliente = albaranService.buscarPorCliente(clienteId);
                tableAlbaranes.setItems(FXCollections.observableArrayList(porCliente));
                if (lblTotal != null) lblTotal.setText(porCliente.size() + " albaranes");
                return;
            } catch (NumberFormatException ignored) {
                // fallback a filtrado habitual
            }
        }

        filtrarAlbaranes(busqueda);
    }

    @FXML
    public void onNuevo() {
        try {
            log.info("Crear nuevo albarán - abriendo formulario");
            java.net.URL resource = getClass().getResource("/ui/albaran_form.fxml");
            if (resource == null) { mostrarAlerta("No se encuentra el formulario de albarán"); return; }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            javafx.scene.Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof alicanteweb.erp.controller.formcontroller.AlbaranFormController) {
                ((alicanteweb.erp.controller.formcontroller.AlbaranFormController) ctrl).setAlbaran(null);
            }
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Nuevo Albarán");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de albarán", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onVer() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán primero");
            return;
        }
        log.info("Ver albarán: {}", albaran.getNumero());
        mostrarDetalleAlbaran(albaran);
    }

    @FXML
    public void onEditar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para editar");
            return;
        }
        log.info("Editar albarán: {}", albaran.getNumero());
        abrirFormularioAlbaran(albaran);
    }

    @FXML
    public void onImprimir() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para imprimir");
            return;
        }
        log.info("Imprimir albarán: {}", albaran.getNumero());
        try {
            impresionService.imprimirAlbaran(albaran, true);
            mostrarInfo("Albarán generado correctamente en:\n" + impresionService.getDirectorioImpresiones());
        } catch (Exception e) {
            log.error("Error imprimiendo albarán {}", albaran.getNumero(), e);
            mostrarError("Error al imprimir albarán: " + e.getMessage());
        }
    }

    @FXML
    public void onFacturar() {
        var selected = tableAlbaranes.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            mostrarAlerta("Selecciona un albarán para facturar");
            return;
        }

        try {
            if (selected.size() == 1) {
                AlbaranVenta albaran = selected.get(0);
                log.info("Facturar albarán: {}", albaran.getNumero());
                Factura factura = albaranService.convertirAFactura(albaran.getId(), null);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Éxito");
                info.setContentText("Albarán convertido a factura: " + (factura != null ? factura.getNumero() : "(sin número)"));
                info.showAndWait();
            } else {
                // convertir varios
                List<Long> ids = selected.stream().map(AlbaranVenta::getId).toList();
                Factura factura = albaranService.convertirVariosAFactura(ids, null);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Éxito");
                info.setContentText("Albaranes convertidos a factura: " + (factura != null ? factura.getNumero() : "(sin número)"));
                info.showAndWait();
            }
            cargarDatos();
        } catch (Exception e) {
            log.error("Error al convertir albarán(es) a factura", e);
            mostrarError("Error al convertir albarán(es) a factura: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        AlbaranVenta albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            mostrarAlerta("Selecciona un albarán para eliminar");
            return;
        }
        log.info("Eliminar albarán: {}", albaran.getNumero());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmación");
        confirm.setHeaderText("¿Estás seguro de que deseas eliminar este albarán?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // No se permite eliminación real por ahora
            mostrarAlerta("Eliminación de albaranes no permitida en este momento");
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando lista de albaranes");
        cargarDatos();
    }

    @FXML
    public void onDuplicar() {
        AlbaranVenta seleccionado = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un albarán para duplicar");
            return;
        }
        try {
            AlbaranVenta duplicado = albaranService.duplicar(seleccionado.getId());
            mostrarAlerta("Albarán duplicado: " + duplicado.getNumero());
            cargarDatos();
        } catch (Exception e) {
            log.error("Error duplicando albarán", e);
            mostrarError("Error duplicando albarán: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarDetalleAlbaran(AlbaranVenta albaran) {
        String cliente = albaran.getCliente() != null ? albaran.getCliente().getNombre() : "Sin cliente";
        int lineas = albaran.getLineas() != null ? albaran.getLineas().size() : 0;
        String detalle = "Número: " + valor(albaran.getNumero()) + "\n"
            + "Fecha: " + valor(albaran.getFecha()) + "\n"
            + "Cliente: " + cliente + "\n"
            + "Total: " + valor(albaran.getTotal()) + "\n"
            + "Líneas: " + lineas + "\n"
            + "Observaciones: " + valor(albaran.getObservaciones());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de albarán");
        alert.setHeaderText(albaran.getNumero());
        alert.setContentText(detalle);
        alert.showAndWait();
    }

    private void abrirFormularioAlbaran(AlbaranVenta albaran) {
        try {
            java.net.URL resource = getClass().getResource("/ui/albaran_form.fxml");
            if (resource == null) {
                mostrarAlerta("No se encuentra el formulario de albarán");
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            javafx.scene.Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof alicanteweb.erp.controller.formcontroller.AlbaranFormController formController) {
                formController.setAlbaran(albaran);
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Editar Albarán");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de albarán {}", albaran.getNumero(), e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    private String valor(Object value) {
        return value != null ? value.toString() : "";
    }
}
