package alicanteweb.erp.service;

import alicanteweb.erp.entities.AppccControl;
import alicanteweb.erp.repository.AppccControlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AppccControlService {

    private final AppccControlRepository repository;

    public AppccControlService(AppccControlRepository repository) {
        this.repository = repository;
    }

    public List<AppccControl> findAll() {
        return repository.findAll();
    }

    public Optional<AppccControl> findById(Long id) {
        return repository.findById(id);
    }

    public List<AppccControl> findByFecha(java.time.LocalDate fecha) {
        return repository.findByFecha(fecha);
    }

    public List<AppccControl> findByLoteId(Long loteId) {
        return repository.findByLoteId(loteId);
    }

    public List<AppccControl> buscar(String q) {
        return repository.buscar(q);
    }

    @Transactional
    public AppccControl save(AppccControl control) {
        if (control == null) throw new IllegalArgumentException("Control APPCC nulo");
        if (control.getHora() == null) control.setHora(java.time.LocalDateTime.now());
        return repository.save(control);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
