package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Receta;
import alicanteweb.erp.service.RecetaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RecetaController extends BaseController<Receta> {
    private static final Logger log = LoggerFactory.getLogger(RecetaController.class);

    private final RecetaService recetaService;

    @FXML private TableView<Receta> tableRecetas;
    @FXML private TableColumn<Receta, String> colCodigo;
    @FXML private TableColumn<Receta, String> colNombre;
    @FXML private TableColumn<Receta, Integer> colTiempoPrep;
    @FXML private TableColumn<Receta, Integer> colTemperatura;
    @FXML private TableColumn<Receta, Boolean> colActivo;
    @FXML private Label lblTotal;

    public RecetaController(RecetaService recetaService) {
        this.recetaService = recetaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando RecetaController");
        this.table = tableRecetas;
        this.lblEstado = lblTotal;

        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colTiempoPrep != null) colTiempoPrep.setCellValueFactory(new PropertyValueFactory<>("tiempoPreparacion"));
        if (colTemperatura != null) colTemperatura.setCellValueFactory(new PropertyValueFactory<>("temperaturaHorneado"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        if (tableRecetas != null) {
            tableRecetas.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Receta> recetas = recetaService.findAll();
            actualizarTabla(recetas);
            log.info("Cargadas {} recetas", recetas.size());
        } catch (Exception e) {
            log.error("Error cargando recetas", e);
            mostrarError("Error al cargar recetas: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Receta";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/receta_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Receta item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getCodigo() != null && item.getCodigo().toLowerCase().contains(t)) ||
               (item.getNombre() != null && item.getNombre().toLowerCase().contains(t)) ||
               (item.getDescripcion() != null && item.getDescripcion().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Receta item) {
        if (item != null && item.getId() != null) {
            recetaService.darDeBaja(item.getId());
        }
    }
}
