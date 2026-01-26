# Plan de revisión: FXML → Controladores → Entidades → Repositorios

Objetivo
- Revisar sistemáticamente todos los ficheros FXML del proyecto para asegurar que:
  - Cada FXML referencia un controlador válido (fx:controller) y todos los fx:id/eventos referenciados existen en ese controlador.
  - Cada controlador está correctamente conectado a sus entidades (DTOs/Entities) y a los repositorios/servicios que utiliza.
  - No haya campos/métodos @FXML faltantes, firmas incorrectas, o uso incorrecto de dependencias que provoquen warnings o errores en tiempo de compilación o ejecución.

Resultado esperado
- Un repositorio con un `docs/` que contiene:
  - Este plan (`FXML_CONTROLLER_REVIEW_PLAN.md`).
  - Un archivo `reviews/` por cada FXML revisado (tabla con hallazgos y acciones tomadas).
- Código compilable sin warnings relacionados a FXML/controladores (además de pruebas locales que verifiquen wiring básico).

Alcance
- Revisar todos los ficheros `*.fxml` en `src/main/resources` y `src/main/java/**/view` (si aplica).
- Revisar controladores en `src/main/java` que se referencien desde FXML.
- Revisar que controladores usan servicios/repositorios correctos y que éstos existen y están inyectados correctamente.

Contrato (qué se verificará)
- Inputs: ficheros FXML, clases Controller, Entities, Repositories, Services.
- Outputs: por cada FXML un archivo de revisión con:
  - Resultado: OK / Warning / Error
  - Lista de issues (campo faltante, método/evento sin handler, import faltante, tipo mismatched)
  - Acción tomada o recomendación precisa (ej. añadir @FXML, corregir fx:id, inyectar servicio, crear repositorio)
- Error modes: referencias rotas, fx:id no encontradas, handlers con firma incorrecta, beans no disponibles en Spring Context.

Checklist global
- [ ] Listar todos los FXML del proyecto
- [ ] Por cada FXML:
  - [ ] Validar XML (bien formado)
  - [ ] Verificar atributo fx:controller apunta a clase existente
  - [ ] Extraer todos los fx:id y handlers (onAction, onMouseClicked, etc.)
  - [ ] Comprobar campo @FXML y métodos públicos en el controlador con la misma firma
  - [ ] Ejecutar una compilación para atrapar warnings/errores de tipos
  - [ ] Revisar inyecciones (@Autowired / constructor) en el controlador: services/repositories
  - [ ] Verificar que los repositories y services declarados existen y compilan
  - [ ] Probar una inicialización mínima del controlador en test unitario (si procede)
  - [ ] Documentar y corregir (cambiar FXML o Controller) sin usar @SuppressWarnings
- [ ] Ejecutar `mvn -DskipTests clean compile` y `mvn dependency:analyze`
- [ ] Ejecutar pruebas unitarias/integra si existen (mvn test)

Pasos detallados por FXML (procedimiento)
1. Localizar todos los FXML
   - Comando PowerShell (desde la raíz del repo):

```powershell
# lista de FXML
Get-ChildItem -Path . -Recurse -Include *.fxml | Select-Object FullName | Out-File docs/reviews/fxml-list.txt -Encoding UTF8
```

2. Para cada FXML (repite el flujo):
   a) Validar XML y extraer fx:controller, fx:id y handlers

```powershell
$fxml = 'ruta\al\archivo.fxml'
# extraer fx:controller
Select-String -Path $fxml -Pattern 'fx:controller' -SimpleMatch
# extraer fx:id
Select-String -Path $fxml -Pattern 'fx:id="' -AllMatches | % { $_.Matches.Value }
# extraer handlers (onAction, onMouseClicked...)
Select-String -Path $fxml -Pattern 'on[A-Za-z]+' -AllMatches | % { $_.Matches.Value }
```

   b) Verificar que la clase controlador existe y abrirla

```powershell
# ejemplo: convertir el fx:controller en ruta de clase y buscar fichero
Select-String -Path .\src\main\java\**\*.java -Pattern 'NombreDelControlador' -List
```

   c) Verificar campos @FXML para cada fx:id
   - Buscar en el controlador: `@FXML` y campo con mismo nombre
   - Si no existe, revisar si la field está `private` y necesita `@FXML`, o si el fx:id está mal escrito en el FXML.

   d) Verificar handlers: métodos públicos con firma adecuada (por ejemplo `public void onClick(ActionEvent e)` o `@FXML public void onClick(ActionEvent e)`)
   - Si el handler no existe o la firma es distinta, corregir el FXML o agregar método en el controlador.

   e) Revisar inyecciones de Spring en el controlador
   - Preferir inyección por constructor. Si usa `@Autowired` en campos, considerar convertir a constructor.
   - Buscar servicios/repositories usados en el controlador (ej. `verifactuService`) y comprobar que esas clases existen y son `@Service`/`@Repository`.

   f) Compilar y ejecutar análisis local

