package alicanteweb.erp.service;

import alicanteweb.erp.entities.AuditoriaAccion;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.AuditoriaAccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para AuditoriaService
 * Verifica el correcto funcionamiento del sistema de auditoría
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaAccionRepository auditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Usuario usuarioPrueba;
    private AuditoriaAccion accionPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setUsername("admin");
        usuarioPrueba.setNombre("Administrador");

        accionPrueba = new AuditoriaAccion();
        accionPrueba.setId(1L);
        accionPrueba.setTipoAccion("LOGIN");
        accionPrueba.setUsuarioNombre("admin");
        accionPrueba.setFecha(LocalDateTime.now());
        accionPrueba.setResultado("EXITOSO");
        accionPrueba.setModulo("SEGURIDAD");
    }

    @Test
    void testRegistrarLoginExitoso() {
        // Arrange
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionPrueba);

        // Act
        auditoriaService.registrarLogin(usuarioPrueba, true);

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }

    @Test
    void testRegistrarLoginFallido() {
        // Arrange
        AuditoriaAccion accionFallida = new AuditoriaAccion();
        accionFallida.setResultado("FALLIDO");
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionFallida);

        // Act
        auditoriaService.registrarLogin(usuarioPrueba, false);

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }

    @Test
    void testRegistrarAccionCrear() {
        // Arrange
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionPrueba);

        // Act
        auditoriaService.registrarAccion("CREAR", "CLIENTES", 1L, usuarioPrueba, "Cliente creado");

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }

    @Test
    void testRegistrarAccionModificar() {
        // Arrange
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionPrueba);

        // Act
        auditoriaService.registrarAccion("MODIFICAR", "FACTURAS", 100L, usuarioPrueba, "Factura actualizada");

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }

    @Test
    void testRegistrarAccionEliminar() {
        // Arrange
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionPrueba);

        // Act
        auditoriaService.registrarAccion("ELIMINAR", "ARTICULOS", 50L, usuarioPrueba, "Artículo eliminado");

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }

    @Test
    void testObtenerHistorialUsuario() {
        // Arrange
        List<AuditoriaAccion> historial = Arrays.asList(
            accionPrueba,
            new AuditoriaAccion()
        );
        when(auditoriaRepository.findByUsuarioNombreOrderByFechaDesc("admin")).thenReturn(historial);

        // Act
        List<AuditoriaAccion> resultado = auditoriaService.obtenerHistorialUsuario("admin");

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(auditoriaRepository, times(1)).findByUsuarioNombreOrderByFechaDesc("admin");
    }

    @Test
    void testObtenerHistorialModulo() {
        // Arrange
        List<AuditoriaAccion> historial = Arrays.asList(accionPrueba);
        when(auditoriaRepository.findByModuloOrderByFechaDesc("FACTURAS")).thenReturn(historial);

        // Act
        List<AuditoriaAccion> resultado = auditoriaService.obtenerHistorialModulo("FACTURAS");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("SEGURIDAD", resultado.get(0).getModulo());
    }

    @Test
    void testRegistrarError() {
        // Arrange
        AuditoriaAccion accionError = new AuditoriaAccion();
        accionError.setResultado("ERROR");
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionError);

        // Act
        auditoriaService.registrarError("Error de conexión", "BASE_DATOS", usuarioPrueba);

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }

    @Test
    void testNoRegistrarSiRepositoryFalla() {
        // Arrange
        when(auditoriaRepository.save(any(AuditoriaAccion.class)))
            .thenThrow(new RuntimeException("Error BD"));

        // Act & Assert
        assertDoesNotThrow(() ->
            auditoriaService.registrarLogin(usuarioPrueba, true)
        );
    }

    @Test
    void testRegistrarAccionSinUsuario() {
        // Arrange
        when(auditoriaRepository.save(any(AuditoriaAccion.class))).thenReturn(accionPrueba);

        // Act
        auditoriaService.registrarAccion("SISTEMA", "INICIO", null, null, "Sistema iniciado");

        // Assert
        verify(auditoriaRepository, times(1)).save(any(AuditoriaAccion.class));
    }
}

