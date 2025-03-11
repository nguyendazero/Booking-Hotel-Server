package com.vinova.booking_hotel.common.scheduling;

import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingStatusUpdaterScheduler {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void updateBookingStatuses() {
        ZonedDateTime now = ZonedDateTime.now();

        // Cập nhật trạng thái booking thành CHECKIN
        List<Booking> checkinBookings = bookingRepository.findByStartDateBeforeAndStatus(now, BookingStatus.CONFIRMED);
        for (Booking booking : checkinBookings) {
            booking.setStatus(BookingStatus.CHECKIN);
            bookingRepository.save(booking);
        }

        // Cập nhật trạng thái booking thành CHECKOUT
        List<Booking> checkoutBookings = bookingRepository.findByEndDateBeforeAndStatus(now, BookingStatus.CHECKIN);
        for (Booking booking : checkoutBookings) {
            booking.setStatus(BookingStatus.CHECKOUT);
            bookingRepository.save(booking);
        }
    }
}