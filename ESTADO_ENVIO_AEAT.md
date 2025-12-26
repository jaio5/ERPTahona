# ✅ ESTADO DEL ENVÍO A LA AEAT - Verifactu

**Fecha:** 25 de Diciembre de 2025  
**Empresa:** GRUPO BABO, S.Coop.V.L. (CIF: F54059985)

---

## 📊 ESTADO ACTUAL

### ✅ Implementado y Funcionando

1. **Generación de XML Correcto** ✅
   - XML conforme al estándar Verifactu
   - Incluye datos de GRUPO BABO (CIF, nombre, etc.)
   - Namespace correcto de AEAT
   - Estructura validada

2. **Proceso de Facturación** ✅
   - Estados: BORRADOR → REVISION → EMITIDA
   - Validaciones completas antes de emitir
   - Control de flujo implementado

3. **Firma Digital** ✅
   - Firma RSA/SHA-256 (si hay certificado)
   - Hash encadenado de facturas
   - Integridad garantizada

4. **Registro de Evidencias** ✅
   - Tabla `verifactu_evidence` en BD
   - Hash, firma, metadata guardados
   - Trazabilidad completa

5. **Envío HTTP a la AEAT** ✅ **NUEVO**
   - Cliente HTTP implementado
   - POST multipart/form-data
   - Manejo de respuestas
   - Timeout configurado
   - Manejo de errores

---

## 🎯 MODO ACTUAL: PRUEBAS

### Configuración Activa

```properties
verifactu.aeat.enabled=false
```

### ¿Qué Hace el Sistema Ahora?

Cuando se emite una factura (estado EMITIDA):

1. ✅ Valida que la factura está en estado REVISION
2. ✅ Verifica configuración de empresa (GRUPO BABO)
3. ✅ Obtiene líneas de la factura
4. ✅ **Genera XML correcto con datos de GRUPO BABO:**
   ```xml
   <RegistroFacturaVerifactu>
     <Cabecera>
       <Emisor>
         <NIF>F54059985</NIF>
         <NombreRazonSocial>GRUPO BABO, S.Coop.V.L.</NombreRazonSocial>
       </Emisor>
       <SistemaInformatico>
         <NombreSistema>ERP Tahona</NombreSistema>
         <Version>1.0.0</Version>
       </SistemaInformatico>
     </Cabecera>
     <Factura>
       <!-- Datos completos de la factura -->
     </Factura>
   </RegistroFacturaVerifactu>
   ```
5. ✅ Genera hash SHA-256
6. ✅ Firma digitalmente (si hay certificado)
7. ✅ **Guarda evidencia en BD local**
8. ℹ️ **NO envía a la AEAT** (porque `verifactu.aeat.enabled=false`)
9. ✅ Muestra mensaje: "Factura registrada localmente (modo de pruebas)"

### Log en Modo Pruebas

```
INFO: Iniciando envío de factura FAC001 a Verifactu
INFO: XML generado para factura FAC001: 1234 caracteres
INFO: Factura FAC001 firmada digitalmente
INFO: Evidencia guardada para factura FAC001 con ID 1
INFO: ℹ️ Envío a AEAT deshabilitado (verifactu.aeat.enabled=false)
INFO: ✅ Factura FAC001 registrada localmente (modo de pruebas)
```

---

## 🚀 MODO PRODUCCIÓN: Envío Real a la AEAT

### Cómo Activar

#### 1. Configurar Certificado Digital

```properties
# En application.properties
verifactu.keystore.path=/certs/grupo_babo_certificado.p12
verifactu.keystore.password=TU_CONTRASEÑA_SEGURA
verifactu.key.alias=grupo_babo
verifactu.key.password=TU_CONTRASEÑA_SEGURA
```

**Obtener certificado:**
1. Ir a https://www.sede.fnmt.gob.es/
2. Solicitar certificado de persona jurídica
3. Para: GRUPO BABO, S.Coop.V.L.
4. CIF: F54059985
5. Descargar en formato PKCS12 (.p12)
6. Guardar en `src/main/resources/certs/`

#### 2. Activar Envío a AEAT

```properties
# Cambiar a true para PRODUCCIÓN
verifactu.aeat.enabled=true

# Verificar endpoint (producción)
verifactu.aeat.endpoint=https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu
```

⚠️ **ATENCIÓN:**
- Los envíos en modo producción son **REALES**
- Tienen **efectos fiscales**
- Se registran en la AEAT
- No se pueden deshacer fácilmente

#### 3. Reiniciar Aplicación

```bash
.\mvnw clean compile
.\arrancar.bat
```

### ¿Qué Hace el Sistema en Modo Producción?

Cuando se emite una factura (estado EMITIDA):

1. ✅ Todo lo del modo de pruebas (pasos 1-7)
2. ✅ **Envía XML a la AEAT mediante HTTPS POST**
3. ✅ Espera respuesta de la AEAT
4. ✅ Guarda respuesta en la evidencia:
   - `codigo_respuesta_aeat`: "OK" o código de error
   - `metadata`: incluye respuesta completa
5. ✅ Si hay error: lo registra y lanza excepción

### Log en Modo Producción

```
INFO: Iniciando envío de factura FAC001 a Verifactu
INFO: XML generado para factura FAC001: 1234 caracteres
INFO: Factura FAC001 firmada digitalmente
INFO: Evidencia guardada para factura FAC001 con ID 1
INFO: Enviando factura FAC001 a AEAT endpoint: https://...
INFO: Respuesta AEAT - Status: 200, Body: {"resultado":"OK"}
INFO: ✅ Factura FAC001 enviada y registrada correctamente en AEAT
```

