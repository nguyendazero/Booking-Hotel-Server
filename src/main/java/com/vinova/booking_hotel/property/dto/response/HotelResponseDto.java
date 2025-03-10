package com.vinova.booking_hotel.property.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerDay;
    private String highLightImageUrl;
    private String streetAddress;
    private String latitude;
    private String longitude;
    private ImageResponseDto images;
    private Double rating;
}
