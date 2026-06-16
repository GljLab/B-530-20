package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class GuestCountUpdateStrategy extends AbstractFieldUpdateStrategy<Integer> {

    public GuestCountUpdateStrategy() {
        super("guestCount", Integer.class,
                Booking::getGuestCount,
                Booking::setGuestCount);
    }
}
