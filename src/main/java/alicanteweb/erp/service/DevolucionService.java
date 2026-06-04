package alicanteweb.erp.service;

import alicanteweb.erp.entities.Devolucion;
import alicanteweb.erp.entities.DevolucionLinea;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.repository.DevolucionLineaRepository;
import alicanteweb.erp.repository.DevolucionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final DevolucionLineaRepository lineaRepository;
    private final ArticuloService articuloService;
    private final LoteService loteService;

    public DevolucionService(DevolucionRepository devolucionRepository,
                              DevolucionLineaRepository lineaRepository,
                              ArticuloService articuloService,
                              LoteService loteService) {
        this.devolucionRepository = devolucionRepository;
        this.lineaRepository = lineaRepository;
        this.articuloService = articuloService;
        this.loteService = loteService;
    }

    public List<Devolucion> findAll() {
        return devolucionRepository.findAll();
    }

    public Optional<Devolucion> findById(Long id) {
        return devolucionRepository.findById(id);
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

    public List<Devolucion> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return devolucionRepository.findByFechaBetween(inicio, fin);
    }

    public List<DevolucionLinea> getLineas(Long devolucionId) {
        return lineaRepository.findByDevolucionId(devolucionId);
    }

    @Transactional
    public Devolucion save(Devolucion devolucion) {
        if (devolucion == null) throw new IllegalArgumentException("Devolución nula");
        BigDecimal total = BigDecimal.ZERO;
        if (devolucion.getLineas() != null) {
            for (DevolucionLinea linea : devolucion.getLineas()) {
                if (linea.getImporte() != null) {
                    total = total.add(linea.getImporte());
                }
            }
        }
        devolucion.setImporteTotal(total);
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
