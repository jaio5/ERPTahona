# Pipeline de implementación — Impresión por lotes, IVA configurable, formato editable y descuentos

> Documento de planificación. Describe, como pipeline por fases, cómo implementar cuatro
> capacidades nuevas en el ERP con **código limpio**, reutilizando lo que ya existe y
> respetando las restricciones fiscales (RRSIF / VeriFactu). Cada fase es **una PR
> independiente y desplegable**, en la línea de las anteriores del proyecto.

Fecha: 2026-07-08 · Rama base: `main`

> **Estado (2026-07-08): pipeline COMPLETO.** ✅ Fase 1 (descuentos), ✅ Fase 2 (tipos de IVA
> configurables), ✅ Fase 3 (impresión por lotes), ✅ Fase 4 (campos personalizados). Todo en
> `main` y `desarrollo`, con 305 tests en verde.

---

## 1. Objetivo

Añadir de forma incremental:

1. **Cola de impresión / impresión por lotes**: seleccionar varias facturas o albaranes y
   generarlos/imprimirlos todos de una vez cómodamente.
2. **Tipos de IVA configurables** desde Ajustes (por si cambian los tipos legales).
3. **Formato de impresión editable**: poder añadir cosas para un cliente concreto, p. ej. un
   **campo exclusivo de ese cliente** en su factura/albarán.
4. **Descuentos**: por **línea** (artículo concreto de ese documento) y/o por **documento
   completo**, ambos disponibles al crear la factura o el albarán.

---

## 2. Principios de diseño (código limpio)

- **Reutilizar antes que crear.** Ya existen piezas que son la base de todo esto:
  - `util/FinancialMath` — aritmética financiera con escala/redondeo fiscal. Se **amplía**, no se duplica.
  - `service/ImpresionService` — render Thymeleaf → iText, `desgloseIva(...)`, `resumenAlbaran(...)`, carga de logo.
  - `service/DocumentoService` — punto único de cálculo de totales (`guardarLineasFactura/Albaran`).
  - `controller/web/DocumentoParserUtil.construirLineas(...)` — ya admite `descuentos` (variante de 5 args).
  - Plantillas `templates/pdf/factura.html` y `albaran.html` (formato clásico).
- **Un solo lugar por responsabilidad.** El cálculo de bases/IVA/descuentos vive en el dominio
  (`DocumentoService` + `FinancialMath`), nunca en controladores ni plantillas.
- **Dominio explícito.** Modelar conceptos (TipoImpositivo, Descuento) como entidades/VOs, no
  como columnas sueltas dispersas.
- **Inmutabilidad fiscal (RRSIF).** Las facturas `EMITIDA/PAGADA/VENCIDA/ANULADA/RECTIFICADA`
  son inalterables (guards `@PreUpdate` en `Factura`/`FacturaLinea`). Todo lo editable
  (descuentos, líneas) solo aplica en `BORRADOR`. Cambiar el catálogo de IVA **no** recalcula
  documentos ya emitidos (se guarda el tipo aplicado en la línea, no una FK viva).
- **Sin edición de HTML crudo por el usuario.** El "formato editable" se resuelve con
  **datos configurables** (campos personalizados, textos, toggles), no dejando editar la
  plantilla (evita romper el PDF y riesgos de inyección en iText).
- **Migraciones aditivas** (Flyway `V44+`), columnas `NULL`/con default, compatibles con MySQL 8.
- **Tests primero en el dominio** (cálculos de descuento y prorrateo de IVA) y verificación de
  render del PDF por muestreo.

### Mapa de reutilización

| Necesidad | Se apoya en | Acción |
|-----------|-------------|--------|
| Descuento de línea | `FinancialMath.subtotalConDescuento`, `FacturaLinea.descuento` | Exponer en albarán + permitir importe fijo |
| Descuento global | `DocumentoService` (suma de bases) | Añadir prorrateo por tipo de IVA |
| Desglose IVA en PDF | `ImpresionService.desgloseIva` | Sin cambios (ya agrupa por `linea.iva`) |
| Impresión lote | `ImpresionService.generar*Pdf` | Nuevo servicio que fusiona PDFs (iText) |
| Campos por cliente | Plantillas clásicas | Zona genérica de "campos extra" |

---

## 3. Estado actual (línea base)

