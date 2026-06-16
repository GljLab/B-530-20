package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class GuestPhoneUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public GuestPhoneUpdateStrategy() {
        super("guestPhone", String.class,
                Booking::getGuestPhone,
                Booking::setGuestPhone);
    }
}
