package alicanteweb.erp.api;

import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.entities.Cliente;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> list(@RequestParam(defaultValue = "20") int limit) {
        return clienteService.findTop(Math.min(Math.max(limit, 1), 100));
    }

    @GetMapping("/{codigo}")
    public Cliente getById(@PathVariable String codigo) {
        return clienteService.findById(codigo);
    }
}

