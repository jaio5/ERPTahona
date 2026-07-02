# INFORME DE AUDITORÍA COMPLETA — ERP TAHONA

**Fecha de auditoría:** 15 de junio de 2026  
**Versión auditada:** 0.0.1 (rama desarrollo)  
**Tests:** 76/76 pasan (BUILD SUCCESS)  
**Stack:** Spring Boot 3.5.7 · Java 17 · Thymeleaf + Alpine.js · H2/MySQL · Flyway  
**Archivos Java:** 213 · **Templates:** 58 · **Migraciones:** 14  

---

## 1. INFORME DE AUDITORÍA FUNCIONAL

### 1.1 Arquitectura y estructura
- **Capas bien definidas:** Controller (web + REST), Service, Repository, Entity.
- **Controlador genérico:** `WebEntityController` expone CRUD vía reflexión para ~30 módulos. Riesgo: validaciones de negocio escapan al service dedicado.
- **Frontend híbrido:** Thymeleaf tradicional convive con SPA Alpine.js (`app.html`). Inconsistencia: algunos módulos usan la SPA, otros usan templates Thymeleaf independientes (facturas, clientes, albaranes).
- **Seguridad:** Spring Security con login propio, PBKDF2/BCrypt, roles JSON en BD.
- **Scheduled jobs:** Backups (02:00), notificaciones, VeriFactu hardening (03:00/03:30).

### 1.2 Flujo documental real (ventas)
```
Pedido → Albarán → Factura
```
- **Pedido:** se crea sin líneas desde el formulario web básico. Conversión completa o parcial a albarán implementada.
- **Albarán:** se genera desde pedido o manualmente. Conversión a factura (individual o masiva). Duplicación implementada.
- **Factura:** estados BORRADOR → REVISION → EMITIDA → ANULADA. No hay flujo de "preparado" ni "en reparto".

### 1.3 Hallazgos críticos funcionales
| # | Hallazgo | Severidad |
|---|----------|-----------|
| F1 | **No hay descuento de stock automático** al finalizar orden de producción ni al servir pedido. El inventario es teórico. | Crítica |
| F2 | **No existe servicio `MovimientoStockService`**. La entidad `MovimientoStock` tiene repositorio pero ninguna lógica de negocio que la alimente sistemáticamente. | Crítica |
| F3 | **Recepciones de compra:** el frontend (`app-config.js`) declara módulo `recepciones` y `recepcion-lineas`, pero **no existe entidad `Recepcion` ni `RecepcionLinea`** en Java. Es un frontend huérfano. | Crítica |
| F4 | **No hay tarifas personalizadas por cliente.** Todos los clientes pagan PVP único. No hay lista de precios, descuentos por volumen ni condiciones comerciales. | Alta |
| F5 | **No hay pedidos recurrentes / plantillas.** Un panadero debe poder repetir "todos los días 20 barras al hotel X". No existe. | Alta |
| F6 | **Planificación de producción es manual.** No hay cálculo automático de necesidades diarias a partir de pedidos + stock de seguridad. | Alta |
| F7 | **No hay asignación automática de albaranes a rutas de reparto.** La hoja de ruta se genera con las paradas maestras, pero los albaranes del día no se vinculan automáticamente. | Alta |
| F8 | **Rentabilidad es placeholder.** El template `reportes/rentabilidad.html` muestra un mensaje estático sin datos reales. | Media |
| F9 | **No hay TPV / pantalla de venta rápida para mostrador.** La "Nueva Venta" del dashboard abre modal genérico de albarán. | Media |
| F10 | **Factura rectificativa y abono comparten la misma entidad `Factura`**. No hay entidad `Abono` ni flujo específico de nota de crédito con numeración separada. | Media |
| F11 | **Modelo 347 existe como entidad pero no hay generación automática** desde facturas/clientes. | Media |
| F12 | **Caja diaria (`Caja`) existe pero no hay apertura/cierre automático** ni arqueo con lectura de movimientos del día. | Media |
| F13 | **No hay control de mermas estructurado.** Solo campo `merma` en `OrdenProduccion` y `Horneada`, sin categorización ni valoración. | Media |
| F14 | **GDPR exporta JSON pero no anonimiza** ni gestiona plazos de conservación automáticos. | Baja |

