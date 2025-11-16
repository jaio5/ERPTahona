package alicanteweb.erp.service;

import alicanteweb.erp.entities.Provincia;
import alicanteweb.erp.repository.ProvinciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinciaService {
    private final ProvinciaRepository provinciaRepository;

    public ProvinciaService(ProvinciaRepository provinciaRepository) {
        this.provinciaRepository = provinciaRepository;
    }

    public List<Provincia> listar() {
        return provinciaRepository.findAll();
    }

    public Provincia obtener(String codigo) {
        return provinciaRepository.findById(codigo).orElse(null);
    }
}


