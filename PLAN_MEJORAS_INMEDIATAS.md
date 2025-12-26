# 🚀 PLAN DE MEJORAS INMEDIATAS - ERP Panadería Tahona

## 🎯 OBJETIVO
Convertir el ERP actual en una aplicación completamente funcional y lista para producción.

---

## ⚡ TOP 5 MEJORAS URGENTES (2-3 semanas)

### 1. 🔐 SISTEMA DE LOGIN Y USUARIOS (CRÍTICO)
**Tiempo estimado:** 3-4 días  
**Prioridad:** 🔴 CRÍTICA

#### Tareas:
```
✓ Paso 1: Crear entidad Usuario
  - id, username, password (encriptado), email, nombre, rol, activo, fechaCreacion

✓ Paso 2: Crear LoginController y login.fxml
  - Pantalla de login antes del MainPanel
  - Validación de credenciales
  - Botón "Recordarme"

✓ Paso 3: Implementar encriptación de contraseñas
  - BCryptPasswordEncoder o similar
  - No guardar contraseñas en texto plano

✓ Paso 4: Gestión de sesión
  - Almacenar usuario actual en singleton/servicio
  - Cerrar sesión
  - Timeout de inactividad

✓ Paso 5: Crear panel de gestión de usuarios
  - CRUD de usuarios (solo admin)
  - Cambiar contraseña
  - Activar/desactivar usuarios

✓ Paso 6: Roles básicos
  - ADMIN (acceso total)
  - USUARIO (acceso limitado)
  - VENDEDOR (solo ventas)
```

#### Archivos a crear:
```
src/main/java/alicanteweb/erp/entities/Usuario.java
src/main/java/alicanteweb/erp/repository/UsuarioRepository.java
src/main/java/alicanteweb/erp/service/UsuarioService.java
src/main/java/alicanteweb/erp/service/AuthService.java
src/main/java/alicanteweb/erp/controller/ui/LoginController.java
src/main/java/alicanteweb/erp/controller/UsuarioController.java
src/main/resources/ui/login.fxml
src/main/resources/ui/usuarios_panel.fxml
src/main/resources/ui/usuario_form.fxml
```

---

### 2. 📦 MÓDULO DE PEDIDOS (ALTA)
**Tiempo estimado:** 4-5 días  
**Prioridad:** 🟠 ALTA

#### Tareas:
```
✓ Paso 1: Crear PedidosPanelController
  - Listar pedidos pendientes y completados
  - Filtros por estado, fecha, cliente

✓ Paso 2: Crear formulario de pedidos
  - Similar a facturas pero con estado "Pendiente"
  - Añadir líneas de pedido

✓ Paso 3: Convertir pedido a albarán
  - Botón "Convertir a Albarán"
  - Copiar líneas del pedido
  - Cambiar estado del pedido a "Servido"

✓ Paso 4: Convertir pedido a factura
  - Botón "Convertir a Factura"
  - Opción de facturar múltiples pedidos juntos

✓ Paso 5: Estados de pedidos
  - PENDIENTE, EN_PREPARACION, SERVIDO, FACTURADO, CANCELADO
  - Colores en la tabla según estado
```

#### Archivos a crear:
```
src/main/java/alicanteweb/erp/controller/ui/PedidosPanelController.java
src/main/resources/ui/pedidos_panel.fxml
src/main/resources/ui/pedido_form.fxml
```

#### Entidades existentes a usar:
```
entities/Pedido.java (ya existe)
entities/PedidoLinea.java (ya existe)
service/PedidoService.java (ya existe)
```

---

### 3. 📊 DASHBOARD Y REPORTES BÁSICOS (ALTA)
**Tiempo estimado:** 4-5 días  
**Prioridad:** 🟠 ALTA

