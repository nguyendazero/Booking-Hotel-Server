package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.property.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByEntityIdAndEntityType(Long id, EntityType entityType);
}
