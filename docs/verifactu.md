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

## Conformidad con las especificaciones oficiales (julio 2026)

Los registros de facturacion se generan con el formato oficial de AEAT:

- **XML**: envoltorio `RegFactuSistemaFacturacion` con los esquemas `SuministroLR.xsd` /
  `SuministroInformacion.xsd` (tikeV1.0): `RegistroAlta` y `RegistroAnulacion` con `IDVersion`,
  `IDFactura`, `TipoFactura` (codigos F1/F2/R4/R5), `Desglose` por tipo impositivo,
  `Encadenamiento` (PrimerRegistro/RegistroAnterior), bloque `SistemaInformatico` completo,
  `FechaHoraHusoGenRegistro`, `TipoHuella` 01 y `Huella`.
- **Huella**: formula oficial de la Orden HAC/1177/2024 (concatenacion `campo=valor` con `&`,
  SHA-256 hexadecimal en mayusculas, huella anterior encadenada).
- **QR tributario**: URL oficial de cotejo `.../wlpl/TIKE-CONT/ValidarQR?nif=&numserie=&fecha=&importe=`
  (configurable con `verifactu.qr.base-url`; en dev apunta al entorno de pruebas `prewww2.aeat.es`).
- **SistemaInformatico**: configurable con `verifactu.sistema.*` (productor, id de sistema,
  numero de instalacion); si el productor es la propia empresa se usan sus datos fiscales.
- Los XSD internos (`xsd/verifactu-suministro-*.xsd`) son una transcripcion del esquema oficial
  para validacion estructural local: ante divergencias prevalece el XSD publicado por AEAT.

## Riesgo pendiente

La preparacion tecnica no sustituye la validacion formal con AEAT. Antes de operar con remision
real hay que validar contra el entorno de pruebas oficial con certificado:

- Aceptacion del XML por el servicio SOAP real (WSDL y cabeceras WS-Security).
- Respuestas y codigos de error de AEAT.
- Declaracion responsable del productor del software (art. 13 RRSIF), que es un tramite documental.
- Exportacion y conservacion de registros.
