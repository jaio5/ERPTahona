package alicanteweb.erp.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DocumentoParserUtil {

    private DocumentoParserUtil() {}

    /**
     * Construye la lista de líneas con descuento (valor + tipo) opcional. Omite líneas sin
     * artículo o con cantidad 0. El tipo de descuento (PORCENTAJE|IMPORTE) se pasa tal cual;
     * el dominio lo normaliza.
     */
    public static List<Map<String, Object>> construirLineas(String[] articuloIds, String[] cantidades,
                                                             String[] precios, String[] ivas,
                                                             String[] descuentos, String[] descuentoTipos) {
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
            if (descuentoTipos != null) linea.put("descuentoTipo", at(descuentoTipos, i));
            lineas.add(linea);
        }
        return lineas;
    }

    /** Variante con descuento (valor) sin tipo — delega con descuentoTipos=null. */
    public static List<Map<String, Object>> construirLineas(String[] articuloIds, String[] cantidades,
                                                             String[] precios, String[] ivas,
                                                             String[] descuentos) {
        return construirLineas(articuloIds, cantidades, precios, ivas, descuentos, null);
    }

    /** Variante sin descuento — delega al método principal con descuentos=null. */
    public static List<Map<String, Object>> construirLineas(String[] articuloIds, String[] cantidades,
                                                             String[] precios, String[] ivas) {
        return construirLineas(articuloIds, cantidades, precios, ivas, null, null);
    }

    public static Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.valueOf(value.trim());
    }

    /**
     * Parsea un decimal escrito a la española o con punto: '12,50', '1.234,50', '12.5'.
     * Vacío o null devuelve 0; un texto que no es un número lanza NumberFormatException.
     */
    public static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(normalizarDecimal(value));
    }

    /**
     * Para campos numéricos sueltos de un formulario: null si vienen vacíos y, si no son un número,
     * IllegalArgumentException con un mensaje legible ("{mensajeError}: valor") en lugar de un 400.
     */
    public static BigDecimal parseDecimalCampo(String value, String mensajeError) {
        if (value == null || value.isBlank()) return null;
        try {
            return parseDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensajeError + ": " + value.trim());
        }
    }

    /**
     * Si aparecen coma y punto, el último que aparece es el separador decimal y el otro el de
     * miles ('1.234,50' o '1,234.50'); si solo hay coma, es el decimal.
     */
    private static String normalizarDecimal(String value) {
        String s = value.trim().replace(" ", "").replace("\u00A0", "");
        int coma = s.lastIndexOf(',');
        int punto = s.lastIndexOf('.');
        if (coma >= 0 && punto >= 0) {
            return coma > punto ? s.replace(".", "").replace(',', '.') : s.replace(",", "");
        }
        return coma >= 0 ? s.replace(',', '.') : s;
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
