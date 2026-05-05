package alicanteweb.erp.service;

import alicanteweb.erp.entities.Rol;
import alicanteweb.erp.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    UsuarioService usuarioService;

    @Mock
    AuditoriaService auditoriaService;

    @InjectMocks
    AutenticacionService service;

    @Test
    void login_devuelveNullSiElUsuarioNoExiste() {
        when(usuarioService.buscarPorUsername("ghost")).thenReturn(Optional.empty());

        Usuario usuario = service.login("ghost", "secret");

        assertNull(usuario);
        verify(auditoriaService).registrarError(null, "Usuario", "ghost", "Intento de login - usuario no encontrado");
    }

    @Test
    void login_exitoso_estableceSesionYActualizaUltimoLogin() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setUsername("admin");
        usuario.setNombre("Administrador");
        usuario.setEnabled(true);
        usuario.setBloqueado(false);
        usuario.setRole("ROLE_ADMIN");

        when(usuarioService.buscarPorUsername("admin")).thenReturn(Optional.of(usuario));
        when(usuarioService.validarCredenciales("admin", "ok")).thenReturn(true);

        Usuario autenticado = service.login("admin", "ok");

        assertEquals(usuario, autenticado);
        assertTrue(service.haySesionActiva());
        assertTrue(service.esAdministrador());
        assertEquals("Administrador", service.getNombreUsuarioActual());
        verify(usuarioService).actualizarUltimoLogin(7L);
        verify(auditoriaService).registrarLogin(usuario, null, true);
    }

    @Test
    void tienePermiso_consultaPermisosDelRol() {
        Usuario usuario = new Usuario();
        usuario.setUsername("operador");
        usuario.setRole("ROLE_USER");

        Rol rol = new Rol();
        rol.setPermisos(Map.of(
            "facturas", Map.of("ver", true, "editar", false)
        ));
        usuario.setRol(rol);

        service.setUsuarioActual(usuario);

        assertTrue(service.tienePermiso("facturas", "ver"));
        assertFalse(service.tienePermiso("facturas", "editar"));
        assertFalse(service.tienePermiso("clientes", "ver"));
    }
}
