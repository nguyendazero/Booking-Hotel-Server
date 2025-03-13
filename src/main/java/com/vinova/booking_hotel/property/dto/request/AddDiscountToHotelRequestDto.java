package com.vinova.booking_hotel.property.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDiscountToHotelRequestDto {
    private Long hotelId;
    private BigDecimal rate;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
}
