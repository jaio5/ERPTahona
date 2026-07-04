package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.controller.dto.PageResponse;
import alicanteweb.erp.controller.dto.ProduccionDto;
import alicanteweb.erp.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/produccion")
@PreAuthorize("@permisos.puede('produccion', 'ver')")
public class ProduccionRestController {

    private final RecetaService recetaService;
    private final OrdenProduccionService ordenProduccionService;
    private final HorneadaService horneadaService;

    public ProduccionRestController(RecetaService recetaService,
                                     OrdenProduccionService ordenProduccionService,
                                     HorneadaService horneadaService) {
        this.recetaService = recetaService;
        this.ordenProduccionService = ordenProduccionService;
        this.horneadaService = horneadaService;
    }

    @GetMapping("/recetas")
    public List<Receta> listRecetas(@RequestParam(defaultValue = "true") boolean activo) {
        return recetaService.findByActivo(activo);
    }

    @GetMapping("/recetas/{id}")
    public ResponseEntity<Receta> getReceta(@PathVariable Long id) {
        return recetaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recetas/{id}/ingredientes")
    public ResponseEntity<List<RecetaIngrediente>> getIngredientes(@PathVariable Long id) {
        return ResponseEntity.ok(recetaService.getIngredientes(id));
    }

    @PostMapping("/recetas")
    @PreAuthorize("@permisos.puede('produccion', 'crear')")
    public Receta createReceta(@RequestBody Receta receta) {
        return recetaService.save(receta);
    }

    @PutMapping("/recetas/{id}")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<Receta> updateReceta(@PathVariable Long id, @RequestBody Receta receta) {
        return recetaService.findById(id)
                .map(existing -> {
                    receta.setId(id);
                    return ResponseEntity.ok(recetaService.save(receta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ordenes")
    public PageResponse<ProduccionDto.Orden> listOrdenes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long recetaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return PageResponse.from(ordenProduccionService.findPage(estado, recetaId,
                PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 200)),
                        Sort.by(Sort.Direction.DESC, "fecha", "id"))), ProduccionDto.Orden::from);
    }

    @GetMapping("/ordenes/{id}")
    public ResponseEntity<OrdenProduccion> getOrden(@PathVariable Long id) {
        return ordenProduccionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ordenes")
    @PreAuthorize("@permisos.puede('produccion', 'crear')")
    public OrdenProduccion createOrden(@RequestBody OrdenProduccion orden) {
        return ordenProduccionService.save(orden);
    }

    @PutMapping("/ordenes/{id}")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<OrdenProduccion> updateOrden(@PathVariable Long id, @RequestBody OrdenProduccion orden) {
        return ordenProduccionService.findById(id)
                .map(existing -> {
                    orden.setId(id);
                    return ResponseEntity.ok(ordenProduccionService.save(orden));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ordenes/{id}/iniciar")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<OrdenProduccion> iniciarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(ordenProduccionService.iniciarProduccion(id));
    }

    @PostMapping(value = "/ordenes/{id}/finalizar", consumes = "application/json")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<OrdenProduccion> finalizarOrden(@PathVariable Long id,
                                                           @Valid @RequestBody FinalizarOrdenRequest request) {
        return ResponseEntity.ok(ordenProduccionService.finalizarProduccion(id, request.cantidad(), request.merma()));
    }

    @PostMapping(value = "/ordenes/{id}/finalizar", params = {"cantidad", "merma"})
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<OrdenProduccion> finalizarOrdenCompat(@PathVariable Long id,
                                                                @RequestParam @DecimalMin("0.01") java.math.BigDecimal cantidad,
                                                                @RequestParam @DecimalMin("0.00") java.math.BigDecimal merma) {
        return ResponseEntity.ok(ordenProduccionService.finalizarProduccion(id, cantidad, merma));
    }

    @GetMapping("/horneadas")
    public PageResponse<ProduccionDto.HorneadaItem> listHorneadas(
            @RequestParam(required = false) Long ordenId,
            @RequestParam(required = false) java.time.LocalDate fecha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return PageResponse.from(horneadaService.findPage(ordenId, fecha,
                PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 200)),
                        Sort.by(Sort.Direction.DESC, "fecha", "id"))), ProduccionDto.HorneadaItem::from);
    }

    @PostMapping("/horneadas")
    @PreAuthorize("@permisos.puede('produccion', 'crear')")
    public Horneada createHorneada(@RequestBody Horneada horneada) {
        return horneadaService.save(horneada);
    }

    public record FinalizarOrdenRequest(
            @NotNull @DecimalMin(value = "0.01") java.math.BigDecimal cantidad,
            @NotNull @DecimalMin(value = "0.00") java.math.BigDecimal merma) {
    }
}
