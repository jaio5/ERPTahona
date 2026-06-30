# Arquitectura del módulo VeriFactu — ERPTahona
*Revisión: 2026-06-30*

---

## 1. Contexto regulatorio

**VeriFactu** (VERI*FACTU) es el sistema de verificación de facturas electrónicas de la AEAT (Agencia Estatal de Administración Tributaria) en España, regulado por el Real Decreto 1007/2023 y la Orden HAC/1177/2024.

Los requisitos clave son:
- Cada factura emitida debe tener un **registro de alta** con hash SHA-256 encadenado con el registro anterior de la misma serie.
- Las facturas anuladas deben tener un **registro de anulación** también encadenado.
- El XML de cada registro debe validarse contra el XSD oficial de la AEAT.
- Opcionalmente, los registros se envían a la AEAT via **SOAP** sobre HTTPS con certificado digital PKCS12.
- El sistema debe conservar evidencias auditables de todos los registros.

---

## 2. Componentes del módulo

```
┌─────────────────────────────────────────────────────────────────┐
│  UI / Web                                                        │
│  FiscalWebController  →  /web/verifactu (lista evidencias)      │
│                       →  /web/verifactu/{id} (detalle)          │
│                       →  POST /web/verifactu/{id}/reenviar      │
│                       →  POST /web/verifactu/{id}/verificar     │
├─────────────────────────────────────────────────────────────────┤
│  Servicios de dominio                                            │
│  VerifactuService         — núcleo: hash, firma, XML, AEAT      │
│  VerifactuEvidenceService — CRUD + estado de evidencias         │
│  VerifactuHardeningService — hardening de seguridad             │
│  VerifactuDiagnosticoService — diagnóstico de configuración     │
├─────────────────────────────────────────────────────────────────┤
│  Infraestructura                                                 │
│  VerifactuAeatSoapClient  — cliente SOAP para AEAT (opcional)   │
│  VerifactuProperties      — @ConfigurationProperties            │
│  QrCodeService            — genera QR para facturas             │
├─────────────────────────────────────────────────────────────────┤
│  Persistencia                                                    │
│  VerifactuEvidence        — entidad JPA                         │
│  VerifactuEvidenceRepository — Spring Data JPA                  │
│  Tabla: verifactu_evidence                                       │
├─────────────────────────────────────────────────────────────────┤
│  Recursos                                                        │
│  xsd/verifactu-registro-facturacion.xsd — esquema AEAT          │
│  Keystore PKCS12 — certificado digital (externo, no en repo)    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Flujo de emisión de factura (modo VeriFactu activo)

```
FacturaWebController.emitirFactura(id)
  │
  ├─ Verifica estado = REVISION
  ├─ Verifica VeriFactu vigente en empresa
  │
  └─→ VerifactuService.enviarFacturaVerifactu(factura)
        │
        ├─ 1. Obtiene líneas de factura (FacturaLineaService)
        ├─ 2. Genera XML de alta: generarRegistroAltaXml(factura, lineas)
        │      ├─ appendCabecera: NIF emisor, nombre empresa, sistema
        │      ├─ appendDestinatario: NIF + nombre cliente
        │      ├─ appendDesglose: base imponible + cuota IVA
        │      ├─ ImporteTotal, TipoFactura
        │      ├─ FechaHoraGeneracion (ISO-8601 con zona horaria)
        │      ├─ Hash: generarHuellaRegistroAlta(empresa, factura, ...)
        │      │        = SHA-256-HEX(NIF|Serie|Num|Fecha|Tipo|CuotaTotal|Total|HashAnterior|FechaHora)
        │      └─ validarXmlRegistroFacturacion() contra XSD
        │
        ├─ 3. generarHash(xml) → SHA-256 Base64 URL-safe sin padding
        ├─ 4. firmarDatos(xml.bytes) → SHA256withRSA con clave privada PKCS12
        ├─ 5. Genera QR (URL de verificación AEAT + parámetros)
        ├─ 6. Guarda evidencia con estado PENDIENTE
        │
        ├─ 7a. Si aeatEnabled=true && certificado cargado:
        │      ├─ enviarXMLaAEAT(xml, firma) → VerifactuAeatSoapClient.enviarFacturaAeat()
        │      ├─ Actualiza evidencia: estado=ENVIADO, fechaEnvio, codigoRespuestaAEAT
        │      └─ Factura.estado = EMITIDA, verifactuEnviada=true, fechaEmisionVerifactu=now
        │
        └─ 7b. Si aeatEnabled=false (dev) o sin certificado:
               ├─ Evidencia: estado=ENVIADO, fechaEnvio=now
               └─ Factura.estado = EMITIDA, verifactuEnviada=true (modo local)
