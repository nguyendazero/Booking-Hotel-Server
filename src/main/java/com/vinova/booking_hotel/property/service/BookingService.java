package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;

public interface BookingService {

    APICustomize<BookingResponseDto> createBooking(AddBookingRequestDto requestDto, Long hotelId, String token);

    APICustomize<BookingResponseDto> cancelBooking(Long bookingId, String token);
        
}
