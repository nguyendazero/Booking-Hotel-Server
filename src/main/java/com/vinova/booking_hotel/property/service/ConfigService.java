package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;

import java.util.List;

public interface ConfigService {
    
    List<ConfigResponseDto> configs();

    List<ConfigResponseDto> configsByToken(String token);

    List<ConfigResponseDto> configsByAccountId(Long accountId);
    
    ConfigResponseDto config(Long id);
    
    ConfigResponseDto create(AddConfigRequestDto requestDto, String token);
    
    ConfigResponseDto update(Long id, AddConfigRequestDto requestDto, String token);
    
    Void delete(Long id, String token);
}
