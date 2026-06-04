package alicanteweb.erp.controller;

import alicanteweb.erp.entities.RutaReparto;
import alicanteweb.erp.entities.RutaParada;
import alicanteweb.erp.service.RutaRepartoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RutaRepartoController extends BaseController<RutaReparto> {
    private static final Logger log = LoggerFactory.getLogger(RutaRepartoController.class);

    private final RutaRepartoService rutaService;

    @FXML private TableView<RutaReparto> tableRutas;
    @FXML private TableColumn<RutaReparto, String> colCodigo;
    @FXML private TableColumn<RutaReparto, String> colNombre;
    @FXML private TableColumn<RutaReparto, String> colConductor;
    @FXML private TableColumn<RutaReparto, Boolean> colActivo;
    @FXML private Label lblTotal;

    public RutaRepartoController(RutaRepartoService rutaService) {
        this.rutaService = rutaService;
    }

    @FXML
    public void initialize() {
        this.table = tableRutas;
        this.lblEstado = lblTotal;

        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colConductor != null) colConductor.setCellValueFactory(new PropertyValueFactory<>("conductor"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        if (tableRutas != null) {
            tableRutas.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<RutaReparto> rutas = rutaService.findAll();
            actualizarTabla(rutas);
            log.info("Cargadas {} rutas", rutas.size());
        } catch (Exception e) {
            log.error("Error cargando rutas", e);
            mostrarError("Error al cargar rutas: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() { return "RutaReparto"; }

    @Override
    protected String getRutaFormulario() { return "/ui/ruta_reparto_form.fxml"; }

    @Override
    protected boolean coincideConBusqueda(RutaReparto item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getCodigo() != null && item.getCodigo().toLowerCase().contains(t)) ||
               (item.getNombre() != null && item.getNombre().toLowerCase().contains(t)) ||
               (item.getConductor() != null && item.getConductor().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(RutaReparto item) {
        if (item != null && item.getId() != null) rutaService.darDeBaja(item.getId());
    }

    @FXML
    public void onVerParadas() {
        RutaReparto seleccionada = table.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Selecciona una ruta para ver sus paradas");
            return;
        }
        List<RutaParada> paradas = rutaService.getParadas(seleccionada.getId());
        StringBuilder sb = new StringBuilder("Paradas de: ").append(seleccionada.getNombre()).append("\n\n");
        if (paradas.isEmpty()) {
            sb.append("Sin paradas configuradas.");
        } else {
            for (RutaParada p : paradas) {
                sb.append(p.getOrden()).append(". ")
                  .append(p.getCliente() != null ? p.getCliente().getNombre() : "Sin cliente");
                if (p.getHoraEstimada() != null) sb.append(" (").append(p.getHoraEstimada()).append(")");
                sb.append("\n");
            }
        }
        mostrarInfo(sb.toString());
    }
}
