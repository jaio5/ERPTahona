package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

/**
 * Servicio para generación automática de asientos contables
 */
@Service
@Slf4j
@Transactional
public class AsientoAutomaticoService {

    private final AsientoContableRepository asientoRepository;
    private final PlanCuentasRepository planCuentasRepository;

    public AsientoAutomaticoService(AsientoContableRepository asientoRepository,
                                   PlanCuentasRepository planCuentasRepository) {
        this.asientoRepository = asientoRepository;
        this.planCuentasRepository = planCuentasRepository;
    }

    /**
     * Genera asiento al emitir una factura de venta
     * DEBE: 430 Clientes (base + IVA)
     * HABER: 700 Ventas (base)
     * HABER: 477 IVA repercutido (IVA)
     */
    public AsientoContable generarAsientoFacturaVenta(Factura factura) {
        log.info("Generando asiento automático para factura: {}", factura.getNumero());

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Factura de venta " + factura.getNumero());
        asiento.setConcepto("VENTA");
        asiento.setAsientoApertura(false);
        asiento.setAsientoCierre(false);

        Set<LineaAsiento> lineas = new HashSet<>();

        // DEBE: 430 Clientes
        LineaAsiento lineaCliente = new LineaAsiento();
        PlanCuentas cuentaClientes = buscarCuenta("430");
        lineaCliente.setCuenta(cuentaClientes);
        lineaCliente.setConcepto("Cliente: " + factura.getCliente().getNombre());
        lineaCliente.setDebe(factura.getTotal());
        lineaCliente.setHaber(BigDecimal.ZERO);
        lineaCliente.setAsiento(asiento);
        lineas.add(lineaCliente);

        // HABER: 700 Ventas (base)
        LineaAsiento lineaVentas = new LineaAsiento();
        PlanCuentas cuentaVentas = buscarCuenta("700");
        lineaVentas.setCuenta(cuentaVentas);
        lineaVentas.setConcepto("Venta según factura " + factura.getNumero());
        lineaVentas.setDebe(BigDecimal.ZERO);
        lineaVentas.setHaber(factura.getBaseImponible());
        lineaVentas.setAsiento(asiento);
        lineas.add(lineaVentas);

        // HABER: 477 IVA repercutido
        LineaAsiento lineaIva = new LineaAsiento();
        PlanCuentas cuentaIvaRepercutido = buscarCuenta("477");
        lineaIva.setCuenta(cuentaIvaRepercutido);
        lineaIva.setConcepto("IVA repercutido factura " + factura.getNumero());
        lineaIva.setDebe(BigDecimal.ZERO);
        lineaIva.setHaber(factura.getTotalIva());
        lineaIva.setAsiento(asiento);
        lineas.add(lineaIva);

        asiento.setLineas(lineas);

        // Guardar
        AsientoContable asientoGuardado = asientoRepository.save(asiento);
        log.info("✅ Asiento {} generado para factura {}", asientoGuardado.getId(), factura.getNumero());

        return asientoGuardado;
    }

    /**
     * Genera asiento al cobrar una factura
     * DEBE: 572 Banco (importe)
     * HABER: 430 Clientes (importe)
     */
    public AsientoContable generarAsientoCobroFactura(Factura factura, Banco banco) {
        log.info("Generando asiento de cobro para factura: {}", factura.getNumero());

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Cobro factura " + factura.getNumero());
        asiento.setConcepto("COBRO");
        asiento.setAsientoApertura(false);
        asiento.setAsientoCierre(false);

        Set<LineaAsiento> lineas = new HashSet<>();

        // DEBE: 572 Banco
        LineaAsiento lineaBanco = new LineaAsiento();
        PlanCuentas cuentaBanco = buscarCuenta("572");
        lineaBanco.setCuenta(cuentaBanco);
        lineaBanco.setConcepto("Cobro de " + factura.getCliente().getNombre());
        lineaBanco.setDebe(factura.getTotal());
        lineaBanco.setHaber(BigDecimal.ZERO);
        lineaBanco.setAsiento(asiento);
        lineas.add(lineaBanco);

        // HABER: 430 Clientes
        LineaAsiento lineaCliente = new LineaAsiento();
        PlanCuentas cuentaClientes = buscarCuenta("430");
        lineaCliente.setCuenta(cuentaClientes);
        lineaCliente.setConcepto("Cobro factura " + factura.getNumero());
        lineaCliente.setDebe(BigDecimal.ZERO);
        lineaCliente.setHaber(factura.getTotal());
        lineaCliente.setAsiento(asiento);
        lineas.add(lineaCliente);

        asiento.setLineas(lineas);

        AsientoContable asientoGuardado = asientoRepository.save(asiento);
        log.info("✅ Asiento de cobro {} generado", asientoGuardado.getId());

        return asientoGuardado;
    }

