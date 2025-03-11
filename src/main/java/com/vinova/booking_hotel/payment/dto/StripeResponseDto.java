package com.vinova.booking_hotel.payment.dto;

import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeResponseDto {
    private BookingResponseDto booking;
    private String sessionId;
    private String sessionUrl;
}
