package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String timestamp;
    private String path;
    private List<ErrorDetail> errors;
}
