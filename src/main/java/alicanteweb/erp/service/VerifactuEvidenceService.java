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

    public VerifactuEvidenceService(VerifactuEvidenceRepository repository) {
        this.repository = repository;
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
}

