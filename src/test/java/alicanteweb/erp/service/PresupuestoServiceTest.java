package alicanteweb.erp.service;

import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para PresupuestoService
 * Verifica la gestión de presupuestos
 */
@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private PresupuestoService presupuestoService;

    private Presupuesto presupuestoPrueba;
    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        clientePrueba = new Cliente();
        clientePrueba.setId(1L);
        clientePrueba.setNombre("Cliente Test");
        clientePrueba.setCif("B12345678");

        presupuestoPrueba = new Presupuesto();
        presupuestoPrueba.setId(1L);
        presupuestoPrueba.setNumero("PRE-2026-001");
        presupuestoPrueba.setCliente(clientePrueba);
        presupuestoPrueba.setFecha(LocalDate.now());
        presupuestoPrueba.setTotal(new BigDecimal("500.00"));
        presupuestoPrueba.setEstado("PENDIENTE");
        presupuestoPrueba.setValidoHasta(LocalDate.now().plusDays(30));
    }

    @Test
    void testCrearPresupuesto() {
        // Arrange
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuestoPrueba);

        // Act
        Presupuesto resultado = presupuestoService.save(presupuestoPrueba);

        // Assert
        assertNotNull(resultado);
        assertEquals("PRE-2026-001", resultado.getNumero());
        assertEquals("PENDIENTE", resultado.getEstado());
        verify(presupuestoRepository, times(1)).save(presupuestoPrueba);
    }

    @Test
    void testBuscarPresupuestoPorId() {
        // Arrange
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuestoPrueba));

        // Act
        Optional<Presupuesto> resultado = presupuestoService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("PRE-2026-001", resultado.get().getNumero());
        verify(presupuestoRepository, times(1)).findById(1L);
    }

    @Test
    void testListarTodosLosPresupuestos() {
        // Arrange
        List<Presupuesto> presupuestos = Arrays.asList(presupuestoPrueba, new Presupuesto());
        when(presupuestoRepository.findAll()).thenReturn(presupuestos);

        // Act
        List<Presupuesto> resultado = presupuestoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(presupuestoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPresupuestosPorCliente() {
        // Arrange
        List<Presupuesto> presupuestos = Arrays.asList(presupuestoPrueba);
        when(presupuestoRepository.findByClienteId(1L)).thenReturn(presupuestos);

        // Act
        List<Presupuesto> resultado = presupuestoService.findByCliente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Cliente Test", resultado.get(0).getCliente().getNombre());
    }

    @Test
    void testGenerarNumeroPresupuesto() {
        // Arrange
        when(presupuestoRepository.findMaxNumeroByAnio(2026)).thenReturn(10);

        // Act
        String numero = presupuestoService.generarNumeroPresupuesto();

        // Assert
        assertNotNull(numero);
        assertTrue(numero.contains("2026"));
        assertTrue(numero.contains("011"));
    }

    @Test
    void testAceptarPresupuesto() {
        // Arrange
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuestoPrueba));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuestoPrueba);

        // Act
        presupuestoService.aceptar(1L);

        // Assert
        verify(presupuestoRepository, times(1)).save(any(Presupuesto.class));
    }

    @Test
    void testRechazarPresupuesto() {
        // Arrange
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuestoPrueba));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuestoPrueba);

        // Act
        presupuestoService.rechazar(1L);

        // Assert
        verify(presupuestoRepository, times(1)).save(any(Presupuesto.class));
    }

    @Test
    void testVerificarPresupuestoCaducado() {
        // Arrange
        presupuestoPrueba.setValidoHasta(LocalDate.now().minusDays(1));

        // Act
        boolean caducado = presupuestoService.estaCaducado(presupuestoPrueba);

        // Assert
        assertTrue(caducado);
    }

    @Test
    void testVerificarPresupuestoVigente() {
        // Arrange
        presupuestoPrueba.setValidoHasta(LocalDate.now().plusDays(15));

        // Act
        boolean caducado = presupuestoService.estaCaducado(presupuestoPrueba);

        // Assert
        assertFalse(caducado);
    }

    @Test
    void testBuscarPresupuestosPendientes() {
        // Arrange
        List<Presupuesto> presupuestos = Arrays.asList(presupuestoPrueba);
        when(presupuestoRepository.findByEstado("PENDIENTE")).thenReturn(presupuestos);

        // Act
        List<Presupuesto> resultado = presupuestoService.findPendientes();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
    }

    @Test
    void testEliminarPresupuesto() {
        // Arrange
        doNothing().when(presupuestoRepository).deleteById(1L);

        // Act
        presupuestoService.deleteById(1L);

        // Assert
        verify(presupuestoRepository, times(1)).deleteById(1L);
    }
}

