package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de autenticación y gestión de sesión
 */
@Service
@Slf4j
public class AutenticacionService {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    // Usuario actualmente autenticado (sesión)
    private Usuario usuarioActual;

    public AutenticacionService(UsuarioService usuarioService, AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Realiza el login de un usuario
     * @param username Nombre de usuario
     * @param password Contraseña en texto plano
     * @return Usuario si las credenciales son válidas, null si no
     */
    public Usuario login(String username, String password) {
        log.info("Intento de login: {}", username);

        // Buscar usuario
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);

        if (usuarioOpt.isEmpty()) {
            log.warn("Usuario no encontrado: {}", username);
            auditoriaService.registrarError(null, "Usuario", username,
                    "Intento de login - usuario no encontrado");
            return null;
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si está activo (enabled)
        if (!Boolean.TRUE.equals(usuario.getEnabled())) {
            log.warn("Usuario deshabilitado: {}", username);
            auditoriaService.registrarError(usuario, "Usuario", usuario.getId().toString(),
                    "Intento de login - usuario deshabilitado");
            return null;
        }

        // Verificar si está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            log.warn("Usuario bloqueado: {}", username);
            auditoriaService.registrarError(usuario, "Usuario", usuario.getId().toString(),
                    "Intento de login - usuario bloqueado");
            return null;
        }

        // Validar credenciales
        boolean credencialesValidas = usuarioService.validarCredenciales(username, password);

        if (!credencialesValidas) {
            log.warn("Credenciales inválidas para: {}", username);
            auditoriaService.registrarLogin(usuario, null, false);
            return null;
        }

        // Login exitoso
        usuarioActual = usuario;
        usuarioService.actualizarUltimoLogin(usuario.getId());
        auditoriaService.registrarLogin(usuario, null, true);

        log.info("Login exitoso: {}", username);
        return usuario;
    }

    /**
     * Realiza el logout del usuario actual
     */
    public void logout() {
        if (usuarioActual != null) {
            log.info("Logout: {}", usuarioActual.getUsername());
            auditoriaService.registrarLogout(usuarioActual);
            usuarioActual = null;
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario actual o null si no hay sesión
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay sesión activa
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Verifica si el usuario actual tiene un permiso específico
     * @param modulo Módulo a verificar (ej: "clientes", "facturas")
     * @param accion Acción a verificar (ej: "ver", "crear", "editar", "eliminar")
     * @return true si tiene el permiso
     */
    public boolean tienePermiso(String modulo, String accion) {
        if (usuarioActual == null) {
            return false;
        }

        // Si es ROLE_ADMIN, tiene todos los permisos
        if ("ROLE_ADMIN".equals(usuarioActual.getRole())) {
            return true;
        }

        // Por ahora, si no es admin, no tiene permisos
        // TODO: implementar sistema de roles más complejo
        return false;
    }

    /**
     * Verifica si el usuario actual es administrador
     * @return true si es administrador
     */
    public boolean esAdministrador() {
        if (usuarioActual == null) {
            return false;
        }
        return "ROLE_ADMIN".equals(usuarioActual.getRole());
    }

    /**
     * Obtiene el nombre del usuario actual
     * @return Nombre del usuario o "Invitado" si no hay sesión
     */
    public String getNombreUsuarioActual() {
        if (usuarioActual == null) {
            return "Invitado";
        }
        return usuarioActual.getNombre() != null ?
                usuarioActual.getNombre() : usuarioActual.getUsername();
    }

    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o null si no hay sesión
     */
    public Long getIdUsuarioActual() {
        return usuarioActual != null ? usuarioActual.getId() : null;
    }

    /**
     * Verifica que haya sesión activa, lanza excepción si no
     */
    public void verificarSesion() {
        if (!haySesionActiva()) {
            throw new IllegalStateException("No hay sesión activa. Por favor, inicie sesión.");
        }
    }

    /**
     * Verifica que el usuario tenga un permiso, lanza excepción si no
     */
    public void verificarPermiso(String modulo, String accion) {
        verificarSesion();
        if (!tienePermiso(modulo, accion)) {
            throw new SecurityException("No tiene permisos para realizar esta acción: " +
                    modulo + " - " + accion);
        }
    }

    /**
     * Establece manualmente el usuario actual (útil para testing)
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    // ============================================
    // Métodos alias para compatibilidad con tests
    // ============================================

    /**
     * Autenticar usuario (alias de login)
     */
    public Usuario autenticar(String username, String password) {
        return login(username, password);
    }
}


