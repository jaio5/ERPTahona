package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.repository.ArticuloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ArticuloService
 */
@ExtendWith(MockitoExtension.class)
class ArticuloServiceTest {

    @Mock
    private ArticuloRepository articuloRepository;

    @InjectMocks
    private ArticuloService articuloService;

    private Articulo articuloTest;

    @BeforeEach
    void setUp() {
        articuloTest = new Articulo();
        articuloTest.setId(1L);
        articuloTest.setNombre("Pan Integral");
        articuloTest.setPrecioVenta(new BigDecimal("2.50"));
        articuloTest.setPrecioCompra(new BigDecimal("1.50"));
        articuloTest.setStock(new BigDecimal("100"));
        articuloTest.setStockMinimo(new BigDecimal("10"));
        articuloTest.setIva(new BigDecimal("10"));
    }

    @Test
    void testFindAll() {
        // Given
        List<Articulo> articulos = Arrays.asList(articuloTest);
        when(articuloRepository.findAll()).thenReturn(articulos);

        // When
        List<Articulo> result = articuloService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pan Integral", result.get(0).getNombre());
    }

    @Test
    void testSave() {
        // Given
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articuloTest);

        // When
        Articulo result = articuloService.save(articuloTest);

        // Then
        assertNotNull(result);
        assertEquals("Pan Integral", result.getNombre());
        assertEquals(new BigDecimal("2.50"), result.getPrecioVenta());
        verify(articuloRepository, times(1)).save(articuloTest);
    }

    @Test
    void testVerificarStockBajo() {
        // Given - artículo con stock = 100, mínimo = 10
        // When
        boolean stockBajo = articuloTest.getStock().compareTo(articuloTest.getStockMinimo()) <= 0;

        // Then
        assertFalse(stockBajo, "Stock de 100 no debería ser considerado bajo");

        // Given - artículo con stock bajo
        articuloTest.setStock(new BigDecimal("5"));

        // When
        stockBajo = articuloTest.getStock().compareTo(articuloTest.getStockMinimo()) <= 0;

        // Then
        assertTrue(stockBajo, "Stock de 5 debería ser considerado bajo");
    }

    @Test
    void testCalculoMargen() {
        // Given
        BigDecimal precioVenta = new BigDecimal("2.50");
        BigDecimal precioCompra = new BigDecimal("1.50");

        // When
        BigDecimal margen = precioVenta.subtract(precioCompra);
        BigDecimal porcentajeMargen = margen.divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP)
                                           .multiply(new BigDecimal("100"));

        // Then
        assertEquals(new BigDecimal("1.00"), margen);
        assertTrue(porcentajeMargen.compareTo(new BigDecimal("66")) > 0); // Más del 66%
    }
}

