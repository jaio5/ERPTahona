# VERI*FACTU en ERP Tahona: decisión de arquitectura y plan

Fecha: 20/10/2026. Rama: `fix/errores-conocidos`.
Lectores: Javier (desarrollador y productor del ERP), asesor fiscal de Grupo BABO y dirección de Grupo BABO, S.C.V.L.

Este documento resume el trabajo de un equipo de investigación (asesor normativo, análisis de mercado, auditoría de código, propuesta de decisión y dos revisiones independientes). No es asesoramiento jurídico: las conclusiones legales las tiene que confirmar el asesor fiscal.

**Cómo leer las marcas**

- **[V] Verificado**: leído en la fuente citada (BOE, AEAT, Hacienda, documentación o términos del proveedor, o el propio código del repositorio).
- **[I] Inferencia**: interpretación del equipo o dato no confirmado. Hay que validarlo antes de darlo por cierto.

---

## 1. Resumen de la decisión

Recomendamos **no terminar la implementación propia** del envío a la AEAT y, en su lugar, integrar en el ERP un **adaptador fiscal ("puerto") que, al emitir cada factura, llame de forma síncrona a un proveedor de API VERI*FACTU**, que genera el registro, devuelve la huella y el QR, y se encarga de remitir a la AEAT y de los reintentos. Proveedor preferente: **B2Brouter** (API de Tax Report); alternativa: **Verifacti**, si B2Brouter no entrega sus condiciones por escrito antes del **7/11/2026**. Si el proveedor no responde en el momento de emitir, la factura pasa a un estado de contingencia explícito y avisado (`INCIDENCIA_PROVEEDOR`), que no es el funcionamiento normal. El cliente SOAP propio queda desactivado. La remisión real empieza el **1/1/2027**, sin arranque anticipado. Además, **esta misma semana** hay que contener dos problemas que ya existen en el ERP con independencia de VERI*FACTU: muestra estados fiscales falsos ("enviado", "verificado") y las rectificativas anulan la factura original, lo que resta dos veces en el libro de IVA y en el modelo 347. Con cualquier opción, Javier sigue siendo productor del ERP y debe firmar su declaración responsable; lo que cambia es cuánto tiene que garantizar él directamente.

---

## 2. Situación legal

### 2.1 Fechas en vigor hoy

- [V] El Real Decreto-ley 15/2025 (BOE 03/12/2025) modificó la disposición final 4ª del RD 1007/2023:
  - Contribuyentes del Impuesto sobre Sociedades (art. 3.1.a del Reglamento, RRSIF): sistemas adaptados **antes del 1/1/2027**.
  - Resto de obligados (autónomos, etc.): **antes del 1/7/2027**.
- [V] El texto consolidado del RD 1007/2023 en el BOE tiene como última actualización la del 03/12/2025. No hay norma posterior que lo modifique.
- [V] Para los productores de software la obligación de ofrecer productos adaptados rige desde el **29/07/2025** (9 meses desde la entrada en vigor de la Orden HAC/1177/2024, DF 4ª del RD 1007/2023). [I, fuentes secundarias] Ni el RDL 15/2025 ni el anuncio de 2026 la cambian; hay que confirmarlo en el texto consolidado del BOE.
- [I, muy probable] Grupo BABO, como cooperativa valenciana (S.C.V.L.), tributa por el Impuesto sobre Sociedades (régimen de la Ley 20/1990), así que **su fecha es el 1/1/2027**. Lo debe confirmar el asesor.
- [V] Excepción: el Reglamento no se aplica a quien lleva los libros por el SII (art. 3.3). [I] Con decenas de facturas al mes es improbable que BABO esté en el SII, pero hay que confirmarlo.
- [V] `docs/plan-lanzamiento.md` da como plazo el 01/07/2027, que es incorrecto para Grupo BABO.

### 2.2 Aplazamiento anunciado y **no publicado**

- [V] El 5/10/2026 Hacienda publicó la nota "Previsión de modificación de los plazos…", en la que prevé "proceder al aplazamiento de las obligaciones pendientes de aplicación del Real Decreto 1007/2023 … hasta octubre de 2028", para alinearlo con la factura electrónica obligatoria de quienes facturan 8 M€ o menos. Dice también que los requisitos de integridad, trazabilidad, inalterabilidad, etc. "se mantendrán en términos sustancialmente equivalentes".
- [V] A 20/10/2026 **no hay ninguna norma en el BOE** que lo haga efectivo. Sigue vigente la redacción del RDL 15/2025.
- [V] Las fuentes no coinciden en el alcance: algunas dicen que afecta a todos y otras que solo a empresas y autónomos de hasta 8 M€. La nota habla de "obligaciones pendientes" en general y no concreta si las sociedades pasan también a 2028.
- [I, fuente secundaria, no comprobado en el BOE ni enlazado en el apartado 11] La Orden HAC/1028/2026 (BOE 05/10/2026) regula la factura electrónica B2B: 6/10/2027 para facturaciones de más de 8 M€ y 6/10/2028 para el resto. No modifica VERI*FACTU.
- [I] **Planificamos para el 1/1/2027** y tratamos 2028 como posible margen solo cuando salga en el BOE. Aun con aplazamiento, la fase de contención (F0) y el núcleo (F1) se hacen igualmente, porque corrigen problemas que ya existen hoy.

### 2.3 Quién es productor y qué se arriesga

- [V] La declaración responsable (DR) la emite el productor, por escrito, visible en el propio sistema, **una por versión** ("cada versión, por pequeña variación que introduzca, es un producto distinto"). Un software propio de uso interno, no comercializado, también la necesita. No hay registro previo ni certificación externa (art. 13 RRSIF; FAQ de certificación de la AEAT).
- [V] Su contenido está fijado en el art. 15 de la Orden HAC/1177/2024, con el título literal «DECLARACIÓN RESPONSABLE DEL SISTEMA INFORMÁTICO DE FACTURACIÓN» y los datos a) a l) en ese orden (nombre, código identificador, versión, componentes, S/N solo VERI*FACTU, S/N multiobligado, tipos de firma, productor con NIF y dirección postal, manifestación de cumplimiento, fecha y lugar).
- [V] Si el sistema tiene componentes de varios productores, "todas ellas deberán aportar las correspondientes declaraciones responsables de sus componentes" (art. 15.4 de la Orden). Según la FAQ de desarrolladores de la AEAT (v1.3, aclaración nº 5), la DR del componente principal "debería indicar que invoca indefectiblemente dichos CF (con mención expresa a la versión concreta usada de esos CF), y también cómo lo hace, cuándo y para qué".
- [I] Conclusión: **ningún proveedor de API asume la condición de productor del ERP** mientras el ERP numere y emita facturas. Javier (o ReseteaTek) es productor del componente principal; el proveedor es productor de su componente y emite su propia DR. Delegar reduce la superficie técnica que Javier tiene que garantizar, pero no le quita la condición de productor.
- [V] Sanciones del art. 201 bis de la LGT (infracción grave):
  - Productor: **150.000 €** por ejercicio con ventas y por cada tipo de sistema no conforme; **1.000 €** por cada sistema sin certificar (letra f).
  - Usuario (BABO): **50.000 €** por ejercicio por tener sistemas no certificados o alterados.
  - Las multas son fijas, sin graduación por tamaño.
