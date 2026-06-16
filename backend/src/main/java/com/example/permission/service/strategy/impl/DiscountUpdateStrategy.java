package com.example.permission.service.strategy.impl;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DiscountUpdateStrategy extends AbstractFieldUpdateStrategy<BigDecimal> {

    public DiscountUpdateStrategy() {
        super("discount", BigDecimal.class,
                Booking::getDiscount,
                Booking::setDiscount);
    }

    @Override
    public void validate(BigDecimal oldValue, BigDecimal newValue, Booking booking, LoginUser loginUser) {
        if (newValue != null && newValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("折扣金额不能为负数");
        }
    }

    @Override
    public boolean triggersPriceRecalculation() {
        return true;
    }
}
