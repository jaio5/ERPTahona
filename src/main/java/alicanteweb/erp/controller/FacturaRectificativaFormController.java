package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.FacturaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para crear facturas rectificativas
 * Implementa requisitos legales del RD 1619/2012
 */
@Controller
public class FacturaRectificativaFormController {
    private static final Logger log = LoggerFactory.getLogger(FacturaRectificativaFormController.class);

    @FXML private Label lblTitulo;

    // Factura original
    @FXML private ComboBox<Factura> cbFacturaOriginal;
    @FXML private Label lblNumeroOriginal;
    @FXML private Label lblFechaOriginal;
    @FXML private Label lblClienteOriginal;
    @FXML private Label lblTotalOriginal;

    // Datos de rectificación
    @FXML private ComboBox<String> cbTipoRectificacion;
    @FXML private TextArea txtMotivo;
    @FXML private DatePicker dpFechaRectificativa;

    // Datos ajustados (si es tipo DIFERENCIAS)
    @FXML private TextField txtNuevaBaseImponible;
    @FXML private TextField txtNuevoIva;
    @FXML private TextField txtNuevoTotal;
    @FXML private Label lblDiferenciaBase;
    @FXML private Label lblDiferenciaIva;
    @FXML private Label lblDiferenciaTotal;

    private final FacturaService facturaService;
    private Factura facturaOriginal;

    public FacturaRectificativaFormController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @FXML
    public void initialize() {
        log.info("✅ FacturaRectificativaFormController inicializado");

        configurarTiposRectificacion();
        cargarFacturasEmitidas();
        configurarListeners();

        // Fecha por defecto: hoy
        dpFechaRectificativa.setValue(LocalDate.now());
    }

    private void configurarTiposRectificacion() {
        if (cbTipoRectificacion != null) {
            cbTipoRectificacion.getItems().addAll(
                "SUSTITUCION",  // Anula la original y crea una nueva
                "DIFERENCIAS"   // Solo ajusta las diferencias
            );
            cbTipoRectificacion.setValue("DIFERENCIAS");
        }
    }

