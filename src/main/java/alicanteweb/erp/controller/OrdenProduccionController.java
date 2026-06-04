package alicanteweb.erp.controller;

import alicanteweb.erp.entities.OrdenProduccion;
import alicanteweb.erp.service.OrdenProduccionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrdenProduccionController extends BaseController<OrdenProduccion> {
    private static final Logger log = LoggerFactory.getLogger(OrdenProduccionController.class);

    private final OrdenProduccionService ordenProduccionService;

    @FXML private TableView<OrdenProduccion> tableOrdenes;
    @FXML private TableColumn<OrdenProduccion, String> colNumero;
    @FXML private TableColumn<OrdenProduccion, java.time.LocalDate> colFecha;
    @FXML private TableColumn<OrdenProduccion, String> colEstado;
    @FXML private TableColumn<OrdenProduccion, java.math.BigDecimal> colCantidad;
    @FXML private Label lblTotal;
    @FXML private ComboBox<String> cmbEstado;

    public OrdenProduccionController(OrdenProduccionService ordenProduccionService) {
        this.ordenProduccionService = ordenProduccionService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando OrdenProduccionController");
        this.table = tableOrdenes;
        this.lblEstado = lblTotal;

        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colFecha != null) {
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            colFecha.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(java.time.LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            });
        }
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadPlanificada"));

        if (tableOrdenes != null) {
            tableOrdenes.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        if (cmbEstado != null) {
            cmbEstado.getItems().addAll("Todos", "PLANIFICADA", "EN_CURSO", "FINALIZADA", "CANCELADA");
            cmbEstado.setValue("Todos");
            cmbEstado.setOnAction(e -> aplicarFiltros());
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<OrdenProduccion> ordenes = ordenProduccionService.findAll();
            actualizarTabla(ordenes);
            log.info("Cargadas {} órdenes", ordenes.size());
        } catch (Exception e) {
            log.error("Error cargando órdenes", e);
            mostrarError("Error al cargar órdenes: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (datosCompletos == null) return;
        String estadoSel = cmbEstado != null ? cmbEstado.getValue() : "Todos";

        List<OrdenProduccion> filtrados = datosCompletos.stream()
            .filter(o -> "Todos".equals(estadoSel) || o.getEstado().equals(estadoSel))
            .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtrados));
        if (lblEstado != null) {
            lblEstado.setText(String.format("Mostrando: %d de %d órdenes", filtrados.size(), datosCompletos.size()));
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Orden de Producción";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/orden_produccion_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(OrdenProduccion item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getNumero() != null && item.getNumero().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(OrdenProduccion item) {
        if (item != null && item.getId() != null) {
            ordenProduccionService.deleteById(item.getId());
        }
    }

    @FXML
    public void onIniciarProduccion() {
        OrdenProduccion seleccionada = table.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Selecciona una orden para iniciar");
            return;
        }
        if (!"PLANIFICADA".equals(seleccionada.getEstado())) {
            mostrarError("Solo se pueden iniciar órdenes en estado PLANIFICADA");
            return;
        }
        if (mostrarConfirmacion("¿Iniciar la producción de esta orden?")) {
            try {
                ordenProduccionService.iniciarProduccion(seleccionada.getId());
                cargarDatos();
                mostrarExito("Orden iniciada correctamente");
            } catch (Exception e) {
                mostrarError("Error: " + e.getMessage());
            }
        }
    }
}
