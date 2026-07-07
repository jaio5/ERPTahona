package alicanteweb.erp.controller.rest;

import alicanteweb.erp.controller.dto.TrazabilidadDto;
import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/trazabilidad")
@PreAuthorize("@permisos.puede('almacen', 'ver')")
public class TrazabilidadRestController {

    private final LoteService loteService;

    public TrazabilidadRestController(LoteService loteService) {
        this.loteService = loteService;
    }

    @GetMapping("/lotes")
    public List<TrazabilidadDto.LoteItem> listLotes(
            @RequestParam(required = false) Long articuloId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String caducidadAntes) {
        List<Lote> lotes;
        if (articuloId != null) {
            lotes = loteService.findByArticuloId(articuloId);
        } else if (estado != null) {
            lotes = loteService.findByEstado(estado);
        } else if (caducidadAntes != null) {
            lotes = loteService.findByFechaCaducidadBefore(java.time.LocalDate.parse(caducidadAntes));
        } else {
            lotes = loteService.findAll();
        }
        return lotes.stream().map(TrazabilidadDto.LoteItem::from).toList();
    }

    @GetMapping("/lotes/{id}")
    public ResponseEntity<TrazabilidadDto.LoteItem> getLote(@PathVariable Long id) {
        return loteService.findDetailById(id)
                .map(TrazabilidadDto.LoteItem::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/lotes")
    @PreAuthorize("@permisos.puede('almacen', 'crear')")
    public TrazabilidadDto.LoteItem createLote(@RequestBody Lote lote) {
        Lote saved = loteService.save(lote);
        return loteService.findDetailById(saved.getId())
                .map(TrazabilidadDto.LoteItem::from)
                .orElseGet(() -> TrazabilidadDto.LoteItem.from(saved));
    }

    @PutMapping("/lotes/{id}")
    @PreAuthorize("@permisos.puede('almacen', 'editar')")
    public ResponseEntity<TrazabilidadDto.LoteItem> updateLote(@PathVariable Long id, @RequestBody Lote lote) {
        return loteService.findById(id)
                .map(existing -> {
                    lote.setId(id);
                    Lote saved = loteService.save(lote);
                    TrazabilidadDto.LoteItem dto = loteService.findDetailById(saved.getId())
                            .map(TrazabilidadDto.LoteItem::from)
                            .orElseGet(() -> TrazabilidadDto.LoteItem.from(saved));
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lotes/caducar")
    public List<TrazabilidadDto.LoteItem> lotesProximosACaducar(@RequestParam(defaultValue = "7") int dias) {
        return loteService.findByFechaCaducidadBetween(
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now().plusDays(dias))
                .stream().map(TrazabilidadDto.LoteItem::from).toList();
    }

    @GetMapping("/lotes/{id}/trazabilidad-adelante")
    public ResponseEntity<List<TrazabilidadDto.InsumoItem>> trazabilidadAdelante(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.findProductosQueUsaronInsumoDetail(id)
                .stream().map(TrazabilidadDto.InsumoItem::from).toList());
    }

    @GetMapping("/lotes/{id}/trazabilidad-atras")
    public ResponseEntity<List<TrazabilidadDto.InsumoItem>> trazabilidadAtras(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.findInsumosDeProductoDetail(id)
                .stream().map(TrazabilidadDto.InsumoItem::from).toList());
    }

    @PostMapping("/lotes/{id}/insumos")
    @PreAuthorize("@permisos.puede('almacen', 'editar')")
    public ResponseEntity<TrazabilidadDto.InsumoItem> addInsumo(@PathVariable Long id, @RequestBody LoteInsumo insumo) {
        LoteInsumo saved = loteService.addInsumo(insumo);
        Lote producto = loteService.findDetailById(saved.getLoteProducto().getId()).orElse(null);
        Lote insumoLote = loteService.findDetailById(saved.getLoteInsumo().getId()).orElse(null);
        return ResponseEntity.ok(new TrazabilidadDto.InsumoItem(
                saved.getId(),
                saved.getCantidadUsada(),
                TrazabilidadDto.LoteRef.from(producto),
                TrazabilidadDto.LoteRef.from(insumoLote)));
    }
}
