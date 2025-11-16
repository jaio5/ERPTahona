package alicanteweb.erp.api;

import alicanteweb.erp.service.FamiliaService;
import alicanteweb.erp.entities.Familia;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/familias")
public class FamiliaController {

    private final FamiliaService familiaService;

    public FamiliaController(FamiliaService familiaService) {
        this.familiaService = familiaService;
    }

    @GetMapping
    public List<Familia> list() {
        return familiaService.findAll();
    }

    @GetMapping("/{codigo}")
    public Familia getById(@PathVariable String codigo) {
        return familiaService.findById(codigo);
    }
}

