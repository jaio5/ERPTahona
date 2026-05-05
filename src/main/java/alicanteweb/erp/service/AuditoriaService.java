package alicanteweb.erp.service;

import alicanteweb.erp.entities.AuditoriaAccion;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.AuditoriaAccionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio de auditoría de acciones
 * Registra todas las operaciones realizadas en el sistema para cumplimiento RGPD
 */
@Service
public class AuditoriaService {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaAccionRepository auditoriaRepository;

    public AuditoriaService(AuditoriaAccionRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra una acción genérica en el sistema
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void registrarAccion(Usuario usuario, String tipoAccion, String entidadTipo,
                                String entidadId, String descripcion) {
        registrarAccion(usuario, tipoAccion, entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra una acción completa con todos los detalles
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void registrarAccion(Usuario usuario, String tipoAccion, String entidadTipo,
                                String entidadId, String descripcion, String modulo,
                                Map<String, Object> valoresAnteriores, Map<String, Object> valoresNuevos) {
        try {
            AuditoriaAccion auditoria = new AuditoriaAccion();
            auditoria.setUsuario(usuario);
            auditoria.setUsuarioNombre(usuario != null ? usuario.getUsername() : "SISTEMA");
            auditoria.setTipoAccion(tipoAccion);
            auditoria.setFecha(LocalDateTime.now());
            auditoria.setEntidadTipo(entidadTipo);
            auditoria.setEntidadId(entidadId);
            auditoria.setDescripcion(descripcion);
            auditoria.setModulo(modulo);
            auditoria.setValoresAnteriores(valoresAnteriores);
            auditoria.setValoresNuevos(valoresNuevos);
            auditoria.setResultado("EXITO");

            auditoriaRepository.save(auditoria);
            log.debug("Acción auditada: {} - {} - {}", tipoAccion, entidadTipo, descripcion);
        } catch (Exception e) {
            log.error("Error registrando auditoría", e);
            // No propagar la excepción para no afectar la operación principal
        }
    }

    /**
     * Registra un login exitoso o fallido
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void registrarLogin(Usuario usuario, String ip, boolean exitoso) {
        try {
            AuditoriaAccion auditoria = new AuditoriaAccion();
            auditoria.setUsuario(usuario);
            auditoria.setUsuarioNombre(usuario != null ? usuario.getUsername() : "DESCONOCIDO");
            auditoria.setTipoAccion(exitoso ? "LOGIN" : "LOGIN_FALLIDO");
            auditoria.setFecha(LocalDateTime.now());
            auditoria.setEntidadTipo("Usuario");
            auditoria.setEntidadId(usuario != null ? String.valueOf(usuario.getId()) : null);
            auditoria.setDescripcion(exitoso ? "Login exitoso" : "Login fallido");
            auditoria.setModulo("AUTENTICACION");
            auditoria.setIp(ip);
            auditoria.setResultado(exitoso ? "EXITO" : "ERROR");

            auditoriaRepository.save(auditoria);
            log.info("Login auditado: {} - {}", usuario != null ? usuario.getUsername() : "DESCONOCIDO", exitoso ? "EXITOSO" : "FALLIDO");
        } catch (Exception e) {
            log.error("Error registrando auditoría de login", e);
        }
    }

    /**
     * Registra un logout
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void registrarLogout(Usuario usuario) {
        registrarAccion(usuario, "LOGOUT", null, null,
                "Usuario cerró sesión", "AUTENTICACION", null, null);
    }

    /**
     * Registra un acceso denegado
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void registrarAccesoDenegado(Usuario usuario, String modulo, String accion) {
        try {
            AuditoriaAccion auditoria = new AuditoriaAccion();
            auditoria.setUsuario(usuario);
            auditoria.setUsuarioNombre(usuario != null ? usuario.getUsername() : "ANONIMO");
            auditoria.setTipoAccion("ACCESO_DENEGADO");
            auditoria.setFecha(LocalDateTime.now());
            auditoria.setModulo(modulo);
            auditoria.setDescripcion("Acceso denegado a: " + accion);
            auditoria.setResultado("DENEGADO");

            auditoriaRepository.save(auditoria);
            log.warn("Acceso denegado: {} - {} - {}", usuario != null ? usuario.getUsername() : "ANONIMO", modulo, accion);
        } catch (Exception e) {
            log.error("Error registrando acceso denegado", e);
        }
    }

    /**
     * Registra una creación de entidad
     */
    @Transactional
    public void registrarCreacion(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        registrarAccion(usuario, "CREAR", entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra una actualización de entidad
     */
    @Transactional
    public void registrarActualizacion(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        registrarAccion(usuario, "ACTUALIZAR", entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra una actualización con valores anteriores y nuevos
     */
    @Transactional
    public void registrarCambio(Usuario usuario, String entidadTipo, String entidadId,
                                Map<String, Object> valoresAnteriores, Map<String, Object> valoresNuevos) {
        registrarAccion(usuario, "ACTUALIZAR", entidadTipo, entidadId,
                "Entidad actualizada", null, valoresAnteriores, valoresNuevos);
    }

    /**
     * Registra una eliminación de entidad
     */
    @Transactional
    public void registrarEliminacion(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        registrarAccion(usuario, "ELIMINAR", entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra un error
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void registrarError(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        try {
            AuditoriaAccion auditoria = new AuditoriaAccion();
            auditoria.setUsuario(usuario);
            auditoria.setUsuarioNombre(usuario != null ? usuario.getUsername() : "SISTEMA");
            auditoria.setTipoAccion("ERROR");
            auditoria.setFecha(LocalDateTime.now());
            auditoria.setEntidadTipo(entidadTipo);
            auditoria.setEntidadId(entidadId);
            auditoria.setDescripcion(descripcion);
            auditoria.setResultado("ERROR");

            auditoriaRepository.save(auditoria);
            log.error("Error auditado: {}", descripcion);
        } catch (Exception e) {
            log.error("Error registrando auditoría de error", e);
        }
    }

    /**
     * Registra una exportación de datos
     */
    @Transactional
    public void registrarExportacion(Usuario usuario, String tipoExportacion, String descripcion) {
        registrarAccion(usuario, "EXPORTAR", tipoExportacion, null, descripcion, "EXPORTACION", null, null);
    }

    /**
     * Registra una impresión
     */
    @Transactional
    public void registrarImpresion(Usuario usuario, String tipoDocumento, String documentoId, String descripcion) {
        registrarAccion(usuario, "IMPRIMIR", tipoDocumento, documentoId, descripcion, "IMPRESION", null, null);
    }

    /**
     * Obtiene el historial de una entidad
     */
    public List<AuditoriaAccion> obtenerHistorial(String entidadTipo, String entidadId) {
        return auditoriaRepository.findHistorialEntidad(entidadTipo, entidadId);
    }

    /**
     * Obtiene acciones por usuario
     */
    public List<AuditoriaAccion> obtenerPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    /**
     * Obtiene acciones por tipo
     */
    public List<AuditoriaAccion> obtenerPorTipo(String tipoAccion) {
        return auditoriaRepository.findByTipoAccionOrderByFechaDesc(tipoAccion);
    }

    /**
     * Obtiene acciones por módulo
     */
    public List<AuditoriaAccion> obtenerPorModulo(String modulo) {
        return auditoriaRepository.findByModuloOrderByFechaDesc(modulo);
    }

    /**
     * Obtiene acciones recientes (últimas 24 horas)
     */
    public List<AuditoriaAccion> obtenerRecientes() {
        return auditoriaRepository.findRecientes(LocalDateTime.now().minusHours(24));
    }

    /**
     * Obtiene acciones por rango de fechas
     */
    public List<AuditoriaAccion> obtenerPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return auditoriaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    /**
     * Obtiene logins fallidos
     */
    public List<AuditoriaAccion> obtenerLoginsFallidos() {
        return auditoriaRepository.findLoginsFallidos();
    }

    /**
     * Obtiene accesos denegados
     */
    public List<AuditoriaAccion> obtenerAccesosDenegados() {
        return auditoriaRepository.findAccesosDenegados();
    }

    /**
     * Obtiene estadísticas por módulo
     */
    public List<Object[]> obtenerEstadisticasPorModulo() {
        return auditoriaRepository.estadisticasPorModulo();
    }

    /**
     * Obtiene estadísticas por usuario
     */
    public List<Object[]> obtenerEstadisticasPorUsuario() {
        return auditoriaRepository.estadisticasPorUsuario();
    }

    /**
     * Obtiene estadísticas por acción
     */
    public List<Object[]> obtenerEstadisticasPorAccion() {
        return auditoriaRepository.estadisticasPorAccion();
    }

    /**
     * Cuenta acciones por usuario
     */
    public long contarPorUsuario(Long usuarioId) {
        return auditoriaRepository.countByUsuarioId(usuarioId);
    }

    /**
     * Cuenta acciones por tipo
     */
    public long contarPorTipo(String tipoAccion) {
        return auditoriaRepository.countByTipoAccion(tipoAccion);
    }

    /**
     * Cuenta acciones por módulo
     */
    public long contarPorModulo(String modulo) {
        return auditoriaRepository.countByModulo(modulo);
    }
}


