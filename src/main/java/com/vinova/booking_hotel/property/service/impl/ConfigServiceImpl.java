package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.ConfigRepository;
import com.vinova.booking_hotel.property.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    
    private final ConfigRepository configRepository;
    
}
