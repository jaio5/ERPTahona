package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Modelo347ServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaCompraRepository facturaCompraRepository;

    private Modelo347Service service;

    private Cliente cliente;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        service = new Modelo347Service(facturaRepository, facturaCompraRepository);
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCif("B11111111");
        cliente.setNombre("Cliente 347");
        proveedor = new Proveedor();
        proveedor.setId(2L);
        proveedor.setCif("B22222222");
        proveedor.setNombre("Proveedor 347");
    }

    private Factura facturaVenta(String numero, LocalDate fecha, String base, String iva, String total) {
        Factura f = new Factura();
        f.setNumero(numero);
        f.setFecha(fecha);
        f.setCliente(cliente);
        f.setBaseImponible(new BigDecimal(base));
        f.setTotalIva(new BigDecimal(iva));
        f.setTotal(new BigDecimal(total));
        f.setEstado("EMITIDA");
        return f;
    }

    private FacturaCompra facturaCompra(String numero, LocalDate fecha, String total, String estado) {
        FacturaCompra fc = new FacturaCompra();
        fc.setNumero(numero);
        fc.setFecha(fecha);
        fc.setProveedor(proveedor);
        fc.setBaseImponible(new BigDecimal(total).divide(new BigDecimal("1.21"), 2, java.math.RoundingMode.HALF_UP));
        fc.setImporteIva(BigDecimal.ZERO);
        fc.setTotal(new BigDecimal(total));
        fc.setEstado(estado);
        return fc;
    }

    @Test
    void soloIncluyeFacturasEmitidasYDesglosaTrimestres() {
        // El servicio consulta solo EMITIDAS: los borradores/anuladas quedan fuera vía query
        when(facturaRepository.findByFechaBetweenAndEstado(any(), any(), eq("EMITIDA")))
                .thenReturn(List.of(
                        facturaVenta("F-1", LocalDate.of(2025, 2, 10), "1000.00", "100.00", "1100.00"),
                        facturaVenta("F-2", LocalDate.of(2025, 8, 5), "2000.00", "200.00", "2200.00")
                ));
        when(facturaCompraRepository.findByFechaBetween(any(), any())).thenReturn(List.of());

        var resultado = service.generarModelo347(2025);

        assertEquals(1, resultado.operaciones().size());
        var op = resultado.operaciones().get(0);
        assertEquals("B", op.claveOperacion());
        assertEquals(new BigDecimal("3300.00"), op.totalDeclarar());
        assertEquals(new BigDecimal("1100.00"), op.trimestre1());
        assertEquals(BigDecimal.ZERO, op.trimestre2());
        assertEquals(new BigDecimal("2200.00"), op.trimestre3());
        assertEquals(BigDecimal.ZERO, op.trimestre4());
        assertTrue(service.validarModelo347(resultado).isEmpty());
    }

    @Test
    void porDebajoDelUmbralNoSeDeclara() {
        when(facturaRepository.findByFechaBetweenAndEstado(any(), any(), eq("EMITIDA")))
                .thenReturn(List.of(facturaVenta("F-1", LocalDate.of(2025, 2, 10), "1000.00", "100.00", "1100.00")));
        when(facturaCompraRepository.findByFechaBetween(any(), any())).thenReturn(List.of());

        var resultado = service.generarModelo347(2025);

        assertTrue(resultado.operaciones().isEmpty());
        assertEquals(0, resultado.totalDeclarantes());
    }

    @Test
    void incluyeComprasConClaveAyExcluyeAnuladas() {
        when(facturaRepository.findByFechaBetweenAndEstado(any(), any(), eq("EMITIDA"))).thenReturn(List.of());
        when(facturaCompraRepository.findByFechaBetween(any(), any()))
                .thenReturn(List.of(
                        facturaCompra("FC-1", LocalDate.of(2025, 4, 1), "4000.00", "PENDIENTE"),
                        facturaCompra("FC-2", LocalDate.of(2025, 5, 1), "9999.00", "ANULADA")
                ));

        var resultado = service.generarModelo347(2025);

        assertEquals(1, resultado.operaciones().size());
        var op = resultado.operaciones().get(0);
        assertEquals("A", op.claveOperacion());
        assertEquals("PROVEEDOR", op.tipo());
        assertEquals(new BigDecimal("4000.00"), op.totalDeclarar());
        assertEquals(new BigDecimal("4000.00"), op.trimestre2());
    }

    @Test
    void ficheroBoeIncluyeClaveYDesgloseTrimestral() {
        when(facturaRepository.findByFechaBetweenAndEstado(any(), any(), eq("EMITIDA")))
                .thenReturn(List.of(facturaVenta("F-1", LocalDate.of(2025, 11, 20), "4000.00", "400.00", "4400.00")));
        when(facturaCompraRepository.findByFechaBetween(any(), any())).thenReturn(List.of());

        var resultado = service.generarModelo347(2025);
        var declarante = new Modelo347Service.DatosDeclarante("B99999999", "Tahona", "965123456", "Contacto", 2025);
        String fichero = service.generarFicheroBOE(resultado, declarante);

        String[] lineas = fichero.split("\r\n");
        assertEquals(2, lineas.length);
        assertEquals(500, lineas[0].length());
        assertEquals(500, lineas[1].length());
        // Posición 60 (índice 59): clave de operación
        assertEquals('B', lineas[1].charAt(59));
        // Desglose trimestral en posiciones 76-135: 4T con importe (440000 céntimos)
        String registro2 = lineas[1];
        String t4 = registro2.substring(120, 135);
        assertTrue(t4.contains("440000"), "El 4T debe contener el importe en céntimos: " + t4);
    }
}
