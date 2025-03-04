package com.vinova.booking_hotel.authentication.dto.request;

import com.vinova.booking_hotel.authentication.validation.annotation.ValidPassword;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    private String email;
    private String code;
    @ValidPassword(message = "Invalid password. Must be at least 8 characters, number, including uppercase, lowercase, and special characters.")
    private String newPassword;
    @ValidPassword(message = "Invalid password. Must be at least 8 characters, number, including uppercase, lowercase, and special characters.")
    private String rePassword;
    
}
