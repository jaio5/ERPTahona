package alicanteweb.erp.service;

import alicanteweb.erp.entities.Zona;
import org.springframework.stereotype.Service;
import alicanteweb.erp.repository.ZonaRepository;

import java.util.List;

@Service
public class ZonaService {
    private final ZonaRepository zonaRepository;

    public ZonaService(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    public List<Zona> listar() { return zonaRepository.findAll(); }
    public Zona obtener(String codigo) { return zonaRepository.findById(codigo).orElse(null); }
}

