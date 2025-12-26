# 📋 PLAN DE ACCIÓN COMPLETO - ERP Panadería Tahona

**Fecha:** 26 de diciembre de 2025  
**Objetivo:** Convertir el ERP en un sistema legal, profesional y completo para España

---

## 🎯 ESTADO ACTUAL

### ✅ Lo que funciona (40% completitud):
- Gestión básica de clientes, proveedores, artículos
- Facturas y albaranes
- Almacenes básicos
- VeriFactu básico (sin firma digital)
- Sistema de impresión

### ❌ Lo que falta (60%):
- **Legal:** Cumplimiento completo de normativa española
- **Funcional:** Módulos profesionales (CRM, BI, TPV, etc.)
- **Seguridad:** Autenticación, RGPD, cifrado
- **Fiscal:** SII, modelos AEAT, libros registro
- **Financiero:** Tesorería, remesas SEPA, contabilidad

---

## 🚨 ANÁLISIS DE RIESGO LEGAL

### Riesgos CRÍTICOS actuales:

| Riesgo | Multa potencial | Probabilidad | Urgencia |
|--------|----------------|--------------|----------|
| **RGPD incompleto** | Hasta 20M€ | Alta | 🔴 Crítica |
| **VeriFactu sin firma** | 1.000-150.000€ | Alta | 🔴 Crítica |
| **Sin libros registro** | Variable | Media | 🟠 Alta |
| **Sin modelos AEAT** | Variable | Media | 🟠 Alta |
| **Facturas sin campos obligatorios** | Factura nula | Media | 🟠 Alta |

### ⚠️ RECOMENDACIÓN:
**NO USAR EN PRODUCCIÓN** hasta completar requisitos CRÍTICOS (FASE 1)

---

## 📊 COMPARATIVA: ESTADO ACTUAL VS OBJETIVO

### Funcionalidades por categoría:

| Categoría | Actual | Objetivo | Gap |
|-----------|--------|----------|-----|
| **Legal/Fiscal** | 20% | 100% | 80% ❌ |
| **Seguridad** | 10% | 100% | 90% ❌ |
| **Gestión comercial** | 40% | 100% | 60% ❌ |
| **Gestión financiera** | 5% | 100% | 95% ❌ |
| **Gestión stock** | 30% | 100% | 70% ❌ |
| **Analytics/BI** | 0% | 100% | 100% ❌ |
| **CRM** | 0% | 100% | 100% ❌ |
| **RRHH** | 0% | 80% | 80% ❌ |
| **TPV** | 0% | 100% | 100% ❌ |
| **E-commerce** | 0% | 60% | 60% ❌ |

**COMPLETITUD GLOBAL:** 10.5% → **OBJETIVO: 100%**

---

## 🎯 ROADMAP COMPLETO (12 MESES)

### 📅 FASE 1: LEGALIZACIÓN (Mes 1-2) - CRÍTICO
**Objetivo:** Poder operar legalmente en España

#### Semana 1-2: VeriFactu Completo
- [x] Panel básico (✅ ya existe)
- [ ] Implementar firma digital SHA-256
- [ ] Implementar cadena de bloques
- [ ] Generar QR en facturas
- [ ] Integrar certificado digital
- [ ] API de envío a AEAT
- [ ] Fichero LREO
- [ ] Logs inmutables

**Entregable:** Facturas con firma digital y QR operativas

#### Semana 3-4: RGPD Completo
- [ ] Sistema de consentimientos
- [ ] Cifrado AES-256 de datos personales
- [ ] Cifrado BCrypt de contraseñas
- [ ] Implementar derecho al olvido
- [ ] Implementar derecho de acceso
- [ ] Implementar portabilidad de datos
- [ ] Logs de acceso a datos personales
- [ ] Política de privacidad integrada
- [ ] Procedimiento de brechas de seguridad

**Entregable:** Sistema RGPD compliant con auditoría

#### Semana 5-6: Certificado Digital y Seguridad
- [ ] Integrar certificado digital empresa
- [ ] Firma electrónica de documentos
- [ ] Validación de certificados
- [ ] Keystore management
- [ ] CRL/OCSP checking

**Entregable:** Sistema de firma operativo

