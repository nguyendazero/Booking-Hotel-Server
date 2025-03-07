package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;
import com.vinova.booking_hotel.property.service.AmenityService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<APICustomize<List<AmenityResponseDto>>> amenities() {
        APICustomize<List<AmenityResponseDto>> response = amenityService.amenities();
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/public/amenity/{id}")
    public ResponseEntity<APICustomize<AmenityResponseDto>> amenity(@PathVariable Long id) {
        APICustomize<AmenityResponseDto> response = amenityService.amenity(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/owner/amenity")
    public ResponseEntity<APICustomize<AmenityResponseDto>> create(@RequestBody AddAmenityRequestDto requestDto) {
        APICustomize<AmenityResponseDto> response = amenityService.create(requestDto);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/owner/amenity/{id}")
    public ResponseEntity<APICustomize<AmenityResponseDto>> update(@PathVariable Long id, @RequestBody AddAmenityRequestDto requestDto) {
        APICustomize<AmenityResponseDto> response = amenityService.update(id, requestDto);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @DeleteMapping("/owner/amenity/{id}")
    public ResponseEntity<APICustomize<Void>> delete(@PathVariable Long id) {
        APICustomize<Void> response = amenityService.delete(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
}