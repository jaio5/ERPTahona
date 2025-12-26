# 🇪🇸 REQUISITOS LEGALES Y NORMATIVOS PARA ERP EN ESPAÑA

**Fecha de análisis:** 26 de diciembre de 2025  
**Ámbito:** Legislación española aplicable a software de gestión empresarial

---

## 🚨 CUMPLIMIENTO LEGAL OBLIGATORIO EN ESPAÑA

### 1. ⚖️ LEY ANTIFRAUDE Y VERIFACTU (Prioridad: CRÍTICA)
**Estado:** ⚠️ PARCIALMENTE IMPLEMENTADO

#### Normativa:
- **Ley 11/2021** - Prevención y lucha contra el fraude fiscal
- **Reglamento de facturación (RD 1619/2012)**
- **Sistema VeriFactu** - Obligatorio desde 2025

#### ✅ Ya implementado:
- Panel básico de Verifactu
- Generación de evidencias

#### ❌ FALTA IMPLEMENTAR (URGENTE):
- [ ] **Firma electrónica de facturas** - QR en cada factura
- [ ] **Cadena de bloques (blockchain)** - Hash encadenado de facturas
- [ ] **Huella digital de cada factura** - SHA-256
- [ ] **Registro de alta en sistema VeriFactu** de la AEAT
- [ ] **Envío automático a AEAT** - API de VeriFactu
- [ ] **Certificado digital de la empresa**
- [ ] **Integridad de registros** - No modificables una vez firmados
- [ ] **Fichero LREO** (Libro Registro de Operaciones Económicas)
- [ ] **Logs de auditoría inmutables**
- [ ] **Backup de evidencias criptográficas**

#### Estructura técnica requerida:
```
Factura → Generar Hash SHA-256 → Encadenar con anterior → 
Firmar digitalmente → Generar QR → Enviar a AEAT → 
Almacenar evidencia → Registro LREO
```

#### Sanciones por incumplimiento:
- 💰 **1.000 a 150.000 euros** por no usar software certificado
- 🚫 **Cierre temporal del negocio**
- ⚠️ **Multas accesorias del 50-100% de la cuota defraudada**

---

### 2. 📋 LIBROS REGISTRO OFICIALES (Prioridad: ALTA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **Código de Comercio (Art. 25-31)**
- **Reglamento del Impuesto sobre Sociedades**
- **Ley del IVA (Art. 62-72)**

#### Libros obligatorios:
- [ ] **Libro Diario** - Registro cronológico de operaciones
- [ ] **Libro de Inventarios y Cuentas Anuales**
- [ ] **Libro Mayor** - Movimientos por cuenta contable
- [ ] **Libro de Facturas Emitidas** - Con firma electrónica
- [ ] **Libro de Facturas Recibidas** - Con firma electrónica
- [ ] **Libro Registro de Bienes de Inversión**
- [ ] **Libro Registro IVA Soportado** - Modelo 303
- [ ] **Libro Registro IVA Repercutido** - Modelo 303
- [ ] **Libro Registro de Operaciones Intracomunitarias**

#### Requisitos técnicos:
- Legalización electrónica obligatoria (Registro Mercantil)
- Conservación mínima: **6 años**
- Formato XML estructurado
- Firma electrónica con certificado reconocido
- Sellado de tiempo

---

### 3. 💳 PROTECCIÓN DE DATOS (RGPD/LOPD-GDD) (Prioridad: CRÍTICA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **RGPD (Reglamento UE 2016/679)**
- **LOPDGDD (Ley Orgánica 3/2018)**

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Consentimiento explícito** para datos de clientes
- [ ] **Política de privacidad** integrada
- [ ] **Derecho al olvido** - Borrado completo de datos
- [ ] **Derecho de acceso** - Exportar datos del cliente
- [ ] **Derecho de rectificación** - Corrección de datos
- [ ] **Registro de actividades de tratamiento**
- [ ] **Cifrado de datos personales** (AES-256)
- [ ] **Cifrado de contraseñas** (BCrypt o Argon2)
- [ ] **Logs de acceso a datos personales**
- [ ] **Auditoría de cumplimiento RGPD**
- [ ] **Delegado de Protección de Datos (DPO)** - Información
- [ ] **Notificación de brechas de seguridad** (<72h)
- [ ] **Cláusulas informativas** en formularios
- [ ] **Consentimiento para marketing**
- [ ] **Portabilidad de datos** (formato estructurado)

