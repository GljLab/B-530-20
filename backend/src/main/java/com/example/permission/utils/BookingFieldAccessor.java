package com.example.permission.utils;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import com.example.permission.service.strategy.FieldUpdateStrategy;
import com.example.permission.service.strategy.FieldUpdateStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BookingFieldAccessor {

    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    @Autowired
    private FieldUpdateStrategyFactory strategyFactory;

    public String getFieldValue(Booking booking, String fieldName) {
        if (booking == null || fieldName == null) {
            return "";
        }

        if (strategyFactory.hasStrategy(fieldName)) {
            return getValueByStrategy(booking, fieldName);
        }

        return getValueByReflection(booking, fieldName);
    }

    @SuppressWarnings("unchecked")
    private String getValueByStrategy(Booking booking, String fieldName) {
        FieldUpdateStrategy<?> strategy = strategyFactory.getStrategy(fieldName);
        if (strategy instanceof AbstractFieldUpdateStrategy) {
            AbstractFieldUpdateStrategy<Object> abstractStrategy =
                    (AbstractFieldUpdateStrategy<Object>) strategy;
            Object value = abstractStrategy.getValue(booking);
            return abstractStrategy.formatValue(value);
        }
        return "";
    }

    private String getValueByReflection(Booking booking, String fieldName) {
        try {
            Field field = getCachedField(fieldName);
            if (field == null) {
                return "";
            }
            Object value = field.get(booking);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private Field getCachedField(String fieldName) {
        return fieldCache.computeIfAbsent(fieldName, name -> {
            try {
                Field field = Booking.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                return null;
            }
        });
    }
}
