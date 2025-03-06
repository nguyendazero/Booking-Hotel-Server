package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.ApiError;
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
    public APICustomize<String> addToWishList(Long hotelId, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(ResourceNotFoundException::new);

        // Kiểm tra xem khách sạn đã có trong danh sách yêu thích chưa
        if (wishListRepository.findByAccountAndHotel(account, hotel) != null) {
            return new APICustomize<>(ApiError.CONFLICT.getCode(), ApiError.CONFLICT.getMessage(),"Hotel already in wishlist");
        }

        WishList wishList = new WishList();
        wishList.setAccount(account);
        wishList.setHotel(hotel);
        wishListRepository.save(wishList);

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(),"Added to wishlist");
    }

    @Override
    public APICustomize<String> removeFromWishList(Long hotelId, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(ResourceNotFoundException::new);

        WishList wishList = wishListRepository.findByAccountAndHotel(account, hotel);
        if (wishList == null) {
            return new APICustomize<>(ApiError.NOT_FOUND.getCode(), ApiError.NOT_FOUND.getMessage(),"Hotel not found in wishlist");
        }
        wishListRepository.delete(wishList);
        
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(),"Removed from wishlist");
    }
}