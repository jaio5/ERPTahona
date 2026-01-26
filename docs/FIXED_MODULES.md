# Módulos corregidos (registro automático)

Fecha: 2026-01-23

## módulo: presupuestos
- Archivos modificados:
  - src/main/resources/ui/presupuestos_panel.fxml
  - src/main/java/alicanteweb/erp/controller/PresupuestoController.java

- Advertencias detectadas y acciones realizadas:
  - `colBaseImponible` estaba declarado en el controlador pero no existía en el FXML -> Añadida la columna `<TableColumn fx:id="colBaseImponible" .../>` al FXML.
  - `lblEstado` estaba declarado en el controlador pero en el FXML había `lblTotal` -> Reemplazado `lblEstado` por `lblTotal` en el controlador y actualizado su uso.
  - Métodos `handleButtonHover` y `handleButtonExit` estaban sin uso -> Conectados en FXML añadiendo `onMouseEntered`/`onMouseExited` a los botones relevantes.
  - Lambdas y genéricos: se aplicó `<>` (diamond) en las instancias de `TableCell` y se simplificó una lambda a expresión lambda para eliminar avisos de estilo.
  - Concatenación de cadenas en `onVer()` convertida a text block para claridad.
  - Se añadieron botones `Aceptar` y `Rechazar` en el FXML y se enlazaron a `onAceptar()` y `onRechazar()` del controlador.
  - Renombrado de fx:id de la tabla y columnas a `pres_*` para evitar duplicados globales en varios FXML.
  - Añadidos DatePicker `dpFechaDesde` y `dpFechaHasta` en el controlador; se establecen valores por defecto (inicio del mes y fecha actual) y se crean defensivamente si la inyección falla. Se añadieron listeners para re-filtrar cuando cambian.

- Validación:
  - Se ejecutó compilación: `./mvnw -DskipTests package` → BUILD SUCCESS.
  - Comprobación estática del archivo `PresupuestoController.java` → sin errores.

- Notas:
  - No se usaron `@SuppressWarnings` globales; se evitó raw types.
  - Cambios mínimos y reversibles. Si prefieres otra convención de nombres (por ejemplo mantener ids sin prefijo `pres_`), puedo revertir y aplicar un plan alternativo.

---

(Se irán añadiendo entradas por cada módulo corregido.)
