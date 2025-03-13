package com.vinova.booking_hotel.property.dto.response;

import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto {
    private Long id;
    private Integer stars;
    private String content;
    private ZonedDateTime createDt;
    private List<ImageResponseDto> images;
    private AccountResponseDto account;
}
