package com.example.permission.service.strategy.impl;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExtraBedPriceUpdateStrategy extends AbstractFieldUpdateStrategy<BigDecimal> {

    public ExtraBedPriceUpdateStrategy() {
        super("extraBedPrice", BigDecimal.class,
                Booking::getExtraBedPrice,
                Booking::setExtraBedPrice);
    }

    @Override
    public void validate(BigDecimal oldValue, BigDecimal newValue, Booking booking, LoginUser loginUser) {
        if (newValue != null && newValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("加床价格不能为负数");
        }
    }

    @Override
    public boolean triggersPriceRecalculation() {
        return true;
    }
}
