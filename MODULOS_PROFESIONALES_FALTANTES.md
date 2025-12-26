# 🏢 MÓDULOS PROFESIONALES FALTANTES - ERP Panadería Tahona

**Fecha de análisis:** 26 de diciembre de 2025  
**Objetivo:** Convertir el ERP en un sistema profesional completo de nivel empresarial

---

## 📊 ANÁLISIS DE MÓDULOS CORE DE UN ERP PROFESIONAL

### Comparativa: ERP Actual vs ERP Profesional

| Módulo | Estado Actual | Necesidad | Prioridad |
|--------|--------------|-----------|-----------|
| **CRM** | ❌ No existe | Crítica | 🔴 ALTA |
| **Recursos Humanos** | ❌ No existe | Media | 🟡 MEDIA |
| **BI / Analytics** | ❌ No existe | Alta | 🟠 ALTA |
| **Multiempresa** | ❌ No existe | Media | 🟡 MEDIA |
| **Multi-almacén** | ⚠️ Básico | Alta | 🟠 ALTA |
| **Compras avanzadas** | ❌ No existe | Alta | 🟠 ALTA |
| **Presupuestos** | ❌ No existe | Alta | 🟠 ALTA |
| **Proyectos** | ❌ No existe | Baja | 🟢 BAJA |
| **TPV** | ❌ No existe | Alta | 🟠 ALTA |
| **E-commerce** | ❌ No existe | Media | 🟡 MEDIA |
| **Calidad** | ❌ No existe | Media | 🟡 MEDIA |
| **Mantenimiento** | ❌ No existe | Baja | 🟢 BAJA |

---

## 🎯 MÓDULO 1: CRM (Customer Relationship Management)
**Prioridad:** 🔴 CRÍTICA  
**Estado:** ❌ NO IMPLEMENTADO

### ¿Por qué es crítico?
Sin CRM, no hay gestión de relaciones con clientes, seguimiento comercial ni análisis de ventas.

### Funcionalidades requeridas:

#### 1.1. Gestión de Contactos
- [ ] **Ficha completa de cliente**
  - Múltiples direcciones (facturación, envío, etc.)
  - Múltiples contactos (responsable compras, financiero, etc.)
  - Teléfonos, emails, web, redes sociales
  - Notas y comentarios
  - Historial de interacciones
  
- [ ] **Segmentación de clientes**
  - Por volumen de compras
  - Por zona geográfica
  - Por tipo (hostelería, minorista, mayorista)
  - Por rentabilidad
  - Clientes VIP
  - Clientes inactivos

- [ ] **Clasificación ABC**
  - A: Clientes estrella (80% facturación)
  - B: Clientes medios (15% facturación)
  - C: Clientes pequeños (5% facturación)

#### 1.2. Gestión Comercial
- [ ] **Leads (Oportunidades)**
  - Registro de leads
  - Origen del lead (web, teléfono, feria, etc.)
  - Estado (nuevo, contactado, cualificado, perdido, ganado)
  - Probabilidad de cierre
  - Valor estimado
  - Fecha estimada de cierre
  - Seguimiento de acciones

- [ ] **Pipeline de Ventas**
  - Embudo de ventas visual
  - Fases personalizables
  - Arrastrar y soltar entre fases
  - KPIs por fase
  - Tasa de conversión

- [ ] **Tareas y Actividades**
  - Llamadas programadas
  - Reuniones
  - Emails
  - Visitas comerciales
  - Seguimientos
  - Recordatorios automáticos

#### 1.3. Análisis de Cliente
- [ ] **Informes de cliente**
  - Histórico de compras
  - Productos más comprados
  - Frecuencia de compra
  - Ticket medio
  - Tendencias de consumo
  - Estacionalidad
  - Margen generado

- [ ] **RFM Analysis**
  - Recency (última compra)
  - Frequency (frecuencia de compra)
  - Monetary (valor monetario)
  - Segmentación automática

- [ ] **Customer Lifetime Value (CLV)**
  - Valor del cliente en el tiempo
  - Predicción de valor futuro
  - Coste de adquisición vs valor

#### 1.4. Marketing
- [ ] **Campañas de marketing**
  - Creación de campañas
  - Segmentación de destinatarios
  - Plantillas de email
  - Envío masivo de emails
  - Seguimiento de aperturas y clicks
  - ROI de campañas

