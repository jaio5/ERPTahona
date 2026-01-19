package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProveedorController extends BaseController<Proveedor> {
    private static final Logger log = LoggerFactory.getLogger(ProveedorController.class);

    private final ProveedorService proveedorService;

    @FXML private TableView<Proveedor> tableProveedores;
    @FXML private TableColumn<Proveedor, Long> colId;
    @FXML private TableColumn<Proveedor, String> colCodigo;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colCIF;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, String> colPoblacion;

    // txtBuscar ya está declarado en BaseController; no volver a declarar aquí
    @FXML private Label lblTotal;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        log.info("✅ Inicializando ProveedorController");

        // Asignar la tabla del FXML a la tabla base
        this.table = tableProveedores;
        this.lblEstado = lblTotal;

        // Configurar columnas
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colCIF != null) colCIF.setCellValueFactory(new PropertyValueFactory<>("cif"));
        if (colTelefono != null) colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(new PropertyValueFactory<>("poblacion"));

        // Aplicar estilo a la tabla
        if (tableProveedores != null) {
            tableProveedores.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        // Inicializar controlador base
        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Proveedor> proveedores = proveedorService.findAll();
            actualizarTabla(proveedores);
            log.info("✅ Cargados {} proveedores", proveedores.size());
        } catch (Exception e) {
            log.error("❌ Error cargando proveedores", e);
            mostrarError("Error al cargar proveedores: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Proveedor";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/proveedor_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Proveedor item, String termino) {
        if (item == null || termino == null) return false;

        String t = termino.toLowerCase();
        return (item.getCodigo() != null && item.getCodigo().toLowerCase().contains(t)) ||
               (item.getNombre() != null && item.getNombre().toLowerCase().contains(t)) ||
               (item.getCif() != null && item.getCif().toLowerCase().contains(t)) ||
               (item.getTelefono() != null && item.getTelefono().toLowerCase().contains(t)) ||
               (item.getEmail() != null && item.getEmail().toLowerCase().contains(t)) ||
               (item.getPoblacion() != null && item.getPoblacion().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Proveedor item) {
        if (item != null && item.getId() != null) {
            // Dar de baja en lugar de eliminar
            item.setActivo(false);
            proveedorService.save(item);
            log.info("✅ Proveedor dado de baja: {}", item.getCodigo());
        }
    }

    @FXML
    public void onBuscar() {
        String termino = txtBuscar != null ? txtBuscar.getText() : "";
        filtrar(termino);
    }
}
