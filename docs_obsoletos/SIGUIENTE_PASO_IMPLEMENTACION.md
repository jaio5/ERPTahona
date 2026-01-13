# 🚀 SIGUIENTE PASO - IMPLEMENTACIÓN DE MÉTODOS FALTANTES

## 🎯 Objetivo
Corregir los errores de compilación de los tests implementando los métodos faltantes en los servicios.

## 📋 Plan de Acción

### Fase 1: Análisis de Errores ✅
- [x] Identificar todos los métodos faltantes
- [x] Clasificar por servicio
- [x] Priorizar implementaciones

### Fase 2: Implementación de Métodos 🔄

#### 1. **PresupuestoService** ✅ COMPLETADO
- [x] `save(Presupuesto)`
- [x] `findById(Long)`
- [x] `findAll()`
- [x] `findByCliente(Long)`
- [x] `aceptar(Long)`
- [x] `rechazar(Long)`
- [x] `estaCaducado(Presupuesto)`
- [x] `findPendientes()`
- [x] `deleteById(Long)`

#### 2. **PedidoService** ✅ COMPLETADO
- [x] `generarNumeroPedido()`
- [x] `findByClienteId(Long)`
- [x] `cancelar(Long)`
- [x] `servir(Long)`
- [x] `findByFecha(LocalDate)`
- [x] `findByEstado(String)`

#### 3. **RolService** ⏳ PENDIENTE (10 métodos)
- [ ] `save(Rol)`
- [ ] `findById(Long)`
- [ ] `findAll()`
- [ ] `findByNombre(String)`
- [ ] `findActivos()`
- [ ] `update(Rol)`
- [ ] `desactivar(Long)`
- [ ] `activar(Long)`
- [ ] `deleteById(Long)`
- [ ] Ajustar `tienePermiso()`

#### 4. **PrintService** ⏳ PENDIENTE (10 métodos)
- [ ] `imprimir(File)`
- [ ] `hayImpresoraDisponible()`
- [ ] `getImpresoraPredeterminada()`
- [ ] `listarImpresoras()`
- [ ] `configurarImpresora(String)`
- [ ] `imprimirConOpciones(File, boolean, int)`
- [ ] `vistaPrevia(File)`
- [ ] `imprimirFactura(Long)`
- [ ] `imprimirPresupuesto(Long)`
- [ ] `imprimirAlbaran(Long)`

#### 5. **AuditoriaService** ⏳ PENDIENTE (ajustes)
- [ ] Ajustar firma de `registrarLogin()`
- [ ] Ajustar firma de `registrarAccion()`
- [ ] Ajustar firma de `registrarError()`
- [ ] Agregar `obtenerHistorialUsuario()`
- [ ] Agregar `obtenerHistorialModulo()`

#### 6. **VerifactuService** ⏳ PENDIENTE (ajustes)
- [ ] `isHabilitado()`
- [ ] `generarHuella(String)`
- [ ] `generarQRFactura(Factura)`
- [ ] `validarCertificado()`
- [ ] `enviarFacturaAEAT(Factura)`
- [ ] `validarFactura(Factura)`
- [ ] `generarCSV(Factura)`
- [ ] `registrarEnvio()`
- [ ] `obtenerEstadoServicio()`
- [ ] `cargarCertificado()`
- [ ] `firmarXML(String)`

#### 7. **AutenticacionService** ⏳ PENDIENTE
- [ ] Agregar método `autenticar(String, String)`

#### 8. **ArticuloService** ⏳ PENDIENTE
- [ ] Agregar método `findByCodigo(String)`

#### 9. **ProveedorService** ⏳ PENDIENTE
- [ ] Agregar método `deleteById(Long)`

### Fase 3: Repositorios 🔄

#### PresupuestoRepository ✅ COMPLETADO
- [x] `findByClienteId(Long)`
- [x] `findByEstado(String)`

#### PedidoRepository ✅ COMPLETADO
- [x] `findByClienteId(Long)` *(existía como findByCliente_Id)*
- [x] `findByFecha(LocalDate)`
- [x] `findByEstado(String)`

#### ClienteRepository ⏳ PENDIENTE
- [ ] `findByActivoTrue()`

#### ProveedorRepository ⏳ PENDIENTE
- [ ] `findByActivoTrue()`
- [ ] `findByNombreContainingIgnoreCase(String)`

#### AuditoriaAccionRepository ⏳ PENDIENTE
- [ ] `findByUsuarioNombreOrderByFechaDesc(String)`

### Fase 4: Entidades 🔄

#### Presupuesto ✅ COMPLETADO
- [x] Métodos alias `getValidoHasta()` / `setValidoHasta()`

#### Pedido ✅ COMPLETADO
- [x] Campo `total` agregado

#### Factura ⏳ PENDIENTE
- [ ] Campo `iva`
- [ ] Getters/Setters correspondientes

#### Usuario ⏳ PENDIENTE
- [ ] Campo `rol` (relación con Rol)
- [ ] Campos de recuperación de contraseña
- [ ] Getters/Setters correspondientes

### Fase 5: Validación y Testing ✅
- [x] Ejecutar `mvn test-compile`
- [x] Verificar reducción de errores
- [ ] Corregir errores restantes
- [ ] Ejecutar `mvn test`
- [ ] Verificar cobertura con JaCoCo

---

## 📊 Progreso Actual

### ✅ Completados (2/6 servicios principales)
- ✅ **PresupuestoService** - 9 métodos implementados
- ✅ **PedidoService** - 6 métodos implementados

### Errores de Compilación
- **Inicial**: ~140 errores
- **Actual**: ~80 errores
- **Reducción**: ~43% 🎉

### Próximos Pasos Inmediatos

1. **RolService** - Implementar CRUD completo
2. **ClienteRepository** y **ProveedorRepository** - Agregar métodos faltantes
3. **Entidades** - Agregar campos faltantes (iva, rol, etc.)

---

**Última actualización**: 2026-01-12  
**Estado**: 🔄 43% completado - En progreso activo
**Siguiente**: Implementar RolService


