# Hoja de ruta para completar ERP Tahona

## Objetivo

Llevar ERP Tahona desde el estado actual validado hasta una versión operable, segura y mantenible en producción, con criterios verificables de finalización.

La aplicación se considerará completada cuando se cumpla la definición de terminado indicada al final de este documento. “Completada” no significa que nunca vuelva a evolucionar; significa que no conserva riesgos críticos conocidos ni flujos empresariales esenciales incompletos.

## Reglas de ejecución

- Cada fase debe terminar con `.\mvnw.cmd clean verify`.
- Ninguna fase debe reducir la cobertura por debajo de los gates activos.
- Todo cambio de esquema debe incluir migración Flyway y prueba MySQL.
- Toda operación de negocio nueva debe pasar por un servicio, no por reflexión o `EntityManager` genérico.
- Seguridad, auditoría y concurrencia se prueban con casos positivos y negativos.
- Los documentos fechados son históricos; `estado-actual.md` se actualiza al cerrar cada fase.

## Fase 0 — Línea base y CI reproducible

**Prioridad:** inmediata  
**Objetivo:** hacer que un build verde signifique que producción puede migrar y arrancar.

### Trabajo

- Separar pruebas unitarias e integración.
- Crear un perfil Maven o job obligatorio para MySQL/Flyway.
- Fallar explícitamente si el job de migraciones no dispone de Docker o `MIGRATION_TEST_JDBC_URL`.
- Probar migración desde base vacía y desde cada versión soportada.
- Unificar `application.properties` y `application-test.properties` de pruebas.
- Eliminar el reempaquetado Spring Boot duplicado.
- Alinear `check-production-readiness.ps1` con las variables reales.
- Añadir un endpoint de salud limitado o healthcheck verificable.

### Criterios de salida

- CI ejecuta unitarias, integración MySQL, migraciones y empaquetado.
- Ninguna prueba MySQL aparece silenciosamente como no ejecutada.
- Build limpio reproducible en una máquina nueva.
- Scripts de producción validan exactamente las mismas variables que la aplicación.

## Fase 1 — Autorización y seguridad

**Prioridad:** crítica  
**Objetivo:** garantizar mínimo privilegio en UI, API y servicios.

### Trabajo

- Activar seguridad de método.
- Crear un componente único para evaluar `usuario + módulo + acción`.
- Aplicar permisos JSON a servicios y operaciones mutables.
- Mantener reglas de URL como primera barrera, no como única defensa.
- Definir matriz oficial para ADMIN, GESTOR, VENDEDOR, CONTABLE y USUARIO.
- Añadir pruebas negativas para lectura, creación, edición, borrado, exportación y envío.
- Retirar `unsafe-eval` y reducir scripts inline mediante archivos o nonces.
- Revisar cookies de sesión, HTTPS, SameSite y cabeceras detrás del proxy.
- Limpiar borradores sensibles al cerrar sesión.

### Criterios de salida

- Un usuario sin permiso no puede ejecutar la acción mediante URL directa ni API.
- Los menús reflejan los permisos efectivos.
- Existe una prueba de seguridad por módulo crítico.
- CSP no requiere `unsafe-eval`.

## Fase 2 — Dominio y eliminación del CRUD inseguro

**Prioridad:** crítica  
**Objetivo:** que todas las invariantes se apliquen de forma consistente.

### Trabajo

- Clasificar entidades en catálogos simples y agregados de negocio.
- Retirar facturas, albaranes, pedidos, producción, stock, usuarios, fiscalidad y auditoría del CRUD genérico.
- Crear DTOs explícitos por caso de uso.
- Centralizar cálculo monetario, redondeo, IVA y descuentos.
- Prohibir eliminación física de documentos emitidos, auditoría y evidencias.
- Obtener siempre el usuario actual para auditoría.
- Reducir capturas genéricas y mensajes internos expuestos.

### Criterios de salida

