package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
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

@SuppressWarnings("unused")
@Controller
public class ArticuloController {
    private static final Logger log = LoggerFactory.getLogger(ArticuloController.class);

    @FXML private TableView<Articulo> tableArticulos;
    @FXML private TableColumn<Articulo, Long> colId;
    @FXML private TableColumn<Articulo, String> colCodigo;
    @FXML private TableColumn<Articulo, String> colDescripcion;
    @FXML private TableColumn<Articulo, String> colFamilia;
    @FXML private TableColumn<Articulo, String> colUnidad;
    @FXML private TableColumn<Articulo, String> colPvp;
    @FXML private TableColumn<Articulo, String> colCoste;
    @FXML private TableColumn<Articulo, String> colIva;
    @FXML private TextField txtBuscar;

    private final ArticuloService articuloService;
    private final ObservableList<Articulo> articulosList = FXCollections.observableArrayList();

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        // Configuración segura de columnas
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigo()).orElse("")));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getDescripcion()).orElse("")));
        if (colFamilia != null) colFamilia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getFamilia()).orElse("")));
        if (colUnidad != null) colUnidad.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getUnidad()).orElse("")));
        if (colPvp != null) colPvp.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getPvp() == null ? "" : cell.getValue().getPvp().toString()));
        if (colCoste != null) colCoste.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getCoste() == null ? "" : cell.getValue().getCoste().toString()));
        if (colIva != null) colIva.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null || cell.getValue().getIva() == null ? "" : cell.getValue().getIva().toString()));

        if (tableArticulos != null) tableArticulos.setItems(articulosList);
        loadAll();

        // Listener de búsqueda en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> filtrarArticulos(newV));
        }
    }

    private void loadAll() {
        try {
            log.info("Cargando artículos desde la base de datos...");
            List<Articulo> todos = articuloService.findAll();
            log.info("Se encontraron {} artículos en la base de datos", todos.size());
            articulosList.setAll(todos);
            log.info("Artículos cargados en la lista: {}", articulosList.size());

            // Forzar actualización de la tabla en el hilo de JavaFX
            javafx.application.Platform.runLater(() -> {
                if (tableArticulos != null) {
                    tableArticulos.refresh();
                    log.info("Tabla de artículos refrescada. Items: {}", tableArticulos.getItems().size());
                }
            });
        } catch (Exception e) {
            log.error("Error cargando artículos", e);
            showAlertError("Error cargando artículos: " + e.getMessage());
        }
    }

    @FXML
    public void onCreate() {
        mostrarFormulario(null);
    }

    @FXML
    public void onEdit() {
        if (tableArticulos == null) { showAlertError("Tabla no disponible"); return; }
        Articulo sel = tableArticulos.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlertInfo("Selecciona un artículo para editar"); return; }
        mostrarFormulario(sel);
    }

    private void mostrarFormulario(Articulo articulo) {
        try {
            // Cargar el FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/articulo_form.fxml"));
            VBox formRoot = loader.load();

            // Obtener los campos del formulario
            TextField txtCodigo = (TextField) formRoot.lookup("#txtCodigo");
            TextField txtDescripcion = (TextField) formRoot.lookup("#txtDescripcion");
            TextField txtFamilia = (TextField) formRoot.lookup("#txtFamilia");
            TextField txtUnidad = (TextField) formRoot.lookup("#txtUnidad");
            TextField txtIva = (TextField) formRoot.lookup("#txtIva");
            TextField txtPvp = (TextField) formRoot.lookup("#txtPvp");
            TextField txtCoste = (TextField) formRoot.lookup("#txtCoste");
            Button btnGuardar = (Button) formRoot.lookup("#btnGuardar");
            Button btnCancelar = (Button) formRoot.lookup("#btnCancelar");

            // Si estamos editando, rellenar los campos
            boolean esNuevo = (articulo == null);
            Articulo articuloEditar = esNuevo ? new Articulo() : articulo;

            if (!esNuevo) {
                txtCodigo.setText(articulo.getCodigo());
                txtDescripcion.setText(articulo.getDescripcion());
                txtFamilia.setText(articulo.getFamilia());
                txtUnidad.setText(articulo.getUnidad());
                txtIva.setText(articulo.getIva() != null ? articulo.getIva().toString() : "");
                txtPvp.setText(articulo.getPvp() != null ? articulo.getPvp().toString() : "");
                txtCoste.setText(articulo.getCoste() != null ? articulo.getCoste().toString() : "");
            } else {
                // Generar código automáticamente para nuevo artículo
                txtCodigo.setText(generarNuevoCodigo());
                // Valores por defecto
                txtIva.setText("21.00");
            }

            // Crear Stage modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(esNuevo ? "Nuevo Artículo" : "Editar Artículo");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(formRoot));

            // Configurar botones
            btnGuardar.setOnAction(e -> {
                if (validarFormulario(txtCodigo, txtDescripcion)) {
                    articuloEditar.setCodigo(txtCodigo.getText().trim());
                    articuloEditar.setDescripcion(txtDescripcion.getText().trim());
                    articuloEditar.setFamilia(txtFamilia.getText() != null ? txtFamilia.getText().trim() : null);
                    articuloEditar.setUnidad(txtUnidad.getText() != null ? txtUnidad.getText().trim() : null);

                    try {
                        articuloEditar.setIva(txtIva.getText() != null && !txtIva.getText().trim().isEmpty()
                            ? new java.math.BigDecimal(txtIva.getText().trim()) : null);
                        articuloEditar.setPvp(txtPvp.getText() != null && !txtPvp.getText().trim().isEmpty()
                            ? new java.math.BigDecimal(txtPvp.getText().trim()) : null);
                        articuloEditar.setCoste(txtCoste.getText() != null && !txtCoste.getText().trim().isEmpty()
                            ? new java.math.BigDecimal(txtCoste.getText().trim()) : null);
                    } catch (NumberFormatException ex) {
                        showAlertError("Los valores numéricos no son válidos");
                        return;
                    }

                    try {
                        articuloService.save(articuloEditar);
                        loadAll();
                        showAlertInfo(esNuevo ? "Artículo creado correctamente" : "Artículo actualizado correctamente");
                        stage.close();
                    } catch (Exception ex) {
                        log.error("Error guardando artículo", ex);
                        showAlertError("Error al guardar: " + ex.getMessage());
                    }
                }
            });

            btnCancelar.setOnAction(e -> stage.close());

            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error mostrando formulario", e);
            showAlertError("Error al abrir el formulario: " + e.getMessage());
        }
    }

    private boolean validarFormulario(TextField txtCodigo, TextField txtDescripcion) {
        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            showAlertError("El código es obligatorio");
            return false;
        }
        if (txtDescripcion.getText() == null || txtDescripcion.getText().trim().isEmpty()) {
            showAlertError("La descripción es obligatoria");
            return false;
        }
        return true;
    }

    @FXML
    public void onDelete() {
        if (tableArticulos == null) { showAlertError("Tabla no disponible"); return; }
        Articulo sel = tableArticulos.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlertInfo("Selecciona un artículo"); return; }
        if (sel.getId() == null) { showAlertError("El artículo seleccionado no tiene id"); return; }

        // Verificar estado actual
        boolean estaActivo = sel.getActivo() == null || sel.getActivo();
        String accion = estaActivo ? "dar de baja" : "activar";
        String mensaje = estaActivo ?
            "¿Estás seguro de dar de baja el artículo '" + sel.getDescripcion() + "'?" :
            "¿Estás seguro de activar el artículo '" + sel.getDescripcion() + "'?";

        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar " + accion);
        confirmacion.setHeaderText(mensaje);
        confirmacion.setContentText("Esta operación cambiará el estado del artículo.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Cambiar estado
                    sel.setActivo(!estaActivo);
                    articuloService.save(sel);
                    loadAll();
                    showAlertInfo("Artículo " + (estaActivo ? "dado de baja" : "activado") + " correctamente");
                } catch (Exception e) {
                    log.error("Error cambiando estado del artículo", e);
                    showAlertError("Error cambiando estado: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onRefresh() {
        loadAll();
    }

    private void filtrarArticulos(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            loadAll();
            return;
        }
        try {
            List<Articulo> encontrados = articuloService.searchByDescripcion(filtro.trim());
            articulosList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error filtrando artículos", e);
        }
    }

    private void showAlertInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText("Información");
        a.showAndWait();
    }

    private void showAlertError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText("Error");
        a.showAndWait();
    }

    private String generarNuevoCodigo() {
        try {
            List<Articulo> todos = articuloService.findAll();
            if (todos.isEmpty()) {
                return "ART001";
            }

            // Buscar el código más alto
            int maxNumero = 0;
            for (Articulo a : todos) {
                String codigo = a.getCodigo();
                if (codigo != null && codigo.startsWith("ART")) {
                    try {
                        String numeroStr = codigo.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                        }
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        // Ignorar códigos que no sigan el patrón ARTxxx
                    }
                }
            }

            // Incrementar y formatear
            return String.format("ART%03d", maxNumero + 1);
        } catch (Exception e) {
            log.error("Error generando código automático", e);
            return "ART001";
        }
    }
}
