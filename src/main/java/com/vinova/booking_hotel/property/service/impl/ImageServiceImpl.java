package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.ImageRepository;
import com.vinova.booking_hotel.property.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    
}
