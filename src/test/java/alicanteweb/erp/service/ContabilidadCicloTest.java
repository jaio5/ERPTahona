package alicanteweb.erp.service;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.entities.PlanCuentas;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContabilidadCicloTest {

    @Mock
    AsientoContableRepository asientoRepository;
    @Mock
    PlanCuentasRepository cuentasRepository;
    @Mock
    AuditoriaService auditoriaService;

    @InjectMocks
    ContabilidadService service;

    private PlanCuentas cuenta(String codigo, String nombre) {
        PlanCuentas c = new PlanCuentas();
        c.setCodigo(codigo);
        c.setNombre(nombre);
        c.setTipo("ACTIVO");
        c.setNivel(1);
        return c;
    }

    @Test
    void libroMayor_calculaSaldoAcumulado() {
        when(asientoRepository.movimientosDeCuenta(anyString(), any(), any())).thenReturn(List.of(
                new Object[]{LocalDate.of(2026, 1, 10), "A-001", "Factura", new BigDecimal("100.00"), BigDecimal.ZERO},
                new Object[]{LocalDate.of(2026, 2, 1), "A-002", "Cobro", BigDecimal.ZERO, new BigDecimal("60.00")}));

        var mayor = service.obtenerLibroMayor("430", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(2, mayor.size());
        assertEquals(0, mayor.get(0).saldo().compareTo(new BigDecimal("100.00")));
        assertEquals(0, mayor.get(1).saldo().compareTo(new BigDecimal("40.00")));
    }

    @Test
    void balancePgc_agrupaPorTipoYCalculaResultado() {
        when(asientoRepository.calcularBalanceHasta(any())).thenReturn(List.of(
                new Object[]{"430", "Clientes", "ACTIVO", new BigDecimal("100"), new BigDecimal("40")},
                new Object[]{"400", "Proveedores", "PASIVO", new BigDecimal("10"), new BigDecimal("50")},
                new Object[]{"700", "Ventas", "INGRESO", BigDecimal.ZERO, new BigDecimal("200")},
                new Object[]{"600", "Compras", "GASTO", new BigDecimal("120"), BigDecimal.ZERO}));

        var balance = service.obtenerBalancePgc(LocalDate.of(2026, 6, 30));

        assertEquals(0, balance.totalActivo().compareTo(new BigDecimal("60")));
        assertEquals(0, balance.totalPasivoYNeto().compareTo(new BigDecimal("40")));
        assertEquals(0, balance.totalIngresos().compareTo(new BigDecimal("200")));
        assertEquals(0, balance.totalGastos().compareTo(new BigDecimal("120")));
        assertEquals(0, balance.resultado().compareTo(new BigDecimal("80")));
        // Activo (60) = Pasivo+PN (40) + Resultado (80)? No: en este ejemplo sintético
        // solo se comprueba la suma reportada
        assertEquals(0, balance.totalPasivoYNetoConResultado().compareTo(new BigDecimal("120")));
    }

    @Test
    void asientoApertura_trasladaSaldosDeBalanceYCuadra() {
        when(asientoRepository.findByAsientoAperturaTrue()).thenReturn(List.of());
        when(asientoRepository.calcularBalanceHasta(LocalDate.of(2025, 12, 31))).thenReturn(List.of(
                new Object[]{"572", "Bancos", "ACTIVO", new BigDecimal("500"), new BigDecimal("100")},
                new Object[]{"400", "Proveedores", "PASIVO", BigDecimal.ZERO, new BigDecimal("150")},
                new Object[]{"700", "Ventas", "INGRESO", BigDecimal.ZERO, new BigDecimal("999")}));
        when(cuentasRepository.findByCodigo("572")).thenReturn(Optional.of(cuenta("572", "Bancos")));
        when(cuentasRepository.findByCodigo("400")).thenReturn(Optional.of(cuenta("400", "Proveedores")));
        when(cuentasRepository.findByCodigo("129")).thenReturn(Optional.of(cuenta("129", "Resultado")));
        when(asientoRepository.findMaxNumeroByYear(anyString())).thenReturn(0);
        when(asientoRepository.save(any(AsientoContable.class))).thenAnswer(inv -> inv.getArgument(0));

        AsientoContable apertura = service.generarAsientoApertura(2026);

        assertTrue(Boolean.TRUE.equals(apertura.getAsientoApertura()));
        assertEquals("APERTURA", apertura.getTipo());
        assertEquals(LocalDate.of(2026, 1, 1), apertura.getFecha());
        // 400 (activo) al debe, 150 (pasivo) al haber, diferencia 250 a resultado (129)
        assertEquals(3, apertura.getLineas().size());
        assertTrue(apertura.estaCuadrado());
    }

    @Test
    void asientoApertura_rechazaDuplicado() {
        AsientoContable existente = new AsientoContable();
        existente.setFecha(LocalDate.of(2026, 1, 1));
        when(asientoRepository.findByAsientoAperturaTrue()).thenReturn(List.of(existente));

        assertThrows(IllegalStateException.class, () -> service.generarAsientoApertura(2026));
    }
}
