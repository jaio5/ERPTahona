package alicanteweb.erp.controller;


import alicanteweb.erp.entities.AlbaranesVenta;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Almacene;
import alicanteweb.erp.service.AlbaranesVentaService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.AlmaceneService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class AlbaranesVentaController {

    private final AlbaranesVentaService albaranesVentaService;
    private final ClienteService clienteService;
    private final AlmaceneService almaceneService;

    // JavaFX UI controls (connect in FXML)
    // Table in FXML is 'tableAlbaranes'
    @FXML
    private TableView<AlbaranesVenta> tableAlbaranes;

    // Table columns declared to match FXML fx:id and avoid IDE warnings
    @FXML
    private TableColumn<AlbaranesVenta, String> colNumero;

    @FXML
    private TableColumn<AlbaranesVenta, String> colFecha;

    @FXML
    private TableColumn<AlbaranesVenta, String> colCliente;

    @FXML
    private TableColumn<AlbaranesVenta, String> colAlmacen;

    @FXML
    private TableColumn<AlbaranesVenta, String> colTotal;

    @FXML
    private TableColumn<AlbaranesVenta, String> colEstado;

    // Form fields in FXML
    @FXML
    private TextField txtNumero;

    @FXML
    private TextArea txtObservaciones;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnEliminar;

    // Optional other controls referenced in the FXML (kept nullable)
    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVerLineas;

    @FXML
    private Button btnVolver;

    @FXML
    private TextField txtSearch;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<Cliente> cboCliente;

    @FXML
    private ComboBox<Almacene> cboAlmacen;

    @FXML
    private TextField txtTotal;

    @FXML
    private Label lblLineas;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnApplyFilters;

    private final ObservableList<AlbaranesVenta> albaranesObservable = FXCollections.observableArrayList();

    public AlbaranesVentaController(AlbaranesVentaService albaranesVentaService, ClienteService clienteService, AlmaceneService almaceneService) {
        this.albaranesVentaService = albaranesVentaService;
        this.clienteService = clienteService;
        this.almaceneService = almaceneService;
    }

    @FXML
    public void initialize() {
        // Configurar cell value factories para las columnas si existen
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (colNumero != null) {
            colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNumero()));
        }

        if (colFecha != null) {
            colFecha.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getFecha() != null ? df.format(cell.getValue().getFecha()) : ""));
        }

        if (colCliente != null) {
            colCliente.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getCliente() != null ?
                            (cell.getValue().getCliente().getNombre() != null ? cell.getValue().getCliente().getNombre() : "") : ""));
        }

        if (colAlmacen != null) {
            colAlmacen.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getAlmacen() != null ?
                            (cell.getValue().getAlmacen().getNombre() != null ? cell.getValue().getAlmacen().getNombre() : "") : ""));
        }

        if (colTotal != null) {
            colTotal.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getTotal() != null ? cell.getValue().getTotal().toString() : ""));
        }

        if (colEstado != null) {
            // La entidad AlbaranesVenta no tiene campo 'estado' - mostrar texto vacío por ahora
            colEstado.setCellValueFactory(cell -> new SimpleStringProperty(""));
        }

        // Vincula la lista observable al TableView si existe
        if (tableAlbaranes != null) {
            tableAlbaranes.setItems(albaranesObservable);

            // Selección cambia formulario
            tableAlbaranes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> populateForm(newSel));

            // Doble clic en fila para editar
            tableAlbaranes.setRowFactory(tv -> {
                TableRow<AlbaranesVenta> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        AlbaranesVenta rowData = row.getItem();
                        populateForm(rowData);
                    }
                });
                return row;
            });
        }

        // Poblar combos de cliente y almacén
        if (cboCliente != null) {
            cboCliente.getItems().clear();
            cboCliente.getItems().addAll(clienteService.findAll());
        }
        if (cboAlmacen != null) {
            cboAlmacen.getItems().clear();
            cboAlmacen.getItems().addAll(almaceneService.findAll());
        }

        loadAll();

        // Configure buttons if present
        if (btnNuevo != null) btnNuevo.setOnAction(evt -> crearAlbaran());
        if (btnEliminar != null) btnEliminar.setOnAction(evt -> {
            AlbaranesVenta sel = getSelected();
            if (sel != null) eliminarAlbaran(sel);
        });

        // Connect FXML-declared buttons
        if (btnSearch != null) btnSearch.setOnAction(evt -> handleSearch());
        if (btnApplyFilters != null) btnApplyFilters.setOnAction(evt -> handleApplyFilters());
        if (btnVerLineas != null) btnVerLineas.setOnAction(evt -> handleVerLineas());
        if (btnGuardar != null) btnGuardar.setOnAction(evt -> handleGuardar());
        if (btnVolver != null) btnVolver.setOnAction(evt -> handleVolver());
    }

    private AlbaranesVenta getSelected() {
        if (tableAlbaranes != null) {
            return tableAlbaranes.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    private void loadAll() {
        albaranesObservable.clear();
        List<AlbaranesVenta> todos = albaranesVentaService.findAll();
        albaranesObservable.addAll(todos);
    }

    private void crearAlbaran() {
        String numero = txtNumero != null ? txtNumero.getText() : null;
        String obs = txtObservaciones != null ? txtObservaciones.getText() : null;
        AlbaranesVenta a = new AlbaranesVenta();
        a.setNumero(numero);
        a.setObservaciones(obs);
        albaranesVentaService.save(a);
        loadAll();
    }

    private void eliminarAlbaran(AlbaranesVenta sel) {
        if (sel != null && sel.getId() != null) {
            albaranesVentaService.deleteById(sel.getId());
            loadAll();
        }
    }

    // FXML handlers referenced in the FXML file. Kept public for FXML access.
    @FXML
    public void handleNuevo() {
        crearAlbaran();
    }

    @FXML
    public void handleGuardar() {
        // Guardado simple: si hay seleccionado, actualiza, si no crea
        AlbaranesVenta sel = getSelected();
        if (sel == null) {
            crearAlbaran();
            return;
        }
        if (txtNumero != null) sel.setNumero(txtNumero.getText());
        if (txtObservaciones != null) sel.setObservaciones(txtObservaciones.getText());
        albaranesVentaService.save(sel);
        loadAll();
    }

    @FXML
    public void handleEliminar() {
        AlbaranesVenta sel = getSelected();
        if (sel != null) eliminarAlbaran(sel);
    }

    @FXML
    public void handleVerLineas() {
        // Placeholder: abrir diálogo de líneas (implementar más adelante)
        System.out.println("Ver/Editar líneas (pendiente)");
    }

    @FXML
    public void handleVolver() {
        // Placeholder: navegación al panel anterior
        System.out.println("Volver (pendiente)");
    }

    @FXML
    public void handleSearch() {
        // Búsqueda simple por número
        if (txtSearch == null) return;
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            loadAll();
            return;
        }
        albaranesObservable.clear();
        // Intentamos buscar por número exacto
        albaranesVentaService.findByNumero(q).ifPresentOrElse(
                albaranesObservable::add,
                this::loadAll
        );
    }

    @FXML
    public void handleApplyFilters() {
        // Placeholder: aplicar filtros por fecha/estado
        System.out.println("Aplicar filtros (pendiente)");
    }

    private void populateForm(AlbaranesVenta a) {
        if (a == null) {
            if (txtNumero != null) txtNumero.clear();
            if (txtObservaciones != null) txtObservaciones.clear();
            if (txtTotal != null) txtTotal.setText("0.00");
            if (lblLineas != null) lblLineas.setText("0 línea(s)");
            return;
        }
        if (txtNumero != null) txtNumero.setText(a.getNumero());
        if (txtObservaciones != null) txtObservaciones.setText(a.getObservaciones());
        if (dpFecha != null) dpFecha.setValue(a.getFecha());
        if (cboCliente != null) cboCliente.getSelectionModel().select(a.getCliente());
        if (cboAlmacen != null) cboAlmacen.getSelectionModel().select(a.getAlmacen());
        if (txtTotal != null) txtTotal.setText(a.getTotal() != null ? a.getTotal().toString() : "0.00");
        if (lblLineas != null) {
            int count = a.getAlbaranVentaLineas() != null ? a.getAlbaranVentaLineas().size() : 0;
            lblLineas.setText(count + " línea(s)");
        }
    }

}
