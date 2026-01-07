package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Controlador para el módulo de Clientes
 * Extiende BaseController para reutilizar funcionalidad CRUD
 */
@Controller
public class ClienteController extends BaseController<Cliente> {
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);


    @FXML
    private TableColumn<Cliente, Long> colId;
    @FXML
    private TableColumn<Cliente, String> colCodigo;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colCif;
    @FXML
    private TableColumn<Cliente, String> colDireccion;
    @FXML
    private TableColumn<Cliente, String> colPoblacion;
    @FXML
    private TableColumn<Cliente, String> colProvincia;

    @Autowired
    private ClienteService clienteService;

    @FXML
    public void initialize() {
        log.info("Inicializando ClienteController");

        // Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCif.setCellValueFactory(new PropertyValueFactory<>("cif"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colPoblacion.setCellValueFactory(new PropertyValueFactory<>("poblacion"));
        colProvincia.setCellValueFactory(new PropertyValueFactory<>("provincia"));

        // Inicializar controlador base
        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Cliente> clientes = clienteService.findAll();
            actualizarTabla(clientes);
            log.info("Cargados {} clientes", clientes.size());
        } catch (Exception e) {
            mostrarError("Error cargando clientes: " + e.getMessage());
            log.error("Error en cargarDatos", e);
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Cliente";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/cliente_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Cliente cliente, String termino) {
        return cliente.getNombre().toLowerCase().contains(termino) ||
               cliente.getCif().toLowerCase().contains(termino) ||
               cliente.getCodigo().toLowerCase().contains(termino);
    }

    @Override
    protected void eliminarItem(Cliente cliente) {
        clienteService.deleteById(cliente.getId());
    }
}

