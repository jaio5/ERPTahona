package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.service.LoteService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class LoteController extends BaseController<Lote> {
    private static final Logger log = LoggerFactory.getLogger(LoteController.class);

    private final LoteService loteService;

    @FXML private TableView<Lote> tableLotes;
    @FXML private TableColumn<Lote, String> colCodigo;
    @FXML private TableColumn<Lote, String> colArticulo;
    @FXML private TableColumn<Lote, java.time.LocalDate> colFechaProd;
    @FXML private TableColumn<Lote, java.time.LocalDate> colFechaCad;
    @FXML private TableColumn<Lote, java.math.BigDecimal> colCantidad;
    @FXML private TableColumn<Lote, String> colEstado;
    @FXML private Label lblTotal;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando LoteController");
        this.table = tableLotes;
        this.lblEstado = lblTotal;

        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colFechaProd != null) {
            colFechaProd.setCellValueFactory(new PropertyValueFactory<>("fechaProduccion"));
            colFechaProd.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(java.time.LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            });
        }
        if (colFechaCad != null) {
            colFechaCad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
            colFechaCad.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(java.time.LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    if (item.isBefore(java.time.LocalDate.now().plusDays(7))) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            });
        }
        if (colCantidad != null) colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (tableLotes != null) {
            tableLotes.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Lote> lotes = loteService.findAll();
            actualizarTabla(lotes);
            log.info("Cargados {} lotes", lotes.size());
        } catch (Exception e) {
            log.error("Error cargando lotes", e);
            mostrarError("Error al cargar lotes: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Lote";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/lote_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Lote item, String termino) {
        if (item == null || termino == null) return false;
        String t = termino.toLowerCase();
        return (item.getCodigo() != null && item.getCodigo().toLowerCase().contains(t)) ||
               (item.getOrigen() != null && item.getOrigen().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Lote item) {
        if (item != null && item.getId() != null) {
            loteService.darDeBaja(item.getId());
        }
    }

    @FXML
    public void onVerTrazabilidad() {
        Lote seleccionado = tableLotes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un lote para ver trazabilidad");
            return;
        }
        var insumos = loteService.findInsumosDeProducto(seleccionado.getId());
        StringBuilder sb = new StringBuilder("Trazabilidad del lote: ").append(seleccionado.getCodigo()).append("\n\n");
        if (insumos.isEmpty()) {
            sb.append("No hay registros de insumos para este lote.");
        } else {
            sb.append("Materias primas utilizadas:\n");
            for (var li : insumos) {
                sb.append(" - Lote: ").append(li.getLoteInsumo().getCodigo())
                  .append(" (").append(li.getCantidadUsada()).append(")\n");
            }
        }
        mostrarInfo(sb.toString());
    }
}