- [V] Delegar el cumplimiento material en un tercero "no exime al obligado a expedir facturas de la responsabilidad".
- [V] Los contratos de los proveedores no trasladan estas sanciones: Verifacti limita su responsabilidad al "correcto funcionamiento técnico" (cláusula 9 de sus términos).

### 2.4 Requisitos técnicos clave (resumen)

- [V] Modalidad **VERI*FACTU**: se remiten en línea todos los registros. No se exige firma de los registros, ni registro de eventos, ni conservación y exportación del art. 8, ni reloj con margen de 1 minuto. La opción se ejerce al empezar a remitir y **dura al menos hasta el 31/12 de ese año** (art. 16.5 RRSIF). Está permitido producir un sistema que solo funcione como VERI*FACTU. La AEAT la considera "preferible".
- [V] Modalidad NO VERI*FACTU: exige además firma XAdES, registro de eventos encadenado, conservación y exportación, etc. **No se implementa.**
- [V] Art. 9 del RD 1007/2023: el registro de alta se genera "de forma simultánea o inmediatamente anterior a la expedición" de cada factura.
- [V] Huella SHA-256 (hexadecimal, mayúsculas, 64 caracteres) sobre `campo=valor&…` con los campos y el orden de la especificación oficial v0.1.2. Una única cadena por obligado tributario y sistema (art. 7.c de la Orden).
- [V] QR de 30x30 a 40x40 mm, nivel de corrección M, texto "QR tributario:" encima y, debajo, «Factura verificable en la sede electrónica de la AEAT» o «VERI*FACTU».
- [V] Remisión (art. 16 de la Orden): hasta 1.000 registros por envío, espera inicial de 60 s, reintento al menos cada hora si hay incidencia, aviso de registros pendientes. Un corte de la AEAT no obliga a dejar de facturar (FAQ VERI*FACTU nº 8), pero esa FAQ se refiere a la AEAT, no a un componente propio del sistema.
- [V] Errores: los previstos en el Reglamento de facturación se corrigen con **factura rectificativa**; un error solo en datos del registro, con un registro de **subsanación**; la **anulación** es para facturas emitidas por error y debe ser "muy poco frecuente".
- [V] Remisión por terceros: el tercero debe estar apoderado (registro de apoderamientos) o actuar como colaborador social (Convenio 017 para empresas de software, con autorización del Anexo I de la Resolución de la AEAT de 18/12/2024; FAQ de colaboración social). No basta con aceptar condiciones del servicio. [Falta enlazar la Resolución de 18/12/2024 en el apartado 11.]

---

## 3. Por qué no terminar la implementación propia

La auditoría del código (solo lectura) encontró una base aprovechable: el XML valida contra los XSD oficiales, la URL del QR es correcta, hay protección XXE, inalterabilidad de facturas emitidas y una guardia que impide enviar al entorno de pruebas desde producción. Pero **hoy no está lista para remitir y, si se activara tal cual, cumpliría peor que no tener VERI*FACTU**. Hallazgos críticos:

| # | Hallazgo | Dónde | Estado |
|---|---|---|---|
| 1 | **Huella con nombres de campo erróneos**: usa `NumSerieFacturaEmisor` y `FechaExpedicionFacturaEmisor` en lugar de `NumSerieFactura` y `FechaExpedicionFactura`. Con el vector oficial del caso 1, los nombres oficiales dan `3C464DAF…F12F60` y los del código `314E5A6A…`. Todas las huellas de alta serían incorrectas. El test copia la misma fórmula y no lo detecta. | `VerifactuService.java:970-972`; `VerifactuServiceTest.java:157-166` | [V] comprobado con sha256sum |
| 2 | **El cliente SOAP no se autentica con el certificado (mTLS)**: el `HttpClient` no usa `SSLContext` ni `KeyManager`. En su lugar añade una cabecera WS-Security con `mustUnderstand=1` que el servicio no pide. | `VerifactuAeatSoapClient.java:358-379, 166-353` | [V] código; [I] el SOAP Fault es probable |
| 3 | **Lee la respuesta con búsquedas de texto sin namespaces** (`<EstadoRegistro>Correcto</EstadoRegistro>`), cuando la AEAT responde con prefijos. Nunca coincide, devuelve `RESPUESTA_INESPERADA` y se trata como OK: **un registro Incorrecto se daría por aceptado**. No guarda CSV, `TiempoEsperaEnvio` ni trata el duplicado. | `VerifactuAeatSoapClient.java:418-441`; `VerifactuService.java:632` | [V] |
| 4 | **Factura "en limbo"**: la remisión es síncrona dentro de la transacción de emisión. Un error o timeout hace rollback; si la AEAT llegó a registrarla, el reintento genera otro registro que la AEAT rechaza por duplicado. Además la emisión, y con ella la TPV, se bloquea si la AEAT no está disponible. | `FacturaService.java:168-195`; `VerifactuService.java:624-662`; `FiscalComplianceService.java:57`; `TpvService.java:103` | [V] |
| 5 | **La rectificativa anula la original**: la marca ANULADA y genera un registro de anulación (que además nunca se envía). Como el libro de IVA, el modelo 347 y la cartera excluyen las ANULADA, **hoy la operación se resta dos veces**. El endpoint `anular-rectificativa` no comprueba el estado. | `FacturaService.java:223-309`; `LibroIvaService`, `Modelo347Service`, `CarteraService` | [V] |

> **Estado tras la contención F0 (rama `fix/errores-conocidos`, 20/10/2026)**: el hallazgo 3 ya no da por aceptada una respuesta no reconocida (queda `RESPUESTA_NO_CONFIRMADA`, sin `verifactuEnviada`); el hallazgo 5 está contenido en el código (la rectificativa no anula la original y se retiró `anular-rectificativa`), pero los datos ya existentes siguen pendientes de inventario y reparación. Tampoco se dan ya los puntos de "Reenviar"/"Verificar" ni el de la remisión desactivada: ahora queda `NO_REMITIDO`, sin `verifactuEnviada` y sin leyenda. Las referencias de línea de esta tabla son de antes de F0.
>
> **Desviación respecto al plan**: en F0 la original de una rectificativa se queda `EMITIDA` y no pasa a `RECTIFICADA` (apartados 6.6, 6.8.6 y 7), porque `LibroIvaService` y `Modelo347Service` solo cuentan facturas `EMITIDA` y con `RECTIFICADA` la original saldría de los libros. Pasar a `RECTIFICADA` exige primero incluir ese estado en esos servicios (F1).

Otros hallazgos de severidad alta o media [V salvo indicación]:

