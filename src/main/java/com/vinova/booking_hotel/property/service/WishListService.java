package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;

public interface WishListService {
    
    APICustomize<String> addToWishList(Long hotelId, String token);
    
    APICustomize<String> removeFromWishList(Long hotelId, String token);
    
}