#### Tareas:
```
✓ Paso 1: Crear DashboardController
  - Resumen de ventas del mes
  - Ventas de hoy
  - Top 5 clientes
  - Top 5 productos
  - Gráfico de ventas últimos 7 días

✓ Paso 2: Panel de reportes
  - Informe de ventas por periodo
  - Informe de ventas por cliente
  - Informe de ventas por artículo
  - Exportar a Excel

✓ Paso 3: Implementar gráficos
  - JavaFX Charts (BarChart, LineChart, PieChart)
  - Ventas mensuales
  - Comparativa años anteriores

✓ Paso 4: Filtros avanzados
  - Rango de fechas
  - Por cliente
  - Por artículo
  - Por categoría
```

#### Archivos a crear:
```
src/main/java/alicanteweb/erp/controller/ui/DashboardController.java
src/main/java/alicanteweb/erp/controller/ui/ReportesController.java
src/main/java/alicanteweb/erp/service/ReporteService.java
src/main/java/alicanteweb/erp/service/ExcelExportService.java
src/main/resources/ui/dashboard.fxml
src/main/resources/ui/reportes_panel.fxml
```

---

### 4. 💰 MÓDULO DE TESORERÍA (ALTA)
**Tiempo estimado:** 3-4 días  
**Prioridad:** 🟠 ALTA

#### Tareas:
```
✓ Paso 1: Crear entidades de tesorería
  - Cobro (id, facturaId, fecha, importe, formaPago, banco, referencia)
  - Pago (similar para facturas de proveedor)
  - FormaPago (efectivo, tarjeta, transferencia, etc.)

✓ Paso 2: Panel de cobros pendientes
  - Listar facturas sin cobrar
  - Registrar cobro total o parcial
  - Vencimientos próximos en rojo

✓ Paso 3: Panel de pagos pendientes
  - Listar facturas de proveedor sin pagar
  - Registrar pago

✓ Paso 4: Arqueo de caja
  - Resumen de cobros en efectivo del día
  - Cuadre de caja

✓ Paso 5: Alertas de vencimientos
  - Notificación de cobros vencidos
  - Notificación de pagos próximos (3 días antes)
```

#### Archivos a crear:
```
src/main/java/alicanteweb/erp/entities/Cobro.java
src/main/java/alicanteweb/erp/entities/Pago.java
src/main/java/alicanteweb/erp/entities/FormaPago.java
src/main/java/alicanteweb/erp/repository/CobroRepository.java
src/main/java/alicanteweb/erp/repository/PagoRepository.java
src/main/java/alicanteweb/erp/service/TesoreriaService.java
src/main/java/alicanteweb/erp/controller/ui/TesoreriaPanelController.java
src/main/resources/ui/tesoreria_panel.fxml
src/main/resources/ui/cobro_form.fxml
```

---

### 5. ⚙️ PANEL DE CONFIGURACIÓN (MEDIA)
**Tiempo estimado:** 2-3 días  
**Prioridad:** 🟡 MEDIA

#### Tareas:
```
✓ Paso 1: Crear entidad ConfiguracionEmpresa
  - Nombre empresa, CIF, dirección, teléfono, email
  - Logo de empresa
  - Configuración de IVA por defecto

✓ Paso 2: Crear entidad Serie (numeración)
  - Serie para facturas (F-, A-, etc.)
  - Próximo número
  - Formato de numeración

✓ Paso 3: Panel de configuración
  - Tab "Empresa"
  - Tab "Numeración"
  - Tab "Impuestos"
  - Tab "Email" (configuración SMTP)

✓ Paso 4: Guardar configuración
  - Singleton para acceder desde toda la app
  - Logo se usa en las impresiones
```

#### Archivos a crear:
```
src/main/java/alicanteweb/erp/entities/ConfiguracionEmpresa.java
src/main/java/alicanteweb/erp/entities/Serie.java
src/main/java/alicanteweb/erp/service/ConfiguracionService.java
src/main/java/alicanteweb/erp/controller/ui/ConfiguracionController.java
src/main/resources/ui/configuracion_panel.fxml
```

---

## 🔧 MEJORAS TÉCNICAS URGENTES

