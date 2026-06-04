package alicanteweb.erp.service;

import alicanteweb.erp.service.dto.ClienteDatosExternos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteDatosExternosService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private final String apiKey;
    private final boolean enabled;
    private final String cifParam;
    private final String nombreParam;

    public ClienteDatosExternosService(RestTemplateBuilder restTemplateBuilder,
                                       ObjectMapper objectMapper,
                                       @Value("${clientes.autocompletar.endpoint:https://apiempresas.es/api/v1/companies}") String endpoint,
                                       @Value("${clientes.autocompletar.api-key:}") String apiKey,
                                       @Value("${clientes.autocompletar.enabled:true}") boolean enabled,
                                       @Value("${clientes.autocompletar.timeout-seconds:8}") int timeoutSeconds,
                                       @Value("${clientes.autocompletar.cif-param:cif}") String cifParam,
                                       @Value("${clientes.autocompletar.nombre-param:q}") String nombreParam) {
        this.restTemplate = restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .readTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.cifParam = cifParam;
        this.nombreParam = nombreParam;
    }

    public Optional<ClienteDatosExternos> buscarPorCif(String cif) {
        return buscarCoincidenciasPorCif(cif).stream().findFirst();
    }

    public Optional<ClienteDatosExternos> buscarPorNombre(String nombre) {
        return buscarCoincidenciasPorNombre(nombre).stream().findFirst();
    }

    public List<ClienteDatosExternos> buscarCoincidenciasPorCif(String cif) {
        String cifNormalizado = normalizar(cif);
        return buscarCoincidencias(cifParam, cifNormalizado);
    }

    public List<ClienteDatosExternos> buscarCoincidenciasPorNombre(String nombre) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        return buscarCoincidencias(nombreParam, nombreNormalizado);
    }

    private List<ClienteDatosExternos> buscarCoincidencias(String parametro, String valor) {
        if (!enabled || endpoint == null || endpoint.isBlank() || valor == null || valor.isBlank()) {
            return List.of();
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Configura CLIENTES_AUTOCOMPLETAR_API_KEY para consultar datos externos");
        }

        String url = UriComponentsBuilder.fromUriString(endpoint)
            .queryParam(parametro, valor)
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.set(HttpHeaders.ACCEPT, "application/json");

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.has("data") ? root.path("data") : root;
            List<ClienteDatosExternos> resultados = new ArrayList<>();
            if (data.isArray()) {
                data.forEach(item -> {
                    ClienteDatosExternos datos = mapear(item);
                    if (datos.tieneDatosUtiles()) {
                        resultados.add(datos);
                    }
                });
            } else {
                ClienteDatosExternos datos = mapear(data);
                if (datos.tieneDatosUtiles()) {
                    resultados.add(datos);
                }
            }
            return resultados;
        } catch (RestClientException e) {
            throw new IllegalStateException("No se pudo consultar la API de empresas: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("La respuesta de la API de empresas no tiene un formato válido", e);
        }
    }

    private ClienteDatosExternos mapear(JsonNode data) {
        ClienteDatosExternos datos = new ClienteDatosExternos();
        datos.setCif(texto(data, "cif", "nif", "tax_code", "taxCode", "vat", "vatcode"));
        datos.setNombre(texto(data, "name", "nombre", "razon_social", "razonSocial", "company_name", "denominacion"));
        datos.setDireccion(texto(data, "address", "direccion", "domicilio", "domicilio_social", "registered_office"));
        datos.setCodigoPostal(texto(data, "postal_code", "codigo_postal", "cp", "zip"));
        datos.setPoblacion(texto(data, "city", "municipality", "municipio", "poblacion", "localidad"));
        datos.setProvincia(texto(data, "province", "provincia"));
        datos.setTelefono(texto(data, "phone", "telefono"));
        datos.setEmail(texto(data, "email", "correo"));
        datos.setEstado(texto(data, "status", "estado"));
        return datos;
    }

    private String texto(JsonNode data, String... nombres) {
        for (String nombre : nombres) {
            JsonNode node = data.path(nombre);
            if (!node.isMissingNode() && !node.isNull()) {
                String valor = node.asText("").trim();
                if (!valor.isEmpty()) {
                    return valor;
                }
            }
        }
        return null;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase().replace(" ", "");
    }
}
