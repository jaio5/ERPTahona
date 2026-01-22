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
public class AlmacenValidationTest {

    @Mock
    private AlmacenRepository repository;

    @InjectMocks
    private AlmacenService service;

    @BeforeEach
    void setUp() {}

    @Test
    void save_disponibleGreaterThanCapacidad_shouldThrow() {
        Almacen a = new Almacen();
        a.setCodigo("VAL1");
        a.setNombre("Test");
        a.setCapacidad(new BigDecimal("10.00"));
        a.setDisponible(new BigDecimal("20.00"));

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(a));
        assertTrue(ex.getMessage().toLowerCase().contains("disponible"));
        verify(repository, never()).save(any());
    }
}
