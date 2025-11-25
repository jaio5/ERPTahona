Objetivo
Usar la base de datos MySQL real con los datos que tienes en `basesdedatos/definitivo/tahonaerp.sql` y arrancar la aplicación de escritorio JavaFX + Spring Boot apuntando a esa base de datos.

Resumen de acciones que harás (rápido)
1. Asegúrate de que MySQL/MariaDB está instalado y en ejecución.
2. Ejecuta el script PowerShell `import_tahona_sql.ps1` (se proporciona) para crear la base de datos `tahonaerp` e importar los datos.
3. Verifica las credenciales en `src/main/resources/application-dev.properties` o define variables de entorno: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD.
4. Ejecuta la aplicación desde tu IDE (recomendado) o con Maven (`mvn spring-boot:run -Dspring-boot.run.profiles=dev`).

Precauciones
- `application-dev.properties` por defecto en el proyecto apunta a:
  jdbc:mysql://localhost:3306/tahonaerp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  usuario: root
  contraseña: Iirne322*
  (Revisa y sustituye por tus credenciales si difieren).

- `spring.jpa.hibernate.ddl-auto=update` está activado en `application-dev.properties` — para entornos de desarrollo puede estar bien; si importas un esquema ya completo y no quieres que Hibernate modifique tablas, cambia a `none`.

- Flyway está incluido en dependencias. Si usas import SQL manualmente y no quieres migraciones automáticas por Flyway, añade `spring.flyway.enabled=false` en `src/main/resources/application-dev.properties`.

Pasos detallados
1) Comprobar MySQL
- En Windows, abre PowerShell y confirma que MySQL está corriendo. Por ejemplo (ajusta según tu instalación):

```powershell
# ver servicios
Get-Service | Where-Object { $_.Name -like "*mysql*" }

# o intentar conectar con el cliente mysql
mysql --version
```

2) Importar la base de datos (script incluido)
- Sitúate en la carpeta raíz del proyecto y ejecuta (desde PowerShell con privilegios apropiados):

```powershell
# Ejecutar script que se incluye en el repo
.\import_tahona_sql.ps1
```

- El script pedirá la contraseña del usuario MySQL cuando sea necesario. Si tu usuario/contraseña difieren, edita el script antes de ejecutarlo.

3) Verificar datos importados
- Con el cliente mysql:

```powershell
mysql -u root -p -e "USE tahonaerp; SELECT COUNT(*) FROM clientes;"
```

Sustituye `clientes` por la tabla que quieras comprobar.

4) Ejecutar la aplicación (recomendado: IDE)
- Desde IntelliJ/IDEA: Ejecuta la clase `ErpLauncher` o `ErpFxApplication` (según tu flujo). El `FXMLLoader` ya está configurado para usar Spring (setControllerFactory) y el perfil activo está `dev` (usa `application-dev.properties`).

- Alternativa desde Maven (puede saltarse reglas del enforcer):

```powershell
# Ejecuta la app con perfil 'dev'
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

Si el pom contiene reglas de enforcer que te bloquean al hacer `package`, `spring-boot:run` suele funcionar y no ejecuta el empaquetado; si falla, lo mejor es ejecutar desde IDE.

Notas finales y problemas conocidos
- Si no ves datos en la interfaz, comprueba:
  - que la app se esté ejecutando con el perfil `dev` (en consola se muestra el perfil activo). 
  - la URL/usuario/contraseña en `application-dev.properties` o variables de entorno.
  - que la importación del SQL finalizó sin errores (ver salida del script).

- Si quieres que me encargue de ajustar `application-dev.properties` (por ejemplo desactivar Flyway o fijar `ddl-auto=none`) o de arreglar el `pom.xml` para que `mvn package` sea exitoso, dímelo y lo hago.


Archivo de importación (script incluido): `import_tahona_sql.ps1` (usa este script para automatizar la creación e importación).