- [ ] **Programas de fidelización**
  - Puntos de fidelidad
  - Descuentos por volumen
  - Ofertas personalizadas
  - Cumpleaños y aniversarios

#### 1.5. Atención al Cliente
- [ ] **Sistema de tickets**
  - Incidencias de clientes
  - Reclamaciones
  - Consultas
  - Estados (abierto, en proceso, cerrado)
  - Asignación a responsables
  - SLA (tiempo de respuesta)

- [ ] **Base de conocimiento**
  - Preguntas frecuentes
  - Manuales
  - Tutoriales
  - Respuestas predefinidas

### Estructura de datos necesaria:
```sql
-- Contactos adicionales de cliente
CREATE TABLE cliente_contacto (
    id BIGINT PRIMARY KEY,
    cliente_id BIGINT,
    nombre VARCHAR(100),
    cargo VARCHAR(100),
    email VARCHAR(100),
    telefono VARCHAR(20),
    tipo VARCHAR(50), -- COMPRAS, FINANCIERO, GERENCIA, etc.
    principal BOOLEAN
);

-- Direcciones adicionales
CREATE TABLE cliente_direccion (
    id BIGINT PRIMARY KEY,
    cliente_id BIGINT,
    tipo VARCHAR(20), -- FACTURACION, ENVIO, POSTAL
    direccion VARCHAR(200),
    ciudad VARCHAR(100),
    provincia VARCHAR(50),
    codigo_postal VARCHAR(10),
    pais VARCHAR(50),
    principal BOOLEAN
);

-- Leads / Oportunidades
CREATE TABLE lead (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(200),
    empresa VARCHAR(200),
    email VARCHAR(100),
    telefono VARCHAR(20),
    origen VARCHAR(50), -- WEB, TELEFONO, FERIA, REFERIDO
    estado VARCHAR(50), -- NUEVO, CONTACTADO, CUALIFICADO, GANADO, PERDIDO
    probabilidad INT, -- 0-100
    valor_estimado DECIMAL(10,2),
    fecha_estimada_cierre DATE,
    usuario_asignado_id BIGINT,
    cliente_id BIGINT, -- Si se convierte en cliente
    fecha_creacion TIMESTAMP,
    fecha_conversion TIMESTAMP,
    motivo_perdida VARCHAR(500),
    notas TEXT
);

-- Tareas comerciales
CREATE TABLE tarea (
    id BIGINT PRIMARY KEY,
    tipo VARCHAR(50), -- LLAMADA, REUNION, EMAIL, VISITA
    asunto VARCHAR(200),
    descripcion TEXT,
    fecha_programada TIMESTAMP,
    fecha_realizacion TIMESTAMP,
    estado VARCHAR(20), -- PENDIENTE, COMPLETADA, CANCELADA
    prioridad VARCHAR(20), -- ALTA, MEDIA, BAJA
    cliente_id BIGINT,
    lead_id BIGINT,
    usuario_responsable_id BIGINT,
    resultado TEXT
);

-- Campañas de marketing
CREATE TABLE campana (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(200),
    tipo VARCHAR(50), -- EMAIL, SMS, CORREO_POSTAL
    fecha_inicio DATE,
    fecha_fin DATE,
    presupuesto DECIMAL(10,2),
    objetivo VARCHAR(500),
    estado VARCHAR(20), -- PLANIFICADA, ACTIVA, FINALIZADA
    total_enviados INT,
    total_abiertos INT,
    total_clicks INT,
    total_conversiones INT,
    roi DECIMAL(10,2)
);

-- Tickets de soporte
CREATE TABLE ticket (
    id BIGINT PRIMARY KEY,
    numero VARCHAR(20),
    cliente_id BIGINT,
    tipo VARCHAR(50), -- INCIDENCIA, CONSULTA, RECLAMACION
    prioridad VARCHAR(20), -- ALTA, MEDIA, BAJA
    estado VARCHAR(20), -- ABIERTO, EN_PROCESO, CERRADO
    asunto VARCHAR(200),
    descripcion TEXT,
    fecha_apertura TIMESTAMP,
    fecha_cierre TIMESTAMP,
    usuario_asignado_id BIGINT,
    tiempo_respuesta_minutos INT,
    sla_cumplido BOOLEAN,
    valoracion_cliente INT, -- 1-5
    solucion TEXT
);
```

