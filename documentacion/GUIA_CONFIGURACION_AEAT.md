# 🚀 GUÍA COMPLETA: CONFIGURACIÓN PARA ENVÍO A LA AEAT

## 📋 ESTADO ACTUAL

✅ **Lo que YA está implementado:**
- Flujo de facturación de 3 estados (BORRADOR → REVISION → EMITIDA)
- Generación de XML según especificaciones Verifactu
- Sistema de hash SHA-256 con blockchain
- Generación de código QR
- Registro de evidencias
- Cumplimiento legal al 85%

⚠️ **Lo que FALTA configurar:**
- Certificado digital de la AEAT
- Datos de empresa (GRUPO BABO)
- Configuración de conexión con AEAT
- Pruebas con entorno de preproducción

---

## 🎯 PASOS PARA HACER TODO FUNCIONAL

### PASO 1: Configurar Datos de Empresa (OBLIGATORIO)

Primero necesitas añadir los datos de GRUPO BABO a la base de datos:

```sql
-- Ejecutar en MySQL
USE tahona;

-- Insertar o actualizar configuración de empresa
INSERT INTO empresa_config (
    nombre_empresa,
    cif,
    direccion,
    codigo_postal,
    poblacion,
    provincia,
    telefono,
    email,
    
    -- Datos Verifactu específicos
    verifactu_habilitado,
    verifactu_nif_emisor,
    verifactu_nombre_software,
    verifactu_version_software,
    verifactu_num_instalacion,
    
    activo
) VALUES (
    'GRUPO BABO, S.Coop.V.L.',           -- nombre_empresa
    'F54059985',                          -- cif
    'C/ EJEMPLO, 123',                    -- direccion (CAMBIAR)
    '03001',                              -- codigo_postal (CAMBIAR)
    'Alicante',                           -- poblacion (CAMBIAR)
    'Alicante',                           -- provincia (CAMBIAR)
    '965123456',                          -- telefono (CAMBIAR)
    'info@grupobabo.es',                  -- email (CAMBIAR)
    
    -- Verifactu
    TRUE,                                 -- verifactu_habilitado
    'F54059985',                          -- verifactu_nif_emisor (mismo CIF)
    'ERP Panadería Tahona',               -- verifactu_nombre_software
    '1.0.0',                              -- verifactu_version_software
    '001',                                -- verifactu_num_instalacion (único por instalación)
    
    TRUE                                  -- activo
)
ON DUPLICATE KEY UPDATE
    nombre_empresa = VALUES(nombre_empresa),
    cif = VALUES(cif),
    verifactu_habilitado = VALUES(verifactu_habilitado),
    verifactu_nif_emisor = VALUES(verifactu_nif_emisor);
```

**⚠️ IMPORTANTE:** Actualiza los datos de dirección, teléfono y email con los reales de GRUPO BABO.

---

### PASO 2: Obtener Certificado Digital de la AEAT (CRÍTICO)

Para enviar facturas a la AEAT necesitas un **certificado digital**.

#### Opción A: Certificado de la FNMT (Recomendado)

1. **Ir a la web de la FNMT:**
   - https://www.sede.fnmt.gob.es/certificados

2. **Tipos de certificado disponibles:**
   - **Certificado de Persona Jurídica:** Para empresas (GRUPO BABO)
   - **Certificado de Representante:** Para el administrador

3. **Proceso de solicitud:**
   ```
   1. Solicitud online (video identificación)
   2. Esperar aprobación (1-2 días laborables)
   3. Descargar certificado
   4. Instalar en el navegador
   5. Exportar a formato PKCS12 (.p12)
   ```

4. **Exportar el certificado:**
   - Desde el navegador: Configuración → Certificados → Exportar
   - Guardar como: `grupo_babo_aeat.p12`
   - Establecer contraseña: (anótala bien)

#### Opción B: Certificado de Pruebas (Para desarrollo)

