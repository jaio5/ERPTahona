package alicanteweb.erp.config;

import alicanteweb.erp.service.RolService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Bean de evaluación de permisos granulares.
 * Úsalo en @PreAuthorize: @permisos.puede('clientes','crear')
 * O en Thymeleaf:         ${@permisos.puede('clientes','crear')}
 */
@Component("permisos")
public class PermisoEvaluador {

    private final RolService rolService;

    public PermisoEvaluador(RolService rolService) {
        this.rolService = rolService;
    }

    public boolean puede(String modulo, String accion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        if (esAdmin(auth)) return true;
        if (!(auth.getPrincipal() instanceof SecurityConfig.ErpUserPrincipal p)) return false;
        Long rolId = p.rolId();
        if (rolId == null) return false;
        return rolService.tienePermiso(rolId, modulo, accion);
    }

    public boolean puedeCrearOEditar(String modulo) {
        return puede(modulo, "crear") || puede(modulo, "editar");
    }

    public boolean puedeVerAlguno(String... modulos) {
        for (String modulo : modulos) {
            if (puede(modulo, "ver")) return true;
        }
        return false;
    }

    private boolean esAdmin(Authentication auth) {
        for (GrantedAuthority a : auth.getAuthorities()) {
            String authority = a.getAuthority();
            if ("ROLE_ADMIN".equals(authority) || "ROLE_ADMINISTRADOR".equals(authority)) return true;
        }
        return false;
    }
}
