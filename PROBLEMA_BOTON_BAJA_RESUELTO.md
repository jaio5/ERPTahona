# ✅ PROBLEMA RESUELTO - Botón Dar de Baja en Artículos

**Fecha:** 13 de enero de 2026, 12:35  
**Estado:** ✅ **COMPLETADO**

---

## 🐛 Problema

> "ahora en articulos no se puede seleccionar para borrar"

---

## 🔍 Causa

El FXML tenía el botón configurado con `onAction="#onDarBaja"` pero el controlador **no tenía ese método implementado**.

---

## ✅ Solución

Se implementaron **2 métodos** en `ArticuloController.java`:

### 1. onDarBaja()
- Da de baja o reactiva artículos
- Cambia el campo `activo` (true/false)
- Muestra confirmación
- Recarga automáticamente la tabla

### 2. onVer()
- Muestra detalle completo del artículo
- Código, nombre, descripción, precios, stock, estado
- Diálogo modal

---

## 🎯 Ahora Funciona

✅ **Seleccionar artículo** → Funciona  
✅ **Clic en "Dar de Baja"** → Funciona  
✅ **Confirmación** → Aparece  
✅ **Artículo dado de baja** → OK  
✅ **Tabla actualizada** → OK  
✅ **Mensaje de éxito** → Aparece  

---

## 📊 Compilación

```
mvn compile -DskipTests
✅ BUILD SUCCESS
```

---

## 📚 Documentación

Ver detalle completo en:  
`docs/CORRECCION_BOTON_BAJA_ARTICULOS.md`

---

**🎉 PROBLEMA RESUELTO**

El botón "Dar de Baja" ahora funciona correctamente.

