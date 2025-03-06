package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;

import java.util.List;

public interface DistrictService {
    
    APICustomize<List<DistrictResponseDto>> districts();
    
    APICustomize<DistrictResponseDto> district(Long id);
    
    APICustomize<DistrictResponseDto> create(AddDistrictRequestDto requestDto);
    
    APICustomize<DistrictResponseDto> update(Long id, AddDistrictRequestDto requestDto);
    
    APICustomize<Void> delete(Long id);
    
}
