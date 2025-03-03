package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponseDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private List<String> roles;
    private String jwtToken;
    private String refreshToken;
}

