package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
