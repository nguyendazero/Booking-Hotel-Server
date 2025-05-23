package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Long> {
    Optional<HotelAmenity> findByHotelAndAmenity(Hotel hotel, Amenity amenity);

    @Modifying
    @Query("DELETE FROM HotelAmenity ha WHERE ha.hotel.id = :hotelId")
    void deleteAmenitiesByHotelId(@Param("hotelId") Long hotelId);
}
