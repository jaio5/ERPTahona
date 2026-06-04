package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionRestController {

    private final DevolucionService devolucionService;

    public DevolucionRestController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @GetMapping
    public List<Devolucion> listDevoluciones(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String estado) {
        if (clienteId != null) return devolucionService.findByClienteId(clienteId);
        if (estado != null) return devolucionService.findByEstado(estado);
        return devolucionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devolucion> getDevolucion(@PathVariable Long id) {
        return devolucionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/lineas")
    public ResponseEntity<List<DevolucionLinea>> getLineas(@PathVariable Long id) {
        return ResponseEntity.ok(devolucionService.getLineas(id));
    }

    @PostMapping
    public Devolucion createDevolucion(@RequestBody Devolucion devolucion) {
        return devolucionService.save(devolucion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Devolucion> updateDevolucion(@PathVariable Long id, @RequestBody Devolucion devolucion) {
        return devolucionService.findById(id)
                .map(existing -> {
                    devolucion.setId(id);
                    return ResponseEntity.ok(devolucionService.save(devolucion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<Devolucion> aceptar(@PathVariable Long id) {
        return ResponseEntity.ok(devolucionService.aceptarDevolucion(id));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Devolucion> rechazar(@PathVariable Long id, @RequestParam String motivo) {
        return ResponseEntity.ok(devolucionService.rechazarDevolucion(id, motivo));
    }
}
