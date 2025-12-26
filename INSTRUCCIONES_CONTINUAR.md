# 🚀 INSTRUCCIONES PARA CONTINUAR - FASE 1

**Fecha:** 26 de diciembre de 2025  
**Estado:** Fase 1 iniciada al 35%  
**Próximo objetivo:** Completar Semana 2 (VeriFactu + servicios RGPD)

---

## ✅ LO QUE YA TIENES

Has completado exitosamente:
- ✅ 7 entidades nuevas (Usuario, Rol, Auditoría, RGPD x3, Factura mejorada)
- ✅ 6 repositorios completos
- ✅ 1 script SQL con todo listo para ejecutar
- ✅ 6 documentos de análisis y planificación exhaustivos

---

## 📋 PASOS INMEDIATOS

### 1. Ejecutar el script SQL ✅ CRÍTICO

```bash
# Opción 1: Desde línea de comandos
cd D:\Programación\ERP
mysql -u root -p erp_alicante < basesdedatos\fase1_legalizacion.sql

# Opción 2: Desde MySQL Workbench
# 1. Abrir MySQL Workbench
# 2. Conectar a tu servidor local
# 3. Abrir el archivo: basesdedatos/fase1_legalizacion.sql
# 4. Ejecutar (Ctrl+Shift+Enter)
```

**Verificar que se creó todo:**
```sql
USE erp_alicante;

-- Ver las tablas nuevas
SHOW TABLES LIKE '%usuarios%';
SHOW TABLES LIKE '%rgpd%';
SHOW TABLES LIKE '%auditoria%';

-- Ver los roles creados
SELECT * FROM roles;

-- Ver el usuario admin
SELECT * FROM usuarios;

-- Ver política de privacidad
SELECT * FROM politicas_privacidad;

-- Verificar campos nuevos en facturas
DESCRIBE facturas;
```

---

### 2. Compilar el proyecto

```bash
cd D:\Programación\ERP
mvn clean compile
```

**Si hay errores:**
- Los warnings de "Cannot resolve column" son normales (hasta que ejecutes el SQL)
- Si hay errores de compilación reales, revisa las importaciones

---

### 3. Actualizar application.properties (si es necesario)

Verifica que la conexión a MySQL esté correcta:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/erp_alicante
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD

# Importante: NO usar hibernate.hbm2ddl.auto=create en producción
# Ya tenemos los scripts SQL
spring.jpa.hibernate.ddl-auto=validate
```

---

## 🎯 SIGUIENTE TAREA: CREAR SERVICIOS

### Servicio 1: UsuarioService (ALTA PRIORIDAD)

Crear: `src/main/java/alicanteweb/erp/service/UsuarioService.java`

```java
@Service
@Slf4j
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Necesitas añadir dependencia
    
    // Métodos principales:
    // - crearUsuario(Usuario usuario, String passwordPlain)
    // - actualizarUsuario(Usuario usuario)
    // - cambiarPassword(Long usuarioId, String oldPassword, String newPassword)
    // - buscarPorUsername(String username)
    // - validarCredenciales(String username, String password)
    // - bloquearUsuario(Long usuarioId)
    // - desbloquearUsuario(Long usuarioId)
    // - incrementarIntentosFallidos(Long usuarioId)
    // - resetearIntentosFallidos(Long usuarioId)
    // - generarTokenRecuperacion(String email)
    // - recuperarPassword(String token, String newPassword)
}
```

---

### Servicio 2: AutenticacionService (ALTA PRIORIDAD)

Crear: `src/main/java/alicanteweb/erp/service/AutenticacionService.java`

```java
@Service
@Slf4j
public class AutenticacionService {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private AuditoriaService auditoriaService;
    
    // Métodos principales:
    // - login(String username, String password) -> Usuario o null
    // - logout(Long usuarioId)
    // - verificarSesion(Long usuarioId) -> boolean
    // - obtenerUsuarioActual() -> Usuario
    // - tienePermiso(Long usuarioId, String modulo, String accion) -> boolean
}
```

---

### Servicio 3: AuditoriaService (ALTA PRIORIDAD)

Crear: `src/main/java/alicanteweb/erp/service/AuditoriaService.java`

```java
@Service
@Slf4j
public class AuditoriaService {
    
    @Autowired
    private AuditoriaAccionRepository auditoriaRepository;
    
