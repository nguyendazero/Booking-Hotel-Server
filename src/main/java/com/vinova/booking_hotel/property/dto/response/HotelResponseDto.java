package com.vinova.booking_hotel.property.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    private Double rating;
    private Long reviews;
    private List<ImageResponseDto> images;
    private List<DateRangeResponseDto> bookedDates;
}
