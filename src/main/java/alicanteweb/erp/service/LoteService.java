package alicanteweb.erp.service;

import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.entities.LoteInsumo;
import alicanteweb.erp.repository.LoteInsumoRepository;
import alicanteweb.erp.repository.LoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LoteService {

    private final LoteRepository loteRepository;
    private final LoteInsumoRepository loteInsumoRepository;

    public LoteService(LoteRepository loteRepository, LoteInsumoRepository loteInsumoRepository) {
        this.loteRepository = loteRepository;
        this.loteInsumoRepository = loteInsumoRepository;
    }

    public List<Lote> findAll() {
        return loteRepository.findAll();
    }

    public Page<Lote> findPage(String q, String estado, Pageable pageable) {
        return loteRepository.findPage(q, estado, pageable);
    }

    public Optional<Lote> findById(Long id) {
        return loteRepository.findById(id);
    }

    public Optional<Lote> findDetailById(Long id) {
        return loteRepository.findDetailById(id);
    }

    public Optional<Lote> findByCodigo(String codigo) {
        return loteRepository.findByCodigo(codigo);
    }

    public List<Lote> findByArticuloId(Long articuloId) {
        return loteRepository.findByArticuloId(articuloId);
    }

    public List<Lote> findByEstado(String estado) {
        return loteRepository.findByEstado(estado);
    }

    public List<Lote> findByFechaCaducidadBefore(LocalDate fecha) {
        return loteRepository.findByFechaCaducidadBefore(fecha);
    }

    public long countByFechaCaducidadBefore(LocalDate fecha) {
        return loteRepository.countByFechaCaducidadBefore(fecha);
    }

    public List<Lote> findByFechaCaducidadBetween(LocalDate inicio, LocalDate fin) {
        return loteRepository.findByFechaCaducidadBetween(inicio, fin);
    }

    public long countByFechaCaducidadBetween(LocalDate inicio, LocalDate fin) {
        return loteRepository.countByFechaCaducidadBetween(inicio, fin);
    }

    public long count() {
        return loteRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Lote> searchByCodigo(String q) {
        return loteRepository.findByCodigoContainingIgnoreCase(q);
    }

    public List<Lote> findByAlmacenId(Long almacenId) {
        return loteRepository.findByAlmacenId(almacenId);
    }

    @Transactional
    public Lote save(Lote lote) {
        if (lote == null) throw new IllegalArgumentException("Lote nulo");
        if (lote.getCodigo() == null || lote.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lote es obligatorio");
        }
        var opt = loteRepository.findByCodigo(lote.getCodigo().trim());
        if (opt.isPresent() && (lote.getId() == null || !opt.get().getId().equals(lote.getId()))) {
            throw new IllegalArgumentException("Ya existe un lote con el código: " + lote.getCodigo());
        }
        return loteRepository.save(lote);
    }

    @Transactional
    public void deleteById(Long id) {
        loteRepository.deleteById(id);
    }

    public List<LoteInsumo> getInsumos(Long loteProductoId) {
        return loteInsumoRepository.findByLoteProductoId(loteProductoId);
    }

    @Transactional
    public LoteInsumo addInsumo(LoteInsumo insumo) {
        return loteInsumoRepository.save(insumo);
    }

    /**
     * Trazabilidad completa: dada una materia prima, encuentra todos los lotes de producto
     * que la utilizaron (trazabilidad hacia adelante).
     */
    public List<LoteInsumo> findProductosQueUsaronInsumo(Long loteInsumoId) {
        return loteInsumoRepository.findByLoteInsumoId(loteInsumoId);
    }

    /**
     * Trazabilidad completa: dado un lote de producto terminado, encuentra todas
     * las materias primas utilizadas (trazabilidad hacia atrás).
     */
    public List<LoteInsumo> findInsumosDeProducto(Long loteProductoId) {
        return loteInsumoRepository.findByLoteProductoId(loteProductoId);
    }

    public List<LoteInsumo> findProductosQueUsaronInsumoDetail(Long loteInsumoId) {
        return loteInsumoRepository.findDetailByLoteInsumoId(loteInsumoId);
    }

    public List<LoteInsumo> findInsumosDeProductoDetail(Long loteProductoId) {
        return loteInsumoRepository.findDetailByLoteProductoId(loteProductoId);
    }

    @Transactional
    public void ajustarCantidad(Long loteId, java.math.BigDecimal cantidadAjuste) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado: " + loteId));
        lote.setCantidadActual(lote.getCantidadActual().add(cantidadAjuste));
        loteRepository.save(lote);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado: " + id));
        lote.setEstado("BAJA");
        loteRepository.save(lote);
    }
}
