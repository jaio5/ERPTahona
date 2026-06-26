package alicanteweb.erp.service;

import alicanteweb.erp.entities.Recepcion;
import alicanteweb.erp.entities.RecepcionLinea;
import alicanteweb.erp.repository.RecepcionLineaRepository;
import alicanteweb.erp.repository.RecepcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RecepcionService {

    private final RecepcionRepository repository;
    private final RecepcionLineaRepository lineaRepository;
    private final StockService stockService;

    public RecepcionService(RecepcionRepository repository,
                             RecepcionLineaRepository lineaRepository,
                             StockService stockService) {
        this.repository = repository;
        this.lineaRepository = lineaRepository;
        this.stockService = stockService;
    }

    public List<Recepcion> findAll() {
        return repository.findAll();
    }

    public List<Recepcion> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public Optional<Recepcion> findById(Long id) {
        return repository.findById(id);
    }

    public List<RecepcionLinea> findByRecepcionId(Long id) {
        return lineaRepository.findByRecepcionId(id);
    }

    @Transactional
    public Recepcion save(Recepcion recepcion) {
        return repository.save(recepcion);
    }

    @Transactional
    public RecepcionLinea saveLinea(RecepcionLinea linea) {
        return lineaRepository.save(linea);
    }

    @Transactional
    public void deleteLineasByRecepcionId(Long id) {
        lineaRepository.deleteByRecepcionId(id);
    }

    @Transactional
    public Recepcion confirmar(Long id) {
        Recepcion r = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recepción no encontrada: " + id));
        if ("CONFIRMADA".equals(r.getEstado())) {
            throw new IllegalStateException("La recepción ya está confirmada");
        }
        r.setEstado("CONFIRMADA");
        r = repository.save(r);

        // Registrar entrada de stock por cada línea
        Long almacenId = r.getAlmacen() != null ? r.getAlmacen().getId() : null;
        for (RecepcionLinea linea : lineaRepository.findByRecepcionId(id)) {
            if (linea.getArticulo() != null && linea.getCantidadRecibida() != null
                    && linea.getCantidadRecibida().compareTo(java.math.BigDecimal.ZERO) > 0) {
                stockService.registrarEntrada(linea.getArticulo().getId(), almacenId, linea.getCantidadRecibida(),
                        "Recepción " + r.getNumero(), "RECEPCION", r.getId());
            }
        }
        return r;
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
