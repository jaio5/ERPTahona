package alicanteweb.erp.service;

import org.springframework.stereotype.Service;
import alicanteweb.erp.repository.ArticuloRepository;

@Service
public class ArticuloService {

    private final ArticuloRepository articuloRepository;

    public ArticuloService(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }
}

