package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;

import java.util.List;

public interface RatingService {

   List<RatingResponseDto> ratingsByHotelId(Long hotelId);
    
    RatingResponseDto rating(Long id);
    
    RatingResponseDto create(AddRatingRequestDto requestDto, String token);

    Void delete(Long id, String token);
    
}
