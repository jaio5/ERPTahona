package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Devolucion;
import alicanteweb.erp.service.DevolucionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class DevolucionController extends BaseController<Devolucion> {
    private static final Logger log = LoggerFactory.getLogger(DevolucionController.class);

    private final DevolucionService devolucionService;

    @FXML private TableView<Devolucion> tableDevoluciones;
    @FXML private TableColumn<Devolucion, String> colNumero;
    @FXML private TableColumn<Devolucion, java.time.LocalDate> colFecha;
    @FXML private TableColumn<Devolucion, String> colCliente;
    @FXML private TableColumn<Devolucion, String> colEstado;
    @FXML private TableColumn<Devolucion, java.math.BigDecimal> colImporte;
    @FXML private Label lblTotal;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @FXML
    public void initialize() {
        this.table = tableDevoluciones;
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
        if (colImporte != null) colImporte.setCellValueFactory(new PropertyValueFactory<>("importeTotal"));

        if (tableDevoluciones != null) {
            tableDevoluciones.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Devolucion> devoluciones = devolucionService.findAll();
            actualizarTabla(devoluciones);
            log.info("Cargadas {} devoluciones", devoluciones.size());
        } catch (Exception e) {
            log.error("Error cargando devoluciones", e);
            mostrarError("Error al cargar devoluciones: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() { return "Devolución"; }

    @Override
    protected String getRutaFormulario() { return "/ui/devolucion_form.fxml"; }

    @Override
    protected boolean coincideConBusqueda(Devolucion item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getNumero() != null && item.getNumero().toLowerCase().contains(t)) ||
               (item.getMotivo() != null && item.getMotivo().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Devolucion item) {
        if (item != null && item.getId() != null) devolucionService.deleteById(item.getId());
    }

    @FXML
    public void onAceptarDevolucion() {
        Devolucion seleccionada = table.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Selecciona una devolución para aceptar");
            return;
        }
        if (mostrarConfirmacion("¿Aceptar esta devolución?")) {
            try {
                devolucionService.aceptarDevolucion(seleccionada.getId());
                cargarDatos();
                mostrarExito("Devolución aceptada");
            } catch (Exception e) {
                mostrarError("Error: " + e.getMessage());
            }
        }
    }
}
