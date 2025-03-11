package com.vinova.booking_hotel.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
}
