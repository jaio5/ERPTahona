package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.ClienteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller
public class ClienteController {
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colCodigo;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colCif;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TableColumn<Cliente, String> colPoblacion;
    @FXML private TableColumn<Cliente, String> colProvincia;
    @FXML private TableColumn<Cliente, String> colCodigoPostal;
    @FXML private TextField txtBuscar;

    private final ClienteService clienteService;
    private final ObservableList<Cliente> clientesList = FXCollections.observableArrayList();

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        log.info("=== INICIALIZANDO ClienteController ===");
        log.info("tableClientes es null: {}", tableClientes == null);
        log.info("clientesList tamaño inicial: {}", clientesList.size());

        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigo()).orElse("")));
        if (colNombre != null) colNombre.setCellValueFactory(cell -> {
            Cliente c = cell.getValue();
            if (c == null) return new SimpleStringProperty("");
            String nombre = Optional.ofNullable(c.getNombre()).orElse("");
            // Agregar indicador si está inactivo
            if (c.getActivo() != null && !c.getActivo()) {
                nombre = "❌ " + nombre + " (INACTIVO)";
            }
            return new SimpleStringProperty(nombre);
        });
        if (colCif != null) colCif.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCif()).orElse("")));
        if (colDireccion != null) colDireccion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getDireccion()).orElse("")));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getPoblacion()).orElse("")));
        if (colProvincia != null) colProvincia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getProvincia()).orElse("")));
        if (colCodigoPostal != null) colCodigoPostal.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigoPostal()).orElse("")));

        if (tableClientes != null) {
            log.info("Vinculando ObservableList a tableClientes");
            tableClientes.setItems(clientesList);
        } else {
            log.error("ERROR: tableClientes es null, no se puede vincular la lista");
        }

        loadAll();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarClientes(newV));
        }

        log.info("=== FINALIZÓ INICIALIZACIÓN ClienteController ===");
    }

    private void loadAll() {
        try {
            log.info("Cargando clientes desde la base de datos...");
            List<Cliente> todos = clienteService.findAll();
            log.info("Se encontraron {} clientes en la base de datos", todos.size());
            clientesList.setAll(todos);
            log.info("Clientes cargados en la lista: {}", clientesList.size());

            // Forzar actualización de la tabla en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                if (tableClientes != null) {
                    tableClientes.refresh();
                    log.info("Tabla de clientes refrescada. Items: {}", tableClientes.getItems().size());
                }
            });
        } catch (Exception e) {
            log.error("Error cargando clientes", e);
            mostrarError("Error cargando clientes: " + e.getMessage());
        }
    }

    private void filtrarClientes(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Cliente> encontrados = clienteService.searchByNombre(filtro.trim());
            clientesList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error filtrando clientes", e);
        }
    }

    @FXML
    public void onCreate() {
        mostrarFormulario(null);
    }

    @FXML
    public void onEdit() {
        Cliente seleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un cliente para editar");
            return;
        }
        mostrarFormulario(seleccionado);
    }

    private void mostrarFormulario(Cliente cliente) {
        try {
            // Cargar el FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cliente_form.fxml"));
            VBox formRoot = loader.load();

            // Obtener los campos del formulario
            TextField txtCodigo = (TextField) formRoot.lookup("#txtCodigo");
            TextField txtNombre = (TextField) formRoot.lookup("#txtNombre");
            TextField txtCif = (TextField) formRoot.lookup("#txtCif");
            TextField txtDireccion = (TextField) formRoot.lookup("#txtDireccion");
            TextField txtPoblacion = (TextField) formRoot.lookup("#txtPoblacion");
            TextField txtCodigoPostal = (TextField) formRoot.lookup("#txtCodigoPostal");
            TextField txtProvincia = (TextField) formRoot.lookup("#txtProvincia");
            TextArea txtNotas = (TextArea) formRoot.lookup("#txtNotas");
            Button btnGuardar = (Button) formRoot.lookup("#btnGuardar");
            Button btnCancelar = (Button) formRoot.lookup("#btnCancelar");

            // Si estamos editando, rellenar los campos
            boolean esNuevo = (cliente == null);
            Cliente clienteEditar = esNuevo ? new Cliente() : cliente;

            if (!esNuevo) {
                txtCodigo.setText(cliente.getCodigo());
                txtNombre.setText(cliente.getNombre());
                txtCif.setText(cliente.getCif());
                txtDireccion.setText(cliente.getDireccion());
                txtPoblacion.setText(cliente.getPoblacion());
                txtCodigoPostal.setText(cliente.getCodigoPostal());
                txtProvincia.setText(cliente.getProvincia());
                txtNotas.setText(cliente.getNotas());
            } else {
                // Generar código automáticamente para nuevo cliente
                txtCodigo.setText(generarNuevoCodigo());
            }

            // Crear Stage modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(esNuevo ? "Nuevo Cliente" : "Editar Cliente");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(formRoot));

            // Configurar botones
            btnGuardar.setOnAction(e -> {
                if (validarFormulario(txtCodigo, txtNombre)) {
                    clienteEditar.setCodigo(txtCodigo.getText().trim());
                    clienteEditar.setNombre(txtNombre.getText().trim());
                    clienteEditar.setCif(txtCif.getText() != null ? txtCif.getText().trim() : null);
                    clienteEditar.setDireccion(txtDireccion.getText() != null ? txtDireccion.getText().trim() : null);
                    clienteEditar.setPoblacion(txtPoblacion.getText() != null ? txtPoblacion.getText().trim() : null);
                    clienteEditar.setCodigoPostal(txtCodigoPostal.getText() != null ? txtCodigoPostal.getText().trim() : null);
                    clienteEditar.setProvincia(txtProvincia.getText() != null ? txtProvincia.getText().trim() : null);
                    clienteEditar.setNotas(txtNotas.getText() != null ? txtNotas.getText().trim() : null);

                    try {
                        clienteService.save(clienteEditar);
                        loadAll();
                        mostrarInfo(esNuevo ? "Cliente creado correctamente" : "Cliente actualizado correctamente");
                        stage.close();
                    } catch (Exception ex) {
                        log.error("Error guardando cliente", ex);
                        mostrarError("Error al guardar: " + ex.getMessage());
                    }
                }
            });

            btnCancelar.setOnAction(e -> stage.close());

            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error mostrando formulario", e);
            mostrarError("Error al abrir el formulario: " + e.getMessage());
        }
    }


    private boolean validarFormulario(TextField txtCodigo, TextField txtNombre) {
        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            mostrarError("El código es obligatorio");
            return false;
        }
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            return false;
        }
        return true;
    }

    @FXML
    public void onDelete() {
        if (tableClientes == null) { mostrarError("Tabla no disponible"); return; }
        Cliente sel = tableClientes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarInfo("Selecciona un cliente"); return; }
        if (sel.getId() == null) { mostrarError("El cliente seleccionado no tiene id"); return; }

        // Verificar estado actual
        boolean estaActivo = sel.getActivo() == null || sel.getActivo();
        String accion = estaActivo ? "dar de baja" : "activar";
        String mensaje = estaActivo ?
            "¿Estás seguro de dar de baja el cliente '" + sel.getNombre() + "'?" :
            "¿Estás seguro de activar el cliente '" + sel.getNombre() + "'?";

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar " + accion);
        confirmacion.setHeaderText(mensaje);
        confirmacion.setContentText("Esta operación cambiará el estado del cliente.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Cambiar estado
                    sel.setActivo(!estaActivo);
                    clienteService.save(sel);
                    loadAll();
                    mostrarInfo("Cliente " + (estaActivo ? "dado de baja" : "activado") + " correctamente");
                } catch (Exception e) {
                    log.error("Error cambiando estado del cliente", e);
                    mostrarError("Error cambiando estado: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.setHeaderText("Información");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    private String generarNuevoCodigo() {
        try {
            List<Cliente> todos = clienteService.findAll();
            if (todos.isEmpty()) {
                return "CLI001";
            }

            // Buscar el código más alto
            int maxNumero = 0;
            for (Cliente c : todos) {
                String codigo = c.getCodigo();
                if (codigo != null && codigo.startsWith("CLI")) {
                    try {
                        String numeroStr = codigo.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                        }
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        // Ignorar códigos que no sigan el patrón CLIxxx
                    }
                }
            }

            // Incrementar y formatear
            return String.format("CLI%03d", maxNumero + 1);
        } catch (Exception e) {
            log.error("Error generando código automático", e);
            return "CLI001";
        }
    }
}
