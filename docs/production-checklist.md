# Checklist de produccion

Usa esta lista antes de emitir facturas reales o cargar datos definitivos.

## Sistema

- [ ] JDK 17, 21 o 23 instalado.
- [ ] `JAVA_HOME` apunta al JDK correcto.
- [ ] MySQL 8 instalado y accesible.
- [ ] Base de datos dedicada creada.
- [ ] Usuario MySQL de aplicacion creado sin permisos administrativos globales.
- [ ] Directorios de logs y backups creados y protegidos.
- [ ] Fecha, hora y zona horaria del equipo correctas.

## Configuracion

- [ ] `.env.production.local` creado desde `.env.production.example`.
- [ ] `SPRING_PROFILES_ACTIVE=prod`.
- [ ] `SPRING_DATASOURCE_URL` apunta a MySQL real.
- [ ] `SPRING_DATASOURCE_USERNAME` no es `root`.
- [ ] `SPRING_DATASOURCE_PASSWORD` no usa placeholders.
- [ ] `CIFRADO_AES_KEY` contiene una clave real.
- [ ] `SECURITY_PBKDF2_SECRET` contiene un secreto largo y real.
- [ ] `ADMIN_DEFAULT_PASSWORD` es temporal, largo y no reutilizado.
- [ ] No hay secretos reales en archivos versionados.

## Verificaciones tecnicas

- [ ] `.\scripts\check-production-env.ps1` termina sin errores.
- [ ] `.\scripts\check-verifactu-production.ps1` termina sin errores.
- [ ] `.\mvnw.cmd test` termina sin errores.
- [ ] `.\scripts\build-production.ps1` genera el JAR.
- [ ] `.\scripts\run-production.ps1` arranca con perfil `prod`.
- [ ] Las migraciones Flyway se aplican correctamente.
- [ ] `spring.jpa.hibernate.ddl-auto` queda en `validate`.
- [ ] El fallback H2 no esta habilitado en produccion.

## Primer acceso

- [ ] Entrar con usuario `admin`.
- [ ] Cambiar la contrasena inicial cuando la aplicacion lo solicite.
- [ ] Crear usuarios nominales; no operar a diario con `admin`.
- [ ] Asignar roles adecuados.
- [ ] Verificar que los menus visibles corresponden a los permisos del usuario.
- [ ] Desactivar usuarios que no deban acceder.

## Datos maestros

- [ ] Configurar datos fiscales completos de la empresa.
- [ ] Revisar NIF/CIF, razon social, direccion, codigo postal, municipio y provincia.
- [ ] Configurar series de facturacion.
- [ ] Crear clientes y proveedores de prueba.
- [ ] Crear articulos y revisar IVA/precios.
- [ ] Revisar almacenes y stock inicial.
- [ ] Configurar caja y bancos si se usan.

## Produccion y obrador

- [ ] Crear al menos una receta con ingredientes y alergenos indicados.
- [ ] Verificar BOM/escandallo de la receta y coste calculado.
- [ ] Crear orden de produccion de prueba (estado PLANIFICADA).
- [ ] Iniciar y finalizar orden de produccion.
- [ ] Registrar horneada asociada a la orden.
- [ ] Verificar calculo automatico de mermas y rendimiento.

## Seguridad alimentaria y APPCC

- [ ] Registrar el numero de RGSEAA en Configuracion > Datos Fiscales > Registro Sanitario.
- [ ] Añadir alergenos a cada articulo y receta (obligatorio UE 1169/2011).
- [ ] Registrar controles APPCC diarios: temperaturas de horneado, almacenamiento, etc.
- [ ] Verificar que los lotes incluyen numero de registro sanitario.
- [ ] Comprobar trazabilidad completa: insumo -> produccion -> lote -> entrega -> devolucion.

- [ ] Crear lotes para materias primas (harina, levadura, etc.).
- [ ] Crear lotes de producto terminado con relacion a lotes de insumo.
- [ ] Verificar trazabilidad hacia adelante (insumo → producto).
- [ ] Verificar trazabilidad hacia atras (producto → insumos).
- [ ] Verificar que los lotes proximos a caducar se muestran en rojo.
- [ ] Configurar numero de registro sanitario en los lotes.

## Reparto y distribucion

- [ ] Crear al menos un vehiculo de reparto.
- [ ] Crear ruta maestra con paradas de clientes.
- [ ] Generar hoja de ruta diaria desde ruta maestra.
- [ ] Iniciar ruta (estado EN_CURSO) y finalizarla.
- [ ] Confirmar entregas con/sin incidencia en hoja de ruta.
- [ ] Probar registro de cobro en entrega.

## Devoluciones

- [ ] Crear devolucion asociada a cliente y albaran/factura.
- [ ] Añadir lineas de devolucion con articulos.
- [ ] Aceptar y rechazar devoluciones.
- [ ] Verificar vinculo con lote en linea de devolucion.

## Nuevos modulos REST API

- [ ] Verificar que los endpoints REST responden (si se usa frontend web/movil):
  - `GET /api/produccion/recetas`
  - `GET /api/produccion/ordenes`
  - `GET /api/trazabilidad/lotes`
  - `GET /api/reparto/vehiculos`
  - `GET /api/reparto/hojas`
  - `GET /api/devoluciones`

## VERI*FACTU

- [ ] Certificado real instalado fuera del repositorio.
- [ ] `VERIFACTU_CERT_PATH` apunta al certificado.
- [ ] `VERIFACTU_CERT_PASSWORD` definido.
- [ ] `VERIFACTU_KEY_ALIAS` validado.
- [ ] `VERIFACTU_KEY_PASSWORD` definido.
- [ ] Endpoint AEAT validado para el entorno que corresponda.
- [ ] Pantalla VERI*FACTU muestra NIF de emisor correcto.
- [ ] `Probar AEAT` funciona.
- [ ] Se ha probado XML, QR, firma y envio en entorno de pruebas.
- [ ] Solo despues de validar todo, usar `Iniciar VERI*FACTU`.

## Backups y recuperacion

- [ ] Backup manual probado.
- [ ] Restauracion probada en una base de copia, no sobre la base real.
- [ ] Retencion configurada.
- [ ] Ubicacion de backups protegida y con copia externa.
- [ ] Procedimiento de restauracion documentado para el cliente.

## Cierre de salida

- [ ] Factura de prueba emitida y revisada.
- [ ] Anulacion o rectificacion probada si aplica.
- [ ] Exportacion de auditoria probada.
- [ ] Exportacion/evidencias VERI*FACTU probadas.
- [ ] Logs revisados tras el primer arranque.
- [ ] Plan de actualizacion definido.
- [ ] Responsable de backups y contrasenas identificado.
