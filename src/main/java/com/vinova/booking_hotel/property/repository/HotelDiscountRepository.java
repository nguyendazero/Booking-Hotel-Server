package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.HotelDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface HotelDiscountRepository extends JpaRepository<HotelDiscount, Long> {
    List<HotelDiscount> findByHotelId(Long id);

    @Query("SELECT hd FROM HotelDiscount hd WHERE hd.hotel.id = :hotelId AND " +
            "(hd.startDate < :endDate AND hd.endDate > :startDate)")
    
    List<HotelDiscount> findByHotelIdAndDateRange(@Param("hotelId") Long hotelId,
                                                  @Param("startDate") ZonedDateTime startDate,
                                                  @Param("endDate") ZonedDateTime endDate);
}
