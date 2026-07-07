package alicanteweb.erp.service;

import alicanteweb.erp.entities.CobroFactura;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.PagoFacturaCompra;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.CobroFacturaRepository;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.PagoFacturaCompraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Cartera de cobros (facturas de venta) y pagos (facturas de compra).
 * Admite cobros/pagos parciales; el saldo vivo se refleja en factura.pagado
 * y facturas_compra.pagado, y cada movimiento genera su asiento contable.
 */
@Service
public class CarteraService {

    private static final Logger log = LoggerFactory.getLogger(CarteraService.class);

    private final FacturaRepository facturaRepository;
    private final FacturaCompraRepository facturaCompraRepository;
    private final CobroFacturaRepository cobroRepository;
    private final PagoFacturaCompraRepository pagoRepository;
    private final ContabilidadService contabilidadService;
    private final AuditoriaService auditoriaService;

    public CarteraService(FacturaRepository facturaRepository,
                          FacturaCompraRepository facturaCompraRepository,
                          CobroFacturaRepository cobroRepository,
                          PagoFacturaCompraRepository pagoRepository,
                          ContabilidadService contabilidadService,
                          AuditoriaService auditoriaService) {
        this.facturaRepository = facturaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
        this.cobroRepository = cobroRepository;
        this.pagoRepository = pagoRepository;
        this.contabilidadService = contabilidadService;
        this.auditoriaService = auditoriaService;
    }

    // ───────────────────────────── Cobros (clientes) ─────────────────────────────

