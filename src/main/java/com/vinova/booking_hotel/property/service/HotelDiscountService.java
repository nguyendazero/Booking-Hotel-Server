package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;

public interface HotelDiscountService {
    
   String addDiscountToHotel(AddDiscountToHotelRequestDto requestDto, String token);

   String deleteHotelDiscount(Long hotelDiscountId, String token);
}
