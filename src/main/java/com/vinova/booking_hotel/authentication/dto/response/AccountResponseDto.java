package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;
import java.time.ZonedDateTime;
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
    private List<String> roles;
    private boolean enabled;
    private ZonedDateTime createdDt;
    private ZonedDateTime updatedDt;
}
