package alicanteweb.erp.service;

import alicanteweb.erp.entities.FacturaAlbaran;
import alicanteweb.erp.entities.FacturaAlbaranId;
import alicanteweb.erp.repository.FacturaAlbaranRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacturaAlbaranService {

    private final FacturaAlbaranRepository repository;

    public FacturaAlbaranService(FacturaAlbaranRepository repository) {
        this.repository = repository;
    }

    public List<FacturaAlbaran> findAll() {
        return repository.findAll();
    }

    public Optional<FacturaAlbaran> findById(FacturaAlbaranId id) {
        return repository.findById(id);
    }

    public List<FacturaAlbaran> findByFacturaId(Long facturaId) {
        return repository.findByFactura_Id(facturaId);
    }

    public List<FacturaAlbaran> findByAlbaranId(Long albaranId) {
        return repository.findByAlbaran_Id(albaranId);
    }

    @Transactional
    public FacturaAlbaran save(FacturaAlbaran fa) {
        return repository.save(fa);
    }

    @Transactional
    public void deleteById(FacturaAlbaranId id) {
        repository.deleteById(id);
    }
}

