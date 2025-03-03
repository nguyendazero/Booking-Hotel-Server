package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, Long> {
}
