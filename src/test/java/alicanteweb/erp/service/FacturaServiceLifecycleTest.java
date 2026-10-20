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
    FiscalComplianceService fiscalComplianceService;

    @Mock
    ContabilidadService contabilidadService;

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
        when(repository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Factura emitida = service.aprobarYEmitir(3L);

        assertEquals(factura, emitida);
        verify(fiscalComplianceService).exigirListoParaEmision();
        verify(verifactuService).enviarFacturaVerifactu(factura);
        verify(facturacionEventoService).registrarEvento(eq(FacturacionEventoService.AMBITO_FACTURAS), eq("EMISION_FACTURA"), eq("F-GEN-2026-0003"), any());
    }

    @Test
    void save_rechazaModificarFacturaEmitida() {
        Factura existente = new Factura();
        existente.setId(4L);
        existente.setEstado("EMITIDA");

        Factura cambios = new Factura();
        cambios.setId(4L);
        cambios.setEstado("BORRADOR");

        when(repository.findById(4L)).thenReturn(Optional.of(existente));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.save(cambios));

        assertTrue(ex.getMessage().contains("rectificativa"));
        verify(repository, never()).save(cambios);
    }

    @Test
    void deleteById_rechazaEliminarFacturaEnviada() {
        Factura existente = new Factura();
        existente.setId(5L);
        existente.setEstado("REVISION");
        existente.setVerifactuEnviada(true);

        when(repository.findById(5L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> service.deleteById(5L));
        verify(repository, never()).deleteById(5L);
    }

    // ===== Contención VERI*FACTU F0 =====

    /** Una factura cobrada (PAGADA) pero no remitida a la AEAT sigue protegida por su estado. */
    @Test
    void save_rechazaModificarFacturaExpedidaAunqueNoSeHayaRemitido() {
        Factura existente = new Factura();
        existente.setId(6L);
        existente.setEstado("PAGADA");
        existente.setVerifactuEnviada(false);
        Factura cambios = new Factura();
        cambios.setId(6L);
        cambios.setEstado("BORRADOR");
        when(repository.findById(6L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> service.save(cambios));
        verify(repository, never()).save(cambios);
    }

    @Test
    void deleteById_rechazaEliminarFacturaExpedidaNoRemitida() {
        Factura existente = new Factura();
        existente.setId(7L);
        existente.setEstado("EMITIDA");
        existente.setVerifactuEnviada(false);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> service.deleteById(7L));
        verify(repository, never()).deleteById(7L);
    }

    @Test
    void anularFactura_rechazaFacturasVencidasOCobradasAunqueNoSeHayanRemitido() {
        for (String estado : new String[]{"PAGADA", "VENCIDA", "RECTIFICADA"}) {
            Factura factura = new Factura();
            factura.setId(8L);
            factura.setEstado(estado);
            factura.setVerifactuEnviada(false);
            when(repository.findById(8L)).thenReturn(Optional.of(factura));

            assertThrows(IllegalStateException.class, () -> service.anularFactura(8L, "x"), estado);
        }
        verify(repository, never()).save(any(Factura.class));
    }

    private Factura originalEmitida() {
        Factura original = new Factura();
        original.setId(20L);
        original.setNumero("F-GEN-2026-0020");
        original.setSerie("GEN");
        original.setFecha(java.time.LocalDate.of(2026, 7, 1));
        original.setEstado("EMITIDA");
        original.setTipoFactura("ORDINARIA");
        alicanteweb.erp.entities.FacturaLinea linea = new alicanteweb.erp.entities.FacturaLinea();
        linea.setDescripcion("PAN");
        linea.setCantidad(new java.math.BigDecimal("2"));
        linea.setPrecioUnitario(new java.math.BigDecimal("1.00"));
        linea.setIva(new java.math.BigDecimal("4"));
        linea.setFactura(original);
        original.getFacturaLineas().add(linea);
        return original;
    }

    /**
     * Una rectificativa corrige la original, no la anula: la original sigue EMITIDA (en el libro
     * de IVA y en el 347), no se genera registro de anulación VERI*FACTU y solo se anota la referencia.
     */
    @Test
    void crearRectificativa_noAnulaLaOriginalNiGeneraRegistroDeAnulacion() {
        Factura original = originalEmitida();
        alicanteweb.erp.entities.FacturaSerieSequence secuencia = new alicanteweb.erp.entities.FacturaSerieSequence();
        secuencia.setSerie("GEN");
        secuencia.setEjercicio(2026);
        secuencia.setUltimoNumero(0L);
        when(repository.findByIdWithPdfData(20L)).thenReturn(Optional.of(original));
        when(sequenceRepository.findBySerieAndEjercicio("GEN", 2026)).thenReturn(Optional.of(secuencia));
        when(repository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura f = invocation.getArgument(0);
            if (f.getId() == null) f.setId(21L);
            return f;
        });

        Factura rectificativa = service.crearRectificativa(20L, "Precio erróneo", "DIFERENCIAS",
                java.time.LocalDate.of(2026, 7, 10));

        assertEquals("EMITIDA", original.getEstado(), "La original no se anula");
        assertTrue(original.getObservacionesRevision().contains("Rectificada por " + rectificativa.getNumero()));
        assertEquals("RECTIFICATIVA", rectificativa.getTipoFactura());
        assertEquals("F-GEN-2026-0020", rectificativa.getFacturaRectificadaNumero());
        assertTrue(rectificativa.getNumero().startsWith("R-GEN-2026-"));
        assertEquals(new java.math.BigDecimal("-2.08"), rectificativa.getTotal().setScale(2));
        org.mockito.Mockito.verifyNoInteractions(verifactuService);
        verify(facturacionEventoService).registrarEvento(eq(FacturacionEventoService.AMBITO_FACTURAS),
                eq("RECTIFICATIVA_CREADA"), eq(rectificativa.getNumero()), any());
        verify(facturacionEventoService, never()).registrarEvento(any(), eq("ANULACION_POR_RECTIFICATIVA"), any(), any());
    }

    @Test
    void crearRectificativa_soloSobreFacturasEmitidas() {
        Factura borrador = originalEmitida();
        borrador.setEstado("BORRADOR");
        when(repository.findByIdWithPdfData(20L)).thenReturn(Optional.of(borrador));

        assertThrows(IllegalStateException.class,
                () -> service.crearRectificativa(20L, "x", "DIFERENCIAS", null));
        verify(repository, never()).save(any(Factura.class));
    }

    /**
     * Como la original sigue EMITIDA, nada impedía crear dos rectificativas completas seguidas
     * (doble clic) y restar la operación dos veces: con una pendiente de emitir se rechaza.
     */
    @Test
    void crearRectificativa_rechazaSiYaHayUnaPendienteDeEmitir() {
        Factura original = originalEmitida();
        when(repository.findByIdWithPdfData(20L)).thenReturn(Optional.of(original));
        when(repository.existsByFacturaRectificadaNumeroAndEstadoIn(eq("F-GEN-2026-0020"), any()))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crearRectificativa(20L, "x", "DIFERENCIAS", null));
        assertTrue(ex.getMessage().contains("pendiente"));
        verify(repository, never()).save(any(Factura.class));
        assertEquals("EMITIDA", original.getEstado());
    }

    /** El servicio ya no ofrece una vía para anular una factura por rectificativa. */
    @Test
    void noExisteAnulacionPorRectificativa() {
        boolean existe = java.util.Arrays.stream(FacturaService.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("anularPorRectificativa"));
        org.junit.jupiter.api.Assertions.assertFalse(existe);
    }
}
