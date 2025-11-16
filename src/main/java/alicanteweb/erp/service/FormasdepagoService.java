package alicanteweb.erp.service;

import alicanteweb.erp.entities.Formasdepago;
import org.springframework.stereotype.Service;
import alicanteweb.erp.repository.FormasdepagoRepository;

import java.util.List;

@Service
public class FormasdepagoService {
    private final FormasdepagoRepository formasdepagoRepository;

    public FormasdepagoService(FormasdepagoRepository formasdepagoRepository) {
        this.formasdepagoRepository = formasdepagoRepository;
    }

    public List<Formasdepago> listar() {
        return formasdepagoRepository.findAll();
    }

    public Formasdepago obtener(String codigo) {
        return formasdepagoRepository.findById(codigo).orElse(null);
    }
}

