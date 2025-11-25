package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacturaService {

    private final FacturaRepository repository;

    public FacturaService(FacturaRepository repository) {
        this.repository = repository;
    }

    public List<Factura> findAll() {
        return repository.findAll();
    }

    public Optional<Factura> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Factura> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public boolean existsByNumero(String numero) {
        return repository.existsByNumero(numero);
    }

    @Transactional
    public Factura save(Factura factura) {
        return repository.save(factura);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Devuelve la última factura de un cliente por fecha descendente.
     */
    public Optional<Factura> findUltimaFacturaPorCliente(Long clienteId) {
        return repository.findTopByCliente_IdOrderByFechaDesc(clienteId);
    }
}
