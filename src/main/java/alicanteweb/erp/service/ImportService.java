package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ImportService {

    public record ImportResult(int total, int ok, int errores, List<String> mensajes) {}

    public ImportResult importarClientes(MultipartFile file, ClienteService service) {
        List<String> msgs = new ArrayList<>(); int total = 0, ok = 0;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String header = r.readLine(); total = -1;
            String line;
            while ((line = r.readLine()) != null) {
                total++;
                try {
                    String[] cols = line.split(";");
                    if (cols.length < 2) continue;
                    Cliente c = new Cliente();
                    c.setCodigo(cols[0].trim());
                    c.setNombre(cols[1].trim());
                    if (cols.length > 2) c.setCif(cols[2].trim());
                    if (cols.length > 3) c.setTelefono(cols[3].trim());
                    if (cols.length > 4) c.setEmail(cols[4].trim());
                    if (cols.length > 5) c.setDireccion(cols[5].trim());
                    if (cols.length > 6) c.setPoblacion(cols[6].trim());
                    service.save(c); ok++;
                } catch (Exception e) { msgs.add("Línea " + (total+1) + ": " + e.getMessage()); }
            }
        } catch (Exception e) { msgs.add("Error leyendo archivo: " + e.getMessage()); }
        return new ImportResult(total, ok, total - ok, msgs);
    }

    public ImportResult importarArticulos(MultipartFile file, ArticuloService service) {
        List<String> msgs = new ArrayList<>(); int total = 0, ok = 0;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            r.readLine(); total = -1;
            String line;
            while ((line = r.readLine()) != null) {
                total++;
                try {
                    String[] cols = line.split(";");
                    if (cols.length < 2) continue;
                    Articulo a = new Articulo();
                    a.setCodigo(cols[0].trim()); a.setNombre(cols[1].trim());
                    if (cols.length > 2 && !cols[2].isBlank()) a.setPvp(new BigDecimal(cols[2].trim().replace(",",".")));
                    if (cols.length > 3 && !cols[3].isBlank()) a.setIva(new BigDecimal(cols[3].trim().replace(",",".")));
                    if (cols.length > 4) a.setCategoria(cols[4].trim());
                    if (cols.length > 5) a.setAlergenos(cols[5].trim());
                    service.save(a); ok++;
                } catch (Exception e) { msgs.add("Línea " + (total+1) + ": " + e.getMessage()); }
            }
        } catch (Exception e) { msgs.add("Error: " + e.getMessage()); }
        return new ImportResult(total, ok, total - ok, msgs);
    }
}
