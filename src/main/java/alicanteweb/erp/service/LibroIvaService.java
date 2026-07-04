package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.util.FinancialMath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Libros registro de IVA (facturas emitidas y recibidas) con desglose por tipo
 * impositivo, y borradores de los modelos 303 (IVA) y 111 (retenciones).
 *
 * Los libros usan el mismo criterio de cálculo de base/cuota por línea que los
 * registros VeriFactu (FinancialMath), de modo que ambos cuadran entre sí.
 */
@Service
public class LibroIvaService {

    private final FacturaRepository facturaRepository;
    private final FacturaCompraRepository facturaCompraRepository;

    public LibroIvaService(FacturaRepository facturaRepository,
                           FacturaCompraRepository facturaCompraRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
    }

    // ───────────────────────── Libro de facturas emitidas ─────────────────────────

    @Transactional(readOnly = true)
    public List<LineaLibro> libroEmitidas(LocalDate desde, LocalDate hasta) {
        List<LineaLibro> lineas = new ArrayList<>();
        for (Factura factura : facturaRepository.findByFechaBetweenAndEstado(desde, hasta, "EMITIDA")) {
            Map<BigDecimal, BigDecimal[]> porTipo = desglosePorTipo(factura.getFacturaLineas());
            // En facturas con varios tipos impositivos el recargo, la retención y el
            // total de la factura solo van en la primera línea del desglose; en las
            // siguientes quedan a null (— en pantalla, vacío en CSV) para no sumar doble.
            boolean primera = true;
            for (Map.Entry<BigDecimal, BigDecimal[]> e : porTipo.entrySet()) {
                lineas.add(new LineaLibro(
                        factura.getFecha(),
                        numeroCompleto(factura.getSerie(), factura.getNumero()),
                        factura.getCliente() != null ? factura.getCliente().getCif() : null,
                        factura.getCliente() != null ? factura.getCliente().getNombre() : "(sin cliente)",
                        factura.getTipoFactura(),
                        e.getKey(),
                        e.getValue()[0],
                        e.getValue()[1],
                        primera ? nvl(factura.getTotalRecargo()) : null,
                        primera ? nvl(factura.getRetencionIrpf()) : null,
                        primera ? nvl(factura.getTotal()) : null));
                primera = false;
            }
        }
        return lineas;
    }

    // ───────────────────────── Libro de facturas recibidas ─────────────────────────

    @Transactional(readOnly = true)
    public List<LineaLibro> libroRecibidas(LocalDate desde, LocalDate hasta) {
        List<LineaLibro> lineas = new ArrayList<>();
        for (FacturaCompra fc : facturaCompraRepository.findByFechaBetween(desde, hasta)) {
            if ("ANULADA".equals(fc.getEstado())) {
                continue;
            }
            lineas.add(new LineaLibro(
                    fc.getFecha(),
                    numeroCompleto(fc.getNumeroSerie(), fc.getNumero()),
                    fc.getProveedor() != null ? fc.getProveedor().getCif() : null,
                    fc.getProveedor() != null ? fc.getProveedor().getNombre() : "(sin proveedor)",
                    "RECIBIDA",
                    nvl(fc.getTipoIva()),
                    nvl(fc.getBaseImponible()),
                    nvl(fc.getImporteIva()),
                    nvl(fc.getImporteRecargo()),
                    nvl(fc.getImporteRetencion()),
                    nvl(fc.getTotal())));
        }
        return lineas;
    }

    // ───────────────────────── Export CSV ─────────────────────────

    public String exportarCsv(List<LineaLibro> lineas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fecha;Numero;NIF;Nombre;Tipo factura;Tipo IVA;Base imponible;Cuota IVA;Cuota recargo;Retencion;Total factura\r\n");
        for (LineaLibro l : lineas) {
            sb.append(l.fecha() != null ? l.fecha() : "").append(';')
              .append(csv(l.numero())).append(';')
              .append(csv(l.nif())).append(';')
              .append(csv(l.nombre())).append(';')
              .append(csv(l.tipoFactura())).append(';')
              .append(dec(l.tipoIva())).append(';')
              .append(dec(l.base())).append(';')
              .append(dec(l.cuotaIva())).append(';')
              .append(dec(l.cuotaRecargo())).append(';')
              .append(dec(l.retencion())).append(';')
              .append(dec(l.total())).append("\r\n");
        }
        return sb.toString();
    }

    // ───────────────────────── Modelo 303 (borrador) ─────────────────────────