---

## 2. INFORME UX/UI

### 2.1 Estado actual
- **Diseño base moderno:** dark mode, responsive, sidebar colapsable, bottom nav móvil.
- **SPA Alpine.js** (`app.html`) con tablas dinámicas, filtros básicos (estado, fecha), modales de edición.
- **Inconsistencia de navegación:** algunos módulos (clientes, facturas, albaranes) tienen templates Thymeleaf propios con URL `/web/clientes`, mientras que la SPA carga desde `/web/app`.
- **Formularios:** la mayoría son genéricos generados por `SCHEMAS` en `app-config.js`. Ventajas: rápidos de construir. Desventajas: validaciones limitadas, sin lógica de campo condicional.

### 2.2 Problemas de UX detectados
| # | Problema | Impacto |
|---|----------|---------|
| U1 | **Pedido de venta:** formulario solo pide cliente + observaciones. No se pueden añadir líneas de producto en el mismo formulario. El usuario debe crear pedido, guardar, y luego (si existe UI de líneas) añadir líneas. | Crítico |
| U2 | **Albarán:** igual que pedido. El formulario web tradicional no permite líneas. | Crítico |
| U3 | **Factura:** igual. El flujo de "Nueva Venta Rápida" no está implementado como TPV; solo abre formulario genérico. | Crítico |
| U4 | **No hay calendario** para visualizar pedidos, producción o reparto. Una panadería vive en función del día de la semana. | Alto |
| U5 | **No hay vista de ruta con mapa** ni orden optimizado de entregas. El repartidor no tiene interfaz visual de ruta (solo API REST). | Alto |
| U6 | **Búsqueda global** (`/web/buscar`) solo filtra por texto simple. No hay búsqueda por fecha, rango, ni filtros facetados. | Medio |
| U7 | **No hay atajos de teclado** para operaciones frecuentes (Ctrl+N nuevo pedido, Ctrl+F facturar, etc.). | Medio |
| U8 | **Tablas genéricas** no permiten ordenar por columna ni agrupar. | Medio |
| U9 | **Dashboard** muestra métricas estáticas sin gráficos ni tendencias. | Medio |
| U10 | **Modales genéricos** no tienen validación inline; el error llega tras submit con toast genérico. | Medio |
| U11 | **Formulario de cliente** no permite añadir direcciones de envío inline. Hay que ir a otro módulo. | Medio |
| U12 | **No hay modo "kiosko" o pantalla táctil** para el obrador (resistencia al polvo/harina). | Bajo |

---

## 3. INFORME LEGAL

### 3.1 VeriFactu (RD 1007/2023 y HAC/1177/2024)
| Requisito | Estado | Observación |
|-----------|--------|-------------|
| Generación de XML registro alta | ✅ Implementado | `generarRegistroAltaXml` |
| Generación de XML registro anulación | ✅ Implementado | `generarRegistroAnulacionXml` |
| Firma digital SHA256withRSA | ✅ Implementado | vía certificado PKCS12 |
| Hash encadenado por serie | ✅ Implementado | `generarHashEncadenado` |
| Huella registro (campos obligatorios) | ✅ Implementado | `generarHuellaRegistroAlta` |
| Validación XSD interna | ✅ Implementado | `validarXmlRegistroFacturacion` |
| Envío SOAP a AEAT | ✅ Implementado | `VerifactuAeatSoapClient` con WS-Security |
| QR Code VeriFactu | ✅ Implementado | `QrCodeService` |
| Evidencias en BD | ✅ Implementado | `VerifactuEvidence` |
| Reenvío automático ante error | ❌ No implementado | Solo logging manual |
| Modo contingencia (offline certificado) | ❌ No implementado | Requerido si falla conectividad |
| Registro de eventos de integridad | ⚠️ Parcial | `FacturacionEventoService` registra eventos, pero no hay eventos periódicos de integridad de cadena |
| Renuncia / baja de VeriFactu | ✅ Implementado | `EmpresaConfigService` gestiona fechas |
| Numeración única e inalterable | ⚠️ Parcial | `FacturaSerieSequence` con concurrencia, pero no hay bloqueo estricto de facturas emitidas en BD (se controla en servicio) |

