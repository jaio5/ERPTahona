package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.RgpdSolicitud;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.AlbaranVentaRepository;
import alicanteweb.erp.repository.ClienteRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.PresupuestoRepository;
import alicanteweb.erp.repository.RgpdAccesoDatosRepository;
import alicanteweb.erp.repository.RgpdConsentimientoRepository;
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
 * Servicio de gestión de solicitudes RGPD (derechos ARCO)
 * ARCO: Acceso, Rectificación, Cancelación (Supresión), Oposición
 * También: Portabilidad y Limitación del tratamiento
 */
@Service
@Slf4j
public class RgpdSolicitudService {

    private final RgpdSolicitudRepository solicitudRepository;
    private final AuditoriaService auditoriaService;
    private final ClienteRepository clienteRepository;
    private final FacturaRepository facturaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final PedidoRepository pedidoRepository;
    private final AlbaranVentaRepository albaranVentaRepository;
    private final RgpdConsentimientoRepository consentimientoRepository;
    private final RgpdAccesoDatosRepository accesoDatosRepository;

    public RgpdSolicitudService(RgpdSolicitudRepository solicitudRepository,
                               AuditoriaService auditoriaService,
                               ClienteRepository clienteRepository,
                               FacturaRepository facturaRepository,
                               PresupuestoRepository presupuestoRepository,
                               PedidoRepository pedidoRepository,
                               AlbaranVentaRepository albaranVentaRepository,
                               RgpdConsentimientoRepository consentimientoRepository,
                               RgpdAccesoDatosRepository accesoDatosRepository) {
        this.solicitudRepository = solicitudRepository;
        this.auditoriaService = auditoriaService;
        this.clienteRepository = clienteRepository;
        this.facturaRepository = facturaRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.pedidoRepository = pedidoRepository;
        this.albaranVentaRepository = albaranVentaRepository;
        this.consentimientoRepository = consentimientoRepository;
        this.accesoDatosRepository = accesoDatosRepository;
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
        // RGPD: máximo 1 mes (30 días) para responder
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
     * Añadir notas internas
     */
    @Transactional
    public void anadirNotas(Long solicitudId, Usuario usuario, String notas) {
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
        log.info("Notas ñadidas a solicitud {}", solicitudId);
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
     * Obtener solicitudes próximas a vencer (dentro de N días)
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
     * Obtener estadísticas por tipo de derecho
     */
    public List<Object[]> obtenerEstadisticasPorTipoDerecho() {
        return solicitudRepository.estadisticasPorTipoDerecho();
    }

    /**
     * Obtener estadísticas por estado
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

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        Map<String, Object> datos = new HashMap<>();
        datos.put("cliente_id", clienteId);
        datos.put("fecha_exportacion", LocalDateTime.now());
        datos.put("cliente", mapCliente(cliente));

        datos.put("facturas", facturaRepository.findByCliente_Id(clienteId).stream()
                .map(f -> Map.of(
                        "id", f.getId(),
                        "numero", valor(f.getNumero()),
                        "serie", valor(f.getSerie()),
                        "fecha", valor(f.getFecha()),
                        "estado", valor(f.getEstado()),
                        "base_imponible", valor(f.getBaseImponible()),
                        "iva", valor(f.getTotalIva()),
                        "total", valor(f.getTotal()),
                        "pagada", f.isPagada()
                ))
                .toList());
        datos.put("presupuestos", presupuestoRepository.findByClienteId(clienteId).stream()
                .map(p -> Map.of(
                        "id", p.getId(),
                        "numero", valor(p.getNumero()),
                        "fecha", valor(p.getFecha()),
                        "fecha_validez", valor(p.getFechaValidez()),
                        "estado", valor(p.getEstado()),
                        "total", valor(p.getTotal()),
                        "observaciones", valor(p.getObservaciones())
                ))
                .toList());
        datos.put("pedidos", pedidoRepository.findByClienteId(clienteId).stream()
                .map(p -> Map.of(
                        "id", p.getId(),
                        "numero", valor(p.getNumero()),
                        "fecha", valor(p.getFecha()),
                        "estado", valor(p.getEstado()),
                        "total", valor(p.getTotal()),
                        "observaciones", valor(p.getObservaciones())
                ))
                .toList());
        datos.put("albaranes", albaranVentaRepository.findByClienteId(clienteId).stream()
                .map(a -> Map.of(
                        "id", a.getId(),
                        "numero", valor(a.getNumero()),
                        "fecha", valor(a.getFecha()),
                        "total", valor(a.getTotal()),
                        "observaciones", valor(a.getObservaciones())
                ))
                .toList());
        datos.put("consentimientos", consentimientoRepository.findByClienteId(clienteId).stream()
                .map(c -> Map.of(
                        "id", c.getId(),
                        "tipo", valor(c.getTipoConsentimiento()),
                        "otorgado", valor(c.getOtorgado()),
                        "activo", valor(c.getActivo()),
                        "fecha_consentimiento", valor(c.getFechaConsentimiento()),
                        "fecha_revocacion", valor(c.getFechaRevocacion()),
                        "canal", valor(c.getCanal()),
                        "version_politica", valor(c.getVersionPolitica())
                ))
                .toList());
        datos.put("accesos_datos", accesoDatosRepository.findByClienteIdOrderByFechaAccesoDesc(clienteId).stream()
                .map(a -> Map.of(
                        "id", a.getId(),
                        "fecha", valor(a.getFechaAcceso()),
                        "tipo", valor(a.getTipoAcceso()),
                        "modulo", valor(a.getModulo()),
                        "motivo", valor(a.getMotivo())
                ))
                .toList());
        datos.put("solicitudes_rgpd", solicitudRepository.findByClienteIdOrderByFechaSolicitudDesc(clienteId).stream()
                .map(s -> Map.of(
                        "id", s.getId(),
                        "tipo_derecho", valor(s.getTipoDerecho()),
                        "estado", valor(s.getEstado()),
                        "fecha_solicitud", valor(s.getFechaSolicitud()),
                        "fecha_respuesta", valor(s.getFechaRespuesta())
                ))
                .toList());

        return datos;
    }

    /**
     * Anonimizar datos de un cliente (derecho al olvido)
     */
    @Transactional
    public void anonimizarDatosCliente(Long clienteId, Usuario usuario) {
        log.warn("Anonimizando datos de cliente {} (derecho al olvido)", clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Auditar antes de anonimizar
        auditoriaService.registrarAccion(usuario, "ANONIMIZACION_CLIENTE", "Cliente",
                clienteId.toString(), "Datos anonimizados por derecho al olvido");

        cliente.setNombre("Cliente anonimizado " + clienteId);
        cliente.setCif("ANON-" + clienteId);
        cliente.setTelefono(null);
        cliente.setEmail("anonimizado+" + clienteId + "@local.invalid");
        cliente.setDireccion(null);
        cliente.setPoblacion(null);
        cliente.setCodigoPostal(null);
        cliente.setProvincia(null);
        cliente.setNotas("Datos personales anonimizados por solicitud RGPD el " + LocalDateTime.now());
        cliente.setActivo(false);
        clienteRepository.save(cliente);

        consentimientoRepository.findByClienteId(clienteId).forEach(consentimiento -> {
            consentimiento.setOtorgado(false);
            consentimiento.setActivo(false);
            consentimiento.setFechaRevocacion(LocalDateTime.now());
            consentimiento.setEmail(cliente.getEmail());
            consentimiento.setNombre(cliente.getNombre());
            consentimientoRepository.save(consentimiento);
        });

        solicitudRepository.findByClienteIdOrderByFechaSolicitudDesc(clienteId).forEach(solicitud -> {
            solicitud.setEmailSolicitante(cliente.getEmail());
            solicitud.setNombreSolicitante(cliente.getNombre());
            solicitudRepository.save(solicitud);
        });

        log.info("Cliente {} anonimizado manteniendo los documentos con obligaciones legales", clienteId);
    }

    private Map<String, Object> mapCliente(Cliente cliente) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("id", cliente.getId());
        datos.put("codigo", cliente.getCodigo());
        datos.put("nombre", cliente.getNombre());
        datos.put("cif", cliente.getCif());
        datos.put("telefono", cliente.getTelefono());
        datos.put("email", cliente.getEmail());
        datos.put("direccion", cliente.getDireccion());
        datos.put("poblacion", cliente.getPoblacion());
        datos.put("codigo_postal", cliente.getCodigoPostal());
        datos.put("provincia", cliente.getProvincia());
        datos.put("notas", cliente.getNotas());
        datos.put("activo", cliente.getActivo());
        return datos;
    }

    private Object valor(Object value) {
        return value != null ? value : "";
    }
}


