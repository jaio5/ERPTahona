package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Vehiculo;
import alicanteweb.erp.service.VehiculoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class VehiculoController extends BaseController<Vehiculo> {
    private static final Logger log = LoggerFactory.getLogger(VehiculoController.class);

    private final VehiculoService vehiculoService;

    @FXML private TableView<Vehiculo> tableVehiculos;
    @FXML private TableColumn<Vehiculo, String> colMatricula;
    @FXML private TableColumn<Vehiculo, String> colMarca;
    @FXML private TableColumn<Vehiculo, String> colModelo;
    @FXML private TableColumn<Vehiculo, String> colTipo;
    @FXML private TableColumn<Vehiculo, Boolean> colActivo;
    @FXML private Label lblTotal;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @FXML
    public void initialize() {
        this.table = tableVehiculos;
        this.lblEstado = lblTotal;

        if (colMatricula != null) colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        if (colMarca != null) colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        if (colModelo != null) colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        if (colTipo != null) colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        if (tableVehiculos != null) {
            tableVehiculos.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.findAll();
            actualizarTabla(vehiculos);
            log.info("Cargados {} vehículos", vehiculos.size());
        } catch (Exception e) {
            log.error("Error cargando vehículos", e);
            mostrarError("Error al cargar vehículos: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() { return "Vehículo"; }

    @Override
    protected String getRutaFormulario() { return "/ui/vehiculo_form.fxml"; }

    @Override
    protected boolean coincideConBusqueda(Vehiculo item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getMatricula() != null && item.getMatricula().toLowerCase().contains(t)) ||
               (item.getMarca() != null && item.getMarca().toLowerCase().contains(t)) ||
               (item.getModelo() != null && item.getModelo().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Vehiculo item) {
        if (item != null && item.getId() != null) vehiculoService.darDeBaja(item.getId());
    }
}
