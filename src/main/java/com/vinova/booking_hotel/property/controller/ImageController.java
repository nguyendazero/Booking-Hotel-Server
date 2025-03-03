package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService; 
    
}
