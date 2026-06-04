# Cumplimiento fiscal SIF / VERI*FACTU

Estado: base tecnica de cumplimiento incorporada, pendiente de validacion final con servicios y documentos tecnicos oficiales de AEAT.

## Referencias normativas

- Ley 58/2003, articulo 29.2.j: los sistemas deben garantizar integridad, conservacion, accesibilidad, legibilidad, trazabilidad e inalterabilidad.
- Real Decreto 1007/2023 y modificacion por Real Decreto 254/2025.
- Orden HAC/1177/2024: especificaciones tecnicas, funcionales y de contenido.
- AEAT, Sistemas Informaticos de Facturacion (SIF) y VERI*FACTU: informacion general y tecnica.

Fechas de adaptacion segun RD 254/2025:

- 2026-01-01: contribuyentes del Impuesto sobre Sociedades.
- 2026-07-01: resto de obligados tributarios afectados.

## Modalidades soportadas

### VERIFACTU

Requisitos operativos en la app:

- Configuracion activa de empresa.
- Modalidad SIF = `VERIFACTU`.
- NIF/CIF y razon social configurados.
- Nombre, version e identificador de instalacion del sistema.
- Declaracion responsable emitida e identificando version.
- Certificado disponible.
- Cliente SOAP AEAT disponible.
- Cadena de eventos fiscales valida.

### NO_VERIFACTU

Requisitos operativos en la app:

- Configuracion activa de empresa.
- Modalidad SIF = `NO_VERIFACTU`.
- NIF/CIF y razon social configurados.
- Nombre, version e identificador de instalacion del sistema.
- Declaracion responsable emitida e identificando version.
- Firma local disponible.
- Cadena de eventos fiscales de facturas valida.
- Cadena global de eventos del sistema valida.

## Diagnostico en codigo

`FiscalComplianceService.diagnosticar()` devuelve:

- `listoProduccion`: `true` solo si todos los controles aplicables pasan.
- `modalidad`: modalidad SIF activa.
- `checks`: lista de controles con codigo, estado y descripcion.

Este diagnostico debe ejecutarse antes de declarar una instalacion lista para produccion.

## Pendiente antes de certificacion/entrega final

- Validar XML, QR y envio contra los documentos tecnicos vigentes de AEAT.
- Generar una declaracion responsable imprimible/exportable por version.
- Bloquear emision real si `FiscalComplianceService` no marca listo.
- Probar contra MySQL real y entorno de pruebas AEAT.
- Documentar procedimiento de custodia de certificados, backups y restauracion.
