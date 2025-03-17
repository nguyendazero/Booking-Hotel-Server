package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.service.DiscountService;
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
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping("/public/discounts")
    public ResponseEntity<List<DiscountResponseDto>> discounts() {
        List<DiscountResponseDto> response = discountService.discounts();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/public/discount/{id}")
    public ResponseEntity<DiscountResponseDto> discount(@PathVariable Long id) {
        DiscountResponseDto response = discountService.discount(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/admin/discount")
    public ResponseEntity<DiscountResponseDto> create(@RequestBody AddDiscountRequestDto requestDto) {
        DiscountResponseDto response = discountService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/discount/{id}")
    public ResponseEntity<DiscountResponseDto> update(@PathVariable Long id, @RequestBody AddDiscountRequestDto requestDto) {
        DiscountResponseDto response = discountService.update(id, requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/admin/discount/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Void response = discountService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}