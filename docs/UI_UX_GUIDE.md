# Guía de Diseño UI/UX - ERP Panadería

## 🎨 Principios de Diseño Aplicados

### 1. Jerarquía Visual Clara

#### Niveles de Información
```
Dashboard (Nivel 1)
    ↓
Módulos (Nivel 2)
    ↓
Detalles/Formularios (Nivel 3)
```

#### Tamaños de Fuente
- **Título principal**: 24px (module-title)
- **Título de header**: 22px (header-title)
- **Títulos de cards**: 18px (card-title)
- **Texto normal**: 14px
- **Subtítulos**: 13px
- **Texto pequeño**: 12px (footer)

### 2. Esquema de Colores Coherente

#### Paleta Principal
| Color | Uso | Código Hex |
|-------|-----|------------|
| Dorado | Primario, CTAs | #D4922B |
| Marrón | Secundario | #8B4513 |
| Verde | Éxito, confirmación | #28A745 |
| Azul | Información | #17A2B8 |
| Amarillo | Advertencias | #FFC107 |
| Rojo | Errores, eliminar | #DC3545 |
| Morado | Especial (Verifactu) | #6F42C1 |

#### Grises
- Oscuro: #2C3E50 (texto primario)
- Medio: #95A5A6 (bordes, iconos)
- Claro: #ECF0F1 (fondos)
- Muy claro: #F8F9FA (fondo general)

### 3. Espaciado Consistente

#### Sistema de Espaciado (basado en 5px)
```css
5px   → Mínimo (entre elementos muy cercanos)
10px  → Pequeño (padding de botones)
15px  → Medio (spacing entre elementos)
20px  → Grande (secciones)
25px  → Extra grande (márgenes de contenedores)
30px  → Padding de cards
40px  → Separación entre secciones principales
```

### 4. Tipografía

#### Familias de Fuente
- **Principal**: "Segoe UI", "Roboto", sans-serif
- **Fallback**: Sistema operativo

#### Pesos de Fuente
- Normal: 400
- Medium: 500
- Bold: 600-700

### 5. Iconografía

#### Iconos FontAwesome Usados
| Módulo | Icono | Código |
|--------|-------|--------|
| Clientes | Usuarios | fas-users |
| Proveedores | Camión | fas-truck |
| Artículos | Caja | fas-box |
| Facturas | Factura | fas-file-invoice-dollar |
| Almacenes | Almacén | fas-warehouse |
| Verifactu | Escudo | fas-shield-alt |
| Buscar | Lupa | fas-search |
| Agregar | Plus | fas-plus |
| Editar | Lápiz | fas-edit |
| Eliminar | Papelera | fas-trash |
| Refrescar | Sync | fas-sync |

### 6. Estados Interactivos

#### Botones
```css
Normal → Hover → Active → Disabled
```

- **Normal**: Color de fondo sólido
- **Hover**: Color más oscuro + escala 1.02-1.05
- **Active**: Color aún más oscuro
- **Disabled**: Opacidad 0.5, cursor not-allowed

#### Tablas
- **Fila normal**: Blanco/gris alterno
- **Fila hover**: Fondo primario 10% opacidad
- **Fila seleccionada**: Fondo primario 20% opacidad

### 7. Feedback Visual

#### Animaciones y Transiciones
```css
transition: all 0.3s ease;
```

- **Botones**: Escala y color al hover (150ms)
- **Cards**: Elevación al hover (sombra más pronunciada)
- **Sidebar**: Cambio de color suave

#### Sombras (Depth)
```css
/* Nivel 1 - Elementos planos */
box-shadow: 0 1px 3px rgba(0,0,0,0.1);

/* Nivel 2 - Elementos elevados */
box-shadow: 0 2px 8px rgba(0,0,0,0.1);

/* Nivel 3 - Hover / Focus */
box-shadow: 0 4px 12px rgba(0,0,0,0.2);
```

## 📐 Layout y Composición

### Grid del Dashboard
```
┌─────────────┬─────────────┬─────────────┐
│   Card 1    │   Card 2    │   Card 3    │
│  Clientes   │ Proveedores │  Artículos  │
├─────────────┼─────────────┼─────────────┤
│   Card 4    │   Card 5    │   Card 6    │
│  Facturas   │  Almacenes  │  Verifactu  │
└─────────────┴─────────────┴─────────────┘
```