- Ningún agregado crítico se persiste mediante reflexión.
- Todos los cambios relevantes quedan auditados con el actor real.
- Los cálculos monetarios tienen una única implementación probada.
- Los estados inválidos se rechazan tanto desde UI como desde API.

## Fase 3 — Inventario por almacén y concurrencia

**Prioridad:** crítica  
**Objetivo:** convertir el inventario en una fuente de verdad fiable.

### Trabajo

- Crear `ArticuloAlmacen`.
- Añadir restricción única por artículo y almacén.
- Migrar el stock global existente a un almacén inicial controlado.
- Registrar entradas, salidas, reservas, ajustes y traspasos.
- Definir cuándo se reserva stock y cuándo se descuenta.
- Reconciliar edición, cancelación, devolución y recepción.
- Proteger concurrencia con versión, bloqueo o actualización atómica.
- Añadir inventario físico y diferencias de conteo.

### Criterios de salida

- La suma por almacén coincide con el total reportado.
- No existe stock negativo salvo ajuste autorizado y auditado.
- Dos operaciones simultáneas no pierden movimientos.
- Albarán, recepción, producción, merma y devolución tienen pruebas de ciclo completo.

## Fase 4 — Facturación, contabilidad y VeriFactu

**Prioridad:** alta  
**Objetivo:** cerrar el ciclo fiscal y contable con trazabilidad.

### Trabajo

- Probar numeración concurrente de todos los documentos.
- Consolidar estados de factura y rectificativas.
- Verificar asientos automáticos de venta, compra, cobro y pago.
- Implementar cola de envío VeriFactu con reintentos e idempotencia.
- Implementar tratamiento documentado de contingencia.
- Validar XML, firma, QR y respuestas contra el entorno oficial.
- Revisar generación del Modelo 347 con casos reales.
- Definir conservación y exportación legal de registros.

### Criterios de salida

- No se puede modificar destructivamente una factura emitida.
- Numeración única bajo concurrencia.
- Cada factura emitida tiene evidencia o estado de envío recuperable.
- Conciliación entre factura y asiento comprobada.
- Validación fiscal revisada por profesional competente.

## Fase 5 — Compras, producción y trazabilidad

**Prioridad:** alta  
**Objetivo:** cerrar la operativa diaria de la tahona.

### Trabajo

- Completar pedido de compra → recepción → factura → pago.
- Calcular necesidades de producción desde pedidos y stock.
- Añadir pedidos recurrentes o plantillas.
- Calcular coste real de receta y producto.
- Asociar lotes consumidos y producidos en cada orden.
- Registrar mermas categorizadas y valoradas.
- Completar alertas de caducidad, stock y órdenes pendientes.
- Validar trazabilidad hacia adelante y atrás.

### Criterios de salida

- Una recepción confirmada actualiza stock y pedido sin duplicidad.
- Una orden finalizada consume insumos y produce lote terminado.
- El coste del producto puede explicarse desde sus ingredientes.
- Puede localizarse qué clientes recibieron un lote concreto.

## Fase 6 — Ventas, reparto y experiencia operativa

**Prioridad:** alta  
**Objetivo:** reducir trabajo manual y errores de operación.

### Trabajo

- Consolidar formularios de presupuesto, pedido, albarán y factura.
- Implementar TPV o venta rápida.
- Asignar albaranes a rutas por fecha y cliente.
- Añadir estado logístico preparado → en ruta → entregado.
- Mejorar interfaz móvil del repartidor.
- Registrar firma, foto o evidencia de entrega si el negocio lo exige.
- Revisar calendario y planificador con usuarios reales.
- Añadir accesibilidad y pruebas responsive.

### Criterios de salida

- Un pedido completo se crea sin navegar por varias pantallas inconexas.
- La hoja de ruta contiene todos los albaranes previstos y evita duplicados.
- El repartidor puede operar desde móvil y registrar incidencias.
- Los principales flujos superan una prueba de aceptación con usuarios.

## Fase 7 — Integraciones, importaciones y reporting

**Prioridad:** media  
**Objetivo:** robustecer entradas externas y decisiones de negocio.

### Trabajo

