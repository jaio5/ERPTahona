package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.ImpresionService;
import alicanteweb.erp.service.VerifactuService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.Map;

@Controller
public class FacturaController extends BaseController<Factura> {
    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    private final FacturaService facturaService;
    private final ApplicationContext applicationContext;
    private final ImpresionService impresionService;
    private final VerifactuService verifactuService;

    @FXML private TableView<Factura> tableFacturas;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, LocalDate> colFecha;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, BigDecimal> colBase;
    @FXML private TableColumn<Factura, BigDecimal> colIVA;
    @FXML private TableColumn<Factura, BigDecimal> colTotal;
    @FXML private TableColumn<Factura, String> colEstado;

    // txtBuscar ya está heredado de BaseController
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private Label lblTotal;

    public FacturaController(FacturaService facturaService, ApplicationContext applicationContext,
                            ImpresionService impresionService, VerifactuService verifactuService) {
        this.facturaService = facturaService;
        this.applicationContext = applicationContext;
        this.impresionService = impresionService;
        this.verifactuService = verifactuService;
    }

    @FXML
    public void initialize() {
        log.info("✅ Inicializando FacturaController");

        // Asignar la tabla del FXML a la tabla base
        this.table = tableFacturas;
        this.lblEstado = lblTotal;

        // Configurar columnas
        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colCliente != null) {
            colCliente.setCellValueFactory(cellData -> {
                Factura factura = cellData.getValue();
                String clienteNombre = factura.getCliente() != null ?
                    factura.getCliente().getNombre() : "Sin cliente";
                return new javafx.beans.property.SimpleStringProperty(clienteNombre);
            });
        }
        if (colBase != null) colBase.setCellValueFactory(new PropertyValueFactory<>("baseImponible"));
        if (colIVA != null) colIVA.setCellValueFactory(new PropertyValueFactory<>("totalIva"));
        if (colTotal != null) colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configurar ComboBox de estado
        if (cmbEstado != null) {
            cmbEstado.getItems().addAll("Todas", "BORRADOR", "EMITIDA", "PAGADA", "ANULADA");
            cmbEstado.setValue("Todas");
            cmbEstado.valueProperty().addListener((obs, oldV, newV) -> filtrar(txtBuscar != null ? txtBuscar.getText() : ""));
        }

        // Reaccionar a cambios en los datepickers para re-filtrar
        if (dpFechaDesde != null) {
            dpFechaDesde.valueProperty().addListener((obs, oldV, newV) -> filtrar(txtBuscar != null ? txtBuscar.getText() : ""));
        }
        if (dpFechaHasta != null) {
            dpFechaHasta.valueProperty().addListener((obs, oldV, newV) -> filtrar(txtBuscar != null ? txtBuscar.getText() : ""));
        }

        // Aplicar estilo a la tabla
        if (tableFacturas != null) {
            tableFacturas.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        // Inicializar controlador base
        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Factura> facturas = facturaService.findAll();
            actualizarTabla(facturas);
            log.info("✅ Cargadas {} facturas", facturas.size());
        } catch (Exception e) {
            log.error("❌ Error cargando facturas", e);
            mostrarError("Error al cargar facturas: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Factura";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/factura_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Factura item, String termino) {
        if (item == null || termino == null) return false;

        String t = termino.toLowerCase();
        return (item.getNumero() != null && item.getNumero().toLowerCase().contains(t)) ||
               (item.getCliente() != null && item.getCliente().getNombre() != null &&
                item.getCliente().getNombre().toLowerCase().contains(t)) ||
               (item.getEstado() != null && item.getEstado().toLowerCase().contains(t)) ||
               (item.getObservaciones() != null && item.getObservaciones().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Factura item) {
        if (item != null && item.getId() != null) {
            facturaService.anularFactura(item.getId(), "Anulacion solicitada desde el listado de facturas");
            log.info("✅ Factura anulada: {}", item.getNumero());
        }
    }

    // Delegador para el botón Buscar en el FXML (renombrado)
    @FXML
    public void onBuscarFactura() {
        onBuscar();
    }

    // Delegador para el botón Ver en el FXML (renombrado)
    @FXML
    public void onVerFactura() {
        onVer();
    }

    @FXML
    public void onVer() {
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Ver factura: {}", selected.getNumero());
            mostrarDetalleFactura(selected);
        } else {
            mostrarAdvertencia("Selecciona una factura primero");
        }
    }

    @FXML
    public void onAnular() {
        if (!verificarPermisoAccion("eliminar")) {
            return;
        }
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (mostrarConfirmacion("¿Está seguro de anular la factura " + selected.getNumero() + "?")) {
                try {
                    eliminarItem(selected);
                    cargarDatos();
                } catch (IllegalStateException e) {
                    mostrarAdvertencia(e.getMessage());
                } catch (Exception e) {
                    log.error("Error anulando factura {}", selected.getNumero(), e);
                    mostrarError("Error al anular la factura: " + e.getMessage());
                }
            }
        } else {
            mostrarAdvertencia("Selecciona una factura primero");
        }
    }

