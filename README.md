# 🍞 ERP Tahona - GRUPO BABO
## Sistema de Gestión Empresarial con Verifactu

**Versión:** 1.0.0  
**Empresa:** GRUPO BABO, S.Coop.V.L. (CIF: F54059985)

---

## 📋 Descripción

ERP completo para gestión empresarial con integración Verifactu de la Agencia Tributaria Española.

**Características principales:**
- ✅ Gestión de Clientes, Proveedores y Artículos
- ✅ Albaranes y Facturación
- ✅ Control de Almacenes
- ✅ Integración Verifactu/AEAT
- ✅ Validaciones fiscales españolas
- ✅ Impresión de documentos
- ✅ Interfaz moderna JavaFX

---

## 🚀 Inicio Rápido

### 1. Requisitos

| Software | Versión |
|----------|---------|
| Java JDK | 17+ |
| MySQL | 8.0+ |
| Maven | 3.8+ (incluido) |

### 2. Instalación (3 pasos)

```bash
# Paso 1: Configurar base de datos (5 min)
configurar_bd.bat

# Paso 2: Compilar (2 min)
.\mvnw clean compile

# Paso 3: Arrancar (1 min)
.\arrancar.bat
```

**¡Listo!** La aplicación arranca con datos de GRUPO BABO preconfigurados.

---

## 📖 Documentación

| Documento | Descripción |
|-----------|-------------|
| **[INSTALACION.md](docs/INSTALACION.md)** | Instalación paso a paso |
| **[GUIA_USO.md](docs/GUIA_USO.md)** | Manual de usuario completo |
| **[GUIA_DESPLIEGUE_COMPLETA.md](GUIA_DESPLIEGUE_COMPLETA.md)** | Despliegue en producción |

---

## 🏢 Datos Configurados

```
GRUPO BABO, S.Coop.V.L.
CIF: F54059985
Tel: 965 68 73 58
Dirección: Armada Española, P.2 Nº213
03195 El Altet - ELCHE
```

Estos datos se usan automáticamente en facturas, albaranes y Verifactu.

---

## 🔄 Flujo de Facturación

```
BORRADOR → REVISION → EMITIDA (Verifactu/AEAT)
```

1. **Crear factura** (BORRADOR)
2. **Enviar a revisión** (REVISION)
3. **Aprobar y emitir** (EMITIDA - envía a AEAT)

---

## 💻 Tecnologías

- **Frontend:** JavaFX 17
- **Backend:** Spring Boot 3.5
- **Base de Datos:** MySQL 8.0
- **Build:** Maven 3.8

---

## 📞 Soporte

**GRUPO BABO, S.Coop.V.L.**  
Tel: 965 68 73 58  
Email: administracion@grupobaelo.com

---

**Copyright © 2025 GRUPO BABO, S.Coop.V.L.**

