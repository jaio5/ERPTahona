package alicanteweb.erp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Service
public class VatValidationService {

    private static final Set<String> EU_VAT_COUNTRIES = Set.of(
        "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "EL", "ES", "FI", "FR",
        "HR", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO",
        "SE", "SI", "SK"
    );

    private final RestTemplate restTemplate;
    private final String endpoint;
    private final boolean enabled;

    public VatValidationService(RestTemplateBuilder restTemplateBuilder,
                                @Value("${vat.validation.endpoint:https://ec.europa.eu/taxation_customs/vies/services/checkVatService}") String endpoint,
                                @Value("${vat.validation.enabled:true}") boolean enabled,
                                @Value("${vat.validation.timeout-seconds:10}") int timeoutSeconds) {
        this.restTemplate = restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .readTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
        this.endpoint = endpoint;
        this.enabled = enabled;
    }

    public VatValidationResult validar(String vatOrNif) {
        VatParts parts = normalizar(vatOrNif);
        if (!enabled) {
            return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "La validacion VAT esta deshabilitada");
        }
        if (parts.countryCode().isBlank() || parts.vatNumber().isBlank()) {
            return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "Introduce un VAT/NIF con pais, por ejemplo ESB12345678");
        }
        if (!EU_VAT_COUNTRIES.contains(parts.countryCode())) {
            return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "Pais no soportado por VIES: " + parts.countryCode());
        }

        String body = soapEnvelope(parts.countryCode(), parts.vatNumber());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.set(HttpHeaders.ACCEPT, "text/xml");
        headers.set("SOAPAction", "");

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
                return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "VIES no devolvio una respuesta valida");
            }
            return parsearRespuesta(parts, response.getBody());
        } catch (RestClientException e) {
            return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "No se pudo consultar VIES: " + e.getMessage());
        } catch (Exception e) {
            return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "Respuesta VIES no interpretable: " + e.getMessage());
        }
    }

    private VatParts normalizar(String value) {
        String text = value == null ? "" : value.trim().toUpperCase(Locale.ROOT)
            .replace(" ", "")
            .replace("-", "")
            .replace(".", "");
        if (text.length() >= 3 && Character.isLetter(text.charAt(0)) && Character.isLetter(text.charAt(1))) {
            return new VatParts(normalizarPais(text.substring(0, 2)), text.substring(2));
        }
        return new VatParts("ES", text);
    }

    private String normalizarPais(String countryCode) {
        return "GR".equals(countryCode) ? "EL" : countryCode;
    }

    private String soapEnvelope(String countryCode, String vatNumber) {
        return """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:ec.europa.eu:taxud:vies:services:checkVat:types">
              <soapenv:Header/>
              <soapenv:Body>
                <urn:checkVat>
                  <urn:countryCode>%s</urn:countryCode>
                  <urn:vatNumber>%s</urn:vatNumber>
                </urn:checkVat>
              </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(escapeXml(countryCode), escapeXml(vatNumber));
    }

    private VatValidationResult parsearRespuesta(VatParts parts, String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        String fault = text(document, "faultstring");
        if (!fault.isBlank()) {
            return VatValidationResult.error(parts.countryCode(), parts.vatNumber(), "VIES rechazo la consulta: " + fault);
        }

        boolean valid = Boolean.parseBoolean(text(document, "valid"));
        LocalDate requestDate = parseDate(text(document, "requestDate"));
        return new VatValidationResult(
            true,
            valid,
            text(document, "countryCode"),
            text(document, "vatNumber"),
            requestDate,
            limpiarNoDisponible(text(document, "name")),
            limpiarNoDisponible(text(document, "address")),
            valid ? "VAT valido en VIES" : "VAT no valido para operaciones intracomunitarias en VIES"
        );
    }

    private String text(Document document, String localName) {
        NodeList nodes = document.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagName(localName);
        }
        return nodes.getLength() > 0 && nodes.item(0).getTextContent() != null
            ? nodes.item(0).getTextContent().trim()
            : "";
    }

    private String limpiarNoDisponible(String value) {
        if (value == null || value.isBlank() || "---".equals(value.trim())) {
            return "";
        }
        return value.trim();
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String escapeXml(String value) {
        return value == null ? "" : value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    private record VatParts(String countryCode, String vatNumber) {}

    public record VatValidationResult(
        boolean consultaRealizada,
        boolean valido,
        String countryCode,
        String vatNumber,
        LocalDate requestDate,
        String nombre,
        String direccion,
        String mensaje
    ) {
        static VatValidationResult error(String countryCode, String vatNumber, String mensaje) {
            return new VatValidationResult(false, false, countryCode, vatNumber, null, "", "", mensaje);
        }
    }
}
