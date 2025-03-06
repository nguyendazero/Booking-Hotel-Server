package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    List<WishList> findByAccount(Account account);
    WishList findByAccountAndHotel(Account account, Hotel hotel);
}