---

## 💼 MÓDULO 2: RECURSOS HUMANOS (RRHH)
**Prioridad:** 🟡 MEDIA  
**Estado:** ❌ NO IMPLEMENTADO

### Funcionalidades básicas:

#### 2.1. Gestión de Empleados
- [ ] **Ficha de empleado**
  - Datos personales
  - Datos laborales (categoría, puesto, departamento)
  - Datos salariales
  - Datos bancarios
  - Contrato laboral
  - Fecha de alta/baja
  - Historial laboral

- [ ] **Organigrama**
  - Estructura jerárquica
  - Departamentos
  - Responsables
  - Visualización gráfica

#### 2.2. Control de Presencia
- [ ] **Fichaje de entrada/salida**
  - Registro de jornada
  - Horas trabajadas
  - Horas extras
  - Retrasos
  - Ausencias

- [ ] **Gestión de turnos**
  - Planificación de turnos
  - Rotación de turnos
  - Turnos de noche
  - Festivos trabajados

#### 2.3. Gestión de Vacaciones
- [ ] **Solicitud de vacaciones**
  - Calendario de vacaciones
  - Aprobación/Rechazo
  - Días disponibles
  - Historial de vacaciones

- [ ] **Permisos y ausencias**
  - Bajas médicas
  - Permisos personales
  - Permisos retribuidos
  - Excedencias

#### 2.4. Formación
- [ ] **Plan de formación**
  - Cursos realizados
  - Cursos obligatorios
  - Certificaciones
  - Vencimientos
  - Coste de formación

#### 2.5. Nóminas (Básico)
- [ ] **Generación de nóminas**
  - Datos salariales
  - Complementos
  - Deducciones
  - Cotizaciones SS
  - Retenciones IRPF
  - Exportación a contabilidad

### Estructura de datos:
```sql
CREATE TABLE empleado (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(100),
    apellidos VARCHAR(200),
    dni VARCHAR(20) UNIQUE,
    fecha_nacimiento DATE,
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    -- Datos laborales
    numero_empleado VARCHAR(20) UNIQUE,
    fecha_alta DATE,
    fecha_baja DATE,
    departamento_id BIGINT,
    puesto VARCHAR(100),
    categoria VARCHAR(100),
    -- Datos salariales
    salario_bruto DECIMAL(10,2),
    tipo_contrato VARCHAR(50),
    jornada VARCHAR(50), -- COMPLETA, PARCIAL
    numero_seguridad_social VARCHAR(50),
    -- Bancarios
    iban VARCHAR(34),
    activo BOOLEAN
);

CREATE TABLE fichaje (
    id BIGINT PRIMARY KEY,
    empleado_id BIGINT,
    fecha DATE,
    hora_entrada TIME,
    hora_salida TIME,
    horas_trabajadas DECIMAL(4,2),
    horas_extras DECIMAL(4,2),
    observaciones VARCHAR(500)
);

CREATE TABLE vacacion (
    id BIGINT PRIMARY KEY,
    empleado_id BIGINT,
    fecha_inicio DATE,
    fecha_fin DATE,
    dias_solicitados INT,
    estado VARCHAR(20), -- PENDIENTE, APROBADA, RECHAZADA
    aprobador_id BIGINT,
    fecha_solicitud TIMESTAMP,
    fecha_respuesta TIMESTAMP,
    motivo_rechazo VARCHAR(500)
);
```

---

## 📊 MÓDULO 3: BUSINESS INTELLIGENCE (BI) Y ANALYTICS
**Prioridad:** 🟠 ALTA  
**Estado:** ❌ NO IMPLEMENTADO

### ¿Por qué es importante?
Sin BI, no hay visibilidad del negocio ni toma de decisiones basada en datos.

### Funcionalidades requeridas:

#### 3.1. Dashboard Principal
- [ ] **Indicadores clave (KPIs)**
  - Ventas del día/mes/año
  - Comparativa con periodo anterior
  - Margen bruto
  - Margen neto
  - Ticket medio
  - Número de facturas
  - Nuevos clientes
  - Clientes perdidos
  - Stock valorizado
  - Días de cobertura de stock
  - Caja actual
  - Cuentas por cobrar
  - Cuentas por pagar

