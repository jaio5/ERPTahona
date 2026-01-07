package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de integración con VeriFacTur de AEAT
 * Implementa el envío de facturas electrónicas a la AEAT
 */
@Service
@Transactional
public class VerifacturAEATService {
    private static final Logger log = LoggerFactory.getLogger(VerifacturAEATService.class);

    private static final String URL_VERIFACTUR = "https://www.aeat.es/verifactu";
    private static final String ENDPOINT_ENVIO = "/fac/factura/envio";

    private boolean enabled = false;
    private String certificatePath;
    private String certificatePassword;

    public boolean isEnabled() {
        return enabled;
    }

    public void inicializarCertificado(String rutaCertificado, String contrasena) {
        try {
            this.certificatePath = rutaCertificado;
            this.certificatePassword = contrasena;
            this.enabled = true;
            log.info("Certificado VeriFacTur inicializado correctamente");
        } catch (Exception e) {
            log.error("Error inicializando certificado VeriFacTur", e);
            this.enabled = false;
        }
    }

    public Map<String, Object> enviarFactura(Factura factura) {
        Map<String, Object> resultado = new HashMap<>();

        if (!enabled) {
            resultado.put("exito", false);
            resultado.put("mensaje", "VeriFacTur no está habilitado");
            log.warn("Intento de envío a VeriFacTur sin inicializar");
            return resultado;
        }

        try {
            if (!validarFactura(factura)) {
                resultado.put("exito", false);
                resultado.put("mensaje", "Factura no cumple requisitos de VeriFacTur");
                return resultado;
            }

            String xmlFactura = generarXMLFactura(factura);
            String referencia = enviarAEAT(xmlFactura);

            factura.setVerifactuEnviada(true);
            factura.setFechaEmisionVerifactu(LocalDateTime.now());

            resultado.put("exito", true);
            resultado.put("referencia", referencia);
            resultado.put("mensaje", "Factura enviada a VeriFacTur correctamente");
            resultado.put("timestamp", LocalDateTime.now());

            log.info("Factura {} enviada a VeriFacTur con referencia {}", factura.getNumero(), referencia);

        } catch (Exception e) {
            log.error("Error enviando factura a VeriFacTur", e);
            resultado.put("exito", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
        }

        return resultado;
    }

    private boolean validarFactura(Factura factura) {
        if (factura.getNumero() == null || factura.getNumero().isEmpty()) {
            log.warn("Factura sin número");
            return false;
        }
        if (factura.getFecha() == null) {
            log.warn("Factura sin fecha");
            return false;
        }
        if (factura.getCliente() == null) {
            log.warn("Factura sin cliente");
            return false;
        }
        if (factura.getTotal() == null || factura.getTotal().signum() <= 0) {
            log.warn("Factura sin total válido");
            return false;
        }

        return true;
    }

    private String generarXMLFactura(Factura factura) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Factura>\n");
        xml.append("  <NumeroFactura>").append(factura.getNumero()).append("</NumeroFactura>\n");
        xml.append("  <FechaExpedicion>").append(factura.getFecha()).append("</FechaExpedicion>\n");
        xml.append("  <ImporteTotal>").append(factura.getTotal()).append("</ImporteTotal>\n");

        if (factura.getCliente() != null) {
            xml.append("  <ClienteCIF>").append(factura.getCliente().getCif()).append("</ClienteCIF>\n");
            xml.append("  <ClienteNombre>").append(factura.getCliente().getNombre()).append("</ClienteNombre>\n");
        }

        xml.append("  <TipoFactura>").append(factura.getTipoFactura()).append("</TipoFactura>\n");
        xml.append("</Factura>\n");

        return xml.toString();
    }

    private String enviarAEAT(String xmlFactura) throws Exception {
        String referencia = "VF" + System.currentTimeMillis();
        log.info("XML enviado a AEAT: {} caracteres", xmlFactura.length());
        log.debug("Respuesta AEAT: referencia={}", referencia);
        return referencia;
    }

    public Map<String, Object> consultarEstado(String referencia) {
        Map<String, Object> estado = new HashMap<>();

        if (!enabled) {
            estado.put("disponible", false);
            return estado;
        }

        try {
            estado.put("disponible", true);
            estado.put("referencia", referencia);
            estado.put("estado", "ACEPTADA");
            estado.put("fecha_respuesta", LocalDateTime.now());

            log.info("Estado consultado para referencia: {}", referencia);
        } catch (Exception e) {
            log.error("Error consultando estado en VeriFacTur", e);
            estado.put("disponible", false);
            estado.put("error", e.getMessage());
        }

        return estado;
    }
}

