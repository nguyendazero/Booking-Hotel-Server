package com.vinova.booking_hotel.property.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDiscountRequestDto {
    private BigDecimal rate;
}