#### Semana 7-8: Campos Obligatorios Facturación
- [ ] Validar todos los campos obligatorios RD 1619/2012
- [ ] Implementar tipos de factura (ordinaria, simplificada, rectificativa)
- [ ] Validación pre-emisión
- [ ] Nomenclatura correcta
- [ ] Facturas rectificativas y abonos

**Entregable:** Facturación 100% normativa

**INVERSIÓN FASE 1:** 320 horas → **8.000€**

---

### 📅 FASE 2: FISCAL Y CONTABLE (Mes 3-4) - ALTA PRIORIDAD
**Objetivo:** Cumplir con obligaciones fiscales

#### Semana 1-2: Libros Registro
- [ ] Libro de facturas emitidas
- [ ] Libro de facturas recibidas
- [ ] Libro registro IVA soportado
- [ ] Libro registro IVA repercutido
- [ ] Generación automática desde facturas
- [ ] Exportación a XML
- [ ] Legalización electrónica (Registro Mercantil)

**Entregable:** Libros oficiales operativos

#### Semana 3-4: Modelos AEAT
- [ ] Modelo 303 (IVA trimestral)
- [ ] Modelo 347 (Operaciones >3.005€)
- [ ] Modelo 390 (IVA anual)
- [ ] Generación automática desde datos ERP
- [ ] Validación de campos
- [ ] Exportación a formato AEAT

**Entregable:** Modelos fiscales generables

#### Semana 5-6: SII (Suministro Inmediato Información)
- [ ] API SOAP AEAT
- [ ] Envío automático facturas emitidas (<4 días)
- [ ] Envío automático facturas recibidas (<4 días)
- [ ] Validación XSD
- [ ] Control de estados (Aceptado/Rechazado)
- [ ] Reintento automático
- [ ] CSV de respuesta

**Entregable:** Integración SII completa

#### Semana 7-8: Tipos de IVA y Configuración
- [ ] Tabla configurable de tipos IVA
- [ ] Recargo de equivalencia
- [ ] Inversión del sujeto pasivo
- [ ] IVA diferido
- [ ] Criterio de caja
- [ ] Aplicación automática según artículo

**Entregable:** Sistema IVA flexible y completo

**INVERSIÓN FASE 2:** 320 horas → **8.000€**

---

### 📅 FASE 3: SEGURIDAD Y USUARIOS (Mes 5) - CRÍTICO
**Objetivo:** Control de acceso y auditoría

#### Semana 1-2: Sistema de Usuarios
- [ ] Entidad Usuario y Rol
- [ ] Pantalla de login
- [ ] Cifrado de contraseñas (BCrypt)
- [ ] Gestión de sesiones
- [ ] Pantalla de gestión de usuarios
- [ ] Pantalla de gestión de roles
- [ ] Asignación de permisos

**Entregable:** Sistema de autenticación operativo

#### Semana 3-4: Control de Acceso y Auditoría
- [ ] Permisos por módulo
- [ ] Permisos por operación (crear, leer, editar, eliminar)
- [ ] Tabla de auditoría
- [ ] Registro de todas las acciones
- [ ] Logs de acceso
- [ ] Consulta de auditoría
- [ ] Informes de auditoría

**Entregable:** Sistema auditado y seguro

**INVERSIÓN FASE 3:** 160 horas → **4.000€**

---

### 📅 FASE 4: FINANCIERO Y TESORERÍA (Mes 6-7) - ALTA
**Objetivo:** Gestión completa de cobros y pagos

#### Semana 1-2: Cobros y Pagos
- [ ] Entidades: Cobro, Pago, FormaPago
- [ ] Panel de Tesorería
- [ ] Registro de cobros
- [ ] Registro de pagos
- [ ] Vinculación con facturas
- [ ] Vencimientos
- [ ] Arqueo de caja
- [ ] Conciliación bancaria

**Entregable:** Módulo de Tesorería operativo

#### Semana 3-4: SEPA y Remesas
- [ ] Generación fichero SEPA XML (ISO 20022)
- [ ] Remesas de cobro (mandatos SEPA)
- [ ] Remesas de pago a proveedores
- [ ] Validación IBAN/BIC
- [ ] Cuaderno 19, 34, 58
- [ ] Norma 58 CSB

**Entregable:** Remesas bancarias operativas

#### Semana 5-6: Factura Electrónica
- [ ] Generación Facturae 3.2.2 (XML)
- [ ] Firma XAdES
- [ ] Envío a FACe (B2G)
- [ ] Validación de factura electrónica
- [ ] Recepción de facturas de proveedores
- [ ] Sellado de tiempo (TSA)
- [ ] Conversión PDF/A

