package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;

public interface BookingService {

    APICustomize<StripeResponseDto> createBooking(AddBookingRequestDto requestDto, Long hotelId, String token);

    APICustomize<Void> cancelBooking(Long bookingId, String token);

    APICustomize<Void> confirmBooking(Long bookingId, String token);
        
}
