package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import alicanteweb.erp.ui.Dialogs;

import java.text.DecimalFormat;

/**
 * Controlador para la gestión de Almacenes
 */
@Controller
public class AlmacenController {
    private static final Logger log = LoggerFactory.getLogger(AlmacenController.class);

    @FXML private TableView<Almacen> tableAlmacenes;
    @FXML private TableColumn<Almacen, Long> colId;
    @FXML private TableColumn<Almacen, String> colCodigo;
    @FXML private TableColumn<Almacen, String> colNombre;
    @FXML private TableColumn<Almacen, String> colCapacidad;
    @FXML private TableColumn<Almacen, String> colDisponible;
    @FXML private TableColumn<Almacen, Boolean> colActivo;

    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final AlmacenService almacenService;
    private final ApplicationContext applicationContext;

    public AlmacenController(ApplicationContext applicationContext, AlmacenService almacenService) {
        this.applicationContext = applicationContext;
        this.almacenService = almacenService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AlmacenController");
        configurarColumnas();
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarAlmacenes(newV));
        }

        // Aplicar estilo a la tabla
        if (tableAlmacenes != null) {
            tableAlmacenes.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }
    }

    private void configurarColumnas() {
        DecimalFormat df = new DecimalFormat("#,##0.00");

        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colCodigo != null) {
            colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        }
        if (colNombre != null) {
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        }
        if (colCapacidad != null) {
            colCapacidad.setCellValueFactory(cellData -> {
                var a = cellData.getValue();
                if (a.getCapacidad() != null) {
                    return new SimpleStringProperty(df.format(a.getCapacidad()));
                }
                return new SimpleStringProperty("");
            });
        }
        if (colDisponible != null) {
            colDisponible.setCellValueFactory(cellData -> {
                var a = cellData.getValue();
                if (a.getDisponible() != null) {
                    return new SimpleStringProperty(df.format(a.getDisponible()));
                }
                return new SimpleStringProperty("");
            });
        }
        if (colActivo != null) {
            colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
            // Formatear con emojis
            colActivo.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTextFill(null);
                    } else {
                        setText(item ? "✅ Activo" : "❌ Inactivo");
                        // Usar setTextFill en lugar de setStyle para evitar sobreescritura de CSS
                        setTextFill(item ? Color.web("#2e7d32") : Color.web("#c62828"));
                    }
                }
            });
        }
    }

    private void cargarDatos() {
        try {
            var almacenes = almacenService.findAll();
            if (tableAlmacenes != null) {
                tableAlmacenes.setItems(FXCollections.observableArrayList(almacenes));
            }
            if (lblTotal != null) {
                lblTotal.setText(almacenes.size() + " almacenes");
            }
            log.info("Almacenes cargados: {}", almacenes.size());
        } catch (Exception e) {
            log.error("Error cargando almacenes", e);
            mostrarError("Error al cargar almacenes: " + e.getMessage());
        }
    }

    private void filtrarAlmacenes(String busqueda) {
        try {
            var almacenes = almacenService.findAll();

            if (busqueda != null && !busqueda.isEmpty()) {
                String search = busqueda.toLowerCase();
                almacenes = almacenes.stream()
                    .filter(a -> (a.getCodigo() != null && a.getCodigo().toLowerCase().contains(search)) ||
                                (a.getNombre() != null && a.getNombre().toLowerCase().contains(search)))
                    .toList();
            }

            if (tableAlmacenes != null) {
                tableAlmacenes.setItems(FXCollections.observableArrayList(almacenes));
            }
            if (lblTotal != null) {
                lblTotal.setText(almacenes.size() + " almacenes");
            }
        } catch (Exception e) {
            log.error("Error filtrando almacenes", e);
        }
    }

    @FXML
    public void onBuscar() {
        String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
        filtrarAlmacenes(busqueda);
    }

    @FXML
    public void onNuevo() {
        try {
            log.info("Crear nuevo almacén - abriendo formulario");
            java.net.URL resource = getClass().getResource("/ui/almacen_form.fxml");
            if (resource == null) { mostrarAlerta("No se encuentra el formulario de almacén"); return; }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle("Nuevo Almacén");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();

            // Refrescar despues de cerrar
            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de almacén", e);
            mostrarError("No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onEditar() {
        Almacen almacen = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (almacen == null) {
            mostrarAlerta("Selecciona un almacén para editar");
            return;
        }
        try {
            java.net.URL resource = getClass().getResource("/ui/almacen_form.fxml");
            if (resource == null) { mostrarAlerta("No se encuentra el formulario de almacén"); return; }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof alicanteweb.erp.controller.AlmacenFormController) {
                ((alicanteweb.erp.controller.AlmacenFormController) ctrl).setAlmacen(almacen);
            }

            Stage dialog = new Stage();
            dialog.setTitle("Editar Almacén");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
            cargarDatos();
        } catch (Exception e) {
            log.error("Error abriendo formulario de edición", e);
            mostrarError("No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        Almacen almacen = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (almacen == null) {
            mostrarAlerta("Selecciona un almacén para eliminar");
            return;
        }

        if (mostrarConfirmacion("¿Deseas eliminar este almacén?\n\n" +
                                "Código: " + almacen.getCodigo() + "\n" +
                                "Nombre: " + almacen.getNombre())) {
            try {
                almacenService.deleteById(almacen.getId());
                cargarDatos();
                mostrarExito();
            } catch (Exception e) {
                log.error("Error eliminando almacén", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando almacenes");
        cargarDatos();
    }

    private void mostrarAlerta(String msg) { Dialogs.showWarn(msg); }

    private void mostrarExito() { Dialogs.showInfo("Almacén eliminado correctamente"); }

    private void mostrarError(String msg) { Dialogs.showError(msg); }

    private boolean mostrarConfirmacion(String msg) { return Dialogs.showConfirm(msg); }
}
