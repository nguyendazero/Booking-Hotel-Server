package com.vinova.booking_hotel.authentication.dto.request;

import com.vinova.booking_hotel.authentication.validation.annotation.ValidFullName;
import com.vinova.booking_hotel.authentication.validation.annotation.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInfoRequest {
    
    @ValidFullName
    private String fullName;
    
    @ValidPhoneNumber
    private String phone;
    
    private MultipartFile avatar;
}
