package alicanteweb.erp.service;

import alicanteweb.erp.entities.FacturaSerieSequence;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.FacturaSerieSequenceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

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
    EntityManager entityManager;

    @InjectMocks
    FacturaService service;

    @Test
    void generarSiguienteNumero_propagaColisionSinReusarLaTransaccion() {
        when(sequenceRepository.findBySerieAndEjercicio("GEN", 2026))
            .thenReturn(Optional.empty());
        when(repository.findMaxNumeroSecuencialBySerieAndPrefijo("GEN", "F-GEN-2026-%", 11))
            .thenReturn(7L);
        when(sequenceRepository.saveAndFlush(any(FacturaSerieSequence.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DataIntegrityViolationException.class,
                () -> service.generarSiguienteNumero("gen", LocalDate.of(2026, 4, 24), false));
        verify(entityManager, never()).clear();
        verify(sequenceRepository).saveAndFlush(any(FacturaSerieSequence.class));
    }

    @Test
    void generarSiguienteNumero_inicializaDesdeElMaximoExistenteDeLaSerie() {
        when(sequenceRepository.findBySerieAndEjercicio("CLI01", 2026))
            .thenReturn(Optional.empty());
        when(repository.findMaxNumeroSecuencialBySerieAndPrefijo("CLI01", "R-CLI01-2026-%", 13))
            .thenReturn(12L);
        when(sequenceRepository.saveAndFlush(any(FacturaSerieSequence.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String numero = service.generarSiguienteNumero("cli01", LocalDate.of(2026, 1, 10), true);

        assertEquals("R-CLI01-2026-0013", numero);
        verify(repository).findMaxNumeroSecuencialBySerieAndPrefijo("CLI01", "R-CLI01-2026-%", 13);
    }

    @Test
    void save_generaNumeroYSerieCuandoLaFacturaNuevaNoLosTrae() {
        FacturaSerieSequence sequence = new FacturaSerieSequence();
        sequence.setSerie("GEN");
        sequence.setEjercicio(2026);
        sequence.setUltimoNumero(3L);

        Factura factura = new Factura();
        factura.setFecha(LocalDate.of(2026, 6, 16));

        when(sequenceRepository.findBySerieAndEjercicio("GEN", 2026))
            .thenReturn(Optional.of(sequence));
        when(sequenceRepository.saveAndFlush(any(FacturaSerieSequence.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(Factura.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Factura guardada = service.save(factura);

        assertEquals("GEN", guardada.getSerie());
        assertEquals("F-GEN-2026-0004", guardada.getNumero());
        verify(repository).save(factura);
    }
}
