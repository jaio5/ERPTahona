package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de usuarios
 */
@Service
@Slf4j
public class UsuarioService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int TOKEN_LENGTH = 32;

    private final UsuarioRepository usuarioRepository;
    private final CifradoService cifradoService;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                         CifradoService cifradoService,
                         AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.cifradoService = cifradoService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Crear un nuevo usuario
     */
    @Transactional
    public Usuario crearUsuario(Usuario usuario, String passwordPlain) {
        log.info("Creando usuario: {}", usuario.getUsername());

        // Validar que no exista el username
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Validar que no exista el email
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Cifrar la contraseña
        usuario.setPassword(cifradoService.hashPassword(passwordPlain));
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaCambioPassword(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuario.setActivo(true);
        usuario.setBloqueado(false);

        Usuario guardado = usuarioRepository.save(usuario);

        // Auditar
        auditoriaService.registrarCreacion(null, "Usuario", guardado.getId().toString(),
                "Usuario creado: " + guardado.getUsername());

        log.info("Usuario creado exitosamente: {} (ID: {})", guardado.getUsername(), guardado.getId());
        return guardado;
    }

    /**
     * Actualizar un usuario existente
     */
    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
        log.info("Actualizando usuario: {}", usuario.getUsername());

        Usuario existente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar username único (si cambió)
        if (!existente.getUsername().equals(usuario.getUsername())) {
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
        }

        // Validar email único (si cambió)
        if (!existente.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("El email ya está registrado");
            }
        }

        // No permitir cambio de contraseña aquí (usar cambiarPassword)
        usuario.setPassword(existente.getPassword());
        usuario.setFechaCreacion(existente.getFechaCreacion());

        Usuario actualizado = usuarioRepository.save(usuario);

        // Auditar
        auditoriaService.registrarActualizacion(null, "Usuario", actualizado.getId().toString(),
                "Usuario actualizado: " + actualizado.getUsername());

        log.info("Usuario actualizado exitosamente: {}", actualizado.getUsername());
        return actualizado;
    }

    /**
     * Cambiar contraseña de un usuario
     */
    @Transactional
    public void cambiarPassword(Long usuarioId, String oldPassword, String newPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar contraseña anterior
        if (!cifradoService.verificarPassword(oldPassword, usuario.getPassword())) {
            auditoriaService.registrarError(usuario, "Usuario", usuarioId.toString(),
                    "Intento de cambio de contraseña fallido (contraseña incorrecta)");
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        usuario.setPassword(cifradoService.hashPassword(newPassword));
        usuario.setFechaCambioPassword(LocalDateTime.now());
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);

        // Auditar
        auditoriaService.registrarAccion(usuario, "CAMBIO_PASSWORD", "Usuario", usuarioId.toString(),
                "Contraseña cambiada exitosamente");

        log.info("Contraseña cambiada para usuario: {}", usuario.getUsername());
    }

    /**
     * Validar credenciales de usuario
     */
    public boolean validarCredenciales(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            log.warn("Intento de login en usuario bloqueado: {}", username);
            return false;
        }

        // Verificar contraseña
        boolean valido = cifradoService.verificarPassword(password, usuario.getPassword());

        if (valido) {
            // Reset intentos fallidos
            resetearIntentosFallidos(usuario.getId());
        } else {
            // Incrementar intentos fallidos
            incrementarIntentosFallidos(usuario.getId());
        }

        return valido;
    }

    /**
     * Buscar usuario por username
     */
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Buscar usuario por email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Listar todos los usuarios
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Listar usuarios activos
     */
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Buscar usuarios por texto (nombre, username, email)
     */
    public List<Usuario> buscar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return listarTodos();
        }
        return usuarioRepository.buscar(texto.trim());
    }

    /**
     * Bloquear un usuario
     */
    @Transactional
    public void bloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setBloqueado(true);
        usuarioRepository.save(usuario);

        auditoriaService.registrarAccion(null, "BLOQUEO_USUARIO", "Usuario", usuarioId.toString(),
                "Usuario bloqueado: " + usuario.getUsername());

        log.warn("Usuario bloqueado: {}", usuario.getUsername());
    }

    /**
     * Desbloquear un usuario
     */
    @Transactional
    public void desbloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        auditoriaService.registrarAccion(null, "DESBLOQUEO_USUARIO", "Usuario", usuarioId.toString(),
                "Usuario desbloqueado: " + usuario.getUsername());

        log.info("Usuario desbloqueado: {}", usuario.getUsername());
    }

    /**
     * Incrementar intentos fallidos de login
     */
    @Transactional
    public void incrementarIntentosFallidos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        int intentos = (usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0) + 1;
        usuario.setIntentosFallidos(intentos);

        // Bloquear si supera el máximo
        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            usuario.setBloqueado(true);
            log.warn("Usuario bloqueado por {} intentos fallidos: {}", intentos, usuario.getUsername());
            auditoriaService.registrarAccion(usuario, "BLOQUEO_AUTOMATICO", "Usuario", usuarioId.toString(),
                    "Usuario bloqueado automáticamente por " + intentos + " intentos fallidos");
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Resetear intentos fallidos
     */
    @Transactional
    public void resetearIntentosFallidos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
    }

    /**
     * Generar token de recuperación de contraseña
     */
    @Transactional
    public String generarTokenRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email no encontrado"));

        String token = cifradoService.generarTokenSeguro(TOKEN_LENGTH);
        usuario.setTokenRecuperacion(token);
        usuario.setFechaExpiracionToken(LocalDateTime.now().plusHours(24)); // Token válido 24 horas
        usuarioRepository.save(usuario);

        auditoriaService.registrarAccion(usuario, "TOKEN_RECUPERACION", "Usuario", usuario.getId().toString(),
                "Token de recuperación generado");

        log.info("Token de recuperación generado para: {}", email);
        return token;
    }

    /**
     * Recuperar contraseña usando token
     */
    @Transactional
    public void recuperarPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacionValido(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

        // Actualizar contraseña
        usuario.setPassword(cifradoService.hashPassword(newPassword));
        usuario.setFechaCambioPassword(LocalDateTime.now());
        usuario.setTokenRecuperacion(null);
        usuario.setFechaExpiracionToken(null);
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);

        auditoriaService.registrarAccion(usuario, "RECUPERACION_PASSWORD", "Usuario", usuario.getId().toString(),
                "Contraseña recuperada mediante token");

        log.info("Contraseña recuperada para usuario: {}", usuario.getUsername());
    }

    /**
     * Actualizar último login
     */
    @Transactional
    public void actualizarUltimoLogin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    /**
     * Contar usuarios activos
     */
    public long contarActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    /**
     * Eliminar usuario (desactivar)
     */
    @Transactional
    public void eliminarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        auditoriaService.registrarEliminacion(null, "Usuario", usuarioId.toString(),
                "Usuario desactivado: " + usuario.getUsername());

        log.info("Usuario desactivado: {}", usuario.getUsername());
    }
}


