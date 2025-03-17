package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;

import java.util.List;

public interface DistrictService {
    
    List<DistrictResponseDto> districts();
    
    DistrictResponseDto district(Long id);
    
    DistrictResponseDto create(AddDistrictRequestDto requestDto);
    
    DistrictResponseDto update(Long id, AddDistrictRequestDto requestDto);
    
    Void delete(Long id);
    
}
