package alicanteweb.erp.service;

import alicanteweb.erp.entities.MovimientoCaja;
import alicanteweb.erp.repository.MovimientoCajaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoCajaServiceTest {

    @Mock
    MovimientoCajaRepository repository;

    @InjectMocks
    MovimientoCajaService service;

    @Test
    void registrarIngreso_creaMovimientoConTipoIngreso() {
        when(repository.save(any(MovimientoCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoCaja res = service.registrarIngreso("Venta mostrador", new BigDecimal("12.50"), "Ventas");

        assertEquals("INGRESO", res.getTipo());
        assertEquals(LocalDate.now(), res.getFecha());
        assertEquals("Venta mostrador", res.getConcepto());
        assertEquals(new BigDecimal("12.50"), res.getImporte());
        assertEquals("Ventas", res.getCategoria());
        verify(repository).save(res);
    }

    @Test
    void registrarGasto_creaMovimientoConTipoGasto() {
        when(repository.save(any(MovimientoCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoCaja res = service.registrarGasto("Compra bolsas", new BigDecimal("5.00"), "Material");

        assertEquals("GASTO", res.getTipo());
        assertEquals(LocalDate.now(), res.getFecha());
        assertEquals("Compra bolsas", res.getConcepto());
        verify(repository).save(res);
    }

    @Test
    void delegaConsultasAlRepositorio() {
        LocalDate desde = LocalDate.now().minusDays(1);
        LocalDate hasta = LocalDate.now();
        when(repository.findByFechaBetween(desde, hasta)).thenReturn(List.of(new MovimientoCaja()));
        when(repository.calcularSaldoCaja()).thenReturn(new BigDecimal("10.00"));

        assertEquals(1, service.findByFechaBetween(desde, hasta).size());
        assertEquals(new BigDecimal("10.00"), service.calcularSaldoActual());
        verify(repository).findByFechaBetween(desde, hasta);
        verify(repository).calcularSaldoCaja();
    }

    @Test
    void saveGuardaElMismoMovimiento() {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipo("INGRESO");
        movimiento.setConcepto("X");
        movimiento.setImporte(BigDecimal.ONE);
        when(repository.save(movimiento)).thenReturn(movimiento);

        assertSame(movimiento, service.save(movimiento));
        ArgumentCaptor<MovimientoCaja> captor = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(repository).save(captor.capture());
        assertSame(movimiento, captor.getValue());
    }
}
