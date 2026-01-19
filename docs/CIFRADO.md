# Guía de Cifrado

Este documento resume cómo usar el `CifradoService` del proyecto para cifrar datos con AES-256-GCM y gestionar contraseñas con BCrypt.

## Resumen
- `CifradoService` provee:
  - `cifrarAES256(String texto)`: cifra texto con AES-256-GCM y devuelve Base64(IV||ciphertext).
  - `descifrarAES256(String textoCifrado)`: descifra textos cifrados con el servicio.
  - `hashPassword(String password)`: hash con BCrypt.
  - `verificarPassword(String password, String hash)`: verifica contraseña contra el hash.
  - `generarKeyAES()`: utilidad que genera una clave AES-256 (Base64) para configuración.
  - `generarTokenSeguro(int length)`: token aleatorio seguro (Base64 URL-safe).

## Configuración
Se recomienda configurar la clave AES en formato Base64 (32 bytes para AES-256). Añade en `application.properties` (o inyecta vía variable de entorno) la propiedad:

```
cifrado.aes.key=<TU_CLAVE_BASE64>
```

Generar clave (ejemplos):

- OpenSSL (Linux/macOS):
```
openssl rand -base64 32
```

- PowerShell (Windows):
```
$b = New-Object byte[] 32; (New-Object System.Security.Cryptography.RNGCryptoServiceProvider).GetBytes($b); [Convert]::ToBase64String($b)
```

O bien, en Java puedes ejecutar desde una clase el método `CifradoService.generarKeyAES()`.

**Importante**: NO guardes claves en repositorios. Usa un gestor de secretos (Vault, AWS Secrets Manager, Azure Key Vault) o variables de entorno en producción.

## Uso desde código

Inyecta el servicio y úsalo:

```java
@Autowired
private CifradoService cifradoService;

String cifrado = cifradoService.cifrarAES256("datos sensibles");
String claro = cifradoService.descifrarAES256(cifrado);

String hash = cifradoService.hashPassword("miPassword");
boolean ok = cifradoService.verificarPassword("miPassword", hash);
```

## Notas de seguridad y recomendaciones
- Usa AES-GCM (ya implementado) para confidencialidad y autenticación de integridad.
- No reutilices IV. `CifradoService` usa IV aleatorio de 96 bits prefijado al ciphertext.
- Mantén la clave AES en un gestor de secretos, no en `application.properties` en texto plano.
- Para contraseñas usa BCrypt (work factor ajustable mediante `BCryptPasswordEncoder`).
- Implementa rotación de claves si es necesario: para rotación, almacena con versión de clave en metadata o re-encripta los datos.

## Migración / compatibilidad
- `CifradoService.getSecretKey()` acepta una clave en Base64 o bien una cadena legible (se deriva por SHA-256) para compatibilidad con instalaciones antiguas. Cambia a Base64 para mayor seguridad.

## Tests
- Añadir tests unitarios: cifrar -> descifrar round-trip, verificar que IV cambia en cada cifrado, chequear longitudes, y tests para `generarTokenSeguro`.

---
Si quieres, puedo agregar tests unitarios y ejemplos en código de cómo ejecutar `generarKeyAES()` en una `main` temporal para obtener la clave.

