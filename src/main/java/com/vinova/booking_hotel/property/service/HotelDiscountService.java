package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;

import java.math.BigDecimal;

public interface HotelDiscountService {
    
    APICustomize<String> addDiscountToHotel(AddDiscountToHotelRequestDto requestDto, Long hotelId, String token); 
    
}
