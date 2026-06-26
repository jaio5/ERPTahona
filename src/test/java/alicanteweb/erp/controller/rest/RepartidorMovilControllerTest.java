package alicanteweb.erp.controller.rest;

import alicanteweb.erp.config.SecurityConfig.ErpUserPrincipal;
import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.service.HojaRutaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RepartidorMovilControllerTest {

    private HojaRutaService service;
    private RepartidorMovilController controller;
    private Authentication repartidor;

    @BeforeEach
    void setUp() {
        service = mock(HojaRutaService.class);
        controller = new RepartidorMovilController(service);
        ErpUserPrincipal principal = new ErpUserPrincipal(
                7L, null, "repartidor", "Repartidor Uno", "", true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        repartidor = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }

    @Test
    void obtieneLaRutaPorElPrincipalYNoPorUnConductorManipulado() {
        HojaRuta hoja = new HojaRuta();
        hoja.setId(10L);
        hoja.setFecha(LocalDate.now());
        hoja.setEstado("PLANIFICADA");
        hoja.setConductor("Repartidor Uno");
        when(service.findRutaDelUsuario(LocalDate.now(), 7L, "repartidor", "Repartidor Uno"))
                .thenReturn(Optional.of(hoja));
        when(service.getEntregas(10L)).thenReturn(List.of());

        var response = controller.getMiRutaDelDia("otro-conductor", repartidor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).findRutaDelUsuario(LocalDate.now(), 7L, "repartidor", "Repartidor Uno");
        verify(service, never()).findByFecha(any());
    }

    @Test
    void impideModificarUnaEntregaAjena() {
        when(service.puedeGestionarEntrega(99L, 7L, "repartidor", "Repartidor Uno"))
                .thenReturn(false);

        var response = controller.confirmarParada(
                99L, null, null, null, null, null, null, repartidor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(service, never()).confirmarEntrega(anyLong(), any(), any(), any(), any());
    }

    @Test
    void permiteModificarLaEntregaPropia() {
        HojaRutaEntrega entrega = new HojaRutaEntrega();
        entrega.setId(25L);
        entrega.setEstadoEntrega("ENTREGADO");
        when(service.puedeGestionarEntrega(25L, 7L, "repartidor", "Repartidor Uno"))
                .thenReturn(true);
        when(service.confirmarEntrega(25L, "Ana", null, BigDecimal.TEN, "EFECTIVO"))
                .thenReturn(entrega);

        var response = controller.confirmarParada(
                25L, "Ana", null, BigDecimal.TEN, "EFECTIVO", null, null, repartidor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).saveEntrega(entrega);
    }

    @Test
    void impideIniciarUnaRutaAjena() {
        when(service.puedeGestionarHoja(12L, 7L, "repartidor", "Repartidor Uno"))
                .thenReturn(false);

        var response = controller.iniciarRuta(12L, repartidor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(service, never()).iniciarRuta(anyLong());
    }
}
