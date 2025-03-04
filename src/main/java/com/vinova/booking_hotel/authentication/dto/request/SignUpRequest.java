package com.vinova.booking_hotel.authentication.dto.request;

import com.vinova.booking_hotel.authentication.validation.annotation.ValidPassword;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    
    private String fullName;
    private String username;
    private String email;
    @ValidPassword(message = "Invalid password. Must be at least 8 characters, number, including uppercase, lowercase, and special characters.")
    private String password;
    @ValidPassword(message = "Invalid re-password. Must be at least 8 characters, number, including uppercase, lowercase, and special characters.")
    private String rePassword;
}

