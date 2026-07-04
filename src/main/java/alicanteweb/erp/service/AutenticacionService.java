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
    private final CifradoService cifradoService;

    /**
     * Hash de sacrificio para igualar el tiempo de respuesta cuando el usuario no
     * existe o no puede autenticarse: sin él, la ausencia del cómputo PBKDF2
     * (~cientos de ms) delata por timing qué usuarios existen.
     */
    private volatile String hashSacrificio;

    public AutenticacionService(UsuarioService usuarioService, AuditoriaService auditoriaService,
                                CifradoService cifradoService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
        this.cifradoService = cifradoService;
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
            igualarTiempoDeRespuesta(password);
            // Redactado también en auditoría: un usuario inexistente puede ser una
            // contraseña tecleada por error en el campo de usuario, y no debe persistirse.
            auditoriaService.registrarError(null, "Usuario", redactUsername(username),
                    "Intento de login - usuario no encontrado");
            return null;
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si está activo (enabled)
        if (!Boolean.TRUE.equals(usuario.getEnabled())) {
            log.warn("Usuario deshabilitado: {}", redactUsername(username));
            igualarTiempoDeRespuesta(password);
            auditoriaService.registrarError(usuario, "Usuario", usuario.getId().toString(),
                    "Intento de login - usuario deshabilitado");
            return null;
        }

        // Verificar si está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            log.warn("Usuario bloqueado: {}", redactUsername(username));
            igualarTiempoDeRespuesta(password);
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

    /** Verifica la contraseña contra un hash de sacrificio para que las ramas que no
     *  autentican tarden lo mismo que una verificación real. El resultado se descarta. */
    private void igualarTiempoDeRespuesta(String password) {
        try {
            String hash = hashSacrificio;
            if (hash == null) {
                hash = cifradoService.hashPassword("igualador-de-tiempo-no-usar");
                hashSacrificio = hash;
            }
            cifradoService.verificarPassword(password != null ? password : "", hash);
        } catch (Exception e) {
            log.debug("No se pudo igualar el tiempo de respuesta: {}", e.getMessage());
        }
    }
}


