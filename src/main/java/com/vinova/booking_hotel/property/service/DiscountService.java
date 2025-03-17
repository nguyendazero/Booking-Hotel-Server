package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;

import java.util.List;

public interface DiscountService {
    
    List<DiscountResponseDto> discounts();
    
    DiscountResponseDto discount(Long id);
    
    DiscountResponseDto create(AddDiscountRequestDto requestDto);
    
    DiscountResponseDto update(Long id, AddDiscountRequestDto requestDto);
    
    Void delete(Long id);
    
}
