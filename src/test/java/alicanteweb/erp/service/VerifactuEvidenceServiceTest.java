package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Contención VERI*FACTU F0: la pantalla de evidencias no puede fabricar estados fiscales.
 * "Reenviar" y "Verificar" ponían ENVIADO/VERIFICADO sin llamar a la AEAT y el servicio
 * exponía el borrado de registros, que son inalterables.
 */
@ExtendWith(MockitoExtension.class)
class VerifactuEvidenceServiceTest {

    @Mock
    VerifactuEvidenceRepository repository;

    @Mock
    VerifactuService verifactuService;

    @InjectMocks
    VerifactuEvidenceService service;

    private VerifactuEvidence evidencia(long id, String estado) {
        VerifactuEvidence e = new VerifactuEvidence();
        e.setId(id);
        e.setEstado(estado);
        return e;
    }

    @Test
    void reenviarEvidencia_estaDesactivadoYNoCambiaElEstado() {
        VerifactuEvidence e = evidencia(10L, VerifactuEvidence.ESTADO_NO_REMITIDO);
        when(repository.findById(10L)).thenReturn(Optional.of(e));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.reenviarEvidencia(10L));

        assertEquals(VerifactuEvidenceService.MENSAJE_ACCION_DESACTIVADA, ex.getMessage());
        assertEquals(VerifactuEvidence.ESTADO_NO_REMITIDO, e.getEstado());
        verify(repository, never()).save(any(VerifactuEvidence.class));
        verifyNoInteractions(verifactuService);
    }

    @Test
    void verificarEstadoAeat_estaDesactivadoYNoMarcaVerificado() {
        VerifactuEvidence e = evidencia(11L, "ENVIADO");
        when(repository.findById(11L)).thenReturn(Optional.of(e));

        assertThrows(IllegalStateException.class, () -> service.verificarEstadoAEAT(11L));

        assertEquals("ENVIADO", e.getEstado());
        verify(repository, never()).save(any(VerifactuEvidence.class));
        verifyNoInteractions(verifactuService);
    }

    @Test
    void accionesSobreEvidenciaInexistente_informanDeQueNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.reenviarEvidencia(99L));
        assertEquals("Evidencia no encontrada con ID: 99", ex.getMessage());
    }

    @Test
    void elServicioNoExponeBorradoNiGuardadoDeEvidencias() {
        boolean exponeEscritura = Arrays.stream(VerifactuEvidenceService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .anyMatch(n -> n.startsWith("delete") || n.equals("save") || n.equals("registrarEvidenciaAEAT"));
        assertFalse(exponeEscritura, "VerifactuEvidenceService debe ser de solo lectura");
    }

    @Test
    void soloAceptadoYAceptadoConErroresConfirmanLaRemision() {
        assertEquals(true, VerifactuEvidence.esRemisionConfirmada(VerifactuEvidence.ESTADO_ACEPTADO));
        assertEquals(true, VerifactuEvidence.esRemisionConfirmada(VerifactuEvidence.ESTADO_ACEPTADO_CON_ERRORES));
        assertFalse(VerifactuEvidence.esRemisionConfirmada("ENVIADO"));
        assertFalse(VerifactuEvidence.esRemisionConfirmada("VERIFICADO"));
        assertFalse(VerifactuEvidence.esRemisionConfirmada(VerifactuEvidence.ESTADO_NO_REMITIDO));
        assertFalse(VerifactuEvidence.esRemisionConfirmada(VerifactuEvidence.ESTADO_PENDIENTE));
        assertFalse(VerifactuEvidence.esRemisionConfirmada(VerifactuEvidence.ESTADO_RESPUESTA_NO_CONFIRMADA));
        assertFalse(VerifactuEvidence.esRemisionConfirmada(null));
    }
}
