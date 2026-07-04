package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.controller.dto.DevolucionDto;
import alicanteweb.erp.controller.dto.PageResponse;
import alicanteweb.erp.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/devoluciones")
@PreAuthorize("@permisos.puede('ventas', 'ver')")
public class DevolucionRestController {

    private final DevolucionService devolucionService;

    public DevolucionRestController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @GetMapping
    public PageResponse<DevolucionDto> listDevoluciones(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return PageResponse.from(devolucionService.findPage(clienteId, estado,
                PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 200)),
                        Sort.by(Sort.Direction.DESC, "fecha", "id"))), DevolucionDto::from);
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
    @PreAuthorize("@permisos.puede('ventas', 'crear')")
    public Devolucion createDevolucion(@RequestBody Devolucion devolucion) {
        return devolucionService.save(devolucion);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public ResponseEntity<Devolucion> updateDevolucion(@PathVariable Long id, @RequestBody Devolucion devolucion) {
        return devolucionService.findById(id)
                .map(existing -> {
                    devolucion.setId(id);
                    return ResponseEntity.ok(devolucionService.save(devolucion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public ResponseEntity<Devolucion> aceptar(@PathVariable Long id) {
        return ResponseEntity.ok(devolucionService.aceptarDevolucion(id));
    }

    @PostMapping(value = "/{id}/rechazar", consumes = "application/json")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public ResponseEntity<Devolucion> rechazar(@PathVariable Long id,
                                                @Valid @RequestBody RechazarDevolucionRequest request) {
        return ResponseEntity.ok(devolucionService.rechazarDevolucion(id, request.motivo()));
    }

    @PostMapping(value = "/{id}/rechazar", params = "motivo")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public ResponseEntity<Devolucion> rechazarCompat(@PathVariable Long id,
                                                      @RequestParam @NotBlank String motivo) {
        return ResponseEntity.ok(devolucionService.rechazarDevolucion(id, motivo));
    }

    public record RechazarDevolucionRequest(@NotBlank String motivo) {
    }
}
