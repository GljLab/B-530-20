package com.example.permission.service.strategy.impl;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CheckOutDateUpdateStrategy extends AbstractFieldUpdateStrategy<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CheckOutDateUpdateStrategy() {
        super("checkOutDate", LocalDate.class,
                Booking::getCheckOutDate,
                Booking::setCheckOutDate);
    }

    @Override
    public void validate(LocalDate oldValue, LocalDate newValue, Booking booking, LoginUser loginUser) {
        if (newValue == null) {
            throw new BusinessException("退房日期不能为空");
        }
        LocalDate checkInDate = booking.getCheckInDate();
        if (checkInDate != null && !newValue.isAfter(checkInDate)) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }
    }

    @Override
    public boolean triggersPriceRecalculation() {
        return true;
    }

    @Override
    public String formatValue(LocalDate value) {
        return value != null ? value.format(FORMATTER) : "";
    }
}