- Una cadena por serie (GEN, TPV…) en lugar de una única cadena por obligado; varios `PrimerRegistro=S`.
- Encadenamiento sin bloqueo: dos emisiones simultáneas (TPV y oficina) pueden bifurcar la cadena.
- Sin control de flujo, sin cola, sin reintentos, sin subsanación.
- "Reenviar" y "Verificar" en la pantalla de evidencias cambian el estado a ENVIADO/VERIFICADO **sin llamar a la AEAT** (`VerifactuEvidenceService.java:86-118`).
- Con el envío desactivado, la factura queda EMITIDA con `verifactuEnviada=true`, evidencia "ENVIADO" y leyenda VERI*FACTU en el PDF sin haber enviado nada.
- Destinatarios: una F2 con cliente o una F1 sin cliente serían rechazadas; no hay `IDOtro` para extranjeros; siempre R4 (no hay R5 para rectificar tickets); falta `ImporteRectificacion`; régimen fijo 01/S1 sin recargo de equivalencia ni exentas.
- Zona horaria [I]: sin TZ en el contenedor, probablemente UTC; facturas fechadas el día anterior entre las 00:00 y la 01:00/02:00 de Madrid.
- QR de unos 21,7 mm (mínimo 30 mm), sin "QR tributario:" y con una leyenda no literal a 7 px.
- Declaración responsable que no sigue el art. 15 y que firma "la entidad usuaria" en lugar del productor.
- Evidencias borrables y modificables; consulta de estado con el namespace del SII; numeración con huecos (el número se asigna en BORRADOR y F/R comparten contador).
- Los tests no usan los vectores oficiales de la AEAT y los de huella son tautológicos.
- [I] Sin certificado y con la remisión desactivada, `FiscalComplianceService` no debería permitir emitir en ninguna modalidad. Si producción está emitiendo, alguna configuración difiere de lo que se lee en el código: **hay que preguntar la configuración real de producción**.

**Esfuerzo para terminarla** [I, estimación del equipo]: 22-32 días-persona (4-6 semanas de una persona), más el trámite del certificado y las pruebas en el entorno de la AEAT. Quedan unas 10 semanas hasta el 1/1/2027: viable, pero sin holgura, y con Javier como responsable de todo el sistema ante el art. 201 bis.

---

## 4. Comparativa de opciones

| Criterio | A) Terminar implementación propia (SOAP directo) | B1) B2Brouter (Tax Report API) | B2) Verifacti (API REST) | **C) Puerto único + proveedor síncrono, SOAP propio desactivado (RECOMENDADA)** |
|---|---|---|---|---|
| Riesgo del productor (Javier) | Máximo: responde de huella, cadena, XML, mTLS, flujo y respuestas. Hoy hay 5 bloqueantes [V] | Medio-bajo: sigue siendo productor del componente principal y firma su DR citando el componente [V]. B2Brouter ha presentado su DR [V] | Medio-bajo: arquitectura mixta explícita [V]. Cl. 9: responsabilidad limitada a corregir el defecto o abonar parte de la cuota [V] | Igual que B1/B2, y además puede cambiar de proveedor sin rehacer el ERP; pruebas con vectores oficiales y evidencias documentan la diligencia |
| Riesgo del cliente (BABO) | 50.000 €/ejercicio si no es conforme; con 5 bloqueantes, alto [V] | Bajo si la DR y la integración son correctas | Igual que B1 | Igual que B1; no se traslada al proveedor [V] |
| Continuidad al expedir | Hoy la TPV se bloquea si la AEAT no responde [V] | Depende del proveedor al expedir (art. 9); las caídas de la AEAT las absorbe su cola | Igual; devuelve QR y huella al momento [V] | Llamada de 5 s y un reintento; si cae el proveedor, `INCIDENCIA_PROVEEDOR` avisada y procedimiento validado por el asesor |
| Seguridad y datos | mTLS y custodia del certificado de BABO en el servidor, con secretos en claro en Coolify | Colaborador social: no hay certificado de BABO que custodiar [V]. ISO 27001 [V]; ENS sin fuente enlazada [I]. Ubicación de datos, DPA y exportación de cadena no publicados [I] | DPA (cl. 14) [V]; datos en el EEE pero la cl. 6.7 permite acceso de soporte desde fuera [V]; sin cláusula de exportación en los términos [V] | Clave de API en cuenta de BABO con acceso de emergencia; webhook con HMAC o solo polling; usuario de BD sin DDL; auditoría |
| Coste directo primer año | 0 € de licencia + certificado de representante (unos 14 € + IVA [I]) | App 110 €/año; integración "desde 348 €" + 800 € de alta [V parcial]; pedir precio cerrado | 13,49 €/mes en plan anual (unos 162 €) + 2,90 € por la representación [V] | El del proveedor: entre unos 165 € y unos 1.150 € |
| Esfuerzo de Javier | 22-32 días-persona | 15-20 días-persona [I] | 15-20 días-persona [I] | 15-20 días-persona [I] (el puerto añade ~1 día) |
| Plazo frente al 1/1/2027 | Inviable con holgura | Viable si contestan antes del 7/11 | Viable ya: términos públicos y sandbox gratuito [V] | Viable, con corte el 21/11 |
| Dependencia / cambio de proveedor | Ninguna | Alta: activación irreversible [V]; no consta que admita continuar una cadena importada [I] | Media-alta: la cadena vive en el proveedor [V]; no consta importación [I] | Contenida en el código. El cambio a mitad de cadena **no está resuelto**: exigirlo por escrito o asumir cerrar una cadena y abrir otra |
| Mantenimiento ante cambios de la AEAT | Alto | Bajo | Bajo | Bajo |

Opciones descartadas:

- **A** (terminar la implementación propia): 5 bloqueantes críticos y 22-32 días-persona sin holgura.
- **D** (sacar toda la facturación a un SaaS como Holded, FacturaDirecta, Invopop o la Invoice API de B2Brouter): duplica los datos maestros, rompe la TPV y [I] hay riesgo de que la DGT siga considerando sistema de facturación al ERP si calcula y procesa datos de facturación (consultas V2653-24 y V0058-25, citadas en la fuente del EIC). Sería la única vía para que Javier deje de ser productor, y habría que validarla con el asesor o con consulta vinculante.
- **Variante "generación local + remisión por el proveedor"** (el ERP genera la huella y B2Brouter solo remite el XML): elimina la dependencia de red al expedir, pero devuelve a Javier la responsabilidad de la huella y la cadena, no está verificado que B2Brouter conserve la huella del ERP y Verifacti no lo ofrece. Queda como pregunta escrita y como opción para 2027.

Otros proveedores evaluados (resumen del estudio de mercado):

