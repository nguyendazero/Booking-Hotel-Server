package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;
import com.vinova.booking_hotel.property.service.DistrictService;
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
public class DistrictController {
    
    private final DistrictService districtService;
    
    @GetMapping("/public/districts")
    public ResponseEntity<List<DistrictResponseDto>> districts() {
        List<DistrictResponseDto> response = districtService.districts();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/public/district/{id}")
    public ResponseEntity<DistrictResponseDto> district(@PathVariable Long id) {
        DistrictResponseDto response = districtService.district(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/admin/district")
    public ResponseEntity<DistrictResponseDto> create(@RequestBody AddDistrictRequestDto requestDto) {
        DistrictResponseDto response = districtService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/district/{id}")
    public ResponseEntity<DistrictResponseDto> update(@PathVariable Long id, @RequestBody AddDistrictRequestDto requestDto) {
        DistrictResponseDto response = districtService.update(id, requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/admin/district/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Void response = districtService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
