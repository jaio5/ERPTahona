# ERP Tahona — Guía de Despliegue en Producción

> **Última actualización:** 26 de junio de 2026  
> **Versión:** 0.0.1  
> **Spring Boot:** 3.5.7 | **Java:** 17+ | **Base de datos:** MySQL 8+

---

## 1. Requisitos del sistema

| Componente | Versión mínima | Notas |
|------------|---------------|-------|
| Java JDK | 17 | OpenJDK o Oracle |
| MySQL | 8.0 | Con soporte Flyway |
| Maven | 3.9+ | Para compilar |
| Memoria RAM | 2 GB mínimo | 4 GB recomendado |
| Disco | 1 GB + datos | Crecimiento lineal con BD |

---

## 2. Variables de entorno requeridas

| Variable | Requerida | Descripción | Ejemplo |
|----------|-----------|-------------|---------|
| `DB_URL` | ✅ Prod | JDBC URL MySQL | `jdbc:mysql://localhost:3306/erp_tahona` |
| `DB_USER` | ✅ Prod | Usuario de base de datos | `erp_app` |
| `DB_PASSWORD` | ✅ Prod | Contraseña de base de datos | `**secreto**` |
| `SECURITY_PBKDF2_SECRET` | ✅ Prod | Pepper para hash de contraseñas (32+ chars aleatorios) | `Kx9#mP2$vL7@nQ4!` |
| `CIFRADO_AES_KEY` | ✅ Prod | Clave AES-256 en Base64 (44 caracteres) | Generar con: `openssl rand -base64 32` |
| `ADMIN_DEFAULT_PASSWORD` | ✅ Prod | Contraseña inicial del usuario admin | 12+ caracteres, cumple política |
| `SPRING_PROFILES_ACTIVE` | ✅ | Perfil activo | `prod` |
| `LOCAL_DB_PASSWORD` | ❌ Solo dev | Contraseña para perfil local | `cambiar_en_local` |
| `VERIFACTU_CERT_PATH` | ❌ | Ruta al certificado Verifactu (solo si se usa) | `/etc/ssl/verifactu.p12` |

---

## 3. Compilación y despliegue

### Build

```bash
# Compilar con perfil de producción
./mvnw clean package -Pprod

# El JAR se genera en target/ERP-0.0.1.jar
```

### Ejecución

```bash
# Con variables de entorno
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://localhost:3306/erp_tahona
export DB_USER=erp_app
export DB_PASSWORD=***
export SECURITY_PBKDF2_SECRET=***
export CIFRADO_AES_KEY=***
export ADMIN_DEFAULT_PASSWORD=***

java -jar target/ERP-0.0.1.jar
```

### Health Check

```bash
curl http://localhost:8080/web/login
# Debe devolver la página de login (HTTP 200)
```

---

## 4. Base de datos

### Migraciones Flyway
Las migraciones se ejecutan automáticamente al arrancar (Flyway está integrado).

| Versión | Descripción |
|---------|-------------|
| V1–V6 | Esquema inicial |
| V7 | Columnas `fecha_bloqueo` y `contador_bloqueos` para desbloqueo automático |
| V31 | Columnas `fecha_bloqueo` y `contador_bloqueos` (alternativa si V7 ya existe) |

### Backup

```bash
mysqldump -u erp_app -p erp_tahona > backup_$(date +%Y%m%d).sql
```

### Restore

```bash
mysql -u erp_app -p erp_tahona < backup_20260101.sql
```

---

## 5. Seguridad

### Política de contraseñas
- Mínimo 8 caracteres
- Al menos 1 mayúscula
- Al menos 1 dígito
- PBKDF2WithHmacSHA256 a 185,000 iteraciones con salt aleatorio

### Bloqueo de cuenta
- 5 intentos fallidos → cuenta bloqueada
- Desbloqueo automático con **tiempo incremental** según el contador de bloqueos:

| Bloqueo # | Tiempo de espera |
|-----------|-----------------|
| 1 | 5 minutos |
| 2 | 15 minutos |
| 3 | 30 minutos |
| 4 | 1 hora |
| 5 | 2 horas |
| 6 | 4 horas |
| 7 | 8 horas |
| 8+ | 24 horas |