    @FXML
    public void onImprimir() {
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAdvertencia("Selecciona una factura primero");
            return;
        }

        try {
            log.info("Imprimiendo factura: {}", selected.getNumero());

            // Generar e imprimir PDF con VeriFactu
            impresionService.imprimirFactura(selected, true);

            mostrarExito("Factura impresa correctamente.\nPDF generado en: " +
                impresionService.getDirectorioImpresiones());

        } catch (Exception e) {
            log.error("Error imprimiendo factura", e);
            mostrarError("Error al imprimir factura: " + e.getMessage());
        }
    }

    @FXML
    public void onEnviarAeat() {
        if (!verificarPermisoAccion("editar")) {
            return;
        }
        Factura selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Enviar factura a AEAT: {}", selected.getNumero());

            // Confirmación con el usuario
            if (!mostrarConfirmacion("¿Deseas enviar la factura " + selected.getNumero() + " a Verifactu/AEAT?")) {
                return;
            }

            // Ejecutar envío en background para no bloquear la UI
            Task<Map<String, Object>> task = new Task<>() {
                @Override
                protected Map<String, Object> call() throws Exception {
                    return verifactuService.enviarFactura(selected);
                }
            };

            task.setOnSucceeded(ev -> {
                Map<String, Object> resultado = task.getValue();
                boolean exito = Boolean.TRUE.equals(resultado.get("exito"));
                String mensaje = (String) resultado.getOrDefault("mensaje", "Resultado desconocido");
                if (exito) {
                    mostrarExito("Factura enviada correctamente: " + mensaje);
                } else {
                    mostrarError("Error enviando factura: " + mensaje);
                }
                // Refrescar tabla
                cargarDatos();
            });

            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                log.error("Error enviando factura a AEAT", ex);
                mostrarError("Error enviando factura: " + (ex != null ? ex.getMessage() : "Exception"));
            });

            new Thread(task, "verifactu-enviar-thread").start();
        } else {
            mostrarAdvertencia("Selecciona una factura primero");
        }
    }

    @FXML
    public void onCrearRectificativa() {
        try {
            log.info("Abriendo formulario de factura rectificativa...");

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/ui/factura_rectificativa_form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Crear Factura Rectificativa");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescar tabla después de cerrar
            cargarDatos();

        } catch (Exception e) {
            log.error("Error abriendo formulario de factura rectificativa", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    // Sobrescribimos filtrar para aplicar fecha y estado además del texto
    @Override
    protected void filtrar(String termino) {
        List<Factura> facturas = facturaService.findAll();

        String t = termino != null ? termino.toLowerCase() : "";
        LocalDate desde = dpFechaDesde != null ? dpFechaDesde.getValue() : null;
        LocalDate hasta = dpFechaHasta != null ? dpFechaHasta.getValue() : null;
        String estadoSel = cmbEstado != null && cmbEstado.getValue() != null ? cmbEstado.getValue() : "Todas";

        List<Factura> filtradas = facturas.stream()
            .filter(f -> {
                boolean matchesText = t.isEmpty() || coincideConBusqueda(f, t);

                boolean matchesDesde = true;
                if (desde != null && f.getFecha() != null) {
                    matchesDesde = !f.getFecha().isBefore(desde);
                }

                boolean matchesHasta = true;
                if (hasta != null && f.getFecha() != null) {
                    matchesHasta = !f.getFecha().isAfter(hasta);
                }

                boolean matchesEstado = true;
                if (estadoSel != null && !"Todas".equalsIgnoreCase(estadoSel)) {
                    matchesEstado = f.getEstado() != null && f.getEstado().equalsIgnoreCase(estadoSel);
                }

                return matchesText && matchesDesde && matchesHasta && matchesEstado;
            })
            .collect(Collectors.toList());

        actualizarTabla(filtradas);
    }

    private void mostrarDetalleFactura(Factura factura) {
        String cliente = factura.getCliente() != null ? factura.getCliente().getNombre() : "Sin cliente";
        int lineas = factura.getLineas() != null ? factura.getLineas().size() : 0;
        String detalle = "Número: " + valor(factura.getNumero()) + "\n"
            + "Fecha: " + valor(factura.getFecha()) + "\n"
            + "Cliente: " + cliente + "\n"
            + "Estado: " + valor(factura.getEstado()) + "\n"
            + "Base imponible: " + valor(factura.getBaseImponible()) + "\n"
            + "IVA: " + valor(factura.getTotalIva()) + "\n"
            + "Total: " + valor(factura.getTotal()) + "\n"
            + "Líneas: " + lineas + "\n"
            + "VeriFactu: " + (Boolean.TRUE.equals(factura.getVerifactuEnviada()) ? "Enviada" : "Pendiente/no enviada");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de factura");
        alert.setHeaderText(factura.getNumero());
        alert.setContentText(detalle);
        alert.showAndWait();
    }

    private String valor(Object value) {
        return value != null ? value.toString() : "";
    }
}
