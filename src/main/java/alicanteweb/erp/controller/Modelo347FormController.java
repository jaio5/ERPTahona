package alicanteweb.erp.controller;

import alicanteweb.erp.service.Modelo347Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import alicanteweb.erp.ui.DialogUtils;
import alicanteweb.erp.entities.Modelo347Registro;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para el formulario del Modelo 347
 */
@Controller
public class Modelo347FormController {
    private static final Logger log = LoggerFactory.getLogger(Modelo347FormController.class);

    // Campos vinculados desde modelo347_form.fxml (añadidos para resolver unresolved fx:id)
    @FXML private Label lblEstado;
    // Ajustado a los fx:id reales del FXML
    @FXML private TextField txtEjercicio; // antes cmbEjercicio
    @FXML private ComboBox<String> cbTipo; // antes cbTipo similar
    @FXML private TextField txtNumeroIdentificacion; // antes txtNumeroIdentificacion

    @FXML private Label txtTotalIngresos;
    @FXML private Label txtTotalGastos;
    @FXML private Label lblNumeroOperaciones;

    @FXML private TableView<Modelo347Registro> tableOperaciones;
    @FXML private TableColumn<Modelo347Registro, String> colNIF;
    @FXML private TableColumn<Modelo347Registro, String> colTercero;
    @FXML private TableColumn<Modelo347Registro, String> colTipo;
    @FXML private TableColumn<Modelo347Registro, String> colImporte;
    @FXML private TableColumn<Modelo347Registro, String> colTrimestres;

    @FXML private TextArea txtObservaciones;

    private final Modelo347Service modelo347Service;
    private final ObservableList<Modelo347Registro> registros = FXCollections.observableArrayList();

    public Modelo347FormController(Modelo347Service modelo347Service) {
        this.modelo347Service = modelo347Service;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando Modelo347FormController");

        // Si el campo es TextField para el ejercicio, poner valor por defecto
        if (txtEjercicio != null) {
            int anioActual = java.time.LocalDate.now().getYear();
            txtEjercicio.setText(String.valueOf(anioActual - 1));
        }

        // El resto de inicializaciones mínimas para evitar warnings
        if (cbTipo != null) {
            cbTipo.getItems().addAll("Ordinaria", "Complementaria", "Sustitutiva");
        }

        // Asegurar que los labels y la tabla referenciados en el FXML se usan
        if (txtTotalIngresos != null && (txtTotalIngresos.getText() == null || txtTotalIngresos.getText().isEmpty())) {
            txtTotalIngresos.setText("0.00 €");
        }
        if (txtTotalGastos != null && (txtTotalGastos.getText() == null || txtTotalGastos.getText().isEmpty())) {
            txtTotalGastos.setText("0.00 €");
        }
        if (lblNumeroOperaciones != null && (lblNumeroOperaciones.getText() == null || lblNumeroOperaciones.getText().isEmpty())) {
            lblNumeroOperaciones.setText("0");
        }
        if (tableOperaciones != null) {
            if (tableOperaciones.getPlaceholder() == null) {
                tableOperaciones.setPlaceholder(new Label("No hay operaciones"));
            }
            // Configurar columnas si existen (uso de PropertyValueFactory para evitar warnings de campos sin uso)
            if (colNIF != null) colNIF.setCellValueFactory(new PropertyValueFactory<>("nifDeclarado"));
            if (colTercero != null) colTercero.setCellValueFactory(new PropertyValueFactory<>("nombreDeclarado"));
            if (colTipo != null) colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoOperacion"));
            if (colImporte != null) colImporte.setCellValueFactory(new PropertyValueFactory<>("importeTotal"));
            if (colTrimestres != null) colTrimestres.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatearTrimestres(cellData.getValue())));

