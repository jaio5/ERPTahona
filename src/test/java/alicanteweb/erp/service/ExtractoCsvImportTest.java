package alicanteweb.erp.service;

import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.repository.BancoRepository;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.MovimientoBancoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractoCsvImportTest {

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

    /**
     * El importe del CSV debe conservarse como decimal exacto (dos posiciones) y el
     * signo determina el tipo. Guarda la corrección de parsear directamente a
     * {@link BigDecimal} en lugar de pasar por {@code double}, que puede introducir
     * errores de representación en importes grandes.
     */
    @Test
    void importarDesdeCSV_conservaImporteExactoYSigno() {
        Banco banco = new Banco();
        banco.setId(1L);
        when(bancoRepository.findById(1L)).thenReturn(Optional.of(banco));
        when(repository.save(any(MovimientoBanco.class))).thenAnswer(inv -> inv.getArgument(0));

        String contenido = String.join("\n",
                "fecha;concepto;importe",
                "02/07/2026;Venta pan;1234.56",
                "03/07/2026;Compra harina;-89.90");
        MockMultipartFile archivo = new MockMultipartFile("archivo", "extracto.csv",
                "text/csv", contenido.getBytes(StandardCharsets.UTF_8));

        var resultado = service.importarDesdeCSV(1L, archivo);

        assertEquals(2, resultado.importados());
        assertTrue(resultado.errores().isEmpty(), "sin errores: " + resultado.errores());

        ArgumentCaptor<MovimientoBanco> captor = ArgumentCaptor.forClass(MovimientoBanco.class);
        verify(repository, times(2)).save(captor.capture());
        List<MovimientoBanco> movimientos = captor.getAllValues();

        MovimientoBanco ingreso = movimientos.get(0);
        assertEquals("INGRESO", ingreso.getTipo());
        assertEquals(new BigDecimal("1234.56"), ingreso.getImporte());

        MovimientoBanco gasto = movimientos.get(1);
        assertEquals("GASTO", gasto.getTipo());
        assertEquals(new BigDecimal("89.90"), gasto.getImporte());
    }
}
