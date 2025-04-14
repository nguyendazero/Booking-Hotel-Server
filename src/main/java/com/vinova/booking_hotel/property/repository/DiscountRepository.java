package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Discount findByRate(BigDecimal rate);
}