    // Métodos principales:
    // - registrarAccion(Usuario usuario, String tipoAccion, String entidadTipo, 
    //                   String entidadId, String descripcion)
    // - registrarLogin(Usuario usuario, String ip, boolean exitoso)
    // - registrarLogout(Usuario usuario)
    // - registrarAccesoDenegado(Usuario usuario, String modulo, String accion)
    // - registrarCambio(Usuario usuario, String entidadTipo, String entidadId,
    //                   Map<String, Object> valoresAnteriores, 
    //                   Map<String, Object> valoresNuevos)
    // - obtenerHistorial(String entidadTipo, String entidadId) -> List<AuditoriaAccion>
}
```

---

### Servicio 4: CifradoService (ALTA PRIORIDAD - RGPD)

Crear: `src/main/java/alicanteweb/erp/service/CifradoService.java`

```java
@Service
@Slf4j
public class CifradoService {
    
    @Value("${cifrado.aes.key}")
    private String aesKey; // Configurar en properties
    
    // Métodos principales:
    // - cifrarAES256(String texto) -> String (Base64)
    // - descifrarAES256(String textoCifrado) -> String
    // - hashPassword(String password) -> String (BCrypt)
    // - verificarPassword(String password, String hash) -> boolean
    // - generarKeyAES() -> String
}
```

**Añadir a application.properties:**
```properties
# Cifrado AES-256 (RGPD)
cifrado.aes.key=TU_CLAVE_SECRETA_DE_32_CARACTERES_MINIMO
# IMPORTANTE: Generar una clave segura y guardarla en lugar seguro
# Nunca commitear la clave real a Git
```

---

### Servicio 5: RgpdConsentimientoService

Crear: `src/main/java/alicanteweb/erp/service/RgpdConsentimientoService.java`

```java
@Service
@Slf4j
public class RgpdConsentimientoService {
    
    @Autowired
    private RgpdConsentimientoRepository consentimientoRepository;
    
    // Métodos principales:
    // - registrarConsentimiento(Cliente cliente, String tipo, Boolean otorgado, String ip)
    // - revocarConsentimiento(Long consentimientoId)
    // - tieneConsentimientoActivo(Long clienteId, String tipo) -> boolean
    // - obtenerConsentimientos(Long clienteId) -> List<RgpdConsentimiento>
    // - actualizarPoliticaPrivacidad() // Cuando cambie la política
}
```

---

## 📦 DEPENDENCIAS NECESARIAS

### 1. BCrypt para contraseñas

Añadir a `pom.xml`:
```xml
<!-- Spring Security Crypto para BCrypt -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>6.2.1</version>
</dependency>
```

### 2. Configurar Bean de BCrypt

Crear: `src/main/java/alicanteweb/erp/config/SecurityConfig.java`

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Fuerza 12 (más seguro)
    }
}
```

---

## 🖥️ SIGUIENTE: PANTALLA DE LOGIN

### 1. Crear login.fxml

Crear: `src/main/resources/ui/login.fxml`

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.text.Text?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="alicanteweb.erp.controller.LoginController"
            prefHeight="600.0" prefWidth="800.0"
            style="-fx-background-color: #2c3e50;">
    
    <center>
        <VBox alignment="CENTER" spacing="20" style="-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 10;">
            <Text text="ERP Panadería Tahona" style="-fx-font-size: 24px; -fx-font-weight: bold;"/>
            <Text text="Sistema de Gestión Empresarial" style="-fx-font-size: 14px; -fx-fill: gray;"/>
            
            <Separator />
            
            <TextField fx:id="usernameField" promptText="Usuario" prefWidth="300"/>
            <PasswordField fx:id="passwordField" promptText="Contraseña" prefWidth="300"/>
            
            <Label fx:id="errorLabel" style="-fx-text-fill: red;" visible="false"/>
            
            <Button fx:id="loginButton" text="Iniciar Sesión" onAction="#handleLogin" 
                    prefWidth="300" style="-fx-background-color: #3498db; -fx-text-fill: white;"/>
            
            <Hyperlink text="¿Olvidaste tu contraseña?" onAction="#handleForgotPassword"/>
            
            <VBox.margin>
                <Insets top="20" bottom="20" left="40" right="40"/>
            </VBox.margin>
        </VBox>
    </center>
    
    <bottom>
        <HBox alignment="CENTER" style="-fx-padding: 10;">
            <Label text="© 2025 Panadería Tahona - Versión 1.0" style="-fx-text-fill: white;"/>
        </HBox>
    </bottom>
</BorderPane>
```

---

### 2. Crear LoginController

Crear: `src/main/java/alicanteweb/erp/controller/LoginController.java`

```java
@Component
public class LoginController {
    
    @Autowired
    private AutenticacionService autenticacionService;
    
