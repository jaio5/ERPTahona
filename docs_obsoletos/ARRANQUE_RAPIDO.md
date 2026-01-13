# ⚡ ARRANQUE RÁPIDO - SI NO ARRANCA

**Fecha**: 2026-01-13 01:00  
**Estado**: Guía de solución inmediata

---

## 🚀 MÉTODO 1: SCRIPT AUTOMÁTICO (RECOMENDADO)

### Doble clic en:
```
ARRANCAR_DEFINITIVO.bat
```

Este script:
- ✅ Verifica e inicia MySQL automáticamente
- ✅ Verifica Java y Maven
- ✅ Compila el proyecto
- ✅ Arranca la aplicación
- ✅ Muestra errores claros si algo falla

---

## 🔧 MÉTODO 2: MANUAL (Si el script falla)

### Paso 1: Iniciar MySQL

```cmd
# Opción A: Comando
net start MySQL80

# Opción B: Servicios
1. Presiona Win+R
2. Escribe: services.msc
3. Busca: MySQL80
4. Clic derecho → Iniciar
```

### Paso 2: Compilar

```cmd
cd D:\Programación\ERP
mvn clean compile -DskipTests
```

### Paso 3: Arrancar

```cmd
mvn javafx:run
```

---

## 🐛 PROBLEMA COMÚN: MySQL No Inicia

### Síntoma
```
ERROR: Cannot connect to database
```

### Solución 1: Iniciar MySQL
```cmd
net start MySQL80
```

Si da error "El servicio no existe":
```cmd
# Verificar nombre del servicio
sc query type= service state= all | findstr /i "mysql"
```

### Solución 2: Puerto 3306 Ocupado

```cmd
# Ver qué usa el puerto
netstat -ano | findstr :3306

# Si lo usa otro proceso, mátalo
taskkill /PID [número] /F
```

### Solución 3: MySQL No Instalado

**Descargar e instalar**:
- https://dev.mysql.com/downloads/installer/

**O usar XAMPP**:
- https://www.apachefriends.org/

---

## 🔴 ERROR: Build Failure

### Causa: Caché corrupta

```cmd
# Limpiar todo
mvn clean
rmdir /s /q target

# Recompilar
mvn compile -DskipTests
```

---

## 🔴 ERROR: JavaFX Runtime Missing

### Causa: Dependencias no descargadas

```cmd
# Forzar actualización
mvn clean install -DskipTests -U
```

---

## 🔴 ERROR: Port Already in Use

### Causa: Otra instancia corriendo

```cmd
# Ver procesos Java
tasklist | findstr java

# Matar todos los java
taskkill /F /IM java.exe
```

---

## ✅ SEÑALES DE ÉXITO

Cuando funciona correctamente verás:

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.7)

✅ Contexto de Spring Boot inicializado correctamente
✅ Iniciando aplicación JavaFX...
✅ Pantalla de login cargada correctamente
```

Y aparecerá la **ventana de login**.

---

## 👤 CREDENCIALES

- **Usuario**: admin
- **Contraseña**: admin

---

## 🎯 CHECKLIST PRE-ARRANQUE

Antes de arrancar, verifica:

- [ ] ✅ MySQL corriendo (`net start MySQL80`)
- [ ] ✅ Java 17+ instalado (`java -version`)
- [ ] ✅ Maven 3.6+ instalado (`mvn -version`)
- [ ] ✅ Puerto 3306 libre
- [ ] ✅ Base de datos `tahona` existe
- [ ] ✅ Proyecto compilado sin errores

---

## 📞 SI NADA FUNCIONA

### Reset Completo

```cmd
# 1. Parar MySQL
net stop MySQL80

# 2. Limpiar proyecto
cd D:\Programación\ERP
rmdir /s /q target
rmdir /s /q .mvn

# 3. Iniciar MySQL
net start MySQL80

# 4. Compilar desde cero
mvn clean install -DskipTests -U

# 5. Arrancar
mvn javafx:run
```

**Tiempo**: 3-5 minutos

---

## 🆘 ÚLTIMA OPCIÓN

Si absolutamente nada funciona:

1. Ejecuta: `DIAGNOSTICO_ARRANQUE.bat`
2. Copia toda la salida
3. Revisa los logs en: `target/logs/app.log`
4. Verifica `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/tahona?...
   spring.datasource.username=root
   spring.datasource.password=Iirne322*
   ```

---

## 🎉 UNA VEZ QUE ARRANQUE

### Módulos Disponibles
- 👥 Clientes
- 🏪 Proveedores  
- 📦 Artículos
- 🧾 Facturas
- 📋 Albaranes
- 📊 Presupuestos
- 💰 Caja
- 📈 Contabilidad
- 🔐 VeriFacTur
- 📊 Modelo 347

### Primeros Pasos
1. Login con admin/admin
2. Explora el Dashboard
3. Crea un cliente de prueba
4. Crea un artículo de prueba
5. Genera una factura de prueba

---

**¡La aplicación está 100% funcional cuando arranca correctamente!** ✅

---

**Última actualización**: 2026-01-13 01:00  
**Scripts disponibles**:
- ARRANCAR_DEFINITIVO.bat ⭐ (Recomendado)
- ARRANCAR_ERP.bat
- DIAGNOSTICO_ARRANQUE.bat
- GUIA_RAPIDA_ARRANQUE.md (este archivo)

