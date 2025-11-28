package alicanteweb.erp.service;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.repository.AlmacenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AlmacenService {
    private final AlmacenRepository repository;

    public AlmacenService(AlmacenRepository repository) {
        this.repository = repository;
    }

    public List<Almacen> findAll() {
        return repository.findAll();
    }

    public Optional<Almacen> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Almacen> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Transactional
    public Almacen save(Almacen almacen) {
        return repository.save(almacen);
    }

    @Transactional
    public void delete(Almacen almacen) {
        repository.delete(almacen);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

