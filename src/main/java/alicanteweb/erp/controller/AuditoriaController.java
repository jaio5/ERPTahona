package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AuditoriaAccion;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.AuditoriaAccionRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import alicanteweb.erp.ui.DialogUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de Auditoría del Sistema
 * Muestra y permite filtrar todos los registros de auditoría
 */
@Controller
public class AuditoriaController {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<AuditoriaAccion> tableAuditoria;
    @FXML private TableColumn<AuditoriaAccion, String> colFecha;
    @FXML private TableColumn<AuditoriaAccion, String> colUsuario;
    @FXML private TableColumn<AuditoriaAccion, String> colAccion;
    @FXML private TableColumn<AuditoriaAccion, String> colModulo;
    @FXML private TableColumn<AuditoriaAccion, String> colEntidad;
    @FXML private TableColumn<AuditoriaAccion, String> colDescripcion;
    @FXML private TableColumn<AuditoriaAccion, String> colResultado;
    @FXML private TableColumn<AuditoriaAccion, Void> colAcciones;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbUsuario;
    @FXML private ComboBox<String> cmbAccion;
    @FXML private ComboBox<String> cmbModulo;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;

    @FXML private Label lblTotal;
    @FXML private Label lblEstadisticas;
    @FXML private Label lblSeleccion;

    private final AuditoriaAccionRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private final ObservableList<AuditoriaAccion> auditoriaList = FXCollections.observableArrayList();
    private final ObservableList<AuditoriaAccion> auditoriaFilteredList = FXCollections.observableArrayList();

    public AuditoriaController(AuditoriaAccionRepository auditoriaRepository,
                              UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @FXML
    public void initialize() {
        log.info("✅ Inicializando AuditoriaController");
        configurarColumnas();
        configurarFiltros();
        configurarListeners();
        cargarDatos();
    }

    private void configurarColumnas() {
        // Configurar columnas básicas
        colFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFecha();
            String texto = fecha != null ? fecha.format(DATE_FORMATTER) : "-";
            return new SimpleStringProperty(texto);
        });

        colUsuario.setCellValueFactory(cellData ->
            new SimpleStringProperty(Optional.ofNullable(cellData.getValue().getUsuarioNombre()).orElse("SISTEMA")));

        colAccion.setCellValueFactory(cellData -> {
            String accion = cellData.getValue().getTipoAccion();
            String emoji = getEmojiAccion(accion);
            return new SimpleStringProperty(emoji + " " + accion);
        });

        colModulo.setCellValueFactory(cellData ->
            new SimpleStringProperty(Optional.ofNullable(cellData.getValue().getModulo()).orElse("-")));

        colEntidad.setCellValueFactory(cellData -> {
            String tipo = cellData.getValue().getEntidadTipo();
            String id = cellData.getValue().getEntidadId();
            String texto = tipo != null ? tipo + (id != null ? " #" + id : "") : "-";
            return new SimpleStringProperty(texto);
        });

        colDescripcion.setCellValueFactory(cellData ->
            new SimpleStringProperty(Optional.ofNullable(cellData.getValue().getDescripcion()).orElse("-")));

        colResultado.setCellValueFactory(cellData -> {
            String resultado = cellData.getValue().getResultado();
            String emoji = "EXITO".equals(resultado) ? "✅" :
                          "ERROR".equals(resultado) ? "❌" :
                          "ADVERTENCIA".equals(resultado) ? "⚠️" : "ℹ️";
            return new SimpleStringProperty(emoji + " " + (resultado != null ? resultado : "N/A"));
        });

        // Configurar columna de acciones
        configurarColumnaAcciones();

        tableAuditoria.setItems(auditoriaFilteredList);
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");

