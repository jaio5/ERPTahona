package alicanteweb.erp.api;

import alicanteweb.erp.service.ZonaService;
import alicanteweb.erp.entities.Zona;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/zonas")
public class ZonaController {
    private final ZonaService zonaService;
    public ZonaController(ZonaService zonaService) { this.zonaService = zonaService; }

    @GetMapping
    public List<Zona> listar() { return zonaService.listar(); }

    @GetMapping("/{codigo}")
    public Zona obtener(@PathVariable String codigo) { return zonaService.obtener(codigo); }
}

