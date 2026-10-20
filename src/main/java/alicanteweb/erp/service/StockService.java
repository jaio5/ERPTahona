package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.ArticuloAlmacenRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.AlmacenRepository;
import alicanteweb.erp.repository.MovimientoStockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final MovimientoStockRepository movimientoStockRepository;
    private final ArticuloRepository articuloRepository;
    private final AlmacenRepository almacenRepository;
    private final ArticuloAlmacenRepository articuloAlmacenRepository;

    public StockService(MovimientoStockRepository movimientoStockRepository,
                        ArticuloRepository articuloRepository,
                        AlmacenRepository almacenRepository,
                        ArticuloAlmacenRepository articuloAlmacenRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.articuloRepository = articuloRepository;
        this.almacenRepository = almacenRepository;
        this.articuloAlmacenRepository = articuloAlmacenRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticuloAlmacen> stockPorAlmacen(Long articuloId) {
        return articuloAlmacenRepository.findByArticuloIdWithAlmacen(articuloId);
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> findMovimientosByFecha(LocalDate desde, LocalDate hasta) {
        return movimientoStockRepository.findByFechaBetween(desde, hasta).stream()
                .map(m -> {
                    java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("id", m.getId());
                    map.put("fecha", m.getFecha());
                    map.put("tipo", m.getTipo());
                    map.put("articuloId", m.getArticulo() != null ? m.getArticulo().getId() : null);
                    map.put("articulo", m.getArticulo() != null
                            ? java.util.Map.of("nombre", m.getArticulo().getNombre()) : null);
                    map.put("cantidad", m.getCantidad());
                    map.put("concepto", m.getConcepto());
                    map.put("importe", m.getImporte());
                    return map;
                })
                .toList();
    }

    /**
     * Ajusta el desglose de stock por almacén (ArticuloAlmacen).
     *
     * <p>Decisión: el stock que manda es el global del artículo, y ese sí se controla en
     * {@link #registrarSalida}. El desglose por almacén puede ir por detrás del global cuando el
     * stock se cargó desde la ficha del artículo o con un ajuste sin almacén (no crean fila ni la
     * actualizan). Por eso una salida que dejaría el almacén en negativo (o sin fila) no bloquea la
     * operación: el almacén se deja a 0 y se avisa en el log para poder cuadrarlo después.
     */
    private void actualizarStockAlmacen(Articulo art, Long almacenId, BigDecimal delta) {
        if (almacenId == null) return;
        java.util.Optional<ArticuloAlmacen> existente =
                articuloAlmacenRepository.findByArticuloIdAndAlmacenId(art.getId(), almacenId);
        ArticuloAlmacen aa = existente
                .orElseGet(() -> new ArticuloAlmacen(art, almacenRepository.getReferenceById(almacenId)));
        BigDecimal actual = aa.getStock() != null ? aa.getStock() : BigDecimal.ZERO;
        BigDecimal nuevo = actual.add(delta);
        if (nuevo.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Stock del almacén {} insuficiente para el artículo {} ({}): había {}{}, salida de {}. "
                            + "Se deja a 0; el stock global sí se ha descontado.",
                    almacenId, art.getId(), art.getCodigo(), actual,
                    existente.isPresent() ? "" : " (sin fila de almacén)", delta.negate());
            nuevo = BigDecimal.ZERO;
        }
        aa.setStock(nuevo);
        articuloAlmacenRepository.save(aa);
    }

    @Transactional
    public void registrarSalida(Long articuloId, Long almacenId, BigDecimal cantidad,
                                String concepto, String documentoTipo, Long documentoId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) return;
        Articulo art = articuloRepository.findByIdForUpdate(articuloId)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + articuloId));

        BigDecimal stockActual = art.getStock() != null ? art.getStock() : BigDecimal.ZERO;
        if (stockActual.compareTo(cantidad) < 0) {
            throw new IllegalStateException("Stock insuficiente para " + art.getNombre() +
                    ". Disponible: " + stockActual + ", Requerido: " + cantidad);
        }

        art.setStock(stockActual.subtract(cantidad));
        articuloRepository.save(art);
        actualizarStockAlmacen(art, almacenId, cantidad.negate());

        MovimientoStock mov = new MovimientoStock();
        mov.setArticulo(art);
        if (almacenId != null) {
            mov.setAlmacenOrigen(almacenRepository.getReferenceById(almacenId));
        }
        mov.setCantidad(cantidad);
        mov.setTipo("SALIDA");
        mov.setFecha(LocalDate.now());
        mov.setConcepto(concepto + (documentoTipo != null ? " [" + documentoTipo + " " + documentoId + "]" : ""));
        mov.setFechaCreacion(LocalDateTime.now());
        movimientoStockRepository.save(mov);
    }

    @Transactional
    public void registrarEntrada(Long articuloId, Long almacenId, BigDecimal cantidad,
                                 String concepto, String documentoTipo, Long documentoId) {
        registrarEntradaConCoste(articuloId, almacenId, cantidad, null, concepto, documentoTipo, documentoId);
    }

    /**
     * Entrada de stock con coste de adquisición: actualiza el coste medio
     * ponderado (PMP) del artículo antes de sumar el stock.
     */
    @Transactional
    public void registrarEntradaConCoste(Long articuloId, Long almacenId, BigDecimal cantidad,
                                         BigDecimal precioUnitario,
                                         String concepto, String documentoTipo, Long documentoId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) return;
        Articulo art = articuloRepository.findByIdForUpdate(articuloId)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + articuloId));

        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) >= 0) {
            art.setCosteMedio(calcularPmp(art, cantidad, precioUnitario));
        }

        BigDecimal stockActual = art.getStock() != null ? art.getStock() : BigDecimal.ZERO;
        art.setStock(stockActual.add(cantidad));
        articuloRepository.save(art);
        actualizarStockAlmacen(art, almacenId, cantidad);

        MovimientoStock mov = new MovimientoStock();
        mov.setArticulo(art);
        if (almacenId != null) {
            mov.setAlmacenDestino(almacenRepository.getReferenceById(almacenId));
        }
        mov.setCantidad(cantidad);
        mov.setTipo("ENTRADA");
        mov.setFecha(LocalDate.now());
        mov.setConcepto(concepto + (documentoTipo != null ? " [" + documentoTipo + " " + documentoId + "]" : ""));
        mov.setFechaCreacion(LocalDateTime.now());
        movimientoStockRepository.save(mov);
    }

    /**
     * PMP clásico: (stock_actual × pmp_actual + entrada × precio) / (stock_actual + entrada).
     * Si no hay PMP previo se parte del coste estándar del artículo, y en su defecto
     * del propio precio de entrada.
     */
    private BigDecimal calcularPmp(Articulo art, BigDecimal cantidadEntrada, BigDecimal precioUnitario) {
        BigDecimal stockActual = art.getStock() != null && art.getStock().compareTo(BigDecimal.ZERO) > 0
                ? art.getStock() : BigDecimal.ZERO;
        BigDecimal pmpActual = art.getCosteMedio() != null ? art.getCosteMedio()
                : (art.getCoste() != null ? art.getCoste() : precioUnitario);
        BigDecimal total = stockActual.add(cantidadEntrada);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return precioUnitario.setScale(4, java.math.RoundingMode.HALF_UP);
        }
        return stockActual.multiply(pmpActual)
                .add(cantidadEntrada.multiply(precioUnitario))
                .divide(total, 4, java.math.RoundingMode.HALF_UP);
    }

    @Transactional
    public void registrarAjuste(Long articuloId, BigDecimal cantidadNueva, String motivo) {
        Articulo art = articuloRepository.findByIdForUpdate(articuloId)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + articuloId));
        BigDecimal stockActual = art.getStock() != null ? art.getStock() : BigDecimal.ZERO;
        BigDecimal diferencia = cantidadNueva.subtract(stockActual);
        if (diferencia.compareTo(BigDecimal.ZERO) == 0) return;
        art.setStock(cantidadNueva);
        articuloRepository.save(art);

        MovimientoStock mov = new MovimientoStock();
        mov.setArticulo(art);
        mov.setCantidad(diferencia.abs());
        mov.setTipo(diferencia.compareTo(BigDecimal.ZERO) > 0 ? "ENTRADA" : "SALIDA");
        mov.setFecha(LocalDate.now());
        mov.setConcepto("Ajuste inventario: " + motivo);
        mov.setFechaCreacion(LocalDateTime.now());
        movimientoStockRepository.save(mov);
    }
}
