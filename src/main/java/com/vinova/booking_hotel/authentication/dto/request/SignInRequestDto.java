package com.vinova.booking_hotel.authentication.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequestDto {
    private String usernameOrEmail;
    private String password;
}

