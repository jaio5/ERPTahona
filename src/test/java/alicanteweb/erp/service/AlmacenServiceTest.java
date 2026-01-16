package alicanteweb.erp.service;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.repository.AlmacenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlmacenServiceTest {

    @Mock
    private AlmacenRepository repository;

    @InjectMocks
    private AlmacenService service;

    @BeforeEach
    void setUp() {
        // Mockito inicializa mocks
    }

    @Test
    void save_validAlmacen_callsRepository() {
        Almacen a = new Almacen();
        a.setCodigo("ALM01");
        a.setNombre("Principal");
        a.setCapacidad(new BigDecimal("100.00"));
        a.setDisponible(new BigDecimal("50.00"));

        when(repository.save(any(Almacen.class))).thenAnswer(inv -> inv.getArgument(0));

        Almacen saved = service.save(a);

        assertNotNull(saved);
        verify(repository, times(1)).save(a);
    }

    @Test
    void save_negativeCapacity_throws() {
        Almacen a = new Almacen();
        a.setCodigo("ALM02");
        a.setNombre("Bad");
        a.setCapacidad(new BigDecimal("-1"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(a));
        assertTrue(ex.getMessage().contains("capacidad"));
        verify(repository, never()).save(any());
    }

    @Test
    void save_negativeDisponible_throws() {
        Almacen a = new Almacen();
        a.setCodigo("ALM03");
        a.setNombre("Bad2");
        a.setDisponible(new BigDecimal("-5"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(a));
        assertTrue(ex.getMessage().contains("disponible"));
        verify(repository, never()).save(any());
    }

    @Test
    void save_disponibleGreaterThanCapacidad_throws() {
        Almacen a = new Almacen();
        a.setCodigo("ALM04");
        a.setNombre("Bad3");
        a.setCapacidad(new BigDecimal("10.00"));
        a.setDisponible(new BigDecimal("20.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(a));
        assertTrue(ex.getMessage().toLowerCase().contains("disponible"));
        verify(repository, never()).save(any());
    }
}
