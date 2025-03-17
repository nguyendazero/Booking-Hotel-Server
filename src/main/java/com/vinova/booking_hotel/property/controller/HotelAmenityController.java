package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddAmenityToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.DeleteAmenityFromHotelRequestDto;
import com.vinova.booking_hotel.property.service.HotelAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> addAmenityToHotel(@RequestBody AddAmenityToHotelRequestDto requestDto,
                                                                  @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        String response = hotelAmenityService.addAmenityToHotel(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/owner/hotel-amenity")
    public ResponseEntity<String> removeAmenityFromHotel(@RequestBody DeleteAmenityFromHotelRequestDto requestDto,
                                                                       @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        String response = hotelAmenityService.removeAmenityFromHotel(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
