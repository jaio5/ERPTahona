package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.RgpdSolicitud;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RgpdSolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestiÃ³n de solicitudes RGPD (derechos ARCO)
 * ARCO: Acceso, RectificaciÃ³n, CancelaciÃ³n (SupresiÃ³n), OposiciÃ³n
 * TambiÃ©n: Portabilidad y LimitaciÃ³n del tratamiento
 */
@Service
@Slf4j
public class RgpdSolicitudService {

    private final RgpdSolicitudRepository solicitudRepository;
    private final AuditoriaService auditoriaService;

    public RgpdSolicitudService(RgpdSolicitudRepository solicitudRepository,
                               AuditoriaService auditoriaService) {
        this.solicitudRepository = solicitudRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Crear una nueva solicitud RGPD
     */
    @Transactional
    public RgpdSolicitud crearSolicitud(Cliente cliente, String emailSolicitante,
                                       String nombreSolicitante, String tipoDerecho,
                                       String descripcion, String canal, String ip) {
        log.info("Creando solicitud RGPD tipo {} para {}", tipoDerecho, emailSolicitante);

        RgpdSolicitud solicitud = new RgpdSolicitud();
        solicitud.setCliente(cliente);
        solicitud.setEmailSolicitante(emailSolicitante);
        solicitud.setNombreSolicitante(nombreSolicitante);
        solicitud.setTipoDerecho(tipoDerecho);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());
        // RGPD: mÃ¡ximo 1 mes (30 dÃ­as) para responder
        solicitud.setFechaLimiteRespuesta(LocalDateTime.now().plusDays(30));
        solicitud.setDescripcion(descripcion);
        solicitud.setCanal(canal);
        solicitud.setIpOrigen(ip);
        solicitud.setIdentidadVerificada(false);

        RgpdSolicitud guardada = solicitudRepository.save(solicitud);

        // Auditar
        auditoriaService.registrarCreacion(null, "RgpdSolicitud", guardada.getId().toString(),
                "Solicitud RGPD creada: " + tipoDerecho + " - " + emailSolicitante);

        log.info("Solicitud RGPD creada: ID {}", guardada.getId());
        return guardada;
    }

    /**
     * Asignar responsable a una solicitud
     */
    @Transactional
    public void asignarResponsable(Long solicitudId, Usuario responsable) {
        RgpdSolicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        solicitud.setUsuarioResponsable(responsable);
        if ("PENDIENTE".equals(solicitud.getEstado())) {
            solicitud.setEstado("EN_PROCESO");
        }
        solicitudRepository.save(solicitud);

        auditoriaService.registrarActualizacion(responsable, "RgpdSolicitud", solicitudId.toString(),
                "Responsable asignado: " + responsable.getUsername());

        log.info("Responsable {} asignado a solicitud {}", responsable.getUsername(), solicitudId);
    }

    /**
     * Verificar identidad del solicitante
     */
    @Transactional
    public void verificarIdentidad(Long solicitudId, Usuario usuario, boolean verificada) {
        RgpdSolicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        solicitud.setIdentidadVerificada(verificada);
        solicitudRepository.save(solicitud);

        auditoriaService.registrarActualizacion(usuario, "RgpdSolicitud", solicitudId.toString(),
                "Identidad " + (verificada ? "verificada" : "rechazada"));

        log.info("Identidad {} para solicitud {}", verificada ? "verificada" : "rechazada", solicitudId);
    }

    /**
     * Completar una solicitud con respuesta
     */
    @Transactional
    public void completarSolicitud(Long solicitudId, Usuario usuario, String respuesta,
                                  String rutaArchivo) {
        RgpdSolicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        solicitud.setEstado("COMPLETADA");
        solicitud.setRespuesta(respuesta);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setRutaArchivoRespuesta(rutaArchivo);
        solicitudRepository.save(solicitud);

        auditoriaService.registrarActualizacion(usuario, "RgpdSolicitud", solicitudId.toString(),
                "Solicitud completada: " + solicitud.getTipoDerecho());

        log.info("Solicitud {} completada", solicitudId);
    }

    /**
     * Rechazar una solicitud
     */
    @Transactional
    public void rechazarSolicitud(Long solicitudId, Usuario usuario, String motivo) {
        RgpdSolicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        solicitud.setEstado("RECHAZADA");
        solicitud.setRespuesta(motivo);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrarActualizacion(usuario, "RgpdSolicitud", solicitudId.toString(),
                "Solicitud rechazada: " + motivo);

        log.info("Solicitud {} rechazada", solicitudId);
    }

    /**
     * AÃ±adir notas internas
     */
    @Transactional
    public void aÃ±adirNotas(Long solicitudId, Usuario usuario, String notas) {
        RgpdSolicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        String notasActuales = solicitud.getNotasInternas();
        String nuevasNotas = LocalDateTime.now() + " - " + usuario.getUsername() + ": " + notas;

        if (notasActuales != null && !notasActuales.isEmpty()) {
            solicitud.setNotasInternas(notasActuales + "\n" + nuevasNotas);
        } else {
            solicitud.setNotasInternas(nuevasNotas);
        }

        solicitudRepository.save(solicitud);
        log.info("Notas aÃ±adidas a solicitud {}", solicitudId);
    }

