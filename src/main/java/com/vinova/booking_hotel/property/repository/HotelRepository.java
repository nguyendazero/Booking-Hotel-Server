package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    @Query("SELECT COALESCE(AVG(r.stars), 0.0) FROM Rating r WHERE r.hotel.id = :hotelId")
    Double findAverageRatingByHotelId(@Param("hotelId") Long hotelId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Hotel h WHERE h.id = :hotelId")
    void deleteHotelById(@Param("hotelId") Long hotelId);
    
}
