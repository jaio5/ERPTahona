package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.repository.AlbaranVentaLineaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AlbaranVentaLineaService {

    private final AlbaranVentaLineaRepository repository;

    public AlbaranVentaLineaService(AlbaranVentaLineaRepository repository) {
        this.repository = repository;
    }

    public List<AlbaranVentaLinea> findByAlbaranId(Long albaranId) {
        return repository.findByAlbaranIdWithArticulo(albaranId);
    }

    @Transactional
    public AlbaranVentaLinea save(AlbaranVentaLinea linea) {
        return repository.save(linea);
    }
}

