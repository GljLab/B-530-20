package com.example.permission.service.strategy;

import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;

public interface FieldUpdateStrategy<T> {

    String getFieldName();

    Class<T> getFieldType();

    void validate(T oldValue, T newValue, Booking booking, LoginUser loginUser);

    void apply(Booking booking, T newValue);

    boolean triggersPriceRecalculation();

    String formatValue(T value);

    default String getChangeType() {
        return "修改";
    }

    default String getRemark() {
        return "修改预订信息";
    }
}