#### Sanciones:
- 💰 **Hasta 20.000.000 euros** o 4% facturación anual
- 🚫 **Prohibición de tratamiento de datos**

#### Implementación requerida:
```java
// Ejemplo de estructura necesaria
@Entity
class ConsentimientoGDPR {
    Cliente cliente;
    TipoConsentimiento tipo; // DATOS_BASICOS, MARKETING, PERFILADO
    Boolean otorgado;
    LocalDateTime fechaConsentimiento;
    String ipOrigen;
    String textoConsentimiento; // Versión aceptada
}
```

---

### 4. 📊 MODELOS FISCALES AEAT (Prioridad: ALTA)
**Estado:** ❌ NO IMPLEMENTADO

#### Modelos obligatorios a implementar:

##### 📄 Modelos trimestrales:
- [ ] **Modelo 111** - Retenciones IRPF (trabajadores)
- [ ] **Modelo 115** - Retenciones alquileres
- [ ] **Modelo 130** - IRPF (autónomos - pago fraccionado)
- [ ] **Modelo 303** - IVA trimestral
- [ ] **Modelo 349** - Operaciones intracomunitarias

##### 📄 Modelos anuales:
- [ ] **Modelo 190** - Resumen anual IRPF
- [ ] **Modelo 347** - Operaciones con terceros (>3.005,06€)
- [ ] **Modelo 390** - Resumen anual IVA
- [ ] **Modelo 180** - Resumen anual alquileres

##### 📄 Modelos informativos:
- [ ] **Modelo 036/037** - Censo de empresarios
- [ ] **Modelo 340** - Libros registro (si aplica)

#### Funcionalidades requeridas:
- [ ] Generación automática desde datos del ERP
- [ ] Validación de campos obligatorios
- [ ] Exportación a formato SII (Suministro Inmediato Información)
- [ ] Presentación telemática (API AEAT)
- [ ] Certificado digital integrado
- [ ] Recordatorios de presentación
- [ ] Cálculo automático de importes

---

### 5. 📡 SUMINISTRO INMEDIATO DE INFORMACIÓN (SII) (Prioridad: ALTA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **Orden HFP/417/2017** - Sistema SII

#### Obligatorio para:
- Facturación anual > 6.000.000€
- Grupos IVA
- REDEME (Registro de devolución mensual)
- Opcional para el resto (recomendado)

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Envío automático facturas emitidas** (<4 días hábiles)
- [ ] **Envío automático facturas recibidas** (<4 días hábiles)
- [ ] **Envío bienes de inversión**
- [ ] **Envío cobros/pagos en metálico** (>6.000€)
- [ ] **Integración API SOAP AEAT**
- [ ] **Validación XML contra esquema XSD oficial**
- [ ] **Control de estado de envíos** (Aceptado/Rechazado)
- [ ] **Reintento automático en caso de error**
- [ ] **Registro CSV de respuesta AEAT**
- [ ] **Conciliación con libros registro**

#### Estructura de envío SII:
```xml
<sii:SuministroLRFacturasEmitidas>
  <sii:RegistroLRFacturasEmitidas>
    <sii:PeriodoImpositivo>
      <sii:Ejercicio>2025</sii:Ejercicio>
      <sii:Periodo>12</sii:Periodo>
    </sii:PeriodoImpositivo>
    <sii:FacturaExpedida>
      <!-- Datos de la factura -->
    </sii:FacturaExpedida>
  </sii:RegistroLRFacturasEmitidas>
</sii:SuministroLRFacturasEmitidas>
```

---

