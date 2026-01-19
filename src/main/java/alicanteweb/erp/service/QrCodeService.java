package alicanteweb.erp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para generación de códigos QR
 * Usado principalmente para VeriFactu
 */
@Service
@Slf4j
public class QrCodeService {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    /**
     * Genera un código QR a partir de un texto
     * @param texto Texto a codificar
     * @return Imagen QR en Base64
     */
    public String generarQR(String texto) {
        return generarQR(texto, QR_WIDTH, QR_HEIGHT);
    }

    /**
     * Genera un código QR con tamño personalizado
     * @param texto Texto a codificar
     * @param width Ancho en píxeles
     * @param height Alto en píxeles
     * @return Imagen QR en Base64
     */
    public String generarQR(String texto, int width, int height) {
        try {
            // Configurar parámetros del QR
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // Generar matriz QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, width, height, hints);

            // Convertir a imagen PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // Convertir a Base64
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            log.debug("QR generado exitosamente ({} bytes)", imageBytes.length);
            return base64Image;

        } catch (WriterException | IOException e) {
            log.error("Error generando código QR", e);
            throw new RuntimeException("Error generando código QR", e);
        }
    }

    /**
     * Genera un QR para VeriFactu con la URL de verificación de AEAT
     * @param hash Hash de la factura (Base64 o similar)
     * @param nif NIF del emisor
     * @param numeroFactura Número de factura
     * @param fechaExpedicion Fecha de expedición (formato dd-MM-yyyy)
     * @param importeTotal Importe total de la factura
     * @return Imagen QR en Base64
     */
    public String generarQRVeriFactu(String hash, String nif, String numeroFactura,
                                     String fechaExpedicion, String importeTotal) {
        try {
            // Asegurar que el hash es seguro para la URL: preferimos Base64 URL-safe sin padding
            String hashUrlSafe;
            if (hash == null) {
                hashUrlSafe = "";
            } else {
                // Si parece un Base64 estándar, intentar convertir a URL-safe
                try {
                    byte[] decoded = Base64.getDecoder().decode(hash);
                    hashUrlSafe = Base64.getUrlEncoder().withoutPadding().encodeToString(decoded);
                } catch (IllegalArgumentException e) {
                    // No es Base64: hacer URLEncode del valor tal cual
                    hashUrlSafe = URLEncoder.encode(hash, StandardCharsets.UTF_8);
                }
            }

            // URL de verificación de AEAT
            String url = String.format(
                    "https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/verificar?hash=%s&nif=%s&numero=%s&fecha=%s&importe=%s",
                    URLEncoder.encode(hashUrlSafe, StandardCharsets.UTF_8),
                    URLEncoder.encode(nif != null ? nif : "", StandardCharsets.UTF_8),
                    URLEncoder.encode(numeroFactura != null ? numeroFactura : "", StandardCharsets.UTF_8),
                    URLEncoder.encode(fechaExpedicion != null ? fechaExpedicion : "", StandardCharsets.UTF_8),
                    URLEncoder.encode(importeTotal != null ? importeTotal : "", StandardCharsets.UTF_8)
            );

            log.info("Generando QR VeriFactu para factura: {} -> URL length {}", numeroFactura, url.length());
            return generarQR(url);
        } catch (Exception e) {
            log.error("Error preparando QR VeriFactu", e);
            throw new RuntimeException("Error preparando QR VeriFactu", e);
        }
    }

    /**
     * Genera un QR simple con la huella de la factura
     * @param hash Hash SHA-256 de la factura
     * @return Imagen QR en Base64
     */
    public String generarQRHash(String hash) {
        return generarQR(hash);
    }

    /**
     * Verifica si una cadena es un QR válido (tiene contenido)
     * @param qrBase64 QR en Base64
     * @return true si es válido
     */
    public boolean esQRValido(String qrBase64) {
        if (qrBase64 == null || qrBase64.trim().isEmpty()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(qrBase64);
            return decoded.length > 0;
        } catch (IllegalArgumentException e) {
            log.warn("QR inválido (no es Base64 válido)");
            return false;
        }
    }

    /**
     * Obtiene el tamño de un QR en bytes
     * @param qrBase64 QR en Base64
     * @return Tamño en bytes
     */
    public int obtenerTamanoQR(String qrBase64) {
        if (qrBase64 == null || qrBase64.trim().isEmpty()) {
            return 0;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(qrBase64);
            return decoded.length;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Genera un QR para datos de contacto (vCard)
     * @param nombre Nombre
     * @param telefono Teléfono
     * @param email Email
     * @param empresa Empresa
     * @return Imagen QR en Base64
     */
    public String generarQRContacto(String nombre, String telefono, String email, String empresa) {
        StringBuilder vcard = new StringBuilder();
        vcard.append("BEGIN:VCARD\n");
        vcard.append("VERSION:3.0\n");
        vcard.append("FN:").append(nombre).append("\n");
        if (empresa != null && !empresa.isEmpty()) {
            vcard.append("ORG:").append(empresa).append("\n");
        }
        if (telefono != null && !telefono.isEmpty()) {
            vcard.append("TEL:").append(telefono).append("\n");
        }
        if (email != null && !email.isEmpty()) {
            vcard.append("EMAIL:").append(email).append("\n");
        }
        vcard.append("END:VCARD");

        return generarQR(vcard.toString());
    }

    /**
     * Genera un QR para WiFi
     * @param ssid Nombre de la red
     * @param password Contraseña
     * @param tipo Tipo de seguridad (WPA, WEP, nopass)
     * @return Imagen QR en Base64
     */
    public String generarQRWifi(String ssid, String password, String tipo) {
        String wifiString = String.format("WIFI:T:%s;S:%s;P:%s;;", tipo, ssid, password);
        return generarQR(wifiString);
    }

    /**
     * Genera un QR para un enlace web
     * @param url URL completa
     * @return Imagen QR en Base64
     */
    public String generarQRUrl(String url) {
        return generarQR(url);
    }
}

