package alicanteweb.erp.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class CifradoServiceTest {

     @Test
     public void testGenerarKeyAES_and_decodeLength() {
         String key = CifradoService.generarKeyAES();
         assertNotNull(key);
         byte[] decoded = Base64.getDecoder().decode(key);
         // generarKeyAES crea 32 bytes (AES-256)
         assertTrue(decoded.length == 32 || decoded.length == 16 || decoded.length == 24);
     }

     @Test
     public void testCifrarYDescifrarAES256_roundtrip() {
         // Crear servicio con environment no productivo
         CifradoService service = new CifradoService(new BCryptPasswordEncoder(), new StandardEnvironment());
         String key = CifradoService.generarKeyAES();
         // Forzar inyeccion de key para pruebas
         service.setSecretKey(key);

         String textoOriginal = "Hola Mundo!";
         String textoCifrado = service.cifrarAES256(textoOriginal);
         String textoDescifrado = service.descifrarAES256(textoCifrado);

         assertEquals(textoOriginal, textoDescifrado);
     }

     @Test
     public void testGenerarTokenSeguro_notEmpty() {
         CifradoService service = new CifradoService(new BCryptPasswordEncoder(), new StandardEnvironment());
         // Inyectar key para evitar problemas si getSecretKey utiliza el valor en el futuro
         service.setSecretKey(CifradoService.generarKeyAES());

         String token = service.generarTokenSeguro(16);
         assertNotNull(token);
         assertFalse(token.isEmpty());
     }
 }
