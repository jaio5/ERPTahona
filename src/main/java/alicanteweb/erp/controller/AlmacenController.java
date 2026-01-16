package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
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

    public AlmacenController(AlmacenService almacenService) {
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
        log.info("Crear nuevo almacén");
        mostrarAlerta("Función en desarrollo: Crear nuevo almacén");
    }

    @FXML
    public void onEditar() {
        Almacen almacen = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (almacen == null) {
            mostrarAlerta("Selecciona un almacén para editar");
            return;
        }
        log.info("Editar almacén: {}", almacen.getCodigo());
        mostrarAlerta("Función en desarrollo: Editar almacén");
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

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void mostrarExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText("Almacén eliminado correctamente");
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