    /**
     * Genera asiento de compra a proveedor
     * DEBE: 600 Compras (base)
     * DEBE: 472 IVA soportado (IVA)
     * HABER: 400 Proveedores (base + IVA)
     */
    public AsientoContable generarAsientoCompra(FacturaCompra facturaCompra) {
        log.info("Generando asiento de compra para factura: {}", facturaCompra.getNumeroFactura());

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Compra según factura " + facturaCompra.getNumeroFactura());
        asiento.setConcepto("COMPRA");
        asiento.setAsientoApertura(false);
        asiento.setAsientoCierre(false);

        Set<LineaAsiento> lineas = new HashSet<>();

        // DEBE: 600 Compras
        LineaAsiento lineaCompras = new LineaAsiento();
        PlanCuentas cuentaCompras = buscarCuenta("600");
        lineaCompras.setCuenta(cuentaCompras);
        lineaCompras.setConcepto("Compra a " + facturaCompra.getProveedor().getNombre());
        lineaCompras.setDebe(facturaCompra.getBaseImponible());
        lineaCompras.setHaber(BigDecimal.ZERO);
        lineaCompras.setAsiento(asiento);
        lineas.add(lineaCompras);

        // DEBE: 472 IVA soportado
        LineaAsiento lineaIva = new LineaAsiento();
        PlanCuentas cuentaIvaSoportado = buscarCuenta("472");
        lineaIva.setCuenta(cuentaIvaSoportado);
        lineaIva.setConcepto("IVA soportado factura " + facturaCompra.getNumeroFactura());
        lineaIva.setDebe(facturaCompra.getCuotaIva());
        lineaIva.setHaber(BigDecimal.ZERO);
        lineaIva.setAsiento(asiento);
        lineas.add(lineaIva);

        // HABER: 400 Proveedores
        LineaAsiento lineaProveedor = new LineaAsiento();
        PlanCuentas cuentaProveedores = buscarCuenta("400");
        lineaProveedor.setCuenta(cuentaProveedores);
        lineaProveedor.setConcepto("Proveedor: " + facturaCompra.getProveedor().getNombre());
        lineaProveedor.setDebe(BigDecimal.ZERO);
        lineaProveedor.setHaber(facturaCompra.getTotalFactura());
        lineaProveedor.setAsiento(asiento);
        lineas.add(lineaProveedor);

        asiento.setLineas(lineas);

        AsientoContable asientoGuardado = asientoRepository.save(asiento);
        log.info("✅ Asiento de compra {} generado", asientoGuardado.getId());

        return asientoGuardado;
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
        lineaProveedor.setDebe(facturaCompra.getTotalFactura());
        lineaProveedor.setHaber(BigDecimal.ZERO);
        lineaProveedor.setAsiento(asiento);
        lineas.add(lineaProveedor);

        // HABER: 572 Banco
        LineaAsiento lineaBanco = new LineaAsiento();
        PlanCuentas cuentaBanco = buscarCuenta("572");
        lineaBanco.setCuenta(cuentaBanco);
        lineaBanco.setConcepto("Pago factura " + facturaCompra.getNumeroFactura());
        lineaBanco.setDebe(BigDecimal.ZERO);
        lineaBanco.setHaber(facturaCompra.getTotalFactura());
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

