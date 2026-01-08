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
    private javafx.scene.control.TableView<Cliente> tableClientes;
    @FXML
    private javafx.scene.control.TextField txtBuscar;
    @FXML
    private javafx.scene.control.ComboBox<String> cmbActivo;
    @FXML
    private javafx.scene.control.Label lblTotal;

    @FXML
    private TableColumn<Cliente, Long> colId;
    @FXML
    private TableColumn<Cliente, String> colCodigo;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colCIF;
    @FXML
    private TableColumn<Cliente, String> colDireccion;
    @FXML
    private TableColumn<Cliente, String> colPoblacion;
    @FXML
    private TableColumn<Cliente, String> colProvincia;
    @FXML
    private TableColumn<Cliente, Boolean> colActivo;

    @Autowired
    private ClienteService clienteService;

    @FXML
    public void initialize() {
        log.info("Inicializando ClienteController");

        // Asignar la tabla del FXML a la tabla base
        this.table = tableClientes;

        // Configurar columnas de la tabla
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colCIF != null) colCIF.setCellValueFactory(new PropertyValueFactory<>("cif"));
        if (colDireccion != null) colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(new PropertyValueFactory<>("poblacion"));
        if (colProvincia != null) colProvincia.setCellValueFactory(new PropertyValueFactory<>("provincia"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Configurar ComboBox de estado
        if (cmbActivo != null) {
            cmbActivo.getItems().addAll("Todos", "Activos", "Inactivos");
            cmbActivo.setValue("Activos");
        }

        // Configurar label para el BaseController
        if (lblTotal != null) {
            this.lblEstado = lblTotal;
        }

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

