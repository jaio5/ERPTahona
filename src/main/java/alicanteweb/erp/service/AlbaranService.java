package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AlbaranVentaRepository;
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
 * Servicio completo para la gestión de Albaranes de Venta
 * Incluye flujo completo: crear, convertir a factura, control de stock, etc.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AlbaranService {
    private static final Logger log = LoggerFactory.getLogger(AlbaranService.class);

    private final AlbaranVentaRepository albaranRepository;
    private final FacturaService facturaService;
    private final AuditoriaService auditoriaService;

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
        if (albaran.getLineas() != null && !albaran.getLineas().isEmpty()) {
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

    // ==========================================
    // FLUJO COMPLETO - CONVERSIÓN A FACTURA
    // ==========================================

    /**
     * Convierte un albarán en factura
     */
    public Factura convertirAFactura(Long albaranId, Usuario usuario) {
        log.info("🔄 Convirtiendo albarán {} a factura", albaranId);

        AlbaranVenta albaran = albaranRepository.findById(albaranId)
            .orElseThrow(() -> new IllegalArgumentException("Albarán no encontrado"));

        // Crear factura
        Factura factura = new Factura();
        factura.setCliente(albaran.getCliente());
        factura.setFecha(LocalDate.now());
        factura.setEstado("BORRADOR");
        factura.setObservaciones("Generada desde albarán " + albaran.getNumero());

        // Copiar líneas del albarán
        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        for (AlbaranVentaLinea lineaAlbaran : albaran.getLineas()) {
            FacturaLinea lineaFactura = new FacturaLinea();
            lineaFactura.setFactura(factura);
            lineaFactura.setArticulo(lineaAlbaran.getArticulo());
            lineaFactura.setDescripcion(lineaAlbaran.getDescripcion() != null ?
                lineaAlbaran.getDescripcion() :
                (lineaAlbaran.getArticulo() != null ? lineaAlbaran.getArticulo().getNombre() : ""));
            lineaFactura.setCantidad(lineaAlbaran.getCantidad());
            lineaFactura.setPrecioUnitario(lineaAlbaran.getPrecio());
            lineaFactura.setDescuento(lineaAlbaran.getDescuento());
            lineaFactura.setIva(lineaAlbaran.getIva());

            // Calcular totales
            BigDecimal subtotal = lineaAlbaran.getCantidad().multiply(lineaAlbaran.getPrecio());
            if (lineaAlbaran.getDescuento() != null && lineaAlbaran.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal descuentoImporte = subtotal.multiply(lineaAlbaran.getDescuento())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                subtotal = subtotal.subtract(descuentoImporte);
            }

            lineaFactura.setTotal(subtotal);
            baseImponible = baseImponible.add(subtotal);

            if (lineaAlbaran.getIva() != null) {
                BigDecimal ivaLinea = subtotal.multiply(lineaAlbaran.getIva())
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

        log.info("✅ Albarán convertido a factura: {} -> {}", albaran.getNumero(), facturaGuardada.getNumero());

        // Auditar
        if (auditoriaService != null) {
            auditoriaService.registrarAccion(usuario, "ALBARAN", "CONVERTIR_FACTURA",
                "Albarán " + albaran.getNumero() + " convertido a factura " + facturaGuardada.getNumero(),
                "EXITOSO");
        }

        return facturaGuardada;
    }

    /**
     * Convierte múltiples albaranes en una sola factura
     */
    public Factura convertirVariosAFactura(List<Long> albaranIds, Usuario usuario) {
        log.info("🔄 Convirtiendo {} albaranes a una factura", albaranIds.size());

        if (albaranIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un albarán");
        }

        // Obtener todos los albaranes
        List<AlbaranVenta> albaranes = albaranRepository.findAllById(albaranIds);

        if (albaranes.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron los albaranes especificados");
        }

        // Verificar que todos sean del mismo cliente
        Cliente cliente = albaranes.get(0).getCliente();
        for (AlbaranVenta albaran : albaranes) {
            if (!albaran.getCliente().getId().equals(cliente.getId())) {
                throw new IllegalStateException("Todos los albaranes deben ser del mismo cliente");
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

        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        // Copiar líneas de todos los albaranes
        for (AlbaranVenta albaran : albaranes) {
            for (AlbaranVentaLinea lineaAlbaran : albaran.getLineas()) {
                FacturaLinea lineaFactura = new FacturaLinea();
                lineaFactura.setFactura(factura);
                lineaFactura.setArticulo(lineaAlbaran.getArticulo());
                lineaFactura.setDescripcion(lineaAlbaran.getDescripcion() != null ?
                    lineaAlbaran.getDescripcion() :
                    (lineaAlbaran.getArticulo() != null ? lineaAlbaran.getArticulo().getNombre() : ""));
                lineaFactura.setCantidad(lineaAlbaran.getCantidad());
                lineaFactura.setPrecioUnitario(lineaAlbaran.getPrecio());
                lineaFactura.setDescuento(lineaAlbaran.getDescuento());
                lineaFactura.setIva(lineaAlbaran.getIva());

                // Calcular totales
                BigDecimal subtotal = lineaAlbaran.getCantidad().multiply(lineaAlbaran.getPrecio());
                if (lineaAlbaran.getDescuento() != null && lineaAlbaran.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal descuentoImporte = subtotal.multiply(lineaAlbaran.getDescuento())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    subtotal = subtotal.subtract(descuentoImporte);
                }

                lineaFactura.setTotal(subtotal);
                baseImponible = baseImponible.add(subtotal);

                if (lineaAlbaran.getIva() != null) {
                    BigDecimal ivaLinea = subtotal.multiply(lineaAlbaran.getIva())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    totalIva = totalIva.add(ivaLinea);
                }

                factura.getFacturaLineas().add(lineaFactura);
            }
        }

        factura.setBaseImponible(baseImponible);
        factura.setTotalIva(totalIva);
        factura.setTotal(baseImponible.add(totalIva));

        // Guardar factura
        Factura facturaGuardada = facturaService.save(factura);

        log.info("✅ {} albaranes convertidos a factura: {}", albaranes.size(), facturaGuardada.getNumero());

        // Registrar auditoría usando el usuario proporcionado (si existe)
        if (auditoriaService != null && usuario != null) {
            auditoriaService.registrarAccion(usuario, "ALBARAN", "CONVERTIR_FACTURA_MASIVO",
                "Albaranes " + observaciones + " convertidos a factura " + facturaGuardada.getNumero(),
                "EXITOSO");
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
        duplicado.setObservaciones("Duplicado de " + original.getNumero());

        // Copiar líneas
        for (AlbaranVentaLinea lineaOriginal : original.getLineas()) {
            AlbaranVentaLinea lineaDuplicada = new AlbaranVentaLinea();
            lineaDuplicada.setAlbaran(duplicado);
            lineaDuplicada.setArticulo(lineaOriginal.getArticulo());
            lineaDuplicada.setDescripcion(lineaOriginal.getDescripcion());
            lineaDuplicada.setCantidad(lineaOriginal.getCantidad());
            lineaDuplicada.setPrecio(lineaOriginal.getPrecio());
            lineaDuplicada.setDescuento(lineaOriginal.getDescuento());
            lineaDuplicada.setIva(lineaOriginal.getIva());

            duplicado.getLineas().add(lineaDuplicada);
        }

        calcularTotal(duplicado);

        AlbaranVenta guardado = albaranRepository.save(duplicado);
        log.info("✅ Albarán duplicado: {}", guardado.getNumero());

        return guardado;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    /**
     * Genera un número de albarán automático
     */
    private String generarNumeroAlbaran() {
        int year = LocalDate.now().getYear();
        long count = albaranRepository.count() + 1;
        return String.format("ALB-%d-%05d", year, count);
    }

    /**
     * Calcula el total del albarán
     */
    private void calcularTotal(AlbaranVenta albaran) {
        BigDecimal total = BigDecimal.ZERO;

        if (albaran.getLineas() != null) {
            for (AlbaranVentaLinea linea : albaran.getLineas()) {
                if (linea.getCantidad() != null && linea.getPrecio() != null) {
                    BigDecimal subtotal = linea.getCantidad().multiply(linea.getPrecio());

                    // Aplicar descuento si existe
                    if (linea.getDescuento() != null && linea.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal descuentoImporte = subtotal.multiply(linea.getDescuento())
                            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        subtotal = subtotal.subtract(descuentoImporte);
                    }

                    // Aplicar IVA si existe
                    if (linea.getIva() != null && linea.getIva().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal ivaImporte = subtotal.multiply(linea.getIva())
                            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        subtotal = subtotal.add(ivaImporte);
                    }

                    total = total.add(subtotal);
                }
            }
        }

        albaran.setTotal(total);
    }

    // ==========================================
    // MÉTODOS ALIAS PARA COMPATIBILIDAD
    // ==========================================

    @Transactional(readOnly = true)
    public List<AlbaranVenta> findAll() {
        return obtenerTodos();
    }

    @Transactional(readOnly = true)
    public Optional<AlbaranVenta> findById(Long id) {
        return obtenerPorId(id);
    }

    public AlbaranVenta save(AlbaranVenta albaran) {
        return guardar(albaran);
    }

    public void deleteById(Long id) {
        eliminar(id);
    }

    @Transactional(readOnly = true)
    public List<AlbaranVenta> findByCliente(Long clienteId) {
        return buscarPorCliente(clienteId);
    }
}

