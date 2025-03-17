package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddAmenityToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.DeleteAmenityFromHotelRequestDto;

public interface HotelAmenityService {

    String addAmenityToHotel(AddAmenityToHotelRequestDto requestDto, String token);

    String removeAmenityFromHotel(DeleteAmenityFromHotelRequestDto requestDto, String token);
    
}