| Proveedor | Nota principal |
|---|---|
| fiskaly SIGN ES | Colaborador social, sandbox gratuito, ISO 27001 [V]; precio solo bajo presupuesto y DR no pública; probablemente sobredimensionado para este volumen [I] |
| Invopop | Numera, firma y remite él (más cerca de ser el sistema de facturación) [I]; plan Dev gratuito y Pro 500 €/mes [V]; obliga a modelar en GOBL; plan gratuito sin SLA confirmado [I] |
| Quaderno | Exige **subir el certificado de la cooperativa** al proveedor y la activación es irreversible [V]; orientado a e-commerce |
| Holded / FacturaDirecta | Son facturadores completos, no APIs VERI*FACTU; la API de Holded no expone el estado ante la AEAT [V] |

---

## 5. Proveedor preferente y alternativa

### 5.1 B2Brouter (preferente)

- [V] Colaborador social de la AEAT: el cliente no necesita certificado propio, basta con autorizarle.
- [V] Ha presentado su declaración responsable. [Falta enlazar la DR concreta en el apartado 11; pedirla por escrito, punto 5.3.4.]
- [V] ISO 27001 (fuente: blog de B2Brouter, apartado 11). [I] ENS: no hay fuente enlazada; confirmarlo. Operador de factura electrónica (Peppol, FACe).
- [V] Tax Report API (JSON o XML VeriFactu) e Invoice API; devuelve estado, QR en Base64 y el XML con la respuesta de la AEAT.
- [V] Sandbox y staging (`api-staging.b2brouter.net`) contra el entorno de pruebas de la AEAT.
- [V] La activación es **irreversible** y no retroactiva.
- [I] Precio exacto de la API para un ERP propio, SLA, DPA, ubicación de datos, quién genera la huella en Tax Report y exportación de la cadena: **no publicados**.

### 5.2 Verifacti (alternativa)

- [V] API REST pensada para ERPs propios: `Idempotency-Key`, `create_bulk`, `export`, `downloadXML`, webhooks; devuelve QR, UUID y huella.
- [V] 14,99 €/mes por NIF (13,49 € en anual), hasta 3.000 facturas al mes; firma online del formulario de representación 2,90 € + IVA.
- [V] Términos públicos: SLA de 8 h laborables para fallos críticos con penalizaciones de hasta el 100% de la cuota; DPA (cl. 14); datos en el EEE con posible acceso de soporte desde fuera (cl. 6.7), sin lista de subencargados.
- [V] Empresa de 2024; horario de soporte incoherente en los términos (L-V 9-15 en una cláusula, L-J 9-13 en otra).
- [V, en la FAQ, no en los términos] Tras la baja, los XML se borran a los 30 días salvo el último. [I] No consta si figura como colaborador social formal.

### 5.3 Qué hay que pedir **por escrito** a los dos (antes del 7/11/2026)

1. Contrato y precio cerrado para este volumen (decenas de facturas al mes, un NIF).
2. DPA (art. 28 RGPD) firmado con **Grupo BABO** como responsable, lista de subencargados, ubicación de los datos y garantías de transferencias fuera del EEE.
3. SLA y disponibilidad histórica de la API.
4. Su declaración responsable y la versión del componente que hay que citar en la DR del ERP.
5. **Cómo cumplen el art. 9 del RD 1007/2023 si su API no responde** en el momento de expedir.
6. Exportación completa de la cadena (registros, huellas, XML y respuestas de la AEAT) en cualquier momento y a la baja.
7. Si admiten **continuar una cadena iniciada en otro sistema** (importar el registro anterior).
8. En B2Brouter (Tax Report): quién genera la huella y, si se le envía XML, si conserva la huella del ERP o reencadena.
9. Firma de los webhooks (HMAC o secreto) o confirmación de que basta con polling.
10. Vía de remisión: colaboración social o representación (Anexo I), con la cuenta y la autorización **a nombre de Grupo BABO** y acceso de emergencia.

Requisito del hito: cuenta, autorización y contrato a nombre de Grupo BABO. Si B2Brouter no lo cumple el 7/11, se elige Verifacti.

---

## 6. Diseño técnico

### 6.1 Puerto fiscal

Paquete `alicanteweb.erp.fiscal`:

```java
interface FiscalReportingPort {
    RegistroGenerado generarRegistroAlta(RegistroFiscal r, FacturaFiscalDto f); // SÍNCRONO, timeout 5 s: huella, id, QR, estado inicial
    RegistroGenerado generarRegistroAnulacion(RegistroFiscal r);
    EstadoRemoto consultar(RegistroFiscal r);   // polling del estado ante la AEAT, que gestiona el proveedor
    String idComponente();                      // nombre y versión, para la declaración responsable
}
```

Implementaciones, elegidas con `fiscal.reporting.provider`:

- `B2BrouterReportingAdapter` o `VerifactiReportingAdapter` (`java.net.http`).
- `AeatSoapReportingAdapter`: desactivado; falla si se activa sin el indicador experimental.
- `NoopReportingAdapter`: antes de la fecha de obligación; marca `NO_REMITIBLE`, sin QR ni leyenda.
- `FakeReportingAdapter`: solo en tests.

### 6.2 Flujo de emisión (llamada síncrona)

El diseño inicial usaba una cola de salida asíncrona. La revisión lo rechazó porque el registro lo generaría el proveedor minutos u horas después de imprimir la factura, lo que choca con el art. 9 del RD 1007/2023 [V]. Las arquitecturas mixtas publicadas funcionan de forma síncrona: el proveedor genera el registro y devuelve el QR al momento [V, DR de Billin/Bilbabit]. Flujo resultante de `aprobarYEmitir`:

1. **TX1**: asigna el número bajo bloqueo `PESSIMISTIC_WRITE` sobre la secuencia `(prefijo, serie, ejercicio)` (los borradores dejan de consumir número), inserta `registro_fiscal` en estado `GENERANDO` y hace commit.
2. **Fuera de transacción**: `port.generarRegistroAlta` con timeout de 5 s y un reintento con la misma `Idempotency-Key` (el id del registro).
3. **TX2 si va bien**: guarda huella, referencia del proveedor y QR; la factura pasa a EMITIDA y **solo entonces** se imprime con la leyenda VERI*FACTU.
4. **TX2 si falla**: la factura pasa a `INCIDENCIA_PROVEEDOR` y el registro a `PENDIENTE_GENERAR`.
5. Un worker `@Scheduled` reintenta cada minuto, en orden de secuencia y sin adelantar el siguiente. Como la clave de idempotencia es el id, un timeout que el proveedor sí procesó no crea duplicado.
6. La remisión a la AEAT, el control de flujo (60 s / 1.000 registros) y los reintentos horarios del art. 16 los hace el proveedor. El ERP consulta el estado por polling o por webhook firmado con HMAC.
7. En modo proveedor el ERP **no genera huella ni XML propios**.

### 6.3 Contingencia `INCIDENCIA_PROVEEDOR`

