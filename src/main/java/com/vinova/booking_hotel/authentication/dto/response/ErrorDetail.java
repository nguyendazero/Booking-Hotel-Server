package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    private String errorMessageId;
    private String errorMessage;
}