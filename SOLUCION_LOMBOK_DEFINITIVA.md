# ✅ SOLUCIÓN DEFINITIVA AL PROBLEMA DE LOMBOK

## 🔥 Problema
Lombok no está generando getters/setters, causando **100+ errores de compilación**.

## ✅ SOLUCIÓN INMEDIATA (5 minutos)

### Paso 1: Instalar Plugin de Lombok en IntelliJ
```
1. File → Settings (Ctrl+Alt+S)
2. Plugins → Marketplace
3. Buscar "Lombok"
4. Instalar "Lombok" by JetBrains
5. Restart IDE
```

### Paso 2: Habilitar Annotation Processing
```
1. File → Settings (Ctrl+Alt+S)
2. Build, Execution, Deployment → Compiler → Annotation Processors
3. ✅ Enable annotation processing
4. Obtain processors from project classpath
5. Apply → OK
```

### Paso 3: Invalidar Cachés
```
1. File → Invalidate Caches...
2. ✅ Clear file system cache and Local History
3. ✅ Clear downloaded shared indexes
4. Invalidate and Restart
```

### Paso 4: Rebuild Project
```
1. Build → Rebuild Project
2. Esperar a que termine la compilación
```

## 🚀 VERIFICACIÓN

Después de estos pasos, ejecutar:
```bash
mvn clean compile -DskipTests
```

Debería compilar sin errores.

## ⚠️ SI AÚN NO FUNCIONA

### Opción A: Delombok Manual
Para cada entidad con problema:
```
1. Abrir la clase (ej: Articulo.java)
2. Click derecho en cualquier parte del código
3. Refactor → Delombok → @Data
4. IntelliJ generará automáticamente todos los getters/setters
5. Eliminar la anotación @Data
6. Repetir para todas las entidades con error
```

### Opción B: Reinstalar Lombok
```bash
# En el directorio del proyecto:
cd "D:\Programación\ERP"
java -jar lombok.jar

# Se abrirá una ventana GUI
# 1. Click en "Specify location..."
# 2. Seleccionar la ruta de IntelliJ IDEA
#    Generalmente: C:\Program Files\JetBrains\IntelliJ IDEA xxxx.x
# 3. Click en "Install/Update"
# 4. Restart IntelliJ
```

## 📋 ENTIDADES CORREGIDAS MANUALMENTE

Ya se añadieron getters/setters explícitos en:
- ✅ `Almacen.java`
- ✅ `Articulo.java`
- ✅ `AlbaranVenta.java`

**Entidades restantes que necesitan corrección:**
- `AsientoContable.java`
- `MovimientoCaja.java`
- Otras entidades con @Data

## 🎯 RESULTADO ESPERADO

Después de aplicar estas soluciones:
```bash
mvn clean install -DskipTests
# [INFO] BUILD SUCCESS
```

Y la aplicación arrancará correctamente con:
```bash
mvn javafx:run
```

---

## 📝 NOTAS IMPORTANTES

1. **Nunca uses solo `@Getter` y `@Setter`** → Usa `@Data` que es más robusta
2. **Siempre habilita Annotation Processing** en IntelliJ
3. **Lombok funciona mejor con IntelliJ 2023.x o superior**
4. Si trabajas en equipo, asegúrate de que todos tengan Lombok instalado

## 🆘 ÚLTIMO RECURSO

Si NADA funciona, eliminar completamente Lombok del proyecto:
```xml
<!-- En pom.xml, eliminar: -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

Y usar IntelliJ para generar todos los getters/setters:
```
Click derecho en clase → Generate (Alt+Insert) → Getters and Setters
```

