package com.example.permission.service.strategy.impl;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;
import com.example.permission.service.strategy.AbstractFieldUpdateStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CheckInDateUpdateStrategy extends AbstractFieldUpdateStrategy<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CheckInDateUpdateStrategy() {
        super("checkInDate", LocalDate.class,
                Booking::getCheckInDate,
                Booking::setCheckInDate);
    }

    @Override
    public void validate(LocalDate oldValue, LocalDate newValue, Booking booking, LoginUser loginUser) {
        if (newValue == null) {
            throw new BusinessException("入住日期不能为空");
        }
        LocalDate checkOutDate = booking.getCheckOutDate();
        if (checkOutDate != null && !newValue.isBefore(checkOutDate)) {
            throw new BusinessException("入住日期必须早于退房日期");
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
