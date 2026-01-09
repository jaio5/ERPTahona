package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ArticuloController {

    private final ArticuloService articuloService;

    @FXML private TableView<Articulo> tableArticulos;
    @FXML private TableColumn<Articulo, String> colCodigo;
    @FXML private TableColumn<Articulo, String> colNombre;
    @FXML private TableColumn<Articulo, BigDecimal> colPrecio;
    @FXML private TableColumn<Articulo, BigDecimal> colIVA;
    @FXML private TableColumn<Articulo, Integer> colStock;
    @FXML private TableColumn<Articulo, Boolean> colActivo;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbActivo;
    @FXML private Label lblTotal;
    @FXML private Label lblEstado;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando ArticuloController");

        // Configurar columnas - el nombre en Articulo es "descripcion"
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("pvp"));
        if (colIVA != null) colIVA.setCellValueFactory(new PropertyValueFactory<>("iva"));
        if (colStock != null) {
            // Stock no existe en la entidad, usar coste temporalmente
            colStock.setCellValueFactory(new PropertyValueFactory<>("coste"));
        }
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Aplicar estilo a la tabla - fondo blanco, texto negro
        if (tableArticulos != null) {
            tableArticulos.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            var articulos = articuloService.findAll();
            if (tableArticulos != null) {
                tableArticulos.setItems(FXCollections.observableArrayList(articulos));
            }
            if (lblTotal != null) {
                lblTotal.setText(articulos.size() + " artículos encontrados");
            }
            log.info("Cargados {} artículos", articulos.size());
        } catch (Exception e) {
            log.error("Error cargando artículos", e);
            mostrarError("Error al cargar artículos: " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        log.info("Refrescando lista de artículos");
        cargarDatos();
    }

    @FXML
    public void onBuscar() {
        log.info("Buscar artículos");
        String busqueda = txtBuscar != null ? txtBuscar.getText() : "";
        filtrarArticulos(busqueda);
    }

    @FXML
    public void onVer() {
        Articulo selected = tableArticulos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Ver artículo: {}", selected.getCodigo());
            mostrarInfo("Función en desarrollo");
        } else {
            mostrarAdvertencia("Selecciona un artículo primero");
        }
    }

    @FXML
    public void onDarBaja() {
        Articulo selected = tableArticulos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Dar de baja artículo: {}", selected.getCodigo());
            mostrarInfo("Función en desarrollo");
        } else {
            mostrarAdvertencia("Selecciona un artículo primero");
        }
    }

    private void filtrarArticulos(String termino) {
        try {
            var articulos = articuloService.findAll();
            if (termino != null && !termino.isEmpty()) {
                articulos = articulos.stream()
                    .filter(a -> a.getCodigo().toLowerCase().contains(termino.toLowerCase()) ||
                                a.getDescripcion().toLowerCase().contains(termino.toLowerCase()))
                    .toList();
            }
            if (tableArticulos != null) {
                tableArticulos.setItems(FXCollections.observableArrayList(articulos));
            }
            if (lblTotal != null) {
                lblTotal.setText(articulos.size() + " artículos encontrados");
            }
        } catch (Exception e) {
            log.error("Error filtrando artículos", e);
        }
    }

    @FXML
    public void onNuevo() {
        log.info("Crear nuevo artículo");
        mostrarInfo("Función en desarrollo");
    }

    @FXML
    public void onEditar() {
        Articulo selected = tableArticulos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Editar artículo: {}", selected.getCodigo());
            mostrarInfo("Función en desarrollo");
        } else {
            mostrarAdvertencia("Selecciona un artículo primero");
        }
    }

    @FXML
    public void onEliminar() {
        Articulo selected = tableArticulos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Eliminar artículo: {}", selected.getCodigo());
            mostrarInfo("Función en desarrollo");
        } else {
            mostrarAdvertencia("Selecciona un artículo primero");
        }
    }


    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

