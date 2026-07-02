package alicanteweb.erp.service;

import alicanteweb.erp.entities.Rol;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RolRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository repository;
    @Mock RolRepository rolRepository;
    @Mock CifradoService cifradoService;
    @Mock AuditoriaService auditoriaService;
    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, rolRepository, cifradoService, auditoriaService);
    }

    @Test
    void crearUsuarioInicializaSeguridadYSincronizaRol() {
        Usuario usuario = usuario(1L, "nuevo");
        usuario.setRole("ADMIN");
        Rol rol = new Rol();
        rol.setNombre("ADMIN");
        when(cifradoService.hashPassword("TestPass1")).thenReturn("hash");
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rol));
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario creado = service.crearUsuario(usuario, "TestPass1");

        assertEquals("hash", creado.getPassword());
        assertTrue(creado.getEnabled());
        assertFalse(creado.getBloqueado());
        assertEquals(0, creado.getIntentosFallidos());
        assertSame(rol, creado.getRol());
        verify(auditoriaService).registrarCreacion(isNull(), eq("Usuario"), eq("1"), contains("nuevo"));
    }

    @Test
    void crearUsuarioRechazaUsernameOEmailDuplicados() {
        Usuario usuario = usuario(null, "duplicado");
        when(repository.existsByUsername("duplicado")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.crearUsuario(usuario, "TestPass1"));

        reset(repository);
        when(repository.existsByEmail(usuario.getEmail())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.crearUsuario(usuario, "TestPass1"));
    }

    @Test
    void actualizarConservaPasswordYValidaUnicos() {
        Usuario existente = usuario(1L, "actual");
        existente.setPassword("hash-antiguo");
        Usuario cambios = usuario(1L, "nuevo");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(cambios)).thenReturn(cambios);

        Usuario actualizado = service.actualizarUsuario(cambios);
        assertEquals("hash-antiguo", actualizado.getPassword());

        cambios.setUsername("ocupado");
        when(repository.existsByUsername("ocupado")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.actualizarUsuario(cambios));
    }

    @Test
    void cambiarPasswordValidaAnteriorYAdminPuedeForzarCambio() {
        Usuario usuario = usuario(1L, "u");
        usuario.setPassword("old-hash");
        usuario.setRequiereCambioPassword(true);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cifradoService.verificarPassword("old", "old-hash")).thenReturn(true);
        when(cifradoService.hashPassword("NewPass1")).thenReturn("new-hash");

        service.cambiarPassword(1L, "old", "NewPass1");
        assertEquals("new-hash", usuario.getPassword());
        assertFalse(usuario.getRequiereCambioPassword());

        when(cifradoService.verificarPassword("bad", "new-hash")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.cambiarPassword(1L, "bad", "NewPass1"));

        when(cifradoService.hashPassword("AdminPass1")).thenReturn("admin-hash");
        service.cambiarPasswordAdmin(1L, "AdminPass1");
        assertEquals("admin-hash", usuario.getPassword());
    }

    @Test
    void validarCredencialesGestionaEstadosIntentosYBloqueo() {
        assertFalse(service.validarCredenciales("missing", "x"));

        Usuario usuario = usuario(1L, "u");
        usuario.setPassword("hash");
        usuario.setEnabled(false);
        when(repository.findByUsername("u")).thenReturn(Optional.of(usuario));
        assertFalse(service.validarCredenciales("u", "x"));

        usuario.setEnabled(true);
        usuario.setBloqueado(true);
        assertFalse(service.validarCredenciales("u", "x"));

        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(4);
        when(cifradoService.verificarPassword("bad", "hash")).thenReturn(false);
        assertFalse(service.validarCredenciales("u", "bad"));
        assertEquals(5, usuario.getIntentosFallidos());
        assertTrue(usuario.getBloqueado());

        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(3);
        when(cifradoService.verificarPassword("ok", "hash")).thenReturn(true);
        assertTrue(service.validarCredenciales("u", "ok"));
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test
    void operacionesAdministrativasActualizanEstado() {
        Usuario usuario = usuario(1L, "u");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        service.bloquearUsuario(1L);
        assertTrue(usuario.getBloqueado());
        service.desbloquearUsuario(1L);
        assertFalse(usuario.getBloqueado());
        assertEquals(0, usuario.getIntentosFallidos());

        usuario.setIntentosFallidos(4);
        service.incrementarIntentosFallidos(1L);
        assertTrue(usuario.getBloqueado());
        service.resetearIntentosFallidos(1L);
        assertEquals(0, usuario.getIntentosFallidos());

        service.eliminarUsuario(1L);
        assertFalse(usuario.getEnabled());
        service.activarUsuario(1L);
        assertTrue(usuario.getEnabled());
        service.actualizarUltimoLogin(1L);
        assertNotNull(usuario.getUltimoLogin());
    }

    @Test
    void consultasDeleganYBusquedaVaciaListaTodos() {
        Usuario usuario = usuario(1L, "u");
        when(repository.findAllWithRol()).thenReturn(List.of(usuario));
        when(repository.findByEnabledTrue()).thenReturn(List.of(usuario));
        when(repository.buscar("texto")).thenReturn(List.of(usuario));
        when(repository.countByEnabledTrue()).thenReturn(1L);

        assertEquals(1, service.listarTodos().size());
        assertEquals(1, service.listarActivos().size());
        assertEquals(1, service.buscar(" ").size());
        assertEquals(1, service.buscar(" texto ").size());
        assertEquals(1, service.contarActivos());
        verify(repository).buscar("texto");
    }

    @Test
    void generaTokenRecuperacion() {
        Usuario usuario = usuario(1L, "u");
        when(repository.findByUsername("u")).thenReturn(Optional.of(usuario));
        Usuario result = service.generarTokenRecuperacion("u");
        assertNotNull(result.getTokenRecuperacion());
        assertNotNull(result.getFechaExpiracionToken());
    }

    private Usuario usuario(Long id, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setNombre(username);
        usuario.setEmail(username + "@example.com");
        usuario.setEnabled(true);
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        return usuario;
    }
}