### 1. Añadir validaciones en formularios
```java
// Ejemplo: validar que el CIF tiene formato correcto
if (!cif.matches("[A-Z][0-9]{8}")) {
    mostrarError("CIF inválido");
    return;
}
```

### 2. Manejo de errores mejorado
```java
try {
    // operación
} catch (Exception e) {
    log.error("Error en operación", e);
    mostrarError("Error: " + e.getMessage());
}
```

### 3. Confirmaciones antes de acciones críticas
```java
// Antes de borrar
Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
alert.setTitle("Confirmar eliminación");
alert.setHeaderText("¿Está seguro?");
Optional<ButtonType> result = alert.showAndWait();
if (result.isPresent() && result.get() == ButtonType.OK) {
    // eliminar
}
```

### 4. Añadir loading indicators
```java
// Mostrar spinner mientras carga datos
progressIndicator.setVisible(true);
CompletableFuture.runAsync(() -> {
    // operación larga
}).thenRun(() -> Platform.runLater(() -> {
    progressIndicator.setVisible(false);
}));
```

---

## 📊 MEJORAS DE STOCK (MEDIA PRIORIDAD)

### Implementar movimientos de stock
```
✓ Crear entidad MovimientoStock
  - tipo (ENTRADA, SALIDA, AJUSTE, TRASPASO)
  - almacenId
  - articuloId
  - cantidad
  - fecha
  - usuario
  - motivo

✓ Registrar automáticamente movimientos:
  - Al crear albarán de venta → SALIDA
  - Al crear albarán de compra → ENTRADA
  - Al regularizar → AJUSTE

✓ Panel de Stock:
  - Stock actual por almacén
  - Historial de movimientos
  - Stock valorado
```

---

## 🎨 MEJORAS DE UI/UX (BAJA PRIORIDAD)

### 1. Atajos de teclado
```
F1 = Ayuda
F2 = Editar seleccionado
F3 = Buscar
F5 = Refrescar
Ctrl+N = Nuevo
Ctrl+S = Guardar
Ctrl+P = Imprimir
ESC = Cancelar/Cerrar
```

### 2. Tooltips informativos
```xml
<Button fx:id="btnNuevo" text="Nuevo">
    <tooltip>
        <Tooltip text="Crear nuevo registro (Ctrl+N)"/>
    </tooltip>
</Button>
```

### 3. Indicadores visuales
- Spinner mientras carga
- Checkmarks cuando se guarda
- Animaciones suaves
- Colores según estado (verde=activo, rojo=inactivo)

---

## 🧪 TESTING BÁSICO

### Tests unitarios mínimos:
```java
@Test
void deberiaCrearCliente() {
    Cliente cliente = new Cliente();
    cliente.setNombre("Test");
    cliente = clienteService.save(cliente);
    assertNotNull(cliente.getId());
}

@Test
void deberiaCalcularTotalFactura() {
    Factura factura = crearFacturaConLineas();
    BigDecimal total = facturaService.calcularTotal(factura);
    assertEquals(new BigDecimal("121.00"), total);
}
```

---

## 📁 ESTRUCTURA FINAL ESPERADA

```
src/main/java/alicanteweb/erp/
├── controller/
│   ├── ui/
│   │   ├── LoginController.java          [NUEVO]
│   │   ├── DashboardController.java      [NUEVO]
│   │   ├── PedidosPanelController.java   [NUEVO]
│   │   ├── TesoreriaPanelController.java [NUEVO]
│   │   ├── ReportesController.java       [NUEVO]
│   │   ├── ConfiguracionController.java  [NUEVO]
│   │   └── UsuariosController.java       [NUEVO]
│   └── (existentes...)
├── entities/
│   ├── Usuario.java                      [NUEVO]
│   ├── Cobro.java                        [NUEVO]
│   ├── Pago.java                         [NUEVO]
│   ├── FormaPago.java                    [NUEVO]
│   ├── MovimientoStock.java              [NUEVO]
│   ├── ConfiguracionEmpresa.java         [NUEVO]
│   ├── Serie.java                        [NUEVO]
│   └── (existentes...)
├── service/
│   ├── AuthService.java                  [NUEVO]
│   ├── UsuarioService.java               [NUEVO]
│   ├── TesoreriaService.java             [NUEVO]
│   ├── ReporteService.java               [NUEVO]
│   ├── ExcelExportService.java           [NUEVO]
│   ├── ConfiguracionService.java         [NUEVO]
│   ├── StockService.java                 [NUEVO]
│   └── (existentes...)
└── repository/
    ├── UsuarioRepository.java            [NUEVO]
    ├── CobroRepository.java              [NUEVO]
    ├── PagoRepository.java               [NUEVO]
    └── (existentes...)
```

