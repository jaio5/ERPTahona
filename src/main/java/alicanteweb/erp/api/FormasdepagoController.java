package alicanteweb.erp.api;

import alicanteweb.erp.service.FormasdepagoService;
import alicanteweb.erp.entities.Formasdepago;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formaspago")
public class FormasdepagoController {
    private final FormasdepagoService formasdepagoService;

    public FormasdepagoController(FormasdepagoService formasdepagoService) {
        this.formasdepagoService = formasdepagoService;
    }

    @GetMapping
    public List<Formasdepago> listar() {
        return formasdepagoService.listar();
    }

    @GetMapping("/{codigo}")
    public Formasdepago obtener(@PathVariable String codigo) {
        return formasdepagoService.obtener(codigo);
    }
}


