package alicanteweb.erp.service;

import alicanteweb.erp.entities.AuditoriaAccion;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.AuditoriaAccionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio de auditorÃ­a de acciones
 * Registra todas las operaciones realizadas en el sistema para cumplimiento RGPD
 */
@Service
@Slf4j
public class AuditoriaService {

    private final AuditoriaAccionRepository auditoriaRepository;

    public AuditoriaService(AuditoriaAccionRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra una acciÃ³n genÃ©rica en el sistema
     */
    @Transactional
    public void registrarAccion(Usuario usuario, String tipoAccion, String entidadTipo,
                                String entidadId, String descripcion) {
        registrarAccion(usuario, tipoAccion, entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra una acciÃ³n completa con todos los detalles
     */
    @Transactional
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

            // IP y User Agent se pueden obtener del contexto web si existe
            // Por ahora lo dejamos null para aplicaciÃ³n de escritorio

            auditoriaRepository.save(auditoria);
            log.debug("AcciÃ³n auditada: {} - {} - {}", tipoAccion, entidadTipo, descripcion);
        } catch (Exception e) {
            log.error("Error registrando auditorÃ­a", e);
            // No propagar la excepciÃ³n para no afectar la operaciÃ³n principal
        }
    }

    /**
     * Registra un login exitoso
     */
    @Transactional
    public void registrarLogin(Usuario usuario, String ip, boolean exitoso) {
        AuditoriaAccion auditoria = new AuditoriaAccion();
        auditoria.setUsuario(usuario);
        auditoria.setUsuarioNombre(usuario.getUsername());
        auditoria.setTipoAccion("LOGIN");
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setDescripcion(exitoso ? "Login exitoso" : "Login fallido");
        auditoria.setModulo("AUTENTICACION");
        auditoria.setIp(ip);
        auditoria.setResultado(exitoso ? "EXITO" : "ERROR");

        auditoriaRepository.save(auditoria);
        log.info("Login auditado: {} - {}", usuario.getUsername(), exitoso ? "EXITOSO" : "FALLIDO");
    }

    /**
     * Registra un logout
     */
    @Transactional
    public void registrarLogout(Usuario usuario) {
        registrarAccion(usuario, "LOGOUT", null, null,
                "Usuario cerrÃ³ sesiÃ³n", "AUTENTICACION", null, null);
    }

    /**
     * Registra un acceso denegado
     */
    @Transactional
    public void registrarAccesoDenegado(Usuario usuario, String modulo, String accion) {
        AuditoriaAccion auditoria = new AuditoriaAccion();
        auditoria.setUsuario(usuario);
        auditoria.setUsuarioNombre(usuario != null ? usuario.getUsername() : "ANONIMO");
        auditoria.setTipoAccion("ACCESO_DENEGADO");
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setDescripcion("Acceso denegado a: " + modulo + " - " + accion);
        auditoria.setModulo(modulo);
        auditoria.setResultado("DENEGADO");

        auditoriaRepository.save(auditoria);
        log.warn("Acceso denegado: {} - {} - {}", usuario != null ? usuario.getUsername() : "ANONIMO", modulo, accion);
    }

    /**
     * Registra una creaciÃ³n de entidad
     */
    @Transactional
    public void registrarCreacion(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        registrarAccion(usuario, "CREAR", entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra una actualizaciÃ³n de entidad
     */
    @Transactional
    public void registrarActualizacion(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        registrarAccion(usuario, "ACTUALIZAR", entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra una actualizaciÃ³n con valores anteriores y nuevos
     */
    @Transactional
    public void registrarCambio(Usuario usuario, String entidadTipo, String entidadId,
                                Map<String, Object> valoresAnteriores, Map<String, Object> valoresNuevos) {
        registrarAccion(usuario, "ACTUALIZAR", entidadTipo, entidadId,
                "Entidad actualizada", null, valoresAnteriores, valoresNuevos);
    }

    /**
     * Registra una eliminaciÃ³n de entidad
     */
    @Transactional
    public void registrarEliminacion(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        registrarAccion(usuario, "ELIMINAR", entidadTipo, entidadId, descripcion, null, null, null);
    }

    /**
     * Registra un error
     */
    @Transactional
    public void registrarError(Usuario usuario, String entidadTipo, String entidadId, String descripcion) {
        AuditoriaAccion auditoria = new AuditoriaAccion();
        auditoria.setUsuario(usuario);
        auditoria.setUsuarioNombre(usuario != null ? usuario.getUsername() : "SISTEMA");
        auditoria.setTipoAccion("ERROR");
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setEntidadTipo(entidadTipo);
        auditoria.setEntidadId(entidadId);
        auditoria.setDescripcion(descripcion);
        auditoria.setResultado("ERROR");
        auditoria.setMensajeError(descripcion);

        auditoriaRepository.save(auditoria);
        log.error("Error auditado: {}", descripcion);
    }

    /**
     * Registra una exportaciÃ³n de datos
     */
    @Transactional
    public void registrarExportacion(Usuario usuario, String tipoExportacion, String descripcion) {
        registrarAccion(usuario, "EXPORTAR", tipoExportacion, null, descripcion, "EXPORTACION", null, null);
    }

    /**
     * Registra una impresiÃ³n
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
     * Obtiene acciones por mÃ³dulo
     */
    public List<AuditoriaAccion> obtenerPorModulo(String modulo) {
        return auditoriaRepository.findByModuloOrderByFechaDesc(modulo);
    }

    /**
     * Obtiene acciones recientes (Ãºltimas 24 horas)
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
     * Obtiene estadÃ­sticas por mÃ³dulo
     */
    public List<Object[]> obtenerEstadisticasPorModulo() {
        return auditoriaRepository.estadisticasPorModulo();
    }

    /**
     * Obtiene estadÃ­sticas por usuario
     */
    public List<Object[]> obtenerEstadisticasPorUsuario() {
        return auditoriaRepository.estadisticasPorUsuario();
    }

    /**
     * Obtiene estadÃ­sticas por acciÃ³n
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
     * Cuenta acciones por mÃ³dulo
     */
    public long contarPorModulo(String modulo) {
        return auditoriaRepository.countByModulo(modulo);
    }
}


