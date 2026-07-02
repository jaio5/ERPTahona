package alicanteweb.erp.service;

import alicanteweb.erp.repository.AlbaranSerieSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbaranNumeroServiceTest {

    @Mock AlbaranSerieSequenceRepository repository;
    AlbaranNumeroService service;

    @BeforeEach
    void setUp() {
        service = new AlbaranNumeroService(repository);
    }

    @Test
    void reservaAtomicamenteYFormateaConSeisDigitos() {
        when(repository.reservarSiguiente("ALB", 2027)).thenReturn(2);
        when(repository.obtenerNumeroReservado()).thenReturn(42L);

        String numero = service.generarNumero(LocalDate.of(2027, 3, 1));

        assertEquals("ALB-2027-000042", numero);
        InOrder orden = inOrder(repository);
        orden.verify(repository).reservarSiguiente("ALB", 2027);
        orden.verify(repository).obtenerNumeroReservado();
    }

    @Test
    void usaEjercicioActualCuandoLaFechaEsNula() {
        when(repository.reservarSiguiente("ALB", LocalDate.now().getYear())).thenReturn(1);

        String numero = service.generarNumero(null);

        assertEquals("ALB-" + LocalDate.now().getYear() + "-000001", numero);
    }
}
