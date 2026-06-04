package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produccion")
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
    public Receta createReceta(@RequestBody Receta receta) {
        return recetaService.save(receta);
    }

    @PutMapping("/recetas/{id}")
    public ResponseEntity<Receta> updateReceta(@PathVariable Long id, @RequestBody Receta receta) {
        return recetaService.findById(id)
                .map(existing -> {
                    receta.setId(id);
                    return ResponseEntity.ok(recetaService.save(receta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ordenes")
    public List<OrdenProduccion> listOrdenes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long recetaId) {
        if (estado != null) return ordenProduccionService.findByEstado(estado);
        if (recetaId != null) return ordenProduccionService.findByRecetaId(recetaId);
        return ordenProduccionService.findAll();
    }

    @GetMapping("/ordenes/{id}")
    public ResponseEntity<OrdenProduccion> getOrden(@PathVariable Long id) {
        return ordenProduccionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ordenes")
    public OrdenProduccion createOrden(@RequestBody OrdenProduccion orden) {
        return ordenProduccionService.save(orden);
    }

    @PutMapping("/ordenes/{id}")
    public ResponseEntity<OrdenProduccion> updateOrden(@PathVariable Long id, @RequestBody OrdenProduccion orden) {
        return ordenProduccionService.findById(id)
                .map(existing -> {
                    orden.setId(id);
                    return ResponseEntity.ok(ordenProduccionService.save(orden));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ordenes/{id}/iniciar")
    public ResponseEntity<OrdenProduccion> iniciarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(ordenProduccionService.iniciarProduccion(id));
    }

    @PostMapping("/ordenes/{id}/finalizar")
    public ResponseEntity<OrdenProduccion> finalizarOrden(@PathVariable Long id,
                                                           @RequestParam java.math.BigDecimal cantidad,
                                                           @RequestParam java.math.BigDecimal merma) {
        return ResponseEntity.ok(ordenProduccionService.finalizarProduccion(id, cantidad, merma));
    }

    @GetMapping("/horneadas")
    public List<Horneada> listHorneadas(
            @RequestParam(required = false) Long ordenId,
            @RequestParam(required = false) String fecha) {
        if (ordenId != null) return horneadaService.findByOrdenProduccionId(ordenId);
        if (fecha != null) return horneadaService.findByFecha(java.time.LocalDate.parse(fecha));
        return horneadaService.findAll();
    }

    @PostMapping("/horneadas")
    public Horneada createHorneada(@RequestBody Horneada horneada) {
        return horneadaService.save(horneada);
    }
}