Spacing: 20px horizontal y vertical entre cards

### Estructura de Módulos
```
┌─────────────────────────────────────────┐
│  Header (icono + título + descripción)  │ 
├─────────────────────────────────────────┤
│  Toolbar (búsqueda + acciones)          │
├─────────────────────────────────────────┤
│                                         │
│  Tabla de Datos                         │
│                                         │
│  (Scroll vertical si es necesario)      │
│                                         │
└─────────────────────────────────────────┘
```

### Sidebar
- **Ancho**: 250px fijo
- **Color**: Blanco con sombra
- **Botones**: Full width, alineados a la izquierda
- **Iconos**: 18px con espaciado de 12px

## 🎯 UX Best Practices Aplicadas

### 1. Búsqueda en Tiempo Real
- **Feedback inmediato** al escribir
- **Sin necesidad de botón** de búsqueda
- **Filtrado local** para mejor rendimiento

### 2. Acciones Contextuales
- **Botones de acción** siempre visibles en la toolbar
- **Estados disabled** cuando no hay selección
- **Confirmaciones** para acciones destructivas

### 3. Navegación Intuitiva
- **Breadcrumbs** visual con iconos
- **Sidebar persistente** para cambio rápido
- **Hover effects** que indican clickeabilidad

### 4. Tablas Optimizadas
- **Alternancia de colores** en filas (zebra striping)
- **Hover highlight** para identificar fila
- **Columnas resizable** con política CONSTRAINED
- **Headers sticky** (puede implementarse)

### 5. Responsive Design
- **Min-width/max-width** en cards
- **Flexible layouts** con HBox/VBox
- **Priorización de contenido** en espacios reducidos

## 🎨 Componentes Reutilizables

### Card Component
```xml
<VBox styleClass="dashboard-card">
    <VBox alignment="CENTER" spacing="15">
        <FontIcon iconLiteral="..." iconSize="48" styleClass="card-icon-primary"/>
        <Label text="Título" styleClass="card-title"/>
        <Label text="Descripción" styleClass="card-description"/>
        <Button text="Acción" styleClass="card-button"/>
    </VBox>
</VBox>
```

### Module Header
```xml
<HBox styleClass="module-header">
    <FontIcon iconLiteral="..." iconSize="36"/>
    <VBox>
        <Label text="Título" styleClass="module-title"/>
        <Label text="Subtítulo" styleClass="module-subtitle"/>
    </VBox>
</HBox>
```

### Toolbar
```xml
<HBox styleClass="toolbar">
    <TextField promptText="Buscar..." styleClass="search-field"/>
    <Region HBox.hgrow="ALWAYS"/>
    <Button styleClass="btn-success">Nuevo</Button>
    <Button styleClass="btn-primary">Editar</Button>
    <Button styleClass="btn-danger">Eliminar</Button>
</HBox>
```

## 📱 Accesibilidad

### Contraste de Colores
- Todos los textos cumplen **WCAG AA** (ratio mínimo 4.5:1)
- Iconos y botones tienen **suficiente contraste** con el fondo

### Tamaño de Elementos Interactivos
- **Mínimo 40x40px** para botones (touch-friendly)
- **Areas clicables amplias** en cards y botones del sidebar

### Feedback de Estado
- **Hover effects** visibles
- **Focus indicators** en campos de formulario
- **Loading states** (a implementar)

## 🔄 Flujos de Usuario

### Flujo de Consulta
```
1. Usuario abre módulo
2. Ve lista completa de registros
3. Usa búsqueda para filtrar (opcional)
4. Selecciona registro
5. Ve detalles / Edita
```

### Flujo de Creación
```
1. Click en "Nuevo"
2. Se abre formulario
3. Completa campos
4. Guarda
5. Vuelve a lista actualizada
```

### Flujo de Eliminación
```
1. Selecciona registro
2. Click en "Eliminar"
3. Confirma acción
4. Registro se elimina
5. Lista se actualiza
```

## 📊 Métricas de Diseño

### Carga Cognitiva
- **Máximo 3 niveles** de jerarquía
- **7±2 elementos** por grupo visual
- **Acciones agrupadas** por contexto

