package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.service.HotelAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HotelAmenityController {
    
    private final HotelAmenityService hotelAmenityService;

    @PostMapping("/owner/hotel-amenity")
    public ResponseEntity<APICustomize<String>> addAmenityToHotel(@RequestParam String nameAmenity,
                                                                  @RequestParam Long hotelId,
                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<String> response = hotelAmenityService.addAmenityToHotel(nameAmenity, hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
        
    }
    
}
