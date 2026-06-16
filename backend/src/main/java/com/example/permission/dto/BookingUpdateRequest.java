package com.example.permission.dto;

import com.example.permission.entity.Booking;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class BookingUpdateRequest {

    private Long bookingId;

    private Optional<LocalDate> checkInDate = Optional.empty();
    private Optional<LocalDate> checkOutDate = Optional.empty();
    private Optional<String> customerName = Optional.empty();
    private Optional<String> customerPhone = Optional.empty();
    private Optional<Integer> guestCount = Optional.empty();
    private Optional<String> guestNames = Optional.empty();
    private Optional<String> guestPhone = Optional.empty();
    private Optional<Integer> extraBedCount = Optional.empty();
    private Optional<BigDecimal> extraBedPrice = Optional.empty();
    private Optional<BigDecimal> discount = Optional.empty();
    private Optional<BigDecimal> otherFee = Optional.empty();
    private Optional<String> specialRequirements = Optional.empty();
    private Optional<String> bookingSource = Optional.empty();
    private Optional<String> sourceRemark = Optional.empty();
    private Optional<String> guaranteeType = Optional.empty();
    private Optional<LocalDateTime> expectedArrivalTime = Optional.empty();

    public List<FieldChange<?>> getChangedFields(Booking original) {
        List<FieldChange<?>> changes = new ArrayList<>();

        addChangeIfPresent(changes, "checkInDate", checkInDate, original.getCheckInDate(), LocalDate.class);
        addChangeIfPresent(changes, "checkOutDate", checkOutDate, original.getCheckOutDate(), LocalDate.class);
        addChangeIfPresent(changes, "customerName", customerName, original.getCustomerName(), String.class);
        addChangeIfPresent(changes, "customerPhone", customerPhone, original.getCustomerPhone(), String.class);
        addChangeIfPresent(changes, "guestCount", guestCount, original.getGuestCount(), Integer.class);
        addChangeIfPresent(changes, "guestNames", guestNames, original.getGuestNames(), String.class);
        addChangeIfPresent(changes, "guestPhone", guestPhone, original.getGuestPhone(), String.class);
        addChangeIfPresent(changes, "extraBedCount", extraBedCount, original.getExtraBedCount(), Integer.class);
        addChangeIfPresent(changes, "extraBedPrice", extraBedPrice, original.getExtraBedPrice(), BigDecimal.class);
        addChangeIfPresent(changes, "discount", discount, original.getDiscount(), BigDecimal.class);
        addChangeIfPresent(changes, "otherFee", otherFee, original.getOtherFee(), BigDecimal.class);
        addChangeIfPresent(changes, "specialRequirements", specialRequirements, original.getSpecialRequirements(), String.class);
        addChangeIfPresent(changes, "bookingSource", bookingSource, original.getBookingSource(), String.class);
        addChangeIfPresent(changes, "sourceRemark", sourceRemark, original.getSourceRemark(), String.class);
        addChangeIfPresent(changes, "guaranteeType", guaranteeType, original.getGuaranteeType(), String.class);
        addChangeIfPresent(changes, "expectedArrivalTime", expectedArrivalTime, original.getExpectedArrivalTime(), LocalDateTime.class);

        return changes;
    }

    private <T> void addChangeIfPresent(List<FieldChange<?>> changes, String fieldName,
                                        Optional<T> newValueOpt, T oldValue, Class<T> type) {
        if (newValueOpt.isPresent()) {
            T newValue = newValueOpt.get();
            FieldChange<T> change = new FieldChange<>(fieldName, oldValue, newValue, type);
            if (change.hasChange()) {
                changes.add(change);
            }
        }
    }
}
