package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;
import com.vinova.booking_hotel.property.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<RatingResponseDto>> ratingsByHotelId(@PathVariable Long hotelId) {
        List<RatingResponseDto> response = ratingService.ratingsByHotelId(hotelId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping("/public/rating/{id}")
    public ResponseEntity<RatingResponseDto> rating(@PathVariable Long id) {
        RatingResponseDto response = ratingService.rating(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/user/hotel/rating")
    public ResponseEntity<RatingResponseDto> create(@ModelAttribute AddRatingRequestDto requestDto,
                                                                  @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        RatingResponseDto response = ratingService.create(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/user/rating/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        Void response = ratingService.delete(id, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