**Riesgo legal:** El envío SOAP está implementado pero **no probado contra AEAT real**. La documentación (`README_RUN.md`) advierte explícitamente que "la conformidad legal plena requiere validación oficial de la AEAT".

### 3.2 Facturación española
| Requisito | Estado |
|-----------|--------|
| Campos obligatorios RD 1619/2012 | ✅ (serie, fecha operación, tipo, medio cobro, base, iva, retención) |
| Factura simplificada | ⚠️ Campo tipo existe, flujo no diferenciado |
| Factura rectificativa | ⚠️ Campos en entidad, sin flujo guiado ni numeración R- |
| Conservación 4 años (IVA) / 6 años (mercantil) | ⚠️ BD persistente, pero sin política de archivado ni WORM |

### 3.3 IVA
- Tipos soportados: 21 %, 10 %, 4 %, exento.
- Recargo de equivalencia: campo `totalRecargo` existe.
- Cálculo correcto en líneas (descuento + IVA).

### 3.4 GDPR (LOPD-GDD)
- Consentimientos: `RgpdConsentimiento`.
- Solicitudes de ejercicio de derechos: `RgpdSolicitud` con fecha límite +30 días.
- Auditoría de accesos: `RgpdAccesoDatos`.
- Exportación portabilidad: JSON por cliente.

### 3.5 Seguridad alimentaria (APPCC / Reg 852/2004)
- Controles APPCC: `AppccControl` con temperaturas, límites, acciones correctivas.
- Trazabilidad de lotes: `Lote` + `LoteInsumo`.
- Alérgenos en artículos y recetas.

---

## 4. COBERTURA FUNCIONAL POR MÓDULO

| Módulo | Cobertura | Estado resumido |
|--------|-----------|-----------------|
| **Clientes** | 55 % | Básico. Faltan tarifas, condiciones, contactos, historial integrado. |
| **Productos** | 50 % | Básico. Faltan pesos, formatos, fotos, receta técnica estructurada. |
| **Pedidos** | 40 % | Sin líneas en formulario, sin recurrentes, sin calendario. |
| **Producción** | 55 % | Órdenes y horneadas existen, pero sin planificación automática ni desglose de stock. |
| **Reparto** | 60 % | Rutas, hojas, entregas. Falta asignación de albaranes, optimización, app móvil visual. |
| **Albaranes** | 60 % | Generación y facturación. Falta control de estados logísticos (preparado, en ruta). |
| **Facturación** | 70 % | Ciclo completo con VeriFactu. Falta abono separado, remesas, email. |
| **VeriFactu** | 85 % | Muy avanzado. Falta reenvío automático y modo contingencia. |
| **Inventario** | 40 % | Movimientos y lotes existen, pero sin descuento automático ni valoración. |
| **Compras** | 50 % | Proveedores, pedidos, facturas. Falta recepciones (backend inexistente). |
| **Contabilidad** | 50 % | Plan contable, asientos automáticos. Falta cierre, exportación. |
| **Estadísticas** | 30 % | Placeholder de rentabilidad. Sin gráficos ni análisis real. |
| **Caja/Tesorería** | 45 % | Caja diaria y movimientos. Falta arqueo integrado y conciliación. |
| **GDPR/Auditoría** | 80 % | Completo para un ERP de este tamaño. |

---

## 5. PANTALLAS FALTANTES

