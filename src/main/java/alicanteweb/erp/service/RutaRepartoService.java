package alicanteweb.erp.service;

import alicanteweb.erp.entities.RutaReparto;
import alicanteweb.erp.entities.RutaParada;
import alicanteweb.erp.repository.RutaParadaRepository;
import alicanteweb.erp.repository.RutaRepartoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RutaRepartoService {

    private final RutaRepartoRepository rutaRepository;
    private final RutaParadaRepository paradaRepository;

    public RutaRepartoService(RutaRepartoRepository rutaRepository, RutaParadaRepository paradaRepository) {
        this.rutaRepository = rutaRepository;
        this.paradaRepository = paradaRepository;
    }

    public List<RutaReparto> findAll() {
        return rutaRepository.findAll();
    }

    public Optional<RutaReparto> findById(Long id) {
        return rutaRepository.findById(id);
    }

    public List<RutaReparto> findByActivo(boolean activo) {
        return rutaRepository.findByActivo(activo);
    }

    public List<RutaReparto> searchByNombre(String texto) {
        return rutaRepository.findByNombreContainingIgnoreCase(texto);
    }

    @Transactional
    public RutaReparto save(RutaReparto ruta) {
        if (ruta == null) throw new IllegalArgumentException("Ruta nula");
        return rutaRepository.save(ruta);
    }

    @Transactional
    public void deleteById(Long id) {
        rutaRepository.deleteById(id);
    }

    public List<RutaParada> getParadas(Long rutaId) {
        return paradaRepository.findByRutaIdOrderByOrden(rutaId);
    }

    @Transactional
    public RutaParada addParada(RutaParada parada) {
        if (parada.getOrden() == null) {
            int maxOrden = paradaRepository.findByRutaIdOrderByOrden(parada.getRuta().getId())
                    .stream().mapToInt(p -> p.getOrden() != null ? p.getOrden() : 0).max().orElse(0);
            parada.setOrden(maxOrden + 1);
        }
        return paradaRepository.save(parada);
    }

    @Transactional
    public void removeParada(Long paradaId) {
        paradaRepository.deleteById(paradaId);
    }

    @Transactional
    public void reordenarParadas(Long rutaId, List<Long> paradaIdsEnOrden) {
        for (int i = 0; i < paradaIdsEnOrden.size(); i++) {
            RutaParada parada = paradaRepository.findById(paradaIdsEnOrden.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Parada no encontrada"));
            parada.setOrden(i + 1);
            paradaRepository.save(parada);
        }
    }

    @Transactional
    public void darDeBaja(Long id) {
        RutaReparto ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + id));
        ruta.setActivo(false);
        rutaRepository.save(ruta);
    }
}
