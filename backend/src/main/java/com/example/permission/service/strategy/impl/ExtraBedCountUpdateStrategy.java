package com.example.permission.service.strategy.impl;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class ExtraBedCountUpdateStrategy extends AbstractFieldUpdateStrategy<Integer> {

    public ExtraBedCountUpdateStrategy() {
        super("extraBedCount", Integer.class,
                Booking::getExtraBedCount,
                Booking::setExtraBedCount);
    }

    @Override
    public void validate(Integer oldValue, Integer newValue, Booking booking, LoginUser loginUser) {
        if (newValue != null && newValue < 0) {
            throw new BusinessException("加床数量不能为负数");
        }
    }

    @Override
    public boolean triggersPriceRecalculation() {
        return true;
    }
}
