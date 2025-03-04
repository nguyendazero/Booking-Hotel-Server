package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponseDto {
    private String accessToken;
    private String refreshToken;
}

