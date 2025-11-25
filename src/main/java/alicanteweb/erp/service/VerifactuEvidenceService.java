package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VerifactuEvidenceService {

    private final VerifactuEvidenceRepository repository;
one que     private final VerifactuAEATService aeatService;

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

    public VerifactuEvidence registrarEvidenciaAEAT(String datosFactura, String serie, String numero) throws Exception {
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setFacturaId(datosFactura);
        evidencia.setSerie(serie);
        evidencia.setNumero(numero);
        evidencia.setHash(aeatService.generarHash(datosFactura));
        evidencia.setSignature(aeatService.firmarDatos(datosFactura.getBytes("UTF-8")));
        evidencia.setCertFingerprint(aeatService.getCertFingerprint());
        evidencia.setMetadata("{}");
        evidencia.setFechaEmision(java.time.LocalDateTime.now());
        evidencia.setCreatedAt(java.time.LocalDateTime.now());
        String jsonEvidencia = "{\"facturaId\":\"" + datosFactura + "\",\"hash\":\"" + evidencia.getHash() + "\"}";
        String respuesta = aeatService.enviarAEAT(jsonEvidencia);
        evidencia.setMetadata(respuesta);
        return repository.save(evidencia);
    }
}
