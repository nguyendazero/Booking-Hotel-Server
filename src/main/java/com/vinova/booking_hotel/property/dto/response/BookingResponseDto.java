package com.vinova.booking_hotel.property.dto.response;

import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long id;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private BigDecimal totalPrice;
    private String status;
    private ZonedDateTime createDt;
    private HotelResponseDto hotel;
    private AccountResponseDto account;
}
