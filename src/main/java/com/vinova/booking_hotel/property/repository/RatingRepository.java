package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByHotelId(Long id);

    Long countByHotel(Hotel hotel);

    @Modifying
    @Query("DELETE FROM Rating r WHERE r.hotel.id = :hotelId")
    void deleteRatingsByHotelId(@Param("hotelId") Long hotelId);
    
}
