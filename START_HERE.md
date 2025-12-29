# 🚀 START HERE - EMPIEZA AQUÍ

**¡Bienvenido al proyecto ERP Panadería Tahona!**

---

## ⚡ GUÍA RÁPIDA DE 5 MINUTOS

### 1️⃣ ¿Primera vez aquí?

**Lee esto primero:**
- 📄 [INDICE_DOCUMENTACION.md](INDICE_DOCUMENTACION.md) - Mapa completo de la documentación
- 📄 [README.md](README.md) - Introducción y primeros pasos

**Tiempo:** 5 minutos

---

### 2️⃣ ¿Qué es este proyecto?

Es un **ERP (Sistema de Gestión Empresarial)** para panaderías y negocios del sector alimentario, desarrollado con:
- ☕ Java 17 + Spring Boot 3.5.7
- 🎨 JavaFX 21 (interfaz gráfica)
- 🗄️ MySQL 8.0
- ✅ Cumplimiento legal español (AEAT, RGPD)

**Estado:** 60% completo - Funcional para facturación y ventas

---

### 3️⃣ ¿Qué puede hacer?

✅ **LO QUE FUNCIONA:**
- Gestión de clientes
- Gestión de artículos
- Albaranes de venta
- Facturación completa (con workflow de aprobación)
- Impresión de facturas y albaranes en PDF
- Verifactu (AEAT) - simulado
- RGPD completo
- Auditoría de operaciones

❌ **LO QUE FALTA:**
- Contabilidad
- Tesorería (caja y bancos)
- Módulo de compras
- Módulos fiscales (303, 347, 390)
- TPV (Terminal Punto de Venta)
- Informes y estadísticas

📄 **Detalles completos:** [QUE_FALTA_PARA_ERP_COMPLETO.md](QUE_FALTA_PARA_ERP_COMPLETO.md)

---

### 4️⃣ ¿Cómo lo instalo?

**Opción rápida (Windows):**
```bash
# 1. Clonar (si aún no lo has hecho)
git clone [url-del-repo]
cd ERP

# 2. Ejecutar script automático
.\scripts\iniciar.bat
```

**Opción manual:**
```bash
# 1. Compilar
mvn clean compile

# 2. Ejecutar
mvn javafx:run
```

**Credenciales:**
- Usuario: `admin`
- Contraseña: `admin`

📄 **Guía completa:** [documentacion/INSTALACION.md](documentacion/INSTALACION.md)

---

### 5️⃣ ¿Dónde está la documentación?

```
📚 TODA LA DOCUMENTACIÓN:

🔥 IMPORTANTES (leer primero):
   └─ INDICE_DOCUMENTACION.md ............. Mapa de navegación
   └─ README.md ............................ Introducción
   └─ DOCUMENTACION_COMPLETA.md ............ Todo el detalle técnico
   └─ QUE_FALTA_PARA_ERP_COMPLETO.md ....... Qué falta y plan de acción

📊 ANÁLISIS:
   └─ RESUMEN_VISUAL.md .................... Estado visual del proyecto
   └─ CHECKLIST_LIMPIEZA.md ................ Limpieza realizada

🔧 DESARROLLO:
   └─ RECOMENDACIONES_MEJORA.md ............ Mejores prácticas

📖 GUÍAS ESPECÍFICAS (carpeta documentacion/):
   └─ INSTALACION.md ....................... Instalación detallada
   └─ GUIA_USUARIO.md ...................... Manual del usuario
   └─ CUMPLIMIENTO_LEGAL_ESPANA.md ......... Normativa legal
   └─ FLUJO_FACTURACION_CORREGIDO.md ....... Cómo facturar
```

---

## 🎯 ¿QUÉ QUIERES HACER?

### 👨‍💼 Si eres GERENTE / CLIENTE
```
1. Lee: RESUMEN_VISUAL.md (10 min)
2. Lee: QUE_FALTA_PARA_ERP_COMPLETO.md (30 min)
3. Decide: ¿Implementar más módulos o usar tal cual?
```

### 👨‍💻 Si eres DESARROLLADOR
```
1. Lee: README.md (10 min)
2. Lee: DOCUMENTACION_COMPLETA.md (90 min)
3. Lee: RECOMENDACIONES_MEJORA.md (45 min)
4. Explora el código
5. ⚠️ URGENTE: Implementar tests (0% cobertura actual)
```

### 🔧 Si eres ADMINISTRADOR DE SISTEMAS
```
1. Lee: README.md (10 min)
2. Sigue: documentacion/INSTALACION.md (20 min)
3. Configura: DOCUMENTACION_COMPLETA.md (sección configuración)
4. Prueba: Login con admin/admin
```

