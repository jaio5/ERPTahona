package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.HojaRutaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/web")
@PreAuthorize("@permisos.puede('reparto', 'ver')")
public class HojaRutaRestController {

    private final HojaRutaService hojaRutaService;
    private final AlbaranService albaranService;

    public HojaRutaRestController(HojaRutaService hojaRutaService,
                                   AlbaranService albaranService) {
        this.hojaRutaService = hojaRutaService;
        this.albaranService = albaranService;
    }

    @GetMapping("/hojas-ruta/{id}/entregas")
    public ResponseEntity<List<Map<String, Object>>> entregas(@PathVariable Long id) {
        List<Map<String, Object>> result = hojaRutaService.getEntregasDetalle(id).stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", e.getId());
                    m.put("orden", e.getOrden());
                    m.put("entregado", e.getEntregado());
                    m.put("estadoEntrega", e.getEstadoEntrega());
                    m.put("incidencia", e.getIncidencia());
                    m.put("importeCobrado", e.getImporteCobrado());
                    m.put("medioCobro", e.getMedioCobro());
                    m.put("latitud", e.getLatitud());
                    m.put("longitud", e.getLongitud());
                    m.put("albaranId", e.getAlbaran() != null ? e.getAlbaran().getId() : null);
                    if (e.getCliente() != null) {
                        m.put("clienteNombre", e.getCliente().getNombre());
                        m.put("clienteDireccion", e.getCliente().getDireccion());
                        m.put("clientePoblacion", e.getCliente().getPoblacion());
                    }
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/hojas-ruta/{id}/vincular-albaranes")
    @PreAuthorize("@permisos.puede('reparto', 'editar')")
    public ResponseEntity<Void> vincularAlbaranes(@PathVariable Long id,
                                                   @RequestBody List<Long> albaranIds) {
        hojaRutaService.vincularAlbaranes(id, albaranIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/albaranes/pendientes-reparto")
    public ResponseEntity<List<Map<String, Object>>> albaranesPendientesReparto(@RequestParam LocalDate fecha) {
        List<AlbaranVenta> todos = albaranService.findByFecha(fecha);
        List<Long> yaAsignados = hojaRutaService.getAlbaranIdsAsignados();
        List<Map<String, Object>> pendientes = todos.stream()
                .filter(a -> !yaAsignados.contains(a.getId()))
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("numero", a.getNumero());
                    m.put("fecha", a.getFecha());
                    m.put("cliente", a.getCliente() != null ? a.getCliente().getNombre() : null);
                    m.put("total", a.getTotal());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(pendientes);
    }
}