- [ ] **Gráficos interactivos**
  - Evolución de ventas (líneas)
  - Ventas por categoría (barras)
  - Top 10 productos (barras)
  - Top 10 clientes (barras)
  - Distribución de ventas (tarta)
  - Heatmap de ventas por día/hora
  - Comparativas año/año

#### 3.2. Informes de Ventas
- [ ] **Ventas por periodo**
  - Diario, semanal, mensual, trimestral, anual
  - Por cliente
  - Por producto
  - Por categoría
  - Por zona geográfica
  - Por comercial

- [ ] **Análisis de rentabilidad**
  - Margen por producto
  - Margen por cliente
  - Margen por categoría
  - Productos más rentables
  - Productos menos rentables
  - Break-even point

#### 3.3. Análisis de Compras
- [ ] **Compras por proveedor**
  - Volumen de compras
  - Frecuencia
  - Plazo de pago medio
  - Descuentos obtenidos
  - Proveedores críticos

- [ ] **Análisis de costes**
  - Evolución del coste de materias primas
  - Comparativa entre proveedores
  - Oportunidades de ahorro

#### 3.4. Análisis de Stock
- [ ] **Rotación de stock**
  - Productos de alta rotación
  - Productos de baja rotación
  - Stock muerto (sin movimiento >90 días)
  - Cobertura de stock (días)
  - Punto de reorden

- [ ] **Valoración de stock**
  - Stock actual valorizado
  - Valor por almacén
  - Evolución del stock

#### 3.5. Análisis de Tesorería
- [ ] **Cash Flow**
  - Flujo de caja diario
  - Proyección de caja
  - Vencimientos próximos
  - Cobros pendientes
  - Pagos pendientes

- [ ] **Análisis de morosidad**
  - Clientes morosos
  - Antigüedad de saldos
  - Ratio de morosidad
  - Provisiones necesarias

#### 3.6. Cuadro de Mando Integral (CMI)
- [ ] **Perspectiva financiera**
  - Rentabilidad
  - Crecimiento
  - Liquidez

- [ ] **Perspectiva de clientes**
  - Satisfacción
  - Retención
  - Adquisición

- [ ] **Perspectiva de procesos**
  - Eficiencia
  - Calidad
  - Productividad

- [ ] **Perspectiva de aprendizaje**
  - Formación
  - Innovación
  - Mejora continua

### Implementación técnica:
```java
// Servicio de Analytics
@Service
public class AnalyticsService {
    
    // KPI: Ventas del mes
    public BigDecimal getVentasMesActual() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();
        return facturaRepository.sumTotalBetweenDates(inicio, fin);
    }
    
    // KPI: Comparativa con mes anterior
    public BigDecimal getPorcentajeCrecimientoMensual() {
        BigDecimal ventasMesActual = getVentasMesActual();
        BigDecimal ventasMesAnterior = getVentasMesAnterior();
        return ((ventasMesActual.subtract(ventasMesAnterior))
                .divide(ventasMesAnterior, 4, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100));
    }
    
    // Análisis ABC de productos
    public List<ProductoAnalisisDTO> getAnalisisABCProductos() {
        List<Producto> productos = productoRepository.findAll();
        // Calcular ventas por producto
        // Ordenar por ventas descendente
        // Clasificar en A, B, C según Pareto (80-15-5)
        // Retornar lista con clasificación
    }
    
    // Análisis RFM de clientes
    public List<ClienteRFMDTO> getAnalisisRFMClientes() {
        // Calcular Recency, Frequency, Monetary
        // Puntuar cada dimensión (1-5)
        // Segmentar clientes según puntuación
    }
}
```

---

## 🏪 MÓDULO 4: TPV (Terminal Punto de Venta)
**Prioridad:** 🟠 ALTA  
**Estado:** ❌ NO IMPLEMENTADO

### ¿Por qué es importante?
Una panadería necesita un TPV rápido para venta directa al público.

### Funcionalidades requeridas:

