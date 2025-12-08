# 🎨 Vista Previa del Frontend - ERP Panadería

## Dashboard Principal

```
╔════════════════════════════════════════════════════════════════════════════════════╗
║  🍞 ERP Panadería Tahona                                    🔔  👤                 ║
║     Sistema de Gestión Integral                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║  ┌──────────────┐                                                                 ║
║  │   MÓDULOS    │                                                                 ║
║  ├──────────────┤                                                                 ║
║  │ 👥 Clientes  │   ┌─────────────────┬─────────────────┬─────────────────┐     ║
║  │ 🚚 Proveed.  │   │                 │                 │                 │     ║
║  │ 📦 Artículos │   │   👥  CLIENTES  │  🚚 PROVEEDORES │  📦 ARTÍCULOS   │     ║
║  │ 💰 Facturas  │   │                 │                 │                 │     ║
║  │ 🏭 Almacenes │   │  Gestión de     │   Gestión de    │   Catálogo de   │     ║
║  │              │   │  clientes y     │   proveedores   │   productos     │     ║
║  │ ─────────── │   │  contactos      │                 │                 │     ║
║  │ 🛡️ Verifactu │   │                 │                 │                 │     ║
║  └──────────────┘   │    [  Abrir  ]  │   [  Abrir  ]   │   [  Abrir  ]   │     ║
║                     └─────────────────┴─────────────────┴─────────────────┘     ║
║                                                                                    ║
║                     ┌─────────────────┬─────────────────┬─────────────────┐     ║
║                     │                 │                 │                 │     ║
║                     │  💰 FACTURAS    │  🏭 ALMACENES   │  🛡️ VERIFACTU   │     ║
║                     │                 │                 │                 │     ║
║                     │  Facturación    │   Control de    │    Sistema de   │     ║
║                     │  y ventas       │   inventario    │   verificación  │     ║
║                     │                 │                 │                 │     ║
║                     │                 │                 │                 │     ║
║                     │    [  Abrir  ]  │   [  Abrir  ]   │   [  Abrir  ]   │     ║
║                     └─────────────────┴─────────────────┴─────────────────┘     ║
║                                                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║  © 2025 ERP Panadería Tahona                              Versión 1.0.0           ║
╚════════════════════════════════════════════════════════════════════════════════════╝
```

---

## Vista de Módulo - Artículos

```
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   📦  ARTÍCULOS                                                                    ║
║      Gestión del catálogo de productos                                            ║
║                                                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║   🔍 Buscar artículos por código o descripción...                                 ║
║                                                                                    ║
║   [🟢 Nuevo Artículo]  [🔵 Editar]  [🔴 Eliminar]  [⚪ Refrescar]                ║
║                                                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║  ┌────┬─────────┬──────────────────────┬──────────┬────────┬───────┬────────┬────┐║
║  │ ID │ Código  │    Descripción       │ Familia  │ Unidad │  PVP  │ Coste  │IVA │║
║  ├────┼─────────┼──────────────────────┼──────────┼────────┼───────┼────────┼────┤║
║  │ 1  │ PAN001  │ Pan de barra grande  │ Panadería│   Ud   │ 1.20€ │ 0.45€  │21% │║
║  │ 2  │ PAN002  │ Pan integral         │ Panadería│   Ud   │ 1.50€ │ 0.60€  │21% │║
║  │ 3  │ BOL001  │ Bollo de leche       │ Bollería │   Ud   │ 0.80€ │ 0.30€  │21% │║
║  │ 4  │ CRO001  │ Croissant mantequilla│ Bollería │   Ud   │ 1.10€ │ 0.50€  │21% │║
║  │ 5  │ PAS001  │ Pastel de manzana    │ Pastelería│   Kg  │12.50€ │ 5.20€  │21% │║
║  │ 6  │ GAL001  │ Galletas de chocolate│ Galletas │  Paq   │ 2.30€ │ 0.95€  │21% │║
║  │ 7  │ BIZ001  │ Bizcocho casero      │ Pastelería│   Ud   │ 8.50€ │ 3.40€  │21% │║
║  │ ... │ ...     │ ...                  │ ...      │  ...   │  ...  │  ...   │... │║
║  └────┴─────────┴──────────────────────┴──────────┴────────┴───────┴────────┴────┘║
║                                                                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
```

---

## Vista de Módulo - Clientes

