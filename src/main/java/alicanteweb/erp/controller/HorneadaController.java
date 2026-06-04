package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.service.HorneadaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HorneadaController extends BaseController<Horneada> {
    private static final Logger log = LoggerFactory.getLogger(HorneadaController.class);

    private final HorneadaService horneadaService;

    @FXML private TableView<Horneada> tableHorneadas;
    @FXML private TableColumn<Horneada, java.time.LocalDate> colFecha;
    @FXML private TableColumn<Horneada, String> colTipo;
    @FXML private TableColumn<Horneada, Integer> colTempInicial;
    @FXML private TableColumn<Horneada, Integer> colTempFinal;
    @FXML private TableColumn<Horneada, java.math.BigDecimal> colCantidad;
    @FXML private TableColumn<Horneada, String> colResultado;
    @FXML private Label lblTotal;

    public HorneadaController(HorneadaService horneadaService) {
        this.horneadaService = horneadaService;
    }

    @FXML
    public void initialize() {
        this.table = tableHorneadas;
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
        if (colTipo != null) colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoHorneada"));
        if (colTempInicial != null) colTempInicial.setCellValueFactory(new PropertyValueFactory<>("temperaturaInicial"));
        if (colTempFinal != null) colTempFinal.setCellValueFactory(new PropertyValueFactory<>("temperaturaFinal"));
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadProducida"));
        if (colResultado != null) colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));

        if (tableHorneadas != null) {
            tableHorneadas.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Horneada> horneadas = horneadaService.findAll();
            actualizarTabla(horneadas);
            log.info("Cargadas {} horneadas", horneadas.size());
        } catch (Exception e) {
            log.error("Error cargando horneadas", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() { return "Horneada"; }

    @Override
    protected String getRutaFormulario() { return "/ui/horneada_form.fxml"; }

    @Override
    protected boolean coincideConBusqueda(Horneada item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getTipoHorneada() != null && item.getTipoHorneada().toLowerCase().contains(t)) ||
               (item.getResultado() != null && item.getResultado().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Horneada item) {
        if (item != null && item.getId() != null) horneadaService.deleteById(item.getId());
    }
}
