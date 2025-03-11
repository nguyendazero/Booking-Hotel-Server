package com.vinova.booking_hotel.property.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeResponseDto {
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
}
