package com.vinova.booking_hotel.authentication.security;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
}
