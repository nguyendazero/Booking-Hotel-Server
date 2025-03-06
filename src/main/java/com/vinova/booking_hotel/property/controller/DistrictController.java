package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;
import com.vinova.booking_hotel.property.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DistrictController {
    
    private final DistrictService districtService;
    
    @GetMapping("/public/districts")
    public ResponseEntity<APICustomize<List<DistrictResponseDto>>> districts() {
        APICustomize<List<DistrictResponseDto>> response = districtService.districts();
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/public/district/{id}")
    public ResponseEntity<APICustomize<DistrictResponseDto>> district(@PathVariable Long id) {
        APICustomize<DistrictResponseDto> response = districtService.district(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/admin/district")
    public ResponseEntity<APICustomize<DistrictResponseDto>> create(@RequestBody AddDistrictRequestDto requestDto) {
        APICustomize<DistrictResponseDto> response = districtService.create(requestDto);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/admin/district/{id}")
    public ResponseEntity<APICustomize<DistrictResponseDto>> update(@PathVariable Long id, @RequestBody AddDistrictRequestDto requestDto) {
        APICustomize<DistrictResponseDto> response = districtService.update(id, requestDto);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @DeleteMapping("/admin/district/{id}")
    public ResponseEntity<APICustomize<Void>> delete(@PathVariable Long id) {
        APICustomize<Void> response = districtService.delete(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
