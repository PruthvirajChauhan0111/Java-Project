package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import model.annotation.MaxLength;
import model.annotation.Positive;

public class AnnotationValidator {

    private AnnotationValidator() {
    }

    public static String[] validate(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object is required.");
        }

        List<String> errors = new ArrayList<>();
        Class<?> type = obj.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot read field " + field.getName(), e);
                }

                Positive positive = field.getAnnotation(Positive.class);
                if (positive != null && value instanceof Number number && number.doubleValue() <= 0) {
                    errors.add(field.getName() + ": " + positive.message());
                }

                MaxLength maxLength = field.getAnnotation(MaxLength.class);
                if (maxLength != null && value instanceof CharSequence text
                        && text.length() > maxLength.value()) {
                    errors.add(field.getName() + ": length must be <= " + maxLength.value());
                }
            }
            type = type.getSuperclass();
        }
        return errors.toArray(String[]::new);
    }
}