### Sesiones
- **1 sesión máxima por usuario**
- Si se intenta un segundo login, se rechaza (el anterior sigue activo)
- Timeout de sesión: 15 minutos

### Rate limiting
- Login: máximo 10 intentos por minuto por IP
- Superado → bloqueo de 1 minuto (HTTP 429)

### Headers de seguridad
| Header | Valor |
|--------|-------|
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` |
| CSP | `default-src 'self'; script-src 'self' 'nonce-{random}'; object-src 'none'; frame-ancestors 'none'` |
| `Referrer-Policy` | Configurar en el proxy/load balancer (nginx, Apache) como `strict-origin-when-cross-origin` |

### CSRF
- Protección CSRF habilitada con cookie HttpOnly
- Token disponible vía meta tag `csrf-token` para peticiones `fetch()` desde JavaScript
- Todos los formularios POST incluyen `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">`

---

## 6. Paginación — Estado de controladores

| Controlador | Paginado | Volumen esperado | Notas |
|-------------|----------|-----------------|-------|
| Factura | ✅ | Alto | Buscador + filtro por estado |
| Albaran | ✅ | Alto | Buscador + filtro por estado |
| Cliente | ✅ | Medio | Buscador |
| Artículo | ✅ | Medio | Buscador |
| PedidoCompra | ✅ | Medio | Buscador + filtro por estado |
| FacturaCompra | ✅ | Medio | Buscador + filtro por estado |
| OrdenProduccion | ✅ | Medio | Filtro por estado |
| Usuario | ✅ | Bajo | Buscador |
| PedidoVenta | ✅ | Medio | Buscador + filtro por estado |
| Proveedor | ✅ | Bajo | Buscador |
| Presupuesto | ✅ | Medio | Buscador + filtro por estado |
| Lote | ✅ | Medio | Buscador + filtro por estado |
| Horneada | ✅ | Bajo | Ya tenía `findPage()` |
| Devolucion | ✅ | Bajo | Ya tenía `findPage()` |
| HojaRuta | ❌ | Bajo | Pendiente de paginar |
| Recepcion | ❌ | Bajo | Pendiente de paginar |
| Merma | ❌ | Bajo | Pendiente de paginar |
| AppccControl | ❌ | Bajo | Pendiente de paginar |
| Auditoria | ❌ ⚠️ | **Alto** | **Prioridad: paginar en el próximo sprint** |
| Tesoreria | ❌ | Medio | Diseño especial (modales) |

---

## 7. Firewall y red

| Puerto | Propósito | Acceso |
|--------|-----------|--------|
| 8080 | Aplicación web | Acceso desde usuarios internos (VPN o red local) |
| 3306 | MySQL | Solo localhost (no exponer) |

Recomendación: usar **nginx** como reverse proxy para HTTPS, gzip, y cabeceras adicionales:

```nginx
server {
    listen 443 ssl;
    server_name erp.tahona.local;
    
    ssl_certificate /etc/ssl/certs/erp.crt;
    ssl_certificate_key /etc/ssl/private/erp.key;
    
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 8. Resolución de problemas

### La aplicación no arranca
1. Verificar que MySQL está corriendo: `mysqladmin ping`
2. Verificar credenciales en variables de entorno
3. Revisar logs: `tail -f application.log`

### Error "Too many connections" en BD
Ajustar en `application-prod.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
```

### Usuario admin bloqueado
Si el admin queda bloqueado y no hay otro admin, ejecutar en BD:
```sql
UPDATE users SET bloqueado = 0, intentos_fallidos = 0, contador_bloqueos = 0 WHERE username = 'admin';
```

### El login no funciona (CSRF)
Verificar que `login.html` incluye el token CSRF. A partir de la versión actual, ya está incluido.

---

## 9. Actualización desde Git

```bash
# Configurar en application-prod.properties:
# app.update.enabled=true
# app.update.git-remote=origin
# app.update.git-branch=main

# La aplicación ejecuta git pull + rebuild al recibir
# un webhook o manualmente. Consultar documentación interna.
```
