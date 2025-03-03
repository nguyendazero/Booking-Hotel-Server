package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.HotelDiscountRepository;
import com.vinova.booking_hotel.property.service.HotelDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelDiscountServiceImpl implements HotelDiscountService {
    
    private final HotelDiscountRepository hotelDiscountRepository;
    
}
