package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class GuestNamesUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public GuestNamesUpdateStrategy() {
        super("guestNames", String.class,
                Booking::getGuestNames,
                Booking::setGuestNames);
    }
}
