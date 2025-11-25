package alicanteweb.erp.service;

import alicanteweb.erp.entities.Proveedore;
import alicanteweb.erp.repository.ProveedoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProveedoreService {

    private final ProveedoreRepository repository;

    public ProveedoreService(ProveedoreRepository repository) {
        this.repository = repository;
    }

    public List<Proveedore> findAll() {
        return repository.findAll();
    }

    public Optional<Proveedore> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Proveedore> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public List<Proveedore> searchByNombre(String texto) {
        return repository.findByNombreContainingIgnoreCase(texto);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Transactional
    public Proveedore save(Proveedore p) {
        return repository.save(p);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

