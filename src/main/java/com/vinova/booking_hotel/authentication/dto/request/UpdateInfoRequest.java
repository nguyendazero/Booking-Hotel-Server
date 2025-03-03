package com.vinova.booking_hotel.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInfoRequest {
    private String fullName;
    private MultipartFile avatar;
}