- Es una excepción avisada, no el modo normal: se registra el incidente, se muestra un banner y se envía un correo.
- La venta queda guardada, pero la factura no se expide como VERI*FACTU hasta que el proveedor genera el registro, en orden, en cuanto vuelve.
- **No se apoya en la FAQ VERI*FACTU nº 8**, que cubre la caída de la AEAT, no la de un componente propio del sistema.
- [I] Qué se entrega al cliente durante la caída (justificante no fiscal y factura simplificada en cuanto se pueda) es una cuestión del Reglamento de facturación (RD 1619/2012, expedición en el momento de la operación) que **decide el asesor**. No se da por resuelto.
- Riesgo reconocido: con un proveedor, la TPV deja de depender de la AEAT pero pasa a depender del proveedor en el momento de expedir.

### 6.4 Evidencias

- La evidencia de cada registro es **lo enviado y lo recibido del proveedor**: payload, respuesta, XML y huella devueltos, CSV y estado remoto. No se genera un XML local paralelo con una huella propia que no coincidiría con la registrada en la AEAT.
- `verifactu_evidence` queda **congelada como histórico sin enlazar** a la cadena nueva: tiene huellas incorrectas, una cadena por serie y nunca se remitió.
- La primera remisión real lleva `PrimerRegistro=S`. Las facturas anteriores a la obligación quedan `NO_REMITIBLE`, sin QR ni leyenda.
- La huella local se corrige (`VerifactuService.java:970-972`) solo para un futuro adaptador SOAP, con tests sobre los vectores oficiales `3C464DAF…`, `F7B94CFD…` y `177547C0…`. Esos vectores también sirven para comprobar la huella que devuelve el proveedor.

### 6.5 Inmutabilidad y datos

Migración `V52__registro_fiscal.sql` (la última existente es V51):

- `registro_fiscal`: `id` UUID (también clave de idempotencia), `obligado_nif`, `secuencia` con `UNIQUE(obligado_nif, secuencia)`, `factura_id`, tipo ALTA/ANULACION, `subsanacion`, `rechazo_previo`, `primer_registro`, `fecha_hora_gen`, `payload_json`, `qr_url`; `huella`, `huella_anterior`, `csv` y `proveedor_ref` **escribibles una sola vez** (de NULL a valor); `xml_proveedor`, `respuesta_raw`, `estado_remoto`, `estado`, `intentos`, `proximo_intento`, `ultimo_error`, `proveedor`, `created_at`.
- Triggers: `BEFORE UPDATE` bloquea las columnas de contenido y cualquier cambio de un valor no nulo en las de escritura única; `BEFORE DELETE` siempre lo prohíbe.
- Usuarios de base de datos: el de la aplicación sin `TRIGGER`, `ALTER` ni `DROP`; Flyway con usuario propio. [I] Verificar `log_bin_trust_function_creators` y el `DEFINER` en la restauración (`BackupService` exporta con `--triggers`). Se prueba con Testcontainers MySQL; H2 no sirve.
- `auditoria_fiscal`, de solo añadir: anulaciones, cambio de proveedor o de clave, cambio de `FISCAL_REPORTING`, reintentos manuales.
- Se mantiene la inalterabilidad de `Factura` (`@PreUpdate/@PreRemove`, `ESTADOS_INALTERABLES` con `RECTIFICADA`).
- Conservación: al menos el plazo de prescripción tributaria y mercantil (lo fija el asesor). Copias de seguridad cifradas.

Estados:

- Factura: `BORRADOR` (sin número) → `REVISION` → `EMITIDA` | `INCIDENCIA_PROVEEDOR` → `EMITIDA`; después `RECTIFICADA` o `PAGADA`. `ANULADA` solo por error de emisión, con permiso de administrador y motivo.
- Registro fiscal: `GENERANDO` → `GENERADO` → `REMITIDO` → `ACEPTADO`; `ACEPTADO_CON_ERRORES` crea tarea de subsanación; `RECHAZADO` se corrige con un registro nuevo `Subsanacion=S` y `RechazoPrevio=X`; `PENDIENTE_GENERAR` (incidencia del proveedor); `DUPLICADO` con los mismos datos se trata como aceptado; `NO_REMITIBLE`.

### 6.6 Qué se retira

- La llamada SOAP síncrona dentro de `@Transactional`.
- `AEAT_DISPONIBLE` y `FIRMA_LOCAL` en `FiscalComplianceService`, que pasa a exigir "proveedor configurado y declaración de la versión actual".
- `reenviarEvidencia`, `verificarEstadoAEAT` y `deleteById` de evidencias.
- `registrarEvidenciaAEAT`, la firma RSA en bruto, NO VERI*FACTU y el checkbox de renuncia (`TipoUsoPosibleSoloVerifactu=S`).
- El hash `FALLBACK` y la consulta con el namespace del SII.
- La anulación de la original en `crearRectificativa`: la original pasa a `RECTIFICADA`.

### 6.7 Seguridad

- Clave de API como secreto de Coolify, nunca en logs; rotación documentada; cuenta a nombre de BABO con acceso de emergencia.
- Webhook con firma HMAC, o solo polling.
- Egress restringido al host del proveedor, con validación TLS estándar.
- Payload y XML (con NIF y nombres de destinatarios) cifrados en las copias de seguridad.

### 6.8 Pruebas

1. Vectores oficiales de huella frente a la devuelta por el proveedor.
2. WireMock con respuestas reales del sandbox: Correcto, AceptadoConErrores, Incorrecto, duplicado, 401, 429, 5xx y timeout.
3. Concurrencia TPV + oficina: sin huecos ni bifurcación.
4. Numeración: borrar borradores no deja huecos; F y R con contadores separados.
5. Caída del proveedor → `INCIDENCIA_PROVEEDOR` → recuperación en orden y sin duplicados.
6. Rectificativa: la original pasa a `RECTIFICADA` y el libro de IVA y el 347 no restan dos veces.
7. Triggers e inmutabilidad en Testcontainers MySQL con binlog.
8. Reglas de negocio: F2 sin destinatarios, R5, `ImporteRectificacion`, recargo.
9. Leyenda solo con registro generado y modalidad VERI*FACTU.
10. Extremo a extremo en sandbox/staging desde una demo **reconstruida desde esta rama** en modo VERIFACTU, con polling (la demo solo escucha en 127.0.0.1 y no recibe webhooks) y [I] un NIF válido para el entorno de pruebas. La demo actual (`~/tahona-demo`) se construye desde otro código y funciona en NO_VERIFACTU con certificado autofirmado, así que no prueba esta rama. Nunca se prueba en producción.

---

## 7. Plan por fases