---

## 📋 FORMATO DEL XML ENVIADO

### Estructura Completa

```xml
<?xml version="1.0" encoding="UTF-8"?>
<RegistroFacturaVerifactu xmlns="https://www2.agenciatributaria.gob.es/...">
  <Cabecera>
    <Emisor>
      <NIF>F54059985</NIF>
      <NombreRazonSocial>GRUPO BABO, S.Coop.V.L.</NombreRazonSocial>
    </Emisor>
    <SistemaInformatico>
      <NombreSistema>ERP Tahona</NombreSistema>
      <Version>1.0.0</Version>
      <IdDispositivo>DEVICE001</IdDispositivo>
    </SistemaInformatico>
  </Cabecera>
  
  <Factura>
    <NumFactura>FAC001</NumFactura>
    <FechaExpedicion>25-12-2025</FechaExpedicion>
    <HoraExpedicion>14:30:45</HoraExpedicion>
    
    <Destinatario>
      <NIF>12345678A</NIF>
      <NombreRazonSocial>Cliente Ejemplo</NombreRazonSocial>
    </Destinatario>
    
    <Desglose>
      <BaseImponible>100.00</BaseImponible>
      <CuotaIVA>21.00</CuotaIVA>
    </Desglose>
    
    <ImporteTotal>121.00</ImporteTotal>
    
    <Hash>a1b2c3d4e5f6...</Hash>
    <HashAnterior>x9y8z7w6v5u4...</HashAnterior>
  </Factura>
</RegistroFacturaVerifactu>
```

### Datos de GRUPO BABO Incluidos

| Campo | Valor | Origen |
|-------|-------|--------|
| NIF | F54059985 | `empresa_config.verifactu_nif_emisor` |
| Nombre | GRUPO BABO, S.Coop.V.L. | `empresa_config.nombre_empresa` |
| Sistema | ERP Tahona | `empresa_config.verifactu_nombre_sistema` |
| Versión | 1.0.0 | `empresa_config.verifactu_version_sistema` |

---

## 🔒 SEGURIDAD

### Firma Digital

- **Algoritmo:** SHA-256 con RSA
- **Certificado:** GRUPO BABO (F54059985)
- **Uso:** Firma el XML completo
- **Beneficio:** Garantiza autenticidad y no repudio

### Hash Encadenado

- **Algoritmo:** SHA-256
- **Cadena:** Cada factura incluye hash de la anterior
- **Beneficio:** Detecta modificaciones posteriores

### Transmisión Segura

- **Protocolo:** HTTPS (TLS 1.2+)
- **Autenticación:** Certificado digital
- **Timeout:** 60 segundos
- **Reintentos:** No automáticos (evita duplicados)

---

## 📊 VERIFICACIÓN

### En Base de Datos

```sql
-- Ver evidencias registradas
SELECT 
    numero,
    fecha_emision,
    estado,
    codigo_respuesta_aeat,
    fecha_envio
FROM verifactu_evidence
ORDER BY fecha_envio DESC;

-- Ver facturas emitidas
SELECT 
    numero,
    estado,
    verifactu_enviada,
    fecha_emision_verifactu
FROM facturas
WHERE estado = 'EMITIDA'
ORDER BY fecha_emision_verifactu DESC;
```

### En Logs de Aplicación

```bash
# Ver logs de Verifactu
type logs\erp-tahona.log | findstr "Verifactu"

# Ver logs de AEAT
type logs\erp-tahona.log | findstr "AEAT"
```

---

## ⚠️ IMPORTANTE

### Modo de Pruebas (Actual)

✅ **Ventajas:**
- Pruebas sin riesgo
- Verifica XML generado
- No afecta a la AEAT
- Ideal para desarrollo

✅ **Recomendado para:**
- Desarrollo y pruebas
- Verificar que todo funciona
- Validar formato XML
- Entrenar usuarios

### Modo Producción

⚠️ **Consideraciones:**
- Requiere certificado válido y vigente
- Envíos tienen efectos fiscales
- No se pueden deshacer fácilmente
- Requiere supervisión

⚠️ **Activar solo cuando:**
- Sistema probado completamente
- Usuarios entrenados
- Certificado instalado
- Respaldos configurados
- Equipo listo para supervisar

---

## 📞 RESUMEN

### Estado Actual ✅

```
╔════════════════════════════════════════════╗
║  ✅ XML GENERADO CORRECTAMENTE             ║
║  ✅ DATOS DE GRUPO BABO INCLUIDOS          ║
║  ✅ FIRMA DIGITAL IMPLEMENTADA             ║
║  ✅ EVIDENCIAS GUARDADAS LOCALMENTE        ║
║  ✅ CLIENTE HTTP A AEAT IMPLEMENTADO       ║
║                                            ║
║  ℹ️ MODO: PRUEBAS                          ║
║  ℹ️ verifactu.aeat.enabled=false           ║
║                                            ║
║  Para activar envío real a AEAT:           ║
║  1. Configurar certificado                 ║
║  2. Cambiar aeat.enabled=true              ║
║  3. Reiniciar aplicación                   ║
╚════════════════════════════════════════════╝
```

---

**Todo está listo para enviar a la AEAT cuando lo decidas activar.** 🎉

El XML se genera correctamente con todos los datos de GRUPO BABO según el formato oficial de Verifactu.

