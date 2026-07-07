package alicanteweb.erp.service;

import alicanteweb.erp.entities.OrdenProduccion;
import alicanteweb.erp.entities.OrdenProduccionSerieSequence;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.PedidoLinea;
import alicanteweb.erp.entities.Receta;
import alicanteweb.erp.repository.OrdenProduccionRepository;
import alicanteweb.erp.repository.OrdenProduccionSerieSequenceRepository;
import alicanteweb.erp.repository.RecetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly = true)
public class OrdenProduccionService {

    private static final String SERIE = "OP";

    private final OrdenProduccionRepository repository;
    private final OrdenProduccionSerieSequenceRepository sequenceRepository;
    private final ArticuloService articuloService;
    private final AlmacenService almacenService;
    private final RecetaRepository recetaRepository;
    private final StockService stockService;

    public OrdenProduccionService(OrdenProduccionRepository repository,
                                   OrdenProduccionSerieSequenceRepository sequenceRepository,
                                   ArticuloService articuloService,
                                   AlmacenService almacenService,
                                   RecetaRepository recetaRepository,
                                   StockService stockService) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.recetaRepository = recetaRepository;
        this.stockService = stockService;
    }

    public List<OrdenProduccion> findAll() {
        return repository.findAll();
    }

    public Optional<OrdenProduccion> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<OrdenProduccion> findDetailById(Long id) {
        return repository.findDetailById(id);
    }

    public Page<OrdenProduccion> findPage(String estado, Long recetaId, Pageable pageable) {
        return repository.findPage(normalize(estado), recetaId, pageable);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Optional<OrdenProduccion> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public List<OrdenProduccion> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public long countByEstado(String estado) {
        return repository.countByEstado(estado);
    }

    public List<OrdenProduccion> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return repository.findByFechaBetween(inicio, fin);
    }

    public List<OrdenProduccion> findByRecetaId(Long recetaId) {
        return repository.findByRecetaId(recetaId);
    }

    @Transactional
    public OrdenProduccion save(OrdenProduccion orden) {
        if (orden == null) throw new IllegalArgumentException("Orden de producción nula");
        if (orden.getNumero() == null || orden.getNumero().isBlank()) {
            orden.setNumero(generarNumero());
        }
        return repository.save(orden);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Numeración por secuencia con lock pesimista (mismo patrón que
     * FacturaService/FacturaSerieSequenceRepository): correcta con varias
     * instancias de la app, al contrario que el antiguo MAX(numero) bajo
     * synchronized, que solo serializaba dentro de una JVM.
     */
    @Transactional
    public String generarNumero() {
        int ejercicio = LocalDate.now().getYear();
        OrdenProduccionSerieSequence sequence = sequenceRepository
            .findBySerieAndEjercicio(SERIE, ejercicio)
            .orElseGet(() -> crearSecuencia(ejercicio));

        long siguienteNumero = sequence.getUltimoNumero() + 1L;
        sequence.setUltimoNumero(siguienteNumero);
        sequenceRepository.saveAndFlush(sequence);

        return String.format("%s-%d-%04d", SERIE, ejercicio, siguienteNumero);
    }

    private OrdenProduccionSerieSequence crearSecuencia(int ejercicio) {
        // La migración V42 siembra los ejercicios con órdenes previas; este camino
        // cubre el primer uso de un ejercicio nuevo. Se parte del máximo existente
        // por si hubiera órdenes creadas fuera de la secuencia.
        OrdenProduccionSerieSequence nueva = new OrdenProduccionSerieSequence();
        nueva.setSerie(SERIE);
        nueva.setEjercicio(ejercicio);
        nueva.setUltimoNumero((long) repository.findMaxNumeroSecuencialBySerie(SERIE + "-" + ejercicio));
        return nueva;
    }

    @Transactional
    public OrdenProduccion iniciarProduccion(Long id) {
        OrdenProduccion orden = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
        if (!"PLANIFICADA".equals(orden.getEstado())) {
            throw new IllegalStateException("Solo se pueden iniciar órdenes en estado PLANIFICADA");
        }
        orden.setEstado("EN_CURSO");
        orden.setFechaInicio(java.time.LocalDateTime.now());
        return repository.save(orden);
    }

    @Transactional
    public OrdenProduccion finalizarProduccion(Long id, java.math.BigDecimal cantidadProducida, java.math.BigDecimal merma) {
        OrdenProduccion orden = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
        if (!"EN_CURSO".equals(orden.getEstado())) {
            throw new IllegalStateException("Solo se pueden finalizar órdenes en curso");
        }
        orden.setEstado("FINALIZADA");
        orden.setFechaFin(java.time.LocalDateTime.now());
        orden.setCantidadProducida(cantidadProducida);
        orden.setMerma(merma);
        orden = repository.save(orden);

        // Entrada de stock del artículo resultante
        if (orden.getArticulo() != null && cantidadProducida != null && cantidadProducida.compareTo(BigDecimal.ZERO) > 0) {
            Long almacenId = orden.getAlmacen() != null ? orden.getAlmacen().getId() : null;
            stockService.registrarEntrada(orden.getArticulo().getId(), almacenId, cantidadProducida,
                    "Produccion " + orden.getNumero(), "ORDEN_PRODUCCION", orden.getId());
        }

        // Salida de stock de ingredientes (si hay receta)
        if (orden.getReceta() != null && orden.getReceta().getIngredientes() != null) {
            BigDecimal rendimiento = orden.getReceta().getRendimientoCantidad();
            if (rendimiento == null || rendimiento.compareTo(BigDecimal.ZERO) <= 0) rendimiento = BigDecimal.ONE;
            BigDecimal factor = cantidadProducida.divide(rendimiento, 4, java.math.RoundingMode.HALF_UP);
            Long almacenId = orden.getAlmacen() != null ? orden.getAlmacen().getId() : null;
            for (var ing : orden.getReceta().getIngredientes()) {
                if (ing.getArticulo() == null || ing.getCantidad() == null) continue;
                BigDecimal cantidadInsumo = ing.getCantidad().multiply(factor);
                stockService.registrarSalida(ing.getArticulo().getId(), almacenId, cantidadInsumo,
                        "Consumo produccion " + orden.getNumero(), "ORDEN_PRODUCCION", orden.getId());
            }
        }

        return orden;
    }

    @Transactional
    public OrdenProduccion cancelarProduccion(Long id, String motivo) {
        OrdenProduccion orden = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
        if ("FINALIZADA".equals(orden.getEstado())) {
            throw new IllegalStateException("No se puede cancelar una orden ya finalizada");
        }
        orden.setEstado("CANCELADA");
        orden.setObservaciones((orden.getObservaciones() != null ? orden.getObservaciones() + " | " : "") + "Cancelada: " + motivo);
        return repository.save(orden);
    }

    /**
     * Genera órdenes de producción a partir de los artículos de un pedido de venta
     * que tengan receta asociada. Una orden por cada artículo con receta.
     * @return Lista de órdenes de producción generadas
     */
    @Transactional
    public List<OrdenProduccion> generarDesdePedido(Pedido pedido) {
        List<OrdenProduccion> generadas = new ArrayList<>();
        if (pedido == null || pedido.getLineas() == null) return generadas;

        for (PedidoLinea linea : pedido.getLineas()) {
            if (linea.getArticulo() == null) continue;

            List<Receta> recetas = recetaRepository.findByArticuloResultante_IdAndActivoTrue(linea.getArticulo().getId());

            if (!recetas.isEmpty()) {
                Receta receta = recetas.get(0);
                OrdenProduccion orden = new OrdenProduccion();
                orden.setNumero(generarNumero());
                orden.setFecha(LocalDate.now());
                orden.setReceta(receta);
                orden.setArticulo(linea.getArticulo());
                orden.setCantidadPlanificada(linea.getCantidad());
                orden.setEstado("PLANIFICADA");
                orden.setObservaciones("Generada desde pedido: " + pedido.getNumero());
                generadas.add(repository.save(orden));
            }
        }
        return generadas;
    }
}
