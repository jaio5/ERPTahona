package alicanteweb.erp.controller;

import alicanteweb.erp.config.PermisoEvaluador;
import alicanteweb.erp.controller.rest.WebEntityController;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebEntityControllerTest {

    @Mock EntityManager entityManager;
    @Mock UsuarioService usuarioService;
    @Mock Validator validator;
    @Mock PermisoEvaluador permisos;
    WebEntityController controller;

    @BeforeEach
    void setUp() {
        // Autenticado como ADMIN: el guard granular no interviene (bypass admin);
        // la autorización por rol se cubre en RestApiPermisosTest.
        controller = new WebEntityController(entityManager, usuarioService, validator, permisos);
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void exponeModulosYDevuelveNotFoundParaModuloDesconocido() {
        assertTrue(controller.modules().contains("proveedores"));
        assertEquals(HttpStatus.NOT_FOUND, controller.get("desconocido", 1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.create("desconocido", Map.of()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.delete("desconocido", 1L).getStatusCode());
    }

    @Test
    void obtieneEntidadComoDtoSinRelacionesInternas() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Proveedor");
        proveedor.setActivo(true);
        when(entityManager.find(Proveedor.class, 1L)).thenReturn(proveedor);

        Map<String, Object> body = controller.get("proveedores", 1L).getBody();
        assertNotNull(body);
        assertEquals("Proveedor", body.get("nombre"));
        assertEquals(true, body.get("activo"));
    }

    @Test
    void creaYActualizaProveedorConConversiones() {
        var created = controller.create("proveedores", Map.of(
                "nombre", "Proveedor",
                "descuento", "5.50",
                "activo", "true"
        ));
        assertEquals(HttpStatus.OK, created.getStatusCode());
        verify(entityManager).persist(any(Proveedor.class));
        verify(entityManager).flush();

        Proveedor proveedor = new Proveedor();
        proveedor.setId(2L);
        proveedor.setNombre("Anterior");
        when(entityManager.find(Proveedor.class, 2L)).thenReturn(proveedor);
        controller.update("proveedores", 2L, Map.of("nombre", "Nuevo", "descuento", 3));
        assertEquals("Nuevo", proveedor.getNombre());
        assertEquals(0, new BigDecimal("3").compareTo(proveedor.getDescuento()));
    }

    @Test
    void creaUsuarioSoloMedianteServicioYExigePassword() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.create("usuarios", Map.of("username", "u")));

        Usuario guardado = new Usuario();
        guardado.setId(4L);
        guardado.setUsername("u");
        when(usuarioService.crearUsuario(any(Usuario.class), eq("clave"))).thenReturn(guardado);
        var response = controller.create("usuarios", Map.of(
                "username", "u", "passwordNuevo", "clave"
        ));
        assertEquals(4L, response.getBody().get("id"));
    }

    @Test
    void modulosDeTrazabilidadSonSoloLectura() {
        assertThrows(IllegalStateException.class,
                () -> controller.create("auditoria", Map.of()));
        assertThrows(IllegalStateException.class,
                () -> controller.update("verifactu-evidencias", 1L, Map.of()));
        assertThrows(IllegalStateException.class,
                () -> controller.delete("modelo347", 1L));
    }

    @Test
    void lasFacturasNoAdmitenEscrituraGenerica() {
        // Inalterabilidad RRSIF/VeriFactu: las facturas solo se tocan por sus servicios propios
        assertThrows(IllegalStateException.class,
                () -> controller.create("facturas", Map.of()));
        assertThrows(IllegalStateException.class,
                () -> controller.update("facturas", 1L, Map.of("total", "999.99")));
        assertThrows(IllegalStateException.class,
                () -> controller.delete("facturas", 1L));
        assertThrows(IllegalStateException.class,
                () -> controller.update("facturas-compra", 1L, Map.of()));
        assertThrows(IllegalStateException.class,
                () -> controller.delete("facturas-compra", 1L));
    }

    @Test
    void activaDesactivaYEliminaEntidades() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setActivo(true);
        when(entityManager.find(Proveedor.class, 1L)).thenReturn(proveedor);

        assertEquals(HttpStatus.NO_CONTENT, controller.disable("proveedores", 1L).getStatusCode());
        assertFalse(proveedor.getActivo());
        assertEquals(HttpStatus.NO_CONTENT, controller.enable("proveedores", 1L).getStatusCode());
        assertTrue(proveedor.getActivo());
        assertEquals(HttpStatus.NO_CONTENT, controller.delete("proveedores", 1L).getStatusCode());
        verify(entityManager).remove(proveedor);
    }

    @Test
    void updateUsuarioMantieneServicioEspecializado() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("u");
        when(entityManager.find(Usuario.class, 1L)).thenReturn(usuario);
        when(usuarioService.actualizarUsuario(usuario)).thenReturn(usuario);
        controller.update("usuarios", 1L, Map.of(
                "nombre", "Nombre",
                "passwordNuevo", "nueva"
        ));

        verify(usuarioService).actualizarUsuario(usuario);
        verify(usuarioService).cambiarPasswordAdmin(1L, "nueva");
        assertEquals("Nombre", usuario.getNombre());
    }
}
