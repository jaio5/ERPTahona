package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.FacturaRepository;
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
 * Tests unitarios para FacturaService
 */
@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Factura facturaTest;
    private Cliente clienteTest;

    @BeforeEach
    void setUp() {
        clienteTest = new Cliente();
        clienteTest.setId(1L);
        clienteTest.setNombre("Cliente Test");
        clienteTest.setCif("B12345678");

        facturaTest = new Factura();
        facturaTest.setId(1L);
        facturaTest.setNumero("FAC-2026-0001");
        facturaTest.setFecha(LocalDate.now());
        facturaTest.setCliente(clienteTest);
        facturaTest.setBaseImponible(new BigDecimal("100.00"));
        facturaTest.setIva(new BigDecimal("21.00"));
        facturaTest.setTotal(new BigDecimal("121.00"));
        facturaTest.setEstado("EMITIDA");
    }

    @Test
    void testFindAll() {
        // Given
        List<Factura> facturas = Arrays.asList(facturaTest);
        when(facturaRepository.findAll()).thenReturn(facturas);

        // When
        List<Factura> result = facturaService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FAC-2026-0001", result.get(0).getNumero());
        verify(facturaRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        // Given
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaTest));

        // When
        Optional<Factura> result = facturaService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("FAC-2026-0001", result.get().getNumero());
        assertEquals(new BigDecimal("121.00"), result.get().getTotal());
    }

    @Test
    void testSave() {
        // Given
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaTest);

        // When
        Factura result = facturaService.save(facturaTest);

        // Then
        assertNotNull(result);
        assertEquals("FAC-2026-0001", result.getNumero());
        assertEquals("EMITIDA", result.getEstado());
        verify(facturaRepository, times(1)).save(facturaTest);
    }

    @Test
    void testCalculoTotalFactura() {
        // Given
        BigDecimal baseImponible = new BigDecimal("100.00");
        BigDecimal iva = new BigDecimal("21.00");
        BigDecimal totalEsperado = new BigDecimal("121.00");

        // When
        facturaTest.setBaseImponible(baseImponible);
        facturaTest.setIva(iva);
        facturaTest.setTotal(baseImponible.add(iva));

        // Then
        assertEquals(totalEsperado, facturaTest.getTotal());
    }

    @Test
    void testFindByEstado() {
        // Given
        List<Factura> facturasEmitidas = Arrays.asList(facturaTest);
        when(facturaRepository.findByEstado("EMITIDA")).thenReturn(facturasEmitidas);

        // When
        List<Factura> result = facturaRepository.findByEstado("EMITIDA");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EMITIDA", result.get(0).getEstado());
    }

    @Test
    void testFindByFechaBetween() {
        // Given
        LocalDate desde = LocalDate.now().minusDays(7);
        LocalDate hasta = LocalDate.now();
        List<Factura> facturas = Arrays.asList(facturaTest);
        when(facturaRepository.findByFechaBetween(desde, hasta)).thenReturn(facturas);

        // When
        List<Factura> result = facturaRepository.findByFechaBetween(desde, hasta);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFecha().isAfter(desde.minusDays(1)));
        assertTrue(result.get(0).getFecha().isBefore(hasta.plusDays(1)));
    }
}

