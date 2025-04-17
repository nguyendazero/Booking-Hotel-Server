package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;

import java.util.List;

public interface ConfigService {
    
    List<ConfigResponseDto> configs();
    
    ConfigResponseDto create(AddConfigRequestDto requestDto);
    
    ConfigResponseDto update(Long id, AddConfigRequestDto requestDto);
    
    Void delete(Long id);
}