**Entregable:** Facturación electrónica B2G

#### Semana 7-8: Contabilidad Básica
- [ ] Plan General Contable (grupos 1-9)
- [ ] Entidades: Cuenta, Asiento, AsientoLinea
- [ ] Asientos automáticos desde facturas
- [ ] Libro Diario
- [ ] Libro Mayor
- [ ] Balance de Comprobación
- [ ] Balance de Situación
- [ ] Cuenta de Pérdidas y Ganancias

**Entregable:** Contabilidad operativa

**INVERSIÓN FASE 4:** 320 horas → **8.000€**

---

### 📅 FASE 5: CRM Y COMERCIAL (Mes 8-9) - ALTA
**Objetivo:** Gestión de relaciones con clientes

#### Semana 1-2: CRM Básico
- [ ] Ficha completa de cliente
- [ ] Múltiples contactos y direcciones
- [ ] Segmentación de clientes
- [ ] Clasificación ABC
- [ ] Historial de interacciones
- [ ] Notas y comentarios

**Entregable:** CRM básico operativo

#### Semana 3-4: Leads y Oportunidades
- [ ] Entidad Lead
- [ ] Pipeline de ventas
- [ ] Estados de lead
- [ ] Conversión lead → cliente
- [ ] Tareas comerciales
- [ ] Recordatorios automáticos

**Entregable:** Gestión comercial completa

#### Semana 5-6: Pedidos
- [ ] Entidad Pedido (venta y compra)
- [ ] Panel de pedidos
- [ ] Formulario de pedido
- [ ] Estados de pedido
- [ ] Conversión pedido → albarán
- [ ] Conversión pedido → factura
- [ ] Seguimiento de pedidos

**Entregable:** Módulo de pedidos operativo

#### Semana 7-8: Presupuestos
- [ ] Entidad Presupuesto
- [ ] Creación de presupuestos
- [ ] Plantillas de presupuesto
- [ ] Estados (pendiente, enviado, aceptado, rechazado)
- [ ] Conversión a pedido/factura
- [ ] Análisis de tasa de conversión

**Entregable:** Módulo de presupuestos

**INVERSIÓN FASE 5:** 320 horas → **8.000€**

---

### 📅 FASE 6: BI Y ANALYTICS (Mes 10) - ALTA
**Objetivo:** Visibilidad y toma de decisiones

#### Semana 1-2: Dashboard Principal
- [ ] KPIs principales (ventas, margen, ticket medio)
- [ ] Gráficos interactivos (JavaFX Charts)
- [ ] Comparativas periodo anterior
- [ ] Top productos
- [ ] Top clientes
- [ ] Evolución temporal

**Entregable:** Dashboard operativo

#### Semana 3-4: Informes y Análisis
- [ ] Informe de ventas por periodo
- [ ] Análisis de rentabilidad
- [ ] Análisis ABC de productos
- [ ] Análisis RFM de clientes
- [ ] Rotación de stock
- [ ] Análisis de compras
- [ ] Cash Flow
- [ ] Exportación a Excel/PDF (Apache POI)

**Entregable:** Sistema de informes completo

**INVERSIÓN FASE 6:** 160 horas → **4.000€**

---

### 📅 FASE 7: TPV (Mes 11) - ALTA (Panadería)
**Objetivo:** Venta directa al público

#### Semana 1-2: Interfaz TPV
- [ ] Pantalla táctil optimizada
- [ ] Botones de productos
- [ ] Categorías visuales
- [ ] Búsqueda rápida
- [ ] Carrito de compra
- [ ] Cálculo de cambio

**Entregable:** Interfaz TPV operativa

#### Semana 3-4: Cobros y Tickets
- [ ] Múltiples formas de pago
- [ ] Pago mixto
- [ ] Impresión de tickets
- [ ] Tickets electrónicos
- [ ] QR en tickets
- [ ] Integración con TPV físico

**Entregable:** Sistema de cobros completo

#### Semana 5-6: Arqueo y Control
- [ ] Entidad Caja y ArqueoCaja
- [ ] Apertura de caja
- [ ] Cierre de caja
- [ ] Cuadre de caja
- [ ] Movimientos de caja
- [ ] Informes por turno/empleado

