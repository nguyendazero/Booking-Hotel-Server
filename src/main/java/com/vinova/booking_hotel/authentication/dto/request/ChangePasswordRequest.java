package com.vinova.booking_hotel.authentication.dto.request;

import com.vinova.booking_hotel.authentication.dto.validation.annotation.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    
    private String oldPassword;
    
    @ValidPassword(message = "Invalid password. Must be at least 8 characters, number, including uppercase, lowercase, and special characters.")
    private String newPassword;
    
    @ValidPassword(message = "Invalid re-password. Must be at least 8 characters, number, including uppercase, lowercase, and special characters.")
    private String rePassword;
}
