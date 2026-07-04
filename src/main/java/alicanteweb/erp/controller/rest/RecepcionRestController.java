package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.Recepcion;
import alicanteweb.erp.entities.RecepcionLinea;
import alicanteweb.erp.service.RecepcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/web")
@PreAuthorize("@permisos.puede('compras', 'ver')")
public class RecepcionRestController {

    private final RecepcionService recepcionService;

    public RecepcionRestController(RecepcionService recepcionService) {
        this.recepcionService = recepcionService;
    }

    @GetMapping("/recepciones/{id}/lineas")
    public ResponseEntity<List<RecepcionLinea>> lineas(@PathVariable Long id) {
        return ResponseEntity.ok(recepcionService.findByRecepcionId(id));
    }

    @PostMapping("/recepciones/{id}/confirmar")
    @PreAuthorize("@permisos.puede('compras', 'editar')")
    public ResponseEntity<Recepcion> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(recepcionService.confirmar(id));
    }
}
