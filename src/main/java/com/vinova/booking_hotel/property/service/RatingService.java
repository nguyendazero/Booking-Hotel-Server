package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;

import java.util.List;

public interface RatingService {

    APICustomize<List<RatingResponseDto>> ratingsByHotelId(Long hotelId);
    
    APICustomize<RatingResponseDto> rating(Long id);
    
    APICustomize<RatingResponseDto> create(AddRatingRequestDto requestDto, Long hotelId, String token);

    APICustomize<Void> delete(Long id, String token);
    
}
