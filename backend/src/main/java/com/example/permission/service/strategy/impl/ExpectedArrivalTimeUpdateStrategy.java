package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ExpectedArrivalTimeUpdateStrategy extends AbstractFieldUpdateStrategy<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExpectedArrivalTimeUpdateStrategy() {
        super("expectedArrivalTime", LocalDateTime.class,
                Booking::getExpectedArrivalTime,
                Booking::setExpectedArrivalTime);
    }

    @Override
    public String formatValue(LocalDateTime value) {
        return value != null ? value.format(FORMATTER) : "";
    }
}
