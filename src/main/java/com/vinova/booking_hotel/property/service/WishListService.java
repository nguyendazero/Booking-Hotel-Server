package com.vinova.booking_hotel.property.service;


public interface WishListService {
    
   String addToWishList(Long hotelId, String token);
    
   String removeFromWishList(Long hotelId, String token);
    
}