### 6. 💶 TIPOS DE IVA Y RECARGO DE EQUIVALENCIA (Prioridad: ALTA)
**Estado:** ⚠️ BÁSICO (solo hardcoded en código)

#### Tipos de IVA vigentes en España (2025):
- **IVA General:** 21%
- **IVA Reducido:** 10%
- **IVA Superreducido:** 4%
- **IVA Exento:** 0%

#### Recargo de Equivalencia (minoristas):
- **RE General:** 5,2% (sobre IVA 21%)
- **RE Reducido:** 1,4% (sobre IVA 10%)
- **RE Superreducido:** 0,5% (sobre IVA 4%)

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Gestión configurable de tipos de IVA**
- [ ] **Aplicación automática según artículo**
- [ ] **Aplicación de Recargo de Equivalencia** (si cliente lo solicita)
- [ ] **IVA diferido en importaciones**
- [ ] **Inversión del sujeto pasivo** (construcción, chatarra, oro)
- [ ] **Criterio de caja** (opcional para pymes)
- [ ] **Diferimiento de IVA** (aplazamiento)
- [ ] **Deducción proporcional de IVA** (actividades mixtas)

#### Tabla de configuración necesaria:
```sql
CREATE TABLE tipo_iva (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(50),
    porcentaje DECIMAL(5,2),
    recargo_equivalencia DECIMAL(5,2),
    fecha_inicio DATE,
    fecha_fin DATE,
    activo BOOLEAN
);
```

---

### 7. 🏦 SEPA Y REMESAS BANCARIAS (Prioridad: MEDIA-ALTA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **Reglamento UE 260/2012** - SEPA
- **Normas y procedimientos SEPA Core Direct Debit**

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Generación de ficheros SEPA XML (ISO 20022)**
- [ ] **Remesas de cobro por domiciliación**
- [ ] **Remesas de pago a proveedores**
- [ ] **Validación de IBAN y BIC**
- [ ] **Gestión de mandatos SEPA**
- [ ] **Cuaderno 19 (formato bancario español)**
- [ ] **Cuaderno 34 (transferencias)**
- [ ] **Cuaderno 58 (domiciliaciones)**
- [ ] **Norma 58 CSB (domiciliaciones)**
- [ ] **Comunicación con entidades bancarias**
- [ ] **Conciliación bancaria automática**

#### Formato SEPA requerido:
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02">
  <CstmrDrctDbtInitn>
    <GrpHdr>
      <MsgId>MSG-001-2025</MsgId>
      <CreDtTm>2025-12-26T10:00:00</CreDtTm>
    </GrpHdr>
    <PmtInf>
      <!-- Información de cobros -->
    </PmtInf>
  </CstmrDrctDbtInitn>
</Document>
```

---

### 8. 📝 FACTURA ELECTRÓNICA (Prioridad: ALTA)
**Estado:** ⚠️ PARCIAL (solo impresión)

#### Normativa:
- **Ley 25/2013** - Impulso de la factura electrónica
- **Formato Facturae 3.2.2** - Obligatorio para B2G

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Generación de Facturae (XML)** - Formato oficial AEAT
- [ ] **Firma electrónica XAdES** - Obligatoria en Facturae
- [ ] **Envío a FACe** (plataforma B2G)
- [ ] **Integración con plataformas B2B** (Edicom, Seres, etc.)
- [ ] **Validación de factura electrónica**
- [ ] **Recepción de facturas electrónicas** de proveedores
- [ ] **Conservación electrónica certificada** (6 años)
- [ ] **Sellado de tiempo** (TSA - Time Stamp Authority)
- [ ] **Conversión PDF/A para archivo**

#### Estructura Facturae básica:
```xml
<fe:Facturae>
  <FileHeader>
    <SchemaVersion>3.2.2</SchemaVersion>
    <Modality>I</Modality>
  </FileHeader>
  <Parties>
    <SellerParty><!-- Datos empresa --></SellerParty>
    <BuyerParty><!-- Datos cliente --></BuyerParty>
  </Parties>
  <Invoices>
    <Invoice><!-- Datos factura --></Invoice>
  </Invoices>
  <ds:Signature><!-- Firma XAdES --></ds:Signature>
