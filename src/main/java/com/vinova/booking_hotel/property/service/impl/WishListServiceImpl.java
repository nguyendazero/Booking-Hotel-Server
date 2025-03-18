package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.WishList;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.WishListRepository;
import com.vinova.booking_hotel.property.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final AccountRepository accountRepository;
    private final HotelRepository hotelRepository;
    private final JwtUtils jwtUtils;

    @Override
    public String addToWishList(Long hotelId, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra xem khách sạn đã có trong danh sách yêu thích chưa
        if (wishListRepository.findByAccountAndHotel(account, hotel) != null) {
            return "Hotel already in wishlist";
        }

        WishList wishList = new WishList();
        wishList.setAccount(account);
        wishList.setHotel(hotel);
        wishListRepository.save(wishList);

        return "Added to wishlist";
    }

    @Override
    public String removeFromWishList(Long hotelId, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        WishList wishList = wishListRepository.findByAccountAndHotel(account, hotel);
        if (wishList == null) {
            return "Hotel not found in wishlist";
        }
        wishListRepository.delete(wishList);
        
        return "Removed from wishlist";
    }
}