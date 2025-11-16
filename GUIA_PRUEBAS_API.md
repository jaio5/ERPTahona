# 🧪 GUÍA DE PRUEBAS - API REST ERP PANADERÍA

## 📋 Comandos de Prueba

### 🚀 **Iniciar la Aplicación**

```bash
# Desde la raíz del proyecto
cd "D:\Programación\ERP"
.\mvnw.cmd spring-boot:run
```

---

## 🔍 **Pruebas con PowerShell (Windows)**

### **1. Health Check**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/health"
```

### **2. Familias de Artículos**
```powershell
# Obtener todas las familias
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/familias"
$response | Format-Table

# Obtener familia específica (código 01)
Invoke-RestMethod -Uri "http://localhost:8080/api/familias/01"
```

### **3. Provincias**
```powershell
# Todas las provincias
$provincias = Invoke-RestMethod -Uri "http://localhost:8080/api/provincias"
$provincias | Select-Object -First 5 | Format-Table

# Provincia específica (Alicante)
Invoke-RestMethod -Uri "http://localhost:8080/api/provincias/03"
```

### **4. Artículos**
```powershell
# Primeros 10 artículos
$articulos = Invoke-RestMethod -Uri "http://localhost:8080/api/articulos"
$articulos | Select-Object -First 10 | Select-Object codigoArticulo, descripcionCorta, pvp1 | Format-Table

# Artículo específico
Invoke-RestMethod -Uri "http://localhost:8080/api/articulos/CODIGO_AQUI"
```

### **5. Clientes**
```powershell
# Todos los clientes
$clientes = Invoke-RestMethod -Uri "http://localhost:8080/api/clientes"
$clientes | Select-Object codigo, nombre, poblacion | Format-Table

# Cliente específico
Invoke-RestMethod -Uri "http://localhost:8080/api/clientes/CODIGO_CLIENTE"
```

### **6. Formas de Pago**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/formaspago" | Format-Table
```

### **7. Tipos de IVA**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tiposiva" | Format-Table
```

### **8. Agentes**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/agentes" | Format-Table
```

### **9. Zonas**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/zonas" | Format-Table
```

### **10. Sectores**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/sectores" | Format-Table
```

---

## 🌐 **Pruebas con curl (Windows/Linux/Mac)**

### **Health Check**
```bash
curl http://localhost:8080/api/health
```

### **Familias**
```bash
# Todas
curl http://localhost:8080/api/familias

# Específica
curl http://localhost:8080/api/familias/01
```

### **Provincias**
```bash
# Todas
curl http://localhost:8080/api/provincias

# Alicante
curl http://localhost:8080/api/provincias/03
```

### **Con formato JSON (usando jq en Linux/Mac)**
```bash
curl http://localhost:8080/api/familias | jq '.'
curl http://localhost:8080/api/provincias | jq '.[] | select(.codigoProvincia=="03")'
```

---

## 🔧 **Pruebas Avanzadas con PowerShell**

### **Guardar respuesta en archivo**
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/articulos"
$response | ConvertTo-Json -Depth 10 | Out-File "articulos.json"
```

### **Contar registros**
```powershell
$familias = Invoke-RestMethod -Uri "http://localhost:8080/api/familias"
Write-Host "Total de familias: $($familias.Count)"

$provincias = Invoke-RestMethod -Uri "http://localhost:8080/api/provincias"
Write-Host "Total de provincias: $($provincias.Count)"
```

### **Filtrar resultados**
```powershell
# Provincias de Andalucía
$provincias = Invoke-RestMethod -Uri "http://localhost:8080/api/provincias"
$provincias | Where-Object { $_.zona -eq "AND" } | Format-Table

# Familias que contengan "PAN"
$familias = Invoke-RestMethod -Uri "http://localhost:8080/api/familias"
$familias | Where-Object { $_.descripcionFamilia -like "*PAN*" } | Format-Table
```

### **Buscar artículo por descripción**
```powershell
$articulos = Invoke-RestMethod -Uri "http://localhost:8080/api/articulos"
$articulos | Where-Object { $_.descripcionCorta -like "*BARRA*" } | 
    Select-Object codigoArticulo, descripcionCorta, pvp1 | Format-Table
```

---

## 🧪 **Pruebas con Postman**

### **Colección de Endpoints**

1. **Health Check**
   - Method: `GET`
   - URL: `http://localhost:8080/api/health`

2. **Listar Familias**
   - Method: `GET`
   - URL: `http://localhost:8080/api/familias`

3. **Obtener Familia**
   - Method: `GET`
   - URL: `http://localhost:8080/api/familias/01`

4. **Listar Provincias**
   - Method: `GET`
   - URL: `http://localhost:8080/api/provincias`

5. **Listar Artículos**
   - Method: `GET`
   - URL: `http://localhost:8080/api/articulos`

6. **Listar Clientes**
   - Method: `GET`
   - URL: `http://localhost:8080/api/clientes`

---

## 📊 **Verificar Performance**

