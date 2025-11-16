package alicanteweb.erp.service;

import alicanteweb.erp.entities.Agente;
import org.springframework.stereotype.Service;
import alicanteweb.erp.repository.AgenteRepository;

import java.util.List;

@Service
public class AgenteService {
    private final AgenteRepository agenteRepository;

    public AgenteService(AgenteRepository agenteRepository) {
        this.agenteRepository = agenteRepository;
    }

    public List<Agente> listarTodos() {
        return agenteRepository.findAll();
    }

    public Agente buscarPorCodigo(String codigo) {
        return agenteRepository.findById(codigo).orElse(null);
    }
}

