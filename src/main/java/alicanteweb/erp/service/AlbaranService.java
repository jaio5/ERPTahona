package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AlbaranVentaFacturaRepository;
import alicanteweb.erp.repository.AlbaranVentaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import alicanteweb.erp.util.FinancialMath;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio completo para la gestión de Albaranes de Venta
 * Incluye flujo completo: crear, convertir a factura, control de stock, etc.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AlbaranService {
    private static final Logger log = LoggerFactory.getLogger(AlbaranService.class);

    private final AlbaranVentaRepository albaranRepository;
    private final AlbaranVentaFacturaRepository albaranFacturaRepository;
    private final FacturaService facturaService;
    private final AuditoriaService auditoriaService;
    private final StockService stockService;
    private final AlbaranNumeroService albaranNumeroService;

    // ==========================================
    // OPERACIONES CRUD BÁSICAS
    // ==========================================

    /**
     * Obtiene todos los albaranes
     */
    @Transactional(readOnly = true)
    public List<AlbaranVenta> obtenerTodos() {
        log.debug("Obteniendo todos los albaranes");
        return albaranRepository.findAll();
    }

    /**
     * Obtiene un albarán por ID
     */
    @Transactional(readOnly = true)
    public Optional<AlbaranVenta> obtenerPorId(Long id) {
        log.debug("Obteniendo albarán con ID: {}", id);
        return albaranRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AlbaranVenta> obtenerRecientesPorCliente(Long clienteId) {
        return albaranRepository.findTop10ByClienteIdWithLineas(clienteId, PageRequest.of(0, 10));
    }

    @Transactional(readOnly = true)
    public Optional<AlbaranVenta> obtenerPorIdParaPdf(Long id) {
        log.debug("Obteniendo albaran con datos de impresion: {}", id);
        return albaranRepository.findByIdWithPdfData(id);
    }

    /**
     * Obtiene un albarán por número
     */
    @Transactional(readOnly = true)
    public Optional<AlbaranVenta> obtenerPorNumero(String numero) {
        log.debug("Obteniendo albarán con número: {}", numero);
        return albaranRepository.findByNumero(numero);
    }

    /**
     * Busca albaranes por cliente
     */
    @Transactional(readOnly = true)
    public List<AlbaranVenta> buscarPorCliente(Long clienteId) {
        log.debug("Buscando albaranes del cliente: {}", clienteId);
        return albaranRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<AlbaranVenta> buscarPendientesFacturar() {
        return albaranRepository.findPendientesFacturar();
    }

    @Transactional(readOnly = true)
    public List<AlbaranVenta> buscarPendientesFacturarPorCliente(Long clienteId) {
        return albaranRepository.findPendientesFacturarByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<AlbaranVenta> findByFecha(LocalDate fecha) {
        return albaranRepository.findByFecha(fecha);
    }

    @Transactional(readOnly = true)
    public Page<AlbaranVenta> buscarPaginado(String q, String estado, Pageable pageable) {
        return albaranRepository.buscarPaginado(
            (q != null && !q.isBlank()) ? q : null,
            (estado != null && !estado.isBlank()) ? estado : null,
            pageable);
    }

    /**
     * Guarda un albarán
     */
    public AlbaranVenta guardar(AlbaranVenta albaran) {
        log.info("Guardando albarán: {}", albaran.getId());

        // Generar número si no existe
        if (albaran.getNumero() == null || albaran.getNumero().isEmpty()) {
            albaran.setNumero(generarNumeroAlbaran());
        } else {
            // Validación: si ya existe otro albarán con el mismo número, evitar duplicado
            Optional<AlbaranVenta> existente = obtenerPorNumero(albaran.getNumero());
            if (existente.isPresent() && (albaran.getId() == null || !existente.get().getId().equals(albaran.getId()))) {
                throw new IllegalStateException("Ya existe un albarán con el número: " + albaran.getNumero());
            }
        }

        // Establecer fecha si no existe
        if (albaran.getFecha() == null) {
            albaran.setFecha(LocalDate.now());
        }

        // Calcular total si tiene líneas
        if (albaran.getAlbaranVentaLineas() != null && !albaran.getAlbaranVentaLineas().isEmpty()) {
            calcularTotal(albaran);
        }

        return albaranRepository.save(albaran);
    }

    /**
     * Elimina un albarán
     */
    public void eliminar(Long id) {
        log.info("Eliminando albarán con ID: {}", id);
        albaranRepository.deleteById(id);
    }

    /**
     * Marca el albarán como ENTREGADO y deduce stock por cada línea.
     */
    @Transactional
    public AlbaranVenta marcarEntregado(Long id) {
        AlbaranVenta albaran = albaranRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Albarán no encontrado: " + id));
        if ("ENTREGADO".equals(albaran.getEstado())) {
            return albaran;
        }
        for (AlbaranVentaLinea linea : albaran.getAlbaranVentaLineas()) {
            if (linea.getArticulo() == null || linea.getCantidad() == null
                    || linea.getCantidad().compareTo(BigDecimal.ZERO) <= 0) continue;
            Long almacenId = albaran.getAlmacen() != null ? albaran.getAlmacen().getId() : null;
            stockService.registrarSalida(linea.getArticulo().getId(), almacenId, linea.getCantidad(),
                    "Entrega albaran " + albaran.getNumero(), "ALBARAN", albaran.getId());
        }
        albaran.setEstado("ENTREGADO");
        albaran = albaranRepository.save(albaran);
        log.info("Albarán {} marcado como ENTREGADO", albaran.getNumero());
        return albaran;
    }

    // ==========================================
    // FLUJO COMPLETO - CONVERSIÓN A FACTURA
    // ==========================================

    /**
     * Convierte un albarán en factura (versión básica, para compatibilidad REST)
     */
    public Factura convertirAFactura(Long albaranId, Usuario usuario) {
        return convertirAFactura(albaranId, usuario, null, null, null, null);
    }

    /**
     * Convierte un albarán en factura con datos de facturación opcionales
     */
    @Transactional
    public Factura convertirAFactura(Long albaranId, Usuario usuario,
                                     String fechaStr, String medioCobro,
                                     String fechaVencimientoStr, String observacionesExtra) {
        log.info("[CONVERSION] Convirtiendo albarán {} a factura", albaranId);

        AlbaranVenta albaran = albaranRepository.findById(albaranId)
            .orElseThrow(() -> new IllegalArgumentException("Albarán no encontrado"));
        if (albaranFacturaRepository.existsByAlbaran_Id(albaran.getId())) {
            throw new IllegalStateException("El albarán " + albaran.getNumero() + " ya está facturado");
        }

        // Crear factura
        Factura factura = new Factura();
        factura.setCliente(albaran.getCliente());
        factura.setFecha(fechaStr != null && !fechaStr.isBlank() ? LocalDate.parse(fechaStr) : LocalDate.now());
        factura.setEstado("BORRADOR");
        if (medioCobro != null && !medioCobro.isBlank()) factura.setMedioCobro(medioCobro);
        if (fechaVencimientoStr != null && !fechaVencimientoStr.isBlank()) {
            factura.setFechaVencimiento(LocalDate.parse(fechaVencimientoStr));
        }
        String obs = "Generada desde albarán " + albaran.getNumero();
        if (observacionesExtra != null && !observacionesExtra.isBlank()) obs += ". " + observacionesExtra;
        factura.setObservaciones(obs);
        factura.setNumeroAlbaran(albaran.getNumero());

        // Copiar líneas del albarán
        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        for (AlbaranVentaLinea lineaAlbaran : albaran.getAlbaranVentaLineas()) {
            BigDecimal cantidad = lineaAlbaran.getCantidad();
            BigDecimal precio = lineaAlbaran.getPrecio();
            if (cantidad == null || precio == null) continue;

            FacturaLinea lineaFactura = new FacturaLinea();
            lineaFactura.setFactura(factura);
            lineaFactura.setArticulo(lineaAlbaran.getArticulo());
            lineaFactura.setDescripcion(lineaAlbaran.getDescripcion() != null ?
                lineaAlbaran.getDescripcion() :
                (lineaAlbaran.getArticulo() != null ? lineaAlbaran.getArticulo().getNombre() : ""));
            lineaFactura.setCantidad(cantidad);
            lineaFactura.setPrecioUnitario(precio);
            lineaFactura.setDescuento(lineaAlbaran.getDescuento());
            lineaFactura.setIva(lineaAlbaran.getIva());

            BigDecimal subtotal = calcularSubtotalLinea(cantidad, precio, lineaAlbaran.getDescuento());
            lineaFactura.setTotal(subtotal);
            baseImponible = baseImponible.add(subtotal);

            if (lineaAlbaran.getIva() != null) {
                totalIva = totalIva.add(FinancialMath.porcentaje(subtotal, lineaAlbaran.getIva()));
            }

            factura.getFacturaLineas().add(lineaFactura);
        }

        factura.setBaseImponible(baseImponible);
        factura.setTotalIva(totalIva);
        factura.setTotal(baseImponible.add(totalIva));

        // Guardar factura
        Factura facturaGuardada = facturaService.save(factura);
        vincularAlbaranFactura(albaran, facturaGuardada);

        albaran.setEstado("FACTURADO");
        albaranRepository.save(albaran);

        log.info("[OK] Albarán convertido a factura: {} -> {}", albaran.getNumero(), facturaGuardada.getNumero());

        // Auditar
        if (auditoriaService != null) {
            auditoriaService.registrarAccion(usuario, "CONVERTIR_FACTURA", "AlbaranVenta",
                albaran.getId().toString(),
                "Albarán " + albaran.getNumero() + " convertido a factura " + facturaGuardada.getNumero());
        }

        return facturaGuardada;
    }

    /**
     * Convierte múltiples albaranes en una sola factura
     */
    @Transactional
    public Factura convertirVariosAFactura(List<Long> albaranIds, Usuario usuario) {
        log.info("[CONVERSION] Convirtiendo {} albaranes a una factura", albaranIds.size());

        if (albaranIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un albarán");
        }

        // Obtener todos los albaranes
        List<AlbaranVenta> albaranes = albaranRepository.findAllById(albaranIds);

        if (albaranes.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron los albaranes especificados");
        }
        if (albaranes.size() != albaranIds.size()) {
            List<Long> encontrados = albaranes.stream().map(AlbaranVenta::getId).toList();
            List<Long> noEncontrados = albaranIds.stream().filter(i -> !encontrados.contains(i)).toList();
            throw new IllegalArgumentException("Albaranes no encontrados: " + noEncontrados);
        }

        // Verificar que todos tengan cliente y sean del mismo
        Cliente cliente = albaranes.get(0).getCliente();
        if (cliente == null) {
            throw new IllegalArgumentException("El albarán " + albaranes.get(0).getNumero() + " no tiene cliente asignado");
        }
        for (AlbaranVenta albaran : albaranes) {
            if (albaran.getCliente() == null) {
                throw new IllegalArgumentException("El albarán " + albaran.getNumero() + " no tiene cliente asignado");
            }
            if (!albaran.getCliente().getId().equals(cliente.getId())) {
                throw new IllegalStateException("Todos los albaranes deben ser del mismo cliente");
            }
            if (albaranFacturaRepository.existsByAlbaran_Id(albaran.getId())) {
                throw new IllegalStateException("El albarán " + albaran.getNumero() + " ya está facturado");
            }
        }

        // Crear factura
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setFecha(LocalDate.now());
        factura.setEstado("BORRADOR");

        StringBuilder observaciones = new StringBuilder("Generada desde albaranes: ");
        for (int i = 0; i < albaranes.size(); i++) {
            observaciones.append(albaranes.get(i).getNumero());
            if (i < albaranes.size() - 1) observaciones.append(", ");
        }
        factura.setObservaciones(observaciones.toString());
        factura.setNumeroAlbaran(albaranes.stream().map(AlbaranVenta::getNumero).reduce((a, b) -> a + ", " + b).orElse(null));

        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        // Copiar líneas de todos los albaranes
        for (AlbaranVenta albaran : albaranes) {
            for (AlbaranVentaLinea lineaAlbaran : albaran.getAlbaranVentaLineas()) {
                BigDecimal cantidad = lineaAlbaran.getCantidad();
                BigDecimal precio = lineaAlbaran.getPrecio();
                if (cantidad == null || precio == null) continue;

                FacturaLinea lineaFactura = new FacturaLinea();
                lineaFactura.setFactura(factura);
                lineaFactura.setArticulo(lineaAlbaran.getArticulo());
                lineaFactura.setDescripcion(lineaAlbaran.getDescripcion() != null ?
                    lineaAlbaran.getDescripcion() :
                    (lineaAlbaran.getArticulo() != null ? lineaAlbaran.getArticulo().getNombre() : ""));
                lineaFactura.setCantidad(cantidad);
                lineaFactura.setPrecioUnitario(precio);
                lineaFactura.setDescuento(lineaAlbaran.getDescuento());
                lineaFactura.setIva(lineaAlbaran.getIva());

                BigDecimal subtotal = calcularSubtotalLinea(cantidad, precio, lineaAlbaran.getDescuento());
                lineaFactura.setTotal(subtotal);
                baseImponible = baseImponible.add(subtotal);

                if (lineaAlbaran.getIva() != null) {
                    totalIva = totalIva.add(FinancialMath.porcentaje(subtotal, lineaAlbaran.getIva()));
                }

                factura.getFacturaLineas().add(lineaFactura);
            }
        }

        factura.setBaseImponible(baseImponible);
        factura.setTotalIva(totalIva);
        factura.setTotal(baseImponible.add(totalIva));

        // Guardar factura y marcar albaranes como FACTURADO
        Factura facturaGuardada = facturaService.save(factura);
        for (AlbaranVenta albaran : albaranes) {
            vincularAlbaranFactura(albaran, facturaGuardada);
            albaran.setEstado("FACTURADO");
            albaranRepository.save(albaran);
        }

        log.info("[OK] {} albaranes convertidos a factura: {}", albaranes.size(), facturaGuardada.getNumero());

        // Registrar auditoría usando el usuario proporcionado (si existe)
        if (auditoriaService != null && usuario != null) {
            auditoriaService.registrarAccion(usuario, "CONVERTIR_FACTURA_MASIVO", "AlbaranVenta",
                facturaGuardada.getId().toString(),
                "Albaranes " + observaciones + " convertidos a factura " + facturaGuardada.getNumero());
        }

        return facturaGuardada;
    }

    /**
     * Duplica un albarán
     */
    public AlbaranVenta duplicar(Long albaranId) {
        log.info("📋 Duplicando albarán {}", albaranId);

        AlbaranVenta original = albaranRepository.findById(albaranId)
            .orElseThrow(() -> new IllegalArgumentException("Albarán no encontrado"));

        AlbaranVenta duplicado = new AlbaranVenta();
        duplicado.setNumero(generarNumeroAlbaran());
        duplicado.setFecha(LocalDate.now());
        duplicado.setCliente(original.getCliente());
        duplicado.setAlmacen(original.getAlmacen());
        duplicado.setObservaciones(original.getObservaciones());

        // Copiar líneas
        for (AlbaranVentaLinea lineaOriginal : original.getAlbaranVentaLineas()) {
            AlbaranVentaLinea lineaDuplicada = new AlbaranVentaLinea();
            lineaDuplicada.setAlbaran(duplicado);
            lineaDuplicada.setArticulo(lineaOriginal.getArticulo());
            lineaDuplicada.setDescripcion(lineaOriginal.getDescripcion());
            lineaDuplicada.setCantidad(lineaOriginal.getCantidad());
            lineaDuplicada.setPrecio(lineaOriginal.getPrecio());
            lineaDuplicada.setDescuento(lineaOriginal.getDescuento());
            lineaDuplicada.setIva(lineaOriginal.getIva());

            duplicado.getAlbaranVentaLineas().add(lineaDuplicada);
        }

        calcularTotal(duplicado);

        AlbaranVenta guardado = albaranRepository.save(duplicado);
        log.info("[OK] Albarán duplicado: {}", guardado.getNumero());

        return guardado;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    /**
     * Genera un número de albarán automático
     */
    public String generarNumeroAlbaran() {
        return albaranNumeroService.generarNumero();
    }

    private void vincularAlbaranFactura(AlbaranVenta albaran, Factura factura) {
        if (albaran == null || albaran.getId() == null || factura == null || factura.getId() == null) {
            return;
        }
        if (albaranFacturaRepository.existsByAlbaran_Id(albaran.getId())) {
            return;
        }
        albaranFacturaRepository.vincular(albaran.getId(), factura.getId());
    }

    /**
     * Calcula el total del albarán (base + IVA incluido).
     */
    private void calcularTotal(AlbaranVenta albaran) {
        BigDecimal total = BigDecimal.ZERO;
        if (albaran.getAlbaranVentaLineas() != null) {
            for (AlbaranVentaLinea linea : albaran.getAlbaranVentaLineas()) {
                if (linea.getCantidad() == null || linea.getPrecio() == null) continue;
                BigDecimal subtotal = calcularSubtotalLinea(linea.getCantidad(), linea.getPrecio(), linea.getDescuento());
                if (linea.getIva() != null && linea.getIva().compareTo(BigDecimal.ZERO) > 0) {
                    subtotal = subtotal.add(FinancialMath.porcentaje(subtotal, linea.getIva()));
                }
                total = total.add(subtotal);
            }
        }
        albaran.setTotal(total);
    }

    private BigDecimal calcularSubtotalLinea(BigDecimal cantidad, BigDecimal precio, BigDecimal descuento) {
        return FinancialMath.subtotalConDescuento(cantidad, precio, descuento);
    }
}

