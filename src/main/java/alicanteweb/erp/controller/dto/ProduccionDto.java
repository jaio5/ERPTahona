package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.entities.OrdenProduccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ProduccionDto {
    private ProduccionDto() {
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
