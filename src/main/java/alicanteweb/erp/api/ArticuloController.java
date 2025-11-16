package alicanteweb.erp.api;

import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.entities.Articulo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos")
public class ArticuloController {

    private final ArticuloService articuloService;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @GetMapping
    public List<Articulo> list(@RequestParam(defaultValue = "20") int limit) {
        return articuloService.findTop(Math.min(Math.max(limit, 1), 100));
    }

    @GetMapping("/{codigo}")
    public Articulo getById(@PathVariable String codigo) {
        return articuloService.findById(codigo);
    }
}

