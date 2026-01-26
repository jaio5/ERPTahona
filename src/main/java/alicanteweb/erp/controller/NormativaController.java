package alicanteweb.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub controller para cargas FXML (modelo347_panel.fxml). Proporciona handlers mínimos
 * para que FXMLLoader pueda instanciar el controlador durante los tests de carga de FXML.
 */
@Controller
public class NormativaController {
    private static final Logger log = LoggerFactory.getLogger(NormativaController.class);

    @FXML private ComboBox<Integer> cmbEjercicio;
    @FXML private TextField txtUmbral;
    @FXML private TableView<?> tableOperaciones;
    @FXML private TableColumn<?, ?> colCIF;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colImporte;
    @FXML private TableColumn<?, ?> colTrimestre;
    @FXML private Label lblTotal;

    @FXML
    public void initialize() {
        log.debug("Inicializando NormativaController (stub)");
    }

    @FXML
    public void onGenerar() {
        log.info("onGenerar() invocado (stub)");
    }

    @FXML
    public void onExportar() {
        log.info("onExportar() invocado (stub)");
    }

    @FXML
    public void onInforme() {
        log.info("onInforme() invocado (stub)");
    }
}
