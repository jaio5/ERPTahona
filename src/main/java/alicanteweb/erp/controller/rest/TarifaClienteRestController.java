package alicanteweb.erp.controller.rest;

import alicanteweb.erp.service.TarifaClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/web")
@PreAuthorize("@permisos.puede('clientes', 'ver')")
public class TarifaClienteRestController {

    private final TarifaClienteService tarifaClienteService;

    public TarifaClienteRestController(TarifaClienteService tarifaClienteService) {
        this.tarifaClienteService = tarifaClienteService;
    }

    @GetMapping("/tarifas-cliente")
    public ResponseEntity<List<Map<String, Object>>> listarPorCliente(@RequestParam Long clienteId) {
        List<Map<String, Object>> result = tarifaClienteService.findByCliente(clienteId).stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("clienteId", t.getCliente() != null ? t.getCliente().getId() : null);
                    m.put("articuloId", t.getArticulo() != null ? t.getArticulo().getId() : null);
                    m.put("articulo", t.getArticulo() != null ? t.getArticulo().getNombre() : null);
                    m.put("precioEspecial", t.getPrecioEspecial());
                    m.put("descuento", t.getDescuento());
                    m.put("activo", t.getActivo());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    record TarifaClienteRequest(Long clienteId, Long articuloId, BigDecimal precioEspecial, BigDecimal descuento) {}

    @PostMapping("/tarifas-cliente")
    @PreAuthorize("@permisos.puede('clientes', 'editar')")
    public ResponseEntity<Map<String, Object>> guardar(@RequestBody TarifaClienteRequest req) {
        var saved = tarifaClienteService.crearTarifa(
                req.clienteId(), req.articuloId(), req.precioEspecial(), req.descuento());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("clienteId", saved.getCliente().getId());
        m.put("articuloId", saved.getArticulo().getId());
        m.put("precioEspecial", saved.getPrecioEspecial());
        m.put("descuento", saved.getDescuento());
        m.put("activo", saved.getActivo());
        return ResponseEntity.ok(m);
    }

    @DeleteMapping("/tarifas-cliente/{id}")
    @PreAuthorize("@permisos.puede('clientes', 'editar')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tarifaClienteService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
