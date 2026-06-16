package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class SpecialRequirementsUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public SpecialRequirementsUpdateStrategy() {
        super("specialRequirements", String.class,
                Booking::getSpecialRequirements,
                Booking::setSpecialRequirements);
    }
}
