package alicanteweb.erp.controller;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticuloController extends BaseController<Articulo> {
    private static final Logger log = LoggerFactory.getLogger(ArticuloController.class);

    private final ArticuloService articuloService;

    @FXML private TableView<Articulo> tableArticulos;
    @FXML private TableColumn<Articulo, String> colCodigo;
    @FXML private TableColumn<Articulo, String> colNombre;
    @FXML private TableColumn<Articulo, BigDecimal> colPrecio;
    @FXML private TableColumn<Articulo, BigDecimal> colIVA;
    @FXML private TableColumn<Articulo, BigDecimal> colStock;
    @FXML private TableColumn<Articulo, Boolean> colActivo;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbActivo;
    @FXML private Label lblTotal;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @FXML
    public void initialize() {
        log.info("✅ Inicializando ArticuloController");

        // Asignar la tabla del FXML a la tabla base
        this.table = tableArticulos;
        this.lblEstado = lblTotal;

        // Configurar columnas
        DecimalFormat df = new DecimalFormat("#,##0.00");
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colPrecio != null) {
            colPrecio.setCellValueFactory(new PropertyValueFactory<>("pvp"));
            colPrecio.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : df.format(item));
                }
            });
        }
        if (colIVA != null) {
            colIVA.setCellValueFactory(new PropertyValueFactory<>("iva"));
            colIVA.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : df.format(item));
                }
            });
        }
        if (colStock != null) {
            colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
            colStock.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : df.format(item));
                }
            });
        }
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Aplicar estilo a la tabla
        if (tableArticulos != null) {
            tableArticulos.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        }

        // Inicializar ComboBox de Categoría
        if (cmbCategoria != null) {
            cmbCategoria.getItems().clear();
            cmbCategoria.getItems().addAll(
                "Todas",
                "Materia Prima",
                "Producto Terminado",
                "Envases",
                "Material Auxiliar",
                "Mercadería",
                "Otros"
            );
            cmbCategoria.setValue("Todas");

            // Listener para filtrar cuando cambia la selección
            cmbCategoria.setOnAction(e -> aplicarFiltros());
        }

        // Inicializar ComboBox de Estado
        if (cmbActivo != null) {
            cmbActivo.getItems().clear();
            cmbActivo.getItems().addAll(
                "Todos",
                "Activos",
                "Inactivos"
            );
            cmbActivo.setValue("Todos");

            // Listener para filtrar cuando cambia la selección
            cmbActivo.setOnAction(e -> aplicarFiltros());
        }

        // Inicializar controlador base
        initController();
    }

    @Override
    protected void cargarDatos() {
        try {
            List<Articulo> articulos = articuloService.findAll();
            actualizarTabla(articulos);
            log.info("✅ Cargados {} artículos", articulos.size());
        } catch (Exception e) {
            log.error("❌ Error cargando artículos", e);
            mostrarError("Error al cargar artículos: " + e.getMessage());
        }
    }

    @Override
    protected String getNombreModulo() {
        return "Artículo";
    }

    @Override
    protected String getRutaFormulario() {
        return "/ui/articulo_form.fxml";
    }

    @Override
    protected boolean coincideConBusqueda(Articulo item, String termino) {
        if (item == null || termino == null) return false;

        String t = termino.toLowerCase();
        return (item.getCodigo() != null && item.getCodigo().toLowerCase().contains(t)) ||
               (item.getNombre() != null && item.getNombre().toLowerCase().contains(t)) ||
               (item.getDescripcion() != null && item.getDescripcion().toLowerCase().contains(t)) ||
               (item.getCodigoBarras() != null && item.getCodigoBarras().toLowerCase().contains(t)) ||
               (item.getFamilia() != null && item.getFamilia().toLowerCase().contains(t));
    }

    @Override
    protected void eliminarItem(Articulo item) {
        if (item != null && item.getId() != null) {
            // Delegar al servicio para centralizar la lógica de dar de baja
            articuloService.darDeBaja(item.getId());
            log.info("✅ Artículo dado de baja (servicio): {}", item.getCodigo());
        }
    }

    @FXML
    public void onBuscar() {
        aplicarFiltros();
    }

    /**
     * Aplicar todos los filtros: búsqueda de texto, categoría y estado
     */
    private void aplicarFiltros() {
        if (datosCompletos == null) {
            return;
        }

        String terminoBusqueda = txtBuscar != null ? txtBuscar.getText() : "";
        String categoriaSeleccionada = cmbCategoria != null ? cmbCategoria.getValue() : "Todas";
        String estadoSeleccionado = cmbActivo != null ? cmbActivo.getValue() : "Todos";

        // Base inicial: si se selecciona estado, usar consulta específica del servicio
        List<Articulo> base;
        if ("Activos".equals(estadoSeleccionado)) {
            base = articuloService.findByActivo(true);
        } else if ("Inactivos".equals(estadoSeleccionado)) {
            base = articuloService.findByActivo(false);
        } else {
            base = datosCompletos;
        }

        // Si hay texto de búsqueda, preferir búsqueda en repositorio para rendimiento
        List<Articulo> buscados;
        if (terminoBusqueda != null && !terminoBusqueda.isBlank()) {
            buscados = articuloService.searchByDescripcion(terminoBusqueda);
            // intersect buscados with base
            base = base.stream().filter(a -> buscados.stream().anyMatch(b -> b.getId().equals(a.getId()))).toList();
        }

        // Aplicar filtro por categoría si procede
        List<Articulo> filtrados = base.stream()
            .filter(articulo -> {
                if (categoriaSeleccionada == null || "Todas".equals(categoriaSeleccionada)) return true;
                return articulo.getCategoria() != null && articulo.getCategoria().equals(categoriaSeleccionada);
            })
            .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtrados));

        if (lblEstado != null) {
            lblEstado.setText(String.format("Mostrando: %d de %d artículos",
                filtrados.size(), datosCompletos.size()));
        }

        log.debug("Filtros aplicados: {} artículos de {} totales", filtrados.size(), datosCompletos.size());
    }

    @FXML
    public void onDarBaja() {
        Articulo seleccionado = tableArticulos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un artículo para dar de baja");
            return;
        }

        String mensaje = seleccionado.getActivo() != null && seleccionado.getActivo()
            ? "¿Estás seguro de que deseas dar de baja este artículo?"
            : "¿Deseas reactivar este artículo?";

        if (mostrarConfirmacion(mensaje)) {
            try {
                // Cambiar el estado activo/inactivo
                boolean nuevoEstado = !(seleccionado.getActivo() != null && seleccionado.getActivo());
                seleccionado.setActivo(nuevoEstado);
                articuloService.save(seleccionado);

                cargarDatos(); // Recargar datos

                String textoResultado = nuevoEstado ? "Artículo reactivado" : "Artículo dado de baja";
                mostrarExito(textoResultado + " correctamente");

                log.info("✅ Artículo {} de baja: {}", nuevoEstado ? "reactivado" : "dado", seleccionado.getCodigo());
            } catch (Exception e) {
                log.error("❌ Error al cambiar estado del artículo", e);
                mostrarError("Error al cambiar estado: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onVer() {
        Articulo seleccionado = tableArticulos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona un artículo para ver");
            return;
        }

        // Construir información del artículo
        StringBuilder info = new StringBuilder();
        info.append("Código: ").append(seleccionado.getCodigo()).append("\n");
        info.append("Nombre: ").append(seleccionado.getNombre()).append("\n");
        if (seleccionado.getDescripcion() != null) {
            info.append("Descripción: ").append(seleccionado.getDescripcion()).append("\n");
        }
        if (seleccionado.getCodigoBarras() != null) {
            info.append("Código de Barras: ").append(seleccionado.getCodigoBarras()).append("\n");
        }
        if (seleccionado.getCoste() != null) {
            info.append("Coste: ").append(seleccionado.getCoste()).append("€\n");
        }
        if (seleccionado.getPvp() != null) {
            info.append("PVP: ").append(seleccionado.getPvp()).append("€\n");
        }
        if (seleccionado.getIva() != null) {
            info.append("IVA: ").append(seleccionado.getIva()).append("%\n");
        }
        if (seleccionado.getStock() != null) {
            info.append("Stock: ").append(seleccionado.getStock()).append("\n");
        }
        info.append("Estado: ").append(seleccionado.getActivo() != null && seleccionado.getActivo() ? "Activo" : "Inactivo").append("\n");

        mostrarInfo(info.toString());
    }
}

