package com.vinova.booking_hotel.authentication.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APICustomize<T> {
    private String statusCode;
    private  String message;
    private T result ;
}
