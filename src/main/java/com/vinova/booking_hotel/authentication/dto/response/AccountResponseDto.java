package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String avatar;
    private String phone;
    private String blockReason;
    private List<String> roles;
}
