package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RgpdSolicitudServiceTest {

    @Mock RgpdSolicitudRepository solicitudRepository;
    @Mock AuditoriaService auditoriaService;
    @Mock ClienteRepository clienteRepository;
    @Mock FacturaRepository facturaRepository;
    @Mock PresupuestoRepository presupuestoRepository;
    @Mock PedidoRepository pedidoRepository;
    @Mock AlbaranVentaRepository albaranVentaRepository;
    @Mock RgpdConsentimientoRepository consentimientoRepository;
    @Mock RgpdAccesoDatosRepository accesoDatosRepository;
    RgpdSolicitudService service;

    @BeforeEach
    void setUp() {
        service = new RgpdSolicitudService(solicitudRepository, auditoriaService,
                clienteRepository, facturaRepository, presupuestoRepository,
                pedidoRepository, albaranVentaRepository, consentimientoRepository,
                accesoDatosRepository);
    }

    @Test
    void crearSolicitudInicializaPlazoEstadoYAuditoria() {
        Cliente cliente = cliente(4L);
        when(solicitudRepository.save(any())).thenAnswer(invocation -> {
            RgpdSolicitud solicitud = invocation.getArgument(0);
            solicitud.setId(10L);
            return solicitud;
        });

        RgpdSolicitud creada = service.crearSolicitud(cliente, "cliente@test.es", "Cliente",
                "ACCESO", "Quiero mis datos", "WEB", "127.0.0.1");

        assertSame(cliente, creada.getCliente());
        assertEquals("PENDIENTE", creada.getEstado());
        assertFalse(creada.getIdentidadVerificada());
        assertEquals("WEB", creada.getCanal());
        assertNotNull(creada.getFechaSolicitud());
        assertTrue(creada.getFechaLimiteRespuesta().isAfter(creada.getFechaSolicitud().plusDays(29)));
        verify(auditoriaService).registrarCreacion(isNull(), eq("RgpdSolicitud"), eq("10"),
                contains("ACCESO"));
    }

    @Test
    void asignarResponsableCambiaSoloSolicitudesPendientes() {
        Usuario responsable = usuario(2L, "dpo");
        RgpdSolicitud pendiente = solicitud(1L, "PENDIENTE");
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(pendiente));

        service.asignarResponsable(1L, responsable);

        assertSame(responsable, pendiente.getUsuarioResponsable());
        assertEquals("EN_PROCESO", pendiente.getEstado());
        verify(auditoriaService).registrarActualizacion(responsable, "RgpdSolicitud", "1",
                "Responsable asignado: dpo");

        RgpdSolicitud completada = solicitud(2L, "COMPLETADA");
        when(solicitudRepository.findById(2L)).thenReturn(Optional.of(completada));
        service.asignarResponsable(2L, responsable);
        assertEquals("COMPLETADA", completada.getEstado());
    }

    @Test
    void operacionesRechazanIdentificadoresInexistentes() {
        when(solicitudRepository.findById(anyLong())).thenReturn(Optional.empty());
        Usuario usuario = usuario(1L, "admin");

        assertThrows(IllegalArgumentException.class,
                () -> service.asignarResponsable(1L, usuario));
        assertThrows(IllegalArgumentException.class,
                () -> service.verificarIdentidad(1L, usuario, true));
        assertThrows(IllegalArgumentException.class,
                () -> service.completarSolicitud(1L, usuario, "ok", "ruta"));
        assertThrows(IllegalArgumentException.class,
                () -> service.rechazarSolicitud(1L, usuario, "motivo"));
        assertThrows(IllegalArgumentException.class,
                () -> service.anadirNotas(1L, usuario, "nota"));
    }

    @Test
    void verificarCompletarYRechazarActualizanSolicitud() {
        Usuario usuario = usuario(3L, "gestor");
        RgpdSolicitud solicitud = solicitud(5L, "EN_PROCESO");
        solicitud.setTipoDerecho("PORTABILIDAD");
        when(solicitudRepository.findById(5L)).thenReturn(Optional.of(solicitud));

        service.verificarIdentidad(5L, usuario, true);
        assertTrue(solicitud.getIdentidadVerificada());
        service.verificarIdentidad(5L, usuario, false);
        assertFalse(solicitud.getIdentidadVerificada());

        service.completarSolicitud(5L, usuario, "Datos entregados", "/tmp/datos.zip");
        assertEquals("COMPLETADA", solicitud.getEstado());
        assertEquals("Datos entregados", solicitud.getRespuesta());
        assertEquals("/tmp/datos.zip", solicitud.getRutaArchivoRespuesta());
        assertNotNull(solicitud.getFechaRespuesta());

        service.rechazarSolicitud(5L, usuario, "Identidad no acreditada");
        assertEquals("RECHAZADA", solicitud.getEstado());
        assertEquals("Identidad no acreditada", solicitud.getRespuesta());
        verify(solicitudRepository, times(4)).save(solicitud);
        verify(auditoriaService, times(4)).registrarActualizacion(
                eq(usuario), eq("RgpdSolicitud"), eq("5"), anyString());
    }

    @Test
    void anadirNotasCreaYConcatenaHistorial() {
        Usuario usuario = usuario(1L, "ana");
        RgpdSolicitud solicitud = solicitud(8L, "PENDIENTE");
        when(solicitudRepository.findById(8L)).thenReturn(Optional.of(solicitud));

        service.anadirNotas(8L, usuario, "Primera");
        assertTrue(solicitud.getNotasInternas().contains("ana: Primera"));

        service.anadirNotas(8L, usuario, "Segunda");
        assertTrue(solicitud.getNotasInternas().contains("\n"));
        assertTrue(solicitud.getNotasInternas().contains("ana: Segunda"));

        solicitud.setNotasInternas("");
        service.anadirNotas(8L, usuario, "Tercera");
        assertFalse(solicitud.getNotasInternas().startsWith("\n"));
    }

    @Test
    void consultasEInformeDeleganEnRepositorio() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fin = LocalDateTime.now();
        List<RgpdSolicitud> solicitudes = List.of(solicitud(1L, "PENDIENTE"));
        List<Object[]> estadisticas = List.<Object[]>of(new Object[]{"ACCESO", 2L});

        when(solicitudRepository.findByClienteIdOrderByFechaSolicitudDesc(1L)).thenReturn(solicitudes);
        when(solicitudRepository.findByEmailSolicitanteOrderByFechaSolicitudDesc("a@b.es")).thenReturn(solicitudes);
        when(solicitudRepository.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE")).thenReturn(solicitudes);
        when(solicitudRepository.findByEstadoInOrderByFechaSolicitudAsc(List.of("PENDIENTE", "EN_PROCESO")))
                .thenReturn(solicitudes);
        when(solicitudRepository.findByTipoDerechoOrderByFechaSolicitudDesc("ACCESO")).thenReturn(solicitudes);
        when(solicitudRepository.findByUsuarioResponsableIdOrderByFechaSolicitudDesc(2L)).thenReturn(solicitudes);
        when(solicitudRepository.findVencidas()).thenReturn(solicitudes);
        when(solicitudRepository.findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(inicio, fin))
                .thenReturn(solicitudes);
        when(solicitudRepository.countByEstado("PENDIENTE")).thenReturn(2L);
        when(solicitudRepository.countPendientes()).thenReturn(3L);
        when(solicitudRepository.countVencidas()).thenReturn(1L);
        when(solicitudRepository.estadisticasPorTipoDerecho()).thenReturn(estadisticas);
        when(solicitudRepository.estadisticasPorEstado()).thenReturn(estadisticas);
        when(solicitudRepository.tiempoPromedioRespuestaHoras()).thenReturn(12.5);

        assertSame(solicitudes, service.obtenerSolicitudesCliente(1L));
        assertSame(solicitudes, service.obtenerPorEmail("a@b.es"));
        assertSame(solicitudes, service.obtenerPorEstado("PENDIENTE"));
        assertSame(solicitudes, service.obtenerPendientes());
        assertSame(solicitudes, service.obtenerPorTipoDerecho("ACCESO"));
        assertSame(solicitudes, service.obtenerPorResponsable(2L));
        assertSame(solicitudes, service.obtenerVencidas());
        assertSame(solicitudes, service.obtenerPorFechas(inicio, fin));
        assertEquals(2L, service.contarPorEstado("PENDIENTE"));
        assertEquals(3L, service.contarPendientes());
        assertEquals(1L, service.contarVencidas());
        assertSame(estadisticas, service.obtenerEstadisticasPorTipoDerecho());
        assertSame(estadisticas, service.obtenerEstadisticasPorEstado());
        assertEquals(12.5, service.obtenerTiempoPromedioRespuesta());

        when(solicitudRepository.findProximasAVencer(any())).thenReturn(solicitudes);
        assertSame(solicitudes, service.obtenerProximasAVencer(7));

        when(solicitudRepository.countByEstado("COMPLETADA")).thenReturn(4L);
        when(solicitudRepository.countByEstado("RECHAZADA")).thenReturn(5L);
        Map<String, Object> informe = service.generarInforme();
        assertEquals(3L, informe.get("total_pendientes"));
        assertEquals(1L, informe.get("total_vencidas"));
        assertEquals(4L, informe.get("total_completadas"));
        assertEquals(5L, informe.get("total_rechazadas"));
        assertEquals(1, informe.get("proximas_vencer_7_dias"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportarDatosIncluyeTodasLasCategoriasYNormalizaNulos() {
        Cliente cliente = cliente(7L);
        cliente.setCodigo("C-7");
        cliente.setNombre("Cliente");
        cliente.setActivo(true);
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));

        Factura factura = new Factura();
        factura.setId(1L);
        factura.setNumero("F-1");
        factura.setFecha(LocalDate.of(2026, 1, 2));
        factura.setTotal(BigDecimal.TEN);
        factura.setPagada(true);
        when(facturaRepository.findByCliente_Id(7L)).thenReturn(List.of(factura));

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setId(2L);
        presupuesto.setNumero("P-1");
        when(presupuestoRepository.findByClienteId(7L)).thenReturn(List.of(presupuesto));

        Pedido pedido = new Pedido();
        pedido.setId(3L);
        pedido.setNumero("PED-1");
        when(pedidoRepository.findByClienteId(7L)).thenReturn(List.of(pedido));

        AlbaranVenta albaran = new AlbaranVenta();
        albaran.setId(4L);
        albaran.setNumero("A-1");
        when(albaranVentaRepository.findByClienteId(7L)).thenReturn(List.of(albaran));

        RgpdConsentimiento consentimiento = new RgpdConsentimiento();
        consentimiento.setId(5L);
        consentimiento.setTipoConsentimiento("COMERCIAL");
        when(consentimientoRepository.findByClienteId(7L)).thenReturn(List.of(consentimiento));

        RgpdAccesoDatos acceso = new RgpdAccesoDatos();
        acceso.setId(6L);
        acceso.setTipoAcceso("LECTURA");
        when(accesoDatosRepository.findByClienteIdOrderByFechaAccesoDesc(7L)).thenReturn(List.of(acceso));

        RgpdSolicitud solicitud = solicitud(8L, "COMPLETADA");
        solicitud.setTipoDerecho("ACCESO");
        when(solicitudRepository.findByClienteIdOrderByFechaSolicitudDesc(7L)).thenReturn(List.of(solicitud));

        Map<String, Object> datos = service.exportarDatosCliente(7L);

        assertEquals(7L, datos.get("cliente_id"));
        assertEquals("Cliente", ((Map<String, Object>) datos.get("cliente")).get("nombre"));
        assertEquals(1, ((List<?>) datos.get("facturas")).size());
        assertEquals(1, ((List<?>) datos.get("presupuestos")).size());
        assertEquals(1, ((List<?>) datos.get("pedidos")).size());
        assertEquals(1, ((List<?>) datos.get("albaranes")).size());
        assertEquals(1, ((List<?>) datos.get("consentimientos")).size());
        assertEquals(1, ((List<?>) datos.get("accesos_datos")).size());
        assertEquals(1, ((List<?>) datos.get("solicitudes_rgpd")).size());
        Map<String, Object> facturaExportada =
                (Map<String, Object>) ((List<?>) datos.get("facturas")).get(0);
        assertEquals("", facturaExportada.get("serie"));

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.exportarDatosCliente(99L));
    }

    @Test
    void anonimizarClienteRevocaConsentimientosYAnonimizaSolicitudes() {
        Cliente cliente = cliente(11L);
        cliente.setNombre("Nombre real");
        cliente.setEmail("real@test.es");
        cliente.setActivo(true);
        RgpdConsentimiento consentimiento = new RgpdConsentimiento();
        consentimiento.setOtorgado(true);
        consentimiento.setActivo(true);
        RgpdSolicitud solicitud = solicitud(4L, "COMPLETADA");
        when(clienteRepository.findById(11L)).thenReturn(Optional.of(cliente));
        when(consentimientoRepository.findByClienteId(11L)).thenReturn(List.of(consentimiento));
        when(solicitudRepository.findByClienteIdOrderByFechaSolicitudDesc(11L))
                .thenReturn(List.of(solicitud));
        Usuario usuario = usuario(1L, "dpo");

        service.anonimizarDatosCliente(11L, usuario);

        assertEquals("Cliente anonimizado 11", cliente.getNombre());
        assertEquals("ANON-11", cliente.getCif());
        assertEquals("anonimizado+11@local.invalid", cliente.getEmail());
        assertNull(cliente.getTelefono());
        assertNull(cliente.getDireccion());
        assertFalse(cliente.getActivo());
        assertFalse(consentimiento.getOtorgado());
        assertFalse(consentimiento.getActivo());
        assertNotNull(consentimiento.getFechaRevocacion());
        assertEquals(cliente.getEmail(), consentimiento.getEmail());
        assertEquals(cliente.getNombre(), solicitud.getNombreSolicitante());
        verify(auditoriaService).registrarAccion(usuario, "ANONIMIZACION_CLIENTE",
                "Cliente", "11", "Datos anonimizados por derecho al olvido");
        verify(clienteRepository).save(cliente);
        verify(consentimientoRepository).save(consentimiento);
        verify(solicitudRepository).save(solicitud);

        when(clienteRepository.findById(12L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.anonimizarDatosCliente(12L, usuario));
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        return cliente;
    }

    private Usuario usuario(Long id, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        return usuario;
    }

    private RgpdSolicitud solicitud(Long id, String estado) {
        RgpdSolicitud solicitud = new RgpdSolicitud();
        solicitud.setId(id);
        solicitud.setEstado(estado);
        return solicitud;
    }
}
