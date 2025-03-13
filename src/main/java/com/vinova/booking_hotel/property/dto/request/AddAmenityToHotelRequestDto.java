package com.vinova.booking_hotel.property.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAmenityToHotelRequestDto {
    private Long hotelId;
    private String nameAmenity;
}
