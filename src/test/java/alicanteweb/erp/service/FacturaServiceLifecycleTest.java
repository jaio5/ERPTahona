package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.FacturaSerieSequenceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceLifecycleTest {

    @Mock
    FacturaRepository repository;

    @Mock
    FacturaSerieSequenceRepository sequenceRepository;

    @Mock
    VerifactuService verifactuService;

    @Mock
    FacturacionEventoService facturacionEventoService;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    FacturaService service;

    @Test
    void anularFactura_rechazaFacturasEmitidas() {
        Factura factura = new Factura();
        factura.setId(1L);
        factura.setNumero("F-GEN-2026-0001");
        factura.setEstado("EMITIDA");

        when(repository.findById(1L)).thenReturn(Optional.of(factura));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> service.anularFactura(1L, "prueba"));

        assertTrue(ex.getMessage().contains("rectificativa"));
        verify(repository, never()).save(any(Factura.class));
    }

    @Test
    void anularFactura_marcaAnuladaAntesDeEmision() {
        Factura factura = new Factura();
        factura.setId(2L);
        factura.setNumero("F-GEN-2026-0002");
        factura.setSerie("GEN");
        factura.setEstado("BORRADOR");

        when(repository.findById(2L)).thenReturn(Optional.of(factura));
        when(repository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Factura anulada = service.anularFactura(2L, "cancelada");

        assertEquals("ANULADA", anulada.getEstado());
        assertTrue(anulada.getObservacionesRevision().contains("cancelada"));
        verify(facturacionEventoService).registrarEvento(eq(FacturacionEventoService.AMBITO_FACTURAS), eq("ANULACION_PRE_EMISION"), eq("F-GEN-2026-0002"), any());
    }

    @Test
    void aprobarYEmitir_enviaYRegistraEvento() throws Exception {
        Factura factura = new Factura();
        factura.setId(3L);
        factura.setNumero("F-GEN-2026-0003");
        factura.setSerie("GEN");
        factura.setEstado("REVISION");
        factura.setFechaEmisionVerifactu(LocalDateTime.now());

        when(repository.findById(3L)).thenReturn(Optional.of(factura));
        when(verifactuService.isAeatAvailable()).thenReturn(true);
        when(repository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Factura emitida = service.aprobarYEmitir(3L);

        assertEquals(factura, emitida);
        verify(verifactuService).enviarFacturaVerifactu(factura);
        verify(facturacionEventoService).registrarEvento(eq(FacturacionEventoService.AMBITO_FACTURAS), eq("EMISION_FACTURA"), eq("F-GEN-2026-0003"), any());
    }
}
