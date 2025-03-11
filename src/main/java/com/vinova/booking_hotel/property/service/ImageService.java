package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;

import java.util.List;

public interface ImageService {
    
    APICustomize<List<ImageResponseDto>> imagesByHotelId(Long hotelId);
    
}
