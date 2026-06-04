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

    private final ThreadLocal<Usuario> usuarioActual = new ThreadLocal<>();

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
        usuarioActual.set(usuario);
        usuarioService.actualizarUltimoLogin(usuario.getId());
        auditoriaService.registrarLogin(usuario, null, true);

        log.info("Login exitoso: {}", username);
        return usuario;
    }

    /**
     * Realiza el logout del usuario actual
     */
    public void logout() {
        Usuario u = usuarioActual.get();
        if (u != null) {
            log.info("Logout: {}", u.getUsername());
            auditoriaService.registrarLogout(u);
            usuarioActual.remove();
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario actual o null si no hay sesión
     */
    public Usuario getUsuarioActual() {
        return usuarioActual.get();
    }

    public boolean haySesionActiva() {
        return usuarioActual.get() != null;
    }

    public boolean tienePermiso(String modulo, String accion) {
        Usuario u = usuarioActual.get();
        if (u == null) {
            return false;
        }
        if (esAdministrador()) {
            return true;
        }

        alicanteweb.erp.entities.Rol rol = u.getRol();
        if (rol != null && rol.getPermisos() != null) {
            java.util.Map<String, Boolean> permisosModulo = rol.getPermisos().get(modulo);
            if (permisosModulo != null) {
                return Boolean.TRUE.equals(permisosModulo.get(accion));
            }
        }

        return tienePermisoLegacy(modulo, accion);
    }

    /**
     * Verifica si el usuario actual es administrador
     * @return true si es administrador
     */
    public boolean esAdministrador() {
        Usuario u = usuarioActual.get();
        if (u == null) {
            return false;
        }
        String role = u.getRole();
        String rolNombre = u.getRol() != null ? u.getRol().getNombre() : null;
        return esAdminRole(role) || esAdminRole(rolNombre);
    }

    /**
     * Obtiene el nombre del usuario actual
     * @return Nombre del usuario o "Invitado" si no hay sesión
     */
    public String getNombreUsuarioActual() {
        Usuario u = usuarioActual.get();
        if (u == null) {
            return "Invitado";
        }
        return u.getNombre() != null ?
                u.getNombre() : u.getUsername();
    }

    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o null si no hay sesión
     */
    public Long getIdUsuarioActual() {
        Usuario u = usuarioActual.get();
        return u != null ? u.getId() : null;
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
        if (usuario != null) {
            this.usuarioActual.set(usuario);
        } else {
            this.usuarioActual.remove();
        }
    }

    private boolean tienePermisoLegacy(String modulo, String accion) {
        Usuario u = usuarioActual.get();
        if (u == null) return false;
        String role = u.getRole();
        if (role == null) {
            return false;
        }
        return switch (role.toUpperCase()) {
            case "MANAGER", "GERENTE", "GESTOR" -> !"usuarios".equals(modulo) || !"eliminar".equals(accion);
            case "VENDEDOR", "USER", "USUARIO" -> switch (modulo) {
                case "dashboard", "clientes", "articulos", "ventas", "almacen" -> "ver".equals(accion);
                default -> false;
            };
            case "CONTABLE" -> switch (modulo) {
                case "dashboard", "compras", "tesoreria", "contabilidad", "fiscal" -> true;
                default -> false;
            };
            default -> false;
        };
    }

    private boolean esAdminRole(String role) {
        return role != null && ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role));
    }

}


