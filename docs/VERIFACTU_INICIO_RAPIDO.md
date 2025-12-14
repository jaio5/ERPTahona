# VeriFactu - Guía de Inicio Rápido

## 🚀 ¿Qué es VeriFactu?

VeriFactu es el sistema de verificación de facturas implementado en el ERP que:
- ✅ Registra automáticamente evidencias criptográficas de cada factura
- ✅ Firma digitalmente las evidencias con certificado digital
- ✅ Crea una cadena de bloques (blockchain) inmutable
- ✅ Envía las evidencias a la AEAT (Agencia Tributaria)

## ⚡ Inicio Rápido (3 pasos)

### Paso 1: Actualizar Base de Datos

Ejecuta el script SQL para añadir los campos necesarios:

```bash
mysql -u root -p tahona < basesdedatos/agregar_campos_verifactu.sql
```

O manualmente desde MySQL Workbench:
1. Abrir `basesdedatos/agregar_campos_verifactu.sql`
2. Ejecutar el script completo

### Paso 2: Configurar Certificado (Opcional para pruebas)

El sistema funciona en **modo prueba** por defecto, sin necesidad de certificado.

Para usar en producción, configura en `application.properties`:

```properties
# Activar envíos reales a AEAT
verifactu.aeat.enabled=true

# Ruta al certificado digital
verifactu.cert.path=/ruta/a/tu/certificado.p12
verifactu.cert.password=tu_password
verifactu.key.alias=tu_alias
```

### Paso 3: ¡Listo! Crear Facturas

El sistema funciona automáticamente:
1. Crear una factura normal desde el ERP
2. Al guardar, se genera automáticamente la evidencia VeriFactu
3. Ver las evidencias en el panel "VeriFactu"

## 📊 Uso del Panel VeriFactu

### Acceder al Panel

1. Arrancar la aplicación
2. Clic en el botón **"VeriFactu"** en el menú principal
3. Ver todas las evidencias registradas

### Funciones Disponibles

| Función | Descripción | Cómo Usar |
|---------|-------------|-----------|
| **Ver Evidencias** | Lista de todas las evidencias | Tabla principal |
| **Ver Detalles** | Información completa | Clic en una evidencia |
| **Refrescar** | Actualizar datos | Botón "🔄 Refrescar" |
| **Reenviar** | Reenviar evidencias con error | Seleccionar + "📤 Reenviar" |
| **Verificar** | Consultar estado en AEAT | Seleccionar + "✓ Verificar" |
| **Validar Cadena** | Verificar integridad blockchain | Botón "🔗 Validar Cadena" |
| **Exportar** | Exportar a CSV | Botón "💾 Exportar" |

### Estados de las Evidencias

| Estado | Significado | Color |
|--------|-------------|-------|
| **PENDIENTE** | Creada, no enviada | 🟠 Naranja |
| **ENVIADO** | Enviada y aceptada | 🟢 Verde |
| **ERROR** | Error al enviar | 🔴 Rojo |
| **VERIFICADO** | Verificada en AEAT | 🟢 Verde |

## 🔍 Ejemplos de Uso

### Ejemplo 1: Ver Evidencias de una Factura

1. Ir al panel **VeriFactu**
2. Buscar por número de factura en la tabla
3. Clic en la evidencia
4. Ver detalles en el panel derecho:
   - Hash de la factura
   - Hash anterior (blockchain)
   - Firma digital
   - Estado del envío

### Ejemplo 2: Reenviar una Evidencia con Error

1. Ir al panel **VeriFactu**
2. Buscar evidencias con estado **ERROR** (en rojo)
3. Seleccionar la evidencia
4. Clic en **"📤 Reenviar"**
5. Confirmar el reenvío
6. El estado cambiará a **ENVIADO** si tiene éxito

### Ejemplo 3: Validar Integridad de la Cadena

1. Ir al panel **VeriFactu**
2. Clic en **"🔗 Validar Cadena"**
3. Ver resultado de validación por serie
4. Si aparece **"✓ VÁLIDA"** = Todo OK
5. Si aparece **"✗ ROTA"** = Problema detectado

### Ejemplo 4: Exportar Evidencias

1. Ir al panel **VeriFactu**
2. Clic en **"💾 Exportar"**
3. Elegir ubicación y nombre del archivo
4. Abrir el CSV en Excel para análisis

## 🛠️ Configuración Avanzada

### Modo Prueba (Predeterminado)

```properties
# En application.properties
verifactu.aeat.enabled=false
```

