package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddAmenityToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.DeleteAmenityFromHotelRequestDto;

public interface HotelAmenityService {

    APICustomize<String> addAmenityToHotel(AddAmenityToHotelRequestDto requestDto, String token);

    APICustomize<String> removeAmenityFromHotel(DeleteAmenityFromHotelRequestDto requestDto, String token);
    
}
