package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelDiscountResponseDto;
import com.vinova.booking_hotel.property.service.HotelDiscountService;
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
public class HotelDiscountController {
    
    private final HotelDiscountService hotelDiscountService;

    @PostMapping("/owner/hotel-discount")
    public ResponseEntity<String> addDiscountToHotel(@RequestBody AddDiscountToHotelRequestDto requestDto,
                                                                   @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        String response = hotelDiscountService.addDiscountToHotel(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/owner/hotel-discount/{hotelDiscountId}")
    public ResponseEntity<String> deleteHotelDiscount(
            @PathVariable Long hotelDiscountId,
            @RequestHeader("Authorization") String token) {

        String accessToken = token.substring(7);
        String response = hotelDiscountService.deleteHotelDiscount(hotelDiscountId, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/public/hotel/{hotelId}/discounts")
    public ResponseEntity<List<HotelDiscountResponseDto>> getDiscountsByHotelId(@PathVariable Long hotelId) {
        List<HotelDiscountResponseDto> discounts = hotelDiscountService.getHotelDiscounts(hotelId);
        return ResponseEntity.ok(discounts);
    }
    
}
