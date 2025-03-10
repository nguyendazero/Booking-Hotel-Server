package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId AND " +
            "(b.startDate < :endDate AND b.endDate > :startDate)")
    List<Booking> findByHotelIdAndDateRange(@Param("hotelId") Long hotelId,
                                            @Param("startDate") ZonedDateTime startDate,
                                            @Param("endDate") ZonedDateTime endDate);
    
}