- ✅ No requiere certificado digital
- ✅ Genera evidencias normalmente
- ✅ Simula envíos a AEAT
- ✅ Perfecto para desarrollo y pruebas

### Modo Producción

```properties
# En application.properties
verifactu.aeat.enabled=true
verifactu.cert.path=/ruta/certificado.p12
verifactu.cert.password=MiPassword123
verifactu.key.alias=mi_certificado
verifactu.aeat.endpoint=https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu
```

- ⚠️ Requiere certificado digital válido
- ⚠️ Envía evidencias reales a AEAT
- ⚠️ Solo usar con certificado de producción

### Obtener Certificado Digital

Para producción necesitas un certificado de:
- **FNMT** (Fábrica Nacional de Moneda y Timbre)
- **Agencia Tributaria**
- Otra autoridad certificadora reconocida

Formato requerido: **PKCS12** (.p12 o .pfx)

## 🔐 Seguridad y Blockchain

### ¿Cómo Funciona el Blockchain?

Cada evidencia contiene:
1. **Hash**: Huella digital SHA-256 de la factura
2. **Hash Anterior**: Hash de la evidencia previa

Esto crea una cadena:
```
Evidencia 1: hash1 = SHA256(factura1)
               ↓
Evidencia 2: hash2 = SHA256(factura2 + hash1)
               ↓
Evidencia 3: hash3 = SHA256(factura3 + hash2)
```

**Resultado**: Si alguien modifica cualquier evidencia, toda la cadena posterior se invalida.

### ¿Por Qué es Seguro?

- ✅ **Inmutable**: No se puede modificar sin detectarlo
- ✅ **Firmado**: Firma digital RSA con certificado
- ✅ **Trazable**: Cada cambio deja rastro
- ✅ **Verificable**: La AEAT puede validar todo

## ❓ Preguntas Frecuentes

### ¿Se crean evidencias automáticamente?

**Sí**, cada vez que guardas una factura, se crea automáticamente su evidencia VeriFactu.

### ¿Necesito certificado para probar?

**No**, el sistema funciona en modo prueba sin certificado. Solo simula los envíos.

### ¿Qué pasa si falla un envío?

El sistema marca la evidencia como **ERROR** y puedes reenviarla manualmente desde el panel.

### ¿Puedo eliminar evidencias?

**No recomendado**. Las evidencias son parte de una cadena blockchain. Eliminar una rompe la cadena.

### ¿Cómo verifico que todo funciona?

1. Crear una factura de prueba
2. Ir al panel VeriFactu
3. Verificar que aparece la evidencia
4. Ver que el estado es PENDIENTE o ENVIADO
5. Validar la cadena con el botón "🔗 Validar Cadena"

### ¿Qué hacer si la cadena está rota?

1. Identificar desde qué evidencia se rompió
2. Verificar que no se hayan modificado datos manualmente en BD
3. Revisar logs de la aplicación
4. Si es producción, contactar soporte AEAT

## 📈 Estadísticas

El panel muestra en tiempo real:
- **Total Verificaciones**: Total de evidencias registradas
- **Pendientes**: Evidencias creadas pero no enviadas
- **Errores**: Evidencias que fallaron al enviar (requieren acción)

## 🐛 Solución de Problemas

### Sistema aparece "Deshabilitado"

**Causa**: Keystore no encontrado o inválido (solo afecta en modo producción)

**Solución**: Verificar configuración del certificado o usar modo prueba

### No aparecen evidencias

**Causa**: Script SQL no ejecutado

**Solución**: Ejecutar `basesdedatos/agregar_campos_verifactu.sql`

### Error al exportar

**Causa**: Permisos de escritura

**Solución**: Elegir una carpeta donde tengas permisos

### Errores al reenviar

**Causa**: Endpoint AEAT incorrecto o sin conexión

**Solución**: Verificar configuración de `aeat.endpoint`

## 📚 Más Información

Para información detallada, consultar:
- `docs/VERIFACTU_DOCUMENTACION.md` - Documentación completa
- `docs/VERIFACTU_RESUMEN.md` - Resumen ejecutivo
- Logs de la aplicación en consola

## ✅ Checklist Rápido

Antes de empezar:
- [ ] Script SQL ejecutado
- [ ] Aplicación arrancada correctamente
- [ ] Panel VeriFactu accesible
- [ ] Crear factura de prueba
- [ ] Verificar evidencia creada
- [ ] Probar validación de cadena

---

**¡Listo!** Ya tienes VeriFactu funcionando. 🎉

Para pasar a producción, solo necesitas configurar tu certificado digital y activar `verifactu.aeat.enabled=true`.

