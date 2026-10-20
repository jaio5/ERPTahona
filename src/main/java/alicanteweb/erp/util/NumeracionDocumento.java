package alicanteweb.erp.util;

import java.util.List;
import java.util.OptionalLong;
import java.util.function.Supplier;

/**
 * Numeración correlativa por ejercicio de documentos comerciales (PED-AAAA-00001,
 * PRE-AAAA-00001...).
 *
 * <p>El siguiente número se obtiene a partir del máximo existente con el prefijo
 * del ejercicio, no de un count(): así un borrado intermedio no provoca que se
 * reutilice un número ya existente (y choque con la UNIQUE de BD). La numeración
 * reinicia cada año porque el prefijo incluye el ejercicio.
 */
public final class NumeracionDocumento {

    private NumeracionDocumento() {}

    /** Prefijo del ejercicio: {@code SERIE-AAAA-}. */
    public static String prefijo(String serie, int anio) {
        return serie + "-" + anio + "-";
    }

    /**
     * Calcula el siguiente número con el prefijo dado.
     *
     * @param prefijo   prefijo del ejercicio (p. ej. {@code PED-2026-})
     * @param maximo    máximo alfabético de los números con ese prefijo (puede ser null)
     * @param numeros   proveedor de todos los números con el prefijo; solo se consulta
     *                  si el máximo alfabético no tiene sufijo numérico (números
     *                  heredados tipo {@code PED-2026-ABC}, que ordenan por encima de los dígitos)
     */
    public static String siguiente(String prefijo, String maximo, Supplier<List<String>> numeros) {
        OptionalLong ultimo = sufijoNumerico(prefijo, maximo);
        if (ultimo.isEmpty() && maximo != null && numeros != null) {
            ultimo = numeros.get().stream()
                    .map(n -> sufijoNumerico(prefijo, n))
                    .filter(OptionalLong::isPresent)
                    .mapToLong(OptionalLong::getAsLong)
                    .max();
        }
        long siguiente = ultimo.orElse(0L) + 1;
        return prefijo + String.format("%05d", siguiente);
    }

    /** Sufijo numérico tras el prefijo; vacío si no hay o no es numérico. */
    public static OptionalLong sufijoNumerico(String prefijo, String numero) {
        if (numero == null || !numero.startsWith(prefijo)) return OptionalLong.empty();
        String sufijo = numero.substring(prefijo.length()).trim();
        if (sufijo.isEmpty() || sufijo.length() > 18) return OptionalLong.empty();
        for (int i = 0; i < sufijo.length(); i++) {
            char c = sufijo.charAt(i);
            if (c < '0' || c > '9') return OptionalLong.empty();
        }
        return OptionalLong.of(Long.parseLong(sufijo));
    }
}
