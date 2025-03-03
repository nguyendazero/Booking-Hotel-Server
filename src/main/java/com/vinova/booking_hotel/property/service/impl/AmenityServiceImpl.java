package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService{
    
    private final AmenityRepository amenityRepository;
    
}
