package com.vinova.booking_hotel.property.repository;


import com.vinova.booking_hotel.property.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
}
