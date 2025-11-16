package alicanteweb.erp.api;

import alicanteweb.erp.service.TiposdeivaService;
import alicanteweb.erp.entities.Tiposdeiva;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tiposiva")
public class TiposdeivaController {
    private final TiposdeivaService tiposdeivaService;
    public TiposdeivaController(TiposdeivaService tiposdeivaService) { this.tiposdeivaService = tiposdeivaService; }

    @GetMapping
    public List<Tiposdeiva> listar() { return tiposdeivaService.listar(); }

    @GetMapping("/{codigo}")
    public Tiposdeiva obtener(@PathVariable Integer codigo) { return tiposdeivaService.obtener(codigo); }
}

