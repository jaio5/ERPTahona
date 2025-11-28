package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que gestiona el almacenamiento y consulta de las evidencias Verifactu
 * (registros que contienen hash, firma y metadatos del envío a la AEAT).
 *
 * Explicación para un estudiante de DAM:
 * - El servicio usa un repositorio JPA para persistir VerifactuEvidence.
 * - El método registrarEvidenciaAEAT combina funcionalidad del servicio AEAT
 *   (generar hash/firma) y persiste la evidencia en la BD.
 */
@Service
@Transactional(readOnly = true)
public class VerifactuEvidenceService {

    private final VerifactuEvidenceRepository repository;
    private final VerifactuAEATService aeatService;

    public VerifactuEvidenceService(VerifactuEvidenceRepository repository, VerifactuAEATService aeatService) {
        this.repository = repository;
        this.aeatService = aeatService;
    }

    public List<VerifactuEvidence> findAll() {
        return repository.findAll();
    }

    public Optional<VerifactuEvidence> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<VerifactuEvidence> findByFacturaId(String facturaId) {
        return repository.findByFacturaId(facturaId);
    }

    public Optional<VerifactuEvidence> findByHash(String hash) {
        return repository.findByHash(hash);
    }

    public List<VerifactuEvidence> findBySerie(String serie) {
        return repository.findBySerieContainingIgnoreCase(serie);
    }

    @Transactional
    public VerifactuEvidence save(VerifactuEvidence e) {
        return repository.save(e);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Crea y registra una evidencia en la BD y la envía (simulado) a la AEAT.
     * - datosFactura: contenido textual que se usará para calcular el hash
     * - serie/numero: datos complementarios
     *
     * Nota: este método delega en VerifactuAEATService para generar hash y firma.
     */
    @Transactional
    public VerifactuEvidence registrarEvidenciaAEAT(String datosFactura, String serie, String numero) throws Exception {
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setFacturaId(datosFactura);
        evidencia.setSerie(serie);
        evidencia.setNumero(numero);
        evidencia.setHash(aeatService.generarHash(datosFactura));
        evidencia.setSignature(aeatService.firmarDatos(datosFactura.getBytes(StandardCharsets.UTF_8)));
        evidencia.setCertFingerprint(aeatService.getCertFingerprint());
        evidencia.setMetadata(java.util.Collections.emptyMap()); // Inicializa como Map vacío
        evidencia.setFechaEmision(java.time.Instant.now()); // Usa Instant
        evidencia.setCreatedAt(java.time.Instant.now()); // Usa Instant
        String jsonEvidencia = "{\"facturaId\":\"" + datosFactura + "\",\"hash\":\"" + evidencia.getHash() + "\"}";
        String respuesta = aeatService.enviarAEAT(jsonEvidencia);
        evidencia.setMetadata(java.util.Collections.singletonMap("respuesta", respuesta));
        return repository.save(evidencia);
    }
}