### Eficiencia
- **Máximo 3 clicks** para cualquier acción común
- **Búsqueda accesible** sin navegación adicional
- **Shortcuts** en botones principales

## 🚀 Mejores Prácticas CSS

### Nomenclatura BEM-like
```css
.module-panel           /* Bloque */
.module-header          /* Elemento */
.card-button:hover      /* Modificador */
```

### Variables CSS (Custom Properties)
```css
* {
    -fx-primary: #D4922B;
    -fx-success: #28A745;
    /* etc. */
}
```

### Reutilización
- Clases base: `.btn-primary`, `.card-icon-*`
- Modificadores: `:hover`, `:disabled`, `:selected`
- Estados: `.active`, `.error`, `.success`

## 🔄 Diseño Responsive

### Principios de Responsividad

El diseño del ERP está optimizado para adaptarse a diferentes tamaños de ventana:

#### Sidebar (Menú Lateral)
- **Ancho flexible**: 200px - 250px
- Los botones se expanden al 100% del ancho disponible
- Padding ajustable: 10px (reducido en pantallas pequeñas)
- `maxWidth="Infinity"` para todos los botones

#### Dashboard Cards (Tarjetas del Dashboard)
- **Distribución**: GridPane con 3 columnas al 33.33% cada una
- **Tamaño de tarjetas**: 
  - Mínimo: 180px
  - Máximo: 280px
- **Padding adaptativo**: 25px vertical, 20px horizontal (reducido de 30px)
- **Altura preferida**: 240px (flexible)

#### Contenido Principal
- **ScrollPane**: Envuelve el GridPane para manejar overflow
- `fitToWidth="true"` y `fitToHeight="true"`
- Scroll horizontal deshabilitado (`hbarPolicy="NEVER"`)
- Scroll vertical según necesidad (`vbarPolicy="AS_NEEDED"`)

#### Column Constraints
```xml
<columnConstraints>
    <ColumnConstraints percentWidth="33.33" halignment="CENTER"/>
    <ColumnConstraints percentWidth="33.33" halignment="CENTER"/>
    <ColumnConstraints percentWidth="33.33" halignment="CENTER"/>
</columnConstraints>
```

### Mejoras Implementadas (2025-12-25)

1. **Eliminación de anchos fijos** en sidebar (era 250px fijo)
2. **Tarjetas con tamaños min/max** en lugar de fixed width
3. **Botones responsive** con `maxWidth="Infinity"`
4. **ScrollPane transparente** para mejor visualización
5. **Column constraints con porcentajes** para distribución equitativa

### CSS Responsive

```css
.sidebar {
    /* Sin -fx-min-width ni -fx-pref-width fijos */
    -fx-background-color: -fx-sidebar-bg;
    -fx-effect: dropshadow(gaussian, -fx-shadow, 5, 0, 2, 0);
}

.sidebar-button {
    /* Sin -fx-min-width fijo */
    -fx-padding: 12 10; /* Reducido de 12 15 */
    -fx-alignment: CENTER_LEFT;
}

.dashboard-card {
    /* Sin -fx-min-width, -fx-min-height, -fx-max-width fijos */
    -fx-pref-height: 240px;
    -fx-padding: 25 20; /* Reducido de 30 */
}
```

### Ventajas del Diseño Responsive

✅ **Adaptabilidad**: El contenido se ajusta automáticamente al tamaño de ventana  
✅ **Sin overflow**: El menú y las tarjetas no sobresalen de la ventana  
✅ **Scroll inteligente**: Solo cuando es necesario  
✅ **Mejor UX**: Mantiene la proporción en diferentes resoluciones  
✅ **Mantenibilidad**: Código más limpio sin valores hardcoded  

## 📝 Checklist de Diseño

- [x] Paleta de colores coherente
- [x] Tipografía consistente
- [x] Espaciado uniforme
- [x] Iconografía significativa
- [x] Feedback visual en interacciones
- [x] Navegación intuitiva
- [x] Tablas legibles
- [x] Búsqueda funcional
- [x] Diseño responsive
- [ ] Formularios validados
- [ ] Mensajes de error claros
- [ ] Loading states
- [ ] Modo oscuro (opcional)

---

Este documento debe servir como guía para mantener la consistencia del diseño en futuras actualizaciones y nuevas funcionalidades.



