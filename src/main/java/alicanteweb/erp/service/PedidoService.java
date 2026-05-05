package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.PedidoRepository;
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
 * Servicio completo para la gestión de Pedidos
 * Incluye flujo completo: crear, convertir a albarán, gestión de estados, etc.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

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
        log.info("🔄 Convirtiendo pedido {} a albarán", pedidoId);

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

            albaran.getLineas().add(lineaAlbaran);
        }

        // Guardar albarán
        AlbaranVenta albaranGuardado = albaranService.save(albaran);

        // Actualizar estado del pedido
        pedido.setEstado("SERVIDO");
        pedidoRepository.save(pedido);

        log.info("✅ Pedido convertido a albarán: {} -> {}", pedido.getNumero(), albaranGuardado.getNumero());

        // Auditar
        if (auditoriaService != null) {
            auditoriaService.registrarAccion(usuario, "PEDIDO", "CONVERTIR_ALBARAN",
                "Pedido " + pedido.getNumero() + " convertido a albarán " + albaranGuardado.getNumero(),
                "EXITOSO");
        }

        return albaranGuardado;
    }

    /**
     * Convierte un pedido en albarán con entrega parcial
     */
    public AlbaranVenta convertirAAlbaranParcial(Long pedidoId, List<EntregaParcial> entregas, Usuario usuario) {
        log.info("🔄 Convirtiendo pedido {} a albarán parcial", pedidoId);

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

            albaran.getLineas().add(lineaAlbaran);
        }

        // Guardar albarán
        AlbaranVenta albaranGuardado = albaranService.save(albaran);

        // Actualizar estado del pedido (si está completamente servido)
        boolean completamenteServido = verificarPedidoCompleto(pedido, entregas);
        if (completamenteServido) {
            pedido.setEstado("SERVIDO");
        } else {
            pedido.setEstado("SERVIDO_PARCIAL");
        }
        pedidoRepository.save(pedido);

        log.info("✅ Pedido convertido a albarán parcial: {} -> {}", pedido.getNumero(), albaranGuardado.getNumero());

        return albaranGuardado;
    }

    /**
     * Duplica un pedido
     */
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

        Pedido guardado = pedidoRepository.save(duplicado);
        log.info("✅ Pedido duplicado: {}", guardado.getNumero());

        return guardado;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    /**
     * Genera un número de pedido automático
     */
    private String generarNumeroPedido() {
        int year = LocalDate.now().getYear();
        long count = pedidoRepository.count() + 1;
        return String.format("PED-%d-%05d", year, count);
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

    // ── Métodos alias — @deprecated, usar métodos primarios ─────────────────

    /** @deprecated Usar {@link #obtenerTodos()} */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<Pedido> findAll() { return obtenerTodos(); }

    /** @deprecated Usar {@link #obtenerPorId(Long)} */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public Optional<Pedido> findById(Long id) { return obtenerPorId(id); }

    /** @deprecated Usar {@link #guardar(Pedido)} */
    @Deprecated(since = "1.0", forRemoval = true)
    public Pedido save(Pedido pedido) { return guardar(pedido); }

    /** @deprecated Usar {@link #eliminar(Long)} */
    @Deprecated(since = "1.0", forRemoval = true)
    public void deleteById(Long id) { eliminar(id); }

    /** @deprecated Usar {@link #buscarPorCliente(Long)} */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<Pedido> findByCliente(Long clienteId) { return buscarPorCliente(clienteId); }

    /** @deprecated Usar {@link #buscarPorEstado(String)} con "PENDIENTE" */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<Pedido> findPendientes() { return buscarPorEstado("PENDIENTE"); }

    /** Record para entregas parciales */
    public record EntregaParcial(Long lineaPedidoId, BigDecimal cantidad) {}
}

