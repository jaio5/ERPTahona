package alicanteweb.erp.service;

import alicanteweb.erp.entities.Almacene;
import alicanteweb.erp.repository.AlmaceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AlmaceneService {

    private final AlmaceneRepository repository;

    public AlmaceneService(AlmaceneRepository repository) {
        this.repository = repository;
    }

    public List<Almacene> findAll() {
        return repository.findAll();
    }

    public Optional<Almacene> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Almacene> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Transactional
    public Almacene save(Almacene almacene) {
        return repository.save(almacene);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

