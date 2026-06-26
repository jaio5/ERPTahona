package alicanteweb.erp.service;

import alicanteweb.erp.entities.Rol;
import alicanteweb.erp.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock RolRepository repository;
    @Mock AuditoriaService auditoriaService;
    RolService service;

    @BeforeEach
    void setUp() {
        service = new RolService(repository, auditoriaService);
    }

    @Test
    void crearYActualizarRolAplicanRestricciones() {
        Rol rol = rol(1L, "GESTOR", false);
        when(repository.save(rol)).thenReturn(rol);
        assertSame(rol, service.crearRol(rol));
        assertTrue(rol.getActivo());
        assertFalse(rol.getEsSistema());

        when(repository.existsByNombre("DUPLICADO")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.crearRol(rol(null, "DUPLICADO", false)));

        Rol sistema = rol(2L, "ADMIN", true);
        when(repository.findById(2L)).thenReturn(Optional.of(sistema));
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarRol(rol(2L, "ADMIN", false)));

        Rol existente = rol(3L, "ANTERIOR", false);
        Rol cambio = rol(3L, "NUEVO", false);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.save(cambio)).thenReturn(cambio);
        assertSame(cambio, service.actualizarRol(cambio));

        when(repository.existsByNombre("OCUPADO")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarRol(rol(3L, "OCUPADO", false)));
    }

    @Test
    void consultasBusquedaYContadorDelegan() {
        List<Rol> roles = List.of(rol(1L, "GESTOR", false));
        when(repository.findAll()).thenReturn(roles);
        when(repository.findByActivoTrue()).thenReturn(roles);
        when(repository.findByEsSistemaTrue()).thenReturn(roles);
        when(repository.findByEsSistemaFalse()).thenReturn(roles);
        when(repository.findByEsSistemaFalseAndActivoTrue()).thenReturn(roles);
        when(repository.buscar("ges")).thenReturn(roles);
        when(repository.findByNombre("GESTOR")).thenReturn(Optional.of(roles.get(0)));
        when(repository.findById(1L)).thenReturn(Optional.of(roles.get(0)));
        when(repository.countByActivoTrue()).thenReturn(1L);

        assertEquals("GESTOR", service.buscarPorNombre("GESTOR").orElseThrow().getNombre());
        assertTrue(service.buscarPorId(1L).isPresent());
        assertSame(roles, service.listarTodos());
        assertSame(roles, service.listarActivos());
        assertSame(roles, service.listarRolesSistema());
        assertSame(roles, service.listarRolesPersonalizados());
        assertSame(roles, service.listarRolesPersonalizadosActivos());
        assertSame(roles, service.buscar(" ges "));
        assertSame(roles, service.buscar(" "));
        assertEquals(1L, service.contarActivos());
    }

    @Test
    void activacionEliminacionYPermisosRespetanRolesSistema() {
        Rol rol = rol(4L, "VENTAS", false);
        when(repository.findById(4L)).thenReturn(Optional.of(rol));

        service.eliminarRol(4L);
        assertFalse(rol.getActivo());
        service.activarRol(4L);
        assertTrue(rol.getActivo());

        Map<String, Map<String, Boolean>> permisos =
                Map.of("FACTURAS", Map.of("LEER", true, "BORRAR", false));
        service.actualizarPermisos(4L, permisos);
        assertSame(permisos, rol.getPermisos());
        assertTrue(service.tienePermiso(4L, "FACTURAS", "LEER"));
        assertFalse(service.tienePermiso(4L, "FACTURAS", "BORRAR"));
        assertFalse(service.tienePermiso(4L, "CLIENTES", "LEER"));
        assertFalse(service.tienePermiso(99L, "X", "Y"));

        rol.setPermisos(null);
        assertFalse(service.tienePermiso(4L, "X", "Y"));

        Rol sistema = rol(5L, "ADMIN", true);
        when(repository.findById(5L)).thenReturn(Optional.of(sistema));
        assertThrows(IllegalArgumentException.class, () -> service.eliminarRol(5L));
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarPermisos(5L, permisos));
    }

    private Rol rol(Long id, String nombre, boolean sistema) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        rol.setEsSistema(sistema);
        rol.setActivo(true);
        return rol;
    }
}