    @Transactional
    public CobroFactura registrarCobro(Long facturaId, LocalDate fecha, BigDecimal importe,
                                       String formaPago, String referencia, String observaciones,
                                       Usuario usuario) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"EMITIDA".equals(factura.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden cobrar facturas emitidas (estado actual: "
                    + factura.getEstado() + ")");
        }
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe del cobro debe ser mayor que cero");
        }
        BigDecimal pendiente = pendiente(factura);
        if (importe.compareTo(pendiente) > 0) {
            throw new IllegalArgumentException("El cobro (" + importe + ") supera el pendiente de la factura ("
                    + pendiente + ")");
        }

        CobroFactura cobro = new CobroFactura();
        cobro.setFactura(factura);
        cobro.setFecha(fecha != null ? fecha : LocalDate.now());
        cobro.setImporte(importe);
        cobro.setFormaPago(formaPago != null && !formaPago.isBlank() ? formaPago : "EFECTIVO");
        cobro.setReferencia(referencia);
        cobro.setObservaciones(observaciones);
        cobro.setUsuarioCreacion(usuario != null ? usuario.getUsername() : null);
        cobro = cobroRepository.save(cobro);

        BigDecimal pagadoAcumulado = nvl(factura.getPagado()).add(importe);
        factura.setPagado(pagadoAcumulado);
        factura.setPagada(pagadoAcumulado.compareTo(nvl(factura.getTotal())) >= 0);
        facturaRepository.save(factura);

        contabilidadService.generarAsientoPago(factura, importe, cobro.getFormaPago(), usuario);
        auditoriaService.registrarAccion(usuario, "COBRO_FACTURA", "Factura",
                String.valueOf(factura.getId()),
                "Cobro de " + importe + " EUR (" + cobro.getFormaPago() + ") de la factura " + factura.getNumero());

        log.info("Cobro registrado: factura={} importe={} pendiente={}", factura.getNumero(), importe,
                pendiente(factura));
        return cobro;
    }

    @Transactional(readOnly = true)
    public List<CobroFactura> cobrosDeFactura(Long facturaId) {
        return cobroRepository.findByFacturaIdOrderByFechaAsc(facturaId);
    }

    // ───────────────────────────── Pagos (proveedores) ─────────────────────────────

    @Transactional
    public PagoFacturaCompra registrarPago(Long facturaCompraId, LocalDate fecha, BigDecimal importe,
                                           String formaPago, String referencia, String observaciones,
                                           Usuario usuario) {
        FacturaCompra factura = facturaCompraRepository.findById(facturaCompraId)
                .orElseThrow(() -> new IllegalArgumentException("Factura de compra no encontrada"));

        if ("ANULADA".equals(factura.getEstado())) {
            throw new IllegalArgumentException("No se puede pagar una factura de compra anulada");
        }
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe del pago debe ser mayor que cero");
        }
        BigDecimal pendiente = factura.getPendiente();
        if (importe.compareTo(pendiente) > 0) {
            throw new IllegalArgumentException("El pago (" + importe + ") supera el pendiente de la factura ("
                    + pendiente + ")");
        }

        PagoFacturaCompra pago = new PagoFacturaCompra();
        pago.setFacturaCompra(factura);
        pago.setFecha(fecha != null ? fecha : LocalDate.now());
        pago.setImporte(importe);
        pago.setFormaPago(formaPago != null && !formaPago.isBlank() ? formaPago : "TRANSFERENCIA");
        pago.setReferencia(referencia);
        pago.setObservaciones(observaciones);
        pago.setUsuarioCreacion(usuario != null ? usuario.getUsername() : null);
        pago = pagoRepository.save(pago);

        BigDecimal pagadoAcumulado = nvl(factura.getPagado()).add(importe);
        factura.setPagado(pagadoAcumulado);
        if (pagadoAcumulado.compareTo(nvl(factura.getTotal())) >= 0) {
            factura.marcarComoPagada(pago.getFecha());
        }
        facturaCompraRepository.save(factura);

        contabilidadService.generarAsientoPagoCompra(factura, importe, pago.getFormaPago(), usuario);
        auditoriaService.registrarAccion(usuario, "PAGO_FACTURA_COMPRA", "FacturaCompra",
                String.valueOf(factura.getId()),
                "Pago de " + importe + " EUR (" + pago.getFormaPago() + ") de la factura " + factura.getNumero());

        log.info("Pago registrado: facturaCompra={} importe={} pendiente={}", factura.getNumero(), importe,
                factura.getPendiente());
        return pago;
    }

    @Transactional(readOnly = true)
    public List<PagoFacturaCompra> pagosDeFacturaCompra(Long facturaCompraId) {
        return pagoRepository.findByFacturaCompraIdOrderByFechaAsc(facturaCompraId);
    }

    // ───────────────────────────── Consultas de cartera ─────────────────────────────

    @Transactional(readOnly = true)
    public List<CarteraItem> cobrosPendientes() {
        return facturaRepository.findByEstado("EMITIDA").stream()
                .filter(f -> !f.isPagada())
                .filter(f -> pendiente(f).compareTo(BigDecimal.ZERO) > 0)
                .map(f -> new CarteraItem(
                        f.getId(),
                        f.getNumero(),
                        f.getCliente() != null ? f.getCliente().getNombre() : "(sin cliente)",
                        f.getFecha(),
                        f.getFechaVencimiento(),
                        nvl(f.getTotal()),
                        nvl(f.getPagado()),
                        pendiente(f),
                        diasVencido(f.getFechaVencimiento(), f.getFecha())))
                .sorted((a, b) -> Integer.compare(b.diasVencido(), a.diasVencido()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarteraItem> pagosPendientes() {
        return facturaCompraRepository.findPendientesPago().stream()
                .filter(f -> !"ANULADA".equals(f.getEstado()))
                .filter(f -> f.getPendiente().compareTo(BigDecimal.ZERO) > 0)
                .map(f -> new CarteraItem(
                        f.getId(),
                        f.getNumero(),
                        f.getProveedor() != null ? f.getProveedor().getNombre() : "(sin proveedor)",
                        f.getFecha(),
                        f.getFechaVencimiento(),
                        nvl(f.getTotal()),
                        nvl(f.getPagado()),
                        f.getPendiente(),
                        diasVencido(f.getFechaVencimiento(), f.getFecha())))
                .sorted((a, b) -> Integer.compare(b.diasVencido(), a.diasVencido()))
                .toList();
    }

    public AgingResumen aging(List<CarteraItem> items) {
        BigDecimal noVencido = BigDecimal.ZERO;
        BigDecimal hasta30 = BigDecimal.ZERO;
        BigDecimal hasta60 = BigDecimal.ZERO;
        BigDecimal mas60 = BigDecimal.ZERO;
        for (CarteraItem item : items) {
            int dias = item.diasVencido();
            if (dias <= 0) {
                noVencido = noVencido.add(item.pendiente());
            } else if (dias <= 30) {
                hasta30 = hasta30.add(item.pendiente());
            } else if (dias <= 60) {
                hasta60 = hasta60.add(item.pendiente());
            } else {
                mas60 = mas60.add(item.pendiente());
            }
        }
        return new AgingResumen(noVencido, hasta30, hasta60, mas60,
                noVencido.add(hasta30).add(hasta60).add(mas60));
    }

    // ───────────────────────────── Auxiliares ─────────────────────────────

    private BigDecimal pendiente(Factura f) {
        return nvl(f.getTotal()).subtract(nvl(f.getPagado()));
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Días transcurridos desde el vencimiento (negativo si aún no ha vencido).
     * Sin fecha de vencimiento se usa la fecha de factura + 30 días.
     */
    private static int diasVencido(LocalDate vencimiento, LocalDate fechaFactura) {
        LocalDate referencia = vencimiento != null ? vencimiento
                : (fechaFactura != null ? fechaFactura.plusDays(30) : null);
        if (referencia == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(referencia, LocalDate.now());
    }

    public record CarteraItem(Long id, String numero, String tercero, LocalDate fecha,
                              LocalDate vencimiento, BigDecimal total, BigDecimal pagado,
                              BigDecimal pendiente, int diasVencido) {
    }

    public record AgingResumen(BigDecimal noVencido, BigDecimal hasta30, BigDecimal hasta60,
                               BigDecimal mas60, BigDecimal total) {
    }
}
