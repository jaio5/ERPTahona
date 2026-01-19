package alicanteweb.erp.service;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @BeforeEach
    public void setup() {
        // MockitoAnnotations.openMocks(this); handled by extension
    }

    @Test
    public void testGuardarGeneraCodigoSiFalta() {
        Proveedor p = new Proveedor();
        p.setNombre("Proveedor Test");
        p.setCodigo(null);

        // Simular repositorio con algunos códigos existentes
        Proveedor p1 = new Proveedor(); p1.setCodigo("PROV0001");
        Proveedor p2 = new Proveedor(); p2.setCodigo("PROV0005");
        when(proveedorRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        when(proveedorRepository.save(any())).thenAnswer(invocation -> {
            Proveedor arg = invocation.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        Proveedor saved = proveedorService.save(p);

        assertNotNull(saved.getId());
        assertEquals("PROV0006", saved.getCodigo());
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    public void testActivarYDesactivar() {
        Proveedor p = new Proveedor();
        p.setId(10L);
        p.setCodigo("PRV001");
        p.setActivo(false);

        when(proveedorRepository.findById(10L)).thenReturn(Optional.of(p));

        proveedorService.activar(10L);
        ArgumentCaptor<Proveedor> cap = ArgumentCaptor.forClass(Proveedor.class);
        verify(proveedorRepository, times(1)).save(cap.capture());
        assertTrue(cap.getValue().getActivo());

        // preparar para desactivar
        when(proveedorRepository.findById(10L)).thenReturn(Optional.of(cap.getValue()));
        proveedorService.desactivar(10L);
        verify(proveedorRepository, times(2)).save(cap.capture());
        assertFalse(cap.getValue().getActivo());
    }

    @Test
    public void testBuscarPorCriterio() {
        Proveedor p = new Proveedor(); p.setNombre("Proveedor X");
        when(proveedorRepository.buscarPorCriterio("X")).thenReturn(List.of(p));

        List<Proveedor> res = proveedorService.buscar("X");
        assertEquals(1, res.size());
        assertEquals("Proveedor X", res.get(0).getNombre());
        verify(proveedorRepository).buscarPorCriterio("X");
    }
}
