package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.PresupuestoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio completo para la gestión de Presupuestos
 * Incluye flujo completo: crear, editar, convertir a factura, duplicar, etc.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PresupuestoService {
    private static final Logger log = LoggerFactory.getLogger(PresupuestoService.class);

    private final PresupuestoRepository presupuestoRepository;
    private final FacturaService facturaService;

    // ==========================================
    // OPERACIONES CRUD BÁSICAS
    // ==========================================

    /**
     * Obtiene todos los presupuestos
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> obtenerTodos() {
        log.debug("Obteniendo todos los presupuestos");
        return presupuestoRepository.findAllOrdenados();
    }

    /**
     * Obtiene un presupuesto por ID
     */
    @Transactional(readOnly = true)
    public Optional<Presupuesto> obtenerPorId(Long id) {
        log.debug("Obteniendo presupuesto con ID: {}", id);
        return presupuestoRepository.findById(id);
    }

    /**
     * Obtiene un presupuesto por número
     */
    @Transactional(readOnly = true)
    public Optional<Presupuesto> obtenerPorNumero(String numero) {
        log.debug("Obteniendo presupuesto con número: {}", numero);
        return presupuestoRepository.findByNumero(numero);
    }

    /**
     * Busca presupuestos por cliente
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> buscarPorCliente(Long clienteId) {
        log.debug("Buscando presupuestos del cliente: {}", clienteId);
        return presupuestoRepository.findByClienteId(clienteId);
    }

    /**
     * Busca presupuestos por estado
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> buscarPorEstado(String estado) {
        log.debug("Buscando presupuestos con estado: {}", estado);
        return presupuestoRepository.findByEstado(estado);
    }

    /**
     * Busca presupuestos por texto
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> buscar(String busqueda) {
        log.debug("Buscando presupuestos con: {}", busqueda);
        return presupuestoRepository.buscar(busqueda);
    }

    /**
     * Guarda un presupuesto
     */
    public Presupuesto guardar(Presupuesto presupuesto) {
        log.info("Guardando presupuesto: {}", presupuesto.getId());

        // Generar número si no existe
        if (presupuesto.getNumero() == null || presupuesto.getNumero().isEmpty()) {
            presupuesto.setNumero(generarNumeroPresupuesto());
        }

        // Establecer fecha si no existe
        if (presupuesto.getFecha() == null) {
            presupuesto.setFecha(LocalDate.now());
        }

        return presupuestoRepository.save(presupuesto);
    }

    /**
     * Actualiza un presupuesto
     */
    public Presupuesto actualizar(Long id, Presupuesto presupuestoActualizado) {
        log.info("Actualizando presupuesto con ID: {}", id);

        return presupuestoRepository.findById(id)
            .map(presupuesto -> {
                presupuesto.setFecha(presupuestoActualizado.getFecha());
                presupuesto.setFechaValidez(presupuestoActualizado.getFechaValidez());
                presupuesto.setCliente(presupuestoActualizado.getCliente());
                presupuesto.setTotal(presupuestoActualizado.getTotal());
                presupuesto.setEstado(presupuestoActualizado.getEstado());
                presupuesto.setObservaciones(presupuestoActualizado.getObservaciones());
                return presupuestoRepository.save(presupuesto);
            })
            .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado: " + id));
    }

    /**
     * Elimina un presupuesto
     */
    public void eliminar(Long id) {
        log.info("Eliminando presupuesto con ID: {}", id);
        presupuestoRepository.deleteById(id);
    }

    /**
     * Genera un número de presupuesto automático
     */
    private String generarNumeroPresupuesto() {
        int year = LocalDate.now().getYear();
        long count = presupuestoRepository.count() + 1;
        return String.format("PRE-%d-%05d", year, count);
    }

    /**
     * Cambia el estado de un presupuesto
     */
    public Presupuesto cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado del presupuesto {} a {}", id, nuevoEstado);

        return presupuestoRepository.findById(id)
            .map(presupuesto -> {
                presupuesto.setEstado(nuevoEstado);
                return presupuestoRepository.save(presupuesto);
            })
            .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado: " + id));
    }

    // ============================================
    // Métodos adicionales para compatibilidad con tests
    // ============================================

    /**
     * Guarda un presupuesto (alias de guardar)
     */
    public Presupuesto save(Presupuesto presupuesto) {
        return guardar(presupuesto);
    }

    /**
     * Obtiene un presupuesto por ID (alias de obtenerPorId)
     */
    @Transactional(readOnly = true)
    public Optional<Presupuesto> findById(Long id) {
        return obtenerPorId(id);
    }

    /**
     * Obtiene todos los presupuestos (alias de obtenerTodos)
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> findAll() {
        return obtenerTodos();
    }

    /**
     * Busca presupuestos por cliente (alias de buscarPorCliente)
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> findByCliente(Long clienteId) {
        return buscarPorCliente(clienteId);
    }

    /**
     * Acepta un presupuesto
     */
    public Presupuesto aceptar(Long id) {
        log.info("Aceptando presupuesto con ID: {}", id);
        return cambiarEstado(id, "ACEPTADO");
    }

    /**
     * Rechaza un presupuesto
     */
    public Presupuesto rechazar(Long id) {
        log.info("Rechazando presupuesto con ID: {}", id);
        return cambiarEstado(id, "RECHAZADO");
    }

    /**
     * Verifica si un presupuesto está caducado
     */
    public boolean estaCaducado(Presupuesto presupuesto) {
        if (presupuesto.getFechaValidez() == null) {
            return false;
        }
        return presupuesto.getFechaValidez().isBefore(LocalDate.now());
    }

    /**
     * Obtiene los presupuestos pendientes
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> findPendientes() {
        return buscarPorEstado("PENDIENTE");
    }

    /**
     * Elimina un presupuesto (alias de eliminar)
     */
    public void deleteById(Long id) {
        eliminar(id);
    }

    // ==========================================
    // FLUJO COMPLETO - MÉTODOS AVANZADOS
    // ==========================================

    /**
     * Convierte un presupuesto en factura
     */
    public Factura convertirAFactura(Long presupuestoId, Usuario usuario) {
        log.info("🔄 Convirtiendo presupuesto {} a factura", presupuestoId);

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
            .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        // Verificar estado
        if ("FACTURADO".equals(presupuesto.getEstado())) {
            throw new IllegalStateException("El presupuesto ya ha sido facturado");
        }

        if ("RECHAZADO".equals(presupuesto.getEstado())) {
            throw new IllegalStateException("No se puede facturar un presupuesto rechazado");
        }

        // Crear factura
        Factura factura = new Factura();
        factura.setCliente(presupuesto.getCliente());
        factura.setFecha(LocalDate.now());
        factura.setEstado("BORRADOR");
        factura.setObservaciones("Generada desde presupuesto " + presupuesto.getNumero());

        // Copiar líneas
        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        for (PresupuestoLinea lineaPresupuesto : presupuesto.getLineas()) {
            FacturaLinea lineaFactura = new FacturaLinea();
            lineaFactura.setFactura(factura);
            lineaFactura.setArticulo(lineaPresupuesto.getArticulo());
            lineaFactura.setDescripcion(lineaPresupuesto.getDescripcion());
            lineaFactura.setCantidad(lineaPresupuesto.getCantidad());
            lineaFactura.setPrecioUnitario(lineaPresupuesto.getPrecioUnitario());
            lineaFactura.setDescuento(lineaPresupuesto.getDescuento());
            lineaFactura.setIva(lineaPresupuesto.getTipoIva());

            // Calcular totales
            BigDecimal subtotal = lineaPresupuesto.getCantidad().multiply(lineaPresupuesto.getPrecioUnitario());
            if (lineaPresupuesto.getDescuento() != null && lineaPresupuesto.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal descuentoImporte = subtotal.multiply(lineaPresupuesto.getDescuento())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                subtotal = subtotal.subtract(descuentoImporte);
            }

            lineaFactura.setTotal(subtotal);
            baseImponible = baseImponible.add(subtotal);

            if (lineaPresupuesto.getTipoIva() != null) {
                BigDecimal ivaLinea = subtotal.multiply(lineaPresupuesto.getTipoIva())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                totalIva = totalIva.add(ivaLinea);
            }

            factura.getFacturaLineas().add(lineaFactura);
        }

        factura.setBaseImponible(baseImponible);
        factura.setTotalIva(totalIva);
        factura.setTotal(baseImponible.add(totalIva));

        // Guardar factura
        Factura facturaGuardada = facturaService.save(factura);

        // Actualizar estado del presupuesto
        presupuesto.setEstado("FACTURADO");
        presupuestoRepository.save(presupuesto);

        log.info("✅ Presupuesto convertido a factura: {} -> {}", presupuesto.getNumero(), facturaGuardada.getNumero());

        return facturaGuardada;
    }

    /**
     * Duplica un presupuesto
     */
    public Presupuesto duplicar(Long presupuestoId) {
        log.info("📋 Duplicando presupuesto {}", presupuestoId);

        Presupuesto original = presupuestoRepository.findById(presupuestoId)
            .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        Presupuesto duplicado = new Presupuesto();
        duplicado.setNumero(generarNumeroPresupuesto());
        duplicado.setFecha(LocalDate.now());
        duplicado.setFechaValidez(LocalDate.now().plusDays(30)); // 30 días de validez
        duplicado.setCliente(original.getCliente());
        duplicado.setEstado("BORRADOR");
        duplicado.setObservaciones("Duplicado de " + original.getNumero());
        duplicado.setTotal(original.getTotal());

        // Copiar líneas
        for (PresupuestoLinea lineaOriginal : original.getLineas()) {
            PresupuestoLinea lineaDuplicada = new PresupuestoLinea();
            lineaDuplicada.setPresupuesto(duplicado);
            lineaDuplicada.setArticulo(lineaOriginal.getArticulo());
            lineaDuplicada.setDescripcion(lineaOriginal.getDescripcion());
            lineaDuplicada.setCantidad(lineaOriginal.getCantidad());
            lineaDuplicada.setPrecioUnitario(lineaOriginal.getPrecioUnitario());
            lineaDuplicada.setDescuento(lineaOriginal.getDescuento());
            lineaDuplicada.setTipoIva(lineaOriginal.getTipoIva());
            lineaDuplicada.setImporte(lineaOriginal.getImporte());
            lineaDuplicada.setOrden(lineaOriginal.getOrden());

            duplicado.getLineas().add(lineaDuplicada);
        }

        Presupuesto guardado = presupuestoRepository.save(duplicado);
        log.info("✅ Presupuesto duplicado: {}", guardado.getNumero());

        return guardado;
    }

    /**
     * Marcar presupuestos caducados automáticamente
     */
    @Transactional
    public int marcarCaducados() {
        log.info("⏰ Marcando presupuestos caducados...");

        List<Presupuesto> pendientes = presupuestoRepository.findByEstado("PENDIENTE");
        int caducados = 0;

        LocalDate hoy = LocalDate.now();
        for (Presupuesto presupuesto : pendientes) {
            if (presupuesto.getFechaValidez() != null && presupuesto.getFechaValidez().isBefore(hoy)) {
                presupuesto.setEstado("CADUCADO");
                presupuestoRepository.save(presupuesto);
                caducados++;
            }
        }

        log.info("✅ {} presupuestos marcados como caducados", caducados);
        return caducados;
    }
}

