package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;

import java.util.List;

public interface OwnerRegistrationService {
    APICustomize<String> registerOwner(String token);

    APICustomize<List<OwnerRegistrationDto>> ownerRegistrations();

    APICustomize<Void> acceptRegistration(Long ownerRegistrationId);
    
    APICustomize<Void> rejectRegistration(Long ownerRegistrationId);
    
}
