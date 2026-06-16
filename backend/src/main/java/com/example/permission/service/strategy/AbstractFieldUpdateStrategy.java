package com.example.permission.service.strategy;

import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractFieldUpdateStrategy<T> implements FieldUpdateStrategy<T> {

    protected final String fieldName;
    protected final Class<T> fieldType;
    protected final Function<Booking, T> getter;
    protected final BiConsumer<Booking, T> setter;

    protected AbstractFieldUpdateStrategy(String fieldName, Class<T> fieldType,
                                          Function<Booking, T> getter,
                                          BiConsumer<Booking, T> setter) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Class<T> getFieldType() {
        return fieldType;
    }

    @Override
    public void validate(T oldValue, T newValue, Booking booking, LoginUser loginUser) {
    }

    @Override
    public void apply(Booking booking, T newValue) {
        setter.accept(booking, newValue);
    }

    @Override
    public boolean triggersPriceRecalculation() {
        return false;
    }

    @Override
    public String formatValue(T value) {
        return value != null ? value.toString() : "";
    }

    public T getValue(Booking booking) {
        return getter.apply(booking);
    }

    public boolean hasChange(Booking booking, T newValue) {
        T oldValue = getValue(booking);
        return !Objects.equals(oldValue, newValue);
    }
}
