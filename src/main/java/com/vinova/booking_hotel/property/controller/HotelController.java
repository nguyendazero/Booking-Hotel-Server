package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HotelController {
    
    private final HotelService hotelService;

    @GetMapping("/public/hotels")
    public ResponseEntity<APICustomize<List<HotelResponseDto>>> hotels(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> amenityNames,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        APICustomize<List<HotelResponseDto>> response = hotelService.hotels(accountId, districtId, name, minPrice, maxPrice, amenityNames, pageIndex, pageSize, sortBy, sortOrder);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/owner/hotel")
    public ResponseEntity<APICustomize<HotelResponseDto>> create(@ModelAttribute AddHotelRequestDto requestDto,
                                                                  @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<HotelResponseDto> response = hotelService.create(requestDto, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
