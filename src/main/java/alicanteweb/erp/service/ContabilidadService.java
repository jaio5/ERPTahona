package alicanteweb.erp.service;

import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        Objects.requireNonNull(factura, "Factura no puede ser null");
        // usuario puede ser null cuando la llamada se realiza desde procesos automáticos

        log.info("📝 Generando asiento contable para factura: {}", factura.getNumero());

        try {
            if (factura.getCliente() == null) {
                throw new IllegalArgumentException("La factura debe tener un cliente asociado");
            }

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

            // Asegurar lista de líneas
            if (asiento.getLineas() == null) {
                asiento.setLineas(new LinkedHashSet<>());
            }

            // Línea 1: DEBE - Clientes (Total factura)
            LineaAsiento lineaCliente = new LineaAsiento();
            lineaCliente.setAsiento(asiento);
            lineaCliente.setCuenta(cuentaClientes);
            lineaCliente.setDebe(factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO);
            lineaCliente.setHaber(BigDecimal.ZERO);
            lineaCliente.setConcepto("Cliente: " + factura.getCliente().getNombre());
            lineaCliente.setOrden(1);

            // Línea 2: HABER - Ventas (Base imponible)
            LineaAsiento lineaVentas = new LineaAsiento();
            lineaVentas.setAsiento(asiento);
            lineaVentas.setCuenta(cuentaVentas);
            lineaVentas.setDebe(BigDecimal.ZERO);
            lineaVentas.setHaber(factura.getBaseImponible() != null ? factura.getBaseImponible() : BigDecimal.ZERO);
            lineaVentas.setConcepto("Venta según factura " + factura.getNumero());
            lineaVentas.setOrden(2);

            // Línea 3: HABER - IVA Repercutido
            LineaAsiento lineaIVA = new LineaAsiento();
            lineaIVA.setAsiento(asiento);
            lineaIVA.setCuenta(cuentaIVA);
            lineaIVA.setDebe(BigDecimal.ZERO);
            lineaIVA.setHaber(factura.getTotalIva() != null ? factura.getTotalIva() : BigDecimal.ZERO);
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
            if (auditoriaService != null && usuario != null) {
                String idStr = guardado.getId() != null ? guardado.getId().toString() : null;
                auditoriaService.registrarAccion(usuario, "CREAR_ASIENTO", "AsientoContable",
                    idStr,
                    "Asiento generado automáticamente para factura " + factura.getNumero());
            }

            return guardado;

        } catch (Exception e) {
            log.error("❌ Error generando asiento de factura", e);
            throw new ErpException("Error al generar asiento contable", e);
        }
    }

    /**
     * Generar asiento de pago de cliente
     */
    public AsientoContable generarAsientoPago(Factura factura, BigDecimal importe,
                                              String formaPago, Usuario usuario) {
        Objects.requireNonNull(factura, "Factura no puede ser null");
        Objects.requireNonNull(importe, "Importe no puede ser null");
        Objects.requireNonNull(formaPago, "Forma de pago no puede ser null");
        // usuario puede ser null para ejecuciones automáticas

        if (importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }

        log.info("💰 Generando asiento de pago: Factura={}, Importe={}", factura.getNumero(), importe);

        try {
            AsientoContable asiento = new AsientoContable();
            asiento.setNumero(generarNumeroAsiento());
            asiento.setFecha(LocalDate.now());
            asiento.setConcepto("Cobro factura " + factura.getNumero() + " - " +
                (factura.getCliente() != null ? factura.getCliente().getNombre() : "Cliente desconocido"));
            asiento.setTipo("OPERACION");
            asiento.setUsuario(usuario);
            asiento.setFactura(factura);

            // Obtener cuentas
            PlanCuentas cuentaClientes = obtenerCuenta("430");  // 430 - Clientes
            PlanCuentas cuentaCaja = "EFECTIVO".equalsIgnoreCase(formaPago) ?
                obtenerCuenta("570") :  // 570 - Caja
                obtenerCuenta("572");   // 572 - Bancos

            if (asiento.getLineas() == null) asiento.setLineas(new LinkedHashSet<>());

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
            lineaCliente.setConcepto("Cliente: " + (factura.getCliente() != null ? factura.getCliente().getNombre() : "(sin cliente)"));
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
            throw new ErpException("Error al generar asiento de pago", e);
        }
    }

    /**
     * Generar asiento de compra a proveedor
     */
    public AsientoContable generarAsientoCompra(Long facturaCompraId, BigDecimal base,
                                                BigDecimal iva, BigDecimal total, Usuario usuario) {
        Objects.requireNonNull(base, "Base imponible no puede ser null");
        Objects.requireNonNull(iva, "IVA no puede ser null");
        Objects.requireNonNull(total, "Total no puede ser null");
        // usuario puede ser null para ejecutores automáticos

        if (base.compareTo(BigDecimal.ZERO) < 0 || iva.compareTo(BigDecimal.ZERO) < 0 || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Importes no pueden ser negativos");
        }

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

            if (asiento.getLineas() == null) asiento.setLineas(new LinkedHashSet<>());

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

            // Auditar (solo si usuario proporcionado)
            if (auditoriaService != null && usuario != null) {
                auditoriaService.registrarAccion(usuario, "CREAR_ASIENTO_COMPRA", "AsientoContable",
                    guardado.getId().toString(),
                    "Asiento de compra generado: " + guardado.getNumero());
            }

            return guardado;

        } catch (Exception e) {
            log.error("❌ Error generando asiento de compra", e);
            throw new ErpException("Error al generar asiento de compra", e);
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
        Objects.requireNonNull(desde, "Fecha desde no puede ser null");
        Objects.requireNonNull(hasta, "Fecha hasta no puede ser null");
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        log.info("📖 Obteniendo Libro Diario: {} - {}", desde, hasta);
        return asientoRepository.findByFechaBetween(desde, hasta);
    }

    /**
     * Obtener balance de sumas y saldos hasta la fecha indicada.
     * Agrupa todos los movimientos por cuenta y calcula debe, haber y saldo.
     */
    @Transactional(readOnly = true)
    public List<BalanceCuenta> obtenerBalance(LocalDate fecha) {
        Objects.requireNonNull(fecha, "Fecha no puede ser null");
        log.info("⚖️ Calculando balance a fecha: {}", fecha);

        List<Object[]> filas = asientoRepository.calcularBalanceHasta(fecha);
        List<BalanceCuenta> balance = new ArrayList<>(filas.size());

        for (Object[] fila : filas) {
            String codigo  = (String) fila[0];
            String nombre  = (String) fila[1];
            String tipo    = (String) fila[2];
            BigDecimal debe   = fila[3] != null ? new BigDecimal(fila[3].toString()) : BigDecimal.ZERO;
            BigDecimal haber  = fila[4] != null ? new BigDecimal(fila[4].toString()) : BigDecimal.ZERO;
            // El saldo depende del tipo de cuenta (cuentas de activo/gasto: saldo = debe - haber; pasivo/ingreso/patrimonio: haber - debe)
            BigDecimal saldo;
            if ("ACTIVO".equalsIgnoreCase(tipo) || "GASTO".equalsIgnoreCase(tipo)) {
                saldo = debe.subtract(haber);
            } else {
                saldo = haber.subtract(debe);
            }
            balance.add(new BalanceCuenta(codigo, nombre, debe, haber, saldo));
        }

        log.info("⚖️ Balance calculado: {} cuentas", balance.size());
        return balance;
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

    @Transactional(readOnly = true)
    public Map<String, Object> calcularBalanceSimple(LocalDate desde, LocalDate hasta) {
        List<AsientoContable> asientos = asientoRepository.findByFechaBetween(desde, hasta);
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        for (AsientoContable a : asientos) {
            totalDebe = totalDebe.add(a.getDebe() != null ? a.getDebe() : BigDecimal.ZERO);
            totalHaber = totalHaber.add(a.getHaber() != null ? a.getHaber() : BigDecimal.ZERO);
        }
        return java.util.Map.of(
            "totalDebe", totalDebe,
            "totalHaber", totalHaber,
            "diferencia", totalDebe.subtract(totalHaber),
            "asientos", asientos.size()
        );
    }

    public Map<String, Object> cerrarEjercicio(int año) {
        LocalDate inicio = LocalDate.of(año, 1, 1);
        LocalDate fin = LocalDate.of(año, 12, 31);
        List<AsientoContable> asientos = asientoRepository.findByFechaBetween(inicio, fin);
        boolean yaExisteCierre = asientos.stream().anyMatch(a -> Boolean.TRUE.equals(a.getAsientoCierre()));
        if (yaExisteCierre) {
            throw new IllegalStateException("Ya existe un cierre contable para el ejercicio " + año);
        }
        BigDecimal totalDebe = asientos.stream()
                .map(a -> a.getDebe() != null ? a.getDebe() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHaber = asientos.stream()
                .map(a -> a.getHaber() != null ? a.getHaber() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AsientoContable cierre = new AsientoContable();
        cierre.setNumero(generarNumeroAsiento());
        cierre.setFecha(fin);
        cierre.setConcepto("Cierre ejercicio " + año);
        cierre.setTipo("CIERRE");
        cierre.setDebe(totalHaber);
        cierre.setHaber(totalDebe);
        cierre.setAsientoCierre(true);
        asientoRepository.save(cierre);
        return java.util.Map.of(
            "mensaje", "Cierre del ejercicio " + año + " completado",
            "totalDebe", totalDebe,
            "totalHaber", totalHaber,
            "asientoCierreId", cierre.getId()
        );
    }

    // Comprueba la integridad contable al iniciar
    @EventListener(ApplicationReadyEvent.class)
    public void comprobarIntegridadContableOnStartup() {
        try {
            boolean cuadrados = validarCuadreContable();
            log.info("Integridad contable al inicio: todos los asientos cuadrados = {}", cuadrados);
        } catch (Exception e) {
            log.warn("No se pudo verificar integridad contable al inicio: {}", e.getMessage());
        }
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