| Capacidad | Estado hoy |
|-----------|-----------|
| Descuento **por línea** | ✅ Existe (`descuento` %, aplicado en `FinancialMath`). Factura lo pasa por formulario; **albarán no**. Solo %, no importe fijo. La tarifa de cliente puede pisar el manual. |
| Descuento **global** | ❌ No existe. |
| Catálogo de **tipos de IVA** | ❌ No existe. `linea.iva` es un `DECIMAL` libre; `factura.tipo_impositivo` es una columna suelta. |
| **Impresión por lotes** | ❌ Solo `descargarPdf`/`imprimir` de un documento. |
| **Campos por cliente / formato** | ❌ Plantillas estáticas. |
| Inmutabilidad fiscal | ✅ Guards `@PreUpdate`/`@PreRemove` en `Factura`/`FacturaLinea`. |

---

## 4. Fases del pipeline

Orden recomendado por dependencias y valor. Cada fase → **una PR**.

```
Fase 1  Descuentos (línea + global)        ──┐
Fase 2  Tipos de IVA configurables          ├─ independientes entre sí
Fase 3  Impresión por lotes / cola          │  (pueden paralelizarse)
Fase 4  Formato editable (campos cliente)  ──┘
```

Recomendado empezar por **Fase 1** (afecta al núcleo de facturación) y **Fase 3** (alto valor
percibido, bajo acoplamiento).

---

### Fase 1 — Descuentos por línea y por documento

**Objetivo.** Al crear/editar factura o albarán, poder aplicar:
- un descuento **por línea** (artículo concreto de ese documento), y/o
- un descuento **global** sobre el total del documento.
Ambos, simultáneamente si hace falta. Expresables como **% o importe fijo**.

**Modelo de datos (`V44__descuentos.sql`)**
- `factura_lineas`: (ya hay `descuento` %). Añadir `descuento_tipo VARCHAR(10)` (`PORCENTAJE|IMPORTE`)
  y renombrar semánticamente el valor: mantener `descuento` como valor y `descuento_tipo` para
  interpretarlo (default `PORCENTAJE` para no romper datos existentes).
- `albaran_venta_lineas`: mismo par (`descuento` ya existe, añadir `descuento_tipo`).
- `facturas` y `albaranes_venta`: `descuento_global_tipo VARCHAR(10)`, `descuento_global_valor DECIMAL(10,2)`.

**Dominio / servicios**
- Ampliar `FinancialMath`:
  - `descuentoLinea(base, tipo, valor)` → importe de descuento de una línea (respeta % vs importe).
  - `prorratearDescuentoGlobal(basesPorTipoIva, tipoGlobal, valorGlobal)` → reparte el descuento
    global **proporcionalmente entre las bases de cada tipo de IVA** (imprescindible para que el
    desglose de IVA siga cuadrando y sea correcto en VeriFactu).
- `DocumentoService.guardarLineasFactura/Albaran`:
  1. Calcular base por línea con su descuento de línea.
  2. Sumar bases por tipo de IVA.
  3. Aplicar descuento global prorrateado por tipo.
  4. Recalcular `baseImponible`, `totalIva`, `total`.
  - Corregir `resolverPrecioDescuento`: el descuento **manual** del documento debe poder ganar a
    la tarifa del cliente (hoy solo se usa la tarifa si el manual es 0).

**API / controladores**
- `FacturaWebController.guardar` / `AlbaranWebController.guardar`: añadir params
  `descuentoGlobalTipo`, `descuentoGlobalValor`; en albarán, pasar el array `lineaDescuento`
  (usar la variante de 5 args de `construirLineas`, ya disponible).

**UI**
- `templates/facturas/formulario.html` y `albaranes/formulario.html`:
  - Columna "Dto." por línea (valor + selector %/€).
  - Bloque "Descuento global" (valor + selector %/€) con recálculo del total en vivo (JS).
- Vistas `ver.html`: mostrar descuentos aplicados.

**PDF**
- `factura.html`: la columna existe implícitamente (el `total` de línea ya refleja el descuento).
  Añadir, si hay descuento global, una fila "Descuento" en el pie antes del TOTAL.
- `desgloseIva` seguirá cuadrando porque opera sobre `linea.total` (ya neteado) — pero hay que
  **restar el prorrateo global** de cada base; ajustar `ImpresionService.desgloseIva` para recibir
  las bases ya netas del dominio, o recomputar el prorrateo de forma idéntica.

**Tests**
- `FinancialMathTest`: casos de % vs importe, prorrateo con 2–3 tipos de IVA, redondeos.
- `DocumentoServiceTest`: totales con dto. de línea, global, y ambos; verificar suma de cuotas.
- Render de muestra (factura con dto. global + varios IVA) revisado visualmente.

