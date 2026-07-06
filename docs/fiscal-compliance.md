# Cumplimiento fiscal SIF / VERI*FACTU

Estado: base tecnica de cumplimiento incorporada, pendiente de validacion final con servicios y documentos tecnicos oficiales de AEAT.

## Referencias normativas

- Ley 58/2003, articulo 29.2.j: los sistemas deben garantizar integridad, conservacion, accesibilidad, legibilidad, trazabilidad e inalterabilidad.
- Real Decreto 1007/2023 (texto consolidado), modificado por el Real Decreto-ley 15/2025.
- Orden HAC/1177/2024: especificaciones tecnicas, funcionales y de contenido.
- AEAT, Sistemas Informaticos de Facturacion (SIF) y VERI*FACTU: informacion general y tecnica.

Plazos vigentes segun el texto consolidado del RD 1007/2023 (RD-ley 15/2025):

- 2027-01-01: obligados del articulo 3.1.a) del reglamento (normalmente contribuyentes del Impuesto sobre Sociedades).
- 2027-07-01: resto de obligados del articulo 3.1.

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
