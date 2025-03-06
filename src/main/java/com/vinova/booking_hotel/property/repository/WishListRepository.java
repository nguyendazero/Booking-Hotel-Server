package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.property.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<WishList, Long> {
}
