package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService;

    @GetMapping("/public/hotel/{hotelId}/images")
    public ResponseEntity<List<ImageResponseDto>> images(@PathVariable Long hotelId) {
        List<ImageResponseDto> response = imageService.images(hotelId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
}
