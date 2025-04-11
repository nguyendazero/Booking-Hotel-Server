package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import com.vinova.booking_hotel.property.service.BookingService;
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
public class BookingController {
    
    private final BookingService bookingService;

    @PostMapping("/user/booking")
    public ResponseEntity<StripeResponseDto> bookHotel(@RequestBody AddBookingRequestDto requestDto,
                                                                     @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        StripeResponseDto response = bookingService.createBooking(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/user/booking/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId,
                                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        Void response = bookingService.cancelBooking(bookingId, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/owner/booking/{bookingId}/confirm")
    public ResponseEntity<Void> confirmBooking(@PathVariable Long bookingId,
                                                             @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        Void response = bookingService.confirmBooking(bookingId, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @GetMapping("/user/bookings")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByToken(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<BookingResponseDto> response = bookingService.getBookingsByToken(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/bookings/reservations")
    public ResponseEntity<List<BookingResponseDto>> getReservations(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<BookingResponseDto> response = bookingService.getReservations(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/owner/bookings/hotel/{hotelId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByHotelId(@PathVariable Long hotelId,
                                                                         @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<BookingResponseDto> response = bookingService.getBookingsByHotelId(hotelId, accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/owner/reservations/hotel/{hotelId}")
    public ResponseEntity<List<BookingResponseDto>> getReservationsByHotelId(@PathVariable Long hotelId,
                                                                             @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<BookingResponseDto> response = bookingService.getReservationsByHotelId(hotelId, accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/admin/bookings")
    public ResponseEntity<List<BookingResponseDto>> getAllBooking() {
        List<BookingResponseDto> response = bookingService.getAllBooking();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
}
