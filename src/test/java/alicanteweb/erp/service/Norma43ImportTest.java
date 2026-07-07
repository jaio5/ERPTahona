package alicanteweb.erp.service;

import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.repository.BancoRepository;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.MovimientoBancoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Norma43ImportTest {

    @Mock
    MovimientoBancoRepository repository;
    @Mock
    BancoRepository bancoRepository;
    @Mock
    FacturaRepository facturaRepository;
    @Mock
    FacturaCompraRepository facturaCompraRepository;

    @InjectMocks
    MovimientoBancoService service;

    private static String reg22(String fechaOper, char debeHaber, String importeCentimos, String referencia) {
        // 1-2 tipo, 3-6 libre, 7-10 oficina, 11-16 fecha oper, 17-22 fecha valor,
        // 23-24 concepto común, 25-27 concepto propio, 28 debe/haber, 29-42 importe,
        // 43-52 documento, 53-64 referencia1, 65-80 referencia2
        return "22" + "0000" + "1234" + fechaOper + fechaOper + "12" + "345"
                + debeHaber + String.format("%14s", importeCentimos).replace(' ', '0')
                + "0000000000" + String.format("%-12s", referencia) + String.format("%-16s", "");
    }

    @Test
    void importarNorma43_creaMovimientosConSignoYConcepto() {
        Banco banco = new Banco();
        banco.setId(1L);
        when(bancoRepository.findById(1L)).thenReturn(Optional.of(banco));
        when(repository.save(any(MovimientoBanco.class))).thenAnswer(inv -> inv.getArgument(0));

        String contenido = String.join("\r\n",
                "11" + "0000" + "1234" + "0000012345" + "260701" + "260702" + "2" + "00000000010000" + "978" + "0",
                reg22("260701", '2', "1250", "REM-1"),
                "2301CONCEPTO COMPLEMENTARIO INGRESO",
                reg22("260702", '1', "3000", "RECIBO LUZ"),
                "33" + "0000" + "1234",
                "88" + "999999999999999999");
        MockMultipartFile archivo = new MockMultipartFile("archivo", "extracto.n43",
                "text/plain", contenido.getBytes(StandardCharsets.ISO_8859_1));

        var resultado = service.importarNorma43(1L, archivo);

        assertEquals(2, resultado.importados());
        assertTrue(resultado.errores().isEmpty(), "sin errores: " + resultado.errores());

        var captor = org.mockito.ArgumentCaptor.forClass(MovimientoBanco.class);
        verify(repository, times(2)).save(captor.capture());
        List<MovimientoBanco> movimientos = captor.getAllValues();

        MovimientoBanco ingreso = movimientos.get(0);
        assertEquals("INGRESO", ingreso.getTipo());
        assertEquals(0, ingreso.getImporte().compareTo(new BigDecimal("12.50")));
        assertEquals(LocalDate.of(2026, 7, 1), ingreso.getFecha());
        assertEquals("CONCEPTO COMPLEMENTARIO INGRESO", ingreso.getConcepto());

        MovimientoBanco gasto = movimientos.get(1);
        assertEquals("GASTO", gasto.getTipo());
        assertEquals(0, gasto.getImporte().compareTo(new BigDecimal("30.00")));
        assertEquals("RECIBO LUZ", gasto.getConcepto());
        assertEquals(Boolean.FALSE, gasto.getConciliado());
    }

    @Test
    void importarNorma43_rechazaFicheroNoN43() {
        Banco banco = new Banco();
        banco.setId(1L);
        when(bancoRepository.findById(1L)).thenReturn(Optional.of(banco));
        MockMultipartFile archivo = new MockMultipartFile("archivo", "otro.txt",
                "text/plain", "fecha;concepto;importe\n01/01/2026;x;1".getBytes(StandardCharsets.ISO_8859_1));

        var resultado = service.importarNorma43(1L, archivo);

        assertEquals(0, resultado.importados());
        assertFalse(resultado.errores().isEmpty());
    }
}