### 👤 Si eres USUARIO FINAL
```
1. Lee: documentacion/GUIA_USUARIO.md
2. Lee: documentacion/FLUJO_FACTURACION_CORREGIDO.md
3. ¡Empieza a usar el sistema!
```

---

## 📊 ESTADO ACTUAL

```
╔════════════════════════════════════════╗
║                                        ║
║  PROYECTO ERP TAHONA                   ║
║                                        ║
║  Completado: ████████████░░░░░░ 60%   ║
║                                        ║
║  ✅ Facturación:        100%           ║
║  ✅ Clientes:           100%           ║
║  ✅ Albaranes:          100%           ║
║  ✅ Seguridad:          100%           ║
║  ✅ RGPD:               100%           ║
║  ✅ Auditoría:          100%           ║
║  🟡 Artículos:          60%            ║
║  🟡 Verifactu:          70%            ║
║  ❌ Contabilidad:       0%             ║
║  ❌ Tesorería:          0%             ║
║  ❌ Compras:            0%             ║
║  ❌ Fiscal:             5%             ║
║                                        ║
╚════════════════════════════════════════╝
```

---

## 🏆 VEREDICTO RÁPIDO

**¿Está listo para usar?**

✅ **SÍ para:**
- Facturación y ventas
- Gestión de clientes
- Control básico de inventario
- Emitir facturas legales

❌ **NO para:**
- ERP completo (falta 40%)
- Contabilidad
- Control fiscal completo
- Gestión de tesorería

**Tiempo para completar:** 4-6 meses  
**Coste estimado:** 70.000€

---

## ⚡ ACCIONES RÁPIDAS

### Si quieres PROBARLO:
```bash
mvn javafx:run
# Usuario: admin
# Contraseña: admin
```

### Si quieres VER el código:
```bash
# Abrir en tu IDE favorito
idea .   # IntelliJ
code .   # VS Code
```

### Si quieres DESARROLLAR:
```bash
# Crear rama
git checkout -b feature/mi-mejora

# Leer primero:
# - DOCUMENTACION_COMPLETA.md
# - RECOMENDACIONES_MEJORA.md
```

---

## 🐛 PROBLEMAS COMUNES

### No arranca la aplicación
```bash
mvn clean compile
mvn javafx:run
```

### No puedo hacer login
```sql
-- Resetear contraseña admin
mysql -u root -p
USE tahona;
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
                 bloqueado = 0, 
                 intentos_fallidos = 0 
WHERE username = 'admin';
-- Contraseña será: admin
```

### Error de base de datos
```bash
# Verificar que MySQL está activo
# Verificar credenciales en application.properties
```

📄 **Más soluciones:** [DOCUMENTACION_COMPLETA.md](DOCUMENTACION_COMPLETA.md) - Sección Troubleshooting

---

## 📞 ¿NECESITAS AYUDA?

1. 📖 **Lee primero:** [INDICE_DOCUMENTACION.md](INDICE_DOCUMENTACION.md)
2. 🔍 **Busca en:** [DOCUMENTACION_COMPLETA.md](DOCUMENTACION_COMPLETA.md)
3. ❓ **No lo encuentras:** Consulta la sección apropiada según tu rol

---

## 🎓 ORDEN DE LECTURA RECOMENDADO

### Para EMPEZAR (30 min):
1. Este archivo (5 min)
2. README.md (10 min)
3. RESUMEN_VISUAL.md (10 min)
4. Probar la aplicación (5 min)

### Para ENTENDER TODO (2 horas):
1. INDICE_DOCUMENTACION.md (5 min)
2. DOCUMENTACION_COMPLETA.md (90 min)
3. QUE_FALTA_PARA_ERP_COMPLETO.md (30 min)

### Para DESARROLLAR (3 horas):
1. Todo lo anterior
2. RECOMENDACIONES_MEJORA.md (45 min)
3. Explorar código fuente

---

## ✅ CHECKLIST DE INICIO

- [ ] He leído este archivo (START_HERE.md)
- [ ] He leído el README.md
- [ ] He leído el INDICE_DOCUMENTACION.md
- [ ] He instalado el proyecto
- [ ] He probado el login (admin/admin)
- [ ] He navegado por la aplicación
- [ ] He leído la documentación completa
- [ ] Entiendo qué falta para ser ERP completo
- [ ] He decidido qué hacer a continuación

---

## 🚀 ¡AHORA EMPIEZA!

**Próximo paso:** Lee el [README.md](README.md)

---

<div align="center">

# 👋 ¡Bienvenido!

Este proyecto está **limpio, documentado y listo** para usar o desarrollar.

**¿Tienes dudas?** Consulta el [INDICE_DOCUMENTACION.md](INDICE_DOCUMENTACION.md)

---

**Creado:** 29 de Diciembre de 2025  
**Versión:** 1.0

</div>