1. **Calendario de pedidos y producción** (vista semanal/diaria tipo Gantt o calendario).
2. **Planificación diaria del obrador** (qué recetas hornear, a qué hora, en qué horno).
3. **TPV / Toma de pedidos rápida** (pantalla táctil: cliente → producto → cantidad → confirmar).
4. **Asignación de albaranes a hoja de ruta** (drag & drop o lista de checkboxes).
5. **Recepción de mercancía** (backend y frontend completos).
6. **Inventario físico / conteo cíclico** (lectura de lotes, ajustes masivos).
7. **Rentabilidad real por producto y por cliente** (coste vs venta).
8. **Panel del repartidor** (vista móvil con mapa, lista de entregas, firma digital, fotos).
9. **Gestión de tarifas y descuentos por cliente**.
10. **Gestión de mermas** (registro diario por tipo: quemado, sobrante, caducado, muestra).
11. **Devoluciones a proveedor**.
12. **Conciliación bancaria** (importación de extractos, matching automático).
13. **Configuración de turnos y panaderos**.
14. **Alertas y notificaciones** (stock bajo, lotes a caducar, pedidos sin servir).

---

## 6. FORMULARIOS INCOMPLETOS

| Formulario | Campos expuestos | Campos faltantes / Problemas |
|------------|------------------|------------------------------|
| `clientes/formulario.html` | código, nombre, cif, tel, email, dir, pob, cp | Provincia no se guarda en controller; sin direcciones de envío inline; sin tarifa; sin condiciones de pago; sin comercial asignado. |
| `articulos/formulario.html` | código, nombre, cat, pvp, iva, stock, alérgenos | Sin peso, formato, foto, tiempo elaboración, vida útil, stock mín/máx, punto pedido (existe en entidad, no en form). |
| `pedidos-venta/formulario.html` | cliente, observaciones | **Sin líneas de pedido**. El usuario no puede indicar qué productos quiere. |
| `facturas/formulario.html` | número, fecha, cliente, observaciones | **Sin líneas de factura**. Medio de cobro no aparece en form tradicional (sí en SPA). |
| `albaranes/formulario.html` | fecha, almacén, cliente, observaciones | **Sin líneas de albarán**. |
| `ordenes-produccion/formulario.html` | cantidad, observaciones | Sin receta (existe en entidad, no en form tradicional), sin almacén destino. |
| `recetas/formulario.html` | código, nombre, tiempos, temp, alérgenos | Sin ingredientes inline (van en child schema de SPA). |
| `proveedores/formulario.html` | código, nombre, cif, tel, email | Sin condiciones de compra, días de pago, forma de pago, descuento (existe en entidad, no en form). |
| `empresa/formulario.html` | nombre, cif, dir, cp, ciudad, prov, tel, email, web, registros | Sin logo, sin datos bancarios de la empresa, sin configuración de impresión. |

**Problema transversal:** los formularios Thymeleaf tradicionales no permiten gestionar líneas (pedido, albarán, factura). La SPA sí tiene `docFields` + `lineFields`, pero requiere que el usuario trabaje exclusivamente desde `/web/app`.

---

## 7. CAMPOS FALTANTES (entidades vs operativa real)

### Cliente
- `tipoCliente` (hotel, restaurante, tienda, particular, catering…)
- `diaReparto` (Lunes, Martes…)
- `horaLimitePedido` (hasta qué hora se puede modificar)
- `comercialId` (vendedor asignado)
- `condicionesPago` (contado, 30 días, 60 días)
- `descuentoPorDefecto`
- `tarifaId` (lista de precios asignada)
- `activoReparto` (sí/no para incluir en rutas)

### Artículo
- `pesoUnidad` (gramos)
- `formato` (barra, baguette, redondo, individual…)
- `vidaUtilHoras`
- `fotoUrl`
- `requiereReceta` (sí/no)
- `categoriaPanaderia` (PAN, BOLLERIA, PASTELERIA, ESPECIAL)
- `mermaEstandar` (% esperado)

### Pedido
- `horaEntregaPreferida`
- `rutaId` (ruta asignada)
- `direccionEntregaId`
- `esRecurrente` / `plantillaId`
- `motivoCancelacion`

### Albarán
- `estadoLogistico` (PENDIENTE, PREPARADO, EN_RUTA, ENTREGADO)
- `rutaId`
- `repartidorId`
- `horaEntregaReal`

### OrdenProduccion
- `turno` (mañana, tarde, noche)
- `panaderoId`
- `hornoId`
- `horaInicioPrevista`
- `horaFinPrevista`

---

## 8. ACCIONES FALTANTES

