package com.vinova.booking_hotel.property.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddHotelRequestDto {
    private String name;
    private String description;
    private BigDecimal pricePerDay;
    private MultipartFile highLightImageUrl;
    private String streetAddress;
    private String latitude;
    private String longitude;
    private Long districtId;
}