</fe:Facturae>
```

---

### 9. 🔐 CERTIFICADO DIGITAL Y FIRMA ELECTRÓNICA (Prioridad: CRÍTICA)
**Estado:** ⚠️ PARCIAL (hay certificado pero no integrado)

#### Normativa:
- **Reglamento eIDAS (UE 910/2014)**
- **Ley 6/2020** - Servicios de confianza

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Gestión de certificados digitales** en la aplicación
- [ ] **Almacén de certificados (Keystore)**
- [ ] **Validación de certificados** (no caducados, no revocados)
- [ ] **Firma digital de documentos** (facturas, albaranes)
- [ ] **Verificación de firmas digitales**
- [ ] **Integración con @firma (MINHAP)**
- [ ] **Soporte para múltiples certificados** (empleados)
- [ ] **Renovación automática de certificados**
- [ ] **Validación en lista CRL/OCSP** (revocación)
- [ ] **Firma electrónica cualificada**
- [ ] **Sellado de tiempo TSA**

#### Tipos de certificados necesarios:
- ✅ **Certificado de persona jurídica** (empresa) - Existe pero no integrado
- [ ] **Certificado de representante** (administrador)
- [ ] **Certificado de sede electrónica** (si aplica)
- [ ] **Certificado de sello electrónico** (automático)

---

### 10. 📋 FACTURACIÓN SEGÚN NORMATIVA (Prioridad: CRÍTICA)
**Estado:** ⚠️ BÁSICO (faltan campos obligatorios)

#### Normativa:
- **RD 1619/2012** - Reglamento de facturación

#### Datos obligatorios en TODA factura:
- [x] Número de factura (serie y número correlativo)
- [x] Fecha de expedición
- [x] Fecha de operación (si difiere)
- [x] Datos del emisor (NIF, nombre, domicilio)
- [x] Datos del destinatario (NIF, nombre, domicilio)
- [ ] **Descripción de operaciones** (detallada)
- [x] Base imponible
- [x] Tipo impositivo
- [x] Cuota de IVA
- [x] Total factura
- [ ] **Medio de cobro** (si es distinto al habitual)
- [ ] **Retención IRPF** (si aplica)
- [ ] **Fecha de vencimiento** (si hay aplazamiento)
- [ ] **Referencia del pedido** (si existe)
- [ ] **Número de albarán** (si existe)

#### Datos adicionales obligatorios según caso:
- [ ] **Inversión del sujeto pasivo** - "Inversión del sujeto pasivo"
- [ ] **Régimen especial de criterio de caja** - Indicación expresa
- [ ] **Régimen de viajeros** - "IVA dispensado viajeros"
- [ ] **Operaciones triangulares** - Indicación expresa
- [ ] **Autoconsumo** - Base imponible especial

#### ❌ FALTA EN EL SISTEMA:
- [ ] Validación automática de campos obligatorios
- [ ] Impedimento de emitir sin datos completos
- [ ] Nomenclatura correcta según normativa
- [ ] Distinción de tipos de factura (ordinaria, simplificada, rectificativa)

---

### 11. 🧾 FACTURAS RECTIFICATIVAS Y ABONOS (Prioridad: ALTA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **Art. 15 RD 1619/2012**

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Facturas rectificativas** - Corrección de errores
- [ ] **Abonos completos** - Devolución total
- [ ] **Abonos parciales** - Devolución parcial
- [ ] **Referencia a factura original** (número y fecha)
- [ ] **Motivo de rectificación** (obligatorio)
- [ ] **Tipo de rectificación** (sustitución o diferencias)
- [ ] **Recalculo de IVA** en facturas rectificativas
- [ ] **Registro en SII** como rectificativa
- [ ] **Afectación a libros registro**

#### Tipos de rectificación:
```
1. Por error en datos (NIF, importe, etc.)
2. Por descuento posterior
3. Por devolución de mercancía
4. Por impago (con requisitos)
5. Por concurso de acreedores
```

---

### 12. 📊 CONTABILIDAD SEGÚN PLAN GENERAL CONTABLE (Prioridad: MEDIA-ALTA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **RD 1514/2007** - Plan General de Contabilidad (PGC)
- **RD 1515/2007** - PGC de PYMES

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Plan General Contable completo** (grupos 1-9)
- [ ] **Asientos predefinidos**
- [ ] **Regularización contable**
- [ ] **Cierre contable anual**
- [ ] **Balance de Comprobación**
- [ ] **Balance de Situación**
- [ ] **Cuenta de Pérdidas y Ganancias**
- [ ] **Estado de Cambios en el Patrimonio Neto**
- [ ] **Estado de Flujos de Efectivo**
- [ ] **Memoria contable**
- [ ] **Amortizaciones automáticas**
- [ ] **Provisiones**
- [ ] **Periodificaciones**

#### Cuentas principales del PGC:
```
Grupo 1: Financiación básica
Grupo 2: Inmovilizado
Grupo 3: Existencias
Grupo 4: Acreedores y deudores
Grupo 5: Cuentas financieras
Grupo 6: Compras y gastos
Grupo 7: Ventas e ingresos
Grupo 8: Gastos imputados al patrimonio
Grupo 9: Ingresos imputados al patrimonio
```

---

### 13. 📦 TRAZABILIDAD ALIMENTARIA (Prioridad: MEDIA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa (específica para panaderías):
- **Reglamento CE 178/2002** - Principios generales de legislación alimentaria
- **RD 191/2011** - Registro General Sanitario de Empresas Alimentarias

#### ❌ FALTA IMPLEMENTAR:
- [ ] **Número RGSEAA** (Registro sanitario)
- [ ] **Trazabilidad de lotes** de producción
- [ ] **Fecha de elaboración y caducidad**
- [ ] **Control de alérgenos** por producto
- [ ] **Registro de proveedores de materias primas**
- [ ] **Temperatura de almacenamiento**
- [ ] **Control de calidad**
- [ ] **Incidencias y devoluciones**
- [ ] **Alertas alimentarias**
- [ ] **Etiquetado obligatorio** (ingredientes, alérgenos, nutricional)

#### Información obligatoria en etiquetas:
```
- Denominación del producto
- Lista de ingredientes
- Alérgenos (destacados)
- Cantidad neta
- Fecha de consumo preferente / caducidad
- Condiciones de conservación
- RGSEAA del fabricante
- Información nutricional
- Lote de fabricación
```

---

### 14. 💼 PREVENCIÓN DE RIESGOS LABORALES (Prioridad: BAJA)
**Estado:** ❌ NO IMPLEMENTADO (fuera de alcance ERP básico)

#### Normativa:
- **Ley 31/1995** - Prevención de Riesgos Laborales

#### Funcionalidades opcionales:
- [ ] Gestión de formaciones PRL
- [ ] Registro de accidentes laborales
- [ ] Control de EPIs (equipos de protección)
- [ ] Plan de prevención
- [ ] Evaluación de riesgos

---

### 15. 🌍 MEDIO AMBIENTE Y RESIDUOS (Prioridad: BAJA)
**Estado:** ❌ NO IMPLEMENTADO

#### Normativa:
- **Ley 7/2022** - Residuos y suelos contaminados
- **Ley 11/2022** - Envases y residuos de envases

#### Funcionalidades opcionales:
- [ ] Registro de residuos generados
- [ ] Gestión de envases
- [ ] Punto verde (ecoembes)
- [ ] Certificados de destrucción
- [ ] Memoria anual de envases

---

## 📊 CUMPLIMIENTO LEGAL - RESUMEN

### 🔴 CRÍTICO Y OBLIGATORIO (Sanciones graves)
1. ✅ **VeriFactu** - PARCIAL (falta integración completa)
2. ❌ **RGPD** - NO CUMPLE
3. ❌ **Certificado Digital integrado** - NO CUMPLE
4. ❌ **Facturación normativa completa** - PARCIAL
5. ❌ **Libros Registro oficiales** - NO CUMPLE

### 🟠 OBLIGATORIO (Según actividad y volumen)
6. ❌ **SII** - NO CUMPLE (si facturación >6M€)
7. ❌ **Modelos fiscales AEAT** - NO CUMPLE
8. ❌ **SEPA y remesas** - NO CUMPLE
9. ❌ **Factura electrónica** - NO CUMPLE (B2G obligatorio)

### 🟡 RECOMENDADO (Buenas prácticas)
10. ❌ **Contabilidad PGC** - NO CUMPLE
11. ❌ **Facturas rectificativas** - NO CUMPLE
12. ❌ **Trazabilidad alimentaria** - NO CUMPLE (sector panadería)

### 🟢 OPCIONAL (Mejoras)
13. ❌ **PRL** - NO CUMPLE (opcional)
14. ❌ **Medio ambiente** - NO CUMPLE (opcional)

---

## ⚠️ RIESGO LEGAL ACTUAL

### Estado de cumplimiento: **30% CRÍTICO**

**Riesgos identificados:**
- 🚨 **ALTO:** Sin RGPD completo → Multas hasta 20M€
- 🚨 **ALTO:** VeriFactu incompleto → Multas 1.000-150.000€
- ⚠️ **MEDIO:** Sin libros oficiales → Multas tributarias
- ⚠️ **MEDIO:** Sin modelos fiscales → Multas AEAT
- ⚠️ **MEDIO:** Facturación sin campos obligatorios → Facturas nulas

**Recomendación:**
❗ **NO USAR EN PRODUCCIÓN** hasta completar requisitos CRÍTICOS

---

## 🎯 ROADMAP DE CUMPLIMIENTO LEGAL

### FASE 1: CRÍTICO INMEDIATO (2-3 semanas)
**Objetivo: Poder operar legalmente**

1. **Completar VeriFactu** (1 semana)
   - Firma digital de facturas
   - Cadena de bloques
   - Envío a AEAT
   - QR en facturas

2. **Implementar RGPD básico** (1 semana)
   - Consentimientos
   - Cifrado de datos
   - Derechos ARCO (acceso, rectificación, cancelación, oposición)
   - Política de privacidad

3. **Integrar Certificado Digital** (3 días)
   - Gestión de certificados
   - Firma de documentos
   - Validación

4. **Completar datos obligatorios facturación** (2 días)
   - Validación de campos
   - Nomenclatura correcta
   - Tipos de factura

### FASE 2: OBLIGATORIO FISCAL (2-3 semanas)
**Objetivo: Cumplir con Hacienda**

5. **Implementar Libros Registro** (1 semana)
   - Libro de facturas emitidas
   - Libro de facturas recibidas
   - Libro IVA
   - Legalización electrónica

6. **Desarrollar módulo de Modelos AEAT** (1 semana)
   - Modelo 303 (IVA)
   - Modelo 347 (Operaciones terceros)
   - Modelo 390 (Resumen anual IVA)
   - Exportación y presentación

7. **Implementar SII** (1 semana)
   - API SOAP AEAT
   - Envío automático
   - Control de estados

### FASE 3: FUNCIONALIDAD FINANCIERA (2 semanas)
**Objetivo: Gestión completa de cobros/pagos**

8. **SEPA y Remesas** (1 semana)
   - Generación XML SEPA
   - Remesas de cobro
   - Remesas de pago
   - Validación IBAN

9. **Factura Electrónica** (1 semana)
   - Formato Facturae
   - Firma XAdES
   - Envío FACe

### FASE 4: CONTABILIDAD (2 semanas)
**Objetivo: Gestión contable completa**

10. **Plan General Contable** (2 semanas)
    - PGC completo
    - Asientos automáticos
    - Informes contables

### FASE 5: SECTOR ESPECÍFICO (1 semana)
**Objetivo: Cumplimiento sanitario**

11. **Trazabilidad Alimentaria** (1 semana)
    - Lotes
    - Alérgenos
    - RGSEAA
    - Etiquetado

---

## 📋 CHECKLIST DE CUMPLIMIENTO LEGAL

### Antes de usar en producción, verificar:

#### VeriFactu
- [ ] Facturas con firma digital
- [ ] Hash SHA-256 implementado
- [ ] Cadena de bloques funcional
- [ ] QR generado en cada factura
- [ ] Envío a AEAT configurado
- [ ] Certificado digital válido
- [ ] Logs de auditoría inmutables

#### RGPD
- [ ] Política de privacidad visible
- [ ] Consentimientos implementados
- [ ] Cifrado de datos personales (AES-256)
- [ ] Cifrado de contraseñas (BCrypt)
- [ ] Derecho de acceso implementado
- [ ] Derecho al olvido implementado
- [ ] Logs de acceso a datos
- [ ] Procedimiento de brechas de seguridad

#### Facturación
- [ ] Todos los campos obligatorios presentes
- [ ] Numeración correlativa sin huecos
- [ ] Validación de datos antes de emitir
- [ ] Conservación 6 años garantizada
- [ ] Facturas rectificativas disponibles

#### Fiscal
- [ ] Libros registro generados automáticamente
- [ ] Modelos AEAT disponibles
- [ ] SII configurado (si aplica)
- [ ] Tipos de IVA configurables
- [ ] Recargo de equivalencia implementado

#### Seguridad
- [ ] Certificado digital integrado
- [ ] Firma electrónica operativa
- [ ] Backup automático configurado
- [ ] Control de acceso por usuarios
- [ ] Auditoría de acciones

---

## 🔗 RECURSOS Y ENLACES OFICIALES

### Legislación:
- **AEAT:** https://www.agenciatributaria.es
- **VeriFactu:** https://sede.agenciatributaria.gob.es/Sede/verifactu.html
- **BOE:** https://www.boe.es
- **AEPD (RGPD):** https://www.aepd.es

### Herramientas:
- **Cl@ve Firma:** https://clave.gob.es
- **FACe (B2G):** https://face.gob.es
- **VAT Information Exchange System:** https://ec.europa.eu/taxation_customs/vies/

### Formatos oficiales:
- **Facturae 3.2.2:** https://www.facturae.gob.es
- **Esquemas XSD SII:** https://www2.agenciatributaria.gob.es/L/SII
- **SEPA ISO 20022:** https://www.iso20022.org

---

## 💰 COSTES DE CUMPLIMIENTO

### Desarrollo estimado:
- **Fase 1 (Crítico):** 120-160 horas → 3.000-4.000€
- **Fase 2 (Fiscal):** 120-160 horas → 3.000-4.000€
- **Fase 3 (Financiero):** 80 horas → 2.000€
- **Fase 4 (Contabilidad):** 80 horas → 2.000€
- **Fase 5 (Sector):** 40 horas → 1.000€

**TOTAL:** 440-520 horas → **11.000-13.000€**

### Costes recurrentes:
- **Certificado digital:** 15-50€/año
- **Sellado de tiempo TSA:** 100-300€/año
- **Mantenimiento legal:** 1.000-2.000€/año

---

## ✅ CONCLUSIÓN

Para que el ERP **Panadería Tahona** sea **legal y válido en España**, es **IMPRESCINDIBLE**:

### 🚨 Corto plazo (1 mes):
1. ✅ Completar VeriFactu al 100%
2. ✅ Implementar RGPD completo
3. ✅ Integrar certificado digital
4. ✅ Validar facturación normativa

### 📅 Medio plazo (2-3 meses):
5. ✅ Libros registro oficiales
6. ✅ Modelos fiscales AEAT
7. ✅ SII (si >6M€ facturación)
8. ✅ SEPA y remesas

### 🎯 Largo plazo (6 meses):
9. ✅ Contabilidad PGC completa
10. ✅ Trazabilidad alimentaria
11. ✅ Factura electrónica B2B

---

**Sin estos requisitos, el ERP NO es legalmente utilizable en España.**

---

*Documento actualizado el 26 de diciembre de 2025*
*Legislación vigente a fecha de redacción*

