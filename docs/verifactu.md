# VERI*FACTU

## Estado legal en la aplicacion

La aplicacion no trata VERI*FACTU como un simple interruptor. El flujo preparado es:

1. Configurar datos tecnicos y fiscales.
2. Probar certificado y comunicacion.
3. Iniciar funcionamiento VERI*FACTU con confirmacion expresa.
4. Mantener el estado vigente.
5. Si se renuncia, programar la renuncia para el 31 de diciembre del ano en curso.

Una vez iniciado, no se desactiva con un checkbox. Esto evita dejar el sistema en un estado legalmente incoherente.

## Variables

```properties
VERIFACTU_CERT_PATH=/secure/certs/verifactu.p12
VERIFACTU_CERT_PASSWORD=...
VERIFACTU_KEY_ALIAS=...
VERIFACTU_KEY_PASSWORD=...
VERIFACTU_AEAT_ENABLED=false
VERIFACTU_AEAT_ENDPOINT=https://...
```

El certificado debe estar fuera del repositorio.

## Prechequeo

```powershell
.\scripts\check-verifactu-production.ps1
```

El script comprueba:

- Variables obligatorias.
- Existencia del certificado.
- Alias dentro del PKCS12 mediante `keytool`.
- Endpoint cuando `VERIFACTU_AEAT_ENABLED=true`.

Para exigir que la remision AEAT este habilitada:

```powershell
.\scripts\check-verifactu-production.ps1 -RequireAeatEnabled
```

## Puesta en marcha

1. Rellenar datos fiscales de empresa.
2. Confirmar que el NIF/CIF de emisor es correcto.
3. Configurar certificado y endpoint.
4. Ejecutar el prechequeo.
5. Arrancar la aplicacion.
6. Entrar en VERI*FACTU.
7. Pulsar `Probar AEAT`.
8. Si la prueba es correcta y se han validado XML, QR y firma, pulsar `Iniciar VERI*FACTU`.

## Permisos

Permisos relevantes:

- `verifactu.ver`: acceso a la pantalla.
- `verifactu.enviar`: pruebas, inicio, renuncia y envio.
- `verifactu.exportar`: exportacion de evidencias.

## Evidencias

La aplicacion mantiene registros de:

- Alta de factura.
- Anulacion cuando aplica.
- Huella/hash.
- Hash anterior.
- XML generado.
- QR.
- Firma si hay certificado.
- Eventos de facturacion.

## Riesgo pendiente

La preparacion tecnica no sustituye la validacion formal con AEAT. Antes de operar con remision real hay que validar contra el entorno y especificaciones oficiales vigentes:

- XML.
- Esquemas XSD.
- Firma.
- QR/frase de factura.
- Endpoint.
- Respuestas de AEAT.
- Exportacion y conservacion de registros.
