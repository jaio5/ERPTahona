package alicanteweb.erp.service;

import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.entities.Articulo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticuloService {

    private final ArticuloRepository articuloRepository;

    public ArticuloService(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    public List<Articulo> findTop(int limit) {
        return articuloRepository.findAll(
                PageRequest.of(0, limit, Sort.by("codigoArticulo"))
        ).getContent();
    }

    public Articulo findById(String codigo) {
        return articuloRepository.findById(codigo).orElse(null);
    }

    public List<Articulo> findAll() {
        return articuloRepository.findAll();
    }
}

