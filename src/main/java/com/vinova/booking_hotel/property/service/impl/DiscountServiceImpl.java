package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    
    private final DiscountRepository discountRepository;
    
}