    private void cargarFacturasEmitidas() {
        if (cbFacturaOriginal != null) {
            List<Factura> facturas = facturaService.findAll().stream()
                .filter(f -> "EMITIDA".equals(f.getEstado()) || "PAGADA".equals(f.getEstado()))
                .filter(f -> !"RECTIFICATIVA".equals(f.getTipoFactura()))
                .toList();

            cbFacturaOriginal.getItems().addAll(facturas);

            // Personalizar visualización
            cbFacturaOriginal.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Factura item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String clienteNombre = item.getCliente() != null ?
                            item.getCliente().getNombre() : "Sin cliente";
                        setText(String.format("%s - %s - %.2f EUR - %s",
                            item.getNumero(),
                            item.getFecha(),
                            item.getTotal(),
                            clienteNombre));
                    }
                }
            });

            cbFacturaOriginal.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Factura item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNumero());
                    }
                }
            });
        }
    }

    private void configurarListeners() {
        // Al seleccionar factura original
        if (cbFacturaOriginal != null) {
            cbFacturaOriginal.valueProperty().addListener((obs, old, newValue) -> {
                if (newValue != null) {
                    cargarDatosFacturaOriginal(newValue);
                }
            });
        }

        // Calcular diferencias al cambiar valores
        if (txtNuevaBaseImponible != null) {
            txtNuevaBaseImponible.textProperty().addListener((obs, old, newVal) -> calcularDiferencias());
        }
        if (txtNuevoIva != null) {
            txtNuevoIva.textProperty().addListener((obs, old, newVal) -> calcularDiferencias());
        }
        if (txtNuevoTotal != null) {
            txtNuevoTotal.textProperty().addListener((obs, old, newVal) -> calcularDiferencias());
        }
    }

    private void cargarDatosFacturaOriginal(Factura factura) {
        this.facturaOriginal = factura;

        Platform.runLater(() -> {
            lblNumeroOriginal.setText(factura.getNumero());
            lblFechaOriginal.setText(factura.getFecha() != null ?
                factura.getFecha().toString() : "-");
            lblClienteOriginal.setText(factura.getCliente() != null ?
                factura.getCliente().getNombre() : "Sin cliente");
            lblTotalOriginal.setText(String.format("%.2f EUR", factura.getTotal()));

            // Pre-cargar valores actuales
            txtNuevaBaseImponible.setText(factura.getBaseImponible() != null ?
                factura.getBaseImponible().toString() : "0.00");
            txtNuevoIva.setText(factura.getTotalIva() != null ?
                factura.getTotalIva().toString() : "0.00");
            txtNuevoTotal.setText(factura.getTotal() != null ?
                factura.getTotal().toString() : "0.00");

            log.info("Factura original cargada: {}", factura.getNumero());
        });
    }

    private void calcularDiferencias() {
        if (facturaOriginal == null) return;

        try {
            double originalBase = facturaOriginal.getBaseImponible() != null ?
                facturaOriginal.getBaseImponible().doubleValue() : 0.0;
            double originalIva = facturaOriginal.getTotalIva() != null ?
                facturaOriginal.getTotalIva().doubleValue() : 0.0;
            double originalTotal = facturaOriginal.getTotal() != null ?
                facturaOriginal.getTotal().doubleValue() : 0.0;

            double nuevaBase = txtNuevaBaseImponible.getText().isEmpty() ? 0.0 :
                Double.parseDouble(txtNuevaBaseImponible.getText());
            double nuevoIva = txtNuevoIva.getText().isEmpty() ? 0.0 :
                Double.parseDouble(txtNuevoIva.getText());
            double nuevoTotal = txtNuevoTotal.getText().isEmpty() ? 0.0 :
                Double.parseDouble(txtNuevoTotal.getText());

            double difBase = nuevaBase - originalBase;
            double difIva = nuevoIva - originalIva;
            double difTotal = nuevoTotal - originalTotal;

            lblDiferenciaBase.setText(String.format("Diferencia: %.2f EUR", difBase));
            lblDiferenciaIva.setText(String.format("Diferencia: %.2f EUR", difIva));
            lblDiferenciaTotal.setText(String.format("Diferencia: %.2f EUR", difTotal));

            // Colorear según sea positivo o negativo
            lblDiferenciaBase.setStyle(difBase >= 0 ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            lblDiferenciaIva.setStyle(difIva >= 0 ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            lblDiferenciaTotal.setStyle(difTotal >= 0 ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

        } catch (NumberFormatException e) {
            // Ignorar errores de formato mientras se escribe
        }
    }

    @FXML
    public void onCrearRectificativa() {
        if (!validarFormulario()) {
            return;
        }

        try {
            // Confirmar acción
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Factura Rectificativa");
            confirmacion.setHeaderText("¿Crear factura rectificativa?");
            confirmacion.setContentText(
                "Factura original: " + facturaOriginal.getNumero() + "\n" +
                "Tipo: " + cbTipoRectificacion.getValue() + "\n" +
                "Motivo: " + txtMotivo.getText() + "\n\n" +
                "Esta acción creará una nueva factura rectificativa."
            );

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            // Crear factura rectificativa
            Factura rectificativa = new Factura();

            // Datos básicos
            rectificativa.setCliente(facturaOriginal.getCliente());
            rectificativa.setFecha(dpFechaRectificativa.getValue());
            rectificativa.setTipoFactura("RECTIFICATIVA");
            rectificativa.setEstado("EMITIDA");

            // Generar número (R- prefijo para rectificativas)
            String numeroRectificativa = generarNumeroRectificativa();
            rectificativa.setNumero(numeroRectificativa);

            // Datos de rectificación
            rectificativa.setFacturaRectificadaNumero(facturaOriginal.getNumero());
            rectificativa.setFacturaRectificadaFecha(facturaOriginal.getFecha());
            rectificativa.setMotivoRectificacion(txtMotivo.getText());
            rectificativa.setTipoRectificacion(cbTipoRectificacion.getValue());

            // Importes según tipo de rectificación
            if ("SUSTITUCION".equals(cbTipoRectificacion.getValue())) {
                // Anula la original (valores negativos)
                rectificativa.setBaseImponible(facturaOriginal.getBaseImponible().negate());
                rectificativa.setTotalIva(facturaOriginal.getTotalIva().negate());
                rectificativa.setTotal(facturaOriginal.getTotal().negate());
            } else {
                // Solo las diferencias
                double difBase = Double.parseDouble(txtNuevaBaseImponible.getText()) -
                    facturaOriginal.getBaseImponible().doubleValue();
                double difIva = Double.parseDouble(txtNuevoIva.getText()) -
                    facturaOriginal.getTotalIva().doubleValue();
                double difTotal = Double.parseDouble(txtNuevoTotal.getText()) -
                    facturaOriginal.getTotal().doubleValue();

                rectificativa.setBaseImponible(java.math.BigDecimal.valueOf(difBase));
                rectificativa.setTotalIva(java.math.BigDecimal.valueOf(difIva));
                rectificativa.setTotal(java.math.BigDecimal.valueOf(difTotal));
            }

            // Copiar otros campos relevantes
            rectificativa.setSerie(facturaOriginal.getSerie());
            rectificativa.setMedioCobro(facturaOriginal.getMedioCobro());

            // Guardar
            Factura guardada = facturaService.save(rectificativa);
            log.info("✅ Factura rectificativa creada: {} para factura {}",
                guardada.getNumero(), facturaOriginal.getNumero());

            // Actualizar estado de factura original
            if ("SUSTITUCION".equals(cbTipoRectificacion.getValue())) {
                facturaOriginal.setEstado("ANULADA");
                facturaService.save(facturaOriginal);
                log.info("✅ Factura original anulada: {}", facturaOriginal.getNumero());
            }

            mostrarExito(
                "Factura rectificativa creada correctamente\n\n" +
                "Número: " + guardada.getNumero() + "\n" +
                "Tipo: " + cbTipoRectificacion.getValue() + "\n" +
                "Total: " + guardada.getTotal() + " EUR"
            );

            cerrarVentana();

        } catch (Exception e) {
            log.error("❌ Error creando factura rectificativa", e);
            mostrarError("Error al crear factura rectificativa: " + e.getMessage());
        }
    }

    private String generarNumeroRectificativa() {
        LocalDate hoy = LocalDate.now();
        int año = hoy.getYear();

        // Obtener el último número de rectificativa del año
        List<Factura> rectificativas = facturaService.findAll().stream()
            .filter(f -> "RECTIFICATIVA".equals(f.getTipoFactura()))
            .filter(f -> f.getNumero() != null && f.getNumero().startsWith("R-" + año))
            .toList();

        long numeroMaximo = rectificativas.stream()
            .map(f -> {
                try {
                    String[] partes = f.getNumero().split("-");
                    if (partes.length == 3) {
                        return Long.parseLong(partes[2]);
                    }
                } catch (Exception e) {
                    // Ignorar
                }
                return 0L;
            })
            .max(Long::compareTo)
            .orElse(0L);

        long siguienteNumero = numeroMaximo + 1;
        return String.format("R-%d-%04d", año, siguienteNumero);
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (cbFacturaOriginal.getValue() == null) {
            errores.append("Debe seleccionar una factura original\n");
        }

        if (txtMotivo.getText() == null || txtMotivo.getText().trim().isEmpty()) {
            errores.append("El motivo de rectificación es obligatorio\n");
        }

        if (dpFechaRectificativa.getValue() == null) {
            errores.append("La fecha es obligatoria\n");
        }

        if ("DIFERENCIAS".equals(cbTipoRectificacion.getValue())) {
            if (txtNuevaBaseImponible.getText().isEmpty() ||
                txtNuevoIva.getText().isEmpty() ||
                txtNuevoTotal.getText().isEmpty()) {
                errores.append("Debe especificar los nuevos importes\n");
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta("Por favor, corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atención");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    private void mostrarExito(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
}

