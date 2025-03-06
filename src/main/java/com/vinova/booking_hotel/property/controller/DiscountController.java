package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping("/public/discounts")
    public ResponseEntity<APICustomize<List<DiscountResponseDto>>> discounts() {
        APICustomize<List<DiscountResponseDto>> response = discountService.discounts();
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/public/discount/{id}")
    public ResponseEntity<APICustomize<DiscountResponseDto>> discount(@PathVariable Long id) {
        APICustomize<DiscountResponseDto> response = discountService.discount(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/admin/discount")
    public ResponseEntity<APICustomize<DiscountResponseDto>> create(@RequestBody AddDiscountRequestDto requestDto) {
        APICustomize<DiscountResponseDto> response = discountService.create(requestDto);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/admin/discount/{id}")
    public ResponseEntity<APICustomize<DiscountResponseDto>> update(@PathVariable Long id, @RequestBody AddDiscountRequestDto requestDto) {
        APICustomize<DiscountResponseDto> response = discountService.update(id, requestDto);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @DeleteMapping("/admin/discount/{id}")
    public ResponseEntity<APICustomize<Void>> delete(@PathVariable Long id) {
        APICustomize<Void> response = discountService.delete(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
}