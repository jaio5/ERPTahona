package alicanteweb.erp.service;

import alicanteweb.erp.service.dto.ClienteDatosExternos;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClienteDatosExternosServiceTest {

    @Test
    void buscarPorCif_normalizaConsultaMapeaRespuestaYUsaBearer() {
        ClienteDatosExternosService service = crearServicio(true, "secret");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        server.expect(once(), requestTo("https://api.test/companies?cif=B12345674"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer secret"))
            .andRespond(withSuccess("""
                {
                  "success": true,
                  "data": {
                    "name": "Panaderia Test SL",
                    "cif": "B12345674",
                    "address": "Calle Mayor 1",
                    "postal_code": "03001",
                    "city": "Alicante",
                    "province": "Alicante",
                    "status": "ACTIVA"
                  }
                }
                """, MediaType.APPLICATION_JSON));

        Optional<ClienteDatosExternos> res = service.buscarPorCif(" b12345674 ");

        assertTrue(res.isPresent());
        assertEquals("B12345674", res.get().getCif());
        assertEquals("Panaderia Test SL", res.get().getNombre());
        assertEquals("Calle Mayor 1", res.get().getDireccion());
        assertEquals("03001", res.get().getCodigoPostal());
        assertEquals("Alicante", res.get().getPoblacion());
        server.verify();
    }

    @Test
    void buscarPorNombre_tomaPrimerResultadoSiLaApiDevuelveArray() {
        ClienteDatosExternosService service = crearServicio(true, "secret");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        server.expect(requestTo("https://api.test/companies?q=Tahona"))
            .andRespond(withSuccess("""
                {"data":[{"razon_social":"Tahona SL","nif":"B12345674","municipio":"Alicante"}]}
                """, MediaType.APPLICATION_JSON));

        Optional<ClienteDatosExternos> res = service.buscarPorNombre("Tahona");

        assertTrue(res.isPresent());
        assertEquals("Tahona SL", res.get().getNombre());
        assertEquals("B12345674", res.get().getCif());
        assertEquals("Alicante", res.get().getPoblacion());
        server.verify();
    }

    @Test
    void buscarCoincidenciasPorNombre_devuelveTodosLosResultadosUtiles() {
        ClienteDatosExternosService service = crearServicio(true, "secret");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        server.expect(requestTo("https://api.test/companies?q=Tahona"))
            .andRespond(withSuccess("""
                {"data":[
                  {"razon_social":"Tahona Centro SL","nif":"B12345674","municipio":"Alicante"},
                  {"razon_social":"Tahona Norte SL","nif":"B22222222","municipio":"San Vicente"},
                  {}
                ]}
                """, MediaType.APPLICATION_JSON));

        var res = service.buscarCoincidenciasPorNombre("Tahona");

        assertEquals(2, res.size());
        assertEquals("Tahona Centro SL", res.get(0).getNombre());
        assertEquals("Tahona Norte SL", res.get(1).getNombre());
        server.verify();
    }

    @Test
    void noConsultaSiEstaDeshabilitadoODatosVacios() {
        ClienteDatosExternosService service = crearServicio(false, "secret");
        assertTrue(service.buscarPorCif("B12345674").isEmpty());
        assertTrue(service.buscarPorNombre("Tahona").isEmpty());
        assertTrue(service.buscarCoincidenciasPorNombre("Tahona").isEmpty());
    }

    @Test
    void fallaRapidoSiNoHayApiKey() {
        ClienteDatosExternosService service = crearServicio(true, "");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.buscarPorCif("B12345674"));
        assertTrue(ex.getMessage().contains("CLIENTES_AUTOCOMPLETAR_API_KEY"));
    }

    private ClienteDatosExternosService crearServicio(boolean enabled, String apiKey) {
        return new ClienteDatosExternosService(
            new RestTemplateBuilder(),
            new ObjectMapper(),
            "https://api.test/companies",
            apiKey,
            enabled,
            2,
            "cif",
            "q"
        );
    }
}
