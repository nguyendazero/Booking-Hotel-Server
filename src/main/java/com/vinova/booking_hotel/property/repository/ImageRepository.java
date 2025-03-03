package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
