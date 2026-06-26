package alicanteweb.erp.controller.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

final class EntityFieldConverter {

    private EntityFieldConverter() {}

    static Object convert(Object value, Class<?> type) {
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

    static Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        return null;
    }
}