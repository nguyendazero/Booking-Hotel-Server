package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;

import java.util.List;

public interface ConfigService {
    
    APICustomize<List<ConfigResponseDto>> configs();

    APICustomize<List<ConfigResponseDto>> configsByToken(String token);

    APICustomize<List<ConfigResponseDto>> configsByAccountId(Long accountId);
    
    APICustomize<ConfigResponseDto> config(Long id);
    
    APICustomize<ConfigResponseDto> create(AddConfigRequestDto requestDto, String token);
    
    APICustomize<ConfigResponseDto> update(Long id, AddConfigRequestDto requestDto, String token);
    
    APICustomize<Void> delete(Long id, String token);
}
