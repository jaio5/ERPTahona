package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifactuEvidenceServiceTest {

    @Mock
    VerifactuEvidenceRepository repository;

    @Mock
    VerifactuService verifactuService;

    @InjectMocks
    VerifactuEvidenceService service;

    @Test
    void reenviarEvidencia_marcaPendienteSiAeatNoEstaDisponible() {
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setId(10L);
        evidencia.setMetadata(new HashMap<>());

        when(repository.findById(10L)).thenReturn(Optional.of(evidencia));
        when(verifactuService.isAeatAvailable()).thenReturn(false);
        when(repository.save(any(VerifactuEvidence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifactuEvidence actualizada = service.reenviarEvidencia(10L);

        assertEquals("PENDIENTE", actualizada.getEstado());
        assertEquals("AEAT no disponible para reenvio", actualizada.getErrorMessage());
        assertTrue(Boolean.TRUE.equals(actualizada.getMetadata().get("reenvioSolicitado")));
    }

    @Test
    void verificarEstadoAeat_marcaVerificadoCuandoYaEstabaEnviado() {
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setId(11L);
        evidencia.setEstado("ENVIADO");
        evidencia.setMetadata(new HashMap<>());

        when(repository.findById(11L)).thenReturn(Optional.of(evidencia));
        when(verifactuService.isAeatAvailable()).thenReturn(true);
        when(repository.save(any(VerifactuEvidence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifactuEvidence actualizada = service.verificarEstadoAEAT(11L);

        assertEquals("VERIFICADO", actualizada.getEstado());
        assertNull(actualizada.getErrorMessage());
        assertNotNull(actualizada.getMetadata().get("fechaVerificacion"));
    }

    @Test
    void registrarEvidenciaAeat_firmaYMarcaEnviadoCuandoHayConexion() throws Exception {
        when(verifactuService.obtenerHashAnterior("SER")).thenReturn("prev-hash");
        when(verifactuService.generarHashEncadenado("factura-1", "prev-hash")).thenReturn("hash-actual");
        when(verifactuService.isEnabled()).thenReturn(true);
        when(verifactuService.firmarDatos(any(byte[].class))).thenReturn(new byte[]{1, 2, 3});
        when(verifactuService.getCertificateFingerprint()).thenReturn("fingerprint");
        when(verifactuService.isAeatAvailable()).thenReturn(true);
        when(repository.save(any(VerifactuEvidence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifactuEvidence evidencia = service.registrarEvidenciaAEAT("factura-1", "SER", "F-SER-2026-0001");

        assertEquals("hash-actual", evidencia.getHash());
        assertEquals("hash-actual", evidencia.getHuellaRegistro());
        assertEquals("fingerprint", evidencia.getCertFingerprint());
        assertEquals("ENVIADO", evidencia.getEstado());
        assertNotNull(evidencia.getFechaEnvio());
        verify(repository).save(any(VerifactuEvidence.class));
    }
}
