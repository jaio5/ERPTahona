package alicanteweb.erp.service;

import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaLineaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacturaLineaService {

    private final FacturaLineaRepository repository;

    public FacturaLineaService(FacturaLineaRepository repository) {
        this.repository = repository;
    }

    public List<FacturaLinea> findAll() {
        return repository.findAll();
    }

    public Optional<FacturaLinea> findById(Long id) {
        return repository.findById(id);
    }

    public List<FacturaLinea> findByFacturaId(Long facturaId) {
        return repository.findByFactura_Id(facturaId);
    }

    public List<FacturaLinea> findByArticuloId(Long articuloId) {
        return repository.findByArticulo_Id(articuloId);
    }

    @Transactional
    public FacturaLinea save(FacturaLinea linea) {
        return repository.save(linea);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

