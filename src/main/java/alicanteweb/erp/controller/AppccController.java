package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AppccControl;
import alicanteweb.erp.service.AppccControlService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class AppccController extends BaseController<AppccControl> {
    private static final Logger log = LoggerFactory.getLogger(AppccController.class);

    private final AppccControlService appccService;

    @FXML private TableView<AppccControl> tableControles;
    @FXML private TableColumn<AppccControl, java.time.LocalDate> colFecha;
    @FXML private TableColumn<AppccControl, String> colPuntoCritico;
    @FXML private TableColumn<AppccControl, java.math.BigDecimal> colTemperatura;
    @FXML private TableColumn<AppccControl, String> colResultado;
    @FXML private TableColumn<AppccControl, String> colResponsable;
    @FXML private Label lblTotal;

    public AppccController(AppccControlService appccService) {
        this.appccService = appccService;
    }

    @FXML
    public void initialize() {
        this.table = tableControles;
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
        if (colPuntoCritico != null) colPuntoCritico.setCellValueFactory(new PropertyValueFactory<>("puntoCritico"));
        if (colTemperatura != null) colTemperatura.setCellValueFactory(new PropertyValueFactory<>("temperatura"));
        if (colResultado != null) colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        if (colResponsable != null) colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));

        if (tableControles != null) {
            tableControles.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<AppccControl> controles = appccService.findAll();
            actualizarTabla(controles);
            log.info("Cargados {} controles APPCC", controles.size());
        } catch (Exception e) {
            log.error("Error cargando controles APPCC", e);
            mostrarError("Error al cargar controles: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() { return "AppccControl"; }

    @Override
    protected String getRutaFormulario() { return "/ui/appcc_form.fxml"; }

    @Override
    protected boolean coincideConBusqueda(AppccControl item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getPuntoCritico() != null && item.getPuntoCritico().toLowerCase().contains(t)) ||
               (item.getResponsable() != null && item.getResponsable().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(AppccControl item) {
        if (item != null && item.getId() != null) appccService.deleteById(item.getId());
    }
}
