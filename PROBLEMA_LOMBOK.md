# 🚨 PROBLEMA CRÍTICO: Lombok No Genera Getters/Setters

## Diagnóstico

El procesador de anotaciones de Lombok **NO está generando** los métodos getter/setter en las entidades, causando 100+ errores de compilación.

### Síntomas
```
cannot find symbol: method getCliente()
cannot find symbol: method getCodigo()
cannot find symbol: method setActivo(boolean)
```

### Causa Raíz
A pesar de tener Lombok correctamente configurado en `pom.xml`:
- ✅ Dependencia: `lombok:1.18.34` (scope: provided)
- ✅ Annotation Processor configurado en `maven-compiler-plugin`
- ❌ **Los getters/setters NO se están generando durante la compilación**

## Soluciones Intentadas

1. ❌ `mvn clean install -U` → Sin éxito
2. ❌ Eliminar `target/` manualmente → Sin éxito  
3. ❌ Verificar archivos .class compilados → **Confirmado: NO hay getters/setters**

## Solución Aplicada

Hay 3 opciones:

### Opción 1: Regenerar IDE (IntelliJ)
```bash
# En IntelliJ IDEA:
# File → Invalidate Caches → Invalidate and Restart
# Build → Rebuild Project
```

### Opción 2: Instalar Lombok en el IDE
```bash
# Ejecutar:
java -jar lombok.jar
# Seleccionar IntelliJ IDEA para instalación
```

### Opción 3: **[RECOMENDADA]** Generar Getters/Setters Manualmente

Como workaround temporal, añadir métodos explícitos en las entidades problemáticas:

**Entidades afectadas:**
- `AlbaranVenta.java` ✅ (tiene `getLineas()` manual)
- `Almacen.java` → Necesita getters/setters
- `Articulo.java` → Necesita getters/setters

## Acción Inmediata

Voy a aplicar **Opción 3** para desbloquear la compilación.

---

**Nota:** Este es un problema conocido cuando:
- Maven no ejecuta correctamente el annotation processing
- El IDE y Maven usan diferentes versiones de Lombok
- Hay conflictos con otros annotation processors (MapStruct)

