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

    @FXML
    public void handleNuevoAlbaran() {
        Dialog<AlbaranesVenta> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Albarán");
        dialog.setHeaderText("Crear nuevo albarán");
        ButtonType btnCrear = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);

        TextField txtNumero = new TextField();
        DatePicker dpFecha = new DatePicker();
        ComboBox<Cliente> cboCliente = new ComboBox<>(FXCollections.observableArrayList(clienteService.findAll()));
        ComboBox<Almacene> cboAlmacen = new ComboBox<>(FXCollections.observableArrayList(almaceneService.findAll()));
        TextField txtTotal = new TextField();
        TextArea txtObservaciones = new TextArea();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Número:"), 0, 0);
        grid.add(txtNumero, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(dpFecha, 1, 1);
        grid.add(new Label("Cliente:"), 0, 2);
        grid.add(cboCliente, 1, 2);
        grid.add(new Label("Almacén:"), 0, 3);
        grid.add(cboAlmacen, 1, 3);
        grid.add(new Label("Total:"), 0, 4);
        grid.add(txtTotal, 1, 4);
        grid.add(new Label("Observaciones:"), 0, 5);
        grid.add(txtObservaciones, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnCrear) {
                AlbaranesVenta a = new AlbaranesVenta();
                a.setNumero(txtNumero.getText());
                a.setFecha(dpFecha.getValue());
                a.setCliente(cboCliente.getValue());
                a.setAlmacen(cboAlmacen.getValue());
                try {
                    a.setTotal(new java.math.BigDecimal(txtTotal.getText()));
                } catch (Exception e) {
                    a.setTotal(java.math.BigDecimal.ZERO);
                }
                a.setObservaciones(txtObservaciones.getText());
                return a;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(albaran -> {
            albaranesVentaService.save(albaran);
            tableAlbaranes.getItems().add(albaran);
        });
    }

    @FXML
    public void handleImprimirAlbaran() {
        AlbaranesVenta albaran = null;
        if (tableAlbaranes != null) albaran = tableAlbaranes.getSelectionModel().getSelectedItem();
        if (albaran == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un albarán para imprimir").showAndWait();
            return;
        }
        try {
            albaranesVentaService.imprimirAlbaran(albaran);
            new Alert(Alert.AlertType.INFORMATION, "Albarán impreso correctamente").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al imprimir el albarán: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void handlePrevisualizarAlbaran() {
        AlbaranesVenta albaran = getSelected();
        if (albaran == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un albarán para previsualizar").showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Número: ").append(albaran.getNumero()).append("\n");
        sb.append("Fecha: ").append(albaran.getFecha()).append("\n");
        sb.append("Cliente: ").append(albaran.getCliente() != null ? albaran.getCliente().getNombre() : "").append("\n");
        sb.append("Almacén: ").append(albaran.getAlmacen() != null ? albaran.getAlmacen().getNombre() : "").append("\n");
        sb.append("Total: ").append(albaran.getTotal()).append("\n");
        sb.append("Observaciones: ").append(albaran.getObservaciones()).append("\n");
        // Puedes añadir aquí las líneas del albarán si lo deseas
        new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
    }

}
