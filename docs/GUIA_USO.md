# 📘 Guía de Uso - ERP Tahona

## Índice

1. [Inicio de Sesión](#inicio-de-sesión)
2. [Dashboard Principal](#dashboard-principal)
3. [Gestión de Clientes](#gestión-de-clientes)
4. [Gestión de Proveedores](#gestión-de-proveedores)
5. [Gestión de Artículos](#gestión-de-artículos)
6. [Albaranes de Venta](#albaranes-de-venta)
7. [Facturación](#facturación)
8. [Almacenes](#almacenes)
9. [Verifactu](#verifactu)

---

## Inicio de Sesión

### Arrancar la Aplicación

```bash
# Desde el directorio del proyecto
.\arrancar.bat
```

La aplicación se abre automáticamente mostrando el dashboard principal.

---

## Dashboard Principal

### Pantalla de Inicio

```
┌────────────┐
│ SIDEBAR    │  ÁREA PRINCIPAL
├────────────┤  ┌──────────┬──────────┬──────────┐
│ 👥Clientes │  │ Clientes │Proveed.  │Artículos │
│ 🚚Proveed. │  │          │          │          │
│ 📦Artículos│  ├──────────┼──────────┼──────────┤
│ 📋Albaranes│  │ Albaranes│Facturas  │Almacenes │
│ 💰Facturas │  │          │          │          │
│ 🏭Almacenes│  └──────────┴──────────┴──────────┘
│ 🛡️Verifactu│
└────────────┘
```

### Navegación

- **Clic en tarjeta del dashboard** → Abre el módulo
- **Clic en botón del sidebar** → Cambia de módulo
- **Botón Home** → Vuelve al dashboard

---

## Gestión de Clientes

### Ver Clientes

1. Clic en **"👥 Clientes"** en el dashboard o sidebar
2. Se muestra la lista de todos los clientes

### Buscar Cliente

1. Escribir en el campo **"🔍 Buscar cliente..."**
2. La búsqueda es en tiempo real
3. Busca por: código, nombre, CIF, dirección

### Crear Nuevo Cliente

1. Clic en **"📄 Nuevo Cliente"**
2. Rellenar formulario:
   - **Código** (automático)
   - **Nombre** (obligatorio)
   - **CIF/NIF**
   - **Dirección**
   - **Teléfono**
   - **Email**
3. Clic en **"Guardar"**

### Editar Cliente

1. Seleccionar cliente en la tabla
2. Clic en **"✏️ Editar"**
3. Modificar datos
4. Clic en **"Guardar"**

### Eliminar Cliente

1. Seleccionar cliente
2. Clic en **"🗑️ Eliminar"**
3. Confirmar la eliminación

⚠️ **Nota:** No se puede eliminar un cliente con facturas asociadas.

---

## Gestión de Proveedores

### Funciones Disponibles

- **Ver lista completa** de proveedores
- **Buscar** por código, nombre o CIF
- **Crear nuevo** proveedor
- **Editar** datos de proveedor
- **Eliminar** proveedor
- **Refrescar** lista

### Datos del Proveedor

- Código
- Nombre
- CIF
- Dirección
- Teléfono
- Email
- Estado (Activo/Inactivo)

---

## Gestión de Artículos

### Ver Artículos

Tabla con columnas:
- **Código** - Identificador único
- **Descripción** - Nombre del producto
- **PVP** - Precio de venta al público
- **Stock** - Cantidad disponible
- **Stock Mínimo** - Alerta de reposición
- **IVA** - Tipo impositivo
- **Activo** - Estado

### Crear Artículo

1. Clic en **"📄 Nuevo Artículo"**
2. Rellenar:
   - **Código** (ej: ART001)
   - **Descripción** (ej: BARRA GALLEGA)
   - **PVP** (ej: 1.06)
   - **Stock actual**
   - **Stock mínimo**
   - **IVA** (4%, 10% o 21%)
3. Guardar

### IVA en Panadería

- **4% (Superreducido):** Pan común
- **10% (Reducido):** Bollería, pan especial
- **21% (General):** Repostería, otros

---

## Albaranes de Venta

### ¿Qué es un Albarán?

Documento de entrega de mercancía **sin valor fiscal**.  
Se usa para el control de entregas antes de facturar.

### Crear Albarán

1. Clic en **"📄 Nuevo Albarán"**
2. Seleccionar:
   - **Cliente**
   - **Almacén**
   - **Fecha**
3. **Añadir líneas:**
   - Seleccionar artículo
   - Indicar cantidad
   - Precio automático
4. Guardar

### Imprimir Albarán

1. Seleccionar albarán en la lista
2. Clic en **"🖨️ Imprimir"**
3. Elegir formato:
   - **Recibo Panadería** ⭐ (formato tipo ticket)
   - Clásico
   - Moderno
   - Compacto
4. Se abre en navegador para imprimir

**Formato Recibo Panadería:**
- Encabezado con datos de GRUPO BABO
- Logo LA TAHONA EL ALTET
- Información del cliente
- Tabla de productos
- Totales

---

## Facturación

### Estados de Factura

```
BORRADOR → REVISION → EMITIDA → (ANULADA)
```

#### BORRADOR
- Factura en edición
- Se puede modificar y eliminar
- **No se ha enviado a AEAT**

#### REVISION
- Pendiente de aprobación
- Se pueden añadir observaciones
- No se puede eliminar

#### EMITIDA
- **Enviada a Verifactu/AEAT**
- No se puede modificar
- Solo se puede anular

#### ANULADA
- Factura cancelada
- Registro histórico

### Crear Factura

1. Clic en **"📄 Nueva Factura"**
2. Datos:
   - **Número** (automático: FAC001, FAC002...)
   - **Fecha**
   - **Cliente** (obligatorio)
3. **Añadir líneas:**
   - Artículo
   - Cantidad
   - Precio
   - IVA
4. Guardar (estado: BORRADOR)

### Flujo de Emisión

#### 1. Enviar a Revisión

1. Seleccionar factura en estado **BORRADOR**
2. Clic en **"📝 Enviar a Revisión"**
3. Confirmar
4. Estado cambia a **REVISION**

#### 2. Aprobar y Emitir

1. Seleccionar factura en estado **REVISION**
2. Clic en **"✅ Aprobar y Emitir"**
3. **El sistema automáticamente:**
   - ✅ Valida datos de empresa
   - ✅ Valida líneas de factura
   - ✅ Genera XML con datos de GRUPO BABO:
     ```xml
     <Emisor>
       <NIF>F54059985</NIF>
       <Nombre>GRUPO BABO, S.Coop.V.L.</Nombre>
     </Emisor>
     ```
   - ✅ Genera hash SHA-256
   - ✅ Firma digitalmente (si hay certificado)
   - ✅ Guarda evidencia en BD
   - ✅ Envía a Verifactu/AEAT
4. Estado cambia a **EMITIDA**

**Mensaje de confirmación:**
```
✅ Factura FAC001 emitida a AEAT

La factura ha sido enviada correctamente 
a Verifactu con los datos de:

GRUPO BABO, S.Coop.V.L.
CIF: F54059985

Fecha de emisión: 25/12/2025 14:30:45
```

#### 3. Volver a Borrador (opcional)

Si una factura en REVISION tiene errores:

1. Seleccionar factura en **REVISION**
2. Clic en **"↩️ Volver a Borrador"**
3. Estado vuelve a **BORRADOR**
4. Se puede editar

#### 4. Anular Factura (opcional)

Si una factura EMITIDA tiene errores:

1. Seleccionar factura en **EMITIDA**
2. Clic en **"❌ Anular"**
3. Indicar **motivo de anulación**
4. Estado cambia a **ANULADA**

⚠️ **Importante:** Generar factura rectificativa si es necesario.

### Imprimir Factura

1. Seleccionar factura
2. Clic en **"🖨️ Imprimir"**
3. Elegir diseño
4. Se abre en navegador

---

## Almacenes

### Gestión de Almacenes

Lista de almacenes con:
- **Código**
- **Nombre**
- **Ubicación**

### Funciones

- Crear nuevo almacén
- Editar datos
- Eliminar almacén
- Ver stock por almacén

---

## Verifactu

### Panel de Evidencias

Muestra todas las facturas enviadas a la AEAT.

### Columnas

- **Serie** - Serie de la factura
- **Número** - Número de factura
- **Fecha Emisión** - Cuándo se emitió
- **Hash** - Hash SHA-256 de la factura
- **Hash Anterior** - Hash de la factura anterior (cadena)
- **Estado** - ENVIADO, PENDIENTE, ERROR

### Estadísticas

Panel superior muestra:
- **Total de evidencias**
- **Pendientes de envío**
- **Enviadas correctamente**
- **Errores**

### Verificar Integridad

El sistema verifica automáticamente la cadena de hashes:
- Hash anterior de factura N = Hash de factura N-1
- Garantiza que no se han modificado facturas

---

## Uso Diario Recomendado

### Por la Mañana

1. **Arrancar sistema**
   ```bash
   .\arrancar.bat
   ```

2. **Revisar pendientes:**
   - Facturas en REVISION
   - Albaranes sin facturar

### Durante el Día

1. **Crear albaranes** según entregas
2. **Registrar ventas**
3. **Actualizar stock**

### Al Final del Día

1. **Crear facturas** desde albaranes
2. **Enviar a REVISION**
3. **Aprobar y Emitir**
4. **Verificar en Verifactu**

### Al Cerrar

1. **Revisar facturas emitidas**
2. **Verificar evidencias**
3. **Cerrar aplicación**

El backup automático se ejecuta a las 2:00 AM.

---

## Atajos de Teclado

| Acción | Atajo |
|--------|-------|
| Refrescar | F5 |
| Buscar | Ctrl+F |
| Nuevo | Ctrl+N |
| Guardar | Ctrl+S |
| Eliminar | Delete |

---

## Consejos y Buenas Prácticas

### ✅ Hacer

- Enviar facturas a REVISION antes de emitir
- Revisar datos antes de aprobar
- Verificar evidencias Verifactu
- Hacer backup regular
- Mantener stock actualizado

### ❌ Evitar

- Emitir facturas sin revisar
- Modificar facturas emitidas
- Eliminar clientes con facturas
- Dejar facturas en BORRADOR mucho tiempo

---

## Solución de Problemas

### No puedo emitir una factura

**Causa:** Estado incorrecto

**Solución:** La factura debe estar en estado REVISION.

### Error "No hay configuración de empresa"

**Causa:** Datos de empresa no configurados

**Solución:**
```bash
configurar_bd.bat
```

### Verifactu no funciona

**Causa:** Modo de pruebas activado

**Solución:** Para producción, configurar certificado digital.

---

## Soporte

**GRUPO BABO, S.Coop.V.L.**  
Tel: 965 68 73 58  
Email: administracion@grupobaelo.com

Ver también:
- [INSTALACION.md](INSTALACION.md) - Instalación
- [GUIA_DESPLIEGUE_COMPLETA.md](../GUIA_DESPLIEGUE_COMPLETA.md) - Despliegue

---

**¡Disfruta usando el ERP Tahona! 🍞**