Si estás en fase de desarrollo, puedes usar el entorno de preproducción:

1. **Solicitar acceso al entorno de pruebas:**
   - https://www.agenciatributaria.es/AEAT.internet/Inicio/La_Agencia_Tributaria/Campanas/Verifactu/

2. **Usar certificado de pruebas:**
   - La AEAT proporciona certificados de prueba
   - Solo válidos en entorno de preproducción

---

### PASO 3: Configurar el Certificado en la Aplicación

Una vez tengas el archivo `.p12`:

#### 3.1. Colocar el certificado

```bash
# Copiar el certificado a la carpeta del proyecto
copy grupo_babo_aeat.p12 D:\Programación\ERP\src\main\resources\certs\

# O renombrar el existente
rename grupo_babo_aeat.p12 mi_certificado.p12
```

#### 3.2. Configurar application.properties

Editar: `src/main/resources/application.properties`

```properties
# ============================================
# VERIFACTU / AEAT CONFIGURATION
# ============================================

# Habilitar Verifactu
verifactu.aeat.enabled=true

# Ruta al certificado (.p12)
verifactu.keystore.path=/certs/mi_certificado.p12

# Contraseña del certificado
verifactu.keystore.password=TU_PASSWORD_DEL_CERTIFICADO

# Alias de la clave (normalmente el CIF o nombre)
verifactu.key.alias=f54059985

# Contraseña de la clave privada (si es diferente)
verifactu.key.password=${verifactu.keystore.password}

# ============================================
# ENTORNO AEAT
# ============================================

# PREPRODUCCIÓN (para pruebas)
verifactu.aeat.endpoint=https://prewww2.aeat.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu

# PRODUCCIÓN (cuando estés listo)
#verifactu.aeat.endpoint=https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu

# Timeout de conexión (milisegundos)
verifactu.aeat.timeout=30000

# Reintentos en caso de error
verifactu.aeat.max-retries=3
```

**⚠️ IMPORTANTE:** 
- Usar **PREPRODUCCIÓN** primero para pruebas
- Cambiar a **PRODUCCIÓN** solo cuando todo funcione bien

---

### PASO 4: Verificar la Configuración

Crea un script SQL para verificar:

```sql
-- Verificar configuración de empresa
SELECT 
    nombre_empresa,
    cif,
    verifactu_habilitado,
    verifactu_nif_emisor,
    CASE 
        WHEN verifactu_habilitado = TRUE THEN '✅ Habilitado'
        ELSE '❌ Deshabilitado'
    END AS estado_verifactu
FROM empresa_config
WHERE activo = TRUE;

-- Verificar que hay al menos 1 cliente
SELECT COUNT(*) as total_clientes FROM clientes WHERE activo = TRUE;

-- Verificar que hay artículos
SELECT COUNT(*) as total_articulos FROM articulos WHERE activo = TRUE;
```

---

### PASO 5: Probar el Flujo Completo

#### 5.1. Arrancar la aplicación

```bash
cd D:\Programación\ERP
.\scripts\iniciar.bat
```

#### 5.2. Verificar logs al inicio

Busca en los logs:
```
✅ BUENO:
[INFO] Verifactu habilitado – certificado cargado correctamente
[INFO] Empresa configurada: GRUPO BABO, S.Coop.V.L.
[INFO] NIF Emisor: F54059985

❌ MALO:
[WARN] Keystore no encontrado
[WARN] Verifactu deshabilitado
[ERROR] Error cargando certificado
```

#### 5.3. Crear una factura de prueba

1. Login: admin / admin
2. Ir a módulo "Facturas"
3. Click "Nueva Factura"
4. Datos:
   - Cliente: Seleccionar uno existente
   - Fecha: Hoy
   - Líneas: Añadir al menos 1 artículo
5. Guardar → Queda en **BORRADOR**

#### 5.4. Enviar a revisión

1. Seleccionar la factura
2. Click "Enviar a Revisión"
3. Confirmar
4. Estado cambia a **REVISION**

