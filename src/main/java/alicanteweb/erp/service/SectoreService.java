package alicanteweb.erp.service;

import alicanteweb.erp.entities.Sectore;
import org.springframework.stereotype.Service;
import alicanteweb.erp.repository.SectoreRepository;

import java.util.List;

@Service
public class SectoreService {
    private final SectoreRepository sectoreRepository;

    public SectoreService(SectoreRepository sectoreRepository) {
        this.sectoreRepository = sectoreRepository;
    }

    public List<Sectore> listar() { return sectoreRepository.findAll(); }
    public Sectore obtener(String codigo) { return sectoreRepository.findById(codigo).orElse(null); }
}

