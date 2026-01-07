package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.VerifacturAEATService;
import alicanteweb.erp.service.FacturaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de VeriFacTur (AEAT)
 */
@Controller
public class VerifactuController {
    private static final Logger log = LoggerFactory.getLogger(VerifactuController.class);
    
    @FXML private TableView<Map<String, Object>> tableVerifactu;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    @FXML private TableColumn<Map<String, Object>, Integer> colId;
    @FXML private TableColumn<Map<String, Object>, String> colFactura;
    @FXML private TableColumn<Map<String, Object>, String> colFechaEnvio;
    @FXML private TableColumn<Map<String, Object>, String> colEstado;
    @FXML private TableColumn<Map<String, Object>, String> colReferencia;
    
    private final VerifacturAEATService verifacturService;
    private final FacturaService facturaService;
    private final ObservableList<Map<String, Object>> listaVerifactur = FXCollections.observableArrayList();

    public VerifactuController(VerifacturAEATService verifacturService, FacturaService facturaService) {
        this.verifacturService = verifacturService;
        this.facturaService = facturaService;
    }

    @FXML
    public void initialize() {
        log.info("VerifactuController inicializado");
        tableVerifactu.setItems(listaVerifactur);
        cargarDatos();
    }

    private void cargarDatos() {
        listaVerifactur.clear();
        
        try {
            // Simular datos de facturas enviadas a VeriFacTur
            List<Factura> facturas = facturaService.findAll();

            for (Factura factura : facturas) {
                if (factura.getVerifactuEnviada() != null && factura.getVerifactuEnviada()) {
                    Map<String, Object> registro = new java.util.HashMap<>();
                    registro.put("id", factura.getId());
                    registro.put("factura", factura.getNumero());
                    registro.put("fechaEnvio", factura.getFechaEmisionVerifactu());
                    registro.put("estado", "ACEPTADA");
                    registro.put("referencia", "VF" + factura.getId());
                    
                    listaVerifactur.add(registro);
                }
            }
            
            lblTotal.setText("Total: " + listaVerifactur.size() + " registros");
            
        } catch (Exception e) {
            log.error("Error cargando datos de VeriFacTur", e);
        }
    }

    @FXML
    public void onEnviar() {
        log.info("Enviando facturas a VeriFacTur...");
        
        try {
            List<Factura> facturas = facturaService.findAll();
            int enviadas = 0;
            
            for (Factura factura : facturas) {
                if (!"EMITIDA".equals(factura.getEstado())) {
                    continue;
                }
                
                Map<String, Object> resultado = verifacturService.enviarFactura(factura);
                
                if ((boolean) resultado.get("exito")) {
                    enviadas++;
                    log.info("Factura {} enviada a VeriFacTur", factura.getNumero());
                }
            }
            
            mostrarExito("Se enviaron " + enviadas + " facturas a VeriFacTur");
            cargarDatos();
            
        } catch (Exception e) {
            log.error("Error enviando a VeriFacTur", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        cargarDatos();
    }

    private void mostrarExito(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

