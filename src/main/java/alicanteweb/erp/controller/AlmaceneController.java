package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Almacene;
import alicanteweb.erp.service.AlmaceneService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class AlmaceneController {

    private final AlmaceneService almaceneService;

    @FXML
    private TableView<Almacene> tableAlmacenes;

    @FXML
    private TableColumn<Almacene, String> colCodigo;

    @FXML
    private TableColumn<Almacene, String> colNombre;

    @FXML
    private TableColumn<Almacene, String> colAlbaranes;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cboFiltro;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private Label lblEstadisticas;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnApply;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnVerAlbaranes;

    private final ObservableList<Almacene> almacenesObservable = FXCollections.observableArrayList();

    public AlmaceneController(AlmaceneService almaceneService) {
        this.almaceneService = almaceneService;
    }

    @FXML
    public void initialize() {
        // Vincula la lista observable
        if (tableAlmacenes != null) tableAlmacenes.setItems(almacenesObservable);

        if (colCodigo != null)
            colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCodigo()));
        if (colNombre != null)
            colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        if (colAlbaranes != null)
            colAlbaranes.setCellValueFactory(cell -> new SimpleStringProperty(
                    String.valueOf(cell.getValue().getAlbaranesVentas() != null ? cell.getValue().getAlbaranesVentas().size() : 0)
            ));

        // Inicializar filtro mínimo para evitar advertencias
        if (cboFiltro != null) {
            cboFiltro.getItems().addAll("Todos");
            cboFiltro.getSelectionModel().selectFirst();
        }

        // Conectar botones FXML a handlers
        if (btnSearch != null) btnSearch.setOnAction(e -> handleSearch());
        if (btnApply != null) btnApply.setOnAction(e -> handleApplyFilter());
        if (btnNuevo != null) btnNuevo.setOnAction(e -> handleNuevo());
        if (btnGuardar != null) btnGuardar.setOnAction(e -> handleGuardar());
        if (btnEliminar != null) btnEliminar.setOnAction(e -> handleEliminar());
        if (btnVerAlbaranes != null) btnVerAlbaranes.setOnAction(e -> handleVerAlbaranes());

        // Listener para volcar la fila seleccionada en el formulario
        if (tableAlmacenes != null) {
            tableAlmacenes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    if (txtCodigo != null) txtCodigo.setText(newSel.getCodigo() != null ? newSel.getCodigo() : "");
                    if (txtNombre != null) txtNombre.setText(newSel.getNombre() != null ? newSel.getNombre() : "");
                } else {
                    if (txtCodigo != null) txtCodigo.setText("");
                    if (txtNombre != null) txtNombre.setText("");
                }
            });
        }

        loadAll();
    }

    private void loadAll() {
        almacenesObservable.clear();
        List<Almacene> todos = almaceneService.findAll();
        almacenesObservable.addAll(todos);
        if (lblEstadisticas != null) lblEstadisticas.setText(todos.size() + " almacén(es)");
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        almacenesObservable.clear();
        // buscar por código exacto primero
        Optional<Almacene> byCodigo = almaceneService.findByCodigo(q);
        if (byCodigo.isPresent()) {
            almacenesObservable.add(byCodigo.get());
            return;
        }
        // Fallback: filtrar por nombre
        almaceneService.findAll().stream()
                .filter(a -> a.getNombre() != null && a.getNombre().toLowerCase().contains(q.toLowerCase()))
                .forEach(almacenesObservable::add);
    }

    @FXML
    public void handleApplyFilter() {
        // placeholder
        System.out.println("Aplicar filtro (pendiente)");
    }

    @FXML
    public void handleNuevo() {
        if (txtCodigo != null) txtCodigo.setText("");
        if (txtNombre != null) txtNombre.setText("");
        if (tableAlmacenes != null) tableAlmacenes.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        Almacene selected = null;
        if (tableAlmacenes != null) selected = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (selected == null) selected = new Almacene();

        String codigo = txtCodigo != null ? txtCodigo.getText() : null;
        String nombre = txtNombre != null ? txtNombre.getText() : null;
        selected.setCodigo(codigo);
        selected.setNombre(nombre);

        // Validación de unicidad de código: si existe otro almacén con ese código, error
        if (codigo != null && !codigo.isBlank()) {
            Optional<Almacene> existente = almaceneService.findByCodigo(codigo);
            if (existente.isPresent() && (selected.getId() == null || !existente.get().getId().equals(selected.getId()))) {
                Alert a = new Alert(Alert.AlertType.ERROR, "El código ya existe");
                a.showAndWait();
                return;
            }
        }

        almaceneService.save(selected);
        loadAll();

        // volver a seleccionar el elemento guardado
        if (selected.getId() != null && tableAlmacenes != null) {
            final Long savedId = selected.getId();
            tableAlmacenes.getItems().stream()
                    .filter(a -> a.getId() != null && a.getId().equals(savedId))
                    .findFirst().ifPresent(it -> tableAlmacenes.getSelectionModel().select(it));
        }
    }

    @FXML
    public void handleEliminar() {
        Almacene selected = null;
        if (tableAlmacenes != null) selected = tableAlmacenes.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) return;
        if (selected.getAlbaranesVentas() != null && !selected.getAlbaranesVentas().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se puede eliminar un almacén con albaranes asociados");
            a.showAndWait();
            return;
        }
        almaceneService.deleteById(selected.getId());
        loadAll();
    }

    @FXML
    public void handleVerAlbaranes() {
        // placeholder para abrir la vista de albaranes relacionados
        System.out.println("Ver albaranes (pendiente)");
    }

    @FXML
    public void handleVolver() {
        System.out.println("Volver (pendiente)");
    }
}
