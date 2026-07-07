package alicanteweb.erp.util;

/**
 * Utilidades para la generación de ficheros CSV.
 *
 * <p>Centraliza el escapado de campos según RFC 4180 (comillas dobles,
 * duplicando las comillas internas) que hasta ahora se reimplementaba en
 * varios controladores y servicios.
 */
public final class Csv {

    /** Marca de orden de bytes (BOM) para que Excel abra el CSV como UTF-8. */
    public static final char BOM = '﻿';

    private Csv() {}

    /** Escapa un valor entre comillas dobles, duplicando las comillas internas. */
    public static String campo(Object valor) {
        String texto = valor != null ? String.valueOf(valor) : "";
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
