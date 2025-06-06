package com.vinova.booking_hotel.property.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponseDto {
    private Long id;
    private BigDecimal rate;
}
