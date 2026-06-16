package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class CustomerPhoneUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public CustomerPhoneUpdateStrategy() {
        super("customerPhone", String.class,
                Booking::getCustomerPhone,
                Booking::setCustomerPhone);
    }
}
