package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

/**
 * Controlador para el formulario de creación/edición de artículos
 */
@Controller
public class ArticuloFormController {
    private static final Logger log = LoggerFactory.getLogger(ArticuloFormController.class);

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtFamilia;
    @FXML private TextField txtUnidad;
    @FXML private TextField txtPVP;
    @FXML private TextField txtCoste;
    @FXML private TextField txtIVA;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final ArticuloService articuloService;
    private Articulo articuloActual;

    public ArticuloFormController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("ArticuloFormController inicializado");
    }

    public void setArticulo(Articulo articulo) {
        this.articuloActual = articulo;
        if (articulo != null) {
            txtCodigo.setText(articulo.getCodigo());
            txtDescripcion.setText(articulo.getDescripcion());
            if (articulo.getFamilia() != null) {
                txtFamilia.setText(articulo.getFamilia());
            }
            if (articulo.getUnidad() != null) {
                txtUnidad.setText(articulo.getUnidad());
            }
            if (articulo.getPvp() != null) {
                txtPVP.setText(articulo.getPvp().toString());
            }
            if (articulo.getCoste() != null) {
                txtCoste.setText(articulo.getCoste().toString());
            }
            if (articulo.getIva() != null) {
                txtIVA.setText(articulo.getIva().toString());
            }
        }
    }

    @FXML
    public void onGuardar() {
        if (validar()) {
            try {
                if (articuloActual == null) {
                    articuloActual = new Articulo();
                }

                articuloActual.setCodigo(txtCodigo.getText());
                articuloActual.setDescripcion(txtDescripcion.getText());
                articuloActual.setFamilia(txtFamilia.getText());
                articuloActual.setUnidad(txtUnidad.getText());
                articuloActual.setPvp(new BigDecimal(txtPVP.getText()));
                articuloActual.setCoste(new BigDecimal(txtCoste.getText()));
                articuloActual.setIva(new BigDecimal(txtIVA.getText()));

                articuloService.save(articuloActual);
                log.info("Artículo guardado: {}", articuloActual.getId());

                mostrarExito("Artículo guardado correctamente");

                btnCancelar.getScene().getWindow().hide();
            } catch (Exception e) {
                log.error("Error guardando artículo", e);
                mostrarError("Error al guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onCancelar() {
        btnCancelar.getScene().getWindow().hide();
    }

    private boolean validar() {
        if (txtCodigo.getText().isEmpty() || txtDescripcion.getText().isEmpty()) {
            mostrarAlerta("Código y descripción son obligatorios");
            return false;
        }
        try {
            new BigDecimal(txtPVP.getText());
            new BigDecimal(txtCoste.getText());
            new BigDecimal(txtIVA.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("PVP, costo e IVA deben ser números válidos");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(msg);
        alert.showAndWait();
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