**Criterios de aceptación**
- Base + Σcuotas = Total, con dto. de línea y global combinados.
- El desglose por tipo de IVA del PDF cuadra al céntimo.
- Documentos emitidos no se ven afectados (inalterabilidad).

---

### Fase 2 — Tipos de IVA configurables desde Ajustes

**Objetivo.** Gestionar el catálogo de tipos de IVA (p. ej. 0/4/5/10/21 y recargos) y usarlo como
desplegable en las líneas, sin teclear números a mano.

**Modelo (`V45__tipos_impositivos_catalogo.sql`)**
- `tipos_impositivos`: `id, nombre, porcentaje DECIMAL(5,2), recargo_equivalencia DECIMAL(5,2)
  NULL, activo BOOLEAN, orden INT, es_defecto BOOLEAN`.
- Semilla con los tipos vigentes en España.
- **No** poner FK desde `factura_lineas`: la línea sigue guardando el **valor** `iva` aplicado
  (histórico e inmutable). El catálogo solo alimenta el desplegable en el momento de crear.

**Dominio / servicios**
- Entidad `TipoImpositivo` + `TipoImpositivoRepository` + `TipoImpositivoService`
  (CRUD, orden, activar/desactivar, marcar por defecto, validación 0–100).

**UI**
- Nueva pantalla en Ajustes (`/web/ajustes/tipos-iva`) con lista + alta/edición, protegida por el
  permiso `configuracion`.
- En los formularios de línea, `<select>` de IVA poblado desde el catálogo (con opción de valor
  libre para compatibilidad).

**Tests**
- `TipoImpositivoServiceTest` (CRUD, validaciones, único por defecto).
- Controller/web coverage de la pantalla.

**Criterios de aceptación**
- Cambiar/añadir un tipo afecta solo a documentos **nuevos**; los emitidos conservan su tipo.
- Recargo de equivalencia opcional por tipo, disponible para el pie de la factura.

---

### Fase 3 — Cola de impresión / impresión por lotes

**Objetivo.** Seleccionar varias facturas o albaranes desde el listado y obtener **un único PDF
combinado** (o descarga múltiple) para imprimir todo de una vez.

**Diseño**
- Preferir **un solo PDF fusionado** (más cómodo para imprimir) usando el `PdfMerger` de iText
  (`com.itextpdf.kernel.utils.PdfMerger`), reutilizando `ImpresionService.generar*Pdf` por
  documento y concatenando. Alternativa/futuro: ZIP con un PDF por documento.
- "Cola" en dos niveles:
  - **Mínimo viable**: selección múltiple en el listado → acción "Imprimir seleccionados" → PDF
    combinado en streaming. Sin persistencia.
  - **Evolución (opcional)**: cola persistente por usuario (`cola_impresion`) para ir acumulando
    documentos entre pantallas y vaciarla al imprimir.

**Servicios**
- `ImpresionService.generarLotePdf(List<Documento>)` → `byte[]`/`File` combinando por `PdfMerger`.
- Reutiliza `registrarEventoDocumento` para dejar traza de la impresión por lote.

**API / controladores**
- `FacturaWebController` / `AlbaranWebController`: endpoint `POST .../imprimir-lote` que recibe
  `ids[]` y devuelve el PDF combinado (`Content-Disposition: attachment`).
- Reglas: validar que los ids existen y permisos de impresión; ignorar borradores si procede.

**UI**
- `templates/facturas/lista.html` y `albaranes/lista.html`: checkbox por fila + "seleccionar
  todo" + barra de acciones flotante "Imprimir seleccionados (N)".
- Reutilizar utilidades JS existentes de la remodelación (selección/acciones en tabla).

**Tests**
- `ImpresionServiceTest.generarLotePdf` (nº de páginas = suma, cabeceras correctas).
- Controller coverage del endpoint de lote (ids válidos/ inválidos, vacío, permisos).

**Criterios de aceptación**
- Seleccionar N documentos produce un PDF con los N, cada uno en su(s) página(s).
- Documentos mezclados no permitidos entre tipos (facturas y albaranes por separado).

---

### Fase 4 — Formato de impresión editable (campos por cliente)

**Objetivo.** Poder añadir información específica para un cliente en su factura/albarán (p. ej. un
campo/etiqueta exclusiva), sin tocar código ni editar HTML crudo.