    /**
     * Obtener solicitudes de un cliente
     */
    public List<RgpdSolicitud> obtenerSolicitudesCliente(Long clienteId) {
        return solicitudRepository.findByClienteIdOrderByFechaSolicitudDesc(clienteId);
    }

    /**
     * Obtener solicitudes por email
     */
    public List<RgpdSolicitud> obtenerPorEmail(String email) {
        return solicitudRepository.findByEmailSolicitanteOrderByFechaSolicitudDesc(email);
    }

    /**
     * Obtener solicitudes por estado
     */
    public List<RgpdSolicitud> obtenerPorEstado(String estado) {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudDesc(estado);
    }

    /**
     * Obtener solicitudes pendientes
     */
    public List<RgpdSolicitud> obtenerPendientes() {
        return solicitudRepository.findByEstadoInOrderByFechaSolicitudAsc(
                Arrays.asList("PENDIENTE", "EN_PROCESO"));
    }

    /**
     * Obtener solicitudes por tipo de derecho
     */
    public List<RgpdSolicitud> obtenerPorTipoDerecho(String tipoDerecho) {
        return solicitudRepository.findByTipoDerechoOrderByFechaSolicitudDesc(tipoDerecho);
    }

    /**
     * Obtener solicitudes de un responsable
     */
    public List<RgpdSolicitud> obtenerPorResponsable(Long usuarioId) {
        return solicitudRepository.findByUsuarioResponsableIdOrderByFechaSolicitudDesc(usuarioId);
    }

    /**
     * Obtener solicitudes prÃ³ximas a vencer (dentro de N dÃ­as)
     */
    public List<RgpdSolicitud> obtenerProximasAVencer(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(dias);
        return solicitudRepository.findProximasAVencer(fechaLimite);
    }

    /**
     * Obtener solicitudes vencidas
     */
    public List<RgpdSolicitud> obtenerVencidas() {
        return solicitudRepository.findVencidas();
    }

    /**
     * Obtener solicitudes por rango de fechas
     */
    public List<RgpdSolicitud> obtenerPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return solicitudRepository.findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(inicio, fin);
    }

    /**
     * Contar solicitudes por estado
     */
    public long contarPorEstado(String estado) {
        return solicitudRepository.countByEstado(estado);
    }

    /**
     * Contar solicitudes pendientes
     */
    public long contarPendientes() {
        return solicitudRepository.countPendientes();
    }

    /**
     * Contar solicitudes vencidas
     */
    public long contarVencidas() {
        return solicitudRepository.countVencidas();
    }

    /**
     * Obtener estadÃ­sticas por tipo de derecho
     */
    public List<Object[]> obtenerEstadisticasPorTipoDerecho() {
        return solicitudRepository.estadisticasPorTipoDerecho();
    }

    /**
     * Obtener estadÃ­sticas por estado
     */
    public List<Object[]> obtenerEstadisticasPorEstado() {
        return solicitudRepository.estadisticasPorEstado();
    }

    /**
     * Obtener tiempo promedio de respuesta en horas
     */
    public Double obtenerTiempoPromedioRespuesta() {
        return solicitudRepository.tiempoPromedioRespuestaHoras();
    }

    /**
     * Generar informe de solicitudes RGPD
     */
    public Map<String, Object> generarInforme() {
        Map<String, Object> informe = new HashMap<>();

        informe.put("fecha_generacion", LocalDateTime.now());
        informe.put("total_pendientes", contarPendientes());
        informe.put("total_vencidas", contarVencidas());
        informe.put("total_completadas", contarPorEstado("COMPLETADA"));
        informe.put("total_rechazadas", contarPorEstado("RECHAZADA"));
        informe.put("tiempo_promedio_respuesta_horas", obtenerTiempoPromedioRespuesta());
        informe.put("estadisticas_por_tipo", obtenerEstadisticasPorTipoDerecho());
        informe.put("estadisticas_por_estado", obtenerEstadisticasPorEstado());
        informe.put("proximas_vencer_7_dias", obtenerProximasAVencer(7).size());

        return informe;
    }

    /**
     * Exportar datos de un cliente (derecho de portabilidad)
     */
    public Map<String, Object> exportarDatosCliente(Long clienteId) {
        log.info("Exportando datos de cliente {} (derecho de portabilidad)", clienteId);

        Map<String, Object> datos = new HashMap<>();
        datos.put("cliente_id", clienteId);
        datos.put("fecha_exportacion", LocalDateTime.now());

        // AquÃ­ se agregarÃ­an todos los datos del cliente
        // Por ahora solo un placeholder
        datos.put("nota", "Implementar exportaciÃ³n completa de datos del cliente");

        return datos;
    }

    /**
     * Anonimizar datos de un cliente (derecho al olvido)
     */
    @Transactional
    public void anonimizarDatosCliente(Long clienteId, Usuario usuario) {
        log.warn("Anonimizando datos de cliente {} (derecho al olvido)", clienteId);

        // Auditar antes de anonimizar
        auditoriaService.registrarAccion(usuario, "ANONIMIZACION_CLIENTE", "Cliente",
                clienteId.toString(), "Datos anonimizados por derecho al olvido");

        // AquÃ­ se implementarÃ­a la lÃ³gica de anonimizaciÃ³n
        // Por ahora solo un placeholder
        log.warn("IMPLEMENTAR: AnonimizaciÃ³n completa de datos del cliente");
    }
}


