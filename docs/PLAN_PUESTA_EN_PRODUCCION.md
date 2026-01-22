# Plan maestro: Poner la aplicación ERP en producción (guía paso a paso)

Resumen breve

Este documento es la guía paso a paso que voy a seguir para revisar, corregir y dejar la aplicación compilable, probada y preparada para producción sin dejar elementos pendientes. Está pensado para usarlo como checklist y guía de trabajo: yo lo seguiré y ejecutaré las acciones, y tú podrás seguir el progreso.

Checklist (rápido)

- [ ] 0. Preparación del entorno y perfiles (JDK, Maven, profiles de Spring)
- [ ] 1. Compilación y tests unitarios (corregir fallos de compilación y tests críticos)
- [ ] 2. Base de datos y migraciones (Flyway + scripts coherentes)
- [ ] 3. AEAT / Verifactu (firma SOAP, conformidad con AEAT)
- [ ] 4. Seguridad (PBKDF2 para passwords / migración)
- [ ] 5. UI: FXML, controllers y pruebas de carga FXML
- [ ] 6. Limpieza de warnings, eliminar @Autowired y duplicados
- [ ] 7. Impresión / PDF
- [ ] 8. Tests de integración y smoke tests end-to-end
- [ ] 9. Packaging, CI y despliegue (artifact, docs, secrets)
- [ ] 10. Auditoría final, checklist de normativa y documentación de producción

Contrato de trabajo (qué entrego)

- Inputs: código del repo actual, scripts SQL disponibles en `basesdedatos/` y `scripts/sql/`.
- Outputs:
  - Código corregido y limpio (constructor injection, sin duplicados críticos, tests verdes).
  - Migraciones Flyway colocadas y funcionales.
  - Cliente AEAT conforme a los requisitos (firma y estructura SOAP) con tests que lo validen.
  - Documentación (este archivo + README de despliegue mínimo).
  - Pipeline CI (ejemplo GitHub Actions) que construye, testa y empaqueta.
- Criterios de aceptación:
  - `mvn -DskipTests=false test` pasa (o se documenta cualquier test aislado que requiera infra externa).
  - `mvn package` genera el JAR ejecutable.
  - Tests de firma AEAT pasan contra mocks y validan estructura requerida.
  - FXMLs cargan en tests automatizados (sin excepciones).

Riesgos y casos límite

- Dependencias nativas de JavaFX en CI (requiere headless/test harness o uso del plugin `javafx-maven-plugin`).
- Scripts SQL desordenados que crean tablas en un orden que rompe constraints en H2 durante tests. Solución: Flyway con scripts ordenados.
- Certificados/keystores y secretos deben mantenerse fuera del repo; usar variables de entorno o secret manager.
- APIs internas sun.* prohibidas; uso de BouncyCastle y JDK APIs públicas.

Plan detallado (paso a paso)

Fase 0 — Preparación (mínimo 30–60 min)

1. Verificar JDK y Maven local (usar Java 17, Maven 3.8+). Ejecutar:

```bash
java -version
mvn -v
```

2. Ver perfiles de Spring: revisar `src/main/resources/application-*.properties` y `src/test/resources/application-test.properties` (ya existe y apunta a H2).

3. Asegurar `application-test.properties` tenga al menos:

```ini
spring.datasource.url=jdbc:h2:mem:erpdb;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.jpa.hibernate.ddl-auto=update
spring.jpa.defer-datasource-initialization=true
```

Fase 1 — Compilación y tests unitarios críticos (1–4 h)

Objetivo: dejar `mvn test` lo más verde posible.

Pasos:

1. Ejecutar localmente y recoger fallos:

```bash
mvn -DskipTests=false test
```

2. Corregir errores de compilación (métodos faltantes, imports, tipos). Acciones típicas:
   - Añadir métodos delegados en servicios si tests esperan compatibilidad (ej. `UsuarioService.contarActivos()`).
   - Evitar editar tests de comportamiento salvo para casos claros de fragilidad (y documentar).

3. Para tests que requieren keystore/certificados, crear fixtures en `src/test/resources/certs/` o generar keystore temporal en tests (ya hay ejemplo implementado para AEAT).

Criterio: paso a paso los tests críticos pasan. Documentar tests que dependen de infra externa y aislarlos con profiles.

Fase 2 — Base de datos y migraciones (2–6 h)

Objetivo: asegurar que el esquema se crea de forma determinista en dev/tests/prod.

Pasos:

1. Revisar si Flyway está presente en `pom.xml` (sí: dependencia `flyway-core`). Crear carpeta `src/main/resources/db/migration/` y añadir scripts numerados `V1__create_tables.sql`, `V2__...` según orden correcto.

2. Si existen SQL grandes en `basesdedatos/`, extraer y dividir en migraciones numeradas siguiendo dependencias (tablas padre antes de hijo).

3. Ajustar `application.properties` y `application-test.properties`:
   - `spring.flyway.enabled=true`
   - En prod: `spring.jpa.hibernate.ddl-auto=validate` (no crear tablas automáticamente)
   - En test: permitir flyway + `spring.jpa.hibernate.ddl-auto=none` o `validate` después de migración.

4. Ejecutar migraciones en un run local: `mvn -DskipTests=true flyway:migrate` (o arrancar la app con profile dev que ejecute Flyway).

Criterio: arrancan tests/contxtos JPA sin errores DDL.

Fase 3 — AEAT / Verifactu (firma SOAP) (1–2 días según detalle)

Objetivo: firma y request SOAP exactamente como exige AEAT.

