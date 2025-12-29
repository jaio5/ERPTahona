# 📖 GUÍA DE USUARIO - ERP Tahona

## 📑 ÍNDICE

1. [Inicio de Sesión](#inicio-de-sesión)
2. [Panel Principal](#panel-principal)
3. [Gestión de Clientes](#gestión-de-clientes)
4. [Gestión de Artículos](#gestión-de-artículos)
5. [Gestión de Proveedores](#gestión-de-proveedores)
6. [Gestión de Almacenes](#gestión-de-almacenes)
7. [Gestión de Albaranes](#gestión-de-albaranes)
8. [Gestión de Facturas](#gestión-de-facturas)
9. [Verifactu](#verifactu)
10. [Solución de Problemas](#solución-de-problemas)

---

## 🔐 INICIO DE SESIÓN

### Acceder al Sistema

1. Ejecutar la aplicación:
   ```bash
   .\scripts\iniciar.bat
   ```
   O:
   ```bash
   mvn javafx:run
   ```

2. Se abrirá la ventana de login

3. Introducir credenciales:
   - **Usuario:** `admin`
   - **Contraseña:** `admin`

4. Click en "Iniciar Sesión"

### ⚠️ Problemas Comunes

**"Credenciales inválidas"**
- Verificar que escribes correctamente usuario y contraseña
- Ejecutar `.\scripts\verificar_sistema.bat` para resetear

**Usuario bloqueado**
- El sistema bloquea después de 3 intentos fallidos
- Ejecutar script de verificación para desbloquear

---

## 🏠 PANEL PRINCIPAL

Al iniciar sesión verás el **Dashboard** con 7 módulos principales:

### Módulos Disponibles

| Módulo | Descripción | Icono |
|--------|-------------|-------|
| 👥 **Clientes** | Gestión de clientes | fas-users |
| 🚚 **Proveedores** | Gestión de proveedores | fas-truck |
| 📦 **Artículos** | Catálogo de productos | fas-box |
| 📄 **Albaranes** | Albaranes de entrega | fas-file-alt |
| 🧾 **Facturas** | Facturación | fas-file-invoice-dollar |
| 🏪 **Almacenes** | Gestión de stock | fas-warehouse |
| 🔐 **Verifactu** | Integración AEAT | fas-shield-alt |

### Navegación

- **Click en cualquier módulo** del sidebar para acceder
- El contenido principal cambiará automáticamente
- Los datos se cargan desde la base de datos en tiempo real

---

## 👥 GESTIÓN DE CLIENTES

### Ver Lista de Clientes

1. Click en **"Clientes"** en el sidebar
2. Verás una tabla con todos los clientes
3. La tabla muestra:
   - ID
   - Código
   - Nombre
   - CIF
   - Dirección
   - Población
   - Provincia
   - Código Postal

### Buscar Clientes

1. Usar el campo **"Buscar..."** en la parte superior
2. Escribir nombre del cliente
3. La tabla se filtra automáticamente en tiempo real
4. Borrar el campo para ver todos

### Crear Nuevo Cliente

1. Click en botón **"Nuevo"**
2. Se abre un formulario modal
3. Rellenar los campos:
   - **Código:** Se genera automáticamente (CLIxxx)
   - **Nombre:** ⚠️ Obligatorio
   - **CIF:** Opcional
   - **Dirección:** Opcional
   - **Población:** Opcional
   - **Código Postal:** Opcional
   - **Provincia:** Opcional
   - **Notas:** Opcional
4. Click en **"Guardar"**
5. El cliente aparece en la tabla

### Editar Cliente

1. **Seleccionar** un cliente de la tabla (click en la fila)
2. Click en botón **"Editar"**
3. Se abre el formulario con datos actuales
4. Modificar los campos necesarios
5. Click en **"Guardar"**
6. Los cambios se reflejan en la tabla

### Dar de Baja Cliente

1. Seleccionar un cliente
2. Click en **"Eliminar"**
3. Confirmar la acción
4. El cliente se marca como inactivo (aparece con ❌)
5. **Nota:** No se borra, solo se desactiva

### Refrescar Datos

- Click en **"Refrescar"** para recargar desde la BD
- Útil si otro usuario ha hecho cambios

---

## 📦 GESTIÓN DE ARTÍCULOS

### Ver Catálogo

1. Click en **"Artículos"**
2. Tabla con 131 artículos de ejemplo
3. Columnas:
   - ID
   - Código
   - Descripción
   - PVP (Precio Venta Público)
   - IVA
   - Activo

### Crear Nuevo Artículo

1. Click en **"Nuevo"**
2. Formulario con campos:
   - **Código:** Auto-generado (ARTxxx)
   - **Descripción:** ⚠️ Obligatorio
   - **PVP:** ⚠️ Obligatorio
   - **Coste:** Opcional
   - **IVA:** % de IVA a aplicar
   - **Familia:** Categoría
   - **Unidad:** Ud, Kg, etc.
3. Guardar

### Buscar Artículos

- Campo de búsqueda por descripción
- Filtrado en tiempo real

### Editar/Eliminar

- Mismo proceso que clientes
- Seleccionar, Editar/Eliminar, Confirmar

---

## 🚚 GESTIÓN DE PROVEEDORES

### Funcionalidades

Similar a Clientes pero para proveedores:

- Crear nuevo proveedor
- Campos: Nombre, CIF, Dirección, Teléfono, Email
- Editar, Eliminar, Buscar
- Código auto-generado (PROxxx)

### Datos Específicos

- **Teléfono:** Campo adicional
- **Email:** Para comunicaciones
- **País:** Internacionalización

---

## 🏪 GESTIÓN DE ALMACENES

### Ver Almacenes

1. Click en **"Almacenes"**
2. Lista de almacenes disponibles
3. Por defecto hay 1 almacén

### Crear Almacén

1. Nuevo
2. Campos:
   - Código (auto)
   - Nombre
3. Guardar

### Uso

Los almacenes se usan en:
- Albaranes
- Control de stock
- Movimientos de mercancía

---

## 📄 GESTIÓN DE ALBARANES

### ¿Qué es un Albarán?

Documento que certifica la entrega de mercancía.

### Ver Albaranes

1. Click en **"Albaranes"**
2. Lista de albaranes emitidos
3. Información:
   - Número
   - Fecha
   - Cliente
   - Total
   - Almacén

### Crear Albarán

1. Click en **"Nuevo"**
2. Seleccionar:
   - **Cliente**
   - **Almacén**
   - **Fecha**
3. Añadir líneas:
   - Seleccionar artículo
   - Cantidad
   - Precio se calcula automáticamente
4. Ver totales automáticos
5. Guardar

### Imprimir Albarán

1. Seleccionar albarán
2. Click en **"Imprimir"**
3. Se genera PDF
4. Opción de diseño blanco/negro

---

## 🧾 GESTIÓN DE FACTURAS

### Crear Factura

1. Click en **"Facturas"** → **"Nuevo"**
2. Datos de cabecera:
   - **Cliente:** Seleccionar
   - **Número:** Auto-generado
   - **Serie:** A, B, etc.
   - **Fecha:** Hoy por defecto
   - **Fecha Vencimiento:** Opcional
3. Añadir líneas:
   - Buscar artículo
   - Cantidad
   - Precio
   - IVA aplicado
4. Cálculos automáticos:
   - Base Imponible
   - IVA
   - Total
5. Guardar

### Ver Facturas

- Lista ordenada por fecha
- Filtros por cliente, estado, fecha
- Estados: Pendiente, Pagada, Enviada

### Imprimir Factura

1. Seleccionar factura
2. Click **"Imprimir"**
3. Elegir diseño
4. Generar PDF

### Enviar a Verifactu

1. Seleccionar factura
2. Click **"Enviar a Verifactu"**
3. Sistema registra en AEAT
4. Se genera QR y hash

---

## 🔐 VERIFACTU

### ¿Qué es Verifactu?

Sistema de la Agencia Tributaria para registro de facturas.

### Estado del Servicio

1. Click en **"Verifactu"**
2. Ver estado:
   - Habilitado/Deshabilitado
   - Certificado configurado
   - Última sincronización

### Configuración

1. Subir certificado (.p12)
2. Introducir contraseña
3. Probar conexión
4. Activar

### Evidencias

- Ver facturas registradas
- Hash blockchain
- QR code
- Fecha de registro

**Nota:** Verifactu es opcional. La aplicación funciona sin él.

---

## 🔍 SOLUCIÓN DE PROBLEMAS

### No puedo iniciar sesión

**Síntoma:** "Credenciales inválidas"

**Solución:**
```bash
.\scripts\verificar_sistema.bat
```

### No veo datos en las tablas

**Síntoma:** Tabla vacía

**Solución:**
1. Click en "Refrescar"
2. Verificar conexión a MySQL
3. Verificar que la BD tiene datos

### Error al crear registro

**Síntoma:** Mensaje de error al guardar

**Solución:**
1. Verificar campos obligatorios (*)
2. Verificar formato de datos
3. Revisar logs de aplicación

### Ventana se congela

**Síntoma:** Aplicación no responde

**Solución:**
1. Cerrar y reiniciar
2. Verificar logs
3. Comprobar memoria disponible

### No se imprimen las facturas

**Síntoma:** Error al generar PDF

**Solución:**
1. Verificar permisos de escritura
2. Comprobar espacio en disco
3. Revisar configuración de impresora

---

## 💡 CONSEJOS Y TRUCOS

### Atajos de Teclado

- **Tab:** Navegar entre campos
- **Enter:** Aceptar/Guardar
- **Esc:** Cancelar

### Búsqueda Rápida

- Escribir directamente en campo de búsqueda
- No hace falta click previo
- Filtrado instantáneo

### Datos Seguros

- Todos los cambios se guardan en BD
- Auditoría completa de acciones
- Backup recomendado diario

### Rendimiento

- La app carga solo lo necesario
- Lazy loading en tablas grandes
- Optimizado para MySQL

---

## 📞 SOPORTE

### Información de Sistema

- **Versión:** 0.0.1
- **Java:** 17
- **JavaFX:** 21
- **Spring Boot:** 3.5.7
- **MySQL:** 8.0

### Logs

Los logs se muestran en la consola durante la ejecución.

### Contacto

[Información de contacto de soporte]

---

**Última actualización:** 27/12/2025  
**Versión de guía:** 1.0

