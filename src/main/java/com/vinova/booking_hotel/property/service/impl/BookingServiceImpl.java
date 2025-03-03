package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.property.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    
}