### **Medir tiempo de respuesta**
```powershell
Measure-Command {
    Invoke-RestMethod -Uri "http://localhost:8080/api/articulos"
}
```

### **Múltiples peticiones**
```powershell
1..10 | ForEach-Object {
    $start = Get-Date
    Invoke-RestMethod -Uri "http://localhost:8080/api/health" | Out-Null
    $end = Get-Date
    $elapsed = ($end - $start).TotalMilliseconds
    Write-Host "Request $_ : $elapsed ms"
}
```

---

## 🐛 **Debugging**

### **Ver logs en tiempo real**
```powershell
# Los logs se muestran en la consola donde ejecutaste spring-boot:run
# Para guardar en archivo:
.\mvnw.cmd spring-boot:run > application.log 2>&1
```

### **Verificar conexión a MySQL**
```powershell
# Conectar a MySQL y verificar datos
mysql -u root -p tahona -e "SELECT COUNT(*) FROM articulos;"
mysql -u root -p tahona -e "SELECT * FROM familias;"
```

---

## 📝 **Script de Prueba Completo**

```powershell
# test-api.ps1
Write-Host "=== PRUEBAS API ERP PANADERÍA ===" -ForegroundColor Cyan

# 1. Health Check
Write-Host "`n1. Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/health"
    Write-Host "✅ API está UP: $($health.service)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: API no disponible" -ForegroundColor Red
    exit
}

# 2. Familias
Write-Host "`n2. Probando Familias..." -ForegroundColor Yellow
$familias = Invoke-RestMethod -Uri "http://localhost:8080/api/familias"
Write-Host "✅ Familias obtenidas: $($familias.Count)" -ForegroundColor Green

# 3. Provincias
Write-Host "`n3. Probando Provincias..." -ForegroundColor Yellow
$provincias = Invoke-RestMethod -Uri "http://localhost:8080/api/provincias"
Write-Host "✅ Provincias obtenidas: $($provincias.Count)" -ForegroundColor Green

# 4. Artículos
Write-Host "`n4. Probando Artículos..." -ForegroundColor Yellow
$articulos = Invoke-RestMethod -Uri "http://localhost:8080/api/articulos"
Write-Host "✅ Artículos obtenidos: $($articulos.Count)" -ForegroundColor Green

# 5. Clientes
Write-Host "`n5. Probando Clientes..." -ForegroundColor Yellow
$clientes = Invoke-RestMethod -Uri "http://localhost:8080/api/clientes"
Write-Host "✅ Clientes obtenidos: $($clientes.Count)" -ForegroundColor Green

Write-Host "`n=== TODAS LAS PRUEBAS EXITOSAS ===" -ForegroundColor Green
```

**Ejecutar:**
```powershell
.\test-api.ps1
```

---

## 🎯 **Casos de Uso Comunes**

### **1. Buscar provincia por nombre**
```powershell
$provincias = Invoke-RestMethod -Uri "http://localhost:8080/api/provincias"
$provincias | Where-Object { $_.nombreProvincia -eq "ALICANTE" }
```

### **2. Listar todas las zonas únicas**
```powershell
$provincias = Invoke-RestMethod -Uri "http://localhost:8080/api/provincias"
$provincias | Select-Object -ExpandProperty zona -Unique | Sort-Object
```

### **3. Contar artículos por familia**
```powershell
$articulos = Invoke-RestMethod -Uri "http://localhost:8080/api/articulos"
$articulos | Group-Object familiaArticulo | 
    Select-Object Name, Count | Sort-Object Count -Descending
```

### **4. Verificar tipos de IVA disponibles**
```powershell
$ivas = Invoke-RestMethod -Uri "http://localhost:8080/api/tiposiva"
$ivas | Select-Object codigoTipoDeIva, porcentajeDeIva | Format-Table
```

---

## 📱 **Usar desde Navegador**

Simplemente abre tu navegador y visita:

```
http://localhost:8080/api/health
http://localhost:8080/api/familias
http://localhost:8080/api/provincias
http://localhost:8080/api/articulos
http://localhost:8080/api/clientes
```

**Tip:** Instala una extensión como "JSON Viewer" para ver los datos formateados.

---

## ⚠️ **Troubleshooting**

### **Error: No se puede conectar**
```powershell
# Verificar que la aplicación está corriendo
Get-Process | Where-Object { $_.ProcessName -like "*java*" }

# Verificar puerto 8080
netstat -ano | findstr :8080
```

### **Error: Base de datos**
```powershell
# Verificar MySQL
Get-Service | Where-Object { $_.Name -like "*mysql*" }

# Probar conexión
mysql -u root -p -e "SELECT 1;"
```

### **Reiniciar la aplicación**
```powershell
# Ctrl+C para detener
# Luego:
.\mvnw.cmd spring-boot:run
```

---

**Última actualización:** 16/11/2025  
**Puerto por defecto:** 8080  
**Base de datos:** tahona (MySQL 8.0.43)

