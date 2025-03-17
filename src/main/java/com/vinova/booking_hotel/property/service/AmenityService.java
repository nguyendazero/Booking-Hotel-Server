package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;

import java.util.List;

public interface AmenityService {
    
    List<AmenityResponseDto> amenities();

    List<AmenityResponseDto> amenitiesByHotelId(Long hotelId);
    
    AmenityResponseDto amenity(Long id);
    
    AmenityResponseDto create(AddAmenityRequestDto requestDto);
    
    AmenityResponseDto update(Long id, AddAmenityRequestDto requestDto);
    
    Void delete(Long id);
}
