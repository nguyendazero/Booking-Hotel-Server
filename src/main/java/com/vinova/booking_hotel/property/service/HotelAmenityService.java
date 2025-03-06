package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;

public interface HotelAmenityService {

    APICustomize<String> addAmenityToHotel(String nameAmenity, Long hotelId, String token);

    APICustomize<String> removeAmenityFromHotel(Long amenityId, Long hotelId, String token);
    
}