            {
                btnVer.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 12; -fx-background-radius: 5;");
                btnVer.setTooltip(new Tooltip("Ver detalles completos"));

                btnVer.setOnAction(e -> {
                    AuditoriaAccion auditoria = getTableRow() != null ? getTableRow().getItem() : null;
                    if (auditoria != null) {
                        mostrarDetalleCompleto(auditoria);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVer);
                }
            }
        });
    }

    private void configurarFiltros() {
        // Cargar usuarios para filtro
        try {
            List<String> usuarios = usuarioRepository.findAll().stream()
                .map(Usuario::getUsername)
                .sorted()
                .collect(Collectors.toList());
            usuarios.add(0, "👤 Todos los usuarios");
            if (cmbUsuario != null) {
                cmbUsuario.setItems(FXCollections.observableArrayList(usuarios));
                cmbUsuario.setValue("👤 Todos los usuarios");
            }
        } catch (Exception e) {
            log.error("Error cargando usuarios para filtro", e);
        }

        // Configurar ComboBox de acciones
        if (cmbAccion != null) {
            cmbAccion.setItems(FXCollections.observableArrayList(
                "⚡ Todas las acciones",
                "➕ CREAR", "✏️ ACTUALIZAR", "🗑️ ELIMINAR", "📖 LEER",
                "🔑 LOGIN", "🚪 LOGOUT", "📤 EXPORTAR", "🖨️ IMPRIMIR",
                "⚠️ ERROR", "✅ EXITO"
            ));
            cmbAccion.setValue("⚡ Todas las acciones");
        }

        // Configurar ComboBox de módulos
        if (cmbModulo != null) {
            cmbModulo.setItems(FXCollections.observableArrayList(
                "📦 Todos los módulos",
                "👥 CLIENTES", "📄 FACTURAS", "📦 ARTICULOS", "🏢 PROVEEDORES",
                "💰 CAJA", "📊 CONTABILIDAD", "📋 ALBARANES", "📝 PRESUPUESTOS",
                "👤 USUARIOS", "⚙️ CONFIGURACION", "🔐 AUTENTICACION", "🔍 AUDITORIA"
            ));
            cmbModulo.setValue("📦 Todos los módulos");
        }

        // Configurar fechas por defecto (últimos 30 días)
        if (dpFechaDesde != null && dpFechaHasta != null) {
            dpFechaHasta.setValue(LocalDate.now());
            dpFechaDesde.setValue(LocalDate.now().minusDays(30));
        }
    }

    private void configurarListeners() {
        // Listener para búsqueda en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }

        // Listeners para filtros
        if (cmbUsuario != null) {
            cmbUsuario.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }
        if (cmbAccion != null) {
            cmbAccion.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }
        if (cmbModulo != null) {
            cmbModulo.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }
        if (dpFechaDesde != null) {
            dpFechaDesde.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }
        if (dpFechaHasta != null) {
            dpFechaHasta.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        }

        // Listener para selección de tabla
        tableAuditoria.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (lblSeleccion != null) {
                if (newV != null) {
                    String usuario = Optional.ofNullable(newV.getUsuarioNombre()).orElse("SISTEMA");
                    String accion = newV.getTipoAccion();
                    lblSeleccion.setText("Seleccionado: " + usuario + " - " + accion);
                } else {
                    lblSeleccion.setText("Ningún registro seleccionado");
                }
            }
        });
    }

    private void cargarDatos() {
        try {
            log.info("📊 Cargando registros de auditoría...");
            auditoriaList.clear();

            // Cargar con filtro de fechas si están establecidas
            LocalDate desde = dpFechaDesde != null ? dpFechaDesde.getValue() : null;
            LocalDate hasta = dpFechaHasta != null ? dpFechaHasta.getValue() : null;

            List<AuditoriaAccion> registros;
            if (desde != null && hasta != null) {
                LocalDateTime inicioDateTime = desde.atStartOfDay();
                LocalDateTime finDateTime = hasta.atTime(LocalTime.MAX);
                registros = auditoriaRepository.findByFechaBetweenOrderByFechaDesc(inicioDateTime, finDateTime);
            } else {
                // Cargar todos (limitado a los últimos 1000 para rendimiento)
                registros = auditoriaRepository.findAll();
                registros = registros.stream()
                    .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                    .limit(1000)
                    .collect(Collectors.toList());
            }

            auditoriaList.addAll(registros);
            aplicarFiltros();
            actualizarEstadisticas();
            log.info("✅ {} registros de auditoría cargados", auditoriaList.size());
        } catch (Exception e) {
            log.error("❌ Error cargando auditoría", e);
            DialogUtils.showError("Error al cargar auditoría: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        auditoriaFilteredList.clear();

        String busqueda = txtBuscar != null ? txtBuscar.getText().toLowerCase() : "";
        String filtroUsuario = cmbUsuario != null ? cmbUsuario.getValue() : null;
        String filtroAccion = cmbAccion != null ? cmbAccion.getValue() : null;
        String filtroModulo = cmbModulo != null ? cmbModulo.getValue() : null;

        auditoriaFilteredList.addAll(auditoriaList.stream()
            .filter(a -> {
                // Filtro de búsqueda
                if (!busqueda.isEmpty()) {
                    String descripcion = a.getDescripcion() != null ? a.getDescripcion().toLowerCase() : "";
                    String usuario = a.getUsuarioNombre() != null ? a.getUsuarioNombre().toLowerCase() : "";
                    String entidad = a.getEntidadTipo() != null ? a.getEntidadTipo().toLowerCase() : "";
                    if (!descripcion.contains(busqueda) && !usuario.contains(busqueda) && !entidad.contains(busqueda)) {
                        return false;
                    }
                }

                // Filtro de usuario
                if (filtroUsuario != null && !filtroUsuario.contains("Todos")) {
                    String usuario = a.getUsuarioNombre();
                    if (!filtroUsuario.equals(usuario)) return false;
                }

                // Filtro de acción
                if (filtroAccion != null && !filtroAccion.contains("Todas")) {
                    String accion = a.getTipoAccion();
                    if (accion == null || !filtroAccion.contains(accion)) return false;
                }

                // Filtro de módulo
                if (filtroModulo != null && !filtroModulo.contains("Todos")) {
                    String modulo = a.getModulo();
                    return modulo != null && filtroModulo.contains(modulo);
                }

                return true;
            })
            .toList()
        );

        actualizarContador();
        Platform.runLater(() -> tableAuditoria.refresh());
    }

    private void actualizarContador() {
        if (lblTotal != null) {
            int total = auditoriaList.size();
            int mostrados = auditoriaFilteredList.size();
            if (total == mostrados) {
                lblTotal.setText(total + " registros de auditoría");
            } else {
                lblTotal.setText(mostrados + " de " + total + " registros");
            }
        }
    }

    private void actualizarEstadisticas() {
        if (lblEstadisticas == null) return;

        long exitosos = auditoriaList.stream().filter(a -> "EXITO".equals(a.getResultado())).count();
        long errores = auditoriaList.stream().filter(a -> "ERROR".equals(a.getResultado())).count();
        long usuarios = auditoriaList.stream().map(AuditoriaAccion::getUsuarioNombre).distinct().count();

        lblEstadisticas.setText(String.format(
            "📊 Estadísticas: %d registros | ✅ %d éxitos | ❌ %d errores | 👥 %d usuarios",
            auditoriaList.size(), exitosos, errores, usuarios
        ));
    }

    private String getEmojiAccion(String accion) {
        if (accion == null) return "❓";
        return switch (accion.toUpperCase()) {
            case "CREAR" -> "➕";
            case "ACTUALIZAR", "MODIFICAR" -> "✏️";
            case "ELIMINAR" -> "🗑️";
            case "LEER", "CONSULTAR" -> "📖";
            case "LOGIN" -> "🔑";
            case "LOGOUT" -> "🚪";
            case "EXPORTAR" -> "📤";
            case "IMPRIMIR" -> "🖨️";
            case "ERROR" -> "❌";
            case "EXITO" -> "✅";
            default -> "⚡";
        };
    }

    @FXML
    public void onBuscar() {
        log.info("🔍 Aplicando filtros de búsqueda");
        cargarDatos();
    }

    @FXML
    public void onRefresh() {
        log.info("🔄 Refrescando auditoría");
        cargarDatos();
    }

    @FXML
    public void onLimpiarFiltros() {
        log.info("🗑️ Limpiando filtros");
        if (txtBuscar != null) txtBuscar.clear();
        if (cmbUsuario != null) cmbUsuario.setValue("👤 Todos los usuarios");
        if (cmbAccion != null) cmbAccion.setValue("⚡ Todas las acciones");
        if (cmbModulo != null) cmbModulo.setValue("📦 Todos los módulos");
        if (dpFechaDesde != null) dpFechaDesde.setValue(LocalDate.now().minusDays(30));
        if (dpFechaHasta != null) dpFechaHasta.setValue(LocalDate.now());
        cargarDatos();
    }

    @FXML
    public void onVerDetalles() {
        AuditoriaAccion auditoria = tableAuditoria.getSelectionModel().getSelectedItem();
        if (auditoria == null) {
            DialogUtils.showWarning("Por favor, seleccione un registro para ver detalles");
            return;
        }
        mostrarDetalleCompleto(auditoria);
    }

    private void mostrarDetalleCompleto(AuditoriaAccion auditoria) {
        StringBuilder detalle = new StringBuilder();
        detalle.append("📋 DETALLE COMPLETO DE AUDITORÍA\n");
        detalle.append("═══════════════════════════════════════\n\n");

        detalle.append("🆔 ID: ").append(auditoria.getId()).append("\n");
        detalle.append("📅 Fecha: ").append(auditoria.getFecha().format(DATE_TIME_FORMATTER)).append("\n");
        detalle.append("👤 Usuario: ").append(Optional.ofNullable(auditoria.getUsuarioNombre()).orElse("SISTEMA")).append("\n");
        detalle.append("⚡ Acción: ").append(auditoria.getTipoAccion()).append("\n");
        detalle.append("📦 Módulo: ").append(Optional.ofNullable(auditoria.getModulo()).orElse("-")).append("\n");

        if (auditoria.getEntidadTipo() != null) {
            detalle.append("🎯 Entidad: ").append(auditoria.getEntidadTipo());
            if (auditoria.getEntidadId() != null) {
                detalle.append(" #").append(auditoria.getEntidadId());
            }
            detalle.append("\n");
        }

        detalle.append("📝 Descripción: ").append(Optional.ofNullable(auditoria.getDescripcion()).orElse("-")).append("\n");
        detalle.append("✅ Resultado: ").append(Optional.ofNullable(auditoria.getResultado()).orElse("N/A")).append("\n");

        if (auditoria.getIp() != null) {
            detalle.append("🌐 IP: ").append(auditoria.getIp()).append("\n");
        }

        if (auditoria.getMensajeError() != null) {
            detalle.append("\n❌ Error:\n").append(auditoria.getMensajeError()).append("\n");
        }

        DialogUtils.showInfo(detalle.toString());
    }

    @FXML
    public void onEstadisticas() {
        if (auditoriaList.isEmpty()) {
            DialogUtils.showWarning("No hay datos para mostrar estadísticas");
            return;
        }

        // Calcular estadísticas
        Map<String, Long> porAccion = auditoriaList.stream()
            .collect(Collectors.groupingBy(AuditoriaAccion::getTipoAccion, Collectors.counting()));

        Map<String, Long> porModulo = auditoriaList.stream()
            .filter(a -> a.getModulo() != null)
            .collect(Collectors.groupingBy(AuditoriaAccion::getModulo, Collectors.counting()));

        Map<String, Long> porUsuario = auditoriaList.stream()
            .filter(a -> a.getUsuarioNombre() != null)
            .collect(Collectors.groupingBy(AuditoriaAccion::getUsuarioNombre, Collectors.counting()));

        StringBuilder stats = new StringBuilder();
        stats.append("📊 ESTADÍSTICAS DE AUDITORÍA\n");
        stats.append("═══════════════════════════════════════\n\n");

        stats.append("📈 Total de registros: ").append(auditoriaList.size()).append("\n\n");

        stats.append("⚡ Por Acción:\n");
        porAccion.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> stats.append("  • ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        stats.append("\n📦 Por Módulo:\n");
        porModulo.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> stats.append("  • ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        stats.append("\n👥 Por Usuario:\n");
        porUsuario.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> stats.append("  • ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        DialogUtils.showInfo(stats.toString());
    }

    @FXML
    public void onFiltroRapido() {
        List<String> opciones = Arrays.asList(
            "Últimas 24 horas",
            "Última semana",
            "Último mes",
            "Solo errores",
            "Solo logins",
            "Solo modificaciones"
        );

        ChoiceDialog<String> dialog = new ChoiceDialog<>(opciones.get(0), opciones);
        dialog.setTitle("Filtro Rápido");
        dialog.setHeaderText("Seleccione un filtro rápido");
        dialog.setContentText("Opción:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(opcion -> {
            switch (opcion) {
                case "Últimas 24 horas" -> {
                    dpFechaDesde.setValue(LocalDate.now().minusDays(1));
                    dpFechaHasta.setValue(LocalDate.now());
                }
                case "Última semana" -> {
                    dpFechaDesde.setValue(LocalDate.now().minusWeeks(1));
                    dpFechaHasta.setValue(LocalDate.now());
                }
                case "Último mes" -> {
                    dpFechaDesde.setValue(LocalDate.now().minusMonths(1));
                    dpFechaHasta.setValue(LocalDate.now());
                }
                case "Solo errores" -> cmbAccion.setValue("❌ ERROR");
                case "Solo logins" -> cmbAccion.setValue("🔑 LOGIN");
                case "Solo modificaciones" -> cmbAccion.setValue("✏️ ACTUALIZAR");
            }
            cargarDatos();
        });
    }

    @FXML
    public void onExportar() {
        if (auditoriaFilteredList.isEmpty()) {
            DialogUtils.showWarning("No hay registros de auditoria para exportar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar auditoria");
        fileChooser.setInitialFileName("auditoria_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        Window owner = tableAuditoria != null && tableAuditoria.getScene() != null
            ? tableAuditoria.getScene().getWindow()
            : null;
        var file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return;
        }

        try {
            Path destino = file.toPath();
            Files.writeString(destino, construirCsvAuditoria(auditoriaFilteredList), StandardCharsets.UTF_8);
            DialogUtils.showSuccess("Auditoria exportada correctamente:\n" + destino);
        } catch (IOException e) {
            log.error("Error exportando auditoria", e);
            DialogUtils.showError("No se pudo exportar la auditoria: " + e.getMessage());
        }
    }

    private String construirCsvAuditoria(List<AuditoriaAccion> registros) {
        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append("ID;Fecha;Usuario;Accion;Modulo;Entidad;Entidad ID;Descripcion;Resultado;IP;Error\n");
        for (AuditoriaAccion accion : registros) {
            csv.append(csv(accion.getId())).append(';');
            csv.append(csv(accion.getFecha() != null ? accion.getFecha().format(DATE_TIME_FORMATTER) : "")).append(';');
            csv.append(csv(accion.getUsuarioNombre())).append(';');
            csv.append(csv(accion.getTipoAccion())).append(';');
            csv.append(csv(accion.getModulo())).append(';');
            csv.append(csv(accion.getEntidadTipo())).append(';');
            csv.append(csv(accion.getEntidadId())).append(';');
            csv.append(csv(accion.getDescripcion())).append(';');
            csv.append(csv(accion.getResultado())).append(';');
            csv.append(csv(accion.getIp())).append(';');
            csv.append(csv(accion.getMensajeError())).append('\n');
        }
        return csv.toString();
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
