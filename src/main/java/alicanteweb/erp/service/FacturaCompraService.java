package alicanteweb.erp.service;

import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.repository.FacturaCompraRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de Facturas de Compra
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FacturaCompraService {
    private static final Logger log = LoggerFactory.getLogger(FacturaCompraService.class);

    private final FacturaCompraRepository facturaCompraRepository;

    /**
     * Obtiene todas las facturas de compra
     */
    @Transactional(readOnly = true)
    public List<FacturaCompra> obtenerTodas() {
        log.debug("Obteniendo todas las facturas de compra");
        return facturaCompraRepository.findAllOrdenadas();
    }

    /**
     * Obtiene una factura por ID
     */
    @Transactional(readOnly = true)
    public Optional<FacturaCompra> obtenerPorId(Long id) {
        log.debug("Obteniendo factura con ID: {}", id);
        return facturaCompraRepository.findById(id);
    }

    /**
     * Obtiene una factura por número
     */
    @Transactional(readOnly = true)
    public Optional<FacturaCompra> obtenerPorNumero(String numero) {
        log.debug("Obteniendo factura con número: {}", numero);
        return facturaCompraRepository.findByNumero(numero);
    }

    /**
     * Busca facturas por proveedor
     */
    @Transactional(readOnly = true)
    public List<FacturaCompra> buscarPorProveedor(Long proveedorId) {
        log.debug("Buscando facturas del proveedor: {}", proveedorId);
        return facturaCompraRepository.findByProveedorId(proveedorId);
    }

    /**
     * Busca facturas por estado
     */
    @Transactional(readOnly = true)
    public List<FacturaCompra> buscarPorEstado(String estado) {
        log.debug("Buscando facturas con estado: {}", estado);
        return facturaCompraRepository.findByEstado(estado);
    }

    /**
     * Busca facturas pendientes de pago
     */
    @Transactional(readOnly = true)
    public List<FacturaCompra> buscarPendientesPago() {
        log.debug("Buscando facturas pendientes de pago");
        return facturaCompraRepository.findPendientesPago();
    }

    /**
     * Busca facturas por texto
     */
    @Transactional(readOnly = true)
    public List<FacturaCompra> buscar(String busqueda) {
        log.debug("Buscando facturas con: {}", busqueda);
        return facturaCompraRepository.buscar(busqueda);
    }

    @Transactional(readOnly = true)
    public Page<FacturaCompra> findPage(String q, String estado, Pageable pageable) {
        return facturaCompraRepository.findPage(q, estado, pageable);
    }

    /**
     * Guarda una factura de compra
     */
    public FacturaCompra guardar(FacturaCompra facturaCompra) {
        log.info("Guardando factura de compra: {}", facturaCompra.getId());

        // Establecer fecha si no existe
        if (facturaCompra.getFecha() == null) {
            facturaCompra.setFecha(LocalDate.now());
        }

        // Establecer estado por defecto
        if (facturaCompra.getEstado() == null || facturaCompra.getEstado().isEmpty()) {
            facturaCompra.setEstado("PENDIENTE");
        }

        return facturaCompraRepository.save(facturaCompra);
    }

    /**
     * Actualiza una factura
     */
    public FacturaCompra actualizar(Long id, FacturaCompra facturaActualizada) {
        log.info("Actualizando factura con ID: {}", id);

        return facturaCompraRepository.findById(id)
            .map(factura -> {
                factura.setNumero(facturaActualizada.getNumero());
                factura.setFecha(facturaActualizada.getFecha());
                factura.setProveedor(facturaActualizada.getProveedor());
                factura.setBaseImponible(facturaActualizada.getBaseImponible());
                factura.setImporteIva(facturaActualizada.getImporteIva());
                factura.setTotal(facturaActualizada.getTotal());
                factura.setEstado(facturaActualizada.getEstado());
                return facturaCompraRepository.save(factura);
            })
            .orElseThrow(() -> new RuntimeException("Factura de compra no encontrada: " + id));
    }

    /**
     * Elimina una factura
     */
    public void eliminar(Long id) {
        log.info("Eliminando factura con ID: {}", id);
        facturaCompraRepository.deleteById(id);
    }

    /**
     * Cambia el estado de una factura
     */
    public FacturaCompra cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado de la factura {} a {}", id, nuevoEstado);

        return facturaCompraRepository.findById(id)
            .map(factura -> {
                factura.setEstado(nuevoEstado);
                return facturaCompraRepository.save(factura);
            })
            .orElseThrow(() -> new RuntimeException("Factura de compra no encontrada: " + id));
    }

    /**
     * Marca una factura como pagada
     */
    public FacturaCompra marcarComoPagada(Long id, LocalDate fechaPago) {
        log.info("Marcando factura {} como pagada", id);

        return facturaCompraRepository.findById(id)
            .map(factura -> {
                factura.setPagada(true);
                factura.setFechaPago(fechaPago);
                factura.setEstado("PAGADA");
                return facturaCompraRepository.save(factura);
            })
            .orElseThrow(() -> new RuntimeException("Factura de compra no encontrada: " + id));
    }
}

