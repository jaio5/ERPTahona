package alicanteweb.erp.api;

import alicanteweb.erp.service.AgenteService;
import alicanteweb.erp.entities.Agente;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agentes")
public class AgenteController {

    private final AgenteService agenteService;

    public AgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @GetMapping
    public List<Agente> listar() {
        return agenteService.listarTodos();
    }

    @GetMapping("/{codigo}")
    public Agente obtener(@PathVariable String codigo) {
        return agenteService.buscarPorCodigo(codigo);
    }
}

