package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Long> {
    Optional<HotelAmenity> findByHotelAndAmenity(Hotel hotel, Amenity amenity);
}
