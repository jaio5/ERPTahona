package alicanteweb.erp.javafx;

import alicanteweb.erp.javafx.model.Articulo;
import alicanteweb.erp.javafx.model.Cliente;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ApiSmokeTest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Clientes
        try {
            URL url = new URL("http://localhost:8080/api/clientes?limit=50");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            InputStream in = conn.getInputStream();
            List<Cliente> clientes = mapper.readValue(in, new TypeReference<List<Cliente>>(){});
            System.out.println("Clientes recibidos: " + clientes.size());
            if (!clientes.isEmpty()) System.out.println(clientes.get(0).getNombre());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Articulos
        try {
            URL url = new URL("http://localhost:8080/api/articulos?limit=50");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            InputStream in = conn.getInputStream();
            List<Articulo> articulos = mapper.readValue(in, new TypeReference<List<Articulo>>(){});
            System.out.println("Articulos recibidos: " + articulos.size());
            if (!articulos.isEmpty()) System.out.println(articulos.get(0).getDescripcionArticulo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

