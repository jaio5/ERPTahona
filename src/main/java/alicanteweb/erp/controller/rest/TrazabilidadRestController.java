package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trazabilidad")
public class TrazabilidadRestController {

    private final LoteService loteService;

    public TrazabilidadRestController(LoteService loteService) {
        this.loteService = loteService;
    }

    @GetMapping("/lotes")
    public List<Lote> listLotes(
            @RequestParam(required = false) Long articuloId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String caducidadAntes) {
        if (articuloId != null) return loteService.findByArticuloId(articuloId);
        if (estado != null) return loteService.findByEstado(estado);
        if (caducidadAntes != null)
            return loteService.findByFechaCaducidadBefore(java.time.LocalDate.parse(caducidadAntes));
        return loteService.findAll();
    }

    @GetMapping("/lotes/{id}")
    public ResponseEntity<Lote> getLote(@PathVariable Long id) {
        return loteService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/lotes")
    public Lote createLote(@RequestBody Lote lote) {
        return loteService.save(lote);
    }

    @PutMapping("/lotes/{id}")
    public ResponseEntity<Lote> updateLote(@PathVariable Long id, @RequestBody Lote lote) {
        return loteService.findById(id)
                .map(existing -> {
                    lote.setId(id);
                    return ResponseEntity.ok(loteService.save(lote));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lotes/caducar")
    public List<Lote> lotesProximosACaducar(@RequestParam(defaultValue = "7") int dias) {
        return loteService.findByFechaCaducidadBetween(
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusDays(dias));
    }

    @GetMapping("/lotes/{id}/trazabilidad-adelante")
    public ResponseEntity<List<LoteInsumo>> trazabilidadAdelante(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.findProductosQueUsaronInsumo(id));
    }

    @GetMapping("/lotes/{id}/trazabilidad-atras")
    public ResponseEntity<List<LoteInsumo>> trazabilidadAtras(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.findInsumosDeProducto(id));
    }

    @PostMapping("/lotes/{id}/insumos")
    public ResponseEntity<LoteInsumo> addInsumo(@PathVariable Long id, @RequestBody LoteInsumo insumo) {
        return ResponseEntity.ok(loteService.addInsumo(insumo));
    }
}
