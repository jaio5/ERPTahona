package alicanteweb.erp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestion de impresion de documentos
 * Proporciona funcionalidades para imprimir facturas, albaranes y otros documentos
 */
@Service
public class PrintService {
    private static final Logger log = LoggerFactory.getLogger(PrintService.class);

    /**
     * Imprime un documento PDF
     * @param documento archivo PDF a imprimir
     */
    public void imprimir(File documento) {
        try {
            if (documento == null || !documento.exists()) {
                log.warn("El documento no existe o es nulo");
                return;
            }
            
            log.info("Iniciando impresion del documento: {}", documento.getName());
            
            // Obtener servicio de impresion
            javax.print.PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            if (services.length == 0) {
                log.error("No hay impresoras disponibles");
                return;
            }
            
            // Usar impresora predeterminada
            javax.print.PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultService == null) {
                defaultService = services[0];
            }
            
            // Configurar atributos de impresion
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Copies(1));
            attrs.add(MediaSizeName.ISO_A4);
            
            // Crear trabajo de impresion
            DocPrintJob job = defaultService.createPrintJob();
            FileInputStream fis = new FileInputStream(documento);
            Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            
            job.print(doc, attrs);
            fis.close();
            
            log.info("Documento enviado a la impresora: {}", defaultService.getName());
            
        } catch (Exception e) {
            log.error("Error al imprimir documento: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica si hay impresoras disponibles en el sistema
     * @return true si hay al menos una impresora disponible
     */
    public boolean hayImpresoraDisponible() {
        javax.print.PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        boolean disponible = services != null && services.length > 0;
        log.debug("Impresoras disponibles: {}", disponible);
        return disponible;
    }

    /**
     * Obtiene el nombre de la impresora predeterminada
     * @return nombre de la impresora predeterminada o mensaje si no hay
     */
    public String getImpresoraPredeterminada() {
        javax.print.PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service != null) {
            return service.getName();
        }
        return "Sin impresora predeterminada";
    }

    /**
     * Lista todas las impresoras disponibles en el sistema
     * @return lista de nombres de impresoras
     */
    public List<String> listarImpresoras() {
        List<String> impresoras = new ArrayList<>();
        javax.print.PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        if (services != null) {
            for (javax.print.PrintService service : services) {
                impresoras.add(service.getName());
            }
        }
        
        log.debug("Impresoras encontradas: {}", impresoras.size());
        return impresoras;
    }

    /**
     * Imprime usando una impresora especifica
     * @param documento archivo a imprimir
     * @param nombreImpresora nombre de la impresora a usar
     */
    public void imprimirEn(File documento, String nombreImpresora) {
        try {
            if (documento == null || !documento.exists()) {
                log.warn("El documento no existe o es nulo");
                return;
            }
            
            javax.print.PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            javax.print.PrintService selectedService = null;
            
            for (javax.print.PrintService service : services) {
                if (service.getName().equalsIgnoreCase(nombreImpresora)) {
                    selectedService = service;
                    break;
                }
            }
            
            if (selectedService == null) {
                log.error("No se encontro la impresora: {}", nombreImpresora);
                return;
            }
            
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Copies(1));
            attrs.add(MediaSizeName.ISO_A4);
            
            DocPrintJob job = selectedService.createPrintJob();
            FileInputStream fis = new FileInputStream(documento);
            Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            
            job.print(doc, attrs);
            fis.close();
            
            log.info("Documento enviado a: {}", nombreImpresora);
            
        } catch (Exception e) {
            log.error("Error al imprimir en {}: {}", nombreImpresora, e.getMessage(), e);
        }
    }

    /**
     * Muestra el dialogo de impresion del sistema
     * @return true si el usuario acepto imprimir
     */
    public boolean mostrarDialogoImpresion() {
        PrinterJob job = PrinterJob.getPrinterJob();
        return job.printDialog();
    }

    /**
     * Imprime multiples copias de un documento
     * @param documento archivo a imprimir
     * @param copias numero de copias
     */
    public void imprimirCopias(File documento, int copias) {
        try {
            if (documento == null || !documento.exists()) {
                log.warn("El documento no existe o es nulo");
                return;
            }
            
            if (copias < 1) copias = 1;
            
            javax.print.PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultService == null) {
                log.error("No hay impresora predeterminada");
                return;
            }
            
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Copies(copias));
            attrs.add(MediaSizeName.ISO_A4);
            
            DocPrintJob job = defaultService.createPrintJob();
            FileInputStream fis = new FileInputStream(documento);
            Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            
            job.print(doc, attrs);
            fis.close();
            
            log.info("Imprimiendo {} copias en: {}", copias, defaultService.getName());
            
        } catch (Exception e) {
            log.error("Error al imprimir copias: {}", e.getMessage(), e);
        }
    }
}

