package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.entities.OrdenProduccion;
import alicanteweb.erp.entities.Receta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ProduccionDto {
    private ProduccionDto() {
    }

    /**
     * Receta sin la colección {@code ingredientes} (lazy): se consulta aparte en
     * {@code /recetas/{id}/ingredientes}. Evita LazyInitializationException al
     * serializar con open-in-view desactivado.
     */
    public record RecetaItem(
            Long id, String codigo, String nombre, String descripcion,
            Integer tiempoPreparacion, Integer tiempoHorneado, Integer temperaturaHorneado,
            BigDecimal rendimientoCantidad, String unidadRendimiento,
            Long articuloResultanteId, String articuloResultante,
            Boolean activo, String alergenos
    ) {
        public static RecetaItem from(Receta r) {
            Articulo a = r.getArticuloResultante();
            return new RecetaItem(r.getId(), r.getCodigo(), r.getNombre(), r.getDescripcion(),
                    r.getTiempoPreparacion(), r.getTiempoHorneado(), r.getTemperaturaHorneado(),
                    r.getRendimientoCantidad(), r.getUnidadRendimiento(),
                    a != null ? a.getId() : null,
                    a != null ? a.getNombre() : null,
                    r.getActivo(), r.getAlergenos());
        }
    }

    public record Orden(
            Long id, String numero, LocalDate fecha, String estado,
            Long recetaId, String receta, Long articuloId, String articulo,
            BigDecimal cantidadPlanificada, BigDecimal cantidadProducida, BigDecimal merma
    ) {
        public static Orden from(OrdenProduccion o) {
            return new Orden(o.getId(), o.getNumero(), o.getFecha(), o.getEstado(),
                    o.getReceta() != null ? o.getReceta().getId() : null,
                    o.getReceta() != null ? o.getReceta().getNombre() : null,
                    o.getArticulo() != null ? o.getArticulo().getId() : null,
                    o.getArticulo() != null ? o.getArticulo().getNombre() : null,
                    o.getCantidadPlanificada(), o.getCantidadProducida(), o.getMerma());
        }
    }

    public record HorneadaItem(
            Long id, Long ordenId, String orden, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin, Integer temperaturaInicial,
            Integer temperaturaFinal, BigDecimal cantidadProducida,
            BigDecimal merma, String resultado
    ) {
        public static HorneadaItem from(Horneada h) {
            return new HorneadaItem(h.getId(),
                    h.getOrdenProduccion() != null ? h.getOrdenProduccion().getId() : null,
                    h.getOrdenProduccion() != null ? h.getOrdenProduccion().getNumero() : null,
                    h.getFecha(), h.getHoraInicio(), h.getHoraFin(),
                    h.getTemperaturaInicial(), h.getTemperaturaFinal(),
                    h.getCantidadProducida(), h.getMerma(), h.getResultado());
        }
    }
}
