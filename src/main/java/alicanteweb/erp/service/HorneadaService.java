package alicanteweb.erp.service;

import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.repository.HorneadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly = true)
public class HorneadaService {

    private final HorneadaRepository repository;

    public HorneadaService(HorneadaRepository repository) {
        this.repository = repository;
    }

    public List<Horneada> findAll() {
        return repository.findAll();
    }

    public Optional<Horneada> findById(Long id) {
        return repository.findById(id);
    }

    public List<Horneada> findByOrdenProduccionId(Long ordenProduccionId) {
        return repository.findByOrdenProduccionId(ordenProduccionId);
    }

    public List<Horneada> findByFecha(LocalDate fecha) {
        return repository.findByFecha(fecha);
    }

    public List<Horneada> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return repository.findByFechaBetween(inicio, fin);
    }

    public Page<Horneada> findPage(Long ordenId, LocalDate fecha, Pageable pageable) {
        return repository.findPage(ordenId, fecha, pageable);
    }

    public List<Horneada> buscar(String q) {
        return repository.buscar(q);
    }

    @Transactional
    public Horneada save(Horneada horneada) {
        if (horneada == null) throw new IllegalArgumentException("Horneada nula");
        return repository.save(horneada);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