```
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   👥  CLIENTES                                                                     ║
║      Gestión de clientes y contactos                                              ║
║                                                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║   🔍 Buscar clientes por nombre, código o CIF...                                  ║
║                                                                                    ║
║   [🟢 Nuevo Cliente]  [🔵 Editar]  [🔴 Eliminar]  [⚪ Refrescar]                 ║
║                                                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║  ┌────┬────────┬────────────────┬───────────┬─────────────────┬──────────┬────────┐║
║  │ ID │ Código │    Nombre      │    CIF    │    Dirección    │ Población│  Prov  │║
║  ├────┼────────┼────────────────┼───────────┼─────────────────┼──────────┼────────┤║
║  │ 1  │ CLI001 │ Bar El Rincón  │12345678A  │ Calle Mayor, 23 │ Alicante │Alicante│║
║  │ 2  │ CLI002 │ Restaurante... │87654321B  │ Av. Maisonnave  │ Alicante │Alicante│║
║  │ 3  │ CLI003 │ Hotel Sol y... │11223344C  │ Playa San Juan  │ Alicante │Alicante│║
║  │ 4  │ CLI004 │ Cafetería Lu...│55667788D  │ Rambla Méndez   │ Alicante │Alicante│║
║  │ ... │ ...    │ ...            │ ...       │ ...             │ ...      │ ...    │║
║  └────┴────────┴────────────────┴───────────┴─────────────────┴──────────┴────────┘║
║                                                                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
```

---

## Vista de Módulo - Verifactu

```
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   🛡️  VERIFACTU                                                                    ║
║      Sistema de verificación de facturas                                          ║
║                                                                                    ║
╠════════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║   ┌───────────────────┬───────────────────┬───────────────────┐                  ║
║   │                   │                   │                   │                  ║
║   │  ✅ Total Verif.  │  ⏰ Pendientes    │  ⚠️ Con Errores   │                  ║
║   │                   │                   │                   │                  ║
║   │       156         │        12         │         3         │                  ║
║   │                   │                   │                   │                  ║
║   └───────────────────┴───────────────────┴───────────────────┘                  ║
║                                                                                    ║
║   ┌──────────────────────────────────────────────────────────────┐               ║
║   │ Acciones de Verificación                                      │               ║
║   │                                                                │               ║
║   │ [🔄 Verificar Pendientes] [📄 Generar Informe] [⚙️ Config]    │               ║
║   └──────────────────────────────────────────────────────────────┘               ║
║                                                                                    ║
║   ┌──────────────────────────────────────────────────────────────┐               ║
║   │ Registro de Evidencias                          [⚪ Refrescar] │               ║
║   │                                                                │               ║
║   │  ┌────┬──────────┬────────────┬─────────┬─────────────────┐  │               ║
║   │  │ ID │ Factura  │   Fecha    │ Estado  │      Hash       │  │               ║
║   │  ├────┼──────────┼────────────┼─────────┼─────────────────┤  │               ║
║   │  │ 1  │ FAC-2025 │ 2025-12-01 │ OK      │ a3f2e1d9c8b7... │  │               ║
║   │  │ 2  │ FAC-2026 │ 2025-12-01 │ OK      │ b4g3f2e1d0c9... │  │               ║
║   │  │ 3  │ FAC-2027 │ 2025-12-01 │ OK      │ c5h4g3f2e1d1... │  │               ║
║   │  └────┴──────────┴────────────┴─────────┴─────────────────┘  │               ║
║   └──────────────────────────────────────────────────────────────┘               ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
```

---

## Características Visuales

### 🎨 Colores del Tema
- **Dorado Principal**: `#D4922B` - Botones primarios, acentos
- **Marrón Secundario**: `#8B4513` - Elementos secundarios
- **Verde Éxito**: `#28A745` - Botón "Nuevo"
- **Azul Info**: `#17A2B8` - Botón "Editar", información
- **Rojo Peligro**: `#DC3545` - Botón "Eliminar"
- **Gris Secundario**: `#95A5A6` - Botón "Refrescar"

### ✨ Efectos
- **Hover en Cards**: Elevación con sombra más pronunciada
- **Hover en Botones**: Cambio de color + escala 1.05
- **Hover en Filas**: Fondo dorado con 10% opacidad
- **Transiciones**: 0.3s ease en todos los elementos

### 📐 Espaciado
- **Padding Cards**: 30px
- **Spacing entre Cards**: 20px
- **Padding Contenedores**: 25px
- **Margin Botones**: 15px

### 🔤 Tipografía
- **Font Family**: "Segoe UI", "Roboto", sans-serif
- **Título Principal**: 24px, bold
- **Títulos Módulos**: 18px, bold
- **Texto Normal**: 14px
- **Texto Pequeño**: 12px (footer)

---

## 🎯 Interacciones del Usuario

### Navegación
1. **Dashboard → Módulo**: Click en card o sidebar
2. **Entre Módulos**: Click en botones del sidebar
3. **Volver al Dashboard**: (Pendiente: botón Home)

### Búsqueda
1. **Escribir en campo**: Filtrado automático
2. **Borrar texto**: Muestra todos los registros
3. **Sin resultados**: Tabla vacía

### Acciones CRUD
1. **Nuevo**: Click botón verde → Dialog (pendiente)
2. **Editar**: Seleccionar fila + click botón azul
3. **Eliminar**: Seleccionar fila + click botón rojo → Confirmación
4. **Refrescar**: Click botón gris → Recarga datos

### Estados Visuales
- **Normal**: Color base
- **Hover**: Color más intenso + escala
- **Selected (fila)**: Fondo dorado 20%
- **Disabled**: Opacidad 50%

---

**Esta es la representación visual del frontend implementado.**

