package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/children")
public class WebChildEntityController {

    private static final Map<String, ChildDefinition> CHILDREN = Map.ofEntries(
            entry("pedido-lineas", PedidoLinea.class, Pedido.class, "pedido"),
            entry("presupuesto-lineas", PresupuestoLinea.class, Presupuesto.class, "presupuesto"),
            entry("albaran-lineas", AlbaranVentaLinea.class, AlbaranVenta.class, "albaran"),
            entry("factura-lineas", FacturaLinea.class, Factura.class, "factura"),
            entry("pedido-compra-lineas", PedidoCompraLinea.class, PedidoCompra.class, "pedidoCompra"),
            entry("factura-compra-lineas", FacturaCompraLinea.class, FacturaCompra.class, "facturaCompra"),
            entry("receta-ingredientes", RecetaIngrediente.class, Receta.class, "receta"),
            entry("ruta-paradas", RutaParada.class, RutaReparto.class, "ruta"),
            entry("hoja-ruta-entregas", HojaRutaEntrega.class, HojaRuta.class, "hojaRuta"),
            entry("devolucion-lineas", DevolucionLinea.class, Devolucion.class, "devolucion"),
            entry("asiento-lineas", LineaAsiento.class, AsientoContable.class, "asiento"),
            entry("lote-insumos", LoteInsumo.class, Lote.class, "loteProducto")
    );

    private final EntityManager entityManager;

    public WebChildEntityController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping
    public Map<String, String> modules() {
        Map<String, String> result = new LinkedHashMap<>();
        CHILDREN.forEach((key, value) -> result.put(key, value.entityClass().getSimpleName()));
        return result;
    }

    @GetMapping("/{child}/{parentId}")
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String child,
                                                          @PathVariable Long parentId) {
        ChildDefinition definition = CHILDREN.get(child);
        if (definition == null || entityManager.find(definition.parentClass(), parentId) == null) {
            return ResponseEntity.notFound().build();
        }
        String entityName = definition.entityClass().getSimpleName();
        String parentField = definition.parentField();
        List<?> rows = entityManager.createQuery(
                        "select e from " + entityName + " e where e." + parentField + ".id = :parentId",
                        definition.entityClass())
                .setParameter("parentId", parentId)
                .getResultList();
        return ResponseEntity.ok(rows.stream().map(this::toDto).toList());
    }

    @PostMapping("/{child}/{parentId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@PathVariable String child,
                                                      @PathVariable Long parentId,
                                                      @RequestBody Map<String, Object> data) {
        ChildDefinition definition = CHILDREN.get(child);
        if (definition == null) {
            return ResponseEntity.notFound().build();
        }
        Object parent = entityManager.find(definition.parentClass(), parentId);
        if (parent == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Object entity = definition.entityClass().getDeclaredConstructor().newInstance();
            set(firstField(definition.entityClass(), definition.parentField()), entity, parent);
            apply(entity, data, definition.parentField());
            calculate(entity);
            entityManager.persist(entity);
            entityManager.flush();
            return ResponseEntity.ok(toDto(entity));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo crear " + child, e);
        }
    }

    @PutMapping("/{child}/{parentId}/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@PathVariable String child,
                                                      @PathVariable Long parentId,
                                                      @PathVariable Long id,
                                                      @RequestBody Map<String, Object> data) {
        ChildDefinition definition = CHILDREN.get(child);
        if (definition == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(definition.entityClass(), id);
        Object parent = entityManager.find(definition.parentClass(), parentId);
        if (entity == null || parent == null || !parentId.equals(idValue(get(firstField(definition.entityClass(), definition.parentField()), entity)))) {
            return ResponseEntity.notFound().build();
        }
        apply(entity, data, definition.parentField());
        calculate(entity);
        entityManager.flush();
        return ResponseEntity.ok(toDto(entity));
    }

    @DeleteMapping("/{child}/{parentId}/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String child,
                                       @PathVariable Long parentId,
                                       @PathVariable Long id) {
        ChildDefinition definition = CHILDREN.get(child);
        if (definition == null) {
            return ResponseEntity.notFound().build();
        }
        Object entity = entityManager.find(definition.entityClass(), id);
        if (entity == null || !parentId.equals(idValue(get(firstField(definition.entityClass(), definition.parentField()), entity)))) {
            return ResponseEntity.notFound().build();
        }
        entityManager.remove(entity);
        entityManager.flush();
        return ResponseEntity.noContent().build();
    }

    private static Map.Entry<String, ChildDefinition> entry(String id, Class<?> entityClass, Class<?> parentClass, String parentField) {
        return Map.entry(id, new ChildDefinition(entityClass, parentClass, parentField));
    }

    private Map<String, Object> toDto(Object entity) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (Field field : fields(entity.getClass())) {
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

    private void apply(Object entity, Map<String, Object> data, String parentField) {
        for (Field field : fields(entity.getClass())) {
            boolean relationIdPresent = field.isAnnotationPresent(ManyToOne.class)
                    && data.containsKey(field.getName() + "Id");
            if (field.isAnnotationPresent(Id.class)
                    || field.getName().equals(parentField)
                    || (!data.containsKey(field.getName()) && !relationIdPresent)) {
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

    private void calculate(Object entity) {
        try {
            Method method = entity.getClass().getMethod("calcularImporte");
            method.invoke(entity);
        } catch (ReflectiveOperationException ignored) {
            // Algunas lineas calculan totales con getters o no tienen calculo propio.
        }
    }

    private Object convert(Object value, Class<?> type) {
        if (value == null || String.valueOf(value).isBlank()) {
            return type.isPrimitive() ? primitiveDefault(type) : null;
        }
        if (type == String.class) return String.valueOf(value);
        if (type == Long.class || type == long.class) return Long.valueOf(String.valueOf(value));
        if (type == Integer.class || type == int.class) return Integer.valueOf(String.valueOf(value));
        if (type == Double.class || type == double.class) return Double.valueOf(String.valueOf(value));
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
        if (type == double.class) return 0D;
        return null;
    }

    private boolean isSimple(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == BigDecimal.class
                || type == Instant.class
                || type == LocalDate.class
                || type == LocalTime.class
                || type == LocalDateTime.class
                || type.isEnum();
    }

    private List<Field> fields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            result.addAll(List.of(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return result;
    }

    private Field firstField(Class<?> type, String name) {
        return fields(type).stream()
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Campo no encontrado: " + name));
    }

    private Object get(Field field, Object target) {
        field.setAccessible(true);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void set(Field field, Object target, Object value) {
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private Long idValue(Object entity) {
        if (entity == null) {
            return null;
        }
        for (Field field : fields(entity.getClass())) {
            if (field.isAnnotationPresent(Id.class)) {
                Object id = get(field, entity);
                return id == null ? null : Long.valueOf(String.valueOf(id));
            }
        }
        return null;
    }

    private String displayValue(Object entity) {
        if (entity == null) {
            return "";
        }
        for (String name : List.of("codigo", "numero", "nombre", "descripcion", "username", "matricula")) {
            try {
                Field field = firstField(entity.getClass(), name);
                Object value = get(field, entity);
                if (value != null && !String.valueOf(value).isBlank()) {
                    return String.valueOf(value);
                }
            } catch (IllegalArgumentException ignored) {
                // Probar el siguiente campo habitual.
            }
        }
        Long id = idValue(entity);
        return id == null ? entity.getClass().getSimpleName() : entity.getClass().getSimpleName() + " #" + id;
    }

    private record ChildDefinition(Class<?> entityClass, Class<?> parentClass, String parentField) {
    }
}