Pasos:

1. Extraer la lógica de firma en `AeatSigner` (servicio testable).
2. Mantener `VerifactuAeatSoapClient` responsable solo de envelope y transporte (usa HttpClient).
3. Validar canonicalization (Exclusive C14N), transform chain y que `BinarySecurityToken` y `SecurityTokenReference` cumplan con AEAT.
4. Añadir tests:
   - Unit: XPath sobre el SOAP firmado (ya hay test `VerifactuAeatSoapClientTest`).
   - Integration: usar WireMock para simular AEAT y validar request completo (byte-to-byte ó canonicalized comparison si AEAT exige exactitud).

Criterio: tests de firma pasan; mock AEAT acepta el request de prueba.

Fase 4 — Seguridad: contraseñas (PBKDF2) (2–5 h)

Objetivo: reforzar `CifradoService`.

Pasos:

1. Reemplazar/implementar PBKDF2 con salt por usuario (o usar `PasswordEncoder` de Spring Security con PBKDF2).
2. Añadir estrategia de migración: detectar hashes antiguos y rehashear en el primer login.
3. Pruebas unitarias para vectores y timing aceptable.

Criterio: `UsuarioService` utilizará el nuevo `CifradoService` y tests de autenticación pasados.

Fase 5 — UI: FXML y controllers (3–8 h)

Objetivo: garantizar que cada FXML carga y sus controllers responden.

Pasos:

1. Añadir un test `FxmlLoadTest` que itere sobre `src/main/resources/ui/*.fxml` y haga `FXMLLoader.load()` con `controllerFactory` adaptado.
2. Corregir `fx:controller` y `@FXML` faltantes en controladores, y asegurar constructor-injection o `@Controller` con applicationContext.
3. Añadir tests unitarios para acciones críticas (guardar, eliminar).

Criterio: `FxmlLoadTest` pasa y no hay excepciones al abrir vistas principales.

Fase 6 — Limpieza de warnings, eliminar @Autowired y duplicados (2–6 h)

Objetivo: calidad de código y evitar suppresswarnings innecesarios.

Pasos:

1. Reemplazar `@Autowired` por constructor injection en todos los beans (IDE puede automatizarlo).
2. Ejecutar análisis estático (SpotBugs, Checkstyle) y resolver problemas críticos.
3. Eliminar duplicados en código (métodos repetidos, clases duplicadas) y documentar cambios.

Criterio: compilación sin errores y herramienta de análisis no reporta issues críticos.

Fase 7 — Impresión (1–3 h)

Objetivo: servicio que genere PDF y gestione impresión con fallback.

Pasos:

1. Revisar `ImpresionService` y usar iText o html2pdf para generar PDFs.
2. Añadir flag `printing.enabled=false` en CI para no intentar enviar a impresora.
3. Tests: comprobar generación de PDF (archivo en temp) y simulación de print spool.

Criterio: PDFs generados consistentemente en tests.

Fase 8 — Tests de integración & smoke tests (3–6 h)

Objetivo: comprobar flujos críticos end-to-end.

Pasos:

1. Crear tests de integración que simulen: login -> crear cliente -> crear factura -> enviar Verifactu (mock AEAT) -> verificar evidencia.
2. Añadir script `scripts/smoke_tests.bat` o job CI que ejecute estos tests.

Criterio: smoke tests pasan en entorno CI local.

Fase 9 — Packaging, CI y despliegue (2–5 h)

Pasos:

1. Configurar GitHub Actions (o similar) con jobs: build, test, static analysis, package.
2. Publicar artefacto a releases o dejar pipeline para empaquetado local.
3. Documentar variables de entorno necesarias para producción (DB url, keystore path & password, AEAT endpoint, logging level).

Criterio: pipeline verde y artefacto JAR listo.

Fase 10 — Auditoría final y docs (2–4 h)

Pasos:

1. Crear `docs/PRODUCCION.md` con instrucciones de despliegue, cómo instalar certificados, pruebas AEAT con mock y restauración de DB.
2. Lista de verificación de cumplimiento AEAT y seguridad.

Criterio: documento completo y validado.

Comandos clave (rápidos)

- Ejecutar tests:
```bash
mvn -DskipTests=false test
```
- Empaquetar:
```bash
mvn -DskipTests package
```
- Ejecutar migraciones Flyway (local):
```bash
mvn -DskipTests=true flyway:migrate
```
- Ejecutar JAR en perfil prod:
```bash
java -jar target/ERP-0.0.1.jar --spring.profiles.active=prod
```

Checklist final antes de marcar como "listo"

- [ ] Todos los tests unitarios e integración elementales pasan.
- [ ] Migraciones Flyway ordenadas y aplicables en entornos.
- [ ] AEAT firmado y validado con tests.
- [ ] UI FXMLs cargan sin excepción.
- [ ] Documentación completa de despliegue y pruebas AEAT.

Siguiente paso propuesto (elige una)

- Opción A: Empiezo por la base de datos (Flyway + reconciliación de scripts) — recomendable para estabilizar CI.
- Opción B: Empiezo por AEAT (firma SOAP) — recomendado si AEAT es requisito urgente de producción.
- Opción C: Empiezo por UI (FXML wiring y tests) — si tu prioridad es que la app de escritorio esté impecable.

Dime la opción y comienzo inmediatamente (ejecutaré los comandos relevantes, aplicaré cambios y volveré con resultados y commits).

---

*Archivo generado automáticamente en `docs/PLAN_PUESTA_EN_PRODUCCION.md`.*

