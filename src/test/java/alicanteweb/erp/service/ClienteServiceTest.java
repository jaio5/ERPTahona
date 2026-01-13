package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.ClienteRepository;
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
 * Tests unitarios para ClienteService
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteTest;

    @BeforeEach
    void setUp() {
        clienteTest = new Cliente();
        clienteTest.setId(1L);
        clienteTest.setNombre("Panadería Test");
        clienteTest.setCif("B12345678");
        clienteTest.setEmail("test@panaderia.com");
        clienteTest.setActivo(true);
    }

    @Test
    void testFindAll() {
        // Given
        List<Cliente> clientes = Arrays.asList(clienteTest);
        when(clienteRepository.findAll()).thenReturn(clientes);

        // When
        List<Cliente> result = clienteService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Panadería Test", result.get(0).getNombre());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteTest));

        // When
        Optional<Cliente> result = clienteService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Panadería Test", result.get().getNombre());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    void testSave() {
        // Given
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteTest);

        // When
        Cliente result = clienteService.save(clienteTest);

        // Then
        assertNotNull(result);
        assertEquals("Panadería Test", result.getNombre());
        assertEquals("B12345678", result.getCif());
        verify(clienteRepository, times(1)).save(clienteTest);
    }

    @Test
    void testDelete() {
        // Given
        doNothing().when(clienteRepository).deleteById(1L);

        // When
        clienteService.deleteById(1L);

        // Then
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindByActivo() {
        // Given
        List<Cliente> clientesActivos = Arrays.asList(clienteTest);
        when(clienteRepository.findByActivo(true)).thenReturn(clientesActivos);

        // When
        List<Cliente> result = clienteRepository.findByActivo(true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActivo());
    }
}

