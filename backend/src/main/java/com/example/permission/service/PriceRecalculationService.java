package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.entity.BookingDetail;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingDetailMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.example.permission.entity.table.BookingDetailTableDef.BOOKING_DETAIL;

@Service
public class PriceRecalculationService {

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private BookingDetailMapper bookingDetailMapper;

    @Transactional(rollbackFor = Exception.class)
    public void recalculatePrice(Booking booking) {
        if (booking == null || booking.getId() == null) {
            throw new BusinessException("预订信息不能为空");
        }

        RoomType roomType = roomTypeMapper.selectOneById(booking.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }

        validateDateRange(booking.getCheckInDate(), booking.getCheckOutDate());

        int days = (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal roomTotal = calculateRoomTotal(roomType, booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal extraBedTotal = calculateExtraBedTotal(
                booking.getExtraBedCount(), booking.getExtraBedPrice(), days);
        BigDecimal totalAmount = calculateTotalAmount(roomTotal, extraBedTotal,
                booking.getDiscount(), booking.getOtherFee());

        booking.setDays(days);
        booking.setRoomTotal(roomTotal);
        booking.setExtraBedTotal(extraBedTotal);
        booking.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        regenerateBookingDetails(booking, roomType);
    }

    private void validateDateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new BusinessException("入住日期和退房日期不能为空");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }
    }

    public BigDecimal calculateRoomTotal(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        if (roomType == null || checkInDate == null || checkOutDate == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;

        while (currentDate.isBefore(checkOutDate)) {
            BigDecimal dayPrice = getDayPrice(roomType, currentDate);
            total = total.add(dayPrice);
            currentDate = currentDate.plusDays(1);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getDayPrice(RoomType roomType, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return roomType.getWeekendPrice() != null ? roomType.getWeekendPrice() : roomType.getBasePrice();
        } else {
            return roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateExtraBedTotal(Integer extraBedCount, BigDecimal extraBedPrice, int days) {
        if (extraBedCount == null || extraBedCount <= 0 || extraBedPrice == null) {
            return BigDecimal.ZERO;
        }
        return extraBedPrice.multiply(BigDecimal.valueOf(extraBedCount))
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalAmount(BigDecimal roomTotal, BigDecimal extraBedTotal,
                                           BigDecimal discount, BigDecimal otherFee) {
        BigDecimal total = roomTotal != null ? roomTotal : BigDecimal.ZERO;
        total = total.add(extraBedTotal != null ? extraBedTotal : BigDecimal.ZERO);
        total = total.subtract(discount != null ? discount : BigDecimal.ZERO);
        total = total.add(otherFee != null ? otherFee : BigDecimal.ZERO);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void regenerateBookingDetails(Booking booking, RoomType roomType) {
        QueryWrapper deleteQuery = QueryWrapper.create()
                .from(BookingDetail.class)
                .where(BOOKING_DETAIL.BOOKING_ID.eq(booking.getId()));
        bookingDetailMapper.deleteByQuery(deleteQuery);

        LocalDate currentDate = booking.getCheckInDate();
        while (currentDate.isBefore(booking.getCheckOutDate())) {
            BigDecimal price = getDayPrice(roomType, currentDate);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            String dayType = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) ? "周末" : "平日";

            BookingDetail detail = new BookingDetail();
            detail.setBookingId(booking.getId());
            detail.setBookingNo(booking.getBookingNo());
            detail.setStayDate(currentDate);
            detail.setPrice(price);
            detail.setDayType(dayType);
            detail.setCreateTime(LocalDateTime.now());
            bookingDetailMapper.insert(detail);

            currentDate = currentDate.plusDays(1);
        }
    }
}
