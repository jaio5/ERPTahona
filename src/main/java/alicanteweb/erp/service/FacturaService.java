package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaSerieSequence;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.FacturaSerieSequenceRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final int MAX_REINTENTOS_SECUENCIA = 3;

    private final FacturaRepository repository;
    private final FacturaSerieSequenceRepository sequenceRepository;
    private final VerifactuService verifactuService;
    private final FacturacionEventoService facturacionEventoService;
    private final FiscalComplianceService fiscalComplianceService;
    private final EntityManager entityManager;

    public FacturaService(FacturaRepository repository,
                          FacturaSerieSequenceRepository sequenceRepository,
                          VerifactuService verifactuService,
                          FacturacionEventoService facturacionEventoService,
                          FiscalComplianceService fiscalComplianceService,
                          EntityManager entityManager) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
        this.verifactuService = verifactuService;
        this.facturacionEventoService = facturacionEventoService;
        this.fiscalComplianceService = fiscalComplianceService;
        this.entityManager = entityManager;
    }

    public List<Factura> findAll() {
        return repository.findAllWithCliente();
    }

    public Optional<Factura> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Factura> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public Optional<Factura> findBySerieAndNumero(String serie, String numero) {
        return repository.findBySerieAndNumero(normalizarSerie(serie), numero);
    }

    @Transactional
    public Factura save(Factura factura) {
        if (factura != null && factura.getId() != null) {
            Factura existente = repository.findById(factura.getId())
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
            validarFacturaMutable(existente);
        }
        return repository.save(factura);
    }

    @Transactional
    public void deleteById(Long id) {
        Factura existente = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        validarFacturaMutable(existente);
        repository.deleteById(id);
    }

    @Transactional
    public String generarSiguienteNumero(String serie, LocalDate fecha, boolean rectificativa) {
        String serieNormalizada = normalizarSerie(serie);
        int ejercicio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        String prefijo = rectificativa ? "R" : "F";

        for (int intento = 1; intento <= MAX_REINTENTOS_SECUENCIA; intento++) {
            try {
                FacturaSerieSequence sequence = sequenceRepository.findBySerieAndEjercicio(serieNormalizada, ejercicio)
                    .orElseGet(() -> crearSecuenciaFactura(serieNormalizada, ejercicio, prefijo));

                long siguienteNumero = sequence.getUltimoNumero() + 1L;
                sequence.setUltimoNumero(siguienteNumero);
                sequenceRepository.saveAndFlush(sequence);

                return String.format("%s-%s-%d-%04d", prefijo, serieNormalizada, ejercicio, siguienteNumero);
            } catch (DataIntegrityViolationException ex) {
                entityManager.clear();
                log.warn("Conflicto inicializando secuencia de factura {}/{} en intento {}",
                    serieNormalizada, ejercicio, intento);
            }
        }

        throw new IllegalStateException("No se pudo reservar un numero de factura para la serie " + serieNormalizada);
    }

    @Transactional
    public Factura aprobarYEmitir(Long facturaId) throws Exception {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"REVISION".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden emitir facturas en estado REVISION");
        }

        fiscalComplianceService.exigirListoParaEmision();

        verifactuService.enviarFacturaVerifactu(factura);
        Factura guardada = repository.save(factura);

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

        if (Boolean.TRUE.equals(factura.getVerifactuEnviada()) || "EMITIDA".equalsIgnoreCase(factura.getEstado())) {
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
    public Factura anularPorRectificativa(Long facturaId, String numeroRectificativa, String motivo) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        factura.setEstado("ANULADA");
        factura.setObservacionesRevision(concatObservacion(
                factura.getObservacionesRevision(),
                "Anulada por rectificativa " + numeroRectificativa + (motivo != null && !motivo.isBlank() ? " - " + motivo : "")
        ));
        Factura guardada = repository.save(factura);
        verifactuService.registrarAnulacionLocal(guardada, motivo);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("facturaId", guardada.getId());
        metadata.put("numero", guardada.getNumero());
        metadata.put("serie", guardada.getSerie());
        metadata.put("rectificativa", numeroRectificativa);
        metadata.put("motivo", motivo);
        metadata.put("fecha", LocalDateTime.now().toString());
        facturacionEventoService.registrarEvento(FacturacionEventoService.AMBITO_FACTURAS, "ANULACION_POR_RECTIFICATIVA", guardada.getNumero(), metadata);
        return guardada;
    }

    public String normalizarSerie(String serie) {
        if (serie == null || serie.isBlank()) {
            return "GEN";
        }

        String normalizada = serie.trim().toUpperCase().replaceAll("[^A-Z0-9_-]", "");
        return normalizada.isBlank() ? "GEN" : normalizada;
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

    private void validarFacturaMutable(Factura factura) {
        if (factura == null) {
            return;
        }
        if ("EMITIDA".equalsIgnoreCase(factura.getEstado()) || Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
            throw new IllegalStateException("La factura emitida no se puede modificar ni eliminar. Use una factura rectificativa.");
        }
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
}
