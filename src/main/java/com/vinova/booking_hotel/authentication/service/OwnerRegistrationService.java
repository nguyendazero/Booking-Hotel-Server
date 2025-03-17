package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;

import java.util.List;

public interface OwnerRegistrationService {
    String registerOwner(String token);

    List<OwnerRegistrationDto> ownerRegistrations();

    Void acceptRegistration(Long ownerRegistrationId);
    
    Void rejectRegistration(Long ownerRegistrationId);
    
}