            // Inicializar con lista vacía para que FXCollections sea usado y no exista import sin uso
            tableOperaciones.setItems(registros);
        }
        if (txtObservaciones != null && (txtObservaciones.getText() == null)) {
            txtObservaciones.setText("");
        }
        // Utilizar txtNumeroIdentificacion para evitar advertencia de campo sin uso
        if (txtNumeroIdentificacion != null && (txtNumeroIdentificacion.getText() == null || txtNumeroIdentificacion.getText().isEmpty())) {
            txtNumeroIdentificacion.setText("");
            txtNumeroIdentificacion.setPromptText("(opcional)");
        }

        // Usar lblEstado para evitar warning 'assigned but never accessed'
        if (lblEstado != null && (lblEstado.getText() == null || lblEstado.getText().isEmpty())) {
            lblEstado.setText("PENDIENTE");
        }
    }

    @FXML
    public void onGenerar() {
        try {
            if (!validarFormulario()) return;

            int ejercicio;
            try {
                ejercicio = Integer.parseInt(txtEjercicio.getText());
            } catch (Exception e) {
                mostrarError("Ejercicio inválido");
                return;
            }

            log.info("Generando Modelo 347 para ejercicio {}", ejercicio);

            // Generar el modelo
            cargarResultado(modelo347Service.generarModelo347(ejercicio));

            mostrarExito("Modelo 347 generado correctamente para el ejercicio " + ejercicio);

        } catch (Exception e) {
            log.error("Error generando Modelo 347", e);
            mostrarError("Error al generar: " + e.getMessage());
        }
    }

    @FXML
    public void onCalcular() {
        try {
            // Llamada al servicio para calcular resumen; si no se desea lógica aquí, dejar como no-op
            if (txtEjercicio != null && !txtEjercicio.getText().isEmpty()) {
                int ej;
                try {
                    ej = Integer.parseInt(txtEjercicio.getText());
                } catch (Exception ex) {
                    log.warn("Ejercicio inválido en onCalcular", ex);
                    return;
                }
                cargarResultado(modelo347Service.generarModelo347(ej));
            }
        } catch (Exception e) {
            log.warn("onCalcular: error al calcular modelo 347", e);
        }
    }

    @FXML
    public void onImportarXML() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Modelo 347 XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        Window owner = txtEjercicio != null && txtEjercicio.getScene() != null ? txtEjercicio.getScene().getWindow() : null;
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return;
        }

        try {
            List<Modelo347Registro> importados = importarRegistrosXml(file);
            registros.setAll(importados);
            actualizarResumen();
            if (lblEstado != null) {
                lblEstado.setText("IMPORTADO");
            }
            DialogUtils.showInfo("Importados " + importados.size() + " registros del Modelo 347");
            return;
        } catch (Exception e) {
            log.error("Error importando XML Modelo 347", e);
            DialogUtils.showError("No se pudo importar el XML: " + e.getMessage());
            return;
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (txtEjercicio == null || txtEjercicio.getText() == null || txtEjercicio.getText().isEmpty()) {
            mostrarError("Debe seleccionar un ejercicio");
            return false;
        }
        // validación adicional opcional
        return true;
    }

    private void cargarResultado(Modelo347Service.Modelo347Result resultado) {
        List<Modelo347Registro> nuevos = new ArrayList<>();
        for (Modelo347Service.OperacionTercero operacion : resultado.operaciones()) {
            Modelo347Registro registro = new Modelo347Registro();
            registro.setEjercicio(resultado.ejercicio());
            registro.setNifDeclarado(operacion.nif());
            registro.setNombreDeclarado(operacion.nombre());
            registro.setTipoOperacion("CLIENTE".equals(operacion.tipo()) ? "B" : "A");
            registro.setClaveOperacion(registro.getTipoOperacion());
            registro.setEsCliente("CLIENTE".equals(operacion.tipo()));
            registro.setEsProveedor("PROVEEDOR".equals(operacion.tipo()));
            registro.setImporteTotal(operacion.totalDeclarar());
            registro.setImporteAnual(operacion.totalDeclarar());
            registro.setNumeroOperaciones(1);
            registro.setGenerado(true);
            nuevos.add(registro);
        }
        registros.setAll(nuevos);
        actualizarResumen();
        if (lblEstado != null) {
            lblEstado.setText("GENERADO");
        }
    }

    private List<Modelo347Registro> importarRegistrosXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);

        Document document = factory.newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();

        List<Modelo347Registro> importados = new ArrayList<>();
        var nodes = document.getElementsByTagName("*");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }

            String nif = texto(element, "nifDeclarado", "NifDeclarado", "NIFDeclarado", "NIF", "Nif");
            String nombre = texto(element, "nombreDeclarado", "NombreDeclarado", "Nombre", "RazonSocial", "RazonSocialDeclarado");
            String importe = texto(element, "importeTotal", "ImporteTotal", "ImporteOperaciones", "ImporteAnual", "Importe");
            if (nif.isBlank() || nombre.isBlank() || importe.isBlank()) {
                continue;
            }

            Modelo347Registro registro = new Modelo347Registro();
            registro.setEjercicio(parseEjercicio(texto(element, "ejercicio", "Ejercicio")));
            registro.setNifDeclarado(nif);
            registro.setNombreDeclarado(nombre);
            registro.setTipoOperacion(valorONull(texto(element, "tipoOperacion", "TipoOperacion", "ClaveOperacion"), "B"));
            registro.setClaveOperacion(registro.getTipoOperacion());
            registro.setEsCliente("B".equalsIgnoreCase(registro.getTipoOperacion()));
            registro.setEsProveedor("A".equalsIgnoreCase(registro.getTipoOperacion()));
            registro.setImporteTotal(parseImporte(importe));
            registro.setImporteAnual(registro.getImporteTotal());
            registro.setImporteT1(parseImporte(texto(element, "importeT1", "ImporteT1", "ImportePrimerTrimestre")));
            registro.setImporteT2(parseImporte(texto(element, "importeT2", "ImporteT2", "ImporteSegundoTrimestre")));
            registro.setImporteT3(parseImporte(texto(element, "importeT3", "ImporteT3", "ImporteTercerTrimestre")));
            registro.setImporteT4(parseImporte(texto(element, "importeT4", "ImporteT4", "ImporteCuartoTrimestre")));
            registro.setNumeroOperaciones(1);
            importados.add(registro);
        }
        return importados;
    }

    private void actualizarResumen() {
        BigDecimal ingresos = registros.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEsCliente()) || "B".equalsIgnoreCase(r.getTipoOperacion()))
                .map(Modelo347Registro::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gastos = registros.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEsProveedor()) || "A".equalsIgnoreCase(r.getTipoOperacion()))
                .map(Modelo347Registro::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (txtTotalIngresos != null) txtTotalIngresos.setText(ingresos + " EUR");
        if (txtTotalGastos != null) txtTotalGastos.setText(gastos + " EUR");
        if (lblNumeroOperaciones != null) lblNumeroOperaciones.setText(String.valueOf(registros.size()));
    }

    private String texto(Element parent, String... tagNames) {
        for (String tagName : tagNames) {
            var nodes = parent.getElementsByTagName(tagName);
            if (nodes.getLength() > 0 && nodes.item(0).getTextContent() != null) {
                String value = nodes.item(0).getTextContent().trim();
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return "";
    }

    private String valorONull(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private int parseEjercicio(String value) {
        if (value == null || value.isBlank()) {
            return Integer.parseInt(txtEjercicio.getText());
        }
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseImporte(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        String normalizado = value.trim();
        if (normalizado.contains(",") && normalizado.contains(".")) {
            normalizado = normalizado.replace(".", "").replace(",", ".");
        } else if (normalizado.contains(",")) {
            normalizado = normalizado.replace(",", ".");
        }
        return new BigDecimal(normalizado);
    }

    private String formatearTrimestres(Modelo347Registro registro) {
        return "T1 " + registro.getImporteT1()
                + " | T2 " + registro.getImporteT2()
                + " | T3 " + registro.getImporteT3()
                + " | T4 " + registro.getImporteT4();
    }

    private void cerrarVentana() {
        if (txtEjercicio != null && txtEjercicio.getScene() != null) {
            Stage stage = (Stage) txtEjercicio.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarError(String mensaje) {
        try {
            DialogUtils.showError(mensaje);
        } catch (Throwable t) {
            log.error("Error: {}", mensaje);
        }
    }

    private void mostrarExito(String mensaje) {
        try {
            DialogUtils.showInfo(mensaje);
        } catch (Throwable t) {
            log.info("Exito: {}", mensaje);
        }
    }
}
