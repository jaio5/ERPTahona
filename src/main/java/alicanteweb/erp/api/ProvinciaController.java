package alicanteweb.erp.api;

import alicanteweb.erp.service.ProvinciaService;
import alicanteweb.erp.entities.Provincia;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/provincias")
public class ProvinciaController {
    private final ProvinciaService provinciaService;
    public ProvinciaController(ProvinciaService provinciaService) { this.provinciaService = provinciaService; }

    @GetMapping
    public List<Provincia> listar() { return provinciaService.listar(); }

    @GetMapping("/{codigo}")
    public Provincia obtener(@PathVariable String codigo) { return provinciaService.obtener(codigo); }
}

