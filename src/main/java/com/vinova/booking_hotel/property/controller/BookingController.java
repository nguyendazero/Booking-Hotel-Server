package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import com.vinova.booking_hotel.property.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;

    @PostMapping("/user/booking")
    public ResponseEntity<APICustomize<StripeResponseDto>> bookHotel(@RequestBody AddBookingRequestDto requestDto,
                                                                     @RequestParam Long hotelId,
                                                                     @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<StripeResponseDto> response = bookingService.createBooking(requestDto, hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PatchMapping("/user/booking/{bookingId}/cancel")
    public ResponseEntity<APICustomize<Void>> cancelBooking(@PathVariable Long bookingId,
                                                                          @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<Void> response = bookingService.cancelBooking(bookingId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PatchMapping("/owner/booking/{bookingId}/confirm")
    public ResponseEntity<APICustomize<Void>> confirmBooking(@PathVariable Long bookingId,
                                                             @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<Void> response = bookingService.confirmBooking(bookingId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
    @GetMapping("/user/bookings")
    public ResponseEntity<APICustomize<List<BookingResponseDto>>> getBookingsByToken(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<List<BookingResponseDto>> response = bookingService.getBookingsByToken(accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/user/bookings/reservations")
    public ResponseEntity<APICustomize<List<BookingResponseDto>>> getReservations(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<List<BookingResponseDto>> response = bookingService.getReservations(accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/owner/bookings/hotel/{hotelId}")
    public ResponseEntity<APICustomize<List<BookingResponseDto>>> getBookingsByHotelId(@PathVariable Long hotelId,
                                                                                       @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<List<BookingResponseDto>> response = bookingService.getBookingsByHotelId(hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/owner/reservations/hotel/{hotelId}")
    public ResponseEntity<APICustomize<List<BookingResponseDto>>> getReservationsByHotelId(@PathVariable Long hotelId,
                                                                                           @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<List<BookingResponseDto>> response = bookingService.getReservationsByHotelId(hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/admin/bookings")
    public ResponseEntity<APICustomize<List<BookingResponseDto>>> getAllBooking() {
        APICustomize<List<BookingResponseDto>> response = bookingService.getAllBooking();
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
