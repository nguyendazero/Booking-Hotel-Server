package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;
import com.vinova.booking_hotel.property.service.HotelDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HotelDiscountController {
    
    private final HotelDiscountService hotelDiscountService;

    @PostMapping("/owner/hotel-discount")
    public ResponseEntity<APICustomize<String>> addDiscountToHotel(@RequestBody AddDiscountToHotelRequestDto requestDto,
                                                                   @RequestParam Long hotelId,
                                                                   @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<String> response = hotelDiscountService.addDiscountToHotel(requestDto, hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