```

---

## 4. Flujo de anulación (rectificativa)

```
FacturaWebController.crearRectificativa(id, motivo, tipo, fecha)
  │
  └─→ FacturaService.crearRectificativa(id, motivo, tipo, fecha)
        │
        ├─ Obtiene factura original (debe ser EMITIDA)
        ├─ Crea nueva Factura con número R-{serie}-{año}-{seq}
        ├─ Copia líneas con cantidad negativa (rectificativa total)
        ├─ Guarda rectificativa en estado BORRADOR
        ├─ Marca original: estado = ANULADA, facturaRectificadaId = original.id
        │
        └─→ VerifactuService.registrarAnulacionLocal(facturaOriginal, motivo)
              │
              ├─ Verifica empresa y verifactuNifEmisor configurado
              ├─ Si configurado: generarRegistroAnulacionXml(factura, motivo)
              │    ├─ appendCabecera
              │    ├─ <RegistroAnulacion>: NumFactura, SerieFactura, FechaExpedicion
              │    ├─ FechaHoraGeneracion, MotivoAnulacion (opcional)
              │    ├─ Hash: generarHuellaRegistroAnulacion(empresa, factura, hashAnterior, fechaHora)
              │    │        = SHA-256-HEX(NIF|Serie|Num|Fecha|HashAnterior|FechaHora)
              │    └─ validarXmlRegistroFacturacion() contra XSD
              │
              ├─ Hash del documento: SHA-256 del XML (o hash simplificado sin XML)
              ├─ Crea VerifactuEvidence con tipoRegistro=ANULACION, estado=ANULADO
              └─ Guarda evidencia (xmlGenerado almacenado como LONGTEXT)
```

---

## 5. Cadena de hash (encadenamiento)

La especificación VeriFactu exige que cada registro incluya el hash del registro anterior de la misma serie, formando una cadena inmutable:

```
Registro 1:  hash = SHA256(NIF|Serie|Num1|Fecha1|...|""|FechaHora1)
Registro 2:  hash = SHA256(NIF|Serie|Num2|Fecha2|...|hash1|FechaHora2)
Registro 3:  hash = SHA256(NIF|Serie|Num3|Fecha3|...|hash2|FechaHora3)
```

Implementación en `VerifactuService`:
- `obtenerHashAnterior(serie)` → consulta `verifactu_evidence` buscando el último registro de la serie ordenado por `fecha_generacion_registro DESC, id DESC`
- El campo `huella_registro` almacena el hash del registro actual para que el siguiente lo pueda referenciar
- `validarCadenaIntegridad(serie)` recorre toda la cadena verificando que `hashAnterior` de cada evidencia coincide con el `hash` del registro anterior

---

## 6. Entidad VerifactuEvidence

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | BIGINT PK | Auto-incremental |
| `factura_id` | VARCHAR(100) | ID de la factura (`Factura.id.toString()`) |
| `serie` | VARCHAR(255) | Serie de la factura (ej. "F-GE", "R-GE") |
| `numero` | VARCHAR(255) | Número completo (ej. "F-GEN-2026-0009") |
| `tipo_registro` | VARCHAR(20) | "ALTA" o "ANULACION" |
| `estado` | VARCHAR(50) | PENDIENTE, ENVIADO, VERIFICADO, ERROR, ANULADO |
| `hash` | VARCHAR(128) NOT NULL | Hash SHA-256 del registro (Base64 URL-safe) |
| `hash_anterior` | VARCHAR(128) | Hash del registro anterior de la serie |
| `huella_registro` | VARCHAR(128) | SHA-256-HEX del contenido (para encadenamiento) |
| `nif_emisor` | VARCHAR(20) | NIF del emisor VeriFactu |
| `xml_generado` | LONGTEXT | XML completo del registro de facturación |
| `signature` | BLOB | Firma RSA del XML (bytes) |
| `cert_fingerprint` | VARCHAR(128) | Fingerprint SHA-256 del certificado usado |
| `fecha_emision` | TIMESTAMP | Fecha de emisión de la factura |
| `fecha_generacion_registro` | TIMESTAMP | Momento de generación del registro |
| `fecha_envio` | TIMESTAMP | Momento de envío a AEAT |
| `fecha_expedicion_factura` | DATE | Fecha de expedición (del campo `Factura.fecha`) |
| `codigo_respuesta_aeat` | VARCHAR(100) | Código de respuesta AEAT ("OK", "ERROR") |
| `error_message` | TEXT | Mensaje de error si estado=ERROR |
| `metadata` | JSON | Datos adicionales (empresa, CIF, fechas) |

---

## 7. Configuración

### Properties clave

```properties
# Keystore PKCS12 con certificado digital
verifactu.keystore.path=/ruta/al/certificado.p12
verifactu.keystore.password=contraseña
verifactu.key.alias=alias_clave
verifactu.key.password=contraseña_clave

