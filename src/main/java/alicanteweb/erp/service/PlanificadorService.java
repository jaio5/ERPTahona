package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.RecetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class PlanificadorService {

    private static final Set<String> ESTADOS_PEDIDO_EXCLUIDOS = Set.of("CANCELADO", "ANULADO");
    private static final String SIN_CLIENTE = "Sin cliente";

    private final RecetaRepository recetaRepository;
    private final PedidoRepository pedidoRepository;

    public PlanificadorService(RecetaRepository recetaRepository, PedidoRepository pedidoRepository) {
        this.recetaRepository = recetaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Hoja de obrador: total pedido por artículo para una fecha (sin pedidos
     * cancelados/anulados), con desglose por cliente, stock actual y lo que
     * queda por elaborar (nunca negativo). Ordenado por nombre de artículo.
     */
    @Transactional(readOnly = true)
    public List<TotalArticulo> totalesPorArticulo(LocalDate fecha) {
        Map<Long, Acumulado> porArticulo = new LinkedHashMap<>();
        for (Pedido pedido : pedidoRepository.findByFecha(fecha)) {
            if (pedido.getEstado() != null
                    && ESTADOS_PEDIDO_EXCLUIDOS.contains(pedido.getEstado().trim().toUpperCase(Locale.ROOT))) {
                continue;
            }
            if (pedido.getLineas() == null) continue;
            String cliente = pedido.getCliente() != null && pedido.getCliente().getNombre() != null
                    ? pedido.getCliente().getNombre() : SIN_CLIENTE;
            for (PedidoLinea linea : pedido.getLineas()) {
                Articulo articulo = linea.getArticulo();
                if (articulo == null || articulo.getId() == null || linea.getCantidad() == null) continue;
                Acumulado acc = porArticulo.computeIfAbsent(articulo.getId(), id -> new Acumulado(articulo));
                acc.total = acc.total.add(linea.getCantidad());
                acc.porCliente.merge(cliente, linea.getCantidad(), BigDecimal::add);
            }
        }

        List<TotalArticulo> resultado = new ArrayList<>();
        for (Map.Entry<Long, Acumulado> e : porArticulo.entrySet()) {
            Acumulado acc = e.getValue();
            BigDecimal stock = acc.articulo.getStock() != null ? acc.articulo.getStock() : BigDecimal.ZERO;
            BigDecimal aElaborar = acc.total.subtract(stock).max(BigDecimal.ZERO);
            resultado.add(new TotalArticulo(e.getKey(), acc.articulo.getNombre(), acc.total,
                    ordenarDesglose(acc.porCliente), stock, aElaborar));
        }
        resultado.sort(Comparator.comparing(TotalArticulo::nombre,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return resultado;
    }

    /** Fila de la hoja de obrador. El desglose va de mayor a menor cantidad. */
    public record TotalArticulo(Long articuloId, String nombre, BigDecimal total,
                                Map<String, BigDecimal> desglose, BigDecimal stock, BigDecimal aElaborar) {
    }

    private static final class Acumulado {
        private final Articulo articulo;
        private BigDecimal total = BigDecimal.ZERO;
        private final Map<String, BigDecimal> porCliente = new LinkedHashMap<>();

        private Acumulado(Articulo articulo) {
            this.articulo = articulo;
        }
    }

    private static Map<String, BigDecimal> ordenarDesglose(Map<String, BigDecimal> porCliente) {
        Map<String, BigDecimal> ordenado = new LinkedHashMap<>();
        porCliente.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> ordenado.put(e.getKey(), e.getValue()));
        return ordenado;
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
