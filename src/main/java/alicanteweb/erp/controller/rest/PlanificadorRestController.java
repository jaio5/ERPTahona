package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.OrdenProduccionService;
import alicanteweb.erp.service.PedidoService;
import alicanteweb.erp.service.PlanificadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web")
public class PlanificadorRestController {

    private final PedidoService pedidoService;
    private final AlbaranService albaranService;
    private final OrdenProduccionService ordenProduccionService;
    private final PlanificadorService planificadorService;
    private final ArticuloService articuloService;

    public PlanificadorRestController(PedidoService pedidoService,
                                       AlbaranService albaranService,
                                       OrdenProduccionService ordenProduccionService,
                                       PlanificadorService planificadorService,
                                       ArticuloService articuloService) {
        this.pedidoService = pedidoService;
        this.albaranService = albaranService;
        this.ordenProduccionService = ordenProduccionService;
        this.planificadorService = planificadorService;
        this.articuloService = articuloService;
    }

    @GetMapping("/planificador")
    public ResponseEntity<Map<String, Object>> plan(@RequestParam LocalDate fecha) {
        List<Pedido> pedidos = pedidoService.findByFechaBetween(fecha, fecha);
        List<AlbaranVenta> albaranes = albaranService.findByFecha(fecha);
        List<OrdenProduccion> ordenes = ordenProduccionService.findByFechaBetween(fecha, fecha);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fecha", fecha);
        result.put("pedidos", mapearPedidos(pedidos));
        result.put("albaranes", mapearAlbaranes(albaranes));
        result.put("ordenes", mapearOrdenes(ordenes));
        result.put("necesidades", construirNecesidadesList(pedidos, ordenes));

        return ResponseEntity.ok(result);
    }

    private List<Map<String, Object>> mapearPedidos(List<Pedido> pedidos) {
        return pedidos.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("numero", p.getNumero());
            m.put("cliente", p.getCliente() != null ? p.getCliente().getNombre() : null);
            m.put("estado", p.getEstado());
            return m;
        }).toList();
    }

    private List<Map<String, Object>> mapearAlbaranes(List<AlbaranVenta> albaranes) {
        return albaranes.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("numero", a.getNumero());
            m.put("cliente", a.getCliente() != null ? a.getCliente().getNombre() : null);
            return m;
        }).toList();
    }

    private List<Map<String, Object>> mapearOrdenes(List<OrdenProduccion> ordenes) {
        return ordenes.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("numero", o.getNumero());
            m.put("receta", o.getReceta() != null ? o.getReceta().getNombre() : null);
            m.put("estado", o.getEstado());
            return m;
        }).toList();
    }

    private List<Map<String, Object>> construirNecesidadesList(List<Pedido> pedidos, List<OrdenProduccion> ordenes) {
        Map<Long, BigDecimal> necesidades = planificadorService.calcularNecesidadesMateriaPrima(pedidos, ordenes);
        Map<Long, Articulo> articulosById = articuloService.findAllById(necesidades.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Articulo::getId, a -> a));

        List<Map<String, Object>> necesidadesList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : necesidades.entrySet()) {
            Articulo art = articulosById.get(entry.getKey());
            if (art == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("articuloId", art.getId());
            m.put("nombre", art.getNombre());
            m.put("necesario", entry.getValue());
            m.put("stock", art.getStock() != null ? art.getStock() : BigDecimal.ZERO);
            m.put("diferencia", art.getStock() != null
                    ? art.getStock().subtract(entry.getValue())
                    : entry.getValue().negate());
            necesidadesList.add(m);
        }
        return necesidadesList;
    }
}
