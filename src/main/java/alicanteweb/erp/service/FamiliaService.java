package alicanteweb.erp.service;

import alicanteweb.erp.repository.FamiliaRepository;
import alicanteweb.erp.entities.Familia;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamiliaService {

    private final FamiliaRepository familiaRepository;

    public FamiliaService(FamiliaRepository familiaRepository) {
        this.familiaRepository = familiaRepository;
    }

    public List<Familia> findAll() {
        return familiaRepository.findAll();
    }

    public Familia findById(String codigo) {
        return familiaRepository.findById(codigo).orElse(null);
    }
}