1. **Descuento automático de stock** al servir pedido / finalizar producción.
2. **Generación automática de orden de producción** desde pedidos del día siguiente.
3. **Asignación automática de albaranes a hoja de ruta** según fecha + ruta del cliente.
4. **Cálculo de coste de receta** sumando ingredientes y actualizando `coste` del artículo.
5. **Generación automática Modelo 347** al cerrar ejercicio.
6. **Conciliación bancaria** automática (matching por importe + referencia).
7. **Reenvío VeriFactu** programado para facturas en estado ERROR.
8. **Arqueo de caja** con lectura de movimientos del día y cálculo de diferencia.
9. **Alertas por email/SMS** de lotes próximos a caducar.
10. **Importación de extractos bancarios** (formato CSV/XML).

---

## 9. MEJORAS PRIORITARIAS

### Bloque 1 — Imprescindibles para operar (Semanas 1-4)
| # | Mejora | Justificación |
|---|--------|---------------|
| P1 | **Formularios con líneas integradas** (pedido, albarán, factura) en SPA | Sin esto no se puede tomar un pedido completo. |
| P2 | **Descuento de stock automático** en producción y ventas | Evita sobrepedidos y desabastecimiento. |
| P3 | **Calendario de pedidos** (vista diaria/semanal) | El panadero planifica por días, no por listados. |
| P4 | **Asignación de albaranes a rutas** y estado logístico | Sin esto el reparto es caos. |
| P5 | **Tarifas por cliente** (pvp especial, descuento %) | Los hoteles y restaurantes negocian precios distintos. |

### Bloque 2 — Productividad (Semanas 5-8)
| # | Mejora | Justificación |
|---|--------|---------------|
| P6 | **Planificación de producción automática** (pedidos + stock → órdenes) | Reduce el trabajo manual del maestro panadero. |
| P7 | **TPV / venta rápida** para mostrador y reparto | Agiliza la venta diaria. |
| P8 | **Panel del repartidor** (PWA con mapa, firma, foto) | Control de entregas y pruebas de reparto. |
| P9 | **Gestión de mermas** diaria por tipo | Control de pérdidas obligatorio en panadería. |
| P10 | **Alertas** (stock bajo, lotes a caducar, pedidos pendientes) | Evita sorpresas a las 4 AM. |

### Bloque 3 — Legal y contable (Semanas 9-12)
| # | Mejora | Justificación |
|---|--------|---------------|
| P11 | **Recepciones de mercancía** (backend + frontend) | Cierre del ciclo de compras. |
| P12 | **Modo contingencia VeriFactu** | Obligatorio para operar legalmente si falla la red. |
| P13 | **Factura electrónica** (FaceB2B) y envío por email | Sector HORECA exige cada vez más factura electrónica. |
| P14 | **Rentabilidad real** (coste receta vs pvp) | Toma de decisiones de precios. |

---

## 10. PLAN DE IMPLEMENTACIÓN ORDENADO POR IMPACTO EMPRESARIAL

### Fase 1: Operativa diaria (días 1-30)
1. Unificar pedido/albarán/factura en SPA con líneas integradas (frontend + backend).
2. Implementar descuento de stock en `PedidoService.convertirAAlbaran` y `OrdenProduccionService.finalizarProduccion`.
3. Crear pantalla "Calendario de pedidos" (`/web/app/calendario`) con vista semanal.
4. Añadir `tarifa` y `descuento` a Cliente + lógica de precios en líneas.
5. Vincular albaranes del día a `HojaRuta` automáticamente al generarla.

### Fase 2: Producción y reparto (días 31-60)
6. Planificador diario: algoritmo `calcularNecesidadesProduccion(fecha)` que sume pedidos + stock mínimo − stock actual.
7. Pantalla TPV rápida (`/web/tpv`) para mostrador y reparto.
8. Mejorar panel repartidor: firma digital con canvas, foto de entrega, geolocalización.
9. Gestión de mermas: entidad `Merma` vinculada a `Articulo` y `OrdenProduccion`.
10. Sistema de alertas visuales en dashboard (lotes caducidad, stock bajo).

