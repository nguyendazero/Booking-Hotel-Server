package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelDiscountResponseDto;

import java.util.List;

public interface HotelDiscountService {
    
   String addDiscountToHotel(AddDiscountToHotelRequestDto requestDto, String token);

   String deleteHotelDiscount(Long hotelDiscountId, String token);

   List<HotelDiscountResponseDto> getHotelDiscounts(Long hotelId);
   
}
