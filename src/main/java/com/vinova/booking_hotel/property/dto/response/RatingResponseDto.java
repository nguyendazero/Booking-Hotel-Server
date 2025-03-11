package com.vinova.booking_hotel.property.dto.response;

import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto {
    private Long id;
    private Integer stars;
    private String content;
    private ZonedDateTime createDt;
    private AccountResponseDto account;
}
