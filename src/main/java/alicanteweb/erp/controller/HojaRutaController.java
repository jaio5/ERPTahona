package alicanteweb.erp.controller;

import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.service.HojaRutaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HojaRutaController extends BaseController<HojaRuta> {
    private static final Logger log = LoggerFactory.getLogger(HojaRutaController.class);

    private final HojaRutaService hojaRutaService;

    @FXML private TableView<HojaRuta> tableHojas;
    @FXML private TableColumn<HojaRuta, java.time.LocalDate> colFecha;
    @FXML private TableColumn<HojaRuta, String> colRuta;
    @FXML private TableColumn<HojaRuta, String> colConductor;
    @FXML private TableColumn<HojaRuta, String> colEstado;
    @FXML private Label lblTotal;

    public HojaRutaController(HojaRutaService hojaRutaService) {
        this.hojaRutaService = hojaRutaService;
    }

    @FXML
    public void initialize() {
        this.table = tableHojas;
        this.lblEstado = lblTotal;

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
        if (colConductor != null) colConductor.setCellValueFactory(new PropertyValueFactory<>("conductor"));

        if (tableHojas != null) {
            tableHojas.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<HojaRuta> hojas = hojaRutaService.findAll();
            actualizarTabla(hojas);
            log.info("Cargadas {} hojas de ruta", hojas.size());
        } catch (Exception e) {
            log.error("Error cargando hojas de ruta", e);
            mostrarError("Error al cargar hojas de ruta: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() { return "Hoja de Ruta"; }

    @Override
    protected String getRutaFormulario() { return "/ui/hoja_ruta_form.fxml"; }

    @Override
    protected boolean coincideConBusqueda(HojaRuta item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getConductor() != null && item.getConductor().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(HojaRuta item) {
        if (item != null && item.getId() != null) hojaRutaService.deleteById(item.getId());
    }

    @FXML
    public void onVerEntregas() {
        HojaRuta seleccionada = table.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Selecciona una hoja de ruta para ver entregas");
            return;
        }
        List<HojaRutaEntrega> entregas = hojaRutaService.getEntregas(seleccionada.getId());
        StringBuilder sb = new StringBuilder("Entregas: ").append(seleccionada.getFecha()).append("\n\n");
        for (HojaRutaEntrega e : entregas) {
            sb.append(e.getOrden()).append(". ").append(e.getCliente().getNombre())
              .append(" - ").append(Boolean.TRUE.equals(e.getEntregado()) ? "ENTREGADO" : "PENDIENTE");
            if (e.getIncidencia() != null) sb.append(" [").append(e.getIncidencia()).append("]");
            sb.append("\n");
        }
        mostrarInfo(sb.toString());
    }

    @FXML
    public void onIniciarRuta() {
        HojaRuta seleccionada = table.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Selecciona una hoja de ruta para iniciar");
            return;
        }
        if (!"PLANIFICADA".equals(seleccionada.getEstado())) {
            mostrarError("Solo se pueden iniciar hojas en estado PLANIFICADA");
            return;
        }
        if (mostrarConfirmacion("¿Iniciar esta hoja de ruta?")) {
            try {
                hojaRutaService.iniciarRuta(seleccionada.getId());
                cargarDatos();
                mostrarExito("Hoja de ruta iniciada");
            } catch (Exception e) {
                mostrarError("Error: " + e.getMessage());
            }
        }
    }
}
