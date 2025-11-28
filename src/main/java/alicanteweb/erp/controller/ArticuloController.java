package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private final ArticuloService articuloService;
    private final ObservableList<Articulo> articulosList = FXCollections.observableArrayList();

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        // Configuración segura de columnas (soporta que la vista no declare alguna columna)
        if (colId != null) colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue() == null ? null : cell.getValue().getId()));
        if (colCodigo != null) colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getCodigo()).orElse("")));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() == null ? "" : Optional.ofNullable(cell.getValue().getDescripcion()).orElse("")));

        if (tableArticulos != null) tableArticulos.setItems(articulosList);
        loadAll();

        // Listener seguro para selección (evita NPE si la tabla es null)
        if (tableArticulos != null) {
            tableArticulos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                // por ahora no rellenamos campos porque la UI puede no tenerlos
            });
        }
    }

    private void loadAll() {
        try {
            List<Articulo> todos = articuloService.findAll();
            articulosList.setAll(todos);
        } catch (Exception e) {
            log.error("Error cargando artículos", e);
            showAlertError("Error cargando artículos: " + e.getMessage());
        }
    }

    @FXML
    public void handleNuevo() {
        // Pedimos código y descripción mediante diálogos simples (compatible sin campos FXML adicionales)
        TextInputDialog dlgCodigo = new TextInputDialog();
        dlgCodigo.setHeaderText("Nuevo artículo - Código");
        Optional<String> resCodigo = dlgCodigo.showAndWait();
        if (resCodigo.isEmpty() || resCodigo.get().isBlank()) {
            showAlertInfo("Operación cancelada o código vacío");
            return;
        }

        TextInputDialog dlgDesc = new TextInputDialog();
        dlgDesc.setHeaderText("Nuevo artículo - Descripción");
        Optional<String> resDesc = dlgDesc.showAndWait();

        Articulo a = new Articulo();
        a.setCodigo(resCodigo.get().trim());
        a.setDescripcion(resDesc.orElse("").trim());

        try {
            articuloService.save(a);
            loadAll();
            showAlertInfo("Artículo creado correctamente");
        } catch (Exception e) {
            log.error("Error creando artículo", e);
            showAlertError("Error creando artículo: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditar() {
        if (tableArticulos == null) { showAlertError("Tabla no disponible"); return; }
        Articulo sel = tableArticulos.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlertInfo("Selecciona un artículo para editar"); return; }

        TextInputDialog dlgDesc = new TextInputDialog(Optional.ofNullable(sel.getDescripcion()).orElse(""));
        dlgDesc.setHeaderText("Editar descripción");
        Optional<String> res = dlgDesc.showAndWait();
        if (res.isPresent()) {
            sel.setDescripcion(res.get().trim());
            try {
                articuloService.save(sel);
                loadAll();
                showAlertInfo("Artículo guardado");
            } catch (Exception e) {
                log.error("Error guardando artículo", e);
                showAlertError("Error guardando artículo: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleEliminar() {
        if (tableArticulos == null) { showAlertError("Tabla no disponible"); return; }
        Articulo sel = tableArticulos.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlertInfo("Selecciona un artículo para eliminar"); return; }
        if (sel.getId() == null) { showAlertError("El artículo seleccionado no tiene id"); return; }
        try {
            articuloService.deleteById(sel.getId());
            loadAll();
            showAlertInfo("Artículo eliminado");
        } catch (Exception e) {
            log.error("Error eliminando artículo", e);
            showAlertError("Error eliminando artículo: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefrescar() {
        loadAll();
    }

    @FXML
    public void handleSearch() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Buscar por descripción (texto)");
        Optional<String> q = dlg.showAndWait();
        if (q.isEmpty() || q.get().isBlank()) { loadAll(); return; }
        try {
            List<Articulo> encontrados = articuloService.searchByDescripcion(q.get().trim());
            articulosList.setAll(encontrados);
        } catch (Exception e) {
            log.error("Error buscando artículos", e);
            showAlertError("Error buscando artículos: " + e.getMessage());
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
}
