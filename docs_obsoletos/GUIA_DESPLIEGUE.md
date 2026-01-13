# 🚀 GUÍA DE DESPLIEGUE Y PUESTA EN MARCHA

**Versión**: 1.0  
**Fecha**: 2026-01-13  
**Estado**: ✅ Listo para producción

---

## 📋 CHECKLIST PRE-DESPLIEGUE

### ✅ Requisitos del Sistema

- [ ] Java 17 o superior instalado
- [ ] Maven 3.8+ instalado
- [ ] MySQL 8.0+ instalado y funcionando
- [ ] JavaFX 21.0.5 (incluido en dependencias)
- [ ] Sistema operativo: Windows 10/11, Linux, macOS

### ✅ Base de Datos

- [ ] MySQL corriendo en `localhost:3306`
- [ ] Base de datos `tahona` creada
- [ ] Usuario con permisos completos
- [ ] Scripts SQL ejecutados

---

## 🔧 PASOS DE INSTALACIÓN

### 1. Configurar Base de Datos

```bash
# Crear base de datos
mysql -u root -p
CREATE DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# Ejecutar scripts
mysql -u root -p tahona < basesdedatos/01_estructura_base.sql
mysql -u root -p tahona < basesdedatos/02_datos_iniciales.sql
mysql -u root -p tahona < basesdedatos/03_plan_contable.sql
mysql -u root -p tahona < basesdedatos/04_funcionalidad_completa.sql
```

### 2. Configurar application.properties

Editar: `src/main/resources/application.properties`

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useSSL=false&serverTimezone=Europe/Madrid
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Logging
logging.level.alicanteweb.erp=INFO
logging.file.name=target/logs/app.log

# VeriFacTur (opcional)
verifactu.aeat.enabled=false
verifactu.keystore.path=/path/to/certificado.p12
verifactu.keystore.password=password
verifactu.key.alias=alias
```

### 3. Compilar Proyecto

```bash
cd "D:\Programación\ERP"
mvn clean compile
```

### 4. Ejecutar Aplicación

```bash
mvn javafx:run
```

O crear ejecutable:

```bash
mvn clean package
java -jar target/ERP-0.0.1.jar
```

---

## 👤 PRIMER INICIO

### Credenciales por Defecto

```
Usuario: admin
Contraseña: admin
```

**⚠️ IMPORTANTE**: Cambiar la contraseña en el primer inicio.

### Configurar Datos de Empresa

1. Ir a **Configuración** → **Datos de Empresa**
2. Rellenar:
   - Nombre fiscal
   - NIF/CIF
   - Dirección completa
   - Teléfono
   - Email
   - Datos de contacto

---

## 📊 CONFIGURACIÓN INICIAL

### 1. Plan de Cuentas

**✅ Ya precargado** con Plan General Contable español.

Verificar en: **Contabilidad** → **Plan de Cuentas**

### 2. Crear Usuario Adicional

```
Menú → Usuarios → Nuevo Usuario
```

### 3. Configurar Impresora

El sistema detectará automáticamente las impresoras disponibles.

Para configurar por defecto:
- **Windows**: Panel de Control → Dispositivos → Impresoras
- **Linux**: Sistema → Impresoras

### 4. Primera Caja

```java
// Abrir caja del día
Caja → Abrir Caja
Saldo inicial: 100.00 €
```

---

## 🔐 CONFIGURACIÓN VERIFACTUR (OPCIONAL)

### Si necesitas envío real a AEAT:

1. **Obtener Certificado Digital**
   - FNMT (Fábrica Nacional de Moneda y Timbre)
   - Formato: PKCS#12 (.p12)

2. **Colocar certificado**
   ```
   src/main/resources/certificados/
   ```

3. **Configurar en application.properties**
   ```properties
   verifactu.aeat.enabled=true
   verifactu.keystore.path=classpath:certificados/certificado.p12
   verifactu.keystore.password=tu_password
   verifactu.key.alias=tu_alias
   ```

4. **Probar en entorno de pruebas primero**
   ```properties
   verifactu.aeat.endpoint=https://prewww2.aeat.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu
   ```

---

## 📝 OPERACIÓN DIARIA

### Inicio del Día

1. **Abrir Caja**
   - Caja → Abrir Caja
   - Ingresar saldo inicial

2. **Revisar Pendientes**
   - Pedidos → Estado: PENDIENTE
   - Presupuestos → Por vencer

### Durante el Día

1. **Crear Pedidos**
   ```
   Pedidos → Nuevo Pedido
   ```

2. **Generar Albaranes**
   ```
   Pedido → Convertir a Albarán
   ```

3. **Emitir Facturas**
   ```
   Albarán → Convertir a Factura
   O
   Presupuesto → Convertir a Factura
   ```

4. **Cobrar Facturas**
   ```
   Factura → Cobrar
   (Se registra en Caja automáticamente)
   ```

5. **Imprimir Documentos**
   ```
   Factura → Imprimir
   (Detecta impresora automáticamente)
   ```

### Fin del Día

1. **Arqueo de Caja**
   ```
   Caja → Arqueo
   Verificar diferencias
   ```

2. **Cerrar Caja**
   ```
   Caja → Cerrar Caja
   Saldo final
   ```

3. **Verificar Contabilidad**
   ```
   Contabilidad → Libro Diario
   Verificar cuadre
   ```

---

## 📆 OPERACIONES MENSUALES

### Fin de Mes

1. **Facturar Albaranes Pendientes**
   ```
   Albaranes → Seleccionar cliente
   → Convertir múltiples a Factura
   ```

2. **Revisar Balance**
   ```
   Contabilidad → Balance de Sumas y Saldos
   ```

3. **Generar Reportes**
   ```
   Reportes → Mes actual
   - Facturas emitidas
   - Cobros realizados
   - Gastos
   ```

---

## 📅 OPERACIONES ANUALES

### Fin de Año Fiscal

1. **Generar Modelo 347**
   ```
   Fiscal → Modelo 347
   Ejercicio: 2025
   → Generar
   → Validar
   → Exportar BOE
   ```

2. **Cierre Contable**
   ```
   Contabilidad → Asientos de Cierre
   ```

3. **Backup Completo**
   ```bash
   mysqldump -u root -p tahona > backup_2025.sql
   ```

---

## 🔍 SOLUCIÓN DE PROBLEMAS

### Error: No conecta con MySQL

**Solución:**
```bash
# Verificar que MySQL esté corriendo
mysql -u root -p