---

## 🎯 ROADMAP DE 3 SEMANAS

### Semana 1: Seguridad + Pedidos
- [ ] Día 1-2: Sistema de login completo
- [ ] Día 3-4: Gestión de usuarios y roles
- [ ] Día 5: Panel de pedidos (listado)
- [ ] Día 6-7: Formulario de pedidos + conversión

### Semana 2: Reportes + Tesorería
- [ ] Día 8-9: Dashboard con KPIs
- [ ] Día 10-11: Panel de reportes + gráficos
- [ ] Día 12-13: Módulo de tesorería (cobros)
- [ ] Día 14: Arqueo de caja

### Semana 3: Configuración + Refinamiento
- [ ] Día 15-16: Panel de configuración
- [ ] Día 17: Movimientos de stock
- [ ] Día 18: Validaciones y manejo de errores
- [ ] Día 19: Testing básico
- [ ] Día 20-21: Pulido final + documentación

---

## ✅ CHECKLIST DE CALIDAD

Antes de considerar la app "completa":

### Funcionalidad
- [ ] Login funciona correctamente
- [ ] Todos los módulos accesibles
- [ ] CRUD completo en todas las entidades
- [ ] Reportes generan datos correctos
- [ ] Impresiones se ven bien
- [ ] Conversiones (pedido→factura) funcionan

### Seguridad
- [ ] Contraseñas encriptadas
- [ ] Sesiones con timeout
- [ ] Roles limitando acceso
- [ ] Auditoría de acciones críticas

### UX/UI
- [ ] Sin errores en consola
- [ ] Mensajes de error claros
- [ ] Confirmaciones antes de borrar
- [ ] Loading indicators en operaciones largas
- [ ] Tablas se ordenan correctamente

### Datos
- [ ] Sin pérdida de datos
- [ ] Transacciones funcionan
- [ ] Relaciones BD correctas
- [ ] Backups configurados

---

## 🚀 QUICK WINS (Mejoras rápidas)

### En 1 hora puedes añadir:
1. **Tooltips** en todos los botones
2. **Confirmación** antes de borrar
3. **Validación** de campos obligatorios
4. **Atajos de teclado** básicos (F5=refresh)
5. **Colores** según estado (verde/rojo en tablas)

### En 1 día puedes añadir:
1. **Dashboard básico** con ventas del mes
2. **Exportar a Excel** cualquier tabla
3. **Panel de configuración** simple
4. **Búsqueda mejorada** con filtros
5. **Backup manual** con botón

---

## 💡 CONSEJOS FINALES

### 1. Prioriza lo crítico
No pierdas tiempo en mejoras visuales si falta funcionalidad crítica como login.

### 2. Reutiliza código
Muchos controladores son similares (ClientesController vs ProveedoresController).
Crear una clase base puede ahorrar mucho código.

### 3. Testea continuamente
No esperes al final para probar. Prueba cada módulo según lo implementas.

### 4. Documenta mientras desarrollas
Es más fácil documentar ahora que recordar después qué hace cada cosa.

### 5. Haz commits frecuentes
Git es tu amigo. Commit cada vez que completes una funcionalidad.

---

**Próximo paso recomendado:**  
Empezar por el sistema de login (1-2 días) y luego el módulo de pedidos (2-3 días).

---

*Plan actualizado: 14 de diciembre de 2025*

