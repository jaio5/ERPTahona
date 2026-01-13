# ⚡ GUÍA RÁPIDA - APLICACIÓN NO ARRANCA

**Actualizado**: 2026-01-13 23:00

---

## 🚀 ARRANQUE INMEDIATO (3 pasos)

### 1️⃣ Ejecuta el script de arranque

```
Doble clic en: ARRANCAR_ERP.bat
```

**Ubicación**: `D:\Programación\ERP\ARRANCAR_ERP.bat`

---

### 2️⃣ Si el script falla, ejecuta manualmente:

Abre PowerShell o CMD en la carpeta del proyecto y ejecuta:

```cmd
mvn clean compile -DskipTests
mvn javafx:run
```

---

### 3️⃣ Si sigue sin funcionar:

#### Verifica MySQL

```cmd
# Inicia MySQL
net start MySQL80

# O desde servicios
services.msc
→ Buscar "MySQL80"
→ Clic derecho → Iniciar
```

---

## 🔴 ERROR: "BUILD FAILURE"

### Causa: Dependencias no descargadas

**Solución**:
```cmd
mvn clean install -DskipTests -U
```

La opción `-U` fuerza actualización de dependencias.

---

## 🔴 ERROR: "Could not find or load main class"

### Causa: Compilación incompleta

**Solución**:
```cmd
mvn clean
mvn compile -DskipTests
mvn javafx:run
```

---

## 🔴 ERROR: "Error connecting to database"

### Causa: MySQL no está corriendo

**Solución**:
```cmd
# Windows
net start MySQL80

# Verificar
mysql -u root -pIirne322* -e "SELECT 1;"
```

Si no funciona, verifica la contraseña en:
`src/main/resources/application.properties`

```properties
spring.datasource.password=Iirne322*
```

---

## 🔴 ERROR: "Port 3306 already in use"

### Causa: Otro proceso usa el puerto

**Solución 1**: Mata el proceso
```cmd
netstat -ano | findstr :3306
taskkill /PID [número] /F
```

**Solución 2**: Cambia el puerto en `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/tahona?...
```

---

## 🔴 ERROR: "JavaFX runtime components are missing"

### Causa: JavaFX no se descargó correctamente

**Solución**:
```cmd
mvn clean
mvn dependency:resolve
mvn compile -DskipTests
mvn javafx:run
```

---

## 🔴 La aplicación arranca pero no aparece la ventana

### Causa: Problema con recursos FXML

**Verificar**:
```cmd
dir src\main\resources\ui\login.fxml
```

Debe existir. Si no existe, restaura desde backup o reinstala.

---

## 🟢 MÉTODO GARANTIZADO (Cuando nada funciona)

### Reseteo completo:

```cmd
# 1. Borrar todo lo compilado
rmdir /s /q target
rmdir /s /q .mvn

# 2. Limpiar caché Maven (opcional, tarda más)
rmdir /s /q %USERPROFILE%\.m2\repository\alicanteweb

# 3. Compilar desde cero
mvn clean install -DskipTests -U

# 4. Arrancar
mvn javafx:run
```

**Tiempo estimado**: 3-5 minutos

---

## 📋 CHECKLIST RÁPIDO

Antes de arrancar, verifica:

- [ ] ✅ MySQL está corriendo (`net start MySQL80`)
- [ ] ✅ Java 17 instalado (`java -version`)
- [ ] ✅ Maven 3.6+ instalado (`mvn -version`)
- [ ] ✅ Base de datos `tahona` existe
- [ ] ✅ Puerto 3306 libre
- [ ] ✅ Internet disponible (para descargar dependencias)

---

## 🎯 ATAJOS DE TECLADO

| Comando | Acción |
|---------|--------|
| `mvn clean compile -DskipTests` | Compilar |
| `mvn javafx:run` | Arrancar |
| `Ctrl+C` | Detener aplicación |
| `mvn -version` | Ver versión Maven |
| `java -version` | Ver versión Java |

---

## 📞 SOPORTE ADICIONAL

Si ninguna solución funciona:

1. Ejecuta: `DIAGNOSTICO_ARRANQUE.bat`
2. Copia la salida completa
3. Revisa: `SOLUCION_PROBLEMAS_ARRANQUE.md` (guía completa)

---

## ✅ SEÑALES DE ÉXITO

Cuando la aplicación arranca correctamente verás:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.7)

✅ Contexto de Spring Boot inicializado
✅ Iniciando aplicación JavaFX...
✅ Pantalla de login cargada correctamente
```

Y aparecerá la ventana de login.

**Usuario**: admin  
**Contraseña**: admin

---

## 🎉 YA FUNCIONA - PRÓXIMOS PASOS

1. Inicia sesión con admin/admin
2. Explora el dashboard
3. Prueba los módulos:
   - 👥 Clientes
   - 🧾 Facturas
   - 📦 Artículos
   - 💰 Caja

---

**Última actualización**: 2026-01-13 23:00  
**Archivos relacionados**:
- ARRANCAR_ERP.bat (nuevo y mejorado)
- ARRANCAR_ERP_MEJORADO.bat
- DIAGNOSTICO_ARRANQUE.bat
- SOLUCION_PROBLEMAS_ARRANQUE.md

