package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;

import java.util.List;

public interface AmenityService {
    
    APICustomize<List<AmenityResponseDto>> amenities();
    
    APICustomize<AmenityResponseDto> amenity(Long id);
    
    APICustomize<AmenityResponseDto> create(AddAmenityRequestDto requestDto);
    
    APICustomize<AmenityResponseDto> update(Long id, AddAmenityRequestDto requestDto);
    
    APICustomize<Void> delete(Long id);
}
