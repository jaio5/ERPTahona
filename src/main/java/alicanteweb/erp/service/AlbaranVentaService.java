package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.repository.AlbaranVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AlbaranVentaService {

    private final AlbaranVentaRepository repository;

    public AlbaranVentaService(AlbaranVentaRepository repository) {
        this.repository = repository;
    }

    public List<AlbaranVenta> findAll() {
        return repository.findAllWithRelations();
    }

    public Optional<AlbaranVenta> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<AlbaranVenta> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public boolean existsByNumero(String numero) {
        return repository.existsByNumero(numero);
    }

    public List<AlbaranVenta> findByClienteId(Long clienteId) {
        return repository.findByCliente_Id(clienteId);
    }

    @Transactional
    public AlbaranVenta save(AlbaranVenta albaran) {
        return repository.save(albaran);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

