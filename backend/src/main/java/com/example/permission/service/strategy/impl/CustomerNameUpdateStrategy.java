package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class CustomerNameUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public CustomerNameUpdateStrategy() {
        super("customerName", String.class,
                Booking::getCustomerName,
                Booking::setCustomerName);
    }
}
