package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio para generación automática de asientos contables
 */
@Service
@Slf4j
@Transactional
public class AsientoAutomaticoService {

    private final AsientoContableRepository asientoRepository;
    private final PlanCuentasRepository planCuentasRepository;
    private final ContabilidadService contabilidadService;

    public AsientoAutomaticoService(AsientoContableRepository asientoRepository,
                                   PlanCuentasRepository planCuentasRepository,
                                   ContabilidadService contabilidadService) {
        this.asientoRepository = asientoRepository;
        this.planCuentasRepository = planCuentasRepository;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Genera asiento al emitir una factura de venta
     */
    public AsientoContable generarAsientoFacturaVenta(Factura factura) {
        log.info("Generando asiento automático para factura: {}", factura.getNumero());
        // Delegar a ContabilidadService (sin usuario, ejecución automática)
        return contabilidadService.generarAsientoFactura(factura, null);
    }

    /**
     * Genera asiento al cobrar una factura
     */
    public AsientoContable generarAsientoCobroFactura(Factura factura, Banco banco, BigDecimal importe) {
        log.info("Generando asiento de cobro para factura: {}", factura.getNumero());
        // Delegar a ContabilidadService
        return contabilidadService.generarAsientoPago(factura, importe, banco == null ? "TRANSFERENCIA" : "EFECTIVO", null);
    }

    /**
     * Genera asiento de compra a proveedor
     */
    public AsientoContable generarAsientoCompra(FacturaCompra facturaCompra) {
        log.info("Generando asiento automático de compra para factura: {}", facturaCompra.getNumeroFactura());
        BigDecimal base = facturaCompra.getBaseImponible() != null ? facturaCompra.getBaseImponible() : BigDecimal.ZERO;
        BigDecimal iva = facturaCompra.getImporteIva() != null ? facturaCompra.getImporteIva() : BigDecimal.ZERO;
        BigDecimal total = facturaCompra.getTotal() != null ? facturaCompra.getTotal() : BigDecimal.ZERO;
        // Delegar a ContabilidadService
        return contabilidadService.generarAsientoCompra(facturaCompra.getId(), base, iva, total, null);
    }

    /**
     * Genera asiento de pago a proveedor
     * DEBE: 400 Proveedores (importe)
     * HABER: 572 Banco (importe)
     */
    public AsientoContable generarAsientoPagoProveedor(FacturaCompra facturaCompra, Banco banco) {
        log.info("Generando asiento de pago para factura compra: {}", facturaCompra.getNumeroFactura());

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Pago factura " + facturaCompra.getNumeroFactura());
        asiento.setConcepto("PAGO");
        asiento.setAsientoApertura(false);
        asiento.setAsientoCierre(false);

        Set<LineaAsiento> lineas = new HashSet<>();

        // DEBE: 400 Proveedores
        LineaAsiento lineaProveedor = new LineaAsiento();
        PlanCuentas cuentaProveedores = buscarCuenta("400");
        lineaProveedor.setCuenta(cuentaProveedores);
        lineaProveedor.setConcepto("Pago a " + facturaCompra.getProveedor().getNombre());
        lineaProveedor.setDebe(facturaCompra.getTotal());
        lineaProveedor.setHaber(BigDecimal.ZERO);
        lineaProveedor.setAsiento(asiento);
        lineas.add(lineaProveedor);

        // HABER: 572 Banco (si hay banco) o 570 Caja
        LineaAsiento lineaBanco = new LineaAsiento();
        PlanCuentas cuentaBanco = buscarCuenta(banco != null ? "572" : "570");
        lineaBanco.setCuenta(cuentaBanco);
        lineaBanco.setConcepto("Pago factura " + facturaCompra.getNumeroFactura());
        lineaBanco.setDebe(BigDecimal.ZERO);
        lineaBanco.setHaber(facturaCompra.getTotal());
        lineaBanco.setAsiento(asiento);
        lineas.add(lineaBanco);

        asiento.setLineas(lineas);

        AsientoContable asientoGuardado = asientoRepository.save(asiento);
        log.info("✅ Asiento de pago {} generado", asientoGuardado.getId());

        return asientoGuardado;
    }

    /**
     * Genera asiento de movimiento de caja
     */
    public AsientoContable generarAsientoMovimientoCaja(MovimientoCaja movimiento) {
        log.info("Generando asiento de movimiento de caja: {}", movimiento.getConcepto());

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Movimiento caja: " + movimiento.getConcepto());
        asiento.setConcepto("CAJA");
        asiento.setAsientoApertura(false);
        asiento.setAsientoCierre(false);

        Set<LineaAsiento> lineas = new HashSet<>();

        PlanCuentas cuentaCaja = buscarCuenta("570");

        if ("INGRESO".equals(movimiento.getTipo())) {
            // DEBE: 570 Caja
            LineaAsiento lineaCaja = new LineaAsiento();
            lineaCaja.setCuenta(cuentaCaja);
            lineaCaja.setConcepto(movimiento.getConcepto());
            lineaCaja.setDebe(movimiento.getImporte());
            lineaCaja.setHaber(BigDecimal.ZERO);
            lineaCaja.setAsiento(asiento);
            lineas.add(lineaCaja);

            // HABER: Cuenta correspondiente (por defecto 700)
            LineaAsiento lineaContraparte = new LineaAsiento();
            PlanCuentas cuentaContraparte = buscarCuenta("700");
            lineaContraparte.setCuenta(cuentaContraparte);
            lineaContraparte.setConcepto(movimiento.getConcepto());
            lineaContraparte.setDebe(BigDecimal.ZERO);
            lineaContraparte.setHaber(movimiento.getImporte());
            lineaContraparte.setAsiento(asiento);
            lineas.add(lineaContraparte);

        } else {
            // DEBE: Cuenta correspondiente (por defecto 600)
            LineaAsiento lineaContraparte = new LineaAsiento();
            PlanCuentas cuentaContraparte = buscarCuenta("600");
            lineaContraparte.setCuenta(cuentaContraparte);
            lineaContraparte.setConcepto(movimiento.getConcepto());
            lineaContraparte.setDebe(movimiento.getImporte());
            lineaContraparte.setHaber(BigDecimal.ZERO);
            lineaContraparte.setAsiento(asiento);
            lineas.add(lineaContraparte);

            // HABER: 570 Caja
            LineaAsiento lineaCaja = new LineaAsiento();
            lineaCaja.setCuenta(cuentaCaja);
            lineaCaja.setConcepto(movimiento.getConcepto());
            lineaCaja.setDebe(BigDecimal.ZERO);
            lineaCaja.setHaber(movimiento.getImporte());
            lineaCaja.setAsiento(asiento);
            lineas.add(lineaCaja);
        }

        asiento.setLineas(lineas);

        AsientoContable asientoGuardado = asientoRepository.save(asiento);
        log.info("✅ Asiento de caja {} generado", asientoGuardado.getId());

        return asientoGuardado;
    }

    /**
     * Busca una cuenta del plan contable por código
     */
    private PlanCuentas buscarCuenta(String codigo) {
        return planCuentasRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("No se encontró la cuenta contable: " + codigo + ". ¿Has cargado el Plan Contable?"));
    }

    /**
     * Valida que un asiento esté cuadrado (Debe = Haber)
     */
    public boolean validarAsientoCuadrado(AsientoContable asiento) {
        BigDecimal totalDebe = asiento.getLineas().stream()
                .map(LineaAsiento::getDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHaber = asiento.getLineas().stream()
                .map(LineaAsiento::getHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDebe.compareTo(totalHaber) == 0;
    }
}
