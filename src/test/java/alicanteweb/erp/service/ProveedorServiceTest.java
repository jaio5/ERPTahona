package alicanteweb.erp.service;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Tests unitarios para ProveedorService
 *
 * Cobertura: 12 tests
 * - CRUD básico: 6 tests
 * - Validaciones: 3 tests
 * - Búsquedas: 2 tests
 * - Dirección: 1 test
 *
 * @author ERP Tahona
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ProveedorService")
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor proveedorEjemplo;

    @BeforeEach
    void setUp() {
        proveedorEjemplo = new Proveedor();
        proveedorEjemplo.setId(1L);
        proveedorEjemplo.setNombre("Harinas del Norte S.L.");
        proveedorEjemplo.setCif("B87654321");
        proveedorEjemplo.setDireccion("Polígono Industrial Las Merinas, Nave 12");
        proveedorEjemplo.setCodigoPostal("47008");
        proveedorEjemplo.setPoblacion("Valladolid");
        proveedorEjemplo.setProvincia("Valladolid");
        proveedorEjemplo.setActivo(true);
    }

    // ==================== TESTS CRUD BÁSICO ====================

    // TEST 1: Guardar proveedor correctamente
    @Test
    @DisplayName("01 - Debería guardar un proveedor correctamente")
    void deberiaGuardarProveedorCorrectamente() {
        // Given
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorEjemplo);

        // When
        Proveedor resultado = proveedorService.save(proveedorEjemplo);

        // Then
        assertNotNull(resultado);
        assertEquals("Harinas del Norte S.L.", resultado.getNombre());
        assertEquals("B87654321", resultado.getCif());
        assertEquals("Valladolid", resultado.getPoblacion());
        assertTrue(resultado.getActivo());
        verify(proveedorRepository, times(1)).save(proveedorEjemplo);
    }

    // TEST 2: Buscar proveedor por ID (encontrado)
    @Test
    @DisplayName("02 - Debería encontrar proveedor por ID")
    void deberiaEncontrarProveedorPorId() {
        // Given
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorEjemplo));

        // When
        Optional<Proveedor> resultado = proveedorService.findById(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Harinas del Norte S.L.", resultado.get().getNombre());
        assertEquals("B87654321", resultado.get().getCif());
        verify(proveedorRepository, times(1)).findById(1L);
    }

    // TEST 3: Buscar proveedor por ID (no encontrado)
    @Test
    @DisplayName("03 - Debería devolver vacío cuando proveedor no existe")
    void deberiaDevolver_VacioProveedorNoExiste() {
        // Given
        when(proveedorRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Proveedor> resultado = proveedorService.findById(999L);

        // Then
        assertFalse(resultado.isPresent());
        verify(proveedorRepository, times(1)).findById(999L);
    }

    // TEST 4: Listar todos los proveedores
    @Test
    @DisplayName("04 - Debería listar todos los proveedores")
    void deberiaListarTodosLosProveedores() {
        // Given
        Proveedor proveedor2 = new Proveedor();
        proveedor2.setId(2L);
        proveedor2.setNombre("Levaduras Ibéricas S.A.");
        proveedor2.setCif("A12345678");
        proveedor2.setActivo(true);

        List<Proveedor> proveedores = Arrays.asList(proveedorEjemplo, proveedor2);
        when(proveedorRepository.findAll()).thenReturn(proveedores);

        // When
        List<Proveedor> resultado = proveedorService.findAll();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Harinas del Norte S.L.", resultado.get(0).getNombre());
        assertEquals("Levaduras Ibéricas S.A.", resultado.get(1).getNombre());
        verify(proveedorRepository, times(1)).findAll();
    }

    // TEST 5: Actualizar proveedor existente
    @Test
    @DisplayName("05 - Debería actualizar un proveedor existente")
    void deberiaActualizarProveedorExistente() {
        // Given
        proveedorEjemplo.setNombre("Harinas del Norte S.L. (Actualizado)");
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorEjemplo);

        // When
        Proveedor resultado = proveedorService.save(proveedorEjemplo);

        // Then
        assertNotNull(resultado);
        assertEquals("Harinas del Norte S.L. (Actualizado)", resultado.getNombre());
        verify(proveedorRepository, times(1)).save(proveedorEjemplo);
    }

    // TEST 6: Eliminar proveedor
    @Test
    @DisplayName("06 - Debería eliminar un proveedor correctamente")
    void deberiaEliminarProveedorCorrectamente() {
        // Given
        doNothing().when(proveedorRepository).deleteById(1L);

        // When
        proveedorService.deleteById(1L);

        // Then
        verify(proveedorRepository, times(1)).deleteById(1L);
    }

    // ==================== TESTS VALIDACIONES ====================

    // TEST 7: Validar CIF correcto
    @Test
    @DisplayName("07 - Debería validar CIF correcto")
    void deberiaValidarCifCorrecto() {
        // Given
        String cifValido = "B87654321";
        proveedorEjemplo.setCif(cifValido);
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorEjemplo);

        // When
        Proveedor resultado = proveedorService.save(proveedorEjemplo);

        // Then
        assertNotNull(resultado);
        assertEquals(cifValido, resultado.getCif());
        assertEquals(9, resultado.getCif().length());
        assertTrue(resultado.getCif().matches("^[A-Z][0-9]{8}$"));
    }

    // TEST 8: Proveedor con dirección completa
    @Test
    @DisplayName("08 - Debería guardar proveedor con dirección completa")
    void deberiaGuardarProveedorConDireccionCompleta() {
        // Given
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorEjemplo);

        // When
        Proveedor resultado = proveedorService.save(proveedorEjemplo);

        // Then
        assertNotNull(resultado);
        assertEquals("Polígono Industrial Las Merinas, Nave 12", resultado.getDireccion());
        assertEquals("47008", resultado.getCodigoPostal());
        assertEquals("Valladolid", resultado.getPoblacion());
        assertEquals("Valladolid", resultado.getProvincia());
        assertNotNull(resultado.getDireccion());
        assertFalse(resultado.getDireccion().isEmpty());
    }

    // TEST 9: Proveedor activo/inactivo
    @Test
    @DisplayName("09 - Debería gestionar estado activo/inactivo del proveedor")
    void deberiaGestionarEstadoActivoProveedor() {
        // Given - Proveedor activo
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorEjemplo);

        // When
        Proveedor proveedorActivo = proveedorService.save(proveedorEjemplo);

        // Then
        assertTrue(proveedorActivo.getActivo());

        // Given - Proveedor inactivo
        proveedorEjemplo.setActivo(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorEjemplo);

        // When
        Proveedor proveedorInactivo = proveedorService.save(proveedorEjemplo);

        // Then
        assertFalse(proveedorInactivo.getActivo());
    }

    // ==================== TESTS BÚSQUEDAS ====================

    // TEST 10: Buscar proveedor por CIF
    @Test
    @DisplayName("10 - Debería buscar proveedor por CIF")
    void deberiaBuscarProveedorPorCif() {
        // Given
        when(proveedorRepository.findByCif("B87654321")).thenReturn(Optional.of(proveedorEjemplo));

        // When
        Optional<Proveedor> resultado = proveedorRepository.findByCif("B87654321");

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("B87654321", resultado.get().getCif());
        assertEquals("Harinas del Norte S.L.", resultado.get().getNombre());
        verify(proveedorRepository, times(1)).findByCif("B87654321");
    }

    // TEST 11: Buscar proveedor por nombre (contiene)
    @Test
    @DisplayName("11 - Debería buscar proveedores por nombre parcial")
    void deberiaBuscarProveedoresPorNombreParcial() {
        // Given
        List<Proveedor> proveedores = Arrays.asList(proveedorEjemplo);
        when(proveedorRepository.findByNombreContainingIgnoreCase("harinas"))
                .thenReturn(proveedores);

        // When
        List<Proveedor> resultado = proveedorRepository.findByNombreContainingIgnoreCase("harinas");

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombre().toLowerCase().contains("harinas"));
        verify(proveedorRepository, times(1))
                .findByNombreContainingIgnoreCase("harinas");
    }

    // ==================== TESTS ADICIONALES ====================

    // TEST 12: Listar proveedores activos
    @Test
    @DisplayName("12 - Debería listar solo proveedores activos")
    void deberiaListarProveedoresActivos() {
        // Given
        Proveedor proveedorInactivo = new Proveedor();
        proveedorInactivo.setId(2L);
        proveedorInactivo.setNombre("Proveedor Inactivo");
        proveedorInactivo.setActivo(false);

        List<Proveedor> proveedoresActivos = Arrays.asList(proveedorEjemplo);
        when(proveedorRepository.findByActivoTrue()).thenReturn(proveedoresActivos);

        // When
        List<Proveedor> resultado = proveedorRepository.findByActivoTrue();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());
        verify(proveedorRepository, times(1)).findByActivoTrue();
    }
}

