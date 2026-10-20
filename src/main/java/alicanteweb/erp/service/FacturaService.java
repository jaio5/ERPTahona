package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.FacturaSerieSequence;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.FacturaSerieSequenceRepository;
import alicanteweb.erp.util.FinancialMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacturaService {
    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);
    private final FacturaRepository repository;
    private final FacturaSerieSequenceRepository sequenceRepository;
    private final VerifactuService verifactuService;
    private final FacturacionEventoService facturacionEventoService;
    private final FiscalComplianceService fiscalComplianceService;
    private final ContabilidadService contabilidadService;

    public FacturaService(FacturaRepository repository,
                          FacturaSerieSequenceRepository sequenceRepository,
                          VerifactuService verifactuService,
                          FacturacionEventoService facturacionEventoService,
                          FiscalComplianceService fiscalComplianceService,
                          ContabilidadService contabilidadService) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
        this.verifactuService = verifactuService;
        this.facturacionEventoService = facturacionEventoService;
        this.fiscalComplianceService = fiscalComplianceService;
        this.contabilidadService = contabilidadService;
    }

    public List<Factura> findAll() {
        return repository.findAllWithCliente();
    }

    public long count() {
        return repository.count();
    }

    /** Filtro pseudo-estado del listado: impagadas con el vencimiento pasado (enlace "Facturas vencidas" del panel). */
    public static final String FILTRO_VENCIDAS = "VENCIDAS";

    public Page<Factura> buscarPaginado(String q, String estado, LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        if (FILTRO_VENCIDAS.equalsIgnoreCase(estado)) {
            return repository.buscarVencidasPaginado(
                (q != null && !q.isBlank()) ? q : null,
                LocalDate.now(), fechaDesde, fechaHasta, pageable);
        }
        return repository.buscarPaginado(
            (q != null && !q.isBlank()) ? q : null,
            (estado != null && !estado.isBlank()) ? estado : null,
            fechaDesde, fechaHasta,
            pageable);
    }

    public Optional<Factura> findById(Long id) {
        return repository.findById(id);
    }

    public List<Factura> findByClienteId(Long clienteId) {
        return repository.findByCliente_Id(clienteId);
    }

    public Optional<Factura> findByIdParaPdf(Long id) {
        return repository.findByIdWithPdfData(id);
    }

    public Optional<Factura> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public Optional<Factura> findBySerieAndNumero(String serie, String numero) {
        return repository.findBySerieAndNumero(normalizarSerie(serie), numero);
    }

    @Transactional(readOnly = true)
    public List<Factura> searchByNumero(String q) {
        return repository.findByNumeroContainingIgnoreCase(q);
    }

    @Transactional(readOnly = true)
    public List<Factura> findByFechaBetweenAndEstado(LocalDate inicio, LocalDate fin, String estado) {
        return repository.findByFechaBetweenAndEstado(inicio, fin, estado);
    }

    @Transactional(readOnly = true)
    public List<Factura> findVencidas(LocalDate hoy) {
        return repository.findVencidas(hoy);
    }

    @Transactional
    public Factura save(Factura factura) {
        // Inalterabilidad (RRSIF): una factura expedida (EMITIDA, PAGADA, VENCIDA, RECTIFICADA,
        // ANULADA) no se modifica; los cambios exigen factura rectificativa. La señal es el
        // estado, no verifactuEnviada (que solo indica remisión real a la AEAT).
        if (factura.getId() != null) {
            repository.findById(factura.getId()).ifPresent(existente -> {
                if (Factura.esEstadoInalterable(existente.getEstado())
                        || Boolean.TRUE.equals(existente.getVerifactuEnviada())) {
                    throw new IllegalStateException(
                        "Las facturas emitidas no se modifican. Cree una factura rectificativa.");
                }
            });
        }
        prepararFacturaParaGuardar(factura);
        return repository.save(factura);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(existente -> {
            if (Factura.esEstadoInalterable(existente.getEstado())
                    || Boolean.TRUE.equals(existente.getVerifactuEnviada())) {
                throw new IllegalStateException(
                    "Las facturas emitidas o registradas en VeriFactu no se pueden eliminar.");
            }
        });
        repository.deleteById(id);
    }

    @Transactional
    public String generarSiguienteNumero(String serie, LocalDate fecha, boolean rectificativa) {
        String serieNormalizada = normalizarSerie(serie);
        int ejercicio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        String prefijo = rectificativa ? "R" : "F";

        // findBySerieAndEjercicio usa PESSIMISTIC_WRITE, lo que serializa el acceso concurrente
        // correctamente sin necesidad de retry que contamina la transacción padre
        FacturaSerieSequence sequence = sequenceRepository
            .findBySerieAndEjercicio(serieNormalizada, ejercicio)
            .orElseGet(() -> crearSecuenciaFactura(serieNormalizada, ejercicio, prefijo));

        long siguienteNumero = sequence.getUltimoNumero() + 1L;
        sequence.setUltimoNumero(siguienteNumero);
        sequenceRepository.saveAndFlush(sequence);

        return String.format("%s-%s-%d-%04d", prefijo, serieNormalizada, ejercicio, siguienteNumero);
    }

    @Transactional
    public Factura pasarARevision(Long facturaId) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        if (!"BORRADOR".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden enviar a revisión facturas en estado BORRADOR");
        }
        factura.setEstado("REVISION");
        return repository.save(factura);
    }

    @Transactional
    public Factura aprobarYEmitir(Long facturaId) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"REVISION".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden emitir facturas en estado REVISION");
        }

        // Cumplimiento fiscal previo a la emisión (RRSIF): configuración, declaración
        // responsable y cadena de eventos deben estar en regla antes de emitir
        fiscalComplianceService.exigirListoParaEmision();
        verifactuService.enviarFacturaVerifactu(factura);
        Factura guardada = repository.save(factura);

        // Asiento de venta (430 a 700/477); en misma transacción para coherencia contable
        contabilidadService.generarAsientoFactura(guardada, null);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("facturaId", guardada.getId());
        metadata.put("numero", guardada.getNumero());
        metadata.put("serie", guardada.getSerie());
        metadata.put("estado", guardada.getEstado());
        metadata.put("fechaEmisionVerifactu", guardada.getFechaEmisionVerifactu() != null ? guardada.getFechaEmisionVerifactu().toString() : null);
        facturacionEventoService.registrarEvento(FacturacionEventoService.AMBITO_FACTURAS, "EMISION_FACTURA", guardada.getNumero(), metadata);

        return guardada;
    }

    @Transactional
    public Factura anularFactura(Long facturaId, String motivo) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            return factura;
        }

        if (Factura.esEstadoInalterable(factura.getEstado()) || Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
            throw new IllegalStateException("Las facturas emitidas no se anulan directamente. Cree una factura rectificativa.");
        }

        factura.setEstado("ANULADA");
        factura.setObservacionesRevision(concatObservacion(factura.getObservacionesRevision(), motivo));
        Factura guardada = repository.save(factura);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("facturaId", guardada.getId());
        metadata.put("numero", guardada.getNumero());
        metadata.put("serie", guardada.getSerie());
        metadata.put("motivo", motivo);
        facturacionEventoService.registrarEvento(FacturacionEventoService.AMBITO_FACTURAS, "ANULACION_PRE_EMISION", guardada.getNumero(), metadata);
        return guardada;
    }

    @Transactional
    public Factura crearRectificativa(Long facturaIdOriginal, String motivo, String tipoRectificacion, LocalDate fechaRectificativa) {
        Factura original = repository.findByIdWithPdfData(facturaIdOriginal)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"EMITIDA".equalsIgnoreCase(original.getEstado())) {
            throw new IllegalStateException("Solo se pueden rectificar facturas emitidas. Estado actual: " + original.getEstado());
        }
        // La original ya no pasa a ANULADA al rectificarla, así que nada impedía crear varias
        // rectificativas completas (doble clic) y restar la operación dos veces. Mientras haya una
        // rectificativa sin emitir de esta factura, se termina o se anula esa antes de crear otra.
        if (repository.existsByFacturaRectificadaNumeroAndEstadoIn(original.getNumero(), List.of("BORRADOR", "REVISION"))) {
            throw new IllegalStateException("La factura " + original.getNumero()
                    + " ya tiene una rectificativa pendiente de emitir. Emítala o anúlela antes de crear otra.");
        }

        Factura rectificativa = inicializarCabeceraRectificativa(original, motivo, tipoRectificacion, fechaRectificativa);
        clonarLineasInvertidas(original, rectificativa);
        recalcularTotalesDesdeLineas(rectificativa);
        prepararFacturaParaGuardar(rectificativa);
        Factura saved = repository.save(rectificativa);

        // La rectificativa NO anula la original (ni genera registro de anulación): la original
        // sigue EMITIDA, en el libro de IVA y en el 347, y la rectificativa (líneas en negativo)
        // la corrige. Solo se anota la referencia cruzada.
        anotarRectificacionEnOriginal(original, saved.getNumero(), motivo);
        registrarEventoRectificativa(original, saved, motivo);

        return saved;
    }

    private Factura inicializarCabeceraRectificativa(Factura original, String motivo, String tipoRectificacion, LocalDate fechaRectificativa) {
        Factura rectificativa = new Factura();
        rectificativa.setTipoFactura("RECTIFICATIVA");
        rectificativa.setCliente(original.getCliente());
        rectificativa.setSerie(original.getSerie());
        rectificativa.setFecha(fechaRectificativa != null ? fechaRectificativa : LocalDate.now());
        rectificativa.setFacturaRectificadaNumero(original.getNumero());
        rectificativa.setFacturaRectificadaFecha(original.getFecha());
        rectificativa.setMotivoRectificacion(motivo);
        rectificativa.setTipoRectificacion(tipoRectificacion != null ? tipoRectificacion : "SUSTITUCION");
        rectificativa.setMedioCobro(original.getMedioCobro());
        rectificativa.setObservaciones("Rectificativa de factura " + original.getNumero());
        return rectificativa;
    }

    private void clonarLineasInvertidas(Factura original, Factura rectificativa) {
        for (FacturaLinea lineaOriginal : original.getFacturaLineas()) {
            FacturaLinea linea = new FacturaLinea();
            linea.setFactura(rectificativa);
            linea.setArticulo(lineaOriginal.getArticulo());
            String desc = lineaOriginal.getDescripcion() != null ? lineaOriginal.getDescripcion()
                    : (lineaOriginal.getArticulo() != null ? lineaOriginal.getArticulo().getNombre() : null);
            linea.setDescripcion(desc);
            linea.setCantidad(lineaOriginal.getCantidad() != null ? lineaOriginal.getCantidad().negate() : null);
            linea.setPrecioUnitario(lineaOriginal.getPrecioUnitario());
            linea.setPrecio(lineaOriginal.getPrecio());
            linea.setDescuento(lineaOriginal.getDescuento());
            linea.setIva(lineaOriginal.getIva());
            rectificativa.getFacturaLineas().add(linea);
        }
    }

    private void anotarRectificacionEnOriginal(Factura original, String numeroRectificativa, String motivo) {
        original.setObservacionesRevision(concatObservacion(
                original.getObservacionesRevision(),
                "Rectificada por " + numeroRectificativa
                        + (motivo != null && !motivo.isBlank() ? " - " + motivo : "")
        ));
        repository.save(original);
    }

    private void registrarEventoRectificativa(Factura original, Factura saved, String motivo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("facturaOriginalId", original.getId());
        metadata.put("rectificativaId", saved.getId());
        metadata.put("rectificativaNumero", saved.getNumero());
        metadata.put("motivo", motivo);
        metadata.put("fecha", LocalDateTime.now().toString());
        facturacionEventoService.registrarEvento(FacturacionEventoService.AMBITO_FACTURAS,
                "RECTIFICATIVA_CREADA", saved.getNumero(), metadata);
    }

    public String normalizarSerie(String serie) {
        if (serie == null || serie.isBlank()) {
            return "GEN";
        }

        String normalizada = serie.trim().toUpperCase().replaceAll("[^A-Z0-9_-]", "");
        return normalizada.isBlank() ? "GEN" : normalizada;
    }

    private void prepararFacturaParaGuardar(Factura factura) {
        if (factura.getFecha() == null) {
            factura.setFecha(LocalDate.now());
        }

        String serieNormalizada = normalizarSerie(factura.getSerie());
        factura.setSerie(serieNormalizada);

        if (factura.getNumero() == null || factura.getNumero().isBlank()) {
            boolean rectificativa = "RECTIFICATIVA".equalsIgnoreCase(factura.getTipoFactura());
            factura.setNumero(generarSiguienteNumero(serieNormalizada, factura.getFecha(), rectificativa));
        } else {
            Optional<Factura> existente = repository.findByNumero(factura.getNumero());
            if (existente.isPresent() && (factura.getId() == null || !existente.get().getId().equals(factura.getId()))) {
                throw new IllegalStateException("Ya existe una factura con el número: " + factura.getNumero());
            }
        }
    }

    private String concatObservacion(String observacionActual, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return observacionActual;
        }
        if (observacionActual == null || observacionActual.isBlank()) {
            return motivo.trim();
        }
        return observacionActual + "\n" + motivo.trim();
    }

    private FacturaSerieSequence crearSecuenciaFactura(String serie, int ejercicio, String prefijo) {
        FacturaSerieSequence nueva = new FacturaSerieSequence();
        nueva.setSerie(serie);
        nueva.setEjercicio(ejercicio);
        nueva.setUltimoNumero(obtenerUltimoNumeroExistente(serie, ejercicio, prefijo));
        return nueva;
    }

    private long obtenerUltimoNumeroExistente(String serie, int ejercicio, String prefijo) {
        String numeroPrefix = prefijo + "-" + serie + "-" + ejercicio + "-";
        return repository.findMaxNumeroSecuencialBySerieAndPrefijo(serie, numeroPrefix + "%", numeroPrefix.length());
    }

    public void recalcularTotalesDesdeLineas(Factura factura) {
        BigDecimal baseTotal = BigDecimal.ZERO;
        BigDecimal ivaTotal = BigDecimal.ZERO;
        for (FacturaLinea linea : factura.getFacturaLineas()) {
            BigDecimal cantidad = linea.getCantidad() != null ? linea.getCantidad() : BigDecimal.ZERO;
            BigDecimal precio = linea.getPrecioUnitario() != null ? linea.getPrecioUnitario() : BigDecimal.ZERO;
            BigDecimal descuento = linea.getDescuento() != null ? linea.getDescuento() : BigDecimal.ZERO;
            BigDecimal iva = linea.getIva() != null ? linea.getIva() : BigDecimal.ZERO;
            BigDecimal subtotal = FinancialMath.subtotalConDescuento(cantidad, precio, descuento);
            baseTotal = baseTotal.add(subtotal);
            ivaTotal = ivaTotal.add(FinancialMath.porcentaje(subtotal, iva));
        }
        factura.setBaseImponible(baseTotal);
        factura.setTotalIva(ivaTotal);
        factura.setTotal(baseTotal.add(ivaTotal));
    }
}
