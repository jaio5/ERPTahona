package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;

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
    private javafx.scene.control.ComboBox<String> cmbActivo;
    @FXML
    private javafx.scene.control.Label lblTotal;

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

    private final ClienteService clienteService;

    // Inyección por constructor
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

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

        // Añadir menú contextual para desactivar/activar cliente
        if (tableClientes != null) {
            ContextMenu cm = new ContextMenu();
            MenuItem desactivar = new MenuItem("Dar de baja");
            desactivar.setOnAction(e -> onDesactivarCliente());
            MenuItem activar = new MenuItem("Activar");
            activar.setOnAction(e -> onActivarCliente());
            cm.getItems().addAll(desactivar, activar);
            tableClientes.setContextMenu(cm);

            // Atajos de teclado
            tableClientes.setOnKeyPressed(evt -> {
                if (evt.isControlDown() && evt.getCode() == KeyCode.D) {
                    onDesactivarCliente();
                } else if (evt.isControlDown() && evt.getCode() == KeyCode.A) {
                    onActivarCliente();
                }
            });
        }
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

    // Handlers renombrados para evitar colisiones con BaseController
    @FXML
    public void onBuscarCliente() {
        String termino = (txtBuscar != null) ? txtBuscar.getText() : null;
        if (termino != null && !termino.trim().isEmpty()) {
            try {
                List<Cliente> resultados = clienteService.searchByNombre(termino.trim());
                if (tableClientes != null) tableClientes.setItems(FXCollections.observableArrayList(resultados));
                if (lblTotal != null) lblTotal.setText(resultados.size() + " clientes");
            } catch (Exception e) {
                log.error("Error buscando clientes por nombre", e);
                mostrarError("Error buscando clientes: " + e.getMessage());
            }
            return;
        }

        // fallback: delegar a la implementación base
        onBuscar();
    }

    @FXML
    public void onVerCliente() {
        onVer();
    }

    @FXML
    public void onDarBajaCliente() {
        onDarBaja();
    }

    @FXML
    public void onDesactivarCliente() {
        var seleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un cliente para dar de baja");
            return;
        }
        try {
            clienteService.darDeBaja(seleccionado.getId());
            mostrarInfo("Cliente dado de baja: " + seleccionado.getNombre());
            cargarDatos();
        } catch (Exception e) {
            log.error("Error dando de baja cliente", e);
            mostrarError("Error dando de baja cliente: " + e.getMessage());
        }
    }

    @FXML
    public void onActivarCliente() {
        var seleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un cliente para activar");
            return;
        }
        try {
            clienteService.activar(seleccionado.getId());
            mostrarInfo("Cliente activado: " + seleccionado.getNombre());
            cargarDatos();
        } catch (Exception e) {
            log.error("Error activando cliente", e);
            mostrarError("Error activando cliente: " + e.getMessage());
        }
    }
}
