package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.entities.Rol;
import alicanteweb.erp.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para UsuarioService
 */
@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolService rolService;

    private Rol rolTest;

    @BeforeEach
    void setUp() {
        // Crear rol de prueba si no existe
        rolTest = rolService.buscarPorNombre("TEST_ROL").orElseGet(() -> {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("TEST_ROL");
            nuevoRol.setDescripcion("Rol de prueba");
            nuevoRol.setActivo(true);
            return rolService.crearRol(nuevoRol);
        });
    }

    @Test
    void testCrearUsuario() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setUsername("test_usuario_" + System.currentTimeMillis());
        usuario.setEmail("test" + System.currentTimeMillis() + "@test.com");
        usuario.setNombreCompleto("Usuario Test");
        usuario.setRol(rolTest);

        // When
        Usuario creado = usuarioService.crearUsuario(usuario, "password123");

        // Then
        assertNotNull(creado);
        assertNotNull(creado.getId());
        assertNotNull(creado.getPassword());
        assertNotEquals("password123", creado.getPassword()); // Debe estar cifrada
        assertTrue(creado.getActivo());
        assertFalse(creado.getBloqueado());
    }

    @Test
    void testValidarCredenciales() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setUsername("test_login_" + System.currentTimeMillis());
        usuario.setEmail("testlogin" + System.currentTimeMillis() + "@test.com");
        usuario.setNombreCompleto("Test Login");
        usuario.setRol(rolTest);

        String password = "password123";
        usuarioService.crearUsuario(usuario, password);

        // When
        boolean valido = usuarioService.validarCredenciales(usuario.getUsername(), password);
        boolean invalido = usuarioService.validarCredenciales(usuario.getUsername(), "wrongpassword");

        // Then
        assertTrue(valido);
        assertFalse(invalido);
    }

    @Test
    void testBloquearUsuarioPorIntentosF allidos() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setUsername("test_bloqueo_" + System.currentTimeMillis());
        usuario.setEmail("testbloqueo" + System.currentTimeMillis() + "@test.com");
        usuario.setNombreCompleto("Test Bloqueo");
        usuario.setRol(rolTest);

        Usuario creado = usuarioService.crearUsuario(usuario, "password123");

        // When - Incrementar intentos fallidos 5 veces
        for (int i = 0; i < 5; i++) {
            usuarioService.incrementarIntentosFallidos(creado.getId());
        }

        // Then
        Usuario bloqueado = usuarioRepository.findById(creado.getId()).orElseThrow();
        assertTrue(bloqueado.getBloqueado());
        assertEquals(5, bloqueado.getIntentosFallidos());
    }

    @Test
    void testBuscarUsuarioPorUsername() {
        // Given
        String username = "test_buscar_" + System.currentTimeMillis();
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail("testbuscar" + System.currentTimeMillis() + "@test.com");
        usuario.setNombreCompleto("Test Buscar");
        usuario.setRol(rolTest);

        usuarioService.crearUsuario(usuario, "password123");

        // When
        var encontrado = usuarioService.buscarPorUsername(username);

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals(username, encontrado.get().getUsername());
    }

    @Test
    void testGenerarTokenRecuperacion() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setUsername("test_token_" + System.currentTimeMillis());
        String email = "testtoken" + System.currentTimeMillis() + "@test.com";
        usuario.setEmail(email);
        usuario.setNombreCompleto("Test Token");
        usuario.setRol(rolTest);

        usuarioService.crearUsuario(usuario, "password123");

        // When
        String token = usuarioService.generarTokenRecuperacion(email);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verificar que el token se guardó
        Usuario conToken = usuarioRepository.findByEmail(email).orElseThrow();
        assertEquals(token, conToken.getTokenRecuperacion());
        assertNotNull(conToken.getFechaExpiracionToken());
    }

    @Test
    void testCambiarPassword() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setUsername("test_cambio_" + System.currentTimeMillis());
        usuario.setEmail("testcambio" + System.currentTimeMillis() + "@test.com");
        usuario.setNombreCompleto("Test Cambio");
        usuario.setRol(rolTest);

        String oldPassword = "password123";
        Usuario creado = usuarioService.crearUsuario(usuario, oldPassword);
        String oldHash = creado.getPassword();

        // When
        String newPassword = "newpassword456";
        usuarioService.cambiarPassword(creado.getId(), oldPassword, newPassword);

        // Then
        Usuario actualizado = usuarioRepository.findById(creado.getId()).orElseThrow();
        assertNotEquals(oldHash, actualizado.getPassword());
        assertFalse(actualizado.getRequiereCambioPassword());
    }

    @Test
    void testEliminarUsuario() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setUsername("test_eliminar_" + System.currentTimeMillis());
        usuario.setEmail("testeliminar" + System.currentTimeMillis() + "@test.com");
        usuario.setNombreCompleto("Test Eliminar");
        usuario.setRol(rolTest);

        Usuario creado = usuarioService.crearUsuario(usuario, "password123");
        assertTrue(creado.getActivo());

        // When
        usuarioService.eliminarUsuario(creado.getId());

        // Then
        Usuario eliminado = usuarioRepository.findById(creado.getId()).orElseThrow();
        assertFalse(eliminado.getActivo());
    }

    @Test
    void testContarUsuariosActivos() {
        // When
        long count = usuarioService.contarActivos();

        // Then
        assertTrue(count >= 0);
    }
}

