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
        log.info("Intento de login: {}", redactUsername(username));

        // Buscar usuario
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);

        if (usuarioOpt.isEmpty()) {
            log.warn("Usuario no encontrado: {}", redactUsername(username));
            auditoriaService.registrarError(null, "Usuario", username,
                    "Intento de login - usuario no encontrado");
            return null;
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si está activo (enabled)
        if (!Boolean.TRUE.equals(usuario.getEnabled())) {
            log.warn("Usuario deshabilitado: {}", redactUsername(username));
            auditoriaService.registrarError(usuario, "Usuario", usuario.getId().toString(),
                    "Intento de login - usuario deshabilitado");
            return null;
        }

        // Verificar si está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            log.warn("Usuario bloqueado: {}", redactUsername(username));
            auditoriaService.registrarError(usuario, "Usuario", usuario.getId().toString(),
                    "Intento de login - usuario bloqueado");
            return null;
        }

        // Validar credenciales
        boolean credencialesValidas = usuarioService.validarCredenciales(username, password);

        if (!credencialesValidas) {
            log.warn("Credenciales inválidas para: {}", redactUsername(username));
            // El incremento de intentosFallidos ya lo hace validarCredenciales internamente
            auditoriaService.registrarLogin(usuario, null, false);
            return null;
        }

        // Login exitoso: resetear contador de intentos fallidos y actualizar último acceso
        // El reset ya lo hace validarCredenciales internamente; actualizarUltimoLogin persiste la fecha
        usuarioService.actualizarUltimoLogin(usuario.getId());
        auditoriaService.registrarLogin(usuario, null, true);

        log.info("Login exitoso: {}", redactUsername(username));
        return usuario;
    }

    private String redactUsername(String username) {
        return username.substring(0, Math.min(2, username.length())) + "***";
    }
}


