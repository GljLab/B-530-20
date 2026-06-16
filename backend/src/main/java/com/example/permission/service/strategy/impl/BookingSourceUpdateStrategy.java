package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class BookingSourceUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public BookingSourceUpdateStrategy() {
        super("bookingSource", String.class,
                Booking::getBookingSource,
                Booking::setBookingSource);
    }
}
