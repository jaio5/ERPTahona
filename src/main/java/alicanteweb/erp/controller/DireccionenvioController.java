package alicanteweb.erp.controller;

import alicanteweb.erp.entities.DireccionenvioNew;
import alicanteweb.erp.service.DireccionenvioNewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class DireccionenvioController {
    private static final Logger log = LoggerFactory.getLogger(DireccionenvioController.class);

    @FXML private TableView<DireccionenvioNew> tableDirecciones;
    @FXML private TableColumn<DireccionenvioNew, Integer> colCodigoDireccion;
    @FXML private TableColumn<DireccionenvioNew, String> colNombre;
    @FXML private TableColumn<DireccionenvioNew, String> colDireccion;
    @FXML private TableColumn<DireccionenvioNew, String> colPoblacion;
    @FXML private TableColumn<DireccionenvioNew, String> colProvincia;
    @FXML private TableColumn<DireccionenvioNew, String> colCP;
    @FXML private TableColumn<DireccionenvioNew, String> colTelefono;
    @FXML private TableColumn<DireccionenvioNew, String> colNotas;
    @FXML private TableColumn<DireccionenvioNew, Void> colAcciones;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final DireccionenvioNewService service;
    private final ObservableList<DireccionenvioNew> items = FXCollections.observableArrayList();
    // Si se carga filtrado por cliente, mantener su id para pasar al formulario
    private Long clienteId = null;

    public DireccionenvioController(DireccionenvioNewService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cargarDatos();
    }

    private void configurarTabla() {
        if (tableDirecciones != null) tableDirecciones.setItems(items);
        if (colCodigoDireccion != null) colCodigoDireccion.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCodigoDireccion()));
        if (colNombre != null) colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        if (colDireccion != null) colDireccion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDireccion()));
        if (colPoblacion != null) colPoblacion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPoblacion()));
        if (colProvincia != null) colProvincia.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProvincia()));
        if (colCP != null) colCP.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCp()));
        if (colTelefono != null) colTelefono.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTelefono()));
        if (colNotas != null) colNotas.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNotas()));

        // acciones (editar/eliminar)
        if (colAcciones != null) {
            colAcciones.setCellFactory(col -> new TableCell<>() {
                private final Button btnEditar = new Button("Editar");
                private final Button btnEliminar = new Button("Eliminar");
                private final HBox box = new HBox(6);
                {
                    box.getChildren().addAll(btnEditar, btnEliminar);
                    btnEditar.setOnAction(evt -> {
                        DireccionenvioNew d = getTableRow().getItem();
                        if (d != null) abrirFormulario(d);
                    });
                    btnEliminar.setOnAction(evt -> {
                        DireccionenvioNew d = getTableRow().getItem();
                        if (d != null && mostrarConfirmacion("¿Eliminar esta dirección?")) {
                            try {
                                service.deleteById(d.getId());
                                cargarDatos();
                            } catch (Exception e) { mostrarError("Error al eliminar: " + e.getMessage()); }
                        }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(box);
                    }
                }
            });
        }
    }

    private void cargarDatos() {
        try {
            items.clear();
            List<DireccionenvioNew> list = service.findAll();
            items.addAll(list);
            if (lblTotal != null) lblTotal.setText(items.size() + " direcciones");
        } catch (Exception e) {
            log.error("Error cargando direcciones", e);
            mostrarError("Error al cargar direcciones: " + e.getMessage());
        }
    }

    public void cargarPorCliente(Long clienteId) {
        try {
            this.clienteId = clienteId;
            items.clear();
            if (clienteId != null) {
                items.addAll(service.findByClienteId(clienteId));
            } else {
                items.addAll(service.findAll());
            }
            if (lblTotal != null) lblTotal.setText(items.size() + " direcciones");
        } catch (Exception e) {
            log.error("Error cargando direcciones por cliente", e);
            mostrarError("Error al cargar direcciones: " + e.getMessage());
        }
    }

    @FXML
    public void onBuscar() {
        String q = txtBuscar != null ? txtBuscar.getText() : "";
        if (q == null || q.isBlank()) { cargarDatos(); return; }
        items.clear();
        items.addAll(service.searchByPoblacion(q));
    }

    @FXML
    public void onRefresh() { cargarDatos(); }

    @FXML
    public void onNuevo() { abrirFormulario(null); }

    private void abrirFormulario(DireccionenvioNew d) {
        try {
            var spring = alicanteweb.erp.ErpLauncher.getSpringContext();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ui/direccionenvio_form.fxml"));
            loader.setControllerFactory(spring::getBean);
            javafx.scene.Parent parent = loader.load();
            Object ctrl = loader.getController();
            try {
                var m = ctrl.getClass().getMethod("setDireccion", DireccionenvioNew.class);
                m.invoke(ctrl, d);
            } catch (NoSuchMethodException ignored) {}
            // pasar clienteId si existe
            try {
                if (this.clienteId != null) {
                    var m2 = ctrl.getClass().getMethod("setClienteId", Long.class);
                    m2.invoke(ctrl, this.clienteId);
                }
            } catch (NoSuchMethodException ignored) {}

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(d == null ? "Nueva Dirección" : "Editar Dirección");
            stage.setScene(new javafx.scene.Scene(parent));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarDatos();
        } catch (Exception e) { log.error("Error abriendo formulario direcciones", e); mostrarError("Error: " + e.getMessage()); }
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmación"); a.setHeaderText(null); a.setContentText(msg);
        var res = a.showAndWait(); return res.isPresent() && res.get() == ButtonType.OK;
    }

    private void mostrarError(String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
}
