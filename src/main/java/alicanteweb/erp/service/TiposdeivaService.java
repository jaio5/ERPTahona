package alicanteweb.erp.service;

import alicanteweb.erp.entities.Tiposdeiva;
import org.springframework.stereotype.Service;
import alicanteweb.erp.repository.TiposdeivaRepository;

import java.util.List;

@Service
public class TiposdeivaService {
    private final TiposdeivaRepository tiposdeivaRepository;

    public TiposdeivaService(TiposdeivaRepository tiposdeivaRepository) {
        this.tiposdeivaRepository = tiposdeivaRepository;
    }

    public List<Tiposdeiva> listar() { return tiposdeivaRepository.findAll(); }
    public Tiposdeiva obtener(Integer codigo) { return tiposdeivaRepository.findById(codigo).orElse(null); }
}

