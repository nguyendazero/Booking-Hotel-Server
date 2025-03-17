package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;
import com.vinova.booking_hotel.property.service.AmenityService;
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
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping("/public/amenities")
    public ResponseEntity<List<AmenityResponseDto>> amenities() {
        List<AmenityResponseDto> response = amenityService.amenities();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/public/hotel/{hotelId}/amenities")
    public ResponseEntity<List<AmenityResponseDto>> amenitiesByHotelId(@PathVariable Long hotelId) {
        List<AmenityResponseDto> response = amenityService.amenitiesByHotelId(hotelId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/public/amenity/{id}")
    public ResponseEntity<AmenityResponseDto> amenity(@PathVariable Long id) {
        AmenityResponseDto response = amenityService.amenity(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/owner/amenity")
    public ResponseEntity<AmenityResponseDto> create(@RequestBody AddAmenityRequestDto requestDto) {
        AmenityResponseDto response = amenityService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/owner/amenity/{id}")
    public ResponseEntity<AmenityResponseDto> update(@PathVariable Long id, @RequestBody AddAmenityRequestDto requestDto) {
        AmenityResponseDto response = amenityService.update(id, requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/owner/amenity/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Void response = amenityService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}