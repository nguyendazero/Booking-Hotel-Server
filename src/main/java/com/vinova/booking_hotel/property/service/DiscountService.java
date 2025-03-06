package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;

import java.util.List;

public interface DiscountService {
    
    APICustomize<List<DiscountResponseDto>> discounts();
    
    APICustomize<DiscountResponseDto> discount(Long id);
    
    APICustomize<DiscountResponseDto> create(AddDiscountRequestDto requestDto);
    
    APICustomize<DiscountResponseDto> update(Long id, AddDiscountRequestDto requestDto);
    APICustomize<Void> delete(Long id);
    
}
