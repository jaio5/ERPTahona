package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio completo de contabilidad
 * Genera asientos automáticos de facturas, pagos, etc.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContabilidadService {
    private static final Logger log = LoggerFactory.getLogger(ContabilidadService.class);

    private final AsientoContableRepository asientoRepository;
    private final PlanCuentasRepository cuentasRepository;
    private final AuditoriaService auditoriaService;

    // ==========================================
    // GENERACIÓN AUTOMÁTICA DE ASIENTOS
    // ==========================================

    /**
     * Generar asiento contable de una factura de venta
     */
    public AsientoContable generarAsientoFactura(Factura factura, Usuario usuario) {
        log.info("📝 Generando asiento contable para factura: {}", factura.getNumero());

        try {
            AsientoContable asiento = new AsientoContable();
            asiento.setNumero(generarNumeroAsiento());
            asiento.setFecha(factura.getFecha() != null ? factura.getFecha() : LocalDate.now());
            asiento.setConcepto("Factura de venta " + factura.getNumero() + " - " + factura.getCliente().getNombre());
            asiento.setTipo("OPERACION");
            asiento.setUsuario(usuario);
            asiento.setFactura(factura);

            // Obtener cuentas del plan contable
            PlanCuentas cuentaClientes = obtenerCuenta("430"); // 430 - Clientes
            PlanCuentas cuentaVentas = obtenerCuenta("700");   // 700 - Ventas de mercaderías
            PlanCuentas cuentaIVA = obtenerCuenta("477");      // 477 - HP IVA Repercutido

            // Línea 1: DEBE - Clientes (Total factura)
            LineaAsiento lineaCliente = new LineaAsiento();
            lineaCliente.setAsiento(asiento);
            lineaCliente.setCuenta(cuentaClientes);
            lineaCliente.setDebe(factura.getTotal());
            lineaCliente.setHaber(BigDecimal.ZERO);
            lineaCliente.setConcepto("Cliente: " + factura.getCliente().getNombre());
            lineaCliente.setOrden(1);

            // Línea 2: HABER - Ventas (Base imponible)
            LineaAsiento lineaVentas = new LineaAsiento();
            lineaVentas.setAsiento(asiento);
            lineaVentas.setCuenta(cuentaVentas);
            lineaVentas.setDebe(BigDecimal.ZERO);
            lineaVentas.setHaber(factura.getBaseImponible());
            lineaVentas.setConcepto("Venta según factura " + factura.getNumero());
            lineaVentas.setOrden(2);

            // Línea 3: HABER - IVA Repercutido
            LineaAsiento lineaIVA = new LineaAsiento();
            lineaIVA.setAsiento(asiento);
            lineaIVA.setCuenta(cuentaIVA);
            lineaIVA.setDebe(BigDecimal.ZERO);
            lineaIVA.setHaber(factura.getTotalIva());
            lineaIVA.setConcepto("IVA repercutido");
            lineaIVA.setOrden(3);

            // Agregar líneas al asiento
            asiento.getLineas().add(lineaCliente);
            asiento.getLineas().add(lineaVentas);
            asiento.getLineas().add(lineaIVA);

            // Calcular totales
            asiento.calcularTotales();

            // Verificar cuadre
            if (!asiento.estaCuadrado()) {
                log.error("❌ El asiento no cuadra: Debe={}, Haber={}", asiento.getDebe(), asiento.getHaber());
                throw new IllegalStateException("El asiento contable no cuadra");
            }

            // Guardar asiento
            AsientoContable guardado = asientoRepository.save(asiento);

            log.info("✅ Asiento contable generado: {} - Debe={}, Haber={}",
                guardado.getNumero(), guardado.getDebe(), guardado.getHaber());

            // Auditar
            if (auditoriaService != null) {
                auditoriaService.registrarAccion(usuario, "CONTABILIDAD", "CREAR_ASIENTO",
                    "Asiento generado automáticamente para factura " + factura.getNumero(),
                    "EXITOSO");
            }

            return guardado;

        } catch (Exception e) {
            log.error("❌ Error generando asiento de factura", e);
            throw new RuntimeException("Error al generar asiento contable", e);
        }
    }

    /**
     * Generar asiento de pago de cliente
     */
    public AsientoContable generarAsientoPago(Factura factura, BigDecimal importe,
                                              String formaPago, Usuario usuario) {
        log.info("💰 Generando asiento de pago: Factura={}, Importe={}", factura.getNumero(), importe);

        try {
            AsientoContable asiento = new AsientoContable();
            asiento.setNumero(generarNumeroAsiento());
            asiento.setFecha(LocalDate.now());
            asiento.setConcepto("Cobro factura " + factura.getNumero() + " - " + factura.getCliente().getNombre());
            asiento.setTipo("OPERACION");
            asiento.setUsuario(usuario);
            asiento.setFactura(factura);

            // Obtener cuentas
            PlanCuentas cuentaClientes = obtenerCuenta("430");  // 430 - Clientes
            PlanCuentas cuentaCaja = "EFECTIVO".equals(formaPago) ?
                obtenerCuenta("570") :  // 570 - Caja
                obtenerCuenta("572");   // 572 - Bancos

            // Línea 1: DEBE - Caja/Banco
            LineaAsiento lineaCaja = new LineaAsiento();
            lineaCaja.setAsiento(asiento);
            lineaCaja.setCuenta(cuentaCaja);
            lineaCaja.setDebe(importe);
            lineaCaja.setHaber(BigDecimal.ZERO);
            lineaCaja.setConcepto("Cobro " + formaPago);
            lineaCaja.setOrden(1);

            // Línea 2: HABER - Clientes
            LineaAsiento lineaCliente = new LineaAsiento();
            lineaCliente.setAsiento(asiento);
            lineaCliente.setCuenta(cuentaClientes);
            lineaCliente.setDebe(BigDecimal.ZERO);
            lineaCliente.setHaber(importe);
            lineaCliente.setConcepto("Cliente: " + factura.getCliente().getNombre());
            lineaCliente.setOrden(2);

            // Agregar líneas
            asiento.getLineas().add(lineaCaja);
            asiento.getLineas().add(lineaCliente);

            // Calcular y verificar
            asiento.calcularTotales();
            if (!asiento.estaCuadrado()) {
                throw new IllegalStateException("El asiento de pago no cuadra");
            }

            // Guardar
            AsientoContable guardado = asientoRepository.save(asiento);
            log.info("✅ Asiento de pago generado: {}", guardado.getNumero());

            return guardado;

        } catch (Exception e) {
            log.error("❌ Error generando asiento de pago", e);
            throw new RuntimeException("Error al generar asiento de pago", e);
        }
    }

    /**
     * Generar asiento de compra a proveedor
     */
    public AsientoContable generarAsientoCompra(Long facturaCompraId, BigDecimal base,
                                                BigDecimal iva, BigDecimal total, Usuario usuario) {
        log.info("🛒 Generando asiento de compra: Base={}, IVA={}, Total={}", base, iva, total);

        try {
            AsientoContable asiento = new AsientoContable();
            asiento.setNumero(generarNumeroAsiento());
            asiento.setFecha(LocalDate.now());
            asiento.setConcepto("Factura de compra");
            asiento.setTipo("OPERACION");
            asiento.setUsuario(usuario);
            asiento.setFacturaCompraId(facturaCompraId);

            // Obtener cuentas
            PlanCuentas cuentaCompras = obtenerCuenta("600");     // 600 - Compras
            PlanCuentas cuentaIVASoportado = obtenerCuenta("472"); // 472 - IVA Soportado
            PlanCuentas cuentaProveedores = obtenerCuenta("400");  // 400 - Proveedores

            // Línea 1: DEBE - Compras
            LineaAsiento lineaCompras = new LineaAsiento();
            lineaCompras.setAsiento(asiento);
            lineaCompras.setCuenta(cuentaCompras);
            lineaCompras.setDebe(base);
            lineaCompras.setHaber(BigDecimal.ZERO);
            lineaCompras.setConcepto("Compra de mercaderías");
            lineaCompras.setOrden(1);

            // Línea 2: DEBE - IVA Soportado
            LineaAsiento lineaIVA = new LineaAsiento();
            lineaIVA.setAsiento(asiento);
            lineaIVA.setCuenta(cuentaIVASoportado);
            lineaIVA.setDebe(iva);
            lineaIVA.setHaber(BigDecimal.ZERO);
            lineaIVA.setConcepto("IVA soportado");
            lineaIVA.setOrden(2);

            // Línea 3: HABER - Proveedores
            LineaAsiento lineaProveedor = new LineaAsiento();
            lineaProveedor.setAsiento(asiento);
            lineaProveedor.setCuenta(cuentaProveedores);
            lineaProveedor.setDebe(BigDecimal.ZERO);
            lineaProveedor.setHaber(total);
            lineaProveedor.setConcepto("Proveedor");
            lineaProveedor.setOrden(3);

            // Agregar líneas
            asiento.getLineas().add(lineaCompras);
            asiento.getLineas().add(lineaIVA);
            asiento.getLineas().add(lineaProveedor);

            // Calcular y verificar
            asiento.calcularTotales();
            if (!asiento.estaCuadrado()) {
                throw new IllegalStateException("El asiento de compra no cuadra");
            }

            // Guardar
            AsientoContable guardado = asientoRepository.save(asiento);
            log.info("✅ Asiento de compra generado: {}", guardado.getNumero());

            return guardado;

        } catch (Exception e) {
            log.error("❌ Error generando asiento de compra", e);
            throw new RuntimeException("Error al generar asiento de compra", e);
        }
    }

    // ==========================================
    // CONSULTAS Y REPORTES
    // ==========================================

    /**
     * Obtener Libro Diario
     */
    @Transactional(readOnly = true)
    public List<AsientoContable> obtenerLibroDiario(LocalDate desde, LocalDate hasta) {
        log.info("📖 Obteniendo Libro Diario: {} - {}", desde, hasta);
        return asientoRepository.findByFechaBetween(desde, hasta);
    }

    /**
     * Obtener balance de sumas y saldos
     */
    @Transactional(readOnly = true)
    public List<BalanceCuenta> obtenerBalance(LocalDate fecha) {
        log.info("⚖️ Calculando balance a fecha: {}", fecha);

        // Aquí iría la lógica compleja de cálculo de saldos por cuenta
        // Por ahora retornamos lista vacía como placeholder
        return new ArrayList<>();
    }

    /**
     * Verificar cuadre contable
     */
    @Transactional(readOnly = true)
    public boolean validarCuadreContable() {
        List<AsientoContable> asientos = asientoRepository.findAll();

        for (AsientoContable asiento : asientos) {
            if (!asiento.estaCuadrado()) {
                log.warn("⚠️ Asiento descuadrado: {}", asiento.getNumero());
                return false;
            }
        }

        log.info("✅ Todos los asientos están cuadrados");
        return true;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    /**
     * Generar número de asiento automático
     */
    private String generarNumeroAsiento() {
        int year = LocalDate.now().getYear();
        String pattern = year + "%";

        Integer maxNumero = asientoRepository.findMaxNumeroByYear(pattern);
        int siguiente = (maxNumero != null) ? maxNumero + 1 : 1;

        return String.format("%d-%04d", year, siguiente);
    }

    /**
     * Obtener cuenta del plan contable
     */
    private PlanCuentas obtenerCuenta(String codigo) {
        return cuentasRepository.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalStateException("Cuenta contable no encontrada: " + codigo));
    }

    /**
     * Clase auxiliar para balance
     */
    public record BalanceCuenta(
        String codigo,
        String nombre,
        BigDecimal debe,
        BigDecimal haber,
        BigDecimal saldo
    ) {}
}

