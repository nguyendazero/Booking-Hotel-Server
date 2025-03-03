package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
