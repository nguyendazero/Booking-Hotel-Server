package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;

import java.util.List;

public interface BookingService {

    StripeResponseDto createBooking(AddBookingRequestDto requestDto, String token);

    Void cancelBooking(Long bookingId, String token);

    Void confirmBooking(Long bookingId, String token);
    
    List<BookingResponseDto> getBookingsByToken(String token);

    List<BookingResponseDto> getReservations(String token);

    List<BookingResponseDto> getBookingsByHotelId(Long hotelId,String token);

    List<BookingResponseDto> getReservationsByHotelId(Long hotelId,String token);

    List<BookingResponseDto> getAllBooking();
        
}
