package com.vinova.booking_hotel.authentication.service.impl;

import com.vinova.booking_hotel.authentication.repository.OwnerRegistrationRepository;
import com.vinova.booking_hotel.authentication.service.OwnerRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerRegistrationImpl implements OwnerRegistrationService {
    
    private final OwnerRegistrationRepository ownerRegistrationRepository;
    
}