| Fase | Trabajo | Responsable | Fechas |
|---|---|---|---|
| **F0 Contención, inventario y peticiones** (no depende del proveedor) | 1) La leyenda deja de depender de `verifactuEnviada`; no se marca enviado sin remitir. 2) Desactivar "Reenviar", "Verificar" y el borrado de evidencias. 3) Rectificativa: no anular la original, usar `RECTIFICADA`; desactivar o restringir `/facturas/{id}/anular-rectificativa`. 4) Inventario **de solo lectura** de facturas ANULADA por rectificativa y evidencias falsas, con script SQL de reparación que solo se ejecuta con aprobación del asesor y de BABO. 5) Preguntar la configuración real de producción y cómo emite hoy la TPV. 6) Peticiones escritas a B2Brouter y Verifacti (apartado 5.3). 7) Contrato art. 28 RGPD BABO-ReseteaTek. 8) Enviar las preguntas al asesor (apartado 9). | Javier (código, inventario, peticiones); BABO (contrato art. 28); asesor (dudas) | 21-31 oct 2026 (3-4 días-persona) |
| **F1 Núcleo independiente del proveedor** | Puerto y `generarRegistroAlta` síncrono; `V52__registro_fiscal.sql` con triggers probados en Testcontainers (usuario `tahona`, binlog, backup y restauración); Flyway con otro usuario; numeración en `aprobarYEmitir` y secuencia por (prefijo, serie, ejercicio) con test sin huecos; retirar `AEAT_DISPONIBLE`; estados `EMITIDA`/`INCIDENCIA_PROVEEDOR` con banner y correo; leyenda y QR según modalidad y registro; reglas de negocio (R4/R5, `ImporteRectificacion`, F2 sin destinatarios, F1 con cliente, `IDOtro`, recargo y exentas si se usan); auditoría; TZ Europe/Madrid; `FakeReportingAdapter` y tests. **Corte el 21/11**: si no está cerrada, no hay arranque anticipado. | Javier | 3-21 nov 2026 (8-10 días-persona) |
| **Hito: decisión del proveedor** | B2Brouter si precio, SLA, DPA, DR, exportación de cadena y respuesta sobre el art. 9 son aceptables; si no, Verifacti. Requisito en ambos casos: cuenta, autorización (colaborador social o Anexo I), contrato y DPA a nombre de BABO con acceso de emergencia. Revisión de la cláusula de responsabilidad. | Javier y BABO (firma); asesor (revisión) | 7 nov 2026 |
| **F2 Adaptador y sandbox** | Adaptador del proveedor elegido: síncrono, 5 s, idempotencia, escritura única de huella y CSV, evidencia del proveedor; polling (webhook con HMAC en producción). Unas 20 facturas ficticias: F1, F2 de TPV, R4, R5, anulación, subsanación, caída del proveedor y duplicado. Huella contra vectores oficiales. Contract tests con WireMock. | Javier | 17 nov - 5 dic 2026 (3-4 días-persona) |
| **F3 Declaración responsable, PDF, operación y RGPD** | DR según art. 15 (título literal, `IdSistemaInformatico` estable, versión real, componente del proveedor con versión y cómo y cuándo se invoca, S/N, productor con NIF y dirección, manifestación, lugar y fecha), una por versión, accesible y en PDF persistente. QR de 30-40 mm con "QR tributario:" y leyenda literal en PDF y ticket. Runbooks de `INCIDENCIA_PROVEEDOR` (validado por el asesor), de rechazos y subsanación y de rotación de clave. Conservación y cifrado de copias. Corregir `docs/plan-lanzamiento.md` y `docs/verifactu.md`. | Javier; asesor (contingencia y conservación); representante legal de BABO (firmas) | 1-12 dic 2026 (2-3 días-persona) |
| **F4 Aceptación y puesta en producción** | Aceptación con el personal de la Tahona en la demo. Despliegue con `FISCAL_REPORTING=noop` (facturas previas a la obligación como `NO_REMITIBLE`). Reparación SQL antes del despliegue si está aprobada. **1/1/2027 a las 00:00**: cambio a la clave de producción; primer registro con `PrimerRegistro=S`; `verifactu_evidence` congelada. Vigilancia diaria de incidencias y rechazos durante 2 semanas. | Javier y BABO | 14 dic 2026 - 15 ene 2027 |
| **F5 Después de la fecha (opcional)** | Revisar si el BOE publicó el aplazamiento. Medir las caídas del proveedor; si son frecuentes, valorar la variante de generación local o terminar el cliente SOAP propio (10-15 días-persona) como segundo adaptador; si no, retirarlo. | Javier | T1-T2 2027 |

Esfuerzo total estimado de la opción C: **15-20 días-persona** [I], con una sola persona en la ruta crítica durante unas 8 semanas.

---

## 8. Decisiones pendientes de cada parte

### Dirección de Grupo BABO

- Aprobar la opción C (proveedor síncrono con contingencia) en lugar de terminar la implementación propia.
- Aprobar B2Brouter como preferente con el hito del 7/11/2026 y, si no se cumple, Verifacti.
- Aceptar el coste: unos 165 €/año con Verifacti o, con B2Brouter, unos 1.150 € el primer año solo por integración y alta (348 € + 800 €), que pasan de 1.250 € si además hace falta la app de 110 €/año (por confirmar; [I] cálculo con los precios publicados "desde"). Se recomienda que **BABO contrate y pague directamente**.
- Firmar el contrato y el DPA con el proveedor, la autorización de colaborador social o representación, y el contrato del art. 28 RGPD con ReseteaTek.
- Confirmar que la remisión real empieza el 1/1/2027, sin arranque anticipado, salvo criterio distinto del asesor.
- Autorizar ya la fase F0, aunque se publique el aplazamiento a 2028.
- Informar de la configuración efectiva de producción (modalidad, `AEAT_DISPONIBLE`, certificado) y de cómo emite hoy la TPV.
- Autorizar, tras la revisión del asesor, el script SQL de reparación de las facturas ANULADA por rectificativa y de las evidencias falsas.
- Fijar con el asesor el plazo de conservación de los registros fiscales.
- Aceptar que, si ningún proveedor permite continuar una cadena importada, cambiar de proveedor supondrá cerrar una cadena y abrir otra.

### Javier

- Decidir quién figura como productor en la declaración responsable y en `SistemaInformatico`: Javier Barceló García (autónomo) o ReseteaTek, con NIF y dirección postal.
- Enviar las peticiones escritas a los proveedores y las preguntas al asesor (F0).
- Valorar un seguro de responsabilidad civil profesional para ReseteaTek.
- Decidir en 2027 si se termina el cliente SOAP propio, se adopta la variante de generación local o se retira.

### Asesor fiscal

- Responder las preguntas del apartado 9, en especial el procedimiento durante `INCIDENCIA_PROVEEDOR` y la reparación SQL.

---

## 9. Preguntas para el asesor fiscal