**Enfoque limpio: campos personalizados configurables**
- `campos_personalizados` (`V46`): `id, ambito VARCHAR (CLIENTE|EMPRESA), cliente_id NULL,
  etiqueta, valor, ubicacion VARCHAR (CABECERA|CLIENTE|OBSERVACIONES|PIE), documento VARCHAR
  (FACTURA|ALBARAN|AMBOS), orden INT, activo BOOLEAN`.
- Zona genérica en las plantillas clásicas que itera los campos por `ubicacion` y los pinta
  (etiqueta: valor). El PDF ya recibe `empresa`/`cliente`; añadir `camposExtra` al contexto desde
  `ImpresionService`.
- Además, exponer en Ajustes toggles/textos "de formato" sin editar plantilla: texto de pie
  personalizable, mostrar/ocultar QR (cuando no sea obligatorio), nota legal, etc. (en
  `EmpresaConfig` o tabla `preferencias_impresion`).

**Servicios / UI**
- `CampoPersonalizadoService` (CRUD) + pantalla en la ficha de **Cliente** ("Campos en sus
  documentos") y/o en Ajustes.
- `ImpresionService` compone `camposExtra` filtrando por cliente + tipo de documento + ubicación.

**Tests**
- Servicio CRUD + filtrado por ámbito/ubicación.
- Render de una factura con un campo exclusivo de cliente en cabecera y otro en pie.

**Criterios de aceptación**
- Un campo definido para el cliente X aparece solo en los documentos de X, en la zona elegida.
- Ningún usuario puede romper el PDF (no hay HTML libre; los valores se escapan).

**Nota / evolución.** Si en el futuro se requiere control total del layout, valorar un motor de
plantillas versionadas por cliente (bloques predefinidos y seguros), nunca HTML arbitrario.

---

## 5. Consideraciones transversales

- **Inalterabilidad (RRSIF).** Descuentos, IVA y campos solo se editan en `BORRADOR`. El catálogo
  de IVA no toca documentos emitidos (la línea guarda el valor histórico).
- **VeriFactu.** Cualquier cambio en base/IVA/descuento debe reflejarse **antes** de emitir; una
  vez emitida y firmada, la huella cubre esos importes. El prorrateo del descuento global por tipo
  de IVA es obligatorio para que el desglose remitido a la AEAT cuadre.
- **Decimales y locale.** Mantener el formato español (coma decimal, precios a 4 decimales) usado
  en las plantillas; centralizar formateos.
- **Permisos.** Reutilizar `@permisos.puede('configuracion'|'facturas'|'albaranes', ...)`.
- **Rendimiento del lote.** Generar PDFs en streaming y liberar recursos (`try-with-resources`);
  límite razonable de documentos por lote.

## 6. Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|-----------|
| Descuento global rompe el desglose por tipo de IVA | Prorrateo proporcional + tests de suma de cuotas |
| Editar tipos de IVA altera documentos pasados | La línea guarda el valor aplicado; catálogo solo para altas nuevas |
| Impresión por lotes pesada | Streaming, límite de N, medición |
| "Formato editable" derivando en HTML libre | Campos de datos configurables, sin plantillas editables por el usuario |
| Migraciones sobre tablas grandes | Columnas aditivas `NULL`/default; probadas en `FlywayMySqlMigrationTest` |

## 7. Orden de ejecución y entregables

1. **PR-1 Fase 1** — Descuentos (dominio + UI + PDF + tests).
2. **PR-2 Fase 3** — Impresión por lotes (alto valor, bajo acoplamiento).
3. **PR-3 Fase 2** — Catálogo de tipos de IVA.
4. **PR-4 Fase 4** — Campos por cliente / formato.

Cada PR: build verde, `mvn test` (suite completa) OK, verificación de render del PDF donde aplique,
y actualización de `docs/` si cambia el comportamiento visible.

## 8. Checklist por fase (Definition of Done)

- [ ] Migración Flyway aditiva y validada en MySQL (`FlywayMySqlMigrationTest`).
- [ ] Lógica de negocio en dominio/servicio, no en controlador ni plantilla.
- [ ] Reutilización de `FinancialMath` / `ImpresionService` / `DocumentoParserUtil`.
- [ ] Respetada la inalterabilidad fiscal (solo `BORRADOR` editable).
- [ ] Tests unitarios de cálculo + coverage de controlador + render de muestra.
- [ ] Suite completa verde (271+ tests).
- [ ] Sin regresiones en el formato clásico de factura/albarán.
