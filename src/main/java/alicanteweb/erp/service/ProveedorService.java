package alicanteweb.erp.service;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProveedorService {

    private final ProveedorRepository repository;

    public ProveedorService(ProveedorRepository repository) {
        this.repository = repository;
    }

    public List<Proveedor> findAll() {
        return repository.findAll();
    }

    public Optional<Proveedor> findById(Long id) {
        return repository.findById(id);
    }

    public List<Proveedor> searchByNombre(String texto) {
        return repository.findByNombreContainingIgnoreCase(texto);
    }

    @Transactional
    public Proveedor save(Proveedor p) {
        return repository.save(p);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
