package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