#### 4.1. Interfaz de Venta Rápida
- [ ] **Pantalla táctil optimizada**
  - Botones grandes de productos
  - Categorías visuales
  - Búsqueda rápida
  - Escáner de códigos de barras
  - Teclado numérico para cantidad

- [ ] **Carrito de compra**
  - Añadir productos
  - Modificar cantidad
  - Eliminar productos
  - Aplicar descuentos
  - Ver total en tiempo real

#### 4.2. Gestión de Cobros
- [ ] **Múltiples formas de pago**
  - Efectivo (con cálculo de cambio)
  - Tarjeta de crédito/débito
  - Bizum
  - Transferencia
  - Vale
  - Pago mixto

- [ ] **Integración con TPV físico**
  - Datáfonos
  - Impresoras de tickets
  - Cajones de efectivo automáticos
  - Lectores de tarjetas

#### 4.3. Tickets de Venta
- [ ] **Impresión de tickets**
  - Ticket simplificado
  - Ticket con IVA
  - Factura simplificada
  - Factura completa
  - Logo de la empresa
  - Código QR
  - Mensaje personalizado

- [ ] **Tickets electrónicos**
  - Envío por email
  - Envío por SMS
  - Código QR para descarga

#### 4.4. Arqueo de Caja
- [ ] **Control de caja**
  - Apertura de caja (fondo inicial)
  - Movimientos de caja
  - Cierre de caja
  - Cuadre de caja (diferencias)
  - Retiradas de efectivo
  - Entradas de efectivo

- [ ] **Informes de caja**
  - Ventas por turno
  - Ventas por empleado
  - Ventas por forma de pago
  - Histórico de arqueos

#### 4.5. Funcionalidades Avanzadas
- [ ] **Tarjeta de fidelización**
  - Acumulación de puntos
  - Canje de puntos
  - Descuentos automáticos

- [ ] **Tickets pendientes**
  - Guardar ticket para completar después
  - Recuperar tickets guardados
  - Tickets de reserva

- [ ] **Devoluciones**
  - Devolución total
  - Devolución parcial
  - Generar abono automático

### Estructura de datos:
```sql
CREATE TABLE venta_tpv (
    id BIGINT PRIMARY KEY,
    numero_ticket VARCHAR(20),
    fecha TIMESTAMP,
    empleado_id BIGINT,
    caja_id BIGINT,
    cliente_id BIGINT, -- Opcional
    subtotal DECIMAL(10,2),
    descuento DECIMAL(10,2),
    total DECIMAL(10,2),
    estado VARCHAR(20), -- COMPLETADA, ANULADA, PENDIENTE
    factura_id BIGINT -- Si se genera factura
);

CREATE TABLE venta_tpv_linea (
    id BIGINT PRIMARY KEY,
    venta_tpv_id BIGINT,
    articulo_id BIGINT,
    cantidad DECIMAL(10,3),
    precio_unitario DECIMAL(10,2),
    descuento DECIMAL(5,2),
    importe DECIMAL(10,2)
);

CREATE TABLE venta_tpv_pago (
    id BIGINT PRIMARY KEY,
    venta_tpv_id BIGINT,
    forma_pago VARCHAR(50),
    importe DECIMAL(10,2),
    referencia VARCHAR(100) -- Nº operación tarjeta, etc.
);

CREATE TABLE caja (
    id BIGINT PRIMARY KEY,
    codigo VARCHAR(20),
    nombre VARCHAR(100),
    ubicacion VARCHAR(100),
    activa BOOLEAN
);

CREATE TABLE arqueo_caja (
    id BIGINT PRIMARY KEY,
    caja_id BIGINT,
    empleado_id BIGINT,
    fecha_apertura TIMESTAMP,
    fecha_cierre TIMESTAMP,
    fondo_inicial DECIMAL(10,2),
    total_efectivo_teorico DECIMAL(10,2),
    total_efectivo_real DECIMAL(10,2),
    diferencia DECIMAL(10,2),
    total_tarjeta DECIMAL(10,2),
    total_otros DECIMAL(10,2),
    observaciones TEXT
);
```

---

## 🛒 MÓDULO 5: COMPRAS AVANZADAS
**Prioridad:** 🟠 ALTA  
**Estado:** ❌ NO IMPLEMENTADO (solo gestión básica de proveedores)

### Funcionalidades requeridas:

