package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.repository.ArticuloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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

    public long count() {
        return repository.count();
    }

    public Optional<Articulo> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Articulo> findAllById(Iterable<Long> ids) {
        return repository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public List<Articulo> findConStockBajo() {
        return repository.findConStockBajo();
    }

    public Optional<Articulo> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public List<Articulo> searchByDescripcion(String texto) {
        return repository.findByDescripcionContainingIgnoreCase(texto);
    }

    @Transactional
    public Articulo save(Articulo articulo) {
        if (articulo == null) throw new IllegalArgumentException("Articulo nulo");

        String codigo = articulo.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del artículo es obligatorio");
        }
        String nombre = articulo.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del artículo es obligatorio");
        }

        articulo.setCodigo(codigo.trim());
        articulo.setNombre(nombre.trim());
        if (articulo.getDescripcion() == null || articulo.getDescripcion().isBlank()) {
            articulo.setDescripcion(articulo.getNombre());
        }
        if (articulo.getPvp() == null) articulo.setPvp(BigDecimal.ZERO);
        if (articulo.getCoste() == null) articulo.setCoste(BigDecimal.ZERO);
        if (articulo.getStock() == null) articulo.setStock(BigDecimal.ZERO);
        if (articulo.getIva() == null) articulo.setIva(BigDecimal.ZERO);
        if (articulo.getActivo() == null) articulo.setActivo(true);

        var opt = repository.findByCodigo(articulo.getCodigo());
        if (opt.isPresent()) {
            Articulo existente = opt.get();
            // Si es distinto (nuevo artículo o id diferente) => duplicado
            if (articulo.getId() == null || !existente.getId().equals(articulo.getId())) {
                throw new IllegalArgumentException("Ya existe un artículo con el código: " + codigo);
            }
        }

        return repository.save(articulo);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Articulo articulo = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articulo no encontrado con id: " + id));
        articulo.setActivo(false);
        repository.save(articulo);
    }

    @Transactional
    public void activar(Long id) {
        Articulo articulo = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articulo no encontrado con id: " + id));
        articulo.setActivo(true);
        repository.save(articulo);
    }

    public List<Articulo> findByActivo(boolean activo) {
        return repository.findByActivo(activo);
    }

    public Page<Articulo> buscarParaApi(String q, Boolean activo, Pageable pageable) {
        String qNorm = (q != null && !q.isBlank()) ? q.trim() : null;
        return repository.buscarParaApi(qNorm, activo, pageable);
    }

    public Page<Articulo> buscarPaginado(String q, Pageable pageable) {
        return repository.buscarPaginado(
            (q != null && !q.isBlank()) ? q : null,
            pageable);
    }

    @Transactional(readOnly = true)
    public List<Articulo> search(String q, int limit) {
        return buscarPaginado(q, org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
    }
}
