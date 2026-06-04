package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/web/entities")
public class WebEntityController {

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
            Map.entry("empresa", EmpresaConfig.class)
    );

    private final EntityManager entityManager;
    private final UsuarioService usuarioService;

    public WebEntityController(EntityManager entityManager, UsuarioService usuarioService) {
        this.entityManager = entityManager;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public Set<String> modules() {
        return MODULES.keySet();
    }

    @GetMapping("/{module}")
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String module,
                                                          @RequestParam(required = false) String q) {
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        String entityName = entityClass.getSimpleName();
        List<?> rows = entityManager.createQuery("select e from " + entityName + " e", entityClass)
                .setMaxResults(500)
                .getResultList();
        List<Map<String, Object>> dtoRows = rows.stream()
                .map(this::toDto)
                .filter(row -> matches(q, row))
                .toList();
        return ResponseEntity.ok(dtoRows);
    }

    @GetMapping("/{module}/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String module, @PathVariable Long id) {
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
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Object entity = entityClass.getDeclaredConstructor().newInstance();
            apply(entity, data);
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
        Class<?> entityClass = entityClass(module);
        if (entityClass == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(entityClass, id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        apply(entity, data);
        if (entity instanceof Usuario usuario) {
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
        return setActive(module, id, false);
    }

    @PostMapping("/{module}/{id}/activar")
    @Transactional
    public ResponseEntity<Void> enable(@PathVariable String module, @PathVariable Long id) {
        return setActive(module, id, true);
    }

    @DeleteMapping("/{module}/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String module, @PathVariable Long id) {
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

    private void apply(Object entity, Map<String, Object> data) {
        Class<?> entityClass = entity.getClass();
        for (Field field : fields(entityClass)) {
            boolean relationIdPresent = field.isAnnotationPresent(ManyToOne.class)
                    && data.containsKey(field.getName() + "Id");
            if (field.isAnnotationPresent(Id.class) || (!data.containsKey(field.getName()) && !relationIdPresent)) {
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
        if (value == null || String.valueOf(value).isBlank()) {
            return type.isPrimitive() ? primitiveDefault(type) : null;
        }
        if (type == String.class) return String.valueOf(value);
        if (type == Long.class || type == long.class) return Long.valueOf(String.valueOf(value));
        if (type == Integer.class || type == int.class) return Integer.valueOf(String.valueOf(value));
        if (type == Boolean.class || type == boolean.class) return Boolean.valueOf(String.valueOf(value));
        if (type == BigDecimal.class) return new BigDecimal(String.valueOf(value));
        if (type == Instant.class) return Instant.parse(String.valueOf(value));
        if (type == LocalDate.class) return LocalDate.parse(String.valueOf(value));
        if (type == LocalTime.class) return LocalTime.parse(String.valueOf(value));
        if (type == LocalDateTime.class) return LocalDateTime.parse(String.valueOf(value));
        return value;
    }

    private Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
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
        return Set.of("password", "tokenRecuperacion", "fechaExpiracionToken").contains(fieldName);
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

    private boolean matches(String query, Map<String, Object> row) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.trim().toLowerCase();
        return row.values().stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::toLowerCase)
                .anyMatch(value -> value.contains(normalized));
    }
}
