package alicanteweb.erp.service;

import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.PedidoRepository;
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
 * Tests para PedidoService
 * Verifica la gestión de pedidos
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedidoPrueba;
    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        clientePrueba = new Cliente();
        clientePrueba.setId(1L);
        clientePrueba.setNombre("Cliente Test");
        clientePrueba.setCif("B12345678");

        pedidoPrueba = new Pedido();
        pedidoPrueba.setId(1L);
        pedidoPrueba.setNumero("PED-2026-001");
        pedidoPrueba.setCliente(clientePrueba);
        pedidoPrueba.setFecha(LocalDate.now());
        pedidoPrueba.setTotal(new BigDecimal("250.00"));
        pedidoPrueba.setEstado("PENDIENTE");
    }

    @Test
    void testCrearPedido() {
        // Arrange
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoPrueba);

        // Act
        Pedido resultado = pedidoService.save(pedidoPrueba);

        // Assert
        assertNotNull(resultado);
        assertEquals("PED-2026-001", resultado.getNumero());
        assertEquals("PENDIENTE", resultado.getEstado());
        verify(pedidoRepository, times(1)).save(pedidoPrueba);
    }

    @Test
    void testBuscarPedidoPorId() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPrueba));

        // Act
        Optional<Pedido> resultado = pedidoService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("PED-2026-001", resultado.get().getNumero());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void testListarTodosLosPedidos() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedidoPrueba, new Pedido());
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPedidosPorCliente() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedidoPrueba);
        when(pedidoRepository.findByClienteId(1L)).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.findByClienteId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Cliente Test", resultado.get(0).getCliente().getNombre());
    }

    @Test
    void testGenerarNumeroPedido() {
        // Arrange
        when(pedidoRepository.count()).thenReturn(50L);

        // Act
        String numero = pedidoService.generarNumeroPedido();

        // Assert
        assertNotNull(numero);
        assertTrue(numero.contains("2026"));
        assertTrue(numero.contains("051"));
    }

    @Test
    void testCancelarPedido() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPrueba));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoPrueba);

        // Act
        pedidoService.cancelar(1L);

        // Assert
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testServirPedido() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPrueba));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoPrueba);

        // Act
        pedidoService.servir(1L);

        // Assert
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testEliminarPedido() {
        // Arrange
        doNothing().when(pedidoRepository).deleteById(1L);

        // Act
        pedidoService.deleteById(1L);

        // Assert
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testBuscarPedidosPorEstado() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedidoPrueba);
        when(pedidoRepository.findByEstado("PENDIENTE")).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.findByEstado("PENDIENTE");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
    }

    @Test
    void testBuscarPedidosPorFecha() {
        // Arrange
        LocalDate fecha = LocalDate.now();
        List<Pedido> pedidos = Arrays.asList(pedidoPrueba);
        when(pedidoRepository.findByFecha(fecha)).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.findByFecha(fecha);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }
}

