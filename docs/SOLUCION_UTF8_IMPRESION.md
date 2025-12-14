# Solución al Problema de Codificación UTF-8 en Impresión de Facturas

## Problema Identificado

Las facturas y albaranes no mostraban correctamente los caracteres especiales españoles (á, é, í, ó, ú, ñ, €) al imprimirse. Esto se debía a problemas de codificación UTF-8.

## Causas

1. **FileWriter sin charset explícito**: En Windows, `FileWriter` usa por defecto el charset del sistema (generalmente Windows-1252), no UTF-8.
2. **Fuente tipográfica**: La fuente monoespaciada 'Courier New' puede tener problemas con algunos caracteres especiales.
3. **Símbolos especiales**: El emoji 🖨️ y el símbolo € pueden no renderizarse correctamente dependiendo del navegador.

## Solución Implementada

### 1. Uso Explícito de UTF-8 al Escribir Archivos

**Antes:**
```java
try (FileWriter writer = new FileWriter(tempFile)) {
    writer.write(html.toString());
}
```

**Después:**
```java
try (OutputStreamWriter writer = new OutputStreamWriter(
        new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
    writer.write(html.toString());
}
```

### 2. Mejora de la Fuente Tipográfica

**Antes:**
```css
font-family: 'Courier New', monospace;
```

**Después:**
```css
font-family: Arial, 'Courier New', 'DejaVu Sans', sans-serif;
```

Arial es una fuente sans-serif muy legible que:
- Soporta perfectamente UTF-8 y caracteres españoles
- Es más moderna y profesional
- Funciona bien en impresión en blanco y negro
- Está disponible en todos los sistemas Windows

### 3. Uso de Entidades HTML

**Símbolo del Euro:**
```java
// Antes: return String.format("%.2f €", value);
// Después: return String.format("%.2f &euro;", value);
```

**Icono de Impresora:**
```html
<!-- Antes: 🖨️ Imprimir -->
<!-- Después: &#x1F5A8; Imprimir -->
```

### 4. Corrección de Errores Tipográficos

Se corrigió "www.panaderiata hona.com" por "www.panaderiatahona.com"

## Archivos Modificados

- `src/main/java/alicanteweb/erp/service/PrintService.java`
  - Añadidos imports: `OutputStreamWriter`, `FileOutputStream`, `StandardCharsets`
  - Actualizado método `generarImpresionFactura()`
  - Actualizado método `generarImpresionAlbaran()`
  - Actualizado método `getHtmlHeader()`
  - Actualizado método `formatMoney()`
  - Corregido texto del footer

## Cómo Probar

1. Compilar el proyecto:
   ```bash
   mvn clean compile
   ```

2. Arrancar la aplicación

3. Ir a Facturas o Albaranes

4. Seleccionar un documento y hacer clic en "Imprimir"

5. Verificar que se muestren correctamente:
   - Caracteres acentuados (á, é, í, ó, ú)
   - La letra ñ
   - El símbolo del euro (€)
   - Todos los textos en español

## Resultado

Ahora todos los documentos se generan correctamente en UTF-8 y los caracteres especiales españoles se muestran perfectamente tanto en pantalla como en impresión.

## Notas Técnicas

- El meta tag `<meta charset="UTF-8">` en el HTML ahora coincide con la codificación real del archivo
- Los archivos temporales se crean en la carpeta temporal del sistema con nombres únicos
- La fuente Arial proporciona mejor legibilidad que Courier New para documentos comerciales
- Las entidades HTML garantizan compatibilidad con todos los navegadores

