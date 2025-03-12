package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;

import java.util.List;

public interface BookingService {

    APICustomize<StripeResponseDto> createBooking(AddBookingRequestDto requestDto, Long hotelId, String token);

    APICustomize<Void> cancelBooking(Long bookingId, String token);

    APICustomize<Void> confirmBooking(Long bookingId, String token);
    
    APICustomize<List<BookingResponseDto>> getBookingsByToken(String token);

    APICustomize<List<BookingResponseDto>> getReservations(String token);

    APICustomize<List<BookingResponseDto>> getBookingsByHotelId(Long hotelId,String token);

    APICustomize<List<BookingResponseDto>> getReservationsByHotelId(Long hotelId,String token);

    APICustomize<List<BookingResponseDto>> getAllBooking();
        
}
