package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import alicanteweb.erp.util.FinancialMath;
import alicanteweb.erp.util.NumeracionDocumento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Servicio completo para la gestión de Pedidos
 * Incluye flujo completo: crear, convertir a albarán, gestión de estados, etc.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private static final PageRequest LISTA_PAGEABLE =
            PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "fecha"));

    private final PedidoRepository pedidoRepository;
    private final AlbaranService albaranService;
    private final AuditoriaService auditoriaService;

    // ==========================================
    // OPERACIONES CRUD BÁSICAS
    // ==========================================

    /**
     * Obtiene todos los pedidos
     */
    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodos() {
        log.debug("Obteniendo todos los pedidos");
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodosLimitado() {
        return pedidoRepository.findAll(LISTA_PAGEABLE).getContent();
    }

    @Transactional(readOnly = true)
    public Page<Pedido> obtenerTodos(Pageable pageable) {
        log.debug("Obteniendo todos los pedidos paginados");
        return pedidoRepository.findAll(pageable);
    }

    /**
     * Obtiene un pedido por ID
     */
    @Transactional(readOnly = true)
    public Optional<Pedido> obtenerPorId(Long id) {
        log.debug("Obteniendo pedido con ID: {}", id);
        return pedidoRepository.findById(id);
    }

    /**
     * Obtiene un pedido por número
     */
    @Transactional(readOnly = true)
    public Optional<Pedido> obtenerPorNumero(String numero) {
        log.debug("Obteniendo pedido con número: {}", numero);
        return pedidoRepository.findByNumero(numero);
    }

    /**
     * Busca pedidos por cliente
     */
    @Transactional(readOnly = true)
    public List<Pedido> buscarPorCliente(Long clienteId) {
        log.debug("Buscando pedidos del cliente: {}", clienteId);
        return pedidoRepository.findByClienteId(clienteId);
    }

    /**
     * Busca pedidos por estado
     */
    @Transactional(readOnly = true)
    public List<Pedido> buscarPorEstado(String estado) {
        log.debug("Buscando pedidos con estado: {}", estado);
        return pedidoRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Page<Pedido> buscarPorEstado(String estado, Pageable pageable) {
        log.debug("Buscando pedidos con estado: {} (paginado)", estado);
        return pedidoRepository.findByEstado(estado, pageable);
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscar(String q) {
        log.debug("Buscando pedidos: {}", q);
        return pedidoRepository.findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(q, q);
    }

    @Transactional(readOnly = true)
    public Page<Pedido> buscar(String q, Pageable pageable) {
        log.debug("Buscando pedidos: {} (paginado)", q);
        return pedidoRepository.findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(q, q, pageable);
    }

    @Transactional(readOnly = true)
    public long countByEstado(String estado) {
        return pedidoRepository.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Pedido> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return pedidoRepository.findByFechaBetween(inicio, fin);
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerRecientesPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findTop10ByCliente_IdOrderByFechaDescIdDesc(clienteId);
        // Forzar inicialización de proxies lazy dentro de la transacción
        pedidos.forEach(p -> p.getPedidoLineas().forEach(l -> {
            if (l.getArticulo() != null) org.hibernate.Hibernate.initialize(l.getArticulo());
        }));
        return pedidos;
    }

    /**
     * Guarda un pedido
     */
    public Pedido guardar(Pedido pedido) {
        log.info("Guardando pedido: {}", pedido.getId());

        // Generar número si no existe
        if (pedido.getNumero() == null || pedido.getNumero().isEmpty()) {
            pedido.setNumero(generarNumeroPedido());
        }

        // Establecer fecha si no existe
        if (pedido.getFecha() == null) {
            pedido.setFecha(LocalDate.now());
        }

        // Establecer estado por defecto
        if (pedido.getEstado() == null || pedido.getEstado().isEmpty()) {
            pedido.setEstado("PENDIENTE");
        }

        return pedidoRepository.save(pedido);
    }

    /**
     * Elimina un pedido
     */
    public void eliminar(Long id) {
        log.info("Eliminando pedido con ID: {}", id);
        pedidoRepository.deleteById(id);
    }

    /**
     * Cambia el estado de un pedido
     */
    public Pedido cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado del pedido {} a {}", id, nuevoEstado);

        return pedidoRepository.findById(id)
            .map(pedido -> {
                pedido.setEstado(nuevoEstado);
                return pedidoRepository.save(pedido);
            })
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
    }

    // ==========================================
    // FLUJO COMPLETO - CONVERSIÓN A ALBARÁN
    // ==========================================

    /**
     * Convierte un pedido en albarán de venta
     */
    public AlbaranVenta convertirAAlbaran(Long pedidoId, Usuario usuario) {
        log.info("[CONVERSION] Convirtiendo pedido {} a albarán", pedidoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Verificar estado
        if ("SERVIDO".equals(pedido.getEstado())) {
            throw new IllegalStateException("El pedido ya ha sido servido");
        }

        if ("CANCELADO".equals(pedido.getEstado())) {
            throw new IllegalStateException("No se puede servir un pedido cancelado");
        }

        // Crear albarán
        AlbaranVenta albaran = new AlbaranVenta();
        albaran.setCliente(pedido.getCliente());
        albaran.setFecha(LocalDate.now());
        albaran.setObservaciones("Generado desde pedido " + pedido.getNumero());

        // Copiar líneas del pedido
        for (PedidoLinea lineaPedido : pedido.getLineas()) {
            AlbaranVentaLinea lineaAlbaran = new AlbaranVentaLinea();
            lineaAlbaran.setAlbaran(albaran);
            lineaAlbaran.setArticulo(lineaPedido.getArticulo());
            lineaAlbaran.setDescripcion(lineaPedido.getDescripcion() != null ?
                lineaPedido.getDescripcion() :
                (lineaPedido.getArticulo() != null ? lineaPedido.getArticulo().getNombre() : ""));
            lineaAlbaran.setCantidad(lineaPedido.getCantidad());
            lineaAlbaran.setPrecio(lineaPedido.getPrecio());
            lineaAlbaran.setDescuento(lineaPedido.getDescuento() != null ? lineaPedido.getDescuento() : BigDecimal.ZERO);
            lineaAlbaran.setIva(lineaPedido.getIva() != null ? lineaPedido.getIva() : new BigDecimal("21"));

            albaran.getAlbaranVentaLineas().add(lineaAlbaran);
        }

        // Guardar albarán
        AlbaranVenta albaranGuardado =             albaranService.guardar(albaran);;

        // Actualizar estado del pedido
        pedido.setEstado("SERVIDO");
        pedidoRepository.save(pedido);

        log.info("[OK] Pedido convertido a albarán: {} -> {}", pedido.getNumero(), albaranGuardado.getNumero());

        // Auditar
        if (auditoriaService != null) {
            auditoriaService.registrarAccion(usuario, "CONVERTIR_ALBARAN", "Pedido",
                pedido.getId().toString(),
                "Pedido " + pedido.getNumero() + " convertido a albarán " + albaranGuardado.getNumero());
        }

        return albaranGuardado;
    }

    /**
     * Convierte un pedido en albarán con entrega parcial
     */
    public AlbaranVenta convertirAAlbaranParcial(Long pedidoId, List<EntregaParcial> entregas, Usuario usuario) {
        log.info("[CONVERSION] Convirtiendo pedido {} a albarán parcial", pedidoId);

        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if ("CANCELADO".equals(pedido.getEstado())) {
            throw new IllegalStateException("No se puede servir un pedido cancelado");
        }

        // Crear albarán
        AlbaranVenta albaran = new AlbaranVenta();
        albaran.setCliente(pedido.getCliente());
        albaran.setFecha(LocalDate.now());
        albaran.setObservaciones("Entrega parcial del pedido " + pedido.getNumero());

        // Agregar líneas según entregas parciales
        for (EntregaParcial entrega : entregas) {
            PedidoLinea lineaPedido = pedido.getLineas().stream()
                .filter(l -> l.getId().equals(entrega.lineaPedidoId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Línea de pedido no encontrada"));

            AlbaranVentaLinea lineaAlbaran = new AlbaranVentaLinea();
            lineaAlbaran.setAlbaran(albaran);
            lineaAlbaran.setArticulo(lineaPedido.getArticulo());
            lineaAlbaran.setDescripcion(lineaPedido.getDescripcion());
            lineaAlbaran.setCantidad(entrega.cantidad());
            lineaAlbaran.setPrecio(lineaPedido.getPrecio());
            lineaAlbaran.setDescuento(lineaPedido.getDescuento() != null ? lineaPedido.getDescuento() : BigDecimal.ZERO);
            lineaAlbaran.setIva(lineaPedido.getIva() != null ? lineaPedido.getIva() : new BigDecimal("21"));

            albaran.getAlbaranVentaLineas().add(lineaAlbaran);
        }

        // Guardar albarán
        AlbaranVenta albaranGuardado =             albaranService.guardar(albaran);;

        // Actualizar estado del pedido (si está completamente servido)
        boolean completamenteServido = verificarPedidoCompleto(pedido, entregas);
        if (completamenteServido) {
            pedido.setEstado("SERVIDO");
        } else {
            pedido.setEstado("SERVIDO_PARCIAL");
        }
        pedidoRepository.save(pedido);

        log.info("[OK] Pedido convertido a albarán parcial: {} -> {}", pedido.getNumero(), albaranGuardado.getNumero());

        return albaranGuardado;
    }

    /**
     * Duplica un pedido: mismo cliente y mismas líneas, con número nuevo, fecha de hoy,
     * estado PENDIENTE y total recalculado. No mueve stock ni copia vínculos a albaranes.
     */
    @Transactional
    public Pedido duplicar(Long pedidoId) {
        log.info("📋 Duplicando pedido {}", pedidoId);

        Pedido original = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        Pedido duplicado = new Pedido();
        duplicado.setNumero(generarNumeroPedido());
        duplicado.setFecha(LocalDate.now());
        duplicado.setCliente(original.getCliente());
        duplicado.setEstado("PENDIENTE");
        duplicado.setObservaciones("Duplicado de " + original.getNumero());

        // Copiar líneas
        for (PedidoLinea lineaOriginal : original.getLineas()) {
            PedidoLinea lineaDuplicada = new PedidoLinea();
            lineaDuplicada.setPedido(duplicado);
            lineaDuplicada.setArticulo(lineaOriginal.getArticulo());
            lineaDuplicada.setDescripcion(lineaOriginal.getDescripcion());
            lineaDuplicada.setCantidad(lineaOriginal.getCantidad());
            lineaDuplicada.setPrecio(lineaOriginal.getPrecio());
            lineaDuplicada.setDescuento(lineaOriginal.getDescuento());
            lineaDuplicada.setIva(lineaOriginal.getIva());

            duplicado.getLineas().add(lineaDuplicada);
        }
        duplicado.setTotal(calcularTotal(duplicado.getLineas()));

        Pedido guardado = pedidoRepository.save(duplicado);
        log.info("[OK] Pedido duplicado: {} -> {}", original.getNumero(), guardado.getNumero());

        return guardado;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    /**
     * Genera el siguiente número de pedido del ejercicio (PED-AAAA-00001...).
     * Parte del máximo existente con el prefijo del año, no de un count(), para
     * no repetir números tras un borrado. La UNIQUE de BD es la red de seguridad.
     */
    private String generarNumeroPedido() {
        String prefijo = NumeracionDocumento.prefijo("PED", LocalDate.now().getYear());
        return NumeracionDocumento.siguiente(prefijo,
                pedidoRepository.maxNumeroConPrefijo(prefijo),
                () -> pedidoRepository.numerosConPrefijo(prefijo));
    }

    /** Importe neto de una línea (cantidad x precio con el descuento aplicado, sin IVA). */
    public static BigDecimal importeLinea(PedidoLinea linea) {
        return FinancialMath.subtotalConDescuento(linea.getCantidad(), linea.getPrecio(), linea.getDescuento());
    }

    /** IVA de una línea sobre su importe neto. */
    public static BigDecimal ivaLinea(PedidoLinea linea) {
        return FinancialMath.porcentaje(importeLinea(linea), linea.getIva());
    }

    /**
     * Total del pedido a partir de sus líneas: suma de (neto con descuento + IVA),
     * la misma fórmula que se usa al guardar el pedido desde el formulario.
     */
    public static BigDecimal calcularTotal(Collection<PedidoLinea> lineas) {
        BigDecimal total = BigDecimal.ZERO;
        if (lineas == null) return total;
        for (PedidoLinea linea : lineas) {
            total = total.add(importeLinea(linea)).add(ivaLinea(linea));
        }
        return total;
    }

    /**
     * Verifica si un pedido está completamente servido
     */
    private boolean verificarPedidoCompleto(Pedido pedido, List<EntregaParcial> entregas) {
        for (PedidoLinea linea : pedido.getLineas()) {
            BigDecimal cantidadEntregada = entregas.stream()
                .filter(e -> e.lineaPedidoId().equals(linea.getId()))
                .map(EntregaParcial::cantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (cantidadEntregada.compareTo(linea.getCantidad()) < 0) {
                return false;
            }
        }
        return true;
    }
    public long count() {
        return pedidoRepository.count();
    }

    /** Record para entregas parciales */
    public record EntregaParcial(Long lineaPedidoId, BigDecimal cantidad) {}
}

