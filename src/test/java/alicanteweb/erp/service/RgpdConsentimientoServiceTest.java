package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.RgpdConsentimiento;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RgpdConsentimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RgpdConsentimientoServiceTest {

    @Mock RgpdConsentimientoRepository repository;
    @Mock AuditoriaService auditoriaService;
    RgpdConsentimientoService service;

    @BeforeEach
    void setUp() {
        service = new RgpdConsentimientoService(repository, auditoriaService);
    }

    @Test
    void registraConsentimientoOtorgadoODenegadoConMetadata() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        Usuario usuario = new Usuario();
        usuario.setUsername("dpo");
        when(repository.save(any())).thenAnswer(invocation -> {
            RgpdConsentimiento consentimiento = invocation.getArgument(0);
            consentimiento.setId(9L);
            return consentimiento;
        });

        RgpdConsentimiento guardado = service.registrarConsentimiento(
                cliente, "COMERCIAL", true, "127.0.0.1", usuario);

        assertSame(cliente, guardado.getCliente());
        assertTrue(guardado.getOtorgado());
        assertTrue(guardado.getActivo());
        assertEquals("dpo", guardado.getMetadata().get("usuario_registro"));
        assertNotNull(guardado.getMetadata().get("fecha_registro"));
        verify(auditoriaService).registrarCreacion(usuario, "RgpdConsentimiento", "9",
                "Consentimiento COMERCIAL otorgado");

        service.registrarConsentimiento(null, "PERFILADO", false, null, null);
        verify(auditoriaService).registrarCreacion(isNull(), eq("RgpdConsentimiento"), eq("9"),
                contains("denegado"));
    }

    @Test
    void revocaConsentimientoYValidaExistencia() {
        RgpdConsentimiento consentimiento = new RgpdConsentimiento();
        consentimiento.setTipoConsentimiento("COMERCIAL");
        consentimiento.setOtorgado(true);
        when(repository.findById(1L)).thenReturn(Optional.of(consentimiento));

        service.revocarConsentimiento(1L, null);

        assertFalse(consentimiento.getOtorgado());
        assertNotNull(consentimiento.getFechaRevocacion());
        verify(repository).save(consentimiento);
        verify(auditoriaService).registrarActualizacion(isNull(), eq("RgpdConsentimiento"),
                eq("1"), contains("revocado"));

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.revocarConsentimiento(2L, null));
    }

    @Test
    void consultasYContadoresDelegan() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now();
        List<RgpdConsentimiento> lista = List.of(new RgpdConsentimiento());
        when(repository.tieneConsentimientoActivo(1L, "COMERCIAL")).thenReturn(true);
        when(repository.findByClienteIdAndActivoTrue(1L)).thenReturn(lista);
        when(repository.findByClienteId(1L)).thenReturn(lista);
        when(repository.findFirstByClienteIdAndTipoConsentimientoOrderByFechaConsentimientoDesc(1L, "COMERCIAL"))
                .thenReturn(Optional.of(lista.get(0)));
        when(repository.findByEmail("a@b.es")).thenReturn(lista);
        when(repository.findByOtorgadoTrueAndActivoTrue()).thenReturn(lista);
        when(repository.findRevocados()).thenReturn(lista);
        when(repository.findByTipoConsentimientoAndActivoTrue("COMERCIAL")).thenReturn(lista);
        when(repository.findByFechaConsentimientoBetween(inicio, fin)).thenReturn(lista);
        when(repository.countByActivoTrue()).thenReturn(4L);
        when(repository.countByTipoConsentimientoAndActivoTrue("COMERCIAL")).thenReturn(3L);

        assertTrue(service.tieneConsentimientoActivo(1L, "COMERCIAL"));
        assertSame(lista, service.obtenerConsentimientos(1L));
        assertSame(lista, service.obtenerTodosConsentimientos(1L));
        assertSame(lista.get(0), service.obtenerUltimoConsentimiento(1L, "COMERCIAL"));
        assertSame(lista, service.obtenerPorEmail("a@b.es"));
        assertSame(lista, service.obtenerConsentimientosOtorgados());
        assertSame(lista, service.obtenerConsentimientosRevocados());
        assertSame(lista, service.obtenerPorTipo("COMERCIAL"));
        assertSame(lista, service.obtenerPorFechas(inicio, fin));
        assertEquals(4L, service.contarActivos());
        assertEquals(3L, service.contarPorTipo("COMERCIAL"));

        when(repository.findFirstByClienteIdAndTipoConsentimientoOrderByFechaConsentimientoDesc(2L, "X"))
                .thenReturn(Optional.empty());
        assertNull(service.obtenerUltimoConsentimiento(2L, "X"));
    }

    @Test
    void actualizarPoliticaDesactivaYConservaMetadata() {
        RgpdConsentimiento sinMetadata = new RgpdConsentimiento();
        sinMetadata.setVersionPolitica("1");
        sinMetadata.setActivo(true);
        RgpdConsentimiento conMetadata = new RgpdConsentimiento();
        conMetadata.setVersionPolitica("2");
        conMetadata.setActivo(true);
        conMetadata.setMetadata(new java.util.HashMap<>(Map.of("origen", "web")));
        when(repository.findByOtorgadoTrueAndActivoTrue())
                .thenReturn(List.of(sinMetadata, conMetadata));

        service.actualizarPoliticaPrivacidad("3", null);

        assertFalse(sinMetadata.getActivo());
        assertEquals(true, sinMetadata.getMetadata().get("requiere_revalidacion"));
        assertEquals("1", sinMetadata.getMetadata().get("version_anterior"));
        assertEquals("3", conMetadata.getMetadata().get("nueva_version"));
        assertEquals("web", conMetadata.getMetadata().get("origen"));
        verify(repository).save(sinMetadata);
        verify(repository).save(conMetadata);
        verify(auditoriaService).registrarAccion(isNull(),
                eq("ACTUALIZACION_POLITICA_PRIVACIDAD"), eq("PoliticaPrivacidad"),
                eq("3"), contains("2 consentimientos"));
    }
}
