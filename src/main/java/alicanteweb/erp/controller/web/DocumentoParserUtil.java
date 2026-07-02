package alicanteweb.erp.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DocumentoParserUtil {

    private DocumentoParserUtil() {}

    /** Construye la lista de líneas con descuento opcional. Omite líneas sin artículo o con cantidad 0. */
    public static List<Map<String, Object>> construirLineas(String[] articuloIds, String[] cantidades,
                                                             String[] precios, String[] ivas,
                                                             String[] descuentos) {
        List<Map<String, Object>> lineas = new ArrayList<>();
        int total = maxLength(articuloIds, cantidades, precios, ivas);
        for (int i = 0; i < total; i++) {
            Long articuloId = parseLong(at(articuloIds, i));
            BigDecimal cantidad = parseDecimal(at(cantidades, i));
            if (articuloId == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) continue;
            Map<String, Object> linea = new HashMap<>();
            linea.put("articuloId", articuloId);
            linea.put("cantidad", cantidad);
            linea.put("precio", parseDecimal(at(precios, i)));
            linea.put("iva", parseDecimal(at(ivas, i)));
            if (descuentos != null) linea.put("descuento", parseDecimal(at(descuentos, i)));
            lineas.add(linea);
        }
        return lineas;
    }

    /** Variante sin descuento — delega al método principal con descuentos=null. */
    public static List<Map<String, Object>> construirLineas(String[] articuloIds, String[] cantidades,
                                                             String[] precios, String[] ivas) {
        return construirLineas(articuloIds, cantidades, precios, ivas, null);
    }

    public static Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.valueOf(value.trim());
    }

    public static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(value.trim().replace(',', '.'));
    }

    public static String fmtDecimal(BigDecimal value) {
        return value != null ? value.toPlainString().replace(".", ",") : "0";
    }

    private static int maxLength(String[]... arrays) {
        int max = 0;
        for (String[] arr : arrays) {
            if (arr != null && arr.length > max) max = arr.length;
        }
        return max;
    }

    private static String at(String[] arr, int i) {
        return arr == null || i >= arr.length ? null : arr[i];
    }
}
