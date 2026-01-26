package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.PlanCuentasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContabilidadServiceTest {

    @Mock
    AsientoContableRepository asientoRepository;

    @Mock
    PlanCuentasRepository cuentasRepository;

    @Mock
    AuditoriaService auditoriaService;

    @InjectMocks
    ContabilidadService contabilidadService;

    @Captor
    ArgumentCaptor<AsientoContable> asientoCaptor;

    @BeforeEach
    void setUp() {
        PlanCuentas cuentaClientes = new PlanCuentas();
        cuentaClientes.setCodigo("430");
        cuentaClientes.setNombre("Clientes");

        PlanCuentas cuentaVentas = new PlanCuentas();
        cuentaVentas.setCodigo("700");
        cuentaVentas.setNombre("Ventas");

        PlanCuentas cuentaIVA = new PlanCuentas();
        cuentaIVA.setCodigo("477");
        cuentaIVA.setNombre("IVA repercutido");

        when(cuentasRepository.findByCodigo("430")).thenReturn(Optional.of(cuentaClientes));
        when(cuentasRepository.findByCodigo("700")).thenReturn(Optional.of(cuentaVentas));
        when(cuentasRepository.findByCodigo("477")).thenReturn(Optional.of(cuentaIVA));

        // Añadir stubs para cuentas de caja/banco usadas en pagos
        PlanCuentas cuentaCaja = new PlanCuentas(); cuentaCaja.setCodigo("570"); cuentaCaja.setNombre("Caja");
        PlanCuentas cuentaBanco = new PlanCuentas(); cuentaBanco.setCodigo("572"); cuentaBanco.setNombre("Bancos");
        when(cuentasRepository.findByCodigo("570")).thenReturn(Optional.of(cuentaCaja));
        when(cuentasRepository.findByCodigo("572")).thenReturn(Optional.of(cuentaBanco));

        when(asientoRepository.findMaxNumeroByYear(anyString())).thenReturn(0);
        when(asientoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void generarAsientoFactura_creaYguardaAsiento() {
        Factura factura = new Factura();
        Cliente cliente = new Cliente();
        cliente.setNombre("ACME S.A.");
        factura.setCliente(cliente);
        factura.setNumero("FAC-2026-0001");
        factura.setFecha(java.time.LocalDate.now());
        factura.setBaseImponible(new BigDecimal("100.00"));
        factura.setTotalIva(new BigDecimal("21.00"));
        factura.setTotal(new BigDecimal("121.00"));

        AsientoContable asiento = contabilidadService.generarAsientoFactura(factura, null);

        assertNotNull(asiento);
        verify(asientoRepository).save(asientoCaptor.capture());
        AsientoContable capturado = asientoCaptor.getValue();
        assertEquals("FAC-2026-0001", factura.getNumero());
        assertTrue(capturado.getLineas().size() >= 3);
    }

    @Test
    void generarAsientoPago_paraFactura_guardaAsiento() {
        Factura factura = new Factura();
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente X");
        factura.setCliente(cliente);
        factura.setNumero("FAC-2026-0002");

        AsientoContable asiento = contabilidadService.generarAsientoPago(factura, new BigDecimal("50.00"), "EFECTIVO", null);
        assertNotNull(asiento);
        verify(asientoRepository).save(asientoCaptor.capture());
        AsientoContable capturado = asientoCaptor.getValue();
        assertTrue(capturado.getLineas().size() >= 2);
    }

    @Test
    void generarAsientoCompra_guardaAsiento() {
        FacturaCompra fc = new FacturaCompra();
        fc.setNumero("FC-100");
        fc.setBaseImponible(new BigDecimal("200.00"));
        fc.setImporteIva(new BigDecimal("42.00"));
        fc.setTotal(new BigDecimal("242.00"));
        fc.setId(123L);

        when(cuentasRepository.findByCodigo("600")).thenReturn(Optional.of(new PlanCuentas()));
        when(cuentasRepository.findByCodigo("472")).thenReturn(Optional.of(new PlanCuentas()));
        when(cuentasRepository.findByCodigo("400")).thenReturn(Optional.of(new PlanCuentas()));

        // configurar nombres y codigos para las cuentas creadas
        PlanCuentas pc600 = cuentasRepository.findByCodigo("600").orElseThrow(() -> new AssertionError("Cuenta 600 no stubbed"));
        pc600.setCodigo("600"); pc600.setNombre("Compras");
        PlanCuentas pc472 = cuentasRepository.findByCodigo("472").orElseThrow(() -> new AssertionError("Cuenta 472 no stubbed"));
        pc472.setCodigo("472"); pc472.setNombre("IVA Soportado");
        PlanCuentas pc400 = cuentasRepository.findByCodigo("400").orElseThrow(() -> new AssertionError("Cuenta 400 no stubbed"));
        pc400.setCodigo("400"); pc400.setNombre("Proveedores");

        AsientoContable asiento = contabilidadService.generarAsientoCompra(fc.getId(), fc.getBaseImponible(), fc.getImporteIva(), fc.getTotal(), null);
        assertNotNull(asiento);
        verify(asientoRepository).save(asientoCaptor.capture());
        AsientoContable capturado = asientoCaptor.getValue();
        assertTrue(capturado.getLineas().size() >= 3);
    }

    @Test
    void generarAsientoFactura_conUsuario_audita() {
        Factura factura = new Factura();
        Cliente cliente = new Cliente(); cliente.setNombre("Cliente Auditado");
        factura.setCliente(cliente);
        factura.setNumero("FAC-2026-0003");
        Usuario usuario = new Usuario(); usuario.setUsername("tester");

        contabilidadService.generarAsientoFactura(factura, usuario);

        verify(auditoriaService, atLeastOnce()).registrarAccion(eq(usuario), anyString(), anyString(), argThat(s -> s != null && s.toLowerCase().contains("factura")), anyString());
    }

    @Test
    void obtenerLibroDiario_devuelveAsientosEntreFechas() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);

        AsientoContable a1 = crearAsientoBalanceado(new BigDecimal("100.00"));
        a1.setFecha(LocalDate.of(2026,1,10));
        when(asientoRepository.findByFechaBetween(desde, hasta)).thenReturn(java.util.List.of(a1));

        var res = contabilidadService.obtenerLibroDiario(desde, hasta);
        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    void obtenerBalance_devuelveListaVacia_porAhora() {
        var res = contabilidadService.obtenerBalance(LocalDate.now());
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void validarCuadreContable_detectaAsientoDescuadrado() {
        AsientoContable balanceado = crearAsientoBalanceado(new BigDecimal("150.00"));
        AsientoContable descuadrado = crearAsientoNoBalanceado(new BigDecimal("100.00"), new BigDecimal("90.00"));

        when(asientoRepository.findAll()).thenReturn(java.util.List.of(balanceado, descuadrado));

        boolean valido = contabilidadService.validarCuadreContable();
        assertFalse(valido);

        // ahora solo balanceado
        when(asientoRepository.findAll()).thenReturn(java.util.List.of(balanceado));
        assertTrue(contabilidadService.validarCuadreContable());
    }

    // ===== helpers =====
    private AsientoContable crearAsientoBalanceado(BigDecimal importe) {
        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setNumero("TEST-" + System.nanoTime());
        java.util.Set<LineaAsiento> lineas = new java.util.LinkedHashSet<>();

        LineaAsiento l1 = new LineaAsiento();
        l1.setDebe(importe);
        l1.setHaber(BigDecimal.ZERO);
        l1.setAsiento(asiento);

        LineaAsiento l2 = new LineaAsiento();
        l2.setDebe(BigDecimal.ZERO);
        l2.setHaber(importe);
        l2.setAsiento(asiento);

        lineas.add(l1); lineas.add(l2);
        asiento.setLineas(lineas);
        asiento.calcularTotales();
        return asiento;
    }

    private AsientoContable crearAsientoNoBalanceado(BigDecimal debe, BigDecimal haber) {
        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setNumero("TEST-" + System.nanoTime());
        java.util.Set<LineaAsiento> lineas = new java.util.LinkedHashSet<>();

        LineaAsiento l1 = new LineaAsiento();
        l1.setDebe(debe);
        l1.setHaber(BigDecimal.ZERO);
        l1.setAsiento(asiento);

        LineaAsiento l2 = new LineaAsiento();
        l2.setDebe(BigDecimal.ZERO);
        l2.setHaber(haber);
        l2.setAsiento(asiento);

        lineas.add(l1); lineas.add(l2);
        asiento.setLineas(lineas);
        asiento.calcularTotales();
        return asiento;
    }
}
