package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.util.FinancialMath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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

    private static final int MAX_RESULTADOS_HIJO = 500;
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

    /**
     * Módulo del modelo de permisos granulares que gobierna cada colección hija:
     * las líneas heredan el módulo de su documento padre. GET→ver; POST/PUT→editar
     * (añadir o cambiar líneas es editar el documento existente); DELETE→eliminar.
     * Los ADMIN mantienen acceso total (PermisoEvaluador.puede devuelve true).
     */
    private static final Map<String, String> PERMISO_POR_HIJO = Map.ofEntries(
            Map.entry("pedido-lineas", "ventas"),
            Map.entry("presupuesto-lineas", "ventas"),
            Map.entry("albaran-lineas", "ventas"),
            Map.entry("factura-lineas", "ventas"),
            Map.entry("pedido-compra-lineas", "compras"),
            Map.entry("factura-compra-lineas", "compras"),
            Map.entry("receta-ingredientes", "produccion"),
            Map.entry("ruta-paradas", "reparto"),
            Map.entry("hoja-ruta-entregas", "reparto"),
            Map.entry("devolucion-lineas", "ventas"),
            Map.entry("asiento-lineas", "contabilidad"),
            Map.entry("lote-insumos", "almacen")
    );

    private final EntityManager entityManager;
    private final alicanteweb.erp.config.PermisoEvaluador permisos;

    public WebChildEntityController(EntityManager entityManager,
                                    alicanteweb.erp.config.PermisoEvaluador permisos) {
        this.entityManager = entityManager;
        this.permisos = permisos;
    }

    private void requireAccess(String child, String accion) {
        String modulo = PERMISO_POR_HIJO.get(child);
        if (modulo != null && !permisos.puede(modulo, accion)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Acceso denegado a " + child);
        }
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
        if (definition == null) {
            return ResponseEntity.notFound().build();
        }
        requireAccess(child, "ver");
        if (entityManager.find(definition.parentClass(), parentId) == null) {
            return ResponseEntity.notFound().build();
        }
        List<?> rows = findByParent(definition.entityClass(), definition.parentField(), parentId);
        return ResponseEntity.ok(rows.stream().map(this::toDto).toList());
    }

    private <T> List<T> findByParent(Class<T> entityClass, String parentField, Long parentId) {
        return findByParent(entityClass, parentField, parentId, MAX_RESULTADOS_HIJO);
    }

    private <T> List<T> findByParent(Class<T> entityClass, String parentField, Long parentId, int maxResults) {
        CriteriaQuery<T> query = entityManager.getCriteriaBuilder().createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root).where(entityManager.getCriteriaBuilder()
                .equal(root.get(parentField).get("id"), parentId));
        return entityManager.createQuery(query).setMaxResults(maxResults).getResultList();
    }

    /**
     * Id del padre de una línea sin inicializar el proxy lazy: leer el campo @Id por
     * reflexión sobre un proxy de Hibernate devuelve null (los datos viven en el
     * target), lo que hacía fallar la comprobación de pertenencia con un 404.
     */
    private Long parentIdOf(ChildDefinition definition, Object entity) {
        Object parentRef = get(firstField(definition.entityClass(), definition.parentField()), entity);
        if (parentRef == null) {
            return null;
        }
        if (parentRef instanceof org.hibernate.proxy.HibernateProxy proxy) {
            Object id = proxy.getHibernateLazyInitializer().getIdentifier();
            return id == null ? null : Long.valueOf(String.valueOf(id));
        }
        return idValue(parentRef);
    }

    /**
     * Inalterabilidad (RRSIF, RD 1007/2023): las líneas de una factura que ya forma
     * parte del registro de facturación no se crean, editan ni borran; las correcciones
     * exigen factura rectificativa. Refuerza en el controlador el guard JPA de
     * FacturaLinea (que actúa en el flush) para responder un 4xx claro, y cubre las
     * facturas de compra, que no tienen guard de entidad. Espejo del READ_ONLY_MODULES
     * de WebEntityController.
     */
    private void requirePadreModificable(Object parent) {
        if (parent instanceof Factura factura) {
            String estado = factura.getEstado() == null || factura.getEstado().isBlank()
                    ? "BORRADOR" : factura.getEstado().trim().toUpperCase();
            boolean editable = ("BORRADOR".equals(estado) || "REVISION".equals(estado))
                    && !Boolean.TRUE.equals(factura.getVerifactuEnviada());
            if (!editable) {
                throw new IllegalStateException("La factura " + factura.getNumero() + " está " + estado
                        + ": sus líneas son inalterables (RRSIF). Emita una factura rectificativa.");
            }
        } else if (parent instanceof FacturaCompra compra) {
            String estado = compra.getEstado() == null || compra.getEstado().isBlank()
                    ? "PENDIENTE" : compra.getEstado().trim().toUpperCase();
            if (!"PENDIENTE".equals(estado) || Boolean.TRUE.equals(compra.getContabilizada())) {
                throw new IllegalStateException("La factura de compra " + compra.getNumero() + " está " + estado
                        + ": sus líneas no pueden modificarse una vez contabilizada o pagada.");
            }
        }
    }

    /**
     * Mantiene los totales de la cabecera coherentes con sus líneas tras crear, editar
     * o borrar una línea (mismos redondeos que DocumentoService.guardarLineasFactura).
     * Sin límite de resultados: recortar líneas aquí produciría totales incorrectos.
     */
    private void recalcularTotalesPadre(Object parent) {
        if (parent instanceof Factura factura) {
            BigDecimal base = BigDecimal.ZERO;
            BigDecimal iva = BigDecimal.ZERO;
            for (FacturaLinea linea : findByParent(FacturaLinea.class, "factura", factura.getId(), Integer.MAX_VALUE)) {
                BigDecimal subtotal = FinancialMath.subtotalConDescuento(
                        linea.getCantidad(), linea.getPrecioUnitario(), linea.getDescuento());
                base = base.add(subtotal);
                iva = iva.add(FinancialMath.porcentaje(subtotal, linea.getIva()));
            }
            factura.setBaseImponible(base);
            factura.setTotalIva(iva);
            factura.setTotal(base.add(iva));
            entityManager.flush();
        } else if (parent instanceof FacturaCompra compra) {
            BigDecimal base = BigDecimal.ZERO;
            BigDecimal iva = BigDecimal.ZERO;
            for (FacturaCompraLinea linea : findByParent(FacturaCompraLinea.class, "facturaCompra", compra.getId(), Integer.MAX_VALUE)) {
                BigDecimal subtotal = FinancialMath.subtotalConDescuento(
                        linea.getCantidad(), linea.getPrecioUnitario(), linea.getDescuento());
                base = base.add(subtotal);
                iva = iva.add(FinancialMath.porcentaje(subtotal, linea.getTipoIva()));
            }
            compra.setBaseImponible(base);
            compra.setImporteIva(iva);
            compra.calcularTotal();
            entityManager.flush();
        }
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
        requireAccess(child, "editar");
        Object parent = entityManager.find(definition.parentClass(), parentId);
        if (parent == null) {
            return ResponseEntity.notFound().build();
        }
        requirePadreModificable(parent);
        try {
            Object entity = definition.entityClass().getDeclaredConstructor().newInstance();
            set(firstField(definition.entityClass(), definition.parentField()), entity, parent);
            apply(entity, data, definition.parentField());
            calculate(entity);
            entityManager.persist(entity);
            entityManager.flush();
            recalcularTotalesPadre(parent);
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
        requireAccess(child, "editar");
        Object entity = entityManager.find(definition.entityClass(), id);
        Object parent = entityManager.find(definition.parentClass(), parentId);
        if (entity == null || parent == null || !parentId.equals(parentIdOf(definition, entity))) {
            return ResponseEntity.notFound().build();
        }
        requirePadreModificable(parent);
        apply(entity, data, definition.parentField());
        calculate(entity);
        entityManager.flush();
        recalcularTotalesPadre(parent);
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
        requireAccess(child, "eliminar");
        Object entity = entityManager.find(definition.entityClass(), id);
        if (entity == null || !parentId.equals(parentIdOf(definition, entity))) {
            return ResponseEntity.notFound().build();
        }
        Object parent = entityManager.find(definition.parentClass(), parentId);
        requirePadreModificable(parent);
        entityManager.remove(entity);
        entityManager.flush();
        recalcularTotalesPadre(parent);
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
        return EntityFieldConverter.convert(value, type);
    }

    private Object primitiveDefault(Class<?> type) {
        return EntityFieldConverter.primitiveDefault(type);
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
