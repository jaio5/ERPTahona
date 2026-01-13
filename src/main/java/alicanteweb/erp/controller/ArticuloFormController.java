package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

/**
 * Controlador para el formulario de creación/edición de artículos
 */
@Controller
public class ArticuloFormController {
    private static final Logger log = LoggerFactory.getLogger(ArticuloFormController.class);

    // Header
    @FXML private Label lblTitulo;

    // Datos principales
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCodigoBarras;
    @FXML private TextArea txtDescripcion;

    // Clasificación
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbFamilia;
    @FXML private ComboBox<String> cbUnidadMedida;

    // Precios e impuestos
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtMargen;
    @FXML private ComboBox<String> cbIva;  // Cambiado de cbIVA a cbIva para coincidir con FXML

    // Stock
    @FXML private TextField txtStockMinimo;
    @FXML private TextField txtStockMaximo;
    @FXML private TextField txtStockActual;
    @FXML private TextField txtPuntoPedido;
    @FXML private CheckBox chkControlStock;

    // Estado
    @FXML private CheckBox chkActivo;

    private final ArticuloService articuloService;
    private Articulo articuloActual;
    private boolean modoEdicion = false;
    private boolean actualizandoPrecios = false; // Flag para evitar loops infinitos

    public ArticuloFormController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("✅ ArticuloFormController inicializado");

