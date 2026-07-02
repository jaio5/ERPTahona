package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.MovimientoCaja;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.MovimientoCajaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Venta mostrador (TPV): genera una factura simplificada emitida (con registro
 * VeriFactu), su cobro inmediato en cartera y el movimiento de caja si es efectivo.
 */
@Service
public class TpvService {

    private static final Logger log = LoggerFactory.getLogger(TpvService.class);

    /** Límite legal de la factura simplificada (art. 4 RD 1619/2012). */
    static final BigDecimal LIMITE_SIMPLIFICADA = new BigDecimal("3000.00");

    public static final String SERIE_TPV = "TPV";

    private final FacturaService facturaService;
    private final ArticuloService articuloService;
    private final CarteraService carteraService;
    private final MovimientoCajaRepository movimientoCajaRepository;

    public TpvService(FacturaService facturaService,
                      ArticuloService articuloService,
                      CarteraService carteraService,
                      MovimientoCajaRepository movimientoCajaRepository) {
        this.facturaService = facturaService;
        this.articuloService = articuloService;
        this.carteraService = carteraService;
        this.movimientoCajaRepository = movimientoCajaRepository;
    }

    @Transactional(readOnly = true)
    public List<Articulo> articulosVendibles() {
        return articuloService.findByActivo(true).stream()
                .filter(a -> a.getPvp() != null && a.getPvp().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    /**
     * Registra una venta de mostrador completa: factura simplificada emitida,
     * cobro y movimiento de caja (efectivo).
     */
    @Transactional
    public ResultadoVenta vender(List<LineaTpv> lineas, String formaPago, Usuario usuario) {
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("La venta no tiene líneas");
        }
        String forma = formaPago != null && !formaPago.isBlank() ? formaPago.toUpperCase() : "EFECTIVO";

        Factura factura = new Factura();
        factura.setTipoFactura("SIMPLIFICADA");
        factura.setSerie(SERIE_TPV);
        factura.setFecha(LocalDate.now());
        factura.setMedioCobro(forma);

        for (LineaTpv lineaTpv : lineas) {
            Articulo articulo = articuloService.findById(lineaTpv.articuloId())
                    .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + lineaTpv.articuloId()));
            if (lineaTpv.cantidad() == null || lineaTpv.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para " + articulo.getNombre());
            }
            FacturaLinea linea = new FacturaLinea();
            linea.setFactura(factura);
            linea.setArticulo(articulo);
            linea.setDescripcion(articulo.getNombre());
            linea.setCantidad(lineaTpv.cantidad());
            linea.setPrecioUnitario(articulo.getPvp());
            linea.setIva(articulo.getIva() != null ? articulo.getIva() : BigDecimal.ZERO);
            factura.getFacturaLineas().add(linea);
        }
        facturaService.recalcularTotalesDesdeLineas(factura);

        if (factura.getTotal().compareTo(LIMITE_SIMPLIFICADA) > 0) {
            throw new IllegalArgumentException("Una factura simplificada no puede superar los 3.000 EUR");
        }

        Factura guardada = facturaService.save(factura);
        facturaService.pasarARevision(guardada.getId());
        Factura emitida = facturaService.aprobarYEmitir(guardada.getId());

        carteraService.registrarCobro(emitida.getId(), LocalDate.now(), emitida.getTotal(),
                forma, "TPV", "Venta mostrador", usuario);

        if ("EFECTIVO".equals(forma)) {
            MovimientoCaja movimiento = new MovimientoCaja();
            movimiento.setTipo("INGRESO");
            movimiento.setFecha(LocalDate.now());
            movimiento.setConcepto("Ticket " + emitida.getNumero());
            movimiento.setImporte(emitida.getTotal());
            movimiento.setCategoria("TPV");
            movimiento.setFactura(emitida);
            movimientoCajaRepository.save(movimiento);
        }

        log.info("Venta TPV: {} total={} forma={}", emitida.getNumero(), emitida.getTotal(), forma);
        return new ResultadoVenta(emitida.getId(), emitida.getNumero(), emitida.getTotal());
    }

    public record LineaTpv(Long articuloId, BigDecimal cantidad) {
    }

    public record ResultadoVenta(Long facturaId, String numero, BigDecimal total) {
    }
}