#### 5.1. Solicitudes de Compra
- [ ] **Requisiciones internas**
  - Solicitud de material
  - Aprobación por responsable
  - Generación de pedido a proveedor

#### 5.2. Pedidos a Proveedores
- [ ] **Gestión de pedidos de compra**
  - Crear pedido
  - Enviar pedido (email/fax)
  - Seguimiento de pedido
  - Confirmación de proveedor
  - Fecha estimada de recepción
  - Recepción parcial
  - Recepción total
  - Incidencias

#### 5.3. Albaranes de Compra
- [ ] **Registro de albaranes de proveedor**
  - Vincular con pedido
  - Control de cantidad recibida vs pedida
  - Incidencias de calidad
  - Valoración del albarán

#### 5.4. Facturas de Compra
- [ ] **Registro de facturas de proveedor**
  - Vincular con pedido y albarán
  - Control de tres vías (pedido-albarán-factura)
  - Validación de precios
  - Diferencias de precio
  - Contabilización automática

#### 5.5. Análisis de Compras
- [ ] **Comparativa de proveedores**
  - Mejores precios
  - Mejores plazos
  - Mejor calidad
  - Histórico de compras

- [ ] **Punto de reorden automático**
  - Stock mínimo
  - Stock de seguridad
  - Lead time del proveedor
  - Generación automática de pedido

---

## 💰 MÓDULO 6: PRESUPUESTOS Y OFERTAS
**Prioridad:** 🟠 ALTA  
**Estado:** ❌ NO IMPLEMENTADO

### Funcionalidades:

#### 6.1. Creación de Presupuestos
- [ ] **Editor de presupuestos**
  - Datos del cliente
  - Líneas de presupuesto
  - Descuentos
  - Validez del presupuesto
  - Condiciones comerciales
  - Observaciones

- [ ] **Plantillas de presupuesto**
  - Diseños personalizados
  - Logo empresa
  - Texto legal

#### 6.2. Seguimiento
- [ ] **Estados del presupuesto**
  - Pendiente de envío
  - Enviado
  - Aceptado
  - Rechazado
  - Caducado

- [ ] **Conversión**
  - Convertir a pedido
  - Convertir a albarán
  - Convertir a factura

#### 6.3. Análisis
- [ ] **Tasa de conversión**
  - % de presupuestos aceptados
  - Motivos de rechazo
  - Tiempo medio de respuesta

---

## 🏢 MÓDULO 7: MULTIEMPRESA
**Prioridad:** 🟡 MEDIA  
**Estado:** ❌ NO IMPLEMENTADO

### Funcionalidades:

#### 7.1. Gestión de Empresas
- [ ] **Múltiples empresas en la misma BD**
  - Empresa activa
  - Cambio rápido de empresa
  - Datos independientes por empresa
  - Compartición opcional de datos maestros (artículos, proveedores)

#### 7.2. Contabilidad Consolidada
- [ ] **Informes consolidados**
  - Balance consolidado
  - Cuenta de resultados consolidada
  - Eliminaciones interempresa

---

## 🌐 MÓDULO 8: E-COMMERCE / B2B PORTAL
**Prioridad:** 🟡 MEDIA  
**Estado:** ❌ NO IMPLEMENTADO

### Funcionalidades:

#### 8.1. Portal Web para Clientes
- [ ] **Acceso clientes**
  - Login personalizado
  - Consulta de facturas
  - Descarga de facturas
  - Estado de cuenta
  - Historial de pedidos

#### 8.2. Pedidos Online
- [ ] **Catálogo de productos**
  - Productos disponibles
  - Precios personalizados por cliente
  - Descuentos aplicables
  - Carrito de compra
  - Pedido online
  - Confirmación automática

#### 8.3. Integración con Tienda Online
- [ ] **API REST**
  - Sincronización de productos
  - Sincronización de stock
  - Recepción de pedidos
  - Actualización de estados

---

## ✅ MÓDULO 9: GESTIÓN DE CALIDAD
**Prioridad:** 🟡 MEDIA  
**Estado:** ❌ NO IMPLEMENTADO

### Funcionalidades:

#### 9.1. Control de Calidad
- [ ] **Inspecciones**
  - Inspección de entrada (materia prima)
  - Inspección de proceso
  - Inspección final
  - No conformidades
  - Acciones correctivas

