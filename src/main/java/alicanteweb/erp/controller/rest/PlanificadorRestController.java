package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.RecetaRepository;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.OrdenProduccionService;
import alicanteweb.erp.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web")
public class PlanificadorRestController {

    private final PedidoService pedidoService;
    private final AlbaranService albaranService;
    private final OrdenProduccionService ordenProduccionService;
    private final RecetaRepository recetaRepository;
    private final ArticuloService articuloService;

    public PlanificadorRestController(PedidoService pedidoService,
                                       AlbaranService albaranService,
                                       OrdenProduccionService ordenProduccionService,
                                       RecetaRepository recetaRepository,
                                       ArticuloService articuloService) {
        this.pedidoService = pedidoService;
        this.albaranService = albaranService;
        this.ordenProduccionService = ordenProduccionService;
        this.recetaRepository = recetaRepository;
        this.articuloService = articuloService;
    }

    @GetMapping("/planificador")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> plan(@RequestParam LocalDate fecha) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Pedido> pedidos = pedidoService.findByFechaBetween(fecha, fecha);
        List<AlbaranVenta> albaranes = albaranService.findByFecha(fecha);
        List<OrdenProduccion> ordenes = ordenProduccionService.findByFechaBetween(fecha, fecha);

        result.put("fecha", fecha);
        result.put("pedidos", pedidos.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("numero", p.getNumero());
            m.put("cliente", p.getCliente() != null ? p.getCliente().getNombre() : null);
            m.put("estado", p.getEstado());
            return m;
        }).toList());
        result.put("albaranes", albaranes.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("numero", a.getNumero());
            m.put("cliente", a.getCliente() != null ? a.getCliente().getNombre() : null);
            return m;
        }).toList());
        result.put("ordenes", ordenes.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("numero", o.getNumero());
            m.put("receta", o.getReceta() != null ? o.getReceta().getNombre() : null);
            m.put("estado", o.getEstado());
            return m;
        }).toList());

        // Necesidades de materia prima
        Map<Long, BigDecimal> productos = new LinkedHashMap<>();
        for (Pedido p : pedidos) {
            if (p.getLineas() == null) continue;
            for (PedidoLinea linea : p.getLineas()) {
                if (linea.getArticulo() == null || linea.getCantidad() == null) continue;
                productos.merge(linea.getArticulo().getId(), linea.getCantidad(), BigDecimal::add);
            }
        }
        for (OrdenProduccion o : ordenes) {
            if (o.getReceta() != null && o.getReceta().getArticuloResultante() != null
                    && o.getCantidadPlanificada() != null) {
                productos.merge(o.getReceta().getArticuloResultante().getId(),
                        o.getCantidadPlanificada(), BigDecimal::add);
            }
        }

        Map<Long, Receta> recetasByArticulo = new LinkedHashMap<>();
        if (!productos.isEmpty()) {
            recetaRepository.findActivasConIngredientesByArticuloIds(productos.keySet())
                    .forEach(r -> recetasByArticulo.putIfAbsent(r.getArticuloResultante().getId(), r));
        }
        Map<Long, BigDecimal> necesidades = new LinkedHashMap<>();
        productos.forEach((articuloId, cantidad) ->
                agregarNecesidades(recetasByArticulo.get(articuloId), cantidad, necesidades));

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
            m.put("diferencia", art.getStock() != null ? art.getStock().subtract(entry.getValue()) : entry.getValue().negate());
            necesidadesList.add(m);
        }
        result.put("necesidades", necesidadesList);

        return ResponseEntity.ok(result);
    }

    private void agregarNecesidades(Receta receta, BigDecimal cantidad, Map<Long, BigDecimal> necesidades) {
        if (receta == null || cantidad == null) return;
        BigDecimal rendimiento = receta.getRendimientoCantidad();
        if (rendimiento == null || rendimiento.compareTo(BigDecimal.ZERO) <= 0) rendimiento = BigDecimal.ONE;
        BigDecimal factor = cantidad.divide(rendimiento, 4, RoundingMode.HALF_UP);
        if (receta.getIngredientes() != null) {
            for (RecetaIngrediente ing : receta.getIngredientes()) {
                if (ing.getArticulo() == null || ing.getCantidad() == null) continue;
                BigDecimal cantIng = ing.getCantidad().multiply(factor);
                necesidades.merge(ing.getArticulo().getId(), cantIng, BigDecimal::add);
            }
        }
    }
}