**Entregable:** Control de caja operativo

#### Semana 7-8: Funcionalidades Avanzadas
- [ ] Tarjeta de fidelización
- [ ] Acumulación de puntos
- [ ] Tickets pendientes
- [ ] Devoluciones
- [ ] Descuentos automáticos

**Entregable:** TPV completo

**INVERSIÓN FASE 7:** 240 horas → **6.000€**

---

### 📅 FASE 8: COMPRAS Y STOCK (Mes 12) - MEDIA
**Objetivo:** Gestión completa de compras e inventario

#### Semana 1-2: Ciclo de Compra
- [ ] Solicitudes de compra
- [ ] Pedidos a proveedores
- [ ] Albaranes de compra
- [ ] Facturas de compra
- [ ] Control tres vías
- [ ] Incidencias

**Entregable:** Ciclo de compra completo

#### Semana 3-4: Stock Avanzado
- [ ] Movimientos de stock
- [ ] Traspasos entre almacenes
- [ ] Inventarios físicos
- [ ] Regularizaciones
- [ ] Stock mínimo/máximo
- [ ] Alertas de stock bajo
- [ ] Valoración FIFO/LIFO/PMP
- [ ] Punto de reorden automático

**Entregable:** Gestión de stock avanzada

**INVERSIÓN FASE 8:** 160 horas → **4.000€**

---

### 📅 FASE 9: MÓDULOS SECTOR (Post Mes 12) - MEDIA
**Objetivo:** Específico para panadería

#### Producción
- [ ] Escandallos (recetas)
- [ ] Órdenes de fabricación
- [ ] Consumo de materias primas
- [ ] Control de mermas
- [ ] Costes de producción

#### Trazabilidad Alimentaria
- [ ] Número RGSEAA
- [ ] Trazabilidad de lotes
- [ ] Control de alérgenos
- [ ] Fecha elaboración/caducidad
- [ ] Registro de temperatura
- [ ] Etiquetado obligatorio

#### RRHH Básico
- [ ] Ficha de empleado
- [ ] Control de presencia
- [ ] Gestión de turnos
- [ ] Vacaciones y permisos
- [ ] Formación
- [ ] Nóminas básicas

**INVERSIÓN FASE 9:** 240 horas → **6.000€**

---

### 📅 FASE 10: OPTIMIZACIÓN Y CALIDAD (Post Mes 12) - MEDIA
**Objetivo:** Calidad y rendimiento

#### Testing
- [ ] Tests unitarios (JUnit)
- [ ] Tests de integración
- [ ] Tests de UI (TestFX)
- [ ] Cobertura >70%

#### Optimización
- [ ] Análisis de rendimiento
- [ ] Optimización de consultas SQL
- [ ] Caché de datos
- [ ] Paginación de listados

#### Backups
- [ ] Backup automático programado
- [ ] Backup manual
- [ ] Restauración de backups
- [ ] Almacenamiento en nube

#### Comunicaciones
- [ ] JavaMail integrado
- [ ] Envío de facturas por email
- [ ] Plantillas de email
- [ ] Configuración SMTP
- [ ] Registro de comunicaciones

**INVERSIÓN FASE 10:** 160 horas → **4.000€**

---

## 💰 INVERSIÓN TOTAL

### Resumen por fase:

| Fase | Duración | Horas | Coste | Prioridad |
|------|----------|-------|-------|-----------|
| **FASE 1: Legalización** | 2 meses | 320h | 8.000€ | 🔴 CRÍTICA |
| **FASE 2: Fiscal** | 2 meses | 320h | 8.000€ | 🟠 ALTA |
| **FASE 3: Seguridad** | 1 mes | 160h | 4.000€ | 🔴 CRÍTICA |
| **FASE 4: Financiero** | 2 meses | 320h | 8.000€ | 🟠 ALTA |
| **FASE 5: CRM** | 2 meses | 320h | 8.000€ | 🟠 ALTA |
| **FASE 6: BI** | 1 mes | 160h | 4.000€ | 🟠 ALTA |
| **FASE 7: TPV** | 1.5 meses | 240h | 6.000€ | 🟠 ALTA |
| **FASE 8: Compras** | 1 mes | 160h | 4.000€ | 🟡 MEDIA |
| **FASE 9: Sector** | Variable | 240h | 6.000€ | 🟡 MEDIA |
| **FASE 10: Calidad** | Variable | 160h | 4.000€ | 🟡 MEDIA |