#### 9.2. Trazabilidad
- [ ] **Seguimiento de lotes**
  - Lote de fabricación
  - Lotes de materia prima utilizados
  - Clientes a los que se vendió
  - Recall de productos

#### 9.3. Certificaciones
- [ ] **Gestión de certificados**
  - ISO 9001
  - APPCC
  - IFS Food
  - BRC
  - Renovaciones
  - Auditorías

---

## 🔧 MÓDULO 10: MANTENIMIENTO
**Prioridad:** 🟢 BAJA  
**Estado:** ❌ NO IMPLEMENTADO

### Funcionalidades:

#### 10.1. Gestión de Activos
- [ ] **Maquinaria**
  - Registro de máquinas
  - Datos técnicos
  - Manuales
  - Proveedor

#### 10.2. Mantenimiento Preventivo
- [ ] **Plan de mantenimiento**
  - Calendario de revisiones
  - Checklist de inspección
  - Registro de mantenimientos
  - Alertas de mantenimiento pendiente

#### 10.3. Mantenimiento Correctivo
- [ ] **Averías**
  - Registro de averías
  - Tiempo de parada
  - Coste de reparación
  - Repuestos utilizados

---

## 📊 RESUMEN EJECUTIVO

### Módulos CRÍTICOS para un ERP profesional:
1. 🎯 **CRM** - Gestión comercial completa
2. 📊 **BI / Analytics** - Toma de decisiones
3. 🏪 **TPV** - Venta al público (panadería)
4. 🛒 **Compras Avanzadas** - Ciclo de compra completo
5. 💰 **Presupuestos** - Ofertas y seguimiento

### Estimación de desarrollo:

| Módulo | Horas | Coste estimado |
|--------|-------|----------------|
| **CRM** | 160h | 4.000€ |
| **BI/Analytics** | 120h | 3.000€ |
| **TPV** | 200h | 5.000€ |
| **Compras** | 80h | 2.000€ |
| **Presupuestos** | 60h | 1.500€ |
| **RRHH** | 100h | 2.500€ |
| **Multiempresa** | 40h | 1.000€ |
| **E-commerce** | 160h | 4.000€ |
| **Calidad** | 60h | 1.500€ |
| **Mantenimiento** | 40h | 1.000€ |

**TOTAL:** 1.020 horas → **25.500€**

---

## 🎯 ROADMAP RECOMENDADO

### FASE 1: Completar Base Legal (1 mes)
- VeriFactu completo
- RGPD
- Libros registro
- Modelos AEAT

### FASE 2: CRM + BI (2 meses)
- Implementar CRM completo
- Dashboard e informes
- Analytics básico

### FASE 3: TPV (1.5 meses)
- Interfaz de venta
- Cobros
- Arqueo de caja
- Impresión tickets

### FASE 4: Compras + Presupuestos (1.5 meses)
- Ciclo de compra completo
- Presupuestos y ofertas
- Conversión a documentos

### FASE 5: Módulos Avanzados (2 meses)
- RRHH básico
- E-commerce / Portal B2B
- Calidad

### FASE 6: Optimización (1 mes)
- Performance
- Testing
- Documentación

**TIEMPO TOTAL ESTIMADO:** 9-10 meses de desarrollo

---

## ✅ CONCLUSIÓN

Para que el ERP sea **profesional y completo**, necesitas:

### 🔴 Imprescindible (Corto plazo - 3 meses):
1. ✅ Cumplimiento legal completo (VeriFactu, RGPD, etc.)
2. ✅ CRM funcional
3. ✅ BI y Analytics
4. ✅ TPV para panadería

### 🟠 Muy recomendado (Medio plazo - 6 meses):
5. ✅ Compras avanzadas
6. ✅ Presupuestos y ofertas
7. ✅ RRHH básico

### 🟡 Recomendado (Largo plazo - 12 meses):
8. ✅ Multiempresa
9. ✅ E-commerce / Portal B2B
10. ✅ Gestión de Calidad

---

**Con estos módulos, tendrías un ERP de nivel empresarial comparable a SAP Business One, Sage, o Odoo.**

---

*Documento generado el 26 de diciembre de 2025*

