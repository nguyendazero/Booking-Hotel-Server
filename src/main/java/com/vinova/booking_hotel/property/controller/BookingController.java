package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import com.vinova.booking_hotel.property.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;

    @PostMapping("/user/booking")
    public ResponseEntity<APICustomize<BookingResponseDto>> bookHotel(@RequestBody AddBookingRequestDto requestDto,
                                                           @RequestParam Long hotelId,
                                                           @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<BookingResponseDto> response = bookingService.createBooking(requestDto, hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
