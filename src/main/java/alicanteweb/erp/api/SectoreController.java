package alicanteweb.erp.api;

import alicanteweb.erp.service.SectoreService;
import alicanteweb.erp.entities.Sectore;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sectores")
public class SectoreController {
    private final SectoreService sectoreService;
    public SectoreController(SectoreService sectoreService) { this.sectoreService = sectoreService; }

    @GetMapping
    public List<Sectore> listar() { return sectoreService.listar(); }

    @GetMapping("/{codigo}")
    public Sectore obtener(@PathVariable String codigo) { return sectoreService.obtener(codigo); }
}

