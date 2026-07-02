# Revisión: qué falta para ser un ERP completo y funcional en España

Fecha: 2026-07-02 · Rama: refactor-general (ba58496) · Alcance: revisión de código (servicios, entidades, controladores, migraciones, plantillas)

## Resumen ejecutivo

La aplicación cubre con solvencia el núcleo operativo de una panadería (ventas, compras, stock, producción, trazabilidad, reparto, APPCC) y tiene una base fiscal seria (VeriFactu con huella/QR/XML oficial, Modelo 347 con fichero BOE, tipos de factura F1/F2/rectificativas, recargo de equivalencia, RGPD, auditoría). Los huecos están en: **puesta en producción real de VeriFactu (plazo legal ya vencido)**, **IVA (libros registro y Modelo 303)**, **cartera de cobros/pagos y SEPA**, **valoración de existencias**, y **venta mostrador (TPV)**.

## A. Cumplimiento legal — bloqueante

### A1. VeriFactu en producción (URGENTE: plazo vencido el 2026-07-01)
El propio `FiscalComplianceService` define `LIMITE_RESTO_OBLIGADOS = 2026-07-01` (RD 1007/2023 + RD 254/2025): desde ayer todos los obligados deben operar con un SIF conforme. El código está preparado (registro de facturación, huella encadenada, QR tributario, XML oficial, registro de anulación, cliente SOAP AEAT, declaración responsable), pero falta:
- Certificado digital real (`VERIFACTU_CERT_PATH` vacío en el despliegue actual → firma deshabilitada).
- Validación end-to-end contra el entorno de preproducción AEAT (endpoint `prewww2` ya configurado por defecto).
- Activar `VERIFACTU_AEAT_ENABLED=true` tras validar.

### A2. Libros registro de IVA y Modelo 303/390 — no existe nada
No hay libro registro de facturas emitidas/recibidas exportable (formato AEAT: CSV/XLSX normalizado) ni borrador del Modelo 303 (trimestral) / 390 (resumen anual). Aunque la presentación la haga la gestoría, un ERP español completo genera como mínimo los libros registro; los datos ya están en `facturas` y `facturas_compra` con desglose de IVA por línea.

### A3. Modelos 111/115 (retenciones)
Las facturas de compra soportan retención IRPF, pero no hay informe/borrador trimestral del 111 (profesionales) ni 115 (alquileres). Menor, pero es el complemento natural de A2.

### A4. Factura electrónica B2G/B2B
- **B2G (ya obligatorio)**: si factura a administraciones públicas necesita Facturae 3.2 + integración FACe. No existe.
- **B2B (Ley Crea y Crece)**: reglamento pendiente de despliegue definitivo, pero conviene planificar la generación Facturae/UBL porque afectará a todas las empresas.

### A5. TicketBAI
Solo si opera en País Vasco (no aplica en Alicante). Documentar como fuera de alcance.

## B. Funcionalidad núcleo de ERP que falta

### B1. Cartera de cobros y pagos
`Factura.pagada` es un booleano con `fecha_pago`: no hay pagos parciales, vencimientos múltiples (30/60/90), estado de cartera por cliente/proveedor ni previsión de tesorería. Es probablemente el hueco funcional más visible en el día a día.

### B2. Remesas SEPA y mandatos
Sin generación de remesas de adeudos (Norma 19.14) ni transferencias (Norma 34.14), ni gestión de mandatos SEPA de clientes. Para una panadería con clientes de reparto recurrentes (bares, restaurantes) el cobro por remesa es lo habitual.

### B3. Extracto bancario Norma 43
La conciliación existe (movimientos banco, candidatos, aprobación) y hay importación CSV genérica, pero falta el formato estándar español AEB Norma 43 que exportan todos los bancos.

### B4. Valoración de existencias
El stock es solo de cantidades (`articulo_almacen`, `movimientos_stock`). No hay coste medio ponderado ni FIFO, luego no hay inventario valorado para el cierre contable ni margen real por artículo.

### B5. Contabilidad: completar el ciclo
Existe: asientos automáticos (venta/compra/pago), libro diario, balance de sumas y saldos, cierre de ejercicio, comprobación de integridad. Falta:
- Asiento de apertura automático del ejercicio siguiente (el flag `asiento_apertura` existe en la entidad pero no se genera).
- Libro mayor por cuenta.
- Balance de situación y PyG en formato PGC pymes.
- Export para legalización de libros en Registro Mercantil.

### B6. TPV / venta mostrador
Para una tahona es venta principal. Hay facturas simplificadas (con límite 3.000 €) y arqueo de caja (`cajas`, `caja_movimientos`), pero no hay interfaz de venta rápida (pantalla táctil, tickets, cobro inmediato) ni impresión de ticket con QR VeriFactu. Las facturas simplificadas también entran en VeriFactu.

### B7. Comunicaciones
`EmailConfig` existe pero nadie lo usa: no se envían facturas/albaranes por email a clientes y el flujo de recuperación de contraseña (columna `token_recuperacion`) no está implementado.

## C. Deseable / fuera de alcance habitual

- **Nóminas/RRHH**: normalmente delegado a gestoría; no se considera hueco.
- **SII**: solo obligados >6M€ facturación; no aplica.
- **Multiempresa**: `EmpresaConfig` es single-tenant; suficiente para el caso de uso.
- **Portal de cliente** (consulta de facturas/pedidos), **app de repartidor** (hay API móvil parcial: `RepartidorMovilController`).

## Priorización sugerida

| # | Ítem | Motivo |
|---|------|--------|
| 1 | A1 VeriFactu producción | Obligación legal ya en vigor |
| 2 | B1 Cartera cobros/pagos | Operativa diaria |
| 3 | A2 Libros IVA + borrador 303 | Trimestre en curso (T3 presenta en octubre) |
| 4 | B6 TPV mostrador | Venta principal del negocio |
| 5 | B2+B3 SEPA + Norma 43 | Cobro a clientes de reparto |
| 6 | B4 Valoración existencias | Cierre contable fiable |
| 7 | B5 Contabilidad (apertura, mayor, PGC) | Ciclo contable completo |
| 8 | B7 Email | Calidad de servicio |
| 9 | A4 Facturae/FACe | Solo si factura a AAPP; B2B cuando haya reglamento |
