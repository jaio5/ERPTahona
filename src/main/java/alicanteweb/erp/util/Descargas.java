package alicanteweb.erp.util;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Construcción centralizada de respuestas de descarga de ficheros.
 *
 * <p>Unifica el patrón repetido en los controladores web: cabecera
 * {@code Content-Disposition: attachment} con nombre de archivo codificado en
 * UTF-8, {@code Cache-Control: no-store} y el tipo de contenido correspondiente.
 */
public final class Descargas {

    private static final MediaType CSV = MediaType.parseMediaType("text/csv;charset=UTF-8");

    private Descargas() {}

    /** Respuesta de descarga como adjunto para un cuerpo en bytes ya generado. */
    public static ResponseEntity<byte[]> adjunto(byte[] cuerpo, String nombreArchivo, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nombreArchivo, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(tipo)
                .body(cuerpo);
    }

    /** Descarga de un CSV (UTF-8) como adjunto. */
    public static ResponseEntity<byte[]> csv(String contenido, String nombreArchivo) {
        return adjunto(contenido.getBytes(StandardCharsets.UTF_8), nombreArchivo, CSV);
    }

    /** Descarga de un fichero PDF ya generado en disco. */
    public static ResponseEntity<byte[]> pdf(File pdf) {
        try {
            return adjunto(Files.readAllBytes(pdf.toPath()), pdf.getName(), MediaType.APPLICATION_PDF);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al leer el PDF: " + pdf.getName(), e);
        }
    }

    /** Descarga de un PDF ya generado en memoria (p. ej. un lote combinado). */
    public static ResponseEntity<byte[]> pdf(byte[] cuerpo, String nombreArchivo) {
        return adjunto(cuerpo, nombreArchivo, MediaType.APPLICATION_PDF);
    }
}
