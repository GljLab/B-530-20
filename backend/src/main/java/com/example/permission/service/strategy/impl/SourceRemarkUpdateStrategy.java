package com.example.permission.service.strategy.impl;

import com.example.permission.entity.Booking;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class SourceRemarkUpdateStrategy extends AbstractFieldUpdateStrategy<String> {

    public SourceRemarkUpdateStrategy() {
        super("sourceRemark", String.class,
                Booking::getSourceRemark,
                Booking::setSourceRemark);
    }
}