#### 5.5. Aprobar y emitir (ENVÍO A AEAT)

1. Seleccionar la factura (estado REVISION)
2. Click "Aprobar y Emitir"
3. Leer advertencia
4. Click "YES"

**Qué debería pasar:**

```
✅ ÉXITO:
- Genera XML con datos de GRUPO BABO y cliente
- Calcula hash SHA-256
- Firma digitalmente con certificado
- Genera código QR
- Envía a AEAT (si está habilitado)
- Guarda evidencia en BD
- Estado cambia a EMITIDA
- Muestra mensaje de éxito

❌ ERROR:
- Muestra mensaje de error específico
- Factura permanece en REVISION
- Logs muestran detalles del error
```

---

### PASO 6: Verificar el Envío a la AEAT

#### 6.1. Ver logs detallados

En la consola busca:
```
[INFO] 🚀 Iniciando emisión de factura FAC001 a Verifactu/AEAT
[INFO] XML generado para factura FAC001: 1234 caracteres
[INFO] Factura FAC001 firmada digitalmente
[INFO] QR generado para factura FAC001
[INFO] ✅ Factura FAC001 emitida correctamente a Verifactu/AEAT
```

#### 6.2. Verificar en base de datos

```sql
-- Ver factura emitida
SELECT 
    numero,
    fecha,
    total,
    estado,
    verifactu_enviada,
    fecha_emision_verifactu,
    verifactu_hash
FROM facturas
WHERE numero = 'FAC001';

-- Ver evidencia registrada
SELECT 
    serie,
    numero,
    fecha_emision,
    hash,
    xml_enviado,
    respuesta_aeat
FROM verifactu_evidence
WHERE numero = 'FAC001';
```

#### 6.3. Verificar en la web de la AEAT

1. Ir a: https://sede.agenciatributaria.gob.es/
2. Login con certificado
3. Buscar "Verifactu" o "Sistema de verificación de facturas"
4. Consultar facturas emitidas
5. Buscar por número o fecha

---

### PASO 7: Imprimir Factura con QR

1. Seleccionar factura EMITIDA
2. Click "Imprimir"
3. Elegir diseño
4. Se genera PDF/HTML con:
   - Datos de GRUPO BABO (emisor)
   - Datos del cliente (receptor)
   - Líneas de la factura
   - Totales con IVA
   - **Código QR de Verifactu**
   - Hash de la factura
   - Leyenda legal

---

## 🔍 SOLUCIÓN DE PROBLEMAS COMUNES

### Problema 1: "Keystore no encontrado"

**Causa:** El certificado no está en la ruta correcta

**Solución:**
```bash
# Verificar que existe el archivo
dir D:\Programación\ERP\src\main\resources\certs\mi_certificado.p12

# Si no existe, copiarlo
copy "C:\ruta\al\certificado.p12" "D:\Programación\ERP\src\main\resources\certs\mi_certificado.p12"
```

### Problema 2: "Error cargando certificado"

**Causa:** Contraseña incorrecta o certificado corrupto

**Solución:**
1. Verificar contraseña en `application.properties`
2. Probar abrir el certificado con:
   ```bash
   keytool -list -v -keystore mi_certificado.p12 -storetype PKCS12
   ```

### Problema 3: "Configure los datos de empresa"

**Causa:** No hay configuración en `empresa_config`

**Solución:**
Ejecutar el SQL del PASO 1

### Problema 4: "Verifactu deshabilitado en configuración"

**Causa:** Campo `verifactu_habilitado` está en FALSE

**Solución:**
```sql
UPDATE empresa_config 
SET verifactu_habilitado = TRUE 
WHERE activo = TRUE;
```

### Problema 5: "Error de conexión con AEAT"

**Causas posibles:**
- Sin conexión a internet
- Endpoint incorrecto
- Servicio AEAT caído
- Certificado no válido para ese entorno

