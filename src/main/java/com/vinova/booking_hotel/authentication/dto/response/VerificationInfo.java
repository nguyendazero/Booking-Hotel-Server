package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationInfo {
    private String verificationCode;
    private LocalDateTime sentTime;
    private String username;
    private String fullName;
}

