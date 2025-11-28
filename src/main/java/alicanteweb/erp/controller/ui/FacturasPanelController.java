package alicanteweb.erp.controller.ui;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class FacturasPanelController {
    @FXML private TableView<Factura> tablaFacturas;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colTotal;
    @FXML private TableColumn<Factura, String> colPagada;
    @FXML private VBox facturaForm;
    @FXML private TextField txtNumero;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<?> cbCliente;
    @FXML private TextField txtTotal;
    @FXML private TextField txtPagado;
    @FXML private CheckBox chkPagada;
    @FXML private Button btnGuardarFactura;
    @FXML private Button btnImprimirFactura;
    @FXML private Button btnPrevisualizarFactura;

    private final FacturaService facturaService;

    @Autowired
    public FacturasPanelController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @FXML
    public void initialize() {
        // Aquí puedes cargar las facturas en la tabla y preparar el formulario
        tablaFacturas.setItems(facturaService.findAllObservable());
        // Configura las columnas si es necesario
    }

    // Métodos para guardar, imprimir y previsualizar facturas
    // ...
}