- Sustituir parsers CSV manuales por un parser correcto.
- Validar tamaño, MIME, columnas, filas y codificación.
- Usar `BigDecimal` directamente para importes.
- Completar conciliación bancaria.
- Añadir límites multipart por entorno.
- Consolidar informes de ventas, margen, inventario y producción.
- Definir exportaciones estables y versionadas.

### Criterios de salida

- Una fila inválida no corrompe ni duplica el lote importado.
- Los importes con coma decimal y campos entrecomillados funcionan.
- Los informes se reconcilian con documentos y movimientos.
- Las importaciones grandes tienen límites y mensajes claros.

## Fase 8 — Operación, backup y despliegue

**Prioridad:** crítica para producción  
**Objetivo:** desplegar, observar y recuperar la aplicación con seguridad.

### Trabajo

- Separar Docker local y Docker de producción.
- Añadir secretos, TLS, volúmenes, healthchecks y usuario no root.
- No publicar MySQL externamente salvo necesidad.
- Confinar rutas de backup al directorio configurado.
- Probar backup y restauración sobre una base independiente.
- Añadir métricas, salud, logs estructurados y alertas.
- Definir rotación, retención y anonimización de logs.
- Documentar actualización y reversión.
- Ejecutar prueba de carga y concurrencia.

### Criterios de salida

- Despliegue productivo arranca desde cero con configuración documentada.
- Restauración probada y medida.
- Existe rollback ensayado.
- Healthcheck detecta pérdida de base de datos y fallo de aplicación.
- No hay secretos en imágenes, repositorio ni logs.

## Fase 9 — Calidad y cierre

**Prioridad:** continua  
**Objetivo:** demostrar que los flujos esenciales permanecen estables.

### Trabajo

- Elevar gradualmente cobertura de líneas de 50 % a 60 %.
- Elevar ramas de 20 % a 35 %.
- Priorizar servicios fiscales, stock, backups, seguridad y producción.
- Añadir pruebas E2E de los flujos críticos.
- Ejecutar análisis de dependencias y vulnerabilidades en CI.
- Ejecutar revisión de seguridad y accesibilidad.
- Eliminar recursos frontend no usados y deuda documentada.
- Actualizar manuales de usuario, operación, recuperación y soporte.

### Criterios de salida

- Gates de calidad pasan en CI.
- E2E cubre venta, compra, producción, reparto, cierre y recuperación.
- No hay hallazgos críticos ni altos abiertos.
- La documentación coincide con el comportamiento desplegado.

## Orden recomendado

```text
Fase 0
  ├─ Fase 1
  ├─ Fase 2
  │    └─ Fase 3
  └─ Fase 8

Fase 3 ──> Fase 5 ──> Fase 6
Fase 2 ──> Fase 4
Fase 2 ──> Fase 7

Todas ──> Fase 9
```

## Definición de terminado

ERP Tahona se considera completado para producción cuando:

- No existen riesgos críticos o altos abiertos.
- Autorización efectiva aplicada en servicios y rutas.
- Stock fiable por almacén y protegido ante concurrencia.
- Migraciones MySQL obligatorias y verdes en CI.
- Despliegue productivo reproducible con secretos y TLS.
- Backup y restauración probados.
- Flujos de venta, compra, producción, reparto y contabilidad aceptados.
- VeriFactu validado en el entorno oficial aplicable, con revisión profesional.
- Cobertura mínima de 60 % de líneas y 35 % de ramas.
- E2E de flujos críticos verde.
- Pentest y prueba de carga sin bloqueantes.
- Manual de usuario, operación, seguridad, backup y despliegue actualizados.

## Seguimiento

Al cerrar una tarea:

1. Añadir o actualizar pruebas.
2. Ejecutar `.\mvnw.cmd clean verify`.
3. Ejecutar las pruebas MySQL obligatorias.
4. Actualizar `estado-actual.md`.
5. Marcar el criterio de salida correspondiente.
6. Registrar riesgos nuevos antes de iniciar la fase siguiente.

