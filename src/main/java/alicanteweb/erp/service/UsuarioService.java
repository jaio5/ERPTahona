package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RolRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de usuarios
 */
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private static final int MAX_INTENTOS_FALLIDOS = 5;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CifradoService cifradoService;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         CifradoService cifradoService,
                         AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.cifradoService = cifradoService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Crear un nuevo usuario
     */
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    @Transactional
    public Usuario crearUsuario(Usuario usuario, String passwordPlain) {
        log.info("Creando usuario: {}", usuario.getUsername());

        validarPassword(passwordPlain);

        // Validar que no exista el username
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Validar que no exista el email
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Cifrar la contraseña
        String hashed = cifradoService.hashPassword(passwordPlain);
        log.info("Hashed password length={} for user={}", hashed != null ? hashed.length() : 0, usuario.getUsername());
        usuario.setPassword(hashed);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuario.setEnabled(true);
        usuario.setBloqueado(false);
        sincronizarRol(usuario);

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
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
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
        sincronizarRol(usuario);

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

        validarPassword(newPassword);

        // Verificar contraseña anterior
        if (!cifradoService.verificarPassword(oldPassword, usuario.getPassword())) {
            auditoriaService.registrarError(usuario, "Usuario", usuarioId.toString(),
                    "Intento de cambio de contraseña fallido (contraseña incorrecta)");
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        usuario.setPassword(cifradoService.hashPassword(newPassword));
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);

        // Auditar
        auditoriaService.registrarAccion(usuario, "CAMBIO_PASSWORD", "Usuario", usuarioId.toString(),
                "Contraseña cambiada exitosamente");

        log.info("Contraseña cambiada para usuario: {}", usuario.getUsername());
    }

    /**
     * Cambiar la contraseña de un usuario por un administrador sin necesidad de la contraseña anterior.
     */
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    @Transactional
    public void cambiarPasswordAdmin(Long usuarioId, String newPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPassword(cifradoService.hashPassword(newPassword));
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);

        auditoriaService.registrarAccion(null, "CAMBIO_PASSWORD_ADMIN", "Usuario", usuarioId.toString(),
                "Contraseña cambiada por administrador para usuario: " + usuario.getUsername());

        log.info("Contraseña cambiada por admin para usuario: {}", usuario.getUsername());
    }

    /**
     * Validar credenciales de usuario
     */
    @Transactional
    public boolean validarCredenciales(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si está activo (enabled)
        if (!Boolean.TRUE.equals(usuario.getEnabled())) {
            log.warn("Intento de login en usuario deshabilitado: {}", username);
            return false;
        }

        // Verificar si está bloqueado
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            log.warn("Intento de login en usuario bloqueado: {}", username);
            return false;
        }

        // Verificar contraseña
        boolean valido = cifradoService.verificarPassword(password, usuario.getPassword());

        if (valido) {
            // Reset intentos fallidos - ahora dentro de la misma transacción
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
        } else {
            // Incrementar intentos fallidos - dentro de la misma transacción
            int intentos = (usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0) + 1;
            usuario.setIntentosFallidos(intentos);

            // Bloquear si supera el máximo
            if (intentos >= MAX_INTENTOS_FALLIDOS) {
                usuario.setBloqueado(true);
                usuario.setFechaBloqueo(LocalDateTime.now());
                usuario.setContadorBloqueos(usuario.getContadorBloqueos() != null ? usuario.getContadorBloqueos() + 1 : 1);
                log.warn("Usuario bloqueado por {} intentos fallidos: {}", intentos, usuario.getUsername());
                auditoriaService.registrarAccion(usuario, "BLOQUEO_AUTOMATICO", "Usuario", usuario.getId().toString(),
                        "Usuario bloqueado automáticamente por " + intentos + " intentos fallidos");
            }
            
            usuarioRepository.save(usuario);
        }

        return valido;
    }

    /**
     * Buscar usuario por username
     */
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameWithRol(username);
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
        return usuarioRepository.findAllWithRol();
    }

    /**
     * Listar usuarios activos
     */
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByEnabledTrue();
    }

    public long contarActivos() {
        return usuarioRepository.countByEnabledTrue();
    }

    /**
     * Buscar usuarios por texto (nombre, username, email)
     */
    public List<Usuario> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }
        return usuarioRepository.buscar(texto.trim());
    }

    public Page<Usuario> listarPaginado(String q, Pageable pageable) {
        return usuarioRepository.findPage(q, pageable);
    }

    /**
     * Bloquear un usuario
     */
    @Transactional
    public void bloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setBloqueado(true);
        usuario.setFechaBloqueo(LocalDateTime.now());
        usuario.setContadorBloqueos(usuario.getContadorBloqueos() != null ? usuario.getContadorBloqueos() + 1 : 1);
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
            usuario.setFechaBloqueo(LocalDateTime.now());
            usuario.setContadorBloqueos(usuario.getContadorBloqueos() != null ? usuario.getContadorBloqueos() + 1 : 1);
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
     * Eliminar usuario (desactivar)
     */
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    @Transactional
    public void eliminarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEnabled(false);
        usuarioRepository.save(usuario);

        auditoriaService.registrarEliminacion(null, "Usuario", usuarioId.toString(),
                "Usuario desactivado: " + usuario.getUsername());

        log.info("Usuario desactivado: {}", usuario.getUsername());
    }

    @Transactional
    public void activarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEnabled(true);
        usuarioRepository.save(usuario);

        auditoriaService.registrarActualizacion(null, "Usuario", usuarioId.toString(),
                "Usuario activado: " + usuario.getUsername());

        log.info("Usuario activado: {}", usuario.getUsername());
    }

    /**
     * Generar token de recuperación de contraseña
     */
    @Transactional
    public Usuario generarTokenRecuperacion(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Generar token aleatorio
        String token = java.util.UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setFechaExpiracionToken(LocalDateTime.now().plusHours(24));

        usuarioRepository.save(usuario);

        log.info("Token de recuperación generado para: {}", username);
        return usuario;
    }

    /**
     * Actualiza el timestamp de último acceso del usuario.
     */
    @Transactional
    public void actualizarUltimoLogin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);
        log.info("Último acceso actualizado para usuario: {}", usuario.getUsername());
    }
    private void validarPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una mayúscula");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un dígito");
        }
    }

    private void sincronizarRol(Usuario usuario) {
        if (usuario.getRole() == null && usuario.getRol() != null) {
            usuario.setRole(usuario.getRol().getNombre());
        }
        if (usuario.getRole() != null && !usuario.getRole().isBlank()) {
            rolRepository.findByNombre(usuario.getRole().trim()).ifPresent(usuario::setRol);
        }
    }
}
