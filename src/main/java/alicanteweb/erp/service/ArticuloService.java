package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.repository.ArticuloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ArticuloService {

    private final ArticuloRepository repository;

    public ArticuloService(ArticuloRepository repository) {
        this.repository = repository;
    }

    public List<Articulo> findAll() {
        return repository.findAll();
    }

    public Optional<Articulo> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Articulo> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public List<Articulo> searchByDescripcion(String texto) {
        return repository.findByDescripcionContainingIgnoreCase(texto);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Transactional
    public Articulo save(Articulo articulo) {
        return repository.save(articulo);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

