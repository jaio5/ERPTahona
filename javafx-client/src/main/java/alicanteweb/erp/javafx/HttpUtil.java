package alicanteweb.erp.javafx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T getJson(String urlStr, Class<T> clazz) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        try (InputStream in = conn.getInputStream()) {
            return mapper.readValue(in, clazz);
        }
    }

    public static <T> T getJsonList(String urlStr, TypeReference<T> typeRef) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        try (InputStream in = conn.getInputStream()) {
            return mapper.readValue(in, typeRef);
        }
    }

    public static String putJson(String urlStr, Object body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        try (OutputStream os = conn.getOutputStream()) {
            mapper.writeValue(os, body);
        }
        int code = conn.getResponseCode();
        return Integer.toString(code);
    }
}
