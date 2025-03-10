package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Amenity findByName(String nameAmenity);

    @Query("SELECT a FROM Amenity a JOIN HotelAmenity ha ON a.id = ha.amenity.id WHERE ha.hotel.id = :hotelId")
    List<Amenity> findAmenitiesByHotelId(@Param("hotelId") Long hotelId);
}
