package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.RatingRepository;
import com.vinova.booking_hotel.property.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    
    private final RatingRepository ratingRepository;
    
}
