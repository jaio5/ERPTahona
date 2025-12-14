# Sistema VeriFactu - Documentación Completa

## Índice
1. [Introducción](#introducción)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Componentes Principales](#componentes-principales)
4. [Funcionamiento](#funcionamiento)
5. [Configuración](#configuración)
6. [Uso](#uso)
7. [Cadena de Bloques](#cadena-de-bloques)
8. [Troubleshooting](#troubleshooting)

---

## Introducción

VeriFactu es un sistema de verificación y registro de facturas que cumple con las normativas de la Agencia Tributaria Española (AEAT). El sistema:

- **Genera evidencias criptográficas** de cada factura emitida
- **Firma digitalmente** las evidencias usando certificados digitales
- **Encadena las evidencias** usando tecnología blockchain
- **Envía las evidencias** a la AEAT (opcional)
- **Valida la integridad** de la cadena de evidencias

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                     INTERFAZ DE USUARIO                      │
│                   VerifactuController.java                   │
│                   verifactu_panel.fxml                       │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────────┐
│                   CAPA DE SERVICIOS                          │
├──────────────────────────────────────────────────────────────┤
│  VerifactuEvidenceService  │  Gestión de evidencias          │
│  VerifactuService          │  Firma y validación             │
│  VerifactuAEATService      │  Comunicación con AEAT          │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────────┐
│                   CAPA DE DATOS                              │
│  VerifactuEvidenceRepository  │  Base de datos MySQL         │
│  VerifactuEvidence (entidad)   │  Tabla: verifactu_evidence │
└─────────────────────────────────────────────────────────────┘
```

## Componentes Principales

### 1. Entidad: VerifactuEvidence

Almacena las evidencias de cada factura:

```java
- id: Identificador único
- facturaId: Identificador de la factura
- serie: Serie de la factura
- numero: Número de factura
- hash: Hash SHA-256 de la factura
- hashAnterior: Hash de la evidencia anterior (blockchain)
- signature: Firma digital RSA
- certFingerprint: Huella del certificado
- estado: PENDIENTE | ENVIADO | ERROR | VERIFICADO
- fechaEmision: Fecha de emisión
- fechaEnvio: Fecha de envío a AEAT
- errorMessage: Mensaje de error (si aplica)
- metadata: Datos adicionales en formato JSON
```

### 2. VerifactuService

Servicios criptográficos principales:

**Métodos:**
- `generarHash(datos)`: Genera hash SHA-256
- `generarHashEncadenado(datos, hashAnterior)`: Hash con blockchain
- `firmarDatos(datos)`: Firma digital RSA
- `verificarFirma(datos, firma)`: Verifica firma
- `getCertificateFingerprint()`: Obtiene huella del certificado
- `obtenerHashAnterior(serie)`: Obtiene último hash de la cadena
- `validarCadenaIntegridad(serie)`: Valida blockchain

### 3. VerifactuAEATService

Comunicación con la AEAT:

**Métodos:**
- `enviarAEAT(jsonEvidencia)`: Envía evidencia a AEAT
- `reenviarAEAT(jsonEvidencia)`: Reenvía evidencia fallida
- `verificarEstadoAEAT(hash)`: Verifica estado en AEAT

**Modos de operación:**
- **Modo Prueba** (`aeat.enabled=false`): Simula envíos
- **Modo Producción** (`aeat.enabled=true`): Envíos reales

### 4. VerifactuEvidenceService

Gestión completa de evidencias:

**Métodos principales:**
- `registrarEvidenciaAEAT()`: Crea y envía evidencia
- `reenviarEvidencia()`: Reenvía evidencia con error
- `verificarEstadoAEAT()`: Verifica estado
- `validarCadenaIntegridad()`: Valida blockchain

### 5. VerifactuController

Interfaz de usuario JavaFX:

**Funcionalidades:**
- Ver todas las evidencias registradas
- Ver detalles de cada evidencia
- Reenviar evidencias con error
- Verificar estado en AEAT
- Validar integridad de la cadena
- Exportar evidencias a CSV
- Estadísticas en tiempo real

## Funcionamiento

### Flujo de Registro de una Factura

```
1. Usuario crea factura
   ↓
2. FacturaService.save()
   ↓
3. verifactuEvidenceService.registrarEvidenciaAEAT()
   ↓
4. Se genera evidencia:
   - Obtiene hash anterior (blockchain)
   - Genera hash encadenado
   - Firma digitalmente los datos
   - Obtiene fingerprint del certificado
   ↓
5. Se envía a AEAT:
   - Construye JSON de evidencia
   - Envía vía REST API
   - Procesa respuesta
   ↓
6. Se guarda en BD:
   - Estado: ENVIADO o ERROR
   - Metadata con respuesta
   - Hash encadenado
```

### Cadena de Bloques (Blockchain)

Cada evidencia contiene:
- **Hash**: SHA-256 de (datos_factura + hash_anterior)
- **Hash Anterior**: Hash de la evidencia previa de la misma serie

Esto crea una cadena inmutable:

```
Evidencia 1: hash1 = SHA256(factura1)
Evidencia 2: hash2 = SHA256(factura2 + hash1)
Evidencia 3: hash3 = SHA256(factura3 + hash2)
...
```

Si se modifica cualquier evidencia, toda la cadena posterior queda invalidada.

## Configuración

### application.properties

```properties
# Habilitar envío real a AEAT
verifactu.aeat.enabled=false

# Configuración del keystore
verifactu.keystore.path=/keystore/certificate.p12
verifactu.keystore.password=YourPassword
verifactu.key.alias=your_alias
verifactu.key.password=YourKeyPassword

# Endpoint de AEAT
verifactu.aeat.endpoint=https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu

# Certificado para conexión AEAT
verifactu.cert.path=/keystore/certificate.p12
verifactu.cert.password=YourPassword
```

### Certificado Digital

Para producción necesitas:
1. Certificado digital de la FNMT o autoridad reconocida
2. Formato PKCS12 (.p12 o .pfx)
3. Guardar en `src/main/resources/keystore/` o ruta externa

**Generar certificado de prueba:**
```bash
keytool -genkeypair -alias verifactu -keyalg RSA -keysize 2048 \
  -validity 365 -keystore certificate.p12 -storetype PKCS12
```

## Uso

### Desde la Interfaz

1. **Ver Evidencias:**
   - Abrir panel "VeriFactu"
   - Ver tabla con todas las evidencias
   - Estadísticas en tarjetas superiores

2. **Ver Detalles:**
   - Clic en evidencia
   - Panel derecho muestra detalles completos
   - Hash, firma, metadata, etc.

3. **Reenviar Evidencia:**
   - Seleccionar evidencia con estado ERROR
   - Clic en "📤 Reenviar"
   - Confirmar

4. **Verificar Estado:**
   - Seleccionar evidencia ENVIADA
   - Clic en "✓ Verificar"
   - Consulta estado en AEAT

5. **Validar Cadena:**
   - Clic en "🔗 Validar Cadena"
   - Verifica integridad de todas las series
   - Muestra resultado

6. **Exportar:**
   - Clic en "💾 Exportar"
   - Elige ubicación
   - Genera CSV con todas las evidencias

### Desde el Código

```java
@Autowired
private VerifactuEvidenceService verifactuService;

// Registrar evidencia al crear factura
String datosFactura = "FACTURA-2024-001|Cliente|Total|Fecha";
verifactuService.registrarEvidenciaAEAT(
    datosFactura,
    "2024",
    "001"
);

// Reenviar evidencia con error
verifactuService.reenviarEvidencia(evidenciaId);

// Validar cadena de una serie
boolean valida = verifactuService.validarCadenaIntegridad("2024");
```

## Estados de las Evidencias

| Estado | Descripción | Color |
|--------|-------------|-------|
| **PENDIENTE** | Evidencia creada, no enviada | 🟠 Naranja |
| **ENVIADO** | Enviada y aceptada por AEAT | 🟢 Verde |
| **ERROR** | Error al enviar a AEAT | 🔴 Rojo |
| **VERIFICADO** | Verificada manualmente en AEAT | 🟢 Verde |

## Seguridad

### Firma Digital

- **Algoritmo**: RSA con SHA-256
- **Longitud de clave**: 2048 bits mínimo
- **Proveedor**: BouncyCastle

### Hash

- **Algoritmo**: SHA-256
- **Codificación**: Base64
- **Longitud**: 256 bits (32 bytes)

### Certificado

- **Formato**: X.509 en PKCS12
- **Validez**: Verificada al iniciar
- **Huella**: SHA-256 del certificado

## Troubleshooting

### Sistema Deshabilitado

**Síntoma:** Mensaje "⚠ Sistema deshabilitado"

**Causas:**
- Keystore no encontrado
- Password incorrecto
- Alias no existe
- Certificado inválido

**Solución:**
1. Verificar ruta del keystore
2. Verificar password
3. Listar aliases: `keytool -list -keystore certificate.p12`

### Error al Enviar a AEAT

**Síntoma:** Estado ERROR, mensaje de error guardado

**Causas:**
- `aeat.enabled=false` (modo prueba)
- Endpoint incorrecto
- Certificado no autorizado
- Formato JSON inválido

**Solución:**
1. Verificar configuración `aeat.enabled`
2. Verificar endpoint
3. Ver detalles del error en metadata
4. Reenviar evidencia

### Cadena Rota

**Síntoma:** Validación falla para una serie

**Causas:**
- Evidencia modificada manualmente
- Hash anterior incorrecto
- Orden temporal alterado

**Solución:**
1. Identificar evidencia rota
2. Verificar integridad de BD
3. Regenerar evidencias desde backup
4. Contactar soporte AEAT si es producción

### Performance Lento

**Síntoma:** Carga lenta de evidencias

**Solución:**
1. Verificar índices en BD
2. Ejecutar script de índices
3. Optimizar consultas
4. Paginar resultados

## Scripts SQL Útiles

### Ver Estado del Sistema

```sql
SELECT 
    estado,
    COUNT(*) as total,
    MIN(fecha_emision) as primera,
    MAX(fecha_emision) as ultima
FROM verifactu_evidence
GROUP BY estado;
```

### Evidencias con Error

```sql
SELECT 
    id,
    serie,
    numero,
    error_message,
    fecha_emision
FROM verifactu_evidence
WHERE estado = 'ERROR'
ORDER BY fecha_emision DESC;
```

### Validar Cadena SQL

```sql
SELECT 
    e1.id,
    e1.hash,
    e1.hash_anterior,
    e2.hash as hash_anterior_real,
    CASE 
        WHEN e1.hash_anterior = e2.hash THEN 'OK'
        ELSE 'ROTA'
    END as validacion
FROM verifactu_evidence e1
LEFT JOIN verifactu_evidence e2 ON e2.id = (
    SELECT id FROM verifactu_evidence e3
    WHERE e3.serie = e1.serie 
    AND e3.fecha_emision < e1.fecha_emision
    ORDER BY e3.fecha_emision DESC
    LIMIT 1
)
WHERE e1.serie = '2024'
ORDER BY e1.fecha_emision;
```

## Mantenimiento

### Backup

```bash
# Backup de evidencias
mysqldump tahona verifactu_evidence > verifactu_backup.sql

# Backup de certificados
cp -r src/main/resources/keystore/ backup/keystore_$(date +%Y%m%d)
```

### Limpieza

```sql
-- Eliminar evidencias antiguas (más de 7 años)
DELETE FROM verifactu_evidence 
WHERE fecha_emision < DATE_SUB(NOW(), INTERVAL 7 YEAR)
AND estado = 'ENVIADO';
```

### Monitorización

```sql
-- Vista de estadísticas diarias
CREATE VIEW verifactu_stats AS
SELECT 
    DATE(fecha_emision) as fecha,
    COUNT(*) as total,
    SUM(CASE WHEN estado = 'ENVIADO' THEN 1 ELSE 0 END) as enviados,
    SUM(CASE WHEN estado = 'ERROR' THEN 1 ELSE 0 END) as errores,
    SUM(CASE WHEN estado = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes
FROM verifactu_evidence
GROUP BY DATE(fecha_emision)
ORDER BY fecha DESC;
```

## Soporte

Para más información:
- AEAT: https://www.agenciatributaria.es/
- Documentación VeriFactu: https://sede.agenciatributaria.gob.es/verifactu
- Issues del proyecto: [GitHub/Issues]

---

**Versión:** 1.0  
**Fecha:** 2024-12-13  
**Autor:** Sistema ERP Tahona

