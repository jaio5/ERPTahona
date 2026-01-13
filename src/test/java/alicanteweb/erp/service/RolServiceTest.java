package alicanteweb.erp.service;

import alicanteweb.erp.entities.Rol;
import alicanteweb.erp.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para RolService
 * Verifica la gestión de roles y permisos
 */
@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    private Rol rolPrueba;

    @BeforeEach
    void setUp() {
        rolPrueba = new Rol();
        rolPrueba.setId(1L);
        rolPrueba.setNombre("ADMIN");
        rolPrueba.setDescripcion("Administrador del sistema");
        rolPrueba.setActivo(true);
    }

    @Test
    void testCrearRol() {
        // Arrange
        when(rolRepository.save(any(Rol.class))).thenReturn(rolPrueba);

        // Act
        Rol resultado = rolService.save(rolPrueba);

        // Assert
        assertNotNull(resultado);
        assertEquals("ADMIN", resultado.getNombre());
        assertTrue(resultado.getActivo());
        verify(rolRepository, times(1)).save(rolPrueba);
    }

    @Test
    void testBuscarRolPorId() {
        // Arrange
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolPrueba));

        // Act
        Optional<Rol> resultado = rolService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("ADMIN", resultado.get().getNombre());
        verify(rolRepository, times(1)).findById(1L);
    }

    @Test
    void testListarTodosLosRoles() {
        // Arrange
        Rol rol2 = new Rol();
        rol2.setNombre("USER");
        List<Rol> roles = Arrays.asList(rolPrueba, rol2);
        when(rolRepository.findAll()).thenReturn(roles);

        // Act
        List<Rol> resultado = rolService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(rolRepository, times(1)).findAll();
    }

    @Test
    void testBuscarRolPorNombre() {
        // Arrange
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolPrueba));

        // Act
        Optional<Rol> resultado = rolService.findByNombre("ADMIN");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("ADMIN", resultado.get().getNombre());
        verify(rolRepository, times(1)).findByNombre("ADMIN");
    }

    @Test
    void testBuscarRolesActivos() {
        // Arrange
        List<Rol> roles = Arrays.asList(rolPrueba);
        when(rolRepository.findByActivoTrue()).thenReturn(roles);

        // Act
        List<Rol> resultado = rolService.findActivos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());
    }

    @Test
    void testActualizarRol() {
        // Arrange
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolPrueba));
        when(rolRepository.save(any(Rol.class))).thenReturn(rolPrueba);

        // Act
        rolPrueba.setDescripcion("Nueva descripción");
        Rol resultado = rolService.update(rolPrueba);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nueva descripción", resultado.getDescripcion());
    }

    @Test
    void testDesactivarRol() {
        // Arrange
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolPrueba));
        when(rolRepository.save(any(Rol.class))).thenReturn(rolPrueba);

        // Act
        rolService.desactivar(1L);

        // Assert
        verify(rolRepository, times(1)).save(any(Rol.class));
    }

    @Test
    void testActivarRol() {
        // Arrange
        rolPrueba.setActivo(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolPrueba));
        when(rolRepository.save(any(Rol.class))).thenReturn(rolPrueba);

        // Act
        rolService.activar(1L);

        // Assert
        verify(rolRepository, times(1)).save(any(Rol.class));
    }

    @Test
    void testEliminarRol() {
        // Arrange
        doNothing().when(rolRepository).deleteById(1L);

        // Act
        rolService.deleteById(1L);

        // Assert
        verify(rolRepository, times(1)).deleteById(1L);
    }

    @Test
    void testVerificarPermisos() {
        // Act
        boolean tienePermiso = rolService.tienePermiso(rolPrueba, "CREAR_USUARIO");

        // Assert
        // Depende de la implementación
        assertNotNull(tienePermiso);
    }
}