# AEAT endpoint
verifactu.aeat.enabled=true|false
verifactu.aeat.endpoint=https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu
```

### Campos en EmpresaConfig (requeridos para VeriFactu)

| Campo | Descripción |
|-------|-------------|
| `verifactuNifEmisor` | NIF del emisor (obligatorio para generar XML) |
| `verifactuNombreSistema` | Nombre del sistema informático |
| `verifactuVersionSistema` | Versión del sistema |
| `verifactuIdDispositivo` | ID del dispositivo (opcional) |
| `verifactuHabilitado` | true/false |
| `verifactuFechaInicioFuncionamiento` | Fecha de inicio de funcionamiento VeriFactu |

### Modo dev (aplicación-dev.properties)

```properties
verifactu.aeat.enabled=false    # Sin envío AEAT
verifactu.keystore.path=/certs/mi_certificado.p12  # No existe → VeriFactu deshabilitado
```

En modo dev:
- `VerifactuService.enabled = false` (keystore no cargado)
- `VerifactuService.isAeatAvailable() = false`
- Las facturas IGUALMENTE pasan a EMITIDA (modo local)
- `registrarAnulacionLocal` IGUALMENTE registra evidencias (siempre que `verifactuNifEmisor` esté configurado en empresa)

---

## 8. Inicialización del servicio

`VerifactuService implements InitializingBean`, método `afterPropertiesSet()`:

1. `initKeystore()`: intenta cargar el keystore PKCS12 desde classpath o sistema de ficheros
   - Si el keystore no existe o falla: `enabled = false`, app continúa
   - Si carga OK: `enabled = true`, self-test de firma/verificación
2. Validaciones defensivas: tamaño del keystore, stream vacío, certificado X509 válido
3. Auto-verificación: firma "verifactu-selftest" y verifica la firma

---

## 9. Bugs encontrados y corregidos (revisión 2026-06-30)

| Bug | Severidad | Estado |
|-----|-----------|--------|
| BUG-009: Campo `motivoRectificacion` vs `motivo` en form | Alta | ✅ Corregido |
| BUG-010: `registrarAnulacionLocal` llama XSD sin NIF configurado | Alta | ✅ Corregido |
| BUG-011: Columna `xml_generado` TINYTEXT en dev | Alta | ✅ Corregido |
| BUG-012: Total de factura rectificativa = 0,00 € | Media | ⚠️ Pendiente |

Ver detalle en `docs/frontend-issues.md` (BUG-008 a BUG-012).

---

## 10. Flujo de la pantalla VeriFactu (`/web/verifactu`)

- **Lista**: `FiscalWebController.verifactu()` → paginación + filtros (estado, texto libre)
  - Columnas: Factura ID, Serie/Número, Generación, Estado, Código AEAT, Acciones
  - En dev sin evidencias muestra tabla vacía (correcto)

- **Detalle**: `FiscalWebController.verifactuDetalle(id)` → vista completa de una evidencia
  - Muestra XML generado, hash, firma, metadata JSON

- **Reenviar**: `POST /web/verifactu/{id}/reenviar`
  - Si AEAT disponible: estado → ENVIADO + fechaEnvio
  - Si no: estado → PENDIENTE + error

- **Verificar**: `POST /web/verifactu/{id}/verificar`
  - Si AEAT disponible y estado=ENVIADO: estado → VERIFICADO
  - Autorización: `@PreAuthorize("@permisos.puede('verifactu', 'enviar')")`

---

## 11. Pendiente / mejoras futuras

- **BUG-012**: Recalcular totales en `FacturaService.crearRectificativa()` (total = 0 en rectificativas)
- **Toast BUG-009**: Los mensajes flash no se muestran en redirects (BUG global, ver `docs/frontend-issues.md`)
- **Reenvío automático**: No hay scheduler que reintente PENDIENTE/ERROR → requiere intervención manual
- **Verificación AEAT**: `verificarEstadoAEAT` solo actualiza estado local, no hace llamada SOAP real
- **Integridad de cadena**: `validarCadenaIntegridad()` existe pero no se llama automáticamente; solo disponible via código
