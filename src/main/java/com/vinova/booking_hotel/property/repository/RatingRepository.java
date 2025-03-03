package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
