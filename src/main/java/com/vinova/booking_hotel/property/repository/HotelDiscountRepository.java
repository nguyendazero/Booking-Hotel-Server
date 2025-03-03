package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.HotelDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelDiscountRepository extends JpaRepository<HotelDiscount, Long> {
}
