package alicanteweb.erp.service;

import alicanteweb.erp.entities.Merma;
import alicanteweb.erp.repository.MermaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MermaService {

    private final MermaRepository repository;
    private final StockService stockService;

    public MermaService(MermaRepository repository, StockService stockService) {
        this.repository = repository;
        this.stockService = stockService;
    }

    public List<Merma> findAll() {
        return repository.findAllConArticulo();
    }

    public List<Merma> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return repository.findByFechaBetween(inicio, fin);
    }

    public List<Merma> findByTipo(String tipo) {
        return repository.findByTipo(tipo);
    }

    public Optional<Merma> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Merma> findDetailById(Long id) {
        return repository.findDetailById(id);
    }

    @Transactional
    public Merma save(Merma merma) {
        if (merma.getArticulo() == null) throw new IllegalArgumentException("Artículo obligatorio");
        if (merma.getCantidad() == null || merma.getCantidad().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor que cero");
        }
        merma = repository.save(merma);
        stockService.registrarSalida(merma.getArticulo().getId(), null, merma.getCantidad(),
                "Merma " + merma.getMotivo(), "MERMA", merma.getId());
        return merma;
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