# Verificar puerto
netstat -an | findstr 3306
```

### Error: No carga la aplicación

**Solución:**
```bash
# Limpiar y recompilar
mvn clean
mvn compile
mvn javafx:run
```

### Error: No detecta impresoras

**Solución:**
- Verificar drivers de impresora instalados
- Configurar impresora por defecto en el sistema
- Reiniciar aplicación

### Error: Certificado VeriFacTur inválido

**Solución:**
```properties
# Deshabilitar temporalmente
verifactu.aeat.enabled=false
```

---

## 📞 SOPORTE

### Logs de Aplicación

```
target/logs/app.log
```

### Nivel de Log

Cambiar en `application.properties`:
```properties
# Para más detalle
logging.level.alicanteweb.erp=DEBUG

# Para producción
logging.level.alicanteweb.erp=INFO
```

### Base de Datos

```bash
# Backup
mysqldump -u root -p tahona > backup.sql

# Restore
mysql -u root -p tahona < backup.sql
```

---

## ⚡ OPTIMIZACIÓN

### Para Mejor Rendimiento

1. **Aumentar memoria JVM**
   ```bash
   mvn javafx:run -Djavacpp.platform.custom=true \
     -Xms512m -Xmx2048m
   ```

2. **Índices de Base de Datos**
   ```sql
   -- Ya incluidos en scripts SQL
   CREATE INDEX idx_factura_fecha ON facturas(fecha);
   CREATE INDEX idx_factura_cliente ON facturas(cliente_id);
   ```

3. **Caché de Hibernate**
   ```properties
   spring.jpa.properties.hibernate.cache.use_second_level_cache=true
   ```

---

## 🎯 CHECKLIST POST-INSTALACIÓN

- [ ] Aplicación arranca correctamente
- [ ] Login funciona con admin/admin
- [ ] Se pueden ver clientes existentes
- [ ] Se puede crear un cliente nuevo
- [ ] Se puede crear una factura
- [ ] Se genera PDF de factura
- [ ] Se puede imprimir
- [ ] Caja se puede abrir/cerrar
- [ ] Contabilidad muestra asientos
- [ ] Modelo 347 genera datos

---

## 🎉 ¡LISTO PARA PRODUCCIÓN!

Tu ERP está configurado y listo para usar.

**Funcionalidad disponible:**
- ✅ Gestión comercial completa
- ✅ Contabilidad automática
- ✅ Control de caja
- ✅ Reportes e impresión
- ✅ Modelo 347

**Documentación:**
- `GUIA_USO_MODULOS.md` - Guía de uso detallada
- `IMPLEMENTACION_FINAL_COMPLETA.md` - Resumen de implementación

---

**Última actualización**: 2026-01-13  
**Versión**: 1.0  
**Soporte**: Ver documentación completa en el proyecto