    @Autowired
    private AuditoriaService auditoriaService;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, complete todos los campos");
            return;
        }
        
        Usuario usuario = autenticacionService.login(username, password);
        
        if (usuario != null) {
            // Login exitoso
            // TODO: Cargar pantalla principal
            System.out.println("Login exitoso: " + usuario.getUsername());
        } else {
            mostrarError("Usuario o contraseña incorrectos");
        }
    }
    
    @FXML
    private void handleForgotPassword() {
        // TODO: Implementar recuperación de contraseña
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperar contraseña");
        alert.setHeaderText("Contacte con el administrador");
        alert.setContentText("Para recuperar su contraseña, contacte con el administrador del sistema.");
        alert.showAndWait();
    }
    
    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
    }
}
```

---

### 3. Modificar ErpLauncher para mostrar login primero

```java
@Override
public void start(Stage stage) {
    try {
        // Cargar pantalla de login primero
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setTitle("ERP Panadería Tahona - Login");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        
    } catch (IOException e) {
        log.error("Error cargando login", e);
        mostrarError("Error al cargar la aplicación");
    }
}
```

---

## 🧪 TESTING

### Crear test básico de Usuario

Crear: `src/test/java/alicanteweb/erp/service/UsuarioServiceTest.java`

```java
@SpringBootTest
@Transactional
public class UsuarioServiceTest {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Test
    public void testCrearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("test");
        usuario.setEmail("test@tahona.com");
        usuario.setNombreCompleto("Usuario Test");
        
        Usuario creado = usuarioService.crearUsuario(usuario, "password123");
        
        assertNotNull(creado.getId());
        assertNotNull(creado.getPassword());
        assertNotEquals("password123", creado.getPassword()); // Debe estar cifrada
    }
    
    @Test
    public void testValidarCredenciales() {
        // Usar el usuario admin que se creó en el SQL
        boolean valido = usuarioService.validarCredenciales("admin", "admin123");
        assertTrue(valido);
        
        boolean invalido = usuarioService.validarCredenciales("admin", "wrongpassword");
        assertFalse(invalido);
    }
}
```

---

## 📚 DOCUMENTACIÓN A LEER

Antes de continuar, revisa estos documentos:

1. **REQUISITOS_LEGALES_ESPAÑA.md** - Para entender qué es obligatorio
2. **PLAN_ACCION_COMPLETO.md** - Para ver el roadmap completo
3. **FASE1_PROGRESO.md** - Para ver qué falta en Fase 1
4. **SESION_26DIC2025_RESUMEN.md** - Resumen de lo implementado hoy

---

## ⚠️ IMPORTANTE: SEGURIDAD

### Antes de usar en producción:

1. **Cambiar contraseña del usuario admin**
   ```sql
   UPDATE usuarios 
   SET password = '$2a$10$NUEVO_HASH_BCRYPT',
       requiere_cambio_password = FALSE
   WHERE username = 'admin';
   ```

2. **Generar clave AES-256 segura**
   ```java
   // Usar para generar:
   KeyGenerator keyGen = KeyGenerator.getInstance("AES");
   keyGen.init(256);
   SecretKey secretKey = keyGen.generateKey();
   String key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
   ```

3. **NO commitear claves en Git**
   - Usar variables de entorno
   - Usar archivos .properties.local (en .gitignore)

4. **Configurar HTTPS en producción**

5. **Limitar intentos de login**
   - Bloquear después de 5 intentos fallidos
   - Implementado en UsuarioService

---

## 🎯 CHECKLIST ANTES DE CONTINUAR

- [ ] Script SQL ejecutado correctamente
- [ ] Todas las tablas creadas
- [ ] Roles insertados (5)
- [ ] Usuario admin creado
- [ ] Proyecto compila sin errores
- [ ] Dependencia BCrypt añadida
- [ ] Bean de BCrypt configurado
- [ ] Documentación leída

---

## 🚀 PRÓXIMO MILESTONE

**Objetivo:** Login funcional y servicios básicos operativos

**Entregables:**
1. ✅ Pantalla de login funcional
2. ✅ Autenticación con BCrypt
3. ✅ Auditoría de logins
4. ✅ Gestión básica de usuarios
5. ✅ Tests unitarios pasando

**Tiempo estimado:** 1 semana (Semana 2 de Fase 1)

---

## 💬 SOPORTE

Si tienes dudas:
1. Revisa los documentos de análisis
2. Revisa el código ya implementado (VeriFactuService es un buen ejemplo)
3. Consulta la documentación de Spring Boot y JavaFX

---

## ✅ RESULTADO ESPERADO

Al terminar la próxima semana deberías tener:

```
✅ Base de datos actualizada
✅ Login funcional
✅ Usuarios gestionables
✅ Contraseñas cifradas con BCrypt
✅ Auditoría automática de acciones
✅ Consentimientos RGPD registrables
✅ Tests básicos pasando
✅ Documentación actualizada
```

---

**¡Éxito en la implementación! 🚀**

---

*Documento generado el 26 de diciembre de 2025*

