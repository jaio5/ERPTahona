package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsientoAutomaticoServiceTest {

    @Mock
    AsientoContableRepository asientoRepository;

    @Mock
    PlanCuentasRepository planCuentasRepository;

    @Mock
    ContabilidadService contabilidadService;

    @InjectMocks
    AsientoAutomaticoService service;

    @BeforeEach
    void setUp() {
        lenient().when(planCuentasRepository.findByCodigo(anyString())).thenReturn(Optional.of(new PlanCuentas()));
        lenient().when(asientoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void generarAsientoFacturaVenta_delegaAContabilidad() {
        Factura f = new Factura();
        f.setNumero("F-1");
        f.setFecha(LocalDate.now());
        when(contabilidadService.generarAsientoFactura(eq(f), isNull())).thenReturn(new AsientoContable());

        AsientoContable a = service.generarAsientoFacturaVenta(f);
        assertNotNull(a);
        verify(contabilidadService).generarAsientoFactura(eq(f), isNull());
    }

    @Test
    void generarAsientoCobroFactura_delegaAContabilidad() {
        Factura f = new Factura(); f.setNumero("F-2");
        when(contabilidadService.generarAsientoPago(eq(f), any(), anyString(), isNull())).thenReturn(new AsientoContable());

        AsientoContable a = service.generarAsientoCobroFactura(f, null, BigDecimal.TEN);
        assertNotNull(a);
        verify(contabilidadService).generarAsientoPago(eq(f), eq(BigDecimal.TEN), anyString(), isNull());
    }

    @Test
    void generarAsientoCompra_delegaAContabilidad() {
        FacturaCompra fc = new FacturaCompra(); fc.setId(5L); fc.setBaseImponible(new BigDecimal("100")); fc.setImporteIva(new BigDecimal("21")); fc.setTotal(new BigDecimal("121"));
        when(contabilidadService.generarAsientoCompra(eq(5L), any(), any(), any(), isNull())).thenReturn(new AsientoContable());

        AsientoContable a = service.generarAsientoCompra(fc);
        assertNotNull(a);
        verify(contabilidadService).generarAsientoCompra(eq(5L), any(), any(), any(), isNull());
    }

    @Test
    void generarAsientoPagoProveedor_guardarAsiento() {
        FacturaCompra fc = new FacturaCompra(); fc.setNumero("FC-1"); fc.setTotal(new BigDecimal("50"));
        Proveedor p = new Proveedor(); p.setNombre("Prov"); fc.setProveedor(p);

        // planCuentasRepository.findByCodigo ya stubbed en setUp
        AsientoContable a = service.generarAsientoPagoProveedor(fc, null);
        assertNotNull(a);
        verify(asientoRepository).save(any(AsientoContable.class));
    }

    @Test
    void generarAsientoMovimientoCaja_guardarAsiento() {
        MovimientoCaja m = new MovimientoCaja(); m.setConcepto("Venta"); m.setTipo("INGRESO"); m.setImporte(new BigDecimal("20"));
        AsientoContable a = service.generarAsientoMovimientoCaja(m);
        assertNotNull(a);
        verify(asientoRepository).save(any(AsientoContable.class));
    }

    @Test
    void validarAsientoCuadrado_funciona() {
        AsientoContable asiento = new AsientoContable();
        LineaAsiento l1 = new LineaAsiento(); l1.setDebe(new BigDecimal("10")); l1.setHaber(BigDecimal.ZERO); l1.setAsiento(asiento);
        LineaAsiento l2 = new LineaAsiento(); l2.setDebe(BigDecimal.ZERO); l2.setHaber(new BigDecimal("10")); l2.setAsiento(asiento);
        java.util.Set<LineaAsiento> s = new java.util.LinkedHashSet<>(); s.add(l1); s.add(l2);
        asiento.setLineas(s);

        assertTrue(service.validarAsientoCuadrado(asiento));
    }
}
