package com.vinova.booking_hotel.property.dto.request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBookingRequestDto {
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
}
