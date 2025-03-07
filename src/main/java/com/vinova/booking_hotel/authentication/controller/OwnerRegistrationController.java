package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.service.OwnerRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OwnerRegistrationController {
    
    private final OwnerRegistrationService ownerRegistrationService;
    
}
