package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanContableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para generación automática de asientos contables
 */
@Service
@Slf4j
@Transactional
public class AsientoAutomaticoService {

    private final AsientoContableRepository asientoRepository;
    private final PlanContableRepository planContableRepository;

    public AsientoAutomaticoService(AsientoContableRepository asientoRepository,
                                   PlanContableRepository planContableRepository) {
        this.asientoRepository = asientoRepository;
        this.planContableRepository = planContableRepository;
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

        List<AsientoContableLinea> lineas = new ArrayList<>();

        // DEBE: 430 Clientes
        AsientoContableLinea lineaCliente = new AsientoContableLinea();
        PlanContable cuentaClientes = buscarCuenta("430");
        lineaCliente.setCuentaContable(cuentaClientes);
        lineaCliente.setDescripcion("Cliente: " + factura.getCliente().getNombre());
        lineaCliente.setDebe(factura.getTotal());
        lineaCliente.setHaber(BigDecimal.ZERO);
        lineaCliente.setAsiento(asiento);
        lineas.add(lineaCliente);

        // HABER: 700 Ventas (base)
        AsientoContableLinea lineaVentas = new AsientoContableLinea();
        PlanContable cuentaVentas = buscarCuenta("700");
        lineaVentas.setCuentaContable(cuentaVentas);
        lineaVentas.setDescripcion("Venta según factura " + factura.getNumero());
        lineaVentas.setDebe(BigDecimal.ZERO);
        lineaVentas.setHaber(factura.getBaseImponible());
        lineaVentas.setAsiento(asiento);
        lineas.add(lineaVentas);

        // HABER: 477 IVA repercutido
        AsientoContableLinea lineaIva = new AsientoContableLinea();
        PlanContable cuentaIvaRepercutido = buscarCuenta("477");
        lineaIva.setCuentaContable(cuentaIvaRepercutido);
        lineaIva.setDescripcion("IVA repercutido factura " + factura.getNumero());
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

        List<AsientoContableLinea> lineas = new ArrayList<>();

        // DEBE: 572 Banco
        AsientoContableLinea lineaBanco = new AsientoContableLinea();
        PlanContable cuentaBanco = buscarCuenta("572");
        lineaBanco.setCuentaContable(cuentaBanco);
        lineaBanco.setDescripcion("Cobro de " + factura.getCliente().getNombre());
        lineaBanco.setDebe(factura.getTotal());
        lineaBanco.setHaber(BigDecimal.ZERO);
        lineaBanco.setAsiento(asiento);
        lineas.add(lineaBanco);

        // HABER: 430 Clientes
        AsientoContableLinea lineaCliente = new AsientoContableLinea();
        PlanContable cuentaClientes = buscarCuenta("430");
        lineaCliente.setCuentaContable(cuentaClientes);
        lineaCliente.setDescripcion("Cobro factura " + factura.getNumero());
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

        List<AsientoContableLinea> lineas = new ArrayList<>();

        // DEBE: 600 Compras
        AsientoContableLinea lineaCompras = new AsientoContableLinea();
        PlanContable cuentaCompras = buscarCuenta("600");
        lineaCompras.setCuentaContable(cuentaCompras);
        lineaCompras.setDescripcion("Compra a " + facturaCompra.getProveedor().getNombre());
        lineaCompras.setDebe(facturaCompra.getBaseImponible());
        lineaCompras.setHaber(BigDecimal.ZERO);
        lineaCompras.setAsiento(asiento);
        lineas.add(lineaCompras);

        // DEBE: 472 IVA soportado
        AsientoContableLinea lineaIva = new AsientoContableLinea();
        PlanContable cuentaIvaSoportado = buscarCuenta("472");
        lineaIva.setCuentaContable(cuentaIvaSoportado);
        lineaIva.setDescripcion("IVA soportado factura " + facturaCompra.getNumeroFactura());
        lineaIva.setDebe(facturaCompra.getCuotaIva());
        lineaIva.setHaber(BigDecimal.ZERO);
        lineaIva.setAsiento(asiento);
        lineas.add(lineaIva);

        // HABER: 400 Proveedores
        AsientoContableLinea lineaProveedor = new AsientoContableLinea();
        PlanContable cuentaProveedores = buscarCuenta("400");
        lineaProveedor.setCuentaContable(cuentaProveedores);
        lineaProveedor.setDescripcion("Proveedor: " + facturaCompra.getProveedor().getNombre());
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

        List<AsientoContableLinea> lineas = new ArrayList<>();

        // DEBE: 400 Proveedores
        AsientoContableLinea lineaProveedor = new AsientoContableLinea();
        PlanContable cuentaProveedores = buscarCuenta("400");
        lineaProveedor.setCuentaContable(cuentaProveedores);
        lineaProveedor.setDescripcion("Pago a " + facturaCompra.getProveedor().getNombre());
        lineaProveedor.setDebe(facturaCompra.getTotalFactura());
        lineaProveedor.setHaber(BigDecimal.ZERO);
        lineaProveedor.setAsiento(asiento);
        lineas.add(lineaProveedor);

        // HABER: 572 Banco
        AsientoContableLinea lineaBanco = new AsientoContableLinea();
        PlanContable cuentaBanco = buscarCuenta("572");
        lineaBanco.setCuentaContable(cuentaBanco);
        lineaBanco.setDescripcion("Pago factura " + facturaCompra.getNumeroFactura());
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

        List<AsientoContableLinea> lineas = new ArrayList<>();

        PlanContable cuentaCaja = buscarCuenta("570");

        if ("INGRESO".equals(movimiento.getTipo())) {
            // DEBE: 570 Caja
            AsientoContableLinea lineaCaja = new AsientoContableLinea();
            lineaCaja.setCuentaContable(cuentaCaja);
            lineaCaja.setDescripcion(movimiento.getConcepto());
            lineaCaja.setDebe(movimiento.getImporte());
            lineaCaja.setHaber(BigDecimal.ZERO);
            lineaCaja.setAsiento(asiento);
            lineas.add(lineaCaja);

            // HABER: Cuenta correspondiente (por defecto 700)
            AsientoContableLinea lineaContraparte = new AsientoContableLinea();
            PlanContable cuentaContraparte = buscarCuenta("700");
            lineaContraparte.setCuentaContable(cuentaContraparte);
            lineaContraparte.setDescripcion(movimiento.getConcepto());
            lineaContraparte.setDebe(BigDecimal.ZERO);
            lineaContraparte.setHaber(movimiento.getImporte());
            lineaContraparte.setAsiento(asiento);
            lineas.add(lineaContraparte);

        } else {
            // DEBE: Cuenta correspondiente (por defecto 600)
            AsientoContableLinea lineaContraparte = new AsientoContableLinea();
            PlanContable cuentaContraparte = buscarCuenta("600");
            lineaContraparte.setCuentaContable(cuentaContraparte);
            lineaContraparte.setDescripcion(movimiento.getConcepto());
            lineaContraparte.setDebe(movimiento.getImporte());
            lineaContraparte.setHaber(BigDecimal.ZERO);
            lineaContraparte.setAsiento(asiento);
            lineas.add(lineaContraparte);

            // HABER: 570 Caja
            AsientoContableLinea lineaCaja = new AsientoContableLinea();
            lineaCaja.setCuentaContable(cuentaCaja);
            lineaCaja.setDescripcion(movimiento.getConcepto());
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
    private PlanContable buscarCuenta(String codigo) {
        return planContableRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("No se encontró la cuenta contable: " + codigo + ". ¿Has cargado el Plan Contable?"));
    }

    /**
     * Valida que un asiento esté cuadrado (Debe = Haber)
     */
    public boolean validarAsientoCuadrado(AsientoContable asiento) {
        BigDecimal totalDebe = asiento.getLineas().stream()
                .map(AsientoContableLinea::getDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHaber = asiento.getLineas().stream()
                .map(AsientoContableLinea::getHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDebe.compareTo(totalHaber) == 0;
    }
}

