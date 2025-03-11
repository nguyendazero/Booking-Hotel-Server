package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService; 
    
    @GetMapping("/public/hotel/images")
    public ResponseEntity<APICustomize<List<ImageResponseDto>>> imagesByHotelId(@RequestParam Long hotelId) {
        APICustomize<List<ImageResponseDto>> response = imageService.imagesByHotelId(hotelId);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
