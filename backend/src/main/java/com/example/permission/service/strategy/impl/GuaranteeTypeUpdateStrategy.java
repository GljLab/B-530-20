package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GuaranteeTypeUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public GuaranteeTypeUpdateStrategy() {
        super("guaranteeType", String.class,
                Booking::getGuaranteeType,
                Booking::setGuaranteeType);
    }
}
