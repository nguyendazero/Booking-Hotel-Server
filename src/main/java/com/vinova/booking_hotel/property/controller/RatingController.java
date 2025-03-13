package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;
import com.vinova.booking_hotel.property.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RatingController {
    
    private final RatingService ratingService;
    
    @GetMapping("/public/hotel/{hotelId}/ratings")
    public ResponseEntity<APICustomize<List<RatingResponseDto>>> ratingsByHotelId(@PathVariable Long hotelId) {
        APICustomize<List<RatingResponseDto>> response = ratingService.ratingsByHotelId(hotelId);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
    @GetMapping("/public/rating/{id}")
    public ResponseEntity<APICustomize<RatingResponseDto>> rating(@PathVariable Long id) {
        APICustomize<RatingResponseDto> response = ratingService.rating(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
    @PostMapping("/user/hotel/rating")
    public ResponseEntity<APICustomize<RatingResponseDto>> create(@RequestBody AddRatingRequestDto requestDto,
                                                                  @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<RatingResponseDto> response = ratingService.create(requestDto, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
    @DeleteMapping("/user/rating/{id}")
    public ResponseEntity<APICustomize<Void>> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<Void> response = ratingService.delete(id, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
    
}
