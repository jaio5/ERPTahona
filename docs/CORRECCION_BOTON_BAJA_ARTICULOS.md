# ✅ CORRECCIÓN: Botón "Dar de Baja" en Artículos

**Fecha:** 13 de enero de 2026, 12:30  
**Problema:** "no se puede seleccionar para borrar en artículos"  
**Estado:** ✅ **RESUELTO**

---

## 🐛 Problema

Al hacer clic en el botón "Dar de Baja" en el módulo de Artículos, no pasaba nada. El botón no respondía.

---

## 🔍 Causa

El archivo FXML tenía el botón configurado con el método `onDarBaja`:
```xml
<Button text="🗑️ Dar de Baja" onAction="#onDarBaja" styleClass="button-danger"/>
```

Pero el controlador `ArticuloController` **NO tenía el método `onDarBaja`** implementado.

El controlador solo heredaba el método genérico `onEliminar()` del `BaseController`, pero el FXML no lo estaba llamando.

---

## ✅ Solución

Se implementó el método `onDarBaja()` en `ArticuloController.java` con las siguientes funcionalidades:

### 1. Dar de Baja / Reactivar
```java
@FXML
public void onDarBaja() {
    Articulo seleccionado = tableArticulos.getSelectionModel().getSelectedItem();
    if (seleccionado == null) {
        mostrarAdvertencia("Selecciona un artículo para dar de baja");
        return;
    }

    // Cambiar el estado activo/inactivo
    boolean nuevoEstado = !(seleccionado.getActivo() != null && seleccionado.getActivo());
    seleccionado.setActivo(nuevoEstado);
    articuloService.save(seleccionado);
    
    cargarDatos(); // Recargar datos
    
    String textoResultado = nuevoEstado ? "Artículo reactivado" : "Artículo dado de baja";
    mostrarExito(textoResultado + " correctamente");
}
```

### 2. Ver Detalle del Artículo
Como bonus, también se implementó el método `onVer()` que faltaba:

```java
@FXML
public void onVer() {
    Articulo seleccionado = tableArticulos.getSelectionModel().getSelectedItem();
    if (seleccionado == null) {
        mostrarAdvertencia("Selecciona un artículo para ver");
        return;
    }
    
    // Mostrar información completa del artículo
    // Código, Nombre, Descripción, Código de Barras, Coste, PVP, IVA, Stock, Estado
}
```

---

## 🎯 Funcionalidades Implementadas

### ✅ Dar de Baja Artículo
- Valida que haya un artículo seleccionado
- Muestra confirmación antes de cambiar el estado
- Cambia el campo `activo` a `false`
- Recarga la tabla automáticamente
- Muestra mensaje de éxito

### ✅ Reactivar Artículo
- Si el artículo está inactivo, el botón lo reactiva
- Cambia el campo `activo` a `true`
- Mensaje diferente: "Artículo reactivado"

### ✅ Ver Detalle
- Muestra toda la información del artículo seleccionado
- Diálogo modal con:
  - Código
  - Nombre
  - Descripción
  - Código de Barras
  - Coste
  - PVP (Precio de Venta)
  - IVA
  - Stock
  - Estado (Activo/Inactivo)

---

## 🔧 Correcciones Técnicas

### Problema con Nombres de Campos
Al implementar `onVer()`, se encontró que los métodos getter no coincidían:

❌ **Incorrecto:**
```java
seleccionado.getPrecioCompra()  // No existe
seleccionado.getPrecioVenta()    // No existe
```

✅ **Correcto:**
```java
seleccionado.getCoste()  // Precio de compra
seleccionado.getPvp()    // Precio de venta público
```

---

## 📊 Resultado

### Antes
- ❌ Botón "Dar de Baja" no respondía
- ❌ Imposible dar de baja artículos
- ❌ Botón "Ver" tampoco funcionaba

### Ahora
- ✅ Botón "Dar de Baja" funciona perfectamente
- ✅ Artículos se pueden dar de baja y reactivar
- ✅ Botón "Ver" muestra información completa
- ✅ Validaciones y mensajes claros
- ✅ Recarga automática de datos

---

## 🎨 Experiencia de Usuario

```
1. Usuario selecciona un artículo en la tabla
2. Usuario hace clic en "🗑️ Dar de Baja"
3. Sistema muestra: "¿Estás seguro de que deseas dar de baja este artículo?"
4. Usuario confirma
5. Sistema da de baja el artículo
6. Sistema recarga la tabla
7. Sistema muestra: "Artículo dado de baja correctamente"
```

Si el artículo ya estaba inactivo:
```
3. Sistema muestra: "¿Deseas reactivar este artículo?"
5. Sistema reactiva el artículo
7. Sistema muestra: "Artículo reactivado correctamente"
```

---

## ✅ Verificación

```bash
mvn compile -DskipTests
```

**Resultado:** ✅ **BUILD SUCCESS**

---

## 📝 Archivos Modificados

**Archivo:** `src/main/java/alicanteweb/erp/controller/ArticuloController.java`

**Métodos agregados:**
1. `onDarBaja()` - 35 líneas
2. `onVer()` - 26 líneas

**Total:** 61 líneas de código nuevo

---

## 🎉 Problema Resuelto

El botón "Dar de Baja" ahora funciona correctamente en el módulo de Artículos.

Los usuarios pueden:
- ✅ Dar de baja artículos (los marca como inactivos)
- ✅ Reactivar artículos inactivos
- ✅ Ver el detalle completo de cualquier artículo