        configurarCategorias();
        configurarFamilias();
        configurarUnidadesMedida();
        configurarIVA();
        configurarValidaciones();
        configurarCalculoAutomatico();
    }

    private void configurarCategorias() {
        if (cbCategoria != null) {
            cbCategoria.getItems().addAll(
                "Materia Prima",
                "Producto Terminado",
                "Envases",
                "Material Auxiliar",
                "Mercadería",
                "Otros"
            );
            cbCategoria.setValue("Producto Terminado");
        }
    }

    private void configurarFamilias() {
        if (cbFamilia != null) {
            cbFamilia.getItems().addAll(
                "Pan",
                "Bollería",
                "Pastelería",
                "Dulces",
                "Salados",
                "Bebidas",
                "Otros"
            );
            cbFamilia.setValue("Pan");
        }
    }

    private void configurarUnidadesMedida() {
        if (cbUnidadMedida != null) {
            cbUnidadMedida.getItems().addAll(
                "Unidad",
                "Kg",
                "g",
                "Litro",
                "ml",
                "Docena",
                "Paquete",
                "Caja"
            );
            cbUnidadMedida.setValue("Unidad");
        }
    }

    private void configurarIVA() {
        if (cbIva != null) {
            cbIva.getItems().addAll(
                "4%",   // Superreducido (pan, leche, etc.)
                "10%",  // Reducido (algunos alimentos)
                "21%"   // General
            );
            cbIva.setValue("4%");
        }
    }

    private void configurarValidaciones() {
        // Validar campos numéricos
        validarCampoNumerico(txtPrecioCompra);
        validarCampoNumerico(txtPrecioVenta);
        validarCampoNumerico(txtMargen);
        validarCampoNumerico(txtStockMinimo);
        validarCampoNumerico(txtStockMaximo);
        validarCampoNumerico(txtStockActual);
        validarCampoNumerico(txtPuntoPedido);
    }

    private void validarCampoNumerico(TextField campo) {
        if (campo != null) {
            campo.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isEmpty()) {
                    return; // Permitir campo vacío
                }

                // Permitir: números, punto decimal
                // Regex: dígitos opcionales, punto opcional, dígitos opcionales
                if (!newVal.matches("\\d*\\.?\\d*")) {
                    Platform.runLater(() -> {
                        int caretPos = campo.getCaretPosition();
                        campo.setText(oldVal);
                        campo.positionCaret(Math.min(caretPos - 1, campo.getText().length()));
                    });
                    return;
                }

                // Validar que no haya más de un punto
                long puntos = newVal.chars().filter(ch -> ch == '.').count();
                if (puntos > 1) {
                    Platform.runLater(() -> {
                        int caretPos = campo.getCaretPosition();
                        campo.setText(oldVal);
                        campo.positionCaret(Math.min(caretPos - 1, campo.getText().length()));
                    });
                    return;
                }

                // Validar que no tenga más de 2 decimales
                if (newVal.contains(".")) {
                    String[] partes = newVal.split("\\.");
                    if (partes.length > 1 && partes[1].length() > 2) {
                        Platform.runLater(() -> {
                            int caretPos = campo.getCaretPosition();
                            campo.setText(oldVal);
                            campo.positionCaret(Math.min(caretPos - 1, campo.getText().length()));
                        });
                    }
                }
            });
        }
    }

    private void configurarCalculoAutomatico() {
        // Calcular margen automáticamente solo cuando el usuario termina de escribir
        if (txtPrecioCompra != null && txtPrecioVenta != null && txtMargen != null) {

            // Listener para precio de compra - calcular margen cuando cambia
            txtPrecioCompra.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) { // Cuando pierde el foco
                    calcularMargen();
                }
            });

            // Listener para precio de venta - calcular margen cuando cambia
            txtPrecioVenta.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) { // Cuando pierde el foco
                    calcularMargen();
                }
            });

            // Calcular precio venta desde margen solo cuando pierde el foco
            txtMargen.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && !txtMargen.getText().isEmpty() && !txtPrecioCompra.getText().isEmpty()) {
                    try {
                        double compra = Double.parseDouble(txtPrecioCompra.getText());
                        double margen = Double.parseDouble(txtMargen.getText());
                        double venta = compra * (1 + margen / 100);

                        actualizandoPrecios = true;
                        txtPrecioVenta.setText(String.format("%.2f", venta));
                        actualizandoPrecios = false;
                    } catch (Exception e) {
                        // Ignorar errores de formato
                    }
                }
            });
        }
    }

    private void calcularMargen() {
        if (actualizandoPrecios) {
            return; // No calcular si estamos actualizando precios automáticamente
        }

        try {
            String precioCompraText = txtPrecioCompra.getText();
            String precioVentaText = txtPrecioVenta.getText();

            if (precioCompraText != null && !precioCompraText.isEmpty() &&
                precioVentaText != null && !precioVentaText.isEmpty()) {

                double compra = Double.parseDouble(precioCompraText);
                double venta = Double.parseDouble(precioVentaText);

                if (compra > 0) {
                    double margen = ((venta - compra) / compra) * 100;

                    actualizandoPrecios = true;
                    txtMargen.setText(String.format("%.2f", margen));
                    actualizandoPrecios = false;
                }
            }
        } catch (NumberFormatException e) {
            // Ignorar errores de formato mientras el usuario escribe
        }
    }

    public void setArticulo(Articulo articulo) {
        this.articuloActual = articulo;
        this.modoEdicion = (articulo != null && articulo.getId() != null);

        Platform.runLater(() -> {
            if (modoEdicion) {
                lblTitulo.setText("Editar Artículo");
                cargarDatosArticulo(articulo);
            } else {
                lblTitulo.setText("Nuevo Artículo");
                limpiarFormulario();
                generarCodigoAutomatico();
            }
        });
    }

    private void cargarDatosArticulo(Articulo articulo) {
        txtCodigo.setText(articulo.getCodigo());
        txtNombre.setText(articulo.getNombre() != null ? articulo.getNombre() : "");
        txtCodigoBarras.setText(articulo.getCodigoBarras() != null ? articulo.getCodigoBarras() : "");
        txtDescripcion.setText(articulo.getDescripcion() != null ? articulo.getDescripcion() : "");

        if (articulo.getCategoria() != null && cbCategoria.getItems().contains(articulo.getCategoria())) {
            cbCategoria.setValue(articulo.getCategoria());
        }

        if (articulo.getFamilia() != null && cbFamilia.getItems().contains(articulo.getFamilia())) {
            cbFamilia.setValue(articulo.getFamilia());
        }

        if (articulo.getUnidad() != null && cbUnidadMedida.getItems().contains(articulo.getUnidad())) {
            cbUnidadMedida.setValue(articulo.getUnidad());
        }

        txtPrecioCompra.setText(articulo.getCoste() != null ? articulo.getCoste().toString() : "0.00");
        txtPrecioVenta.setText(articulo.getPvp() != null ? articulo.getPvp().toString() : "0.00");

        if (articulo.getIva() != null) {
            String ivaStr = articulo.getIva().intValue() + "%";
            if (cbIva.getItems().contains(ivaStr)) {
                cbIva.setValue(ivaStr);
            }
        }

        txtStockMinimo.setText(articulo.getStockMinimo() != null ? articulo.getStockMinimo().toString() : "0");
        txtStockMaximo.setText(articulo.getStockMaximo() != null ? articulo.getStockMaximo().toString() : "0");
        txtStockActual.setText(articulo.getStock() != null ? articulo.getStock().toString() : "0");
        // txtPuntoPedido.setText(articulo.getPuntoPedido() != null ? articulo.getPuntoPedido().toString() : "0");
        // Campo puntoPedido no existe en la entidad Articulo

        chkControlStock.setSelected(articulo.getControlStock() != null && articulo.getControlStock());
        chkActivo.setSelected(articulo.getActivo() != null ? articulo.getActivo() : true);

        log.info("Datos cargados para artículo: {}", articulo.getId());
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCodigoBarras.clear();
        txtDescripcion.clear();
        cbCategoria.setValue("Producto Terminado");
        cbFamilia.setValue("Pan");
        cbUnidadMedida.setValue("Unidad");
        txtPrecioCompra.setText("0.00");
        txtPrecioVenta.setText("0.00");
        txtMargen.setText("0.00");
        cbIva.setValue("4%");
        txtStockMinimo.setText("0");
        txtStockMaximo.setText("0");
        txtStockActual.setText("0");
        txtPuntoPedido.setText("0");
        chkControlStock.setSelected(true);
        chkActivo.setSelected(true);
    }

    private void generarCodigoAutomatico() {
        try {
            long count = articuloService.findAll().size();
            long nextId = count + 1;
            String codigo = String.format("ART%04d", nextId);
            txtCodigo.setText(codigo);
        } catch (Exception e) {
            log.warn("No se pudo generar código automático", e);
            txtCodigo.setText("ART0001");
        }
    }

    @FXML
    public void onGuardar() {
        if (!validarFormulario()) {
            return;
        }

        try {
            if (articuloActual == null) {
                articuloActual = new Articulo();
            }

            // Datos principales
            articuloActual.setCodigo(txtCodigo.getText().trim());
            articuloActual.setNombre(txtNombre.getText().trim());
            articuloActual.setCodigoBarras(txtCodigoBarras.getText().trim());
            articuloActual.setDescripcion(txtDescripcion.getText().trim());

            // Clasificación
            articuloActual.setCategoria(cbCategoria.getValue());
            articuloActual.setFamilia(cbFamilia.getValue());
            articuloActual.setUnidad(cbUnidadMedida.getValue());

            // Precios
            articuloActual.setCoste(new BigDecimal(txtPrecioCompra.getText()));
            articuloActual.setPvp(new BigDecimal(txtPrecioVenta.getText()));

            // IVA - con validación para null
            if (cbIva.getValue() != null && !cbIva.getValue().isEmpty()) {
                String ivaStr = cbIva.getValue().replace("%", "");
                articuloActual.setIva(new BigDecimal(ivaStr));
            } else {
                // Valor por defecto si no se selecciona
                articuloActual.setIva(new BigDecimal("4"));
                log.warn("IVA no seleccionado, usando valor por defecto: 4%");
            }

            // Stock
            articuloActual.setStockMinimo(new BigDecimal(txtStockMinimo.getText()));
            articuloActual.setStockMaximo(new BigDecimal(txtStockMaximo.getText()));
            articuloActual.setStock(new BigDecimal(txtStockActual.getText()));
            // articuloActual.setPuntoPedido(new BigDecimal(txtPuntoPedido.getText()));
            // Campo puntoPedido no existe en la entidad Articulo
            articuloActual.setControlStock(chkControlStock.isSelected());

            // Estado
            articuloActual.setActivo(chkActivo.isSelected());

            // Guardar
            Articulo guardado = articuloService.save(articuloActual);
            log.info("✅ Artículo guardado: {} - {}", guardado.getId(), guardado.getNombre());

            mostrarExito(modoEdicion ?
                "Artículo actualizado correctamente" :
                "Artículo creado correctamente");

            cerrarVentana();

        } catch (Exception e) {
            log.error("❌ Error guardando artículo", e);
            mostrarError("Error al guardar el artículo: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        if (formularioModificado()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar");
            confirmacion.setHeaderText("¿Descartar cambios?");
            confirmacion.setContentText("Hay cambios sin guardar. ¿Desea salir sin guardar?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                cerrarVentana();
            }
        } else {
            cerrarVentana();
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar campos obligatorios
        if (txtCodigo.getText().trim().isEmpty()) {
            errores.append("• El código es obligatorio\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }

        // Validar precios
        try {
            BigDecimal precioCompra = new BigDecimal(txtPrecioCompra.getText());
            if (precioCompra.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("• El precio de compra no puede ser negativo\n");
            }
        } catch (Exception e) {
            errores.append("• El precio de compra no es válido\n");
        }

        try {
            BigDecimal precioVenta = new BigDecimal(txtPrecioVenta.getText());
            if (precioVenta.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("• El precio de venta no puede ser negativo\n");
            }
        } catch (Exception e) {
            errores.append("• El precio de venta no es válido\n");
        }

        // Validar stock
        try {
            BigDecimal stockMin = new BigDecimal(txtStockMinimo.getText());
            BigDecimal stockMax = new BigDecimal(txtStockMaximo.getText());

            if (stockMin.compareTo(BigDecimal.ZERO) < 0) {
                errores.append("• El stock mínimo no puede ser negativo\n");
            }

            if (stockMax.compareTo(stockMin) < 0) {
                errores.append("• El stock máximo debe ser mayor que el mínimo\n");
            }
        } catch (Exception e) {
            errores.append("• Los valores de stock no son válidos\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Por favor, corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private boolean formularioModificado() {
        return !txtNombre.getText().trim().isEmpty() ||
               !txtCodigo.getText().trim().isEmpty();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atención");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarExito(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void mostrarError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}

