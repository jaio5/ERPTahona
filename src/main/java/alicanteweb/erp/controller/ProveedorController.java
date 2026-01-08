package alicanteweb.erp.controller;

import alicanteweb.erp.ErpLauncher;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ProveedorController {

    @FXML private TableView<Proveedor> tableProveedores;
    @FXML private TableColumn<Proveedor, Long> colId;
    @FXML private TableColumn<Proveedor, String> colCodigo;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colCIF;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, String> colPoblacion;

    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final ProveedorService proveedorService;
    private ObservableList<Proveedor> proveedores;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando ProveedorController");

        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCIF.setCellValueFactory(new PropertyValueFactory<>("cif"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPoblacion.setCellValueFactory(new PropertyValueFactory<>("poblacion"));

        // Aplicar estilo a la tabla - fondo blanco, texto negro
        tableProveedores.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        // Cargar datos
        cargarProveedores();
    }

    private void cargarProveedores() {
        try {
            List<Proveedor> lista = proveedorService.findAll();
            proveedores = FXCollections.observableArrayList(lista);
            tableProveedores.setItems(proveedores);

            actualizarContador();
            log.info("Proveedores cargados: {}", proveedores.size());
        } catch (Exception e) {
            log.error("Error cargando proveedores", e);
            mostrarError("Error", "No se pudieron cargar los proveedores: " + e.getMessage());
        }
    }

    private void actualizarContador() {
        if (lblTotal != null) {
            int total = proveedores != null ? proveedores.size() : 0;
            lblTotal.setText(total + " proveedor" + (total != 1 ? "es" : ""));
        }
    }

    @FXML
    private void onCreate() {
        log.info("Abriendo formulario de nuevo proveedor");
        abrirFormulario(null);
    }

    @FXML
    public void onNuevo() {
        onCreate();
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando lista de proveedores");
        cargarProveedores();
    }

    @FXML
    public void onBuscar() {
        String termino = txtBuscar != null ? txtBuscar.getText() : "";
        filtrarProveedores(termino);
    }

    @FXML
    public void onVer() {
        Proveedor seleccionado = tableProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Por favor, selecciona un proveedor para ver.");
            return;
        }
        abrirFormulario(seleccionado);
    }

    @FXML
    public void onEditar() {
        onEdit();
    }

    @FXML
    public void onDarBaja() {
        onDelete();
    }

    private void filtrarProveedores(String termino) {
        try {
            List<Proveedor> lista = proveedorService.findAll();
            if (termino != null && !termino.isEmpty()) {
                lista = lista.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(termino.toLowerCase()) ||
                                p.getCodigo().toLowerCase().contains(termino.toLowerCase()) ||
                                (p.getCif() != null && p.getCif().toLowerCase().contains(termino.toLowerCase())))
                    .toList();
            }
            proveedores = FXCollections.observableArrayList(lista);
            tableProveedores.setItems(proveedores);
            actualizarContador();
            log.info("Proveedores filtrados: {}", proveedores.size());
        } catch (Exception e) {
            log.error("Error filtrando proveedores", e);
        }
    }

    @FXML
    private void onEdit() {
        Proveedor seleccionado = tableProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Por favor, selecciona un proveedor para editar.");
            return;
        }

        log.info("Editando proveedor: {}", seleccionado.getCodigo());
        abrirFormulario(seleccionado);
    }

    @FXML
    private void onDelete() {
        Proveedor seleccionado = tableProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Por favor, selecciona un proveedor para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar proveedor?");
        confirmacion.setContentText("¿Estás seguro de que deseas eliminar el proveedor: " + seleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                proveedorService.delete(seleccionado.getId());
                mostrarInfo("Éxito", "Proveedor eliminado correctamente");
                cargarProveedores();
            } catch (Exception e) {
                log.error("Error eliminando proveedor", e);
                mostrarError("Error", "No se pudo eliminar el proveedor: " + e.getMessage());
            }
        }
    }


    private void abrirFormulario(Proveedor proveedor) {
        try {
            mostrarInfo("Información", "Función en desarrollo");
        } catch (Exception e) {
            log.error("Error abriendo formulario", e);
            mostrarError("Error", "Error: " + e.getMessage());
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

