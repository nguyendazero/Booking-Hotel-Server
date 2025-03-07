package com.vinova.booking_hotel.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRegistrationDto {
    private Long id;
    private AccountResponseDto account;
    private String status;
}
