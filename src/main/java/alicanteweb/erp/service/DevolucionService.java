package alicanteweb.erp.service;

import alicanteweb.erp.entities.Devolucion;
import alicanteweb.erp.entities.DevolucionLinea;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.repository.DevolucionLineaRepository;
import alicanteweb.erp.repository.DevolucionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly = true)
public class DevolucionService {

    private static final Logger log = LoggerFactory.getLogger(DevolucionService.class);

    private final DevolucionRepository devolucionRepository;
    private final DevolucionLineaRepository lineaRepository;
    private final ArticuloService articuloService;
    private final LoteService loteService;
    private final StockService stockService;

    public DevolucionService(DevolucionRepository devolucionRepository,
                              DevolucionLineaRepository lineaRepository,
                              ArticuloService articuloService,
                              LoteService loteService,
                              StockService stockService) {
        this.devolucionRepository = devolucionRepository;
        this.lineaRepository = lineaRepository;
        this.articuloService = articuloService;
        this.loteService = loteService;
        this.stockService = stockService;
    }

    public List<Devolucion> findAll() {
        return devolucionRepository.findAllConCliente();
    }

    public Optional<Devolucion> findById(Long id) {
        return devolucionRepository.findById(id);
    }

    public Optional<Devolucion> findDetailById(Long id) {
        return devolucionRepository.findDetailById(id);
    }

    public List<Devolucion> findByClienteId(Long clienteId) {
        return devolucionRepository.findByClienteId(clienteId);
    }

    public List<Devolucion> findByAlbaranId(Long albaranId) {
        return devolucionRepository.findByAlbaranId(albaranId);
    }

    public List<Devolucion> findByEstado(String estado) {
        return devolucionRepository.findByEstado(estado);
    }

    public List<Devolucion> buscar(String q) {
        return devolucionRepository.buscar(q);
    }

    public List<Devolucion> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return devolucionRepository.findByFechaBetween(inicio, fin);
    }

    public Page<Devolucion> findPage(Long clienteId, String estado, Pageable pageable) {
        String normalized = estado == null || estado.isBlank() ? null : estado.trim();
        return devolucionRepository.findPage(clienteId, normalized, pageable);
    }

    public List<DevolucionLinea> getLineas(Long devolucionId) {
        return lineaRepository.findByDevolucionId(devolucionId);
    }

    @Transactional
    public Devolucion save(Devolucion devolucion) {
        if (devolucion == null) throw new IllegalArgumentException("Devolución nula");
        // Solo recalcular el total si la colección está cargada; en ediciones de cabecera
        // la entidad llega detached y acceder a las líneas lazy lanzaría LazyInitializationException
        if (devolucion.getLineas() != null && org.hibernate.Hibernate.isInitialized(devolucion.getLineas())) {
            BigDecimal total = BigDecimal.ZERO;
            for (DevolucionLinea linea : devolucion.getLineas()) {
                if (linea.getImporte() != null) {
                    total = total.add(linea.getImporte());
                }
            }
            devolucion.setImporteTotal(total);
        } else if (devolucion.getImporteTotal() == null) {
            devolucion.setImporteTotal(BigDecimal.ZERO);
        }
        return devolucionRepository.save(devolucion);
    }

    @Transactional
    public void deleteById(Long id) {
        devolucionRepository.deleteById(id);
    }

    @Transactional
    public DevolucionLinea addLinea(DevolucionLinea linea) {
        if (linea.getImporte() == null && linea.getPrecioUnitario() != null && linea.getCantidad() != null) {
            linea.setImporte(linea.getPrecioUnitario().multiply(linea.getCantidad()));
        }
        return lineaRepository.save(linea);
    }

    @Transactional
    public void removeLinea(Long lineaId) {
        lineaRepository.deleteById(lineaId);
    }

    @Transactional
    public Devolucion aceptarDevolucion(Long devolucionId) {
        Devolucion devolucion = devolucionRepository.findById(devolucionId)
                .orElseThrow(() -> new IllegalArgumentException("Devolución no encontrada: " + devolucionId));
        devolucion.setEstado("ACEPTADA");

        // Reponer stock por cada línea devuelta
        Long almacenId = devolucion.getAlbaran() != null && devolucion.getAlbaran().getAlmacen() != null
                ? devolucion.getAlbaran().getAlmacen().getId() : null;
        for (DevolucionLinea linea : devolucion.getLineas()) {
            if (linea.getArticulo() == null || linea.getCantidad() == null) continue;
            try {
                stockService.registrarEntrada(
                        linea.getArticulo().getId(), almacenId, linea.getCantidad(),
                        "Devolución aceptada " + devolucion.getNumero(), "DEVOLUCION", devolucion.getId());
            } catch (Exception e) {
                log.error("Error reponiendo stock artículo {} en devolución {}: {}",
                        linea.getArticulo().getId(), devolucionId, e.getMessage());
                throw new IllegalStateException(
                        "No se pudo reponer stock del artículo " + linea.getArticulo().getCodigo() +
                        ": " + e.getMessage(), e);
            }
        }
        log.info("Devolución {} aceptada, stock repuesto para {} líneas",
                devolucion.getNumero(), devolucion.getLineas().size());
        return devolucionRepository.save(devolucion);
    }

    @Transactional
    public Devolucion rechazarDevolucion(Long devolucionId, String motivo) {
        Devolucion devolucion = devolucionRepository.findById(devolucionId)
                .orElseThrow(() -> new IllegalArgumentException("Devolución no encontrada: " + devolucionId));
        devolucion.setEstado("RECHAZADA");
        devolucion.setObservaciones((devolucion.getObservaciones() != null ? devolucion.getObservaciones() + " | " : "") + motivo);
        return devolucionRepository.save(devolucion);
    }
}