1. ¿Se publicará en el BOE el aplazamiento a octubre de 2028 antes del 31/12/2026 y alcanza también a los contribuyentes del Impuesto sobre Sociedades? Mientras no se publique, ¿confirma que hay que planificar para el 1/1/2027?
2. ¿Confirma que Grupo BABO, S.C.V.L. tributa por el Impuesto sobre Sociedades (Ley 20/1990), entra en el art. 3.1.a del RRSIF y **no** está en el SII (art. 3.3)?
3. ¿Confirma que Javier Barceló, como desarrollador y mantenedor del ERP a medida para un solo cliente, es "productor" (art. 3.2 RRSIF y art. 201 bis LGT)? ¿Cómo le afecta que la obligación de ofrecer productos adaptados rija desde el 29/07/2025 con el ERP ya emitiendo facturas?
4. Con una API de un tercero: ¿basta la DR propia del ERP mencionando el componente del tercero y su versión, junto con la DR del proveedor? ¿Qué cláusula contractual de responsabilidad conviene y quién asume las sanciones por fallos del componente externo?
5. Vía de remisión: ¿colaboración social o representación del proveedor (Convenio 017, Anexo I de la Resolución de 18/12/2024), o certificado de representante o de sello de la cooperativa? Si es certificado propio, ¿cómo custodiar la clave en el servidor?
6. Revisión del texto definitivo de la declaración responsable según el art. 15 de la Orden HAC/1177/2024 (la actual del ERP no cumple el formato) y si debe incluir una cláusula sobre el componente externo.
7. ¿Optar por VERI*FACTU antes de la fecha obligatoria o esperar al 1/1/2027, teniendo en cuenta que la opción dura hasta el 31/12 (art. 16.5) y que la activación en B2Brouter es irreversible? (Según una nota de la AEAT, en período de pruebas se puede dejar de enviar. [I] Falta identificar y enlazar esa nota.)
8. Durante una caída del proveedor (`INCIDENCIA_PROVEEDOR`), ¿qué se entrega al cliente para cumplir el Reglamento de facturación (RD 1619/2012) y el art. 9 del RD 1007/2023?
9. ¿Se aprueba el script SQL de reparación de facturas marcadas ANULADA por rectificativa (hoy restan dos veces en el libro de IVA y en el 347) y de las evidencias con estados falsos? ¿Cómo documentar los huecos de numeración ya existentes?
10. Plazo de conservación de los registros fiscales y de las evidencias.
11. Si hay que cambiar de proveedor a mitad de ejercicio, ¿es aceptable cerrar una cadena y abrir otra?
12. Obligaciones del usuario que siguen en VERI*FACTU (libro registro de IVA, modelos 347/349) y procedimiento de rectificativas frente a subsanación o anulación para el flujo real de la panadería (tiques simplificados y facturas completas). ¿Hay ventas a minoristas en recargo de equivalencia, exentas o no sujetas?
13. Si se valorara sacar la facturación a un SaaS para que el ERP deje de ser sistema de facturación, ¿conviene una consulta vinculante a la DGT?

---

## 10. Riesgos y mitigación

| Riesgo | Mitigación |
|---|---|
| **Art. 9 RD 1007/2023**: el registro depende de la API del proveedor al expedir | Llamada síncrona con idempotencia; `INCIDENCIA_PROVEEDOR` avisada y recuperada en orden; runbook validado por el asesor; respuesta escrita de los proveedores sobre disponibilidad; si las caídas son frecuentes, variante de generación local en 2027 |
| **Aplazamiento a 2028 no publicado** | Planificar para el 1/1/2027; F0 y F1 se hacen igualmente |
| **Sanciones art. 201 bis LGT** (150.000 €/ejercicio al productor, 50.000 €/ejercicio a BABO), no trasladables al proveedor | DR del art. 15 correcta; pruebas con vectores oficiales documentadas; revisión contractual por el asesor; seguro de RC profesional para ReseteaTek |
| **Datos ya corruptos en producción**: originales ANULADA por rectificativa que restan dos veces; evidencias ENVIADO/VERIFICADO falsas | Inventario de lectura en F0; reparación SQL solo con aprobación del asesor y de BABO |
| **Numeración con huecos** (secuencia F/R compartida, número asignado en BORRADOR) | Numerar en `aprobarYEmitir` y separar por prefijo en F1; documentar los huecos existentes y consultarlos con el asesor |
| **Cambio de proveedor a mitad de cadena no resuelto** | Exigirlo por escrito; conservar la evidencia del proveedor y exportar periódicamente; validar con el asesor el cierre y apertura de cadena |
| **Continuidad de Verifacti** (empresa de 2024) y **opacidad de B2Brouter** (precio, SLA, DPA, exportación) | Hito del 7/11 y segundo proveedor ya evaluado |
| **RGPD**: posible acceso desde fuera del EEE en Verifacti (cl. 6.7), subencargados no enumerados; falta contrato art. 28 con ReseteaTek | DPA firmado por BABO, lista de subencargados y garantías de transferencia; contrato BABO-ReseteaTek; cifrado de copias; plazo de conservación |
| **Seguridad**: webhook falsificable, usuario de BD con DDL capaz de desactivar triggers, clave de API en manos de una sola persona | HMAC o polling; usuarios de BD separados; escritura única; auditoría; acceso de emergencia a nombre de BABO |
| **Triggers en MySQL 8** con binlog y restauración con otro `DEFINER` [I] | Probar en Testcontainers y en una restauración de prueba antes de F4 |
| **Pruebas poco representativas**: la demo actual va en NO_VERIFACTU desde otro código | Reconstruirla desde esta rama en modo VERIFACTU, con polling y un NIF válido de pruebas |
| **Una sola persona en la ruta crítica** (15-20 días-persona en ~8 semanas) | Corte el 21/11; sin arranque anticipado; F5 fuera de 2026 |
| **Activación irreversible** (B2Brouter) | No activar en producción antes del 1/1/2027; probar solo en sandbox/staging |
| **Configuración real de producción desconocida** (según el código, no debería poder emitir) | Preguntarla en F0; no deducirla de la demo |

---

## 11. Fuentes

### Normativa y documentación oficial

