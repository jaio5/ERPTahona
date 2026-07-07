package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.RecetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PlanificadorService {

    private final RecetaRepository recetaRepository;

    public PlanificadorService(RecetaRepository recetaRepository) {
        this.recetaRepository = recetaRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> calcularNecesidadesMateriaPrima(List<Pedido> pedidos, List<OrdenProduccion> ordenes) {
        Map<Long, BigDecimal> cantidadesPorArticulo = agregarCantidadesPedidos(pedidos);
        agregarCantidadesOrdenes(ordenes, cantidadesPorArticulo);

        if (cantidadesPorArticulo.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Receta> recetasPorArticulo = cargarRecetasPorArticulo(cantidadesPorArticulo.keySet());

        Map<Long, BigDecimal> necesidades = new LinkedHashMap<>();
        cantidadesPorArticulo.forEach((articuloId, cantidad) ->
                agregarNecesidades(recetasPorArticulo.get(articuloId), cantidad, necesidades));
        return necesidades;
    }

    private Map<Long, BigDecimal> agregarCantidadesPedidos(List<Pedido> pedidos) {
        Map<Long, BigDecimal> cantidades = new LinkedHashMap<>();
        for (Pedido p : pedidos) {
            if (p.getLineas() == null) continue;
            for (PedidoLinea linea : p.getLineas()) {
                if (linea.getArticulo() == null || linea.getCantidad() == null) continue;
                cantidades.merge(linea.getArticulo().getId(), linea.getCantidad(), BigDecimal::add);
            }
        }
        return cantidades;
    }

    private void agregarCantidadesOrdenes(List<OrdenProduccion> ordenes, Map<Long, BigDecimal> cantidades) {
        for (OrdenProduccion o : ordenes) {
            if (o.getReceta() != null && o.getReceta().getArticuloResultante() != null
                    && o.getCantidadPlanificada() != null) {
                cantidades.merge(o.getReceta().getArticuloResultante().getId(),
                        o.getCantidadPlanificada(), BigDecimal::add);
            }
        }
    }

    private Map<Long, Receta> cargarRecetasPorArticulo(Set<Long> articuloIds) {
        Map<Long, Receta> recetasPorArticulo = new LinkedHashMap<>();
        recetaRepository.findActivasConIngredientesByArticuloIds(articuloIds)
                .forEach(r -> recetasPorArticulo.putIfAbsent(r.getArticuloResultante().getId(), r));
        return recetasPorArticulo;
    }

    private void agregarNecesidades(Receta receta, BigDecimal cantidad, Map<Long, BigDecimal> necesidades) {
        if (receta == null || cantidad == null) return;
        BigDecimal rendimiento = receta.getRendimientoCantidad();
        if (rendimiento == null || rendimiento.compareTo(BigDecimal.ZERO) <= 0) rendimiento = BigDecimal.ONE;
        BigDecimal factor = cantidad.divide(rendimiento, 4, RoundingMode.HALF_UP);
        if (receta.getIngredientes() == null) return;
        for (RecetaIngrediente ing : receta.getIngredientes()) {
            if (ing.getArticulo() == null || ing.getCantidad() == null) continue;
            necesidades.merge(ing.getArticulo().getId(), ing.getCantidad().multiply(factor), BigDecimal::add);
        }
    }
}
