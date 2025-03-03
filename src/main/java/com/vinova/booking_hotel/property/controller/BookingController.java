package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
}