- RD 1007/2023, texto consolidado (art. 9, art. 16.5, DF 4ª): https://www.boe.es/buscar/act.php?id=BOE-A-2023-24840
- RDL 15/2025: https://www.boe.es/diario_boe/txt.php?id=BOE-A-2025-24446
- Orden HAC/1177/2024: https://www.boe.es/diario_boe/txt.php?id=BOE-A-2024-22138 · https://www.boe.es/buscar/act.php?id=BOE-A-2024-22138 · https://www.boe.es/eli/es/o/2024/10/17/hac1177
- Ley General Tributaria (art. 29.2.j y 201 bis): https://www.boe.es/buscar/act.php?id=BOE-A-2003-23186 · https://iberley.es/legislacion/articulo-201-bis-ley-general-tributaria
- Nota de Hacienda del 5/10/2026: https://www.hacienda.gob.es/sgt/gabsehacienda/nota-informativa-verifactu.pdf
- AEAT, portal VERI*FACTU: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu.html
- AEAT, nota informativa de ampliación de plazo: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/nota-informativa-ampliacion-plazo-adaptacion-facturacion.html
- FAQ de certificación y declaración responsable: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/certificacion-sistemas-informaticos-declaracion-responsable.html
- FAQ de cumplimiento por delegación: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/cuestiones-generales-cumplimiento-delegacion.html
- FAQ de sistemas VERI*FACTU: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/sistemas-verifactu.html
- FAQ de colaboración social: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/colaboracion-social.html
- FAQ de capacidad de remisión: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/caracteristicas-requisitos-sif-capacidad-remision-etc_.html
- FAQ de sistemas de facturación múltiple: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/caracteristicas-requisitos-sistemas-informaticos-facturacion-multiple.html
- FAQ de objeto: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/preguntas-frecuentes/cuestiones-generales-objeto.html
- FAQ para desarrolladores (v1.3): https://sede.agenciatributaria.gob.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/FAQs-Desarrolladores.pdf
- Especificación de la huella (v0.1.2): https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/Veri-Factu_especificaciones_huella_hash_registros.pdf
- Especificación del QR: https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/DetalleEspecificacTecnCodigoQRfactura.pdf
- Descripción de los servicios web: https://sede.agenciatributaria.gob.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/Veri-Factu_Descripcion_SWeb.pdf · https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/Veri-Factu_Descripcion_SWeb.pdf
- Validaciones y errores: https://www.agenciatributaria.es/AEAT.desarrolladores/Desarrolladores/_menu_/Documentacion/Sistemas_Informaticos_de_Facturacion_y_Sistemas_VERI_FACTU/Validaciones_y_errores/Validaciones_y_errores.html · https://www.agenciatributaria.es/static_files/AEAT_Desarrolladores/EEDD/IVA/VERI-FACTU/Validaciones_Errores_Veri-Factu.pdf
- Documentación para desarrolladores: https://www.agenciatributaria.es/AEAT.desarrolladores/Desarrolladores/_menu_/Documentacion/Sistemas_Informaticos_de_Facturacion_y_Sistemas_VERI_FACTU/Sistemas_Informaticos_de_Facturacion_y_Sistemas_VERI_FACTU.html
- WSDL oficial: https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SistemaFacturacion.wsdl
- Información técnica y ejemplos de DR: https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/informacion-tecnica.html · https://sede.agenciatributaria.gob.es/Sede/iva/sistemas-informaticos-facturacion-verifactu/informacion-tecnica/ejemplo-declaracion-responsable.html
- Ley 20/1990, régimen fiscal de cooperativas: https://www.iberley.es/legislacion/ley-20-1990-19-dic-regimen-fiscal-cooperativas-1279752

### Prensa y fuentes secundarias sobre plazos

- https://www.xn--serviciospeas-skb.com/2026/10/13/anuncian-nuevo-aplazamiento-de-verifactu-hasta-2028-pero-cuidado-que-aun-no-esta-aprobado/
- https://teamsystem.es/magazine/verifactu-se-aplaza-2028/
- https://www.facturadirecta.com/blog/verifactu-aplazamiento-octubre-2028/
- https://www.infobae.com/espana/agencias/2026/10/05/hacienda-retrasa-verifactu-hasta-2028-para-alinearlo-con-la-factura-electronica/
- https://eteriaconsultores.com/verifactu-aplazamiento-2028/
- https://www.infoautonomos.com/blog/hacienda-retrasa-entrada-vigor-verifactu/
- https://palenciaasesores.es/verifactu-aplazamiento-2027/
- https://theobjective.com/economia/2025-12-03/verifactu-aplaza-2027-nuevas-fechas/
- https://www.spun.es/verifactu/aplazamiento-2028
- https://www.eleconomista.es/economia/noticias/14018805/10/26/hacienda-vuelve-a-retrasar-la-entrada-en-vigor-de-verifactu-hasta-octubre-de-2028.html
- https://wwwhatsnew.com/2026/10/06/hacienda-verifactu-octubre-2028-aplazamiento-factura-electronica-2026
- https://emprendedores.es/gestion/aplazamiento-verifactu-2028/
- https://www.conversoriaecnae.es/blog/news-20261006-verifactu-aplazamiento-2028
- https://www.legaltoday.com/actualidad-juridica/noticias-de-derecho/nueva-prorroga-verifactu-no-sera-obligatorio-hasta-2027-para-sociedades-y-otros-contribuyentes-2025-12-04/
- https://www.fiscal-impuestos.com/node/40339
- https://www.billin.net/blog/hacienda-retrasa-verifactu-a-2027/
- Consultas DGT V2653-24 y V0058-25 (citadas en): https://www.eic.cat/wp-content/uploads/2026/06/Facturacio_verfact.pdf

### Proveedores

- Verifacti: https://www.verifacti.com/docs · https://www.verifacti.com/precios · https://www.verifacti.com/terminos · https://www.verifacti.com/en/preguntas-frecuentes · https://www.verifacti.com/sobre-nosotros · https://apps.odoo.com/apps/modules/17.0/verifacti_api
- DR de arquitectura mixta (Billin/Bilbabit): https://www.billin.net/wp-content/uploads/pdf-billin/declaracion-responsable-sistema-facturacion-verifactu-ts-api.pdf
- B2Brouter: https://docs.b2brouter.net/en/developers/guides-by-country/spain/verifactu · https://docs.b2brouter.net/en/user-guide/tax-management/verifactu/ · https://www.b2brouter.net/es/precios/ · https://www.b2brouter.net/es/precios-productos-integrados/ · https://www.b2brouter.net/es/app-web/ · https://www.b2brouter.net/es/blog/b2b-router-obtiene-la-certificacion-iso-270012013-que-avala-su-gestion-en-la-seguridad-de-la-informacion
- fiskaly: https://www.fiskaly.com/signes/verifactu-for-in-house-teams · https://www.fiskaly.com/blog/verifactu-integration-challenges-software
- Invopop: https://www.invopop.com/pricing · https://docs.invopop.com/guides/es-verifactu · https://docs.invopop.com/guides/es-verifactu-supplier
- Quaderno: https://developers.quaderno.io/guides/e-invoicing/verifactu · https://support.quaderno.io/article/1240-connect-verifactu
- Holded: https://www.holded.com/es/precios · https://feedback.holded.com/mejoras/p/mapear-el-estado-de-aprobacion-en-verifactu-con-api
- FacturaDirecta: https://www.facturadirecta.com/en/pequenos-negocios/precios/

### Otras referencias técnicas

- MySQL, triggers y binlog [I]: https://dev.mysql.com/doc/refman/8.0/en/stored-programs-logging.html

### Código revisado (repositorio)

`src/main/java/alicanteweb/erp/service/`: `VerifactuService.java`, `VerifactuAeatSoapClient.java`, `VerifactuEvidenceService.java`, `FacturaService.java`, `FiscalComplianceService.java`, `TpvService.java`, `ImpresionService.java`, `DeclaracionResponsableService.java`, `EmpresaConfigService.java`, `LibroIvaService.java`, `Modelo347Service.java`, `CarteraService.java`, `BackupService.java`; `src/main/java/alicanteweb/erp/entities/Factura.java`; `src/main/resources/templates/pdf/factura.html`; `src/main/resources/application*.properties`; `src/main/resources/db/migration/` (última V51); `docker-compose.yml`; `docs/plan-lanzamiento.md`; `docs/verifactu.md`.
