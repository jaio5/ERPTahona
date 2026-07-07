package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.entities.LoteInsumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTOs de la API REST de trazabilidad. Evitan serializar entidades JPA con
 * asociaciones {@code LAZY} (articulo, almacen, ordenProduccion, lotes anidados):
 * como {@code open-in-view} está desactivado, Jackson las tocaría fuera de la
 * sesión de Hibernate y fallaría con {@code LazyInitializationException}.
 * Las factorías {@code from(...)} solo leen atributos ya cargados por las
 * consultas con fetch de los repositorios.
 */
public final class TrazabilidadDto {

    private TrazabilidadDto() {
    }

    public record LoteItem(
            Long id,
            String codigo,
            Long articuloId,
            String articuloCodigo,
            String articuloNombre,
            LocalDate fechaProduccion,
            LocalDate fechaCaducidad,
            BigDecimal cantidadInicial,
            BigDecimal cantidadActual,
            String estado,
            String origen,
            String numeroRegistroSanitario,
            Long ordenProduccionId,
            Long almacenId,
            String almacenNombre,
            String observaciones,
            LocalDateTime fechaCreacion
    ) {
        public static LoteItem from(Lote l) {
            Articulo a = l.getArticulo();
            Almacen alm = l.getAlmacen();
            return new LoteItem(
                    l.getId(),
                    l.getCodigo(),
                    a != null ? a.getId() : null,
                    a != null ? a.getCodigo() : null,
                    a != null ? a.getNombre() : null,
                    l.getFechaProduccion(),
                    l.getFechaCaducidad(),
                    l.getCantidadInicial(),
                    l.getCantidadActual(),
                    l.getEstado(),
                    l.getOrigen(),
                    l.getNumeroRegistroSanitario(),
                    l.getOrdenProduccion() != null ? l.getOrdenProduccion().getId() : null,
                    alm != null ? alm.getId() : null,
                    alm != null ? alm.getNombre() : null,
                    l.getObservaciones(),
                    l.getFechaCreacion());
        }
    }

    /** Referencia ligera a un lote para los enlaces de trazabilidad. */
    public record LoteRef(Long id, String codigo, Long articuloId, String articuloNombre) {
        public static LoteRef from(Lote l) {
            if (l == null) return null;
            Articulo a = l.getArticulo();
            return new LoteRef(
                    l.getId(),
                    l.getCodigo(),
                    a != null ? a.getId() : null,
                    a != null ? a.getNombre() : null);
        }
    }

    public record InsumoItem(
            Long id,
            BigDecimal cantidadUsada,
            LoteRef loteProducto,
            LoteRef loteInsumo
    ) {
        public static InsumoItem from(LoteInsumo li) {
            return new InsumoItem(
                    li.getId(),
                    li.getCantidadUsada(),
                    LoteRef.from(li.getLoteProducto()),
                    LoteRef.from(li.getLoteInsumo()));
        }
    }
}
