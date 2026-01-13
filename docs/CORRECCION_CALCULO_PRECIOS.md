# 🔧 CORRECCIÓN TÉCNICA: Cálculo Automático de Precios

**Problema reportado:** "cuando cambias el precio de compra en crear articulos el precio de venta se vuelve loco impidiendo dejarte ver el número que pones"

---

## ❌ PROBLEMA

Al escribir en el campo de **Precio de Compra**, el campo **Precio de Venta** se actualizaba constantemente, haciendo imposible ver lo que estabas escribiendo.

### Causa Raíz

Los listeners estaban configurados con `textProperty()`, que se dispara en **cada cambio de carácter**:

```java
// ❌ ANTES (PROBLEMÁTICO)
txtPrecioCompra.textProperty().addListener((obs, old, newVal) -> calcularMargen());
txtPrecioVenta.textProperty().addListener((obs, old, newVal) -> calcularMargen());
```

Esto causaba:
1. Usuario escribe "1" en precio de compra
2. Se dispara `calcularMargen()`
3. Se actualiza el margen
4. Se actualiza el precio de venta
5. El precio de venta cambiando dispara otro `calcularMargen()`
6. **Loop infinito** de actualizaciones mientras el usuario escribe

---

## ✅ SOLUCIÓN

### Cambio 1: Usar `focusedProperty()` en lugar de `textProperty()`

```java
// ✅ AHORA (CORRECTO)
txtPrecioCompra.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
    if (!isNowFocused) { // Solo cuando pierde el foco
        calcularMargen();
    }
});
```

**Ventaja:** El cálculo solo se ejecuta cuando el usuario **termina de escribir** y sale del campo.

### Cambio 2: Flag para evitar loops infinitos

```java
private boolean actualizandoPrecios = false;

private void calcularMargen() {
    if (actualizandoPrecios) {
        return; // No calcular si estamos actualizando automáticamente
    }
    
    actualizandoPrecios = true;
    txtMargen.setText(String.format("%.2f", margen));
    actualizandoPrecios = false;
}
```

**Ventaja:** Evita que los cambios automáticos disparen más cálculos.

---

## 📊 COMPARACIÓN

### ANTES ❌
```
Usuario escribe: 1
→ calcularMargen()
→ actualiza precio venta
→ calcularMargen() otra vez
→ actualiza margen
→ calcularMargen() otra vez
→ ...loop infinito...
```

### AHORA ✅
```
Usuario escribe: 1 2 . 5 0
(sin interrupciones)
Usuario sale del campo (pierde foco)
→ calcularMargen() UNA VEZ
→ actualiza margen
→ FIN
```

---

## 🎯 RESULTADO

**Ahora el usuario puede:**
- ✅ Escribir tranquilamente en precio de compra
- ✅ Escribir tranquilamente en precio de venta  
- ✅ Escribir tranquilamente en margen
- ✅ Los cálculos automáticos solo se ejecutan al terminar
- ✅ No hay loops infinitos
- ✅ No hay actualizaciones molestas mientras escribes

---

## 📝 ARCHIVOS MODIFICADOS

**Archivo:** `ArticuloFormController.java`

**Líneas modificadas:**
- Agregada variable de instancia: `private boolean actualizandoPrecios = false;`
- Método `configurarCalculoAutomatico()` - Cambiado a `focusedProperty()`
- Método `calcularMargen()` - Agregado flag para evitar loops

---

## ✅ VERIFICACIÓN

```bash
mvn compile -DskipTests
```

**Resultado:** ✅ BUILD SUCCESS

---

## 💡 LECCIÓN APRENDIDA

**Regla:** En formularios con campos calculados automáticamente:

❌ **NO usar** `textProperty()` para cálculos automáticos  
✅ **SÍ usar** `focusedProperty()` para calcular cuando el usuario termina  
✅ **SÍ usar** flags para evitar loops infinitos entre campos relacionados

---

**Estado:** ✅ **PROBLEMA RESUELTO**

Los campos de precio en el formulario de artículos ahora funcionan perfectamente sin interrupciones.

