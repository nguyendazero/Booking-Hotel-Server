package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    
    WishList findByAccountAndHotel(Account account, Hotel hotel);

    @Modifying
    @Query("DELETE FROM WishList wl WHERE wl.hotel.id = :hotelId")
    void deleteWishListsByHotelId(@Param("hotelId") Long hotelId);
}
