package com.vinova.booking_hotel.authentication.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String fullName;
    private String username;
    private String email;
    private String password;
    private String rePassword;
}