```powershell
# compilar y listar errores/warnings
.\mvnw.cmd -DskipTests clean compile
# analizar dependencias
.\mvnw.cmd dependency:analyze
```

   g) Documentar el resultado en `docs/reviews/<nombre-fxml>-review.md` usando la plantilla (ver más abajo).

Plantilla para `docs/reviews/<nombre-fxml>-review.md`

````md
# Revisión: <nombre-fxml>
- Path: <ruta>
- fx:controller: <controlador.fqdn>

## Hallazgos
- [OK|Warning|Error] fx:id <id> -> <detalle>
- [OK|Warning|Error] handler <onAction...> -> <detalle>
- [OK|Warning|Error] inyecciones -> <detalle>

## Acciones tomadas
- Cambio aplicado: (ej: corregido fx:id, añadido @FXML, inyectado servicio)
- Archivo(s) editado(s): <rutas>

## Validación
- mvn clean compile -> OK/FAIL (adjuntar salida relevante)
- Notas adicionales
````

Automatizaciones y checks útiles (scripts / comandos)
- Buscar fx:controller en todos los FXML:

```powershell
Select-String -Path .\**\*.fxml -Pattern 'fx:controller' | Select-Object Path, LineNumber, Line
```

- Extraer fx:id por FXML (script simple en PowerShell o small Java/Python):

```powershell
# por cada fxml extrae fx:id
Get-ChildItem -Path . -Recurse -Include *.fxml | ForEach-Object {
  $file = $_.FullName
  $ids = Select-String -Path $file -Pattern 'fx:id="([^"]+)"' -AllMatches | ForEach-Object { $_.Matches } | ForEach-Object { $_.Groups[1].Value }
  [PSCustomObject]@{File=$file; IDs = ($ids -join ', ')}
} | Format-Table -AutoSize
```

- Comparador fx:id vs @FXML fields: (manual o con script)
  - Extraer ids del FXML y campos `@FXML` del controlador y comparar; fallos indicar cuáles faltan.

Riesgos comunes y cómo resolverlos
- Fx:id declarado pero sin `@FXML` en el controlador: añadir `@FXML private Type name;` o cambiar visibilidad si se usa sin `@FXML`.
- Handler declarado sin parámetro o con parámetro incorrecto: agregar `@FXML public void handler(ActionEvent e)` o ajustar FXML para usar método sin parámetro (si es válido).
- Controlador no encontrado: comprobar paquete/fqdn en fx:controller y actualizar import o mover clase.
- Beans no inyectados: convertir a inyección por constructor y verificar que la clase es un `@Service` o `@Repository`.

Quality gates (para aceptar la revisión)
- Compilación limpia: `mvn -DskipTests clean compile` sin errores relevantes.
- No hay warnings `Method 'X' is never used` salvo que sean métodos públicos usados por FXML (el analizador a veces no detecta uso por reflexión). En ese caso mantener el `@FXML` en el método/campo.
- Los controladores deben usar `@FXML` en campos y métodos referenciados por FXML.

Siguientes pasos (propuesta de trabajo)
1. Ejecuta los comandos de listado de FXML y genera `docs/reviews/fxml-list.txt`.
2. Asigna responsables por revisar ficheros (o pídeme que haga la primera pasada y genere las `reviews/` automáticamente).
3. Voy revisando uno por uno (si me das permiso, puedo: leer cada FXML, abrir el controlador, detectar fallos y proponer/realizar las correcciones automáticas mínimas). 

Si quieres que ejecute la primera pasada y genere los archivos `docs/reviews/<fxml>-review.md` automáticamente, dime "haz la primera pasada" y la ejecuto: listaré FXML, comprobaré controller existence, extraeré fx:id/handlers y crearé los archivos de revisión con hallazgos iniciales.

---

Notas técnicas finales
- No uses `@SuppressWarnings` para estos casos: la forma correcta es que los métodos/fields referenciados por FXML estén anotados con `@FXML` y tengan la firma correcta.
- El análisis está parcialmente limitado por el analizador local que no siempre detecta usos por reflexión (JavaFX). Por eso la validación final es:
  - Compilación limpia y
  - Revisión manual/automatizada por comparar fx:id ↔ @FXML.

