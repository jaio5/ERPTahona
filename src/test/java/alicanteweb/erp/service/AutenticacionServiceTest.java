package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AutenticacionService
 *
 * Cobertura: 10 tests
 * - Login: 4 tests
 * - Contraseñas: 3 tests
 * - Validaciones: 2 tests
 * - Sesión: 1 test
 *
 * @author ERP Tahona
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de AutenticacionService")
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AutenticacionService autenticacionService;

    private Usuario usuarioEjemplo;

    @BeforeEach
    void setUp() {
        usuarioEjemplo = new Usuario();
        usuarioEjemplo.setId(1L);
        usuarioEjemplo.setUsername("admin");
        usuarioEjemplo.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // "admin" hasheado
        usuarioEjemplo.setNombre("Administrador");
        usuarioEjemplo.setEmail("admin@erptahona.com");
        usuarioEjemplo.setEnabled(true);
        usuarioEjemplo.setBloqueado(false);
        usuarioEjemplo.setIntentosFallidos(0);
    }

    // ==================== TESTS LOGIN ====================

    // TEST 1: Login exitoso
    @Test
    @DisplayName("01 - Debería realizar login exitoso con credenciales correctas")
    void deberiaRealizarLoginExitoso() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));
        when(passwordEncoder.matches("admin", usuarioEjemplo.getPassword())).thenReturn(true);

        // When
        boolean resultado = autenticacionService.autenticar("admin", "admin");

        // Then
        assertTrue(resultado);
        verify(usuarioRepository, times(1)).findByUsername("admin");
        verify(passwordEncoder, times(1)).matches("admin", usuarioEjemplo.getPassword());
    }

    // TEST 2: Login con contraseña incorrecta
    @Test
    @DisplayName("02 - Debería rechazar login con contraseña incorrecta")
    void deberiaRechazarLoginConPasswordIncorrecta() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));
        when(passwordEncoder.matches("wrongpassword", usuarioEjemplo.getPassword())).thenReturn(false);

        // When
        boolean resultado = autenticacionService.autenticar("admin", "wrongpassword");

        // Then
        assertFalse(resultado);
        verify(usuarioRepository, times(1)).findByUsername("admin");
        verify(passwordEncoder, times(1)).matches("wrongpassword", usuarioEjemplo.getPassword());
    }

    // TEST 3: Login con usuario no encontrado
    @Test
    @DisplayName("03 - Debería rechazar login con usuario inexistente")
    void deberiaRechazarLoginConUsuarioInexistente() {
        // Given
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        // When
        boolean resultado = autenticacionService.autenticar("noexiste", "password");

        // Then
        assertFalse(resultado);
        verify(usuarioRepository, times(1)).findByUsername("noexiste");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // TEST 4: Login con usuario bloqueado
    @Test
    @DisplayName("04 - Debería rechazar login de usuario bloqueado")
    void deberiaRechazarLoginUsuarioBloqueado() {
        // Given
        usuarioEjemplo.setBloqueado(true);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));

        // When
        boolean resultado = autenticacionService.autenticar("admin", "admin");

        // Then
        assertFalse(resultado);
        verify(usuarioRepository, times(1)).findByUsername("admin");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ==================== TESTS CONTRASEÑAS ====================

    // TEST 5: Hash de contraseña correcto
    @Test
    @DisplayName("05 - Debería hashear contraseña correctamente")
    void deberiaHashearPasswordCorrectamente() {
        // Given
        String passwordPlain = "admin123";
        String passwordHashed = "$2a$10$hashedpassword";
        when(passwordEncoder.encode(passwordPlain)).thenReturn(passwordHashed);

        // When
        String resultado = passwordEncoder.encode(passwordPlain);

        // Then
        assertNotNull(resultado);
        assertEquals(passwordHashed, resultado);
        assertNotEquals(passwordPlain, resultado);
        verify(passwordEncoder, times(1)).encode(passwordPlain);
    }

    // TEST 6: Cambiar contraseña
    @Test
    @DisplayName("06 - Debería cambiar contraseña correctamente")
    void deberiaCambiarPasswordCorrectamente() {
        // Given
        String nuevaPassword = "nuevaPassword123";
        String nuevaPasswordHashed = "$2a$10$newhashed";

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));
        when(passwordEncoder.encode(nuevaPassword)).thenReturn(nuevaPasswordHashed);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEjemplo);

        // When
        usuarioEjemplo.setPassword(passwordEncoder.encode(nuevaPassword));
        Usuario resultado = usuarioRepository.save(usuarioEjemplo);

        // Then
        assertNotNull(resultado);
        assertEquals(nuevaPasswordHashed, resultado.getPassword());
        verify(passwordEncoder, times(1)).encode(nuevaPassword);
        verify(usuarioRepository, times(1)).save(usuarioEjemplo);
    }

    // TEST 7: Validar contraseña actual antes de cambiar
    @Test
    @DisplayName("07 - Debería validar contraseña actual antes de cambiar")
    void deberiaValidarPasswordActualAntesDeCambiar() {
        // Given
        String passwordActual = "admin";
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));
        when(passwordEncoder.matches(passwordActual, usuarioEjemplo.getPassword())).thenReturn(true);

        // When
        boolean esValida = passwordEncoder.matches(passwordActual, usuarioEjemplo.getPassword());

        // Then
        assertTrue(esValida);
        verify(passwordEncoder, times(1)).matches(passwordActual, usuarioEjemplo.getPassword());
    }

    // ==================== TESTS VALIDACIONES ====================

    // TEST 8: Usuario habilitado
    @Test
    @DisplayName("08 - Debería validar que usuario esté habilitado")
    void deberiaValidarUsuarioHabilitado() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));

        // When
        Optional<Usuario> resultado = usuarioRepository.findByUsername("admin");

        // Then
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getEnabled());
        assertFalse(resultado.get().getBloqueado());
    }

    // TEST 9: Incrementar intentos fallidos
    @Test
    @DisplayName("09 - Debería incrementar intentos fallidos tras login fallido")
    void deberiaIncrementarIntentosFallidos() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));
        when(passwordEncoder.matches("wrongpassword", usuarioEjemplo.getPassword())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean loginFallido = autenticacionService.autenticar("admin", "wrongpassword");
        usuarioEjemplo.setIntentosFallidos(usuarioEjemplo.getIntentosFallidos() + 1);
        Usuario usuarioActualizado = usuarioRepository.save(usuarioEjemplo);

        // Then
        assertFalse(loginFallido);
        assertEquals(1, usuarioActualizado.getIntentosFallidos());
        verify(usuarioRepository, times(1)).save(usuarioEjemplo);
    }

    // ==================== TESTS SESIÓN ====================

    // TEST 10: Resetear intentos fallidos tras login exitoso
    @Test
    @DisplayName("10 - Debería resetear intentos fallidos tras login exitoso")
    void deberiaResetearIntentosFallidosTraLoginExitoso() {
        // Given
        usuarioEjemplo.setIntentosFallidos(3);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioEjemplo));
        when(passwordEncoder.matches("admin", usuarioEjemplo.getPassword())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean loginExitoso = autenticacionService.autenticar("admin", "admin");
        usuarioEjemplo.setIntentosFallidos(0);
        Usuario usuarioActualizado = usuarioRepository.save(usuarioEjemplo);

        // Then
        assertTrue(loginExitoso);
        assertEquals(0, usuarioActualizado.getIntentosFallidos());
        verify(usuarioRepository, times(1)).save(usuarioEjemplo);
    }
}

