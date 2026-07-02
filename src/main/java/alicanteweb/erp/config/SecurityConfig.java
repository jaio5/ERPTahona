package alicanteweb.erp.config;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.HeaderWriterFilter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuración de seguridad para la aplicación
 * Incluye un encoder PBKDF2 personalizado para evitar problemas con la implementación por defecto
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Secret opcional para PBKDF2 (no obligatorio). No lo almacenes en el repo.
    @Value("${security.pbkdf2.secret:}")
    private String secret;

    // Iteraciones recomendadas (ajustar según política y rendimiento)
    @Value("${security.pbkdf2.iterations:185000}")
    private int iterations;

    // Longitud del hash en bits
    @Value("${security.pbkdf2.hashWidth:256}")
    private int hashWidth;

    /**
     * Bean de PasswordEncoder basado en PBKDF2 (implementación local segura)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Default: pbkdf2 (CustomPbkdf2PasswordEncoder)
        String idForEncode = "pbkdf2";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", new CustomPbkdf2PasswordEncoder(secret, iterations, hashWidth));
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder(idForEncode, encoders);
        // Aceptar hashes antiguos no prefijados
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegating;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider erpAuthenticationProvider,
                                                   AuthenticationSuccessHandler erpAuthenticationSuccessHandler,
                                                   CspNonceFilter cspNonceFilter) throws Exception {
        http
                .addFilterBefore(cspNonceFilter, HeaderWriterFilter.class)
                .addFilterBefore(loginRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(erpAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/web/login",
                                "/web/acceso-denegado",
                                "/error",
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/app.css",
                                "/app.js",
                                "/app-config.js",
                                "/erp-core.js",
                                "/erp-crud.js",

                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(
                                "/web/usuarios/**",
                                "/web/backups/**",
                                "/web/empresa/**",
                                "/web/auditoria/**",
                                "/api/web/entities/usuarios/**",
                                "/api/web/entities/roles/**",
                                "/api/web/entities/empresa/**",
                                "/api/web/entities/auditoria/**",
                                "/api/web/entities/verifactu-evidencias/**"
                        ).hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers(
                                "/web/modelo347/**",
                                "/web/verifactu/**",
                                "/web/contabilidad/**",
                                "/api/web/contabilidad/**",
                                "/api/web/reportes/inventario/ajustar"
                        ).hasAnyRole("ADMIN", "ADMINISTRADOR", "CONTABLE")
                        // Módulos contables/financieros: lectura y escritura requieren CONTABLE o ADMIN
                        .requestMatchers(
                                "/api/web/entities/asientos/**",
                                "/api/web/entities/plan-contable/**",
                                "/api/web/entities/plan-cuentas/**",
                                "/api/web/entities/movimientos-banco/**",
                                "/api/web/entities/bancos/**",
                                "/api/web/entities/movimientos-caja/**",
                                "/api/web/entities/cajas/**",
                                "/api/web/entities/caja-movimientos/**",
                                "/api/web/children/asiento-lineas/**"
                        ).hasAnyRole("ADMIN", "ADMINISTRADOR", "CONTABLE")
                        // DELETE sobre cualquier entidad genérica solo para ADMIN
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/web/entities/**",
                                "/api/web/children/**"
                        ).hasAnyRole("ADMIN", "ADMINISTRADOR")
                        .requestMatchers("/api/**", "/web/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/web/login")
                        .loginProcessingUrl("/web/login")
                        .successHandler(erpAuthenticationSuccessHandler)
                        .failureUrl("/web/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/web/logout")
                        .logoutSuccessUrl("/web/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .headers(headers -> headers
                        .contentTypeOptions(cfg -> {})
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                )
                .sessionManagement(session -> session
                        .sessionFixation(fix -> fix.migrateSession())
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(apiAuthEntryPoint())
                        .accessDeniedHandler(apiAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter() {
        return new LoginRateLimitFilter();
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieCustomizer(cookie -> cookie.httpOnly(true));
        return repo;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationEntryPoint apiAuthEntryPoint() {
        LoginUrlAuthenticationEntryPoint loginRedirect = new LoginUrlAuthenticationEntryPoint("/web/login");
        return (request, response, authException) -> {
            if (response.isCommitted()) {
                return;
            }
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Autenticación requerida\"}");
            } else {
                loginRedirect.commence(request, response, authException);
            }
        };
    }

    @Bean
    public AccessDeniedHandler apiAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            if (response.isCommitted()) {
                return;
            }
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Acceso denegado\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/web/acceso-denegado");
            }
        };
    }

    @Bean
    public AuthenticationProvider erpAuthenticationProvider(AutenticacionService autenticacionService) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = String.valueOf(authentication.getName());
                String password = String.valueOf(authentication.getCredentials());
                Usuario usuario = autenticacionService.login(username, password);
                if (usuario == null) {
                    throw new BadCredentialsException("Usuario o contrasena incorrectos");
                }

                ErpUserPrincipal principal = new ErpUserPrincipal(usuario);
                return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler erpAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication.getPrincipal() instanceof ErpUserPrincipal principal) {
                var session = request.getSession(true);
                session.setAttribute("usuarioId", principal.id());
                session.setAttribute("usuarioNombre", principal.displayName());
            }
            response.sendRedirect(request.getContextPath() + "/web/dashboard");
        };
    }

    public record ErpUserPrincipal(Long id,
                                   Long rolId,
                                   String username,
                                   String displayName,
                                   String password,
                                   boolean enabled,
                                   boolean accountNonLocked,
                                   Collection<? extends GrantedAuthority> authorities)
            implements org.springframework.security.core.userdetails.UserDetails {

        ErpUserPrincipal(Usuario usuario) {
            this(
                    usuario.getId(),
                    usuario.getRol() != null ? usuario.getRol().getId() : null,
                    usuario.getUsername(),
                    usuario.getNombre() != null && !usuario.getNombre().isBlank()
                            ? usuario.getNombre()
                            : usuario.getUsername(),
                    usuario.getPassword(),
                    Boolean.TRUE.equals(usuario.getEnabled()),
                    !Boolean.TRUE.equals(usuario.getBloqueado()),
                    authoritiesFor(usuario)
            );
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        private static final org.slf4j.Logger authLog = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

        private static List<GrantedAuthority> authoritiesFor(Usuario usuario) {
            String roleStr = usuario.getRole();
            String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
            if (roleStr != null && !roleStr.isBlank() && rolNombre != null && !rolNombre.isBlank()
                    && !roleStr.trim().equalsIgnoreCase(rolNombre.trim())) {
                authLog.warn("Usuario {}: columna role='{}' y rol.nombre='{}' son distintos; se usa role",
                        usuario.getUsername(), roleStr, rolNombre);
            }
            String role = (roleStr != null && !roleStr.isBlank()) ? roleStr : rolNombre;
            if (role == null || role.isBlank()) {
                role = "USER";
            }
            String normalized = role.trim().toUpperCase();
            if (!normalized.startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized;
            }
            return List.of(new SimpleGrantedAuthority(normalized));
        }
    }
}
