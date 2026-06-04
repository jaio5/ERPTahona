# Despliegue de produccion

## Preparacion inicial

1. Instalar JDK/JRE 17, 21 o 23.
2. Instalar MySQL 8 y crear una base dedicada.
3. Crear un usuario MySQL de aplicacion sin permisos administrativos globales.
4. Copiar `.env.production.example` a `.env.production.local`.
5. Rellenar `.env.production.local` con valores reales.
6. Guardar certificados fuera del repositorio.

## Verificacion previa

```powershell
.\scripts\check-production-env.ps1
.\scripts\check-verifactu-production.ps1
.\scripts\check-production-readiness.ps1
.\mvnw.cmd test
.\scripts\build-production.ps1
```

## Paquete entregable

Para generar una carpeta con JAR, scripts, plantilla de entorno y documentacion:

```powershell
.\scripts\package-production.ps1
```

La salida se genera en `dist\erp-tahona-production`. Ese directorio no se versiona.

## Estructura del paquete

```
dist\erp-tahona-production\
  ERP-0.0.1.jar
  .env.production.example
  README.md
  README_RUN.md
  MANIFEST.txt
  scripts\
    check-production-env.ps1
    check-verifactu-production.ps1
    check-production-readiness.ps1
    load-env-file.ps1
    run-production.ps1
  docs\
    production-deployment.md
    production-checklist.md
    operations.md
    configuration.md
    security-and-authorization.md
    verifactu.md
    architecture.md
```

## Arranque

```powershell
.\scripts\run-production.ps1
```

El script carga `.env.production.local`, valida variables criticas, valida certificado VERI*FACTU y arranca el ultimo JAR generado.

## Primer acceso

1. Entrar con `admin` y la contrasena temporal de `ADMIN_DEFAULT_PASSWORD`.
2. Cambiar la contrasena cuando la aplicacion lo solicite.
3. Crear usuarios nominales y asignar roles.
4. Configurar datos fiscales de empresa.
5. Revisar series y ciclo completo de facturacion.
6. Ejecutar un backup manual y comprobar que se genera correctamente.
7. Nuevos modulos a verificar tras el alta:
   - Crear recetas con ingredientes (Produccion > Recetas).
   - Crear orden de produccion y completar el ciclo (Produccion > Ordenes).
   - Crear lotes con trazabilidad (Produccion > Lotes / Trazabilidad).
   - Configurar vehiculos y rutas (Reparto > Vehiculos).
   - Generar hojas de ruta y confirmar entregas (Reparto > Hojas de ruta).
   - Probar devoluciones (Reparto > Devoluciones).

## Nuevos modulos (2026)

El ERP incluye ahora gestion completa de:

| Modulo | Funcionalidad |
|--------|--------------|
| **Produccion** | Recetas, BOM/escandallo, ordenes de produccion, horneadas |
| **Trazabilidad** | Lotes con caducidad, trazabilidad hacia adelante/atras, registro sanitario |
| **Reparto** | Vehiculos, rutas maestras con paradas, hojas de ruta diarias, confirmacion de entregas |
| **Devoluciones** | Gestion de devoluciones con lineas, vinculacion a lotes y facturas |
| **REST API** | Endpoints para acceso web/movil a produccion, trazabilidad y reparto |

## VERI*FACTU

1. Configurar certificado y endpoint en `.env.production.local`.
2. Arrancar aplicacion.
3. Abrir pantalla VERI*FACTU.
4. Ejecutar `Probar AEAT`.
5. Si la prueba es correcta, ejecutar `Iniciar VERI*FACTU`.

Una vez iniciado, la renuncia se programa para el 31 de diciembre del ano en curso.
