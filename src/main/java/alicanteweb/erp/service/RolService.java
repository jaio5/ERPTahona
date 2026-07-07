package alicanteweb.erp.service;

import alicanteweb.erp.entities.Rol;
import alicanteweb.erp.repository.RolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de gestión de roles
 */
@Service
public class RolService {

    private static final Logger log = LoggerFactory.getLogger(RolService.class);

    private final RolRepository rolRepository;
    private final AuditoriaService auditoriaService;

    public RolService(RolRepository rolRepository, AuditoriaService auditoriaService) {
        this.rolRepository = rolRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Crear un nuevo rol
     */
    @Transactional
    public Rol crearRol(Rol rol) {
        log.info("Creando rol: {}", rol.getNombre());

        // Validar que no exista el nombre
        if (rolRepository.existsByNombre(rol.getNombre())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }

        rol.setActivo(true);
        rol.setEsSistema(false);

        Rol guardado = rolRepository.save(rol);

        // Auditar
        auditoriaService.registrarCreacion(null, "Rol", guardado.getId().toString(),
                "Rol creado: " + guardado.getNombre());

        log.info("Rol creado exitosamente: {} (ID: {})", guardado.getNombre(), guardado.getId());
        return guardado;
    }

    /**
     * Actualizar un rol existente
     */
    @Transactional
    public Rol actualizarRol(Rol rol) {
        log.info("Actualizando rol: {}", rol.getNombre());

        Rol existente = rolRepository.findById(rol.getId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // No permitir editar roles del sistema
        if (Boolean.TRUE.equals(existente.getEsSistema())) {
            throw new IllegalArgumentException("No se pueden modificar roles del sistema");
        }

        // Validar nombre único (si cambió)
        if (!existente.getNombre().equals(rol.getNombre())) {
            if (rolRepository.existsByNombre(rol.getNombre())) {
                throw new IllegalArgumentException("Ya existe un rol con ese nombre");
            }
        }

        Rol actualizado = rolRepository.save(rol);

        // Auditar
        auditoriaService.registrarActualizacion(null, "Rol", actualizado.getId().toString(),
                "Rol actualizado: " + actualizado.getNombre());

        log.info("Rol actualizado exitosamente: {}", actualizado.getNombre());
        return actualizado;
    }

    /**
     * Buscar rol por nombre
     */
    public Optional<Rol> buscarPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    /**
     * Buscar rol por ID
     */
    public Optional<Rol> buscarPorId(Long id) {
        return rolRepository.findById(id);
    }

    /**
     * Listar todos los roles
     */
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    /**
     * Listar roles activos
     */
    public List<Rol> listarActivos() {
        return rolRepository.findByActivoTrue();
    }

    /**
     * Listar roles del sistema
     */
    public List<Rol> listarRolesSistema() {
        return rolRepository.findByEsSistemaTrue();
    }

    /**
     * Listar roles personalizados (no sistema)
     */
    public List<Rol> listarRolesPersonalizados() {
        return rolRepository.findByEsSistemaFalse();
    }

    /**
     * Listar roles personalizados activos
     */
    public List<Rol> listarRolesPersonalizadosActivos() {
        return rolRepository.findByEsSistemaFalseAndActivoTrue();
    }

    /**
     * Buscar roles por texto
     */
    public List<Rol> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }
        return rolRepository.buscar(texto.trim());
    }

    /**
     * Eliminar rol (desactivar si es personalizado)
     */
    @Transactional
    public void eliminarRol(Long rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // No permitir eliminar roles del sistema
        if (Boolean.TRUE.equals(rol.getEsSistema())) {
            throw new IllegalArgumentException("No se pueden eliminar roles del sistema");
        }

        rol.setActivo(false);
        rolRepository.save(rol);

        auditoriaService.registrarEliminacion(null, "Rol", rolId.toString(),
                "Rol desactivado: " + rol.getNombre());

        log.info("Rol desactivado: {}", rol.getNombre());
    }

    /**
     * Activar un rol
     */
    @Transactional
    public void activarRol(Long rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        rol.setActivo(true);
        rolRepository.save(rol);

        auditoriaService.registrarActualizacion(null, "Rol", rolId.toString(),
                "Rol activado: " + rol.getNombre());

        log.info("Rol activado: {}", rol.getNombre());
    }

    /**
     * Actualizar permisos de un rol
     */
    @Transactional
    public void actualizarPermisos(Long rolId, Map<String, Map<String, Boolean>> nuevosPermisos) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // No permitir editar permisos de roles del sistema
        if (Boolean.TRUE.equals(rol.getEsSistema())) {
            throw new IllegalArgumentException("No se pueden modificar permisos de roles del sistema");
        }

        rol.setPermisos(nuevosPermisos);
        rolRepository.save(rol);

        auditoriaService.registrarActualizacion(null, "Rol", rolId.toString(),
                "Permisos actualizados para rol: " + rol.getNombre());

        log.info("Permisos actualizados para rol: {}", rol.getNombre());
    }

    /**
     * Verificar si un rol tiene un permiso específico
     */
    public boolean tienePermiso(Long rolId, String modulo, String accion) {
        Optional<Rol> rolOpt = rolRepository.findById(rolId);
        if (rolOpt.isEmpty()) {
            return false;
        }

        Rol rol = rolOpt.get();
        if (rol.getPermisos() == null) {
            return false;
        }

        var permisos = rol.getPermisos();
        if (!permisos.containsKey(modulo)) {
            return false;
        }

        var permisosModulo = permisos.get(modulo);
        if (permisosModulo == null || !permisosModulo.containsKey(accion)) {
            return false;
        }

        return Boolean.TRUE.equals(permisosModulo.get(accion));
    }

    /**
     * Contar roles activos
     */
    public long contarActivos() {
        return rolRepository.countByActivoTrue();
    }
}


