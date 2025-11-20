package alicanteweb.erp.controller.ui;

import org.springframework.stereotype.Component;
import alicanteweb.erp.service.FacturaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class FacturaFxController implements Initializable {

    private final FacturaService facturaService;

    public FacturaFxController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // inicialización...
    }

    @FXML
    private void onGuardarFactura() {
        // usar facturaService...
    }
}