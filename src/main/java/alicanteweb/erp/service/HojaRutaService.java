package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.HojaRutaEntregaRepository;
import alicanteweb.erp.repository.HojaRutaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class HojaRutaService {

    private final HojaRutaRepository hojaRutaRepository;
    private final HojaRutaEntregaRepository entregaRepository;
    private final RutaRepartoService rutaRepartoService;
    private final AlbaranService albaranService;

    public HojaRutaService(HojaRutaRepository hojaRutaRepository,
                             HojaRutaEntregaRepository entregaRepository,
                             RutaRepartoService rutaRepartoService,
                             AlbaranService albaranService) {
        this.hojaRutaRepository = hojaRutaRepository;
        this.entregaRepository = entregaRepository;
        this.rutaRepartoService = rutaRepartoService;
        this.albaranService = albaranService;
    }

    public List<HojaRuta> findAll() {
        return hojaRutaRepository.findAll();
    }

    public Optional<HojaRuta> findById(Long id) {
        return hojaRutaRepository.findById(id);
    }

    public List<HojaRuta> findByFecha(LocalDate fecha) {
        return hojaRutaRepository.findByFecha(fecha);
    }

    public Optional<HojaRuta> findRutaDelUsuario(LocalDate fecha, Long usuarioId,
                                                  String username, String displayName) {
        if (usuarioId != null) {
            Optional<HojaRuta> asignada = hojaRutaRepository.findFirstByFechaAndUsuarioId(fecha, usuarioId);
            if (asignada.isPresent()) return asignada;
        }
        Optional<HojaRuta> legacy = findRutaLegacy(fecha, username);
        return legacy.isPresent() ? legacy : findRutaLegacy(fecha, displayName);
    }

    public boolean puedeGestionarHoja(Long hojaId, Long usuarioId, String username, String displayName) {
        if (usuarioId != null && hojaRutaRepository.existsByIdAndUsuarioId(hojaId, usuarioId)) return true;
        return existeHojaLegacy(hojaId, username) || existeHojaLegacy(hojaId, displayName);
    }

    public boolean puedeGestionarEntrega(Long entregaId, Long usuarioId, String username, String displayName) {
        if (usuarioId != null && entregaRepository.existsByIdAndHojaRutaUsuarioId(entregaId, usuarioId)) return true;
        return existeEntregaLegacy(entregaId, username) || existeEntregaLegacy(entregaId, displayName);
    }

    public List<HojaRuta> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return hojaRutaRepository.findByFechaBetween(inicio, fin);
    }

    public List<HojaRuta> findByRutaId(Long rutaId) {
        return hojaRutaRepository.findByRutaId(rutaId);
    }

    public List<HojaRuta> findByEstado(String estado) {
        return hojaRutaRepository.findByEstado(estado);
    }

    public long countByEstado(String estado) {
        return hojaRutaRepository.countByEstado(estado);
    }

    @Transactional
    public HojaRuta save(HojaRuta hojaRuta) {
        if (hojaRuta == null) throw new IllegalArgumentException("Hoja de ruta nula");
        return hojaRutaRepository.save(hojaRuta);
    }

    @Transactional
    public void deleteById(Long id) {
        hojaRutaRepository.deleteById(id);
    }

    /**
     * Genera una hoja de ruta a partir de una ruta maestra para una fecha concreta,
     * copiando las paradas como entregas planificadas.
     */
    @Transactional
    public HojaRuta generarDesdeRuta(Long rutaId, LocalDate fecha) {
        RutaReparto ruta = rutaRepartoService.findById(rutaId)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + rutaId));

        HojaRuta hoja = new HojaRuta();
        hoja.setRuta(ruta);
        hoja.setFecha(fecha);
        hoja.setVehiculo(ruta.getVehiculo());
        hoja.setConductor(ruta.getConductor());
        hoja.setEstado("PLANIFICADA");
        hoja = hojaRutaRepository.save(hoja);

        List<RutaParada> paradas = rutaRepartoService.getParadas(rutaId);
        for (RutaParada parada : paradas) {
            HojaRutaEntrega entrega = new HojaRutaEntrega();
            entrega.setHojaRuta(hoja);
            entrega.setCliente(parada.getCliente());
            entrega.setOrden(parada.getOrden());
            entrega.setEntregado(false);
            entregaRepository.save(entrega);
        }

        return hoja;
    }

    public List<HojaRutaEntrega> getEntregas(Long hojaRutaId) {
        return entregaRepository.findByHojaRutaIdOrderByOrden(hojaRutaId);
    }

    public Optional<HojaRuta> findDetailById(Long id) {
        return hojaRutaRepository.findDetailById(id);
    }

    public List<HojaRutaEntrega> getEntregasDetalle(Long hojaRutaId) {
        return entregaRepository.findDetailByHojaRutaId(hojaRutaId);
    }

    private Optional<HojaRuta> findRutaLegacy(LocalDate fecha, String conductor) {
        if (conductor == null || conductor.isBlank()) return Optional.empty();
        return hojaRutaRepository.findFirstByFechaAndUsuarioIsNullAndConductorIgnoreCase(fecha, conductor);
    }

    private boolean existeHojaLegacy(Long hojaId, String conductor) {
        return conductor != null && !conductor.isBlank()
                && hojaRutaRepository.existsByIdAndUsuarioIsNullAndConductorIgnoreCase(hojaId, conductor);
    }

    private boolean existeEntregaLegacy(Long entregaId, String conductor) {
        return conductor != null && !conductor.isBlank()
                && entregaRepository.existsByIdAndHojaRutaUsuarioIsNullAndHojaRutaConductorIgnoreCase(
                        entregaId, conductor);
    }

    public List<Long> getAlbaranIdsAsignados() {
        return entregaRepository.findAllAlbaranIdsAsignados();
    }

    @Transactional
    public HojaRutaEntrega saveEntrega(HojaRutaEntrega entrega) {
        return entregaRepository.save(entrega);
    }

    @Transactional
    public HojaRutaEntrega confirmarEntrega(Long entregaId, String personaRecepcion,
                                              String incidencia, java.math.BigDecimal importeCobrado,
                                              String medioCobro) {
        HojaRutaEntrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + entregaId));
        entrega.setEntregado(true);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setPersonaRecepcion(personaRecepcion);
        entrega.setIncidencia(incidencia);
        entrega.setEstadoEntrega(incidencia != null && !incidencia.isEmpty() ? "ENTREGADO_CON_INCIDENCIA" : "ENTREGADO");
        entrega.setImporteCobrado(importeCobrado);
        entrega.setMedioCobro(medioCobro);
        return entregaRepository.save(entrega);
    }

    @Transactional
    public HojaRutaEntrega marcarIncidencia(Long entregaId, String incidencia) {
        HojaRutaEntrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + entregaId));
        entrega.setEntregado(false);
        entrega.setIncidencia(incidencia);
        entrega.setEstadoEntrega("INCIDENCIA");
        return entregaRepository.save(entrega);
    }

    @Transactional
    public HojaRuta iniciarRuta(Long hojaRutaId) {
        HojaRuta hoja = hojaRutaRepository.findById(hojaRutaId)
                .orElseThrow(() -> new IllegalArgumentException("Hoja de ruta no encontrada: " + hojaRutaId));
        hoja.setEstado("EN_CURSO");
        hoja.setHoraSalida(LocalDateTime.now());
        return hojaRutaRepository.save(hoja);
    }

    @Transactional
    public HojaRuta finalizarRuta(Long hojaRutaId, java.math.BigDecimal kmFin, String incidencias) {
        HojaRuta hoja = hojaRutaRepository.findById(hojaRutaId)
                .orElseThrow(() -> new IllegalArgumentException("Hoja de ruta no encontrada: " + hojaRutaId));
        hoja.setEstado("FINALIZADA");
        hoja.setHoraLlegada(LocalDateTime.now());
        hoja.setKmFin(kmFin);
        hoja.setIncidencias(incidencias);
        return hojaRutaRepository.save(hoja);
    }

    @Transactional
    public void vincularAlbaranes(Long hojaRutaId, List<Long> albaranIds) {
        HojaRuta hoja = hojaRutaRepository.findById(hojaRutaId)
                .orElseThrow(() -> new IllegalArgumentException("Hoja de ruta no encontrada: " + hojaRutaId));
        int ordenBase = (int) entregaRepository.countByHojaRutaId(hojaRutaId);
        for (Long albaranId : albaranIds) {
            AlbaranVenta albaran = albaranService.obtenerPorId(albaranId)
                    .orElseThrow(() -> new IllegalArgumentException("Albarán no encontrado: " + albaranId));
            HojaRutaEntrega entrega = new HojaRutaEntrega();
            entrega.setHojaRuta(hoja);
            entrega.setAlbaran(albaran);
            entrega.setCliente(albaran.getCliente());
            entrega.setOrden(++ordenBase);
            entrega.setEntregado(false);
            entrega.setEstadoEntrega("PENDIENTE");
            entregaRepository.save(entrega);
        }
    }
}
