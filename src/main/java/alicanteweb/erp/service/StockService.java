package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.ArticuloAlmacenRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.AlmacenRepository;
import alicanteweb.erp.repository.MovimientoStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockService {

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

    private void actualizarStockAlmacen(Articulo art, Long almacenId, BigDecimal delta) {
        if (almacenId == null) return;
        ArticuloAlmacen aa = articuloAlmacenRepository
                .findByArticuloIdAndAlmacenId(art.getId(), almacenId)
                .orElseGet(() -> new ArticuloAlmacen(art, almacenRepository.getReferenceById(almacenId)));
        BigDecimal nuevo = (aa.getStock() != null ? aa.getStock() : BigDecimal.ZERO).add(delta);
        if (nuevo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                "Stock insuficiente en almacén para artículo " + art.getCodigo() +
                ". Stock en almacén: " + aa.getStock() + ", requerido: " + delta.negate());
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