### Fase 3: Compras y legal (días 61-90)
11. Crear entidades `Recepcion` y `RecepcionLinea` con migración Flyway; exponer en API y SPA.
12. Modo contingencia VeriFactu: almacenar XML pendiente en tabla `verifactu_cola` con reintentos programados.
13. Enviar factura por email con PDF adjunto.
14. Generación automática Modelo 347 desde facturas emitidas.
15. Exportación contable a formato estándar (XML/CSV).

### Fase 4: Optimización (días 91-120)
16. Reporte de rentabilidad real con cálculo de coste de receta.
17. Conciliación bancaria automática.
18. App móvil progresiva (PWA) para repartidor offline-first.
19. Dashboard analítico con gráficos (ventas, mermas, eficiencia).
20. Integración con GPS para optimización de rutas (OpenRouteService).

---

## ANEXO: Inventario de entidades existentes

| Entidad | Tabla | Propósito |
|---------|-------|-----------|
| Usuario | users | Login, roles, seguridad |
| Rol | roles | Permisos JSON |
| Cliente | clientes | Maestro clientes |
| DireccionenvioNew | direccionesenvio_new | Direcciones múltiples |
| Proveedor | proveedores | Maestro proveedores |
| Articulo | articulos | Catálogo productos |
| Receta | recetas | Fórmulas de producción |
| RecetaIngrediente | receta_ingredientes | Ingredientes por receta |
| OrdenProduccion | ordenes_produccion | Órdenes de fabricación |
| Horneada | horneadas | Registro de horneados |
| Lote | lotes | Trazabilidad |
| LoteInsumo | lote_insumos | Vinculación materia prima → producto |
| Pedido | pedidos | Pedidos de venta |
| PedidoLinea | pedido_lineas | Líneas de pedido |
| AlbaranVenta | albaranes_venta | Albaranes de entrega |
| AlbaranVentaLinea | albaran_venta_lineas | Líneas de albarán |
| Factura | facturas | Facturas de venta |
| FacturaLinea | factura_lineas | Líneas de factura |
| Presupuesto | presupuestos | Presupuestos a clientes |
| PresupuestoLinea | presupuestos_lineas | Líneas de presupuesto |
| Devolucion | devoluciones | Devoluciones de clientes |
| DevolucionLinea | devoluciones_lineas | Líneas de devolución |
| PedidoCompra | pedidos_compra | Pedidos a proveedores |
| PedidoCompraLinea | pedidos_compra_lineas | Líneas de compra |
| FacturaCompra | facturas_compra | Facturas de compra |
| FacturaCompraLinea | facturas_compra_lineas | Líneas factura compra |
| Almacen | almacenes | Almacenes |
| MovimientoStock | movimientos_stock | Movimientos de inventario |
| RutaReparto | rutas_reparto | Rutas maestras |
| RutaParada | rutas_paradas | Paradas de ruta |
| HojaRuta | hojas_ruta | Hojas de ruta diarias |
| HojaRutaEntrega | hojas_ruta_entregas | Entregas de una hoja |
| Vehiculo | vehiculos | Vehículos |
| Caja | cajas | Cierres de caja |
| CajaMovimiento | caja_movimientos | Movimientos de caja |
| Banco | bancos | Cuentas bancarias |
| MovimientoBanco | movimientos_banco | Movimientos bancarios |
| PlanContable / PlanCuentas | plan_contable / plan_cuentas | Estructura contable |
| AsientoContable | asientos_contables | Asientos |
| AsientoContableLinea | asientos_contables_lineas | Apuntes |
| Modelo347Registro | modelo347_registros | Declaración 347 |
| AuditoriaAccion | auditoria_acciones | Auditoría |
| RgpdSolicitud / RgpdConsentimiento / RgpdAccesoDatos | rgpd_* | GDPR |
| AppccControl | appcc_controles | Controles APPCC |
| EmpresaConfig | empresa_config | Configuración fiscal y VeriFactu |
| VerifactuEvidence | verifactu_evidence | Evidencias de envío |
| FacturacionEvento | facturacion_eventos | Eventos de hash chain |

---

*Fin del informe de auditoría.*
