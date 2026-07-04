package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/web/entities")
public class WebEntityController {

    private static final Logger log = LoggerFactory.getLogger(WebEntityController.class);

    /**
     * Módulos sin escritura genérica. Las facturas se gestionan exclusivamente por sus
     * controladores/servicios propios: modificarlas o borrarlas por esta vía reflectiva
     * saltaría el registro VeriFactu y rompería la inalterabilidad exigida por el RRSIF.
     */
    private static final Set<String> READ_ONLY_MODULES = Set.of(
            "auditoria", "verifactu-evidencias", "modelo347", "facturas", "facturas-compra"
    );

    /** Módulos que requieren rol ADMIN o ADMINISTRADOR */
    private static final Set<String> ADMIN_MODULES = Set.of(
            "usuarios", "roles", "empresa", "auditoria", "verifactu-evidencias", "backups"
    );

    /** Módulos que requieren al menos rol CONTABLE */
    private static final Set<String> FINANCE_MODULES = Set.of(
            "asientos", "plan-contable", "plan-cuentas",
            "movimientos-banco", "bancos",
            "movimientos-caja", "cajas", "caja-movimientos",
            "modelo347"
    );

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "tokenRecuperacion", "fechaExpiracionToken"
    );

    private static final Set<String> ALWAYS_BLOCKED = Set.of("id", "version", "password", "rol", "role");

    private static final Map<String, Class<?>> MODULES = Map.ofEntries(
            Map.entry("proveedores", Proveedor.class),
            Map.entry("pedidos-venta", Pedido.class),
            Map.entry("pedidos-compra", PedidoCompra.class),
            Map.entry("facturas", Factura.class),
            Map.entry("facturas-compra", FacturaCompra.class),
            Map.entry("albaranes", AlbaranVenta.class),
            Map.entry("presupuestos", Presupuesto.class),
            Map.entry("almacenes", Almacen.class),
            Map.entry("usuarios", Usuario.class),
            Map.entry("asientos", AsientoContable.class),
            Map.entry("plan-contable", PlanContable.class),
            Map.entry("plan-cuentas", PlanCuentas.class),
            Map.entry("movimientos-caja", MovimientoCaja.class),
            Map.entry("cajas", Caja.class),
            Map.entry("caja-movimientos", CajaMovimiento.class),
            Map.entry("movimientos-banco", MovimientoBanco.class),
            Map.entry("bancos", Banco.class),
            Map.entry("roles", Rol.class),
            Map.entry("direcciones-envio", DireccionenvioNew.class),
            Map.entry("modelo347", Modelo347Registro.class),
            Map.entry("auditoria", AuditoriaAccion.class),
            Map.entry("verifactu-evidencias", VerifactuEvidence.class),
            Map.entry("recetas", Receta.class),
            Map.entry("ordenes-produccion", OrdenProduccion.class),
            Map.entry("horneadas", Horneada.class),
            Map.entry("lotes", Lote.class),
            Map.entry("appcc", AppccControl.class),
            Map.entry("vehiculos", Vehiculo.class),
            Map.entry("rutas-reparto", RutaReparto.class),
            Map.entry("hojas-ruta", HojaRuta.class),
            Map.entry("devoluciones", Devolucion.class),
            Map.entry("empresa", EmpresaConfig.class),
            Map.entry("tarifas-cliente", TarifaCliente.class),
            Map.entry("mermas", Merma.class),
            Map.entry("recepciones", Recepcion.class),
            Map.entry("recepcion-lineas", RecepcionLinea.class),
            Map.entry("fianzas", Fianza.class)
    );

    private final EntityManager entityManager;
    private final UsuarioService usuarioService;
    private final Validator validator;

    public WebEntityController(EntityManager entityManager, UsuarioService usuarioService, Validator validator) {
        this.entityManager = entityManager;
        this.usuarioService = usuarioService;
        this.validator = validator;
    }

    @GetMapping
    public Set<String> modules() {
        return MODULES.keySet();
    }

    @GetMapping("/{module}")
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String module,
                                                          @RequestParam(required = false) String q) {
        requireAccess(module);
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        List<?> rows = (q != null && !q.isBlank())
                ? search(entityClass, q.trim(), 200)
                : findAll(entityClass, 500);
        return ResponseEntity.ok(rows.stream().map(this::toDto).toList());
    }

    @GetMapping("/{module}/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String module, @PathVariable Long id) {
        requireAccess(module);
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(entityClass, id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(toDto(entity));
    }

    @PostMapping("/{module}")
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@PathVariable String module,
                                                      @RequestBody Map<String, Object> data) {
        requireAccess(module);
        requireWritable(module);
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Object entity = entityClass.getDeclaredConstructor().newInstance();
            apply(entity, data, module);
            validate(entity);
            if (entity instanceof Usuario usuario) {
                String password = String.valueOf(data.getOrDefault("passwordNuevo", ""));
                if (password.isBlank()) {
                    throw new IllegalArgumentException("La contraseña es obligatoria para crear usuarios");
                }
                return ResponseEntity.ok(toDto(usuarioService.crearUsuario(usuario, password)));
            }
            entityManager.persist(entity);
            entityManager.flush();
            return ResponseEntity.ok(toDto(entity));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo crear " + module, e);
        }
    }

    @PutMapping("/{module}/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@PathVariable String module,
                                                      @PathVariable Long id,
                                                      @RequestBody Map<String, Object> data) {
        requireAccess(module);
        requireWritable(module);
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(entityClass, id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        apply(entity, data, module);
        validate(entity);
        if (entity instanceof Usuario usuario) {
            entityManager.detach(usuario);
            Usuario updated = usuarioService.actualizarUsuario(usuario);
            Object password = data.get("passwordNuevo");
            if (password != null && !String.valueOf(password).isBlank()) {
                usuarioService.cambiarPasswordAdmin(id, String.valueOf(password));
                updated = entityManager.find(Usuario.class, id);
            }
            return ResponseEntity.ok(toDto(updated));
        }
        entityManager.flush();
        return ResponseEntity.ok(toDto(entity));
    }

    @PostMapping("/{module}/{id}/baja")
    @Transactional
    public ResponseEntity<Void> disable(@PathVariable String module, @PathVariable Long id) {
        requireAccess(module);
        requireWritable(module);
        return setActive(module, id, false);
    }

    @PostMapping("/{module}/{id}/activar")
    @Transactional
    public ResponseEntity<Void> enable(@PathVariable String module, @PathVariable Long id) {
        requireAccess(module);
        requireWritable(module);
        return setActive(module, id, true);
    }

    @DeleteMapping("/{module}/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String module, @PathVariable Long id) {
        requireAccess(module);
        requireWritable(module);
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(entityClass, id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        entityManager.remove(entity);
        entityManager.flush();
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> setActive(String module, Long id, boolean active) {
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(entityClass, id);
        Field field = firstField(entityClass, "activo", "activa", "enabled");
        if (entity == null || field == null) {
            return ResponseEntity.notFound().build();
        }
        set(field, entity, active);
        entityManager.flush();
        return ResponseEntity.noContent().build();
    }

    private Class<?> entityClass(String module) {
        return MODULES.get(module);
    }

    private <T> List<T> findAll(Class<T> entityClass, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root).orderBy(cb.asc(root.get("id")));
        return entityManager.createQuery(query).setMaxResults(limit).getResultList();
    }

    private <T> List<T> search(Class<T> entityClass, String query, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        String pattern = "%" + query.toLowerCase() + "%";
        List<Predicate> predicates = fields(entityClass).stream()
                .filter(f -> f.getType() == String.class
                        && !isSensitive(f.getName())
                        && !f.isAnnotationPresent(ManyToOne.class)
                        && !f.isAnnotationPresent(Transient.class)
                        && !java.lang.reflect.Modifier.isStatic(f.getModifiers()))
                .map(f -> {
                    try {
                        return (Predicate) cb.like(cb.lower(root.<String>get(f.getName())), pattern);
                    } catch (IllegalArgumentException e) {
                        log.debug("Campo '{}' no accesible en Criteria API, se omite del filtro de búsqueda: {}", f.getName(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        if (predicates.isEmpty()) {
            return List.of();
        }
        cq.select(root)
          .where(cb.or(predicates.toArray(new Predicate[0])))
          .orderBy(cb.asc(root.get("id")));
        return entityManager.createQuery(cq).setMaxResults(limit).getResultList();
    }

    private Map<String, Object> toDto(Object entity) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (Field field : fields(entity.getClass())) {
            if (isSensitive(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (field.isAnnotationPresent(ManyToOne.class)) {
                    row.put(field.getName() + "Id", value == null ? null : idValue(value));
                    row.put(field.getName(), displayValue(value));
                } else if (isSimple(field.getType())) {
                    row.put(field.getName(), value);
                }
            } catch (IllegalAccessException ignored) {
                row.put(field.getName(), null);
            }
        }
        return row;
    }

    private void apply(Object entity, Map<String, Object> data, String entityName) {
        Class<?> entityClass = entity.getClass();
        for (Field field : fields(entityClass)) {
            if (isFieldBlocked(entityName, field.getName())) continue;
            boolean relationIdPresent = field.isAnnotationPresent(ManyToOne.class)
                    && data.containsKey(field.getName() + "Id");
            if (field.isAnnotationPresent(Id.class)
                    || field.isAnnotationPresent(Version.class)
                    || (!data.containsKey(field.getName()) && !relationIdPresent)) {
                continue;
            }
            if (isSensitive(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Object value = data.get(field.getName());
            if (field.isAnnotationPresent(ManyToOne.class)) {
                Object id = data.get(field.getName() + "Id");
                value = id == null || String.valueOf(id).isBlank()
                        ? null
                        : entityManager.getReference(field.getType(), Long.valueOf(String.valueOf(id)));
            } else if (!isSimple(field.getType())) {
                continue;
            } else {
                value = convert(value, field.getType());
            }
            set(field, entity, value);
        }
    }

    private Object convert(Object value, Class<?> type) {
        return EntityFieldConverter.convert(value, type);
    }

    private Object primitiveDefault(Class<?> type) {
        return EntityFieldConverter.primitiveDefault(type);
    }

    private boolean isSimple(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Long.class
                || type == Integer.class
                || type == Boolean.class
                || type == BigDecimal.class
                || type == Instant.class
                || type == LocalDate.class
                || type == LocalTime.class
                || type == LocalDateTime.class;
    }

    private boolean isSensitive(String fieldName) {
        return SENSITIVE_FIELDS.contains(fieldName);
    }

    private boolean isFieldBlocked(String module, String fieldName) {
        if (ALWAYS_BLOCKED.contains(fieldName)) return true;
        if ("usuarios".equals(module) && Set.of("bloqueado", "enabled", "intentosFallidos").contains(fieldName)) return true;
        return false;
    }

    private void validate(Object entity) {
        Set<ConstraintViolation<Object>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(msg);
        }
    }

    private List<Field> fields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            result.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return result;
    }

    private Field field(Class<?> type, String name) {
        return fields(type).stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
    }

    private Field firstField(Class<?> type, String... names) {
        for (String name : names) {
            Field found = field(type, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Object idValue(Object entity) {
        Field id = fields(entity.getClass()).stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElse(null);
        if (id == null) {
            return null;
        }
        id.setAccessible(true);
        try {
            return id.get(entity);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private String displayValue(Object entity) {
        if (entity == null) {
            return null;
        }
        for (String candidate : List.of("nombre", "razonSocial", "numero", "codigo", "matricula", "descripcion")) {
            Field field = field(entity.getClass(), candidate);
            if (field != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value != null && !String.valueOf(value).isBlank()) {
                        return String.valueOf(value);
                    }
                } catch (IllegalAccessException ignored) {
                    return String.valueOf(idValue(entity));
                }
            }
        }
        return String.valueOf(idValue(entity));
    }

    private void set(Field field, Object entity, Object value) {
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("No se pudo asignar " + field.getName(), e);
        }
    }

    private void requireWritable(String module) {
        if (READ_ONLY_MODULES.contains(module)) {
            throw new IllegalStateException("El módulo " + module + " es de solo lectura");
        }
    }

    private void requireAccess(String module) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No autenticado");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        boolean isContable = isAdmin || auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CONTABLE"));

        if (ADMIN_MODULES.contains(module) && !isAdmin) {
            throw new AccessDeniedException("Acceso denegado al módulo " + module);
        }
        if (FINANCE_MODULES.contains(module) && !isContable) {
            throw new AccessDeniedException("Acceso denegado al módulo " + module);
        }
    }
}
