package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.AlbaranVenta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Servicio para impresión y exportación de documentos
 */
@Service
public class ImpresionService {
    private static final Logger log = LoggerFactory.getLogger(ImpresionService.class);
    
    private static final String RUTA_REPORTES = "target/reportes/";

    public ImpresionService() {
        // Crear directorio de reportes si no existe
        File dir = new File(RUTA_REPORTES);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("Directorio de reportes creado: {}", RUTA_REPORTES);
        }
    }

    /**
     * Genera HTML simple para imprimir factura
     */
    public String generarHTMLFactura(Factura factura) {
        log.info("Generando HTML para factura: {}", factura.getId());
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
        html.append("<title>FACTURA - ").append(factura.getNumero()).append("</title>\n");
        html.append("<style>body { font-family: Arial; } table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid black; padding: 8px; }</style>\n</head>\n<body>\n");
        
        html.append("<h1>FACTURA</h1>\n");
        html.append("<p><strong>Número:</strong> ").append(factura.getNumero()).append("</p>\n");
        html.append("<p><strong>Fecha:</strong> ").append(factura.getFecha()).append("</p>\n");
        html.append("<p><strong>Estado:</strong> ").append(factura.getEstado()).append("</p>\n");
        
        if (factura.getCliente() != null) {
            html.append("<h2>Cliente</h2>\n");
            html.append("<p>").append(factura.getCliente().getNombre()).append("</p>\n");
        }
        
        html.append("<h2>Importes</h2>\n<table>\n<tr><th>Concepto</th><th>Importe</th></tr>\n");
        if (factura.getTotal() != null) {
            html.append("<tr><td>Total</td><td>").append(factura.getTotal()).append("€</td></tr>\n");
        }
        html.append("</table>\n<p><em>").append(LocalDateTime.now()).append("</em></p>\n");
        html.append("</body>\n</html>\n");
        
        return html.toString();
    }

    /**
     * Genera HTML para imprimir albarán
     */
    public String generarHTMLAlbaran(AlbaranVenta albaran) {
        log.info("Generando HTML para albarán: {}", albaran.getId());
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
        html.append("<title>ALBARÁN - ").append(albaran.getNumero()).append("</title>\n");
        html.append("<style>body { font-family: Arial; }</style>\n</head>\n<body>\n");
        
        html.append("<h1>ALBARÁN DE ENTREGA</h1>\n");
        html.append("<p><strong>Número:</strong> ").append(albaran.getNumero()).append("</p>\n");
        html.append("<p><strong>Fecha:</strong> ").append(albaran.getFecha()).append("</p>\n");
        
        if (albaran.getCliente() != null) {
            html.append("<h2>Cliente</h2>\n");
            html.append("<p>").append(albaran.getCliente().getNombre()).append("</p>\n");
        }
        
        html.append("<p><em>").append(LocalDateTime.now()).append("</em></p>\n");
        html.append("</body>\n</html>\n");
        
        return html.toString();
    }

    /**
     * Guarda el HTML a archivo para impresión
     */
    public String guardarParaImpresion(String html, String nombreDocumento) {
        try {
            String nombreArchivo = RUTA_REPORTES + nombreDocumento + "_" + 
                System.currentTimeMillis() + ".html";
            
            File file = new File(nombreArchivo);
            java.nio.file.Files.write(file.toPath(), html.getBytes("UTF-8"));
            
            log.info("Documento guardado para impresión: {}", nombreArchivo);
            return nombreArchivo;
        } catch (Exception e) {
            log.error("Error guardando documento para impresión", e);
            throw new RuntimeException("Error al guardar documento", e);
        }
    }
}