**TOTAL PROYECTO:** 2.400 horas → **60.000€**

### Desglose temporal:
- **MVP Legal (Fases 1-3):** 5 meses → **20.000€** ✅ Mínimo viable
- **ERP Funcional (Fases 1-6):** 10 meses → **40.000€** ✅ Recomendado
- **ERP Completo (Fases 1-10):** 12-15 meses → **60.000€** ✅ Profesional

---

## 🎯 HITOS Y ENTREGABLES

### 🏁 HITO 1: MVP LEGAL (Mes 5)
**Entregables:**
- ✅ Sistema VeriFactu certificable
- ✅ RGPD completo y auditado
- ✅ Facturación 100% normativa
- ✅ Sistema de usuarios y permisos
- ✅ Auditoría completa

**Resultado:** ERP legal y utilizable en España

---

### 🏁 HITO 2: ERP FUNCIONAL (Mes 10)
**Entregables:**
- ✅ Todo lo del Hito 1
- ✅ Libros registro oficiales
- ✅ Modelos AEAT
- ✅ SII integrado
- ✅ Tesorería y remesas SEPA
- ✅ Contabilidad PGC
- ✅ CRM completo
- ✅ Pedidos y presupuestos
- ✅ Dashboard e informes

**Resultado:** ERP profesional completo

---

### 🏁 HITO 3: ERP AVANZADO (Mes 15)
**Entregables:**
- ✅ Todo lo del Hito 2
- ✅ TPV operativo
- ✅ Compras avanzadas
- ✅ Stock avanzado
- ✅ Producción (escandallos)
- ✅ Trazabilidad alimentaria
- ✅ RRHH básico
- ✅ Backups automáticos
- ✅ Testing completo

**Resultado:** ERP de nivel enterprise

---

## 📊 MÉTRICAS DE ÉXITO

### Indicadores de completitud:

| Categoría | Actual | Objetivo Hito 1 | Objetivo Hito 2 | Objetivo Hito 3 |
|-----------|--------|-----------------|-----------------|-----------------|
| **Legal** | 20% | 100% ✅ | 100% ✅ | 100% ✅ |
| **Seguridad** | 10% | 100% ✅ | 100% ✅ | 100% ✅ |
| **Funcional** | 30% | 40% | 85% ✅ | 100% ✅ |
| **BI/Analytics** | 0% | 0% | 100% ✅ | 100% ✅ |
| **Sector** | 5% | 5% | 20% | 80% ✅ |

**GLOBAL:** 13% → **49%** → **81%** → **96%**

---

## ⚠️ RIESGOS Y MITIGACIÓN

### Riesgos técnicos:

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| **API AEAT caída** | Media | Alto | Sistema de cola y reintento |
| **Certificado caducado** | Baja | Crítico | Alertas 30 días antes |
| **Pérdida de datos** | Baja | Crítico | Backup automático diario |
| **Rendimiento BD** | Media | Medio | Optimización e índices |
| **Cambio normativa** | Alta | Alto | Arquitectura flexible |

### Riesgos de proyecto:

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| **Desviación tiempo** | Alta | Medio | Desarrollo iterativo |
| **Desviación coste** | Media | Medio | Fases independientes |
| **Cambio requisitos** | Alta | Medio | Metodología ágil |
| **Falta recursos** | Media | Alto | Outsourcing selectivo |

---

## 🔄 METODOLOGÍA DE TRABAJO

### Enfoque recomendado: **Agile/Scrum**

#### Sprints de 2 semanas:
1. **Planning** - Definir tareas del sprint
2. **Desarrollo** - Implementación
3. **Testing** - Pruebas
4. **Review** - Demo al stakeholder
5. **Retrospectiva** - Mejoras continuas

#### Entregables por sprint:
- Código funcional
- Tests pasados
- Documentación actualizada
- Demo operativa

---

## 🛠️ STACK TECNOLÓGICO RECOMENDADO

### Backend (actual):
- ✅ Java 21
- ✅ Spring Boot 3.5.7
- ✅ JPA/Hibernate
- ✅ MySQL 8

### Frontend (actual):
- ✅ JavaFX 21

### Librerías adicionales necesarias:

#### Legal/Fiscal:
- **Firma digital:** BouncyCastle
- **SEPA XML:** Apache Commons / JAXB
- **Facturae:** Facturae-API
- **PDF:** iText 8 o Apache PDFBox
- **Excel:** Apache POI

#### Seguridad:
- **Spring Security** (opcional)
- **BCrypt:** Spring Security Crypto
- **JWT:** jjwt (si API REST)

#### Comunicaciones:
- **Email:** JavaMail API
- **HTTP:** Apache HttpClient

#### Analytics:
- **Charts:** JavaFX Charts (built-in)
- **Reporting:** JasperReports

#### Testing:
- **JUnit 5**
- **Mockito**
- **TestFX** (UI testing)
- **Testcontainers** (BD testing)

---

## 📋 CHECKLIST PRE-PRODUCCIÓN

### Antes de usar en producción real, verificar:

#### ✅ Legal y Fiscal:
- [ ] VeriFactu con firma digital operativo
- [ ] Certificado digital válido y configurado
- [ ] RGPD completo implementado
- [ ] Política de privacidad visible
- [ ] Facturación con todos los campos obligatorios
- [ ] Libros registro generándose automáticamente
- [ ] Modelos AEAT configurados
- [ ] SII configurado (si >6M€)

#### ✅ Seguridad:
- [ ] Sistema de login operativo
- [ ] Usuarios y roles configurados
- [ ] Contraseñas cifradas
- [ ] Auditoría activada
- [ ] Backup automático configurado
- [ ] Permisos por módulo

#### ✅ Funcional:
- [ ] Todos los módulos core funcionando
- [ ] Flujo completo: presupuesto → pedido → albarán → factura → cobro
- [ ] Impresión de documentos OK
- [ ] Envío de emails OK
- [ ] Informes principales disponibles

#### ✅ Datos maestros:
- [ ] Datos de empresa completos
- [ ] Tipos de IVA configurados
- [ ] Series de numeración configuradas
- [ ] Formas de pago configuradas
- [ ] Almacenes configurados

#### ✅ Testing:
- [ ] Tests unitarios pasados
- [ ] Tests de integración pasados
- [ ] Pruebas de carga realizadas
- [ ] Pruebas de usuario aceptadas

#### ✅ Documentación:
- [ ] Manual de usuario
- [ ] Manual de administrador
- [ ] Documentación técnica
- [ ] Procedimientos de backup/restore

---

## 📞 SOPORTE Y MANTENIMIENTO

### Post-lanzamiento:

#### Mantenimiento correctivo:
- Resolución de bugs
- Parches de seguridad
- Actualizaciones de dependencias

#### Mantenimiento evolutivo:
- Nuevas funcionalidades
- Adaptación a cambios normativos
- Mejoras de UX

#### Soporte:
- Formación de usuarios
- Consultas técnicas
- Actualizaciones legales

**Coste estimado:** 500-1.000€/mes

---

## ✅ CONCLUSIÓN Y RECOMENDACIÓN

### 🎯 Objetivo mínimo (MVP Legal - 5 meses):
**Inversión:** 20.000€  
**Resultado:** ERP legal y seguro para usar en España

### 🎯 Objetivo recomendado (ERP Funcional - 10 meses):
**Inversión:** 40.000€  
**Resultado:** ERP profesional completo y competitivo

### 🎯 Objetivo óptimo (ERP Avanzado - 15 meses):
**Inversión:** 60.000€  
**Resultado:** ERP de nivel enterprise con todos los módulos

---

### 📊 ROI esperado:

Para una panadería con 300.000-500.000€ de facturación anual:

- **Ahorro en software comercial:** 3.000-5.000€/año (Sage, Holded, etc.)
- **Ahorro en gestoría:** 2.000-3.000€/año (automatización)
- **Reducción de errores:** 5-10% de mejora en margen
- **Mejor toma de decisiones:** Incremento ventas estimado 5-15%

**Retorno de inversión:** 2-3 años

---

### 🚀 PRÓXIMO PASO INMEDIATO:

**Comenzar FASE 1 (Legalización)**  
Duración: 2 meses  
Coste: 8.000€  

**Primera tarea:** Completar VeriFactu con firma digital (Semanas 1-2)

---

*Plan de acción generado el 26 de diciembre de 2025*  
*Sujeto a cambios según evolución normativa*