**Solución:**
1. Verificar conexión a internet
2. Verificar endpoint en `application.properties`
3. Si usas PRODUCCIÓN, asegúrate de tener certificado real
4. Si usas PREPRODUCCIÓN, usa certificado de pruebas

### Problema 6: "Solo se pueden emitir facturas en REVISION"

**Causa:** Intentas emitir desde estado BORRADOR

**Solución:**
Seguir el flujo: BORRADOR → "Enviar a Revisión" → REVISION → "Aprobar y Emitir"

---

## 📊 CHECKLIST DE CONFIGURACIÓN

Antes de emitir facturas reales a la AEAT:

- [ ] Certificado digital obtenido y descargado
- [ ] Certificado copiado a `/certs/` del proyecto
- [ ] `application.properties` configurado con ruta y contraseña
- [ ] Datos de GRUPO BABO en tabla `empresa_config`
- [ ] `verifactu_habilitado = TRUE`
- [ ] NIF emisor correcto (F54059985)
- [ ] Endpoint configurado (preproducción primero)
- [ ] Aplicación arranca sin warnings de Verifactu
- [ ] Prueba de factura en PREPRODUCCIÓN exitosa
- [ ] Verificación en web de AEAT correcta
- [ ] QR generado y legible
- [ ] Todo funcional → Cambiar a PRODUCCIÓN

---

## 🎯 DATOS ESPECÍFICOS DE GRUPO BABO

**Para el SQL de configuración de empresa:**

```sql
-- Actualiza estos valores con los datos reales
nombre_empresa = 'GRUPO BABO, S.Coop.V.L.'
cif = 'F54059985'
direccion = '[DIRECCIÓN REAL]'  -- ACTUALIZAR
codigo_postal = '[CP REAL]'      -- ACTUALIZAR
poblacion = '[POBLACIÓN]'        -- ACTUALIZAR
provincia = '[PROVINCIA]'        -- ACTUALIZAR
telefono = '[TELÉFONO]'          -- ACTUALIZAR
email = '[EMAIL CONTACTO]'       -- ACTUALIZAR
```

**Para el certificado:**
- Debe estar a nombre de: GRUPO BABO, S.Coop.V.L.
- O del representante legal autorizado
- NIF: F54059985

---

## 🚀 RESUMEN RÁPIDO

### Para empezar YA (modo desarrollo):

1. **Ejecutar SQL** del PASO 1 (datos empresa)
2. **Configurar** `application.properties`:
   ```properties
   verifactu.aeat.enabled=false  # Primero deshabilitado
   ```
3. **Probar** el flujo sin envío real
4. **Verificar** que todo funciona

### Para producción (envío real AEAT):

1. **Obtener certificado** de la FNMT
2. **Colocar** en `/certs/`
3. **Configurar** contraseña en `application.properties`
4. **Habilitar:**
   ```properties
   verifactu.aeat.enabled=true
   verifactu.aeat.endpoint=https://prewww2.aeat.es/...  # PREPRODUCCIÓN
   ```
5. **Probar** en preproducción
6. **Verificar** en web AEAT
7. **Cambiar** a producción cuando funcione

---

## 📞 SIGUIENTE PASO INMEDIATO

**AHORA MISMO puedes hacer:**

```bash
# 1. Ejecutar SQL de configuración de empresa
mysql -u root -p tahona < configurar_empresa.sql

# 2. Arrancar la aplicación
cd D:\Programación\ERP
.\scripts\iniciar.bat

# 3. Probar el flujo (sin envío real aún)
# - Crear factura
# - Enviar a revisión
# - Aprobar (generará hash y QR local)
```

**Con certificado real podrás:**
- Enviar a AEAT de verdad
- Cumplir con Verifactu legalmente
- Emitir facturas válidas fiscalmente

---

**Fecha:** 28/12/2025  
**Estado:** ✅ Guía completa lista  
**Siguiente paso:** Configurar datos de empresa y obtener certificado

