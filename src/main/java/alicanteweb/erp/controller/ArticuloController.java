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
    @FXML private TableColumn<Articulo, String> colDescripcion;
    @FXML private TableColumn<Articulo, String> colFamilia;
    @FXML private TableColumn<Articulo, BigDecimal> colPVP;
    @FXML private TableColumn<Articulo, BigDecimal> colCoste;
    @FXML private TableColumn<Articulo, BigDecimal> colIVA;
    @FXML private TableColumn<Articulo, String> colUnidad;
    @FXML private TableColumn<Articulo, Boolean> colActivo;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFamilia;
    @FXML private Label lblTotal;
    @FXML private Label lblEstado;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando ArticuloController");

        // Configurar columnas
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colFamilia != null) colFamilia.setCellValueFactory(new PropertyValueFactory<>("familia"));
        if (colPVP != null) colPVP.setCellValueFactory(new PropertyValueFactory<>("pvp"));
        if (colCoste != null) colCoste.setCellValueFactory(new PropertyValueFactory<>("coste"));
        if (colIVA != null) colIVA.setCellValueFactory(new PropertyValueFactory<>("iva"));
        if (colUnidad != null) colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

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

    @FXML
    public void onBuscar() {
        log.info("Buscando artículos");
        cargarDatos();
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

