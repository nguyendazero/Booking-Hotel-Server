package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.HotelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Long> {
    
}
