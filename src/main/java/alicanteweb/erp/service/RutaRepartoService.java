package alicanteweb.erp.service;

import alicanteweb.erp.entities.RutaReparto;
import alicanteweb.erp.entities.RutaParada;
import alicanteweb.erp.repository.RutaParadaRepository;
import alicanteweb.erp.repository.RutaRepartoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<RutaReparto> findDetailById(Long id) {
        return rutaRepository.findDetailById(id);
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
        Map<Long, RutaParada> paradaMap = paradaRepository.findAllById(paradaIdsEnOrden)
                .stream().collect(Collectors.toMap(RutaParada::getId, p -> p));
        List<RutaParada> actualizadas = new ArrayList<>(paradaIdsEnOrden.size());
        for (int i = 0; i < paradaIdsEnOrden.size(); i++) {
            Long paradaId = paradaIdsEnOrden.get(i);
            RutaParada parada = paradaMap.get(paradaId);
            if (parada == null) throw new IllegalArgumentException("Parada no encontrada: " + paradaId);
            parada.setOrden(i + 1);
            actualizadas.add(parada);
        }
        paradaRepository.saveAll(actualizadas);
    }

    @Transactional
    public void darDeBaja(Long id) {
        RutaReparto ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + id));
        ruta.setActivo(false);
        rutaRepository.save(ruta);
    }
}