    @Transactional(readOnly = true)
    public Modelo303Borrador modelo303(int ejercicio, int trimestre) {
        LocalDate desde = inicioTrimestre(ejercicio, trimestre);
        LocalDate hasta = finTrimestre(ejercicio, trimestre);

        // IVA devengado por tipo (ventas)
        Map<BigDecimal, BigDecimal[]> devengado = new TreeMap<>();
        BigDecimal recargoDevengado = BigDecimal.ZERO;
        for (Factura factura : facturaRepository.findByFechaBetweenAndEstado(desde, hasta, "EMITIDA")) {
            for (Map.Entry<BigDecimal, BigDecimal[]> e : desglosePorTipo(factura.getFacturaLineas()).entrySet()) {
                devengado.merge(e.getKey(), new BigDecimal[]{e.getValue()[0], e.getValue()[1]},
                        (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});
            }
            recargoDevengado = recargoDevengado.add(nvl(factura.getTotalRecargo()));
        }

        // IVA deducible (compras interiores)
        BigDecimal baseSoportada = BigDecimal.ZERO;
        BigDecimal cuotaSoportada = BigDecimal.ZERO;
        for (FacturaCompra fc : facturaCompraRepository.findByFechaBetween(desde, hasta)) {
            if ("ANULADA".equals(fc.getEstado())) {
                continue;
            }
            baseSoportada = baseSoportada.add(nvl(fc.getBaseImponible()));
            cuotaSoportada = cuotaSoportada.add(nvl(fc.getImporteIva()));
        }

        List<CasillaTipo> casillas = new ArrayList<>();
        BigDecimal totalCuotaDevengada = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, BigDecimal[]> e : devengado.entrySet()) {
            casillas.add(new CasillaTipo(e.getKey(), e.getValue()[0], e.getValue()[1]));
            totalCuotaDevengada = totalCuotaDevengada.add(e.getValue()[1]);
        }
        totalCuotaDevengada = totalCuotaDevengada.add(recargoDevengado);

        BigDecimal resultado = totalCuotaDevengada.subtract(cuotaSoportada);
        return new Modelo303Borrador(ejercicio, trimestre, desde, hasta, casillas, recargoDevengado,
                totalCuotaDevengada, baseSoportada, cuotaSoportada, resultado);
    }

    // ───────────────────────── Modelo 111 (borrador) ─────────────────────────

    @Transactional(readOnly = true)
    public Modelo111Borrador modelo111(int ejercicio, int trimestre) {
        LocalDate desde = inicioTrimestre(ejercicio, trimestre);
        LocalDate hasta = finTrimestre(ejercicio, trimestre);

        Map<Long, String> perceptores = new LinkedHashMap<>();
        BigDecimal base = BigDecimal.ZERO;
        BigDecimal retenciones = BigDecimal.ZERO;
        for (FacturaCompra fc : facturaCompraRepository.findByFechaBetween(desde, hasta)) {
            if ("ANULADA".equals(fc.getEstado()) || nvl(fc.getImporteRetencion()).compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (fc.getProveedor() != null) {
                perceptores.put(fc.getProveedor().getId(), fc.getProveedor().getNombre());
            }
            base = base.add(nvl(fc.getBaseImponible()));
            retenciones = retenciones.add(nvl(fc.getImporteRetencion()));
        }
        return new Modelo111Borrador(ejercicio, trimestre, desde, hasta,
                perceptores.size(), base, retenciones);
    }

    // ───────────────────────── Auxiliares ─────────────────────────

    /** Desglose base/cuota por tipo impositivo (mismo criterio que el registro VeriFactu). */
    private Map<BigDecimal, BigDecimal[]> desglosePorTipo(Iterable<FacturaLinea> lineas) {
        Map<BigDecimal, BigDecimal[]> porTipo = new TreeMap<>();
        if (lineas != null) {
            for (FacturaLinea linea : lineas) {
                if (linea.getCantidad() == null || linea.getPrecioUnitario() == null) {
                    continue;
                }
                BigDecimal tipo = (linea.getIva() != null ? linea.getIva() : BigDecimal.ZERO)
                        .setScale(FinancialMath.SCALE, FinancialMath.ROUND);
                BigDecimal base = FinancialMath.subtotalConDescuento(
                        linea.getCantidad(), linea.getPrecioUnitario(), linea.getDescuento());
                BigDecimal cuota = FinancialMath.porcentaje(base, tipo);
                porTipo.merge(tipo, new BigDecimal[]{base, cuota},
                        (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});
            }
        }
        return porTipo;
    }

    public static LocalDate inicioTrimestre(int ejercicio, int trimestre) {
        return LocalDate.of(ejercicio, (trimestre - 1) * 3 + 1, 1);
    }

    public static LocalDate finTrimestre(int ejercicio, int trimestre) {
        return inicioTrimestre(ejercicio, trimestre).plusMonths(3).minusDays(1);
    }

    private static String numeroCompleto(String serie, String numero) {
        return serie != null && !serie.isBlank() ? serie + "-" + numero : numero;
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(';', ',');
    }

    private static String dec(BigDecimal value) {
        return value != null ? value.toPlainString().replace('.', ',') : "";
    }

    // ───────────────────────── Tipos ─────────────────────────

    public record LineaLibro(LocalDate fecha, String numero, String nif, String nombre,
                             String tipoFactura, BigDecimal tipoIva, BigDecimal base,
                             BigDecimal cuotaIva, BigDecimal cuotaRecargo, BigDecimal retencion,
                             BigDecimal total) {
    }

    public record CasillaTipo(BigDecimal tipo, BigDecimal base, BigDecimal cuota) {
    }

    public record Modelo303Borrador(int ejercicio, int trimestre, LocalDate desde, LocalDate hasta,
                                    List<CasillaTipo> devengadoPorTipo, BigDecimal recargoDevengado,
                                    BigDecimal totalCuotaDevengada, BigDecimal baseSoportada,
                                    BigDecimal cuotaSoportada, BigDecimal resultado) {
    }

    public record Modelo111Borrador(int ejercicio, int trimestre, LocalDate desde, LocalDate hasta,
                                    int numeroPerceptores, BigDecimal base, BigDecimal retenciones) {
    }
}
