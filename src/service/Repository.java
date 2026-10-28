package service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import model.annotation.Id;

public class Repository<T> {

    private final HashMap<String, T> entities = new HashMap<>();

    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        entities.put(readId(entity), entity);
        return entity;
    }

    public T findById(String id) {
        return entities.get(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(entities.values());
    }

    public boolean delete(String id) {
        return entities.remove(id) != null;
    }

    @SafeVarargs
    public final void saveAll(T... items) {
        for (T item : items) {
            save(item);
        }
    }

    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values must not be empty.");
        }
        double total = 0;
        for (N value : values) {
            total += value.doubleValue();
        }
        return total / values.length;
    }

    private String readId(T entity) {
        Class<?> type = entity.getClass();
        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(entity);
                        if (value == null) {
                            throw new IllegalArgumentException("@Id field must not be null.");
                        }
                        return value.toString();
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(
                                "Cannot read @Id field " + field.getName(), e);
                    }
                }
            }
            type = type.getSuperclass();
        }
        throw new IllegalArgumentException(
                "No @Id field found on " + entity.getClass().getName());
    }
}
