package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VerifactuEvidenceService {
    private static final Logger log = LoggerFactory.getLogger(VerifactuEvidenceService.class);
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_ENVIADO = "ENVIADO";
    private static final String ESTADO_VERIFICADO = "VERIFICADO";

    private final VerifactuEvidenceRepository repository;
    private final VerifactuService verifactuService;

    public VerifactuEvidenceService(VerifactuEvidenceRepository repository, VerifactuService verifactuService) {
        this.repository = repository;
        this.verifactuService = verifactuService;
    }

    public List<VerifactuEvidence> findAll() { return repository.findAll(); }
    public Optional<VerifactuEvidence> findById(Long id) { return repository.findById(id); }
    public Optional<VerifactuEvidence> findByFacturaId(String facturaId) {
        return repository.findFirstByFacturaIdOrderByFechaGeneracionRegistroDescIdDesc(facturaId)
                .or(() -> repository.findByFacturaId(facturaId));
    }
    public List<VerifactuEvidence> findAllByFacturaId(String facturaId) {
        return repository.findAllByFacturaIdOrderByFechaGeneracionRegistroAscIdAsc(facturaId);
    }
    public Optional<VerifactuEvidence> findByHash(String hash) { return repository.findByHash(hash); }
    public List<VerifactuEvidence> findBySerie(String serie) { return repository.findBySerieContainingIgnoreCase(serie); }
    public List<VerifactuEvidence> findByEstado(String estado) { return repository.findByEstado(estado); }
    public long countByEstado(String estado) { return repository.countByEstado(estado); }
    public Page<VerifactuEvidence> findPage(String estado, String q, Pageable pageable) {
        return repository.findPage(normalize(estado), normalize(q), pageable);
    }

    @Transactional
    public VerifactuEvidence save(VerifactuEvidence evidence) { return repository.save(evidence); }
    @Transactional
    public void deleteById(Long id) { repository.deleteById(id); }

    @Transactional
    public VerifactuEvidence registrarEvidenciaAEAT(String datosFactura, String serie, String numero) throws Exception {
        log.info("Registrando evidencia VeriFactu para factura {}/{}", serie, numero);
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setFacturaId(datosFactura);
        evidencia.setSerie(serie);
        evidencia.setNumero(numero);
        evidencia.setCreatedAt(Instant.now());
        evidencia.setFechaEmision(Instant.now());
        evidencia.setFechaGeneracionRegistro(Instant.now());
        evidencia.setTipoRegistro("ALTA");
        String hashAnterior = verifactuService.obtenerHashAnterior(serie);
        evidencia.setHashAnterior(hashAnterior);
        evidencia.setHash(verifactuService.generarHashEncadenado(datosFactura, hashAnterior));
        evidencia.setHuellaRegistro(evidencia.getHash());
        if (verifactuService.isEnabled()) {
            evidencia.setSignature(verifactuService.firmarDatos(datosFactura.getBytes(StandardCharsets.UTF_8)));
            evidencia.setCertFingerprint(verifactuService.getCertificateFingerprint());
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origen", "VerifactuEvidenceService");
        metadata.put("aeatDisponible", verifactuService.isAeatAvailable());
        metadata.put("fechaRegistro", Instant.now().toString());
        evidencia.setMetadata(metadata);
        evidencia.setEstado(verifactuService.isAeatAvailable() ? ESTADO_ENVIADO : ESTADO_PENDIENTE);
        if (verifactuService.isAeatAvailable()) evidencia.setFechaEnvio(Instant.now());
        return repository.save(evidencia);
    }

    @Transactional
    public VerifactuEvidence reenviarEvidencia(Long id) {
        VerifactuEvidence evidencia = getRequired(id);
        Map<String, Object> metadata = mutableMetadata(evidencia);
        metadata.put("fechaReenvio", Instant.now().toString());
        metadata.put("reenvioSolicitado", true);
        evidencia.setMetadata(metadata);
        if (verifactuService.isAeatAvailable()) {
            evidencia.setEstado(ESTADO_ENVIADO);
            evidencia.setFechaEnvio(Instant.now());
            evidencia.setErrorMessage(null);
        } else {
            evidencia.setEstado(ESTADO_PENDIENTE);
            evidencia.setErrorMessage("AEAT no disponible para reenvio");
        }
        return repository.save(evidencia);
    }

    @Transactional
    public VerifactuEvidence verificarEstadoAEAT(Long id) {
        VerifactuEvidence evidencia = getRequired(id);
        Map<String, Object> metadata = mutableMetadata(evidencia);
        metadata.put("fechaVerificacion", Instant.now().toString());
        metadata.put("aeatDisponible", verifactuService.isAeatAvailable());
        evidencia.setMetadata(metadata);
        if (verifactuService.isAeatAvailable() && ESTADO_ENVIADO.equalsIgnoreCase(evidencia.getEstado())) {
            evidencia.setEstado(ESTADO_VERIFICADO);
            evidencia.setErrorMessage(null);
        } else if (!verifactuService.isAeatAvailable()) {
            evidencia.setErrorMessage("AEAT no disponible para verificacion");
        }
        return repository.save(evidencia);
    }

    public boolean validarCadenaIntegridad(String serie) {
        return verifactuService.validarCadenaIntegridad(serie);
    }

    private VerifactuEvidence getRequired(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evidencia no encontrada con ID: " + id));
    }
    private Map<String, Object> mutableMetadata(VerifactuEvidence evidence) {
        return evidence.getMetadata() != null ? new HashMap<>(evidence.getMetadata()) : new HashMap<>();
    }
    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
