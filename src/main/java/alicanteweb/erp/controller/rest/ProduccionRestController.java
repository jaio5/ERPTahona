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
    public List<ProduccionDto.RecetaItem> listRecetas(@RequestParam(defaultValue = "true") boolean activo) {
        return recetaService.findByActivo(activo).stream().map(ProduccionDto.RecetaItem::from).toList();
    }

    @GetMapping("/recetas/{id}")
    public ResponseEntity<ProduccionDto.RecetaItem> getReceta(@PathVariable Long id) {
        return recetaService.findDetailById(id)
                .map(ProduccionDto.RecetaItem::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recetas/{id}/ingredientes")
    public ResponseEntity<List<RecetaIngrediente>> getIngredientes(@PathVariable Long id) {
        return ResponseEntity.ok(recetaService.getIngredientes(id));
    }

    @PostMapping("/recetas")
    @PreAuthorize("@permisos.puede('produccion', 'crear')")
    public ProduccionDto.RecetaItem createReceta(@RequestBody Receta receta) {
        Receta saved = recetaService.save(receta);
        return recetaService.findDetailById(saved.getId())
                .map(ProduccionDto.RecetaItem::from)
                .orElseGet(() -> ProduccionDto.RecetaItem.from(saved));
    }

    @PutMapping("/recetas/{id}")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<ProduccionDto.RecetaItem> updateReceta(@PathVariable Long id, @RequestBody Receta receta) {
        return recetaService.findById(id)
                .map(existing -> {
                    receta.setId(id);
                    Receta saved = recetaService.save(receta);
                    ProduccionDto.RecetaItem dto = recetaService.findDetailById(saved.getId())
                            .map(ProduccionDto.RecetaItem::from)
                            .orElseGet(() -> ProduccionDto.RecetaItem.from(saved));
                    return ResponseEntity.ok(dto);
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
    public ResponseEntity<ProduccionDto.Orden> getOrden(@PathVariable Long id) {
        return ordenProduccionService.findDetailById(id)
                .map(ProduccionDto.Orden::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ordenes")
    @PreAuthorize("@permisos.puede('produccion', 'crear')")
    public ProduccionDto.Orden createOrden(@RequestBody OrdenProduccion orden) {
        return ordenDto(ordenProduccionService.save(orden));
    }

    @PutMapping("/ordenes/{id}")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<ProduccionDto.Orden> updateOrden(@PathVariable Long id, @RequestBody OrdenProduccion orden) {
        return ordenProduccionService.findById(id)
                .map(existing -> {
                    orden.setId(id);
                    return ResponseEntity.ok(ordenDto(ordenProduccionService.save(orden)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ordenes/{id}/iniciar")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<ProduccionDto.Orden> iniciarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(ordenDto(ordenProduccionService.iniciarProduccion(id)));
    }

    @PostMapping(value = "/ordenes/{id}/finalizar", consumes = "application/json")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<ProduccionDto.Orden> finalizarOrden(@PathVariable Long id,
                                                              @Valid @RequestBody FinalizarOrdenRequest request) {
        return ResponseEntity.ok(ordenDto(
                ordenProduccionService.finalizarProduccion(id, request.cantidad(), request.merma())));
    }

    @PostMapping(value = "/ordenes/{id}/finalizar", params = {"cantidad", "merma"})
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public ResponseEntity<ProduccionDto.Orden> finalizarOrdenCompat(@PathVariable Long id,
                                                                    @RequestParam @DecimalMin("0.01") java.math.BigDecimal cantidad,
                                                                    @RequestParam @DecimalMin("0.00") java.math.BigDecimal merma) {
        return ResponseEntity.ok(ordenDto(ordenProduccionService.finalizarProduccion(id, cantidad, merma)));
    }

    /**
     * Mapea una orden recién guardada/mutada a DTO recargándola con fetch de sus
     * asociaciones (receta/artículo), para no serializar proxies lazy sin sesión.
     */
    private ProduccionDto.Orden ordenDto(OrdenProduccion orden) {
        return ordenProduccionService.findDetailById(orden.getId())
                .map(ProduccionDto.Orden::from)
                .orElseGet(() -> ProduccionDto.Orden.from(orden));
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
